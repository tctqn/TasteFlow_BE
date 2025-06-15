package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.OrderRequestDTO;
import com.startup.tasteflowbe.dto.response.CreatePaymentResponseDTO;
import com.startup.tasteflowbe.dto.response.OrderResponseDTO;
import com.startup.tasteflowbe.enums.OrderStatus;
import com.startup.tasteflowbe.mapper.OrderMapper;
import com.startup.tasteflowbe.model.*;
import com.startup.tasteflowbe.enums.MovementType;
import com.startup.tasteflowbe.repository.*;
import com.startup.tasteflowbe.service.OrderService;
import com.startup.tasteflowbe.service.PaymentService;
import com.startup.tasteflowbe.utils.OrderCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    private final StockMovementRepository stockMovementRepository;
    private final OrderMapper orderMapper;
    private final PaymentService paymentService;
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
                    existingOrder.setVouchers(order.getVouchers());
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
    public Order checkoutFromCartItems(Long userId, List<Long> cartItemIds, List<Long> voucherIds, Long shippingAddressId, Long storeId) {
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
                    maxPromotionDiscount.divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP)));

            // Truy vấn các inventory còn hàng, chưa hết hạn theo batch gần hết hạn trước tại cửa hàng
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

                // Ghi nhận chuyển động tồn kho
                StockMovement stockMovement = new StockMovement();
                stockMovement.setWarehouse(inv.getWarehouse());
                stockMovement.setStore(storeRepository.findByStoreId(storeId));
                stockMovement.setProduct(product);
                stockMovement.setBatch(inv.getBatch());
                stockMovement.setMovementType(MovementType.SALE);  // Giảm tồn kho
                stockMovement.setQuantity(usableQty);
                stockMovement.setNote("Đặt hàng cho sản phẩm: " + product.getName());
                stockMovementRepository.save(stockMovement);

                remainingQty -= usableQty;
            }

            if (remainingQty > 0) {
                throw new IllegalArgumentException("Không đủ hàng cho sản phẩm: " + product.getName() + " tại cửa hàng.");
            }

            // Cộng tổng tiền
            totalPrice = totalPrice.add(discountedPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        // Tính giảm giá từ các voucher (nếu có)
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (Long voucherId : voucherIds) {
            Voucher voucher = voucherRepository.findById(voucherId)
                    .orElseThrow(() -> new IllegalArgumentException("Voucher không hợp lệ."));

            if (voucher.getStartDate().isAfter(LocalDateTime.now()) || voucher.getEndDate().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Voucher đã hết hạn hoặc chưa bắt đầu.");
            }

            BigDecimal voucherDiscount = BigDecimal.ZERO;

            if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
                // Giảm giá phần trăm: tính trên giá trị đã giảm sau các voucher trước đó
                voucherDiscount = totalPrice.multiply(voucher.getDiscountAmount()
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            } else if ("AMOUNT".equalsIgnoreCase(voucher.getDiscountType())) {
                // Giảm giá tiền mặt
                voucherDiscount = voucher.getDiscountAmount();
            }

            // Kiểm tra nếu giảm giá quá giá trị tổng đơn hàng thì chỉ giảm giá bằng tổng giá trị đơn hàng
            if (voucherDiscount.compareTo(totalPrice) > 0) {
                voucherDiscount = totalPrice;
            }

            // Cộng dồn giảm giá từ các voucher
            totalDiscount = totalDiscount.add(voucherDiscount);
        }

        // Áp dụng tổng giảm giá vào totalPrice
        totalPrice = totalPrice.subtract(totalDiscount);

        // Tạo đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(totalPrice);
        order.setVouchers(voucherRepository.findByVoucherIdIn(voucherIds));
        order.setVoucherDiscount(totalDiscount);
        order.setShippingAddress(shippingAddressRepository.findByAddressId(shippingAddressId));
        order.setStore(storeRepository.findByStoreId(storeId));

        order = orderRepository.save(order);

        // Gán order cho từng order item và lưu
        for (OrderItem orderItem : allOrderItems) {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        }

        // Xử lý thanh toán

        // Xóa cart items đã xử lý
        cartItemRepository.deleteAll(cartItems);

        return order;
    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {

        // 1️⃣ Lấy userId từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = (String) authentication.getPrincipal();
            user = userRepository.findByUsername(username).orElse(null);
        }
        System.out.println("Current user: " + (user != null ? user.getUsername() : "Guest"));

        // 2️⃣ Map DTO → Entity
        Order order = orderMapper.toEntity(dto);
        order.setUser(user);

        // 3️⃣ Handle Invoice nếu có
        if (dto.isNeedInvoice() && dto.getInvoiceInfo() != null) {
            order.setInvoiceCompanyName(dto.getInvoiceInfo().getCompanyName());
            order.setInvoiceEmail(dto.getInvoiceInfo().getEmail());
            order.setInvoiceTaxCode(dto.getInvoiceInfo().getTaxCode());
            order.setInvoiceCompanyAddress(dto.getInvoiceInfo().getCompanyAddress());
        }
        order.setOrderDate(LocalDateTime.now());
        order.setOrderCode(OrderCodeGenerator.generateOrderCode());
        order = orderRepository.save(order);
        return orderMapper.toDto(order);
    }




    @Override
    public CreatePaymentResponseDTO handleOnlinePayment(OrderResponseDTO order) {
        Long amount = order.getTotalPrice().longValue();
        String description = "Thanh toán đơn hàng";
        return paymentService.createPayment(    Long.parseLong(order.getOrderCode()), amount, description);
    }

    @Override
    public void markOrderAsPaid(Long orderCode) {
        Optional<Order> optionalOrder = orderRepository.findByOrderCode(orderCode.toString());
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
        }
    }

}
