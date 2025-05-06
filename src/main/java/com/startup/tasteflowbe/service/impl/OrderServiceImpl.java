package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.*;
import com.startup.tasteflowbe.repository.*;
import com.startup.tasteflowbe.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final PromotionRepository promotionRepository;

    private final CartItemRepository cartItemRepository;

    private final VoucherRepository voucherRepository;

    private final UserRepository userRepository;

    private final ShippingAddressRepository shippingAddressRepository;

    private final StoreRepository storeRepository;

    private final InventoryRepository inventoryRepository;

    private final OrderItemRepository orderItemRepository;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrder(Long id, Order order) {
        return orderRepository.findById(id)
                .map(existingOrder -> {
                    existingOrder.setStatus(order.getStatus());
                    existingOrder.setTotalPrice(order.getTotalPrice());
                    existingOrder.setVoucher(order.getVoucher());
                    existingOrder.setVoucherDiscount(order.getVoucherDiscount());
                    existingOrder.setShippingAddress(order.getShippingAddress());
                    return orderRepository.save(existingOrder);
                })
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Order checkoutFromCartItems(Long userId, List<Long> cartItemIds, Long voucherId, Long shippingAddressId, Long storeId) {
        userId = 1L; // Test cố định
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));

        List<CartItem> cartItems = cartItemRepository.findByUser_UserIdAndCartItemIdIn(userId, cartItemIds);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng không hợp lệ.");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> allOrderItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            BigDecimal unitPrice = product.getPrice();

            // Tính giảm giá từ promotion (nếu có)
            List<Promotion> activePromotions = promotionRepository.findActivePromotionsByProductId(
                    product.getProductId(), LocalDateTime.now());

            BigDecimal maxPromotionDiscount = activePromotions.stream()
                    .map(Promotion::getDiscountPercentage)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            BigDecimal discountedPrice = unitPrice.multiply(BigDecimal.ONE.subtract(
                    maxPromotionDiscount.divide(BigDecimal.valueOf(100))));

            // Truy vấn các inventory còn hàng, chưa hết hạn theo batch, tại cửa hàng
            List<Inventory> inventories = inventoryRepository
                    .findByStore_StoreIdAndProduct_ProductIdAndQuantityGreaterThanAndBatch_ExpirationDateAfterOrderByBatch_ExpirationDateAsc(
                            storeId, product.getProductId(), 0, LocalDate.now());

            int remainingQty = quantity;

            for (Inventory inv : inventories) {
                if (remainingQty <= 0) break;

                int usableQty = Math.min(inv.getQuantity(), remainingQty);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(null); // gán sau khi tạo order
                orderItem.setProduct(product);
                orderItem.setQuantity(usableQty);
                orderItem.setPrice(discountedPrice.multiply(BigDecimal.valueOf(usableQty)));
                orderItem.setDiscount(unitPrice.subtract(discountedPrice).multiply(BigDecimal.valueOf(usableQty)));
                orderItem.setBatch(inv.getBatch());

                allOrderItems.add(orderItem);

                // Trừ tồn kho
                inv.setQuantity(inv.getQuantity() - usableQty);
                inventoryRepository.save(inv);

                remainingQty -= usableQty;
            }

            if (remainingQty > 0) {
                throw new IllegalArgumentException("Không đủ hàng cho sản phẩm: " + product.getName() + " tại cửa hàng.");
            }

            // Cộng tổng tiền
            totalPrice = totalPrice.add(discountedPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        // Tính giảm giá từ voucher (nếu có)
        Voucher voucher = null;
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        if (voucherId != null) {
            voucher = voucherRepository.findById(voucherId)
                    .orElseThrow(() -> new IllegalArgumentException("Voucher không hợp lệ."));

            if (voucher.getStartDate().isAfter(LocalDateTime.now()) || voucher.getEndDate().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Voucher đã hết hạn hoặc chưa bắt đầu.");
            }

            if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
                voucherDiscount = totalPrice.multiply(voucher.getDiscountAmount()
                        .divide(BigDecimal.valueOf(100)));
            } else if ("AMOUNT".equalsIgnoreCase(voucher.getDiscountType())) {
                voucherDiscount = voucher.getDiscountAmount();
            }

            if (voucherDiscount.compareTo(totalPrice) > 0) {
                voucherDiscount = totalPrice;
            }
        }
        totalPrice = totalPrice.subtract(voucherDiscount);

        // Tạo đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalPrice(totalPrice);
        order.setVoucher(voucher);
        order.setVoucherDiscount(voucherDiscount);
        order.setShippingAddress(shippingAddressRepository.findByAddressId(shippingAddressId));
        order.setStore(storeRepository.findByStoreId(storeId));

        order = orderRepository.save(order);

        // Gán order cho từng order item và lưu
        for (OrderItem orderItem : allOrderItems) {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        }

        // Xóa cart items đã xử lý
        cartItemRepository.deleteAll(cartItems);

        return order;
    }

}
