package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.CartItemDTO;
import com.startup.tasteflowbe.dto.request.OrderRequestDTO;
import com.startup.tasteflowbe.dto.response.CreatePaymentResponseDTO;
import com.startup.tasteflowbe.dto.response.OrderResponseDTO;
import com.startup.tasteflowbe.enums.OrderStatus;
import com.startup.tasteflowbe.mapper.OrderMapper;
import com.startup.tasteflowbe.model.*;
import com.startup.tasteflowbe.repository.*;
import com.startup.tasteflowbe.service.InvoiceService;
import com.startup.tasteflowbe.service.OrderService;
import com.startup.tasteflowbe.service.PaymentService;
import com.startup.tasteflowbe.service.S3Service;
import com.startup.tasteflowbe.utils.OrderCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final S3Service s3Service;

    private final InvoiceRepository invoiceRepository;

    private final VoucherRepository voucherRepository;

    private final InvoiceService invoiceService;

    private final UserRepository userRepository;

    private final OrderItemRepository orderItemRepository;

    private final ProductUnitRepository productUnitRepository;

    private final ProductRepository productRepository;

    private final OrderMapper orderMapper;

    private final PaymentService paymentService;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order findByOrderCode(Long orderCode) {
        return orderRepository.findByOrderCode(orderCode.toString())
                .orElseThrow(() -> new RuntimeException("Order not found with code " + orderCode));
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> getAllStoreOrders(Long id) {
        return orderRepository.findByStore_StoreId(id);
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
    public OrderResponseDTO updateOrderStatus(Long id, String status, String notes) {
        return orderRepository.findById(id)
                .map(od -> {
                    od.setStatus(OrderStatus.valueOf(status));
                    od.setNote(notes);
                    Order savedOrder = orderRepository.save(od);
                    return orderMapper.toDto(savedOrder);
                })
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

//    @Override
//    @Transactional
//    public Order checkoutFromCartItems(Long userId, List<Long> cartItemIds, List<Long> voucherIds, Long shippingAddressId, Long storeId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));
//
//        List<CartItem> cartItems = cartItemRepository.findByUser_UserIdAndCartItemIdIn(userId, cartItemIds);
//        if (cartItems.isEmpty()) {
//            throw new IllegalArgumentException("Giỏ hàng không hợp lệ.");
//        }
//
//        BigDecimal totalPrice = BigDecimal.ZERO;
//        List<OrderItem> allOrderItems = new ArrayList<>();
//
//        for (CartItem item : cartItems) {
//            Product product = item.getProduct();
//            int quantity = item.getQuantity();
//            BigDecimal unitPrice = product.getPrice();
//
//            // Tính giảm giá từ promotion (nếu có)
//            List<Promotion> activePromotions = promotionRepository.findActivePromotionsByProductId(
//                    product.getProductId(), LocalDateTime.now());
//
//            BigDecimal maxPromotionDiscount = activePromotions.stream()
//                    .map(Promotion::getDiscountPercentage)
//                    .max(BigDecimal::compareTo)
//                    .orElse(BigDecimal.ZERO);
//
//            BigDecimal discountedPrice = unitPrice.multiply(BigDecimal.ONE.subtract(
//                    maxPromotionDiscount.divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP)));
//
//            // Truy vấn các inventory còn hàng, chưa hết hạn theo batch gần hết hạn trước tại cửa hàng
//            List<Inventory> inventories = inventoryRepository
//                    .findByStore_StoreIdAndProduct_ProductIdAndQuantityGreaterThanAndBatch_ExpirationDateAfterOrderByBatch_ExpirationDateAsc(
//                            storeId, product.getProductId(), 0, LocalDate.now());
//
//            int remainingQty = quantity;
//
//            for (Inventory inv : inventories) {
//                if (remainingQty <= 0) break;
//
//                int usableQty = Math.min(inv.getQuantity(), remainingQty);
//
//                OrderItem orderItem = new OrderItem();
//                orderItem.setOrder(null); // gán sau khi tạo order
//                orderItem.setProduct(product);
//                orderItem.setQuantity(usableQty);
//                orderItem.setPrice(discountedPrice.multiply(BigDecimal.valueOf(usableQty)));
//                orderItem.setDiscount(unitPrice.subtract(discountedPrice).multiply(BigDecimal.valueOf(usableQty)));
//                orderItem.setBatch(inv.getBatch());
//
//                allOrderItems.add(orderItem);
//
//                // Trừ tồn kho
//                inv.setQuantity(inv.getQuantity() - usableQty);
//                inventoryRepository.save(inv);
//
//                // Ghi nhận chuyển động tồn kho
//                StockMovement stockMovement = new StockMovement();
//                stockMovement.setWarehouse(inv.getWarehouse());
//                stockMovement.setStore(storeRepository.findByStoreId(storeId));
//                stockMovement.setProduct(product);
//                stockMovement.setBatch(inv.getBatch());
//                stockMovement.setMovementType(MovementType.SALE);  // Giảm tồn kho
//                stockMovement.setQuantity(usableQty);
//                stockMovement.setNote("Đặt hàng cho sản phẩm: " + product.getName());
//                stockMovementRepository.save(stockMovement);
//
//                remainingQty -= usableQty;
//            }
//
//            if (remainingQty > 0) {
//                throw new IllegalArgumentException("Không đủ hàng cho sản phẩm: " + product.getName() + " tại cửa hàng.");
//            }
//
//            // Cộng tổng tiền
//            totalPrice = totalPrice.add(discountedPrice.multiply(BigDecimal.valueOf(quantity)));
//        }
//
//        // Tính giảm giá từ các voucher (nếu có)
//        BigDecimal totalDiscount = BigDecimal.ZERO;
//        for (Long voucherId : voucherIds) {
//            Voucher voucher = voucherRepository.findById(voucherId)
//                    .orElseThrow(() -> new IllegalArgumentException("Voucher không hợp lệ."));
//
//            if (voucher.getStartDate().isAfter(LocalDateTime.now()) || voucher.getEndDate().isBefore(LocalDateTime.now())) {
//                throw new IllegalArgumentException("Voucher đã hết hạn hoặc chưa bắt đầu.");
//            }
//
//            BigDecimal voucherDiscount = BigDecimal.ZERO;
//
//            if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType().toString())) {
//                // Giảm giá phần trăm: tính trên giá trị đã giảm sau các voucher trước đó
//                voucherDiscount = totalPrice.multiply(voucher.getDiscountAmount()
//                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
//            } else if ("AMOUNT".equalsIgnoreCase(voucher.getDiscountType().toString())) {
//                // Giảm giá tiền mặt
//                voucherDiscount = voucher.getDiscountAmount();
//            }
//
//            // Kiểm tra nếu giảm giá quá giá trị tổng đơn hàng thì chỉ giảm giá bằng tổng giá trị đơn hàng
//            if (voucherDiscount.compareTo(totalPrice) > 0) {
//                voucherDiscount = totalPrice;
//            }
//
//            // Cộng dồn giảm giá từ các voucher
//            totalDiscount = totalDiscount.add(voucherDiscount);
//        }
//
//        // Áp dụng tổng giảm giá vào totalPrice
//        totalPrice = totalPrice.subtract(totalDiscount);
//
//        // Tạo đơn hàng
//        Order order = new Order();
//        order.setUser(user);
//        order.setOrderDate(LocalDateTime.now());
//        order.setTotalPrice(totalPrice);
//        order.setVouchers(voucherRepository.findByVoucherIdIn(voucherIds));
//        order.setVoucherDiscount(totalDiscount);
//        order.setShippingAddress(shippingAddressRepository.findByAddressId(shippingAddressId));
//        order.setStore(storeRepository.findByStoreId(storeId));
//
//        order = orderRepository.save(order);
//
//        // Gán order cho từng order item và lưu
//        for (OrderItem orderItem : allOrderItems) {
//            orderItem.setOrder(order);
//            orderItemRepository.save(orderItem);
//        }
//
//        // Xử lý thanh toán
//
//        // Xóa cart items đã xử lý
//        cartItemRepository.deleteAll(cartItems);
//
//        return order;
//    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        // 1. Lấy thông tin user hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = (String) authentication.getPrincipal();
            user = userRepository.findByUsername(username).orElse(null);
        }

        // 2. Tạo Order entity
        Order order = orderMapper.toEntity(dto);
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderCode(OrderCodeGenerator.generateOrderCode());

        // 4. Tính tổng tiền và giảm giá từ voucher
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItemDTO itemDTO : dto.getCartItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            BigDecimal originalPrice = itemDTO.getPrice();
            BigDecimal finalPrice = originalPrice;
            BigDecimal discount = BigDecimal.ZERO;

            // Áp dụng giảm giá từ voucher nếu có
            if (dto.getVoucherIds() != null) {
                for (Long voucherId : dto.getVoucherIds()) {
                    Voucher voucher = voucherRepository.findById(voucherId)
                            .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + voucherId));

                    if (voucher.getStartDate().isAfter(LocalDateTime.now()) || voucher.getEndDate().isBefore(LocalDateTime.now())) {
                        continue; // bỏ qua nếu voucher không hợp lệ thời gian
                    }

                    // Nếu voucher áp dụng cho sản phẩm hiện tại
                    if (voucher.getApplicableProducts().contains(product)
                            || voucher.getApplicableCategories().contains(product.getCategory())) {
                        if (voucher.getDiscountType().name().equals("PERCENT")) {
                            BigDecimal percent = voucher.getDiscountPercent() != null ? voucher.getDiscountPercent() : BigDecimal.ZERO;
                            BigDecimal currentDiscount = originalPrice.multiply(percent).divide(BigDecimal.valueOf(100));
                            if (currentDiscount.compareTo(discount) > 0) {
                                discount = currentDiscount;
                            }
                        } else if (voucher.getDiscountType().name().equals("AMOUNT")) {
                            if (voucher.getDiscountAmount().compareTo(discount) > 0) {
                                discount = voucher.getDiscountAmount();
                            }
                        }
                    }
                }
            }

            finalPrice = originalPrice.subtract(discount);
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalPrice = BigDecimal.ZERO;
            }

            ProductUnit productUnit = productUnitRepository.findById(itemDTO.getProductUnitId()).get();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(finalPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            orderItem.setDiscount(discount.multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            orderItem.setQuantityInBase(itemDTO.getQuantity());
            orderItem.setProductUnit(productUnit);

            orderItems.add(orderItem);
            totalPrice = totalPrice.add(orderItem.getPrice());
        }

        order.setOrderItems(orderItems);

        // 5. Gán lại tổng giá và voucher vào Order
        order.setTotalPrice(totalPrice);
        if (dto.getVoucherIds() != null && !dto.getVoucherIds().isEmpty()) {
            order.setVouchers(voucherRepository.findByVoucherIdIn(dto.getVoucherIds()));
        }

        order = orderRepository.save(order);


        // 6. Lưu orderItems
        orderItemRepository.saveAll(orderItems);

        if (dto.isNeedInvoice() && dto.getInvoiceInfo() != null) {
            Invoice invoice = new Invoice();
            invoice.setOrder(order);
            invoice.setInvoiceCompanyName(dto.getInvoiceInfo().getCompanyName());
            invoice.setInvoiceEmail(dto.getInvoiceInfo().getEmail());
            invoice.setInvoiceTaxCode(dto.getInvoiceInfo().getTaxCode());
            invoice.setInvoiceCompanyAddress(dto.getInvoiceInfo().getCompanyAddress());
            invoice.setTotalAmount(dto.getTotalPrice());

            // 7.1 Generate PDF
            byte[] pdfBytes = null; // gọi service render PDF
            try {
                pdfBytes = invoiceService.generateInvoicePdf(order, invoice);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // 7.2 Upload PDF lên S3 và lấy link
            String invoiceUrl = s3Service.uploadInvoice(order.getOrderCode(), pdfBytes);

            // 7.3 Lưu link vào Invoice
            invoice.setInvoiceUrl(invoiceUrl);
            order.setInvoice(invoice);
            // 7.4 Lưu Invoice
            invoiceRepository.save(invoice);
        }
        orderRepository.save(order);

        return orderMapper.toDto(order);
    }

    @Override
    public CreatePaymentResponseDTO handleOnlinePayment(OrderResponseDTO order) {
        Long amount = order.getTotalPrice().longValue();
        String description = "Thanh toán đơn hàng";
        return paymentService.createPayment(Long.parseLong(order.getOrderCode()), amount, description);
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
