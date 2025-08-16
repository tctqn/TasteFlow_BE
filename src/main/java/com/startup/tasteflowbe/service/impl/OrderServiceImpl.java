package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.CartItemDTO;
import com.startup.tasteflowbe.dto.request.OrderRequestDTO;
import com.startup.tasteflowbe.dto.request.StoreOrderDTO;
import com.startup.tasteflowbe.dto.response.CreatePaymentResponseDTO;
import com.startup.tasteflowbe.dto.response.OrderResponseDTO;
import com.startup.tasteflowbe.enums.DiscountType;
import com.startup.tasteflowbe.enums.MovementType;
import com.startup.tasteflowbe.enums.NotificationType;
import com.startup.tasteflowbe.enums.OrderStatus;
import com.startup.tasteflowbe.enums.PaymentMethod;
import com.startup.tasteflowbe.mapper.OrderMapper;
import com.startup.tasteflowbe.model.*;
import com.startup.tasteflowbe.repository.*;
import com.startup.tasteflowbe.service.*;
import com.startup.tasteflowbe.utils.OrderCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final S3Service s3Service;

    private final InvoiceRepository invoiceRepository;
    private final StockMovementRepository stockMovementRepository;

    private final VoucherRepository voucherRepository;

    private final InvoiceService invoiceService;

    private final NotificationService notificationService;

    private final UserRepository userRepository;

    private final OrderItemRepository orderItemRepository;

    private final ProductUnitRepository productUnitRepository;

    private final ProductRepository productRepository;

    private final OrderMapper orderMapper;

    private final PaymentService paymentService;
    private final StoreService storeService;
    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

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
    public List<OrderResponseDTO> getAllOrdersByUserId(Long userId) {
        return Optional.ofNullable(orderRepository.findOrdersByUser_UserId(userId))
                .orElse(List.of())
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    // @Override
    // @Transactional
    // public Order checkoutFromCartItems(Long userId, List<Long> cartItemIds,
    // List<Long> voucherIds, Long shippingAddressId, Long storeId) {
    // User user = userRepository.findById(userId)
    // .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người
    // dùng."));
    //
    // List<CartItem> cartItems =
    // cartItemRepository.findByUser_UserIdAndCartItemIdIn(userId, cartItemIds);
    // if (cartItems.isEmpty()) {
    // throw new IllegalArgumentException("Giỏ hàng không hợp lệ.");
    // }
    //
    // BigDecimal totalPrice = BigDecimal.ZERO;
    // List<OrderItem> allOrderItems = new ArrayList<>();
    //
    // for (CartItem item : cartItems) {
    // Product product = item.getProduct();
    // int quantity = item.getQuantity();
    // BigDecimal unitPrice = product.getPrice();
    //
    // // Tính giảm giá từ promotion (nếu có)
    // List<Promotion> activePromotions =
    // promotionRepository.findActivePromotionsByProductId(
    // product.getProductId(), LocalDateTime.now());
    //
    // BigDecimal maxPromotionDiscount = activePromotions.stream()
    // .map(Promotion::getDiscountPercentage)
    // .max(BigDecimal::compareTo)
    // .orElse(BigDecimal.ZERO);
    //
    // BigDecimal discountedPrice = unitPrice.multiply(BigDecimal.ONE.subtract(
    // maxPromotionDiscount.divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP)));
    //
    // // Truy vấn các inventory còn hàng, chưa hết hạn theo batch gần hết hạn trước
    // tại cửa hàng
    // List<Inventory> inventories = inventoryRepository
    // .findByStore_StoreIdAndProduct_ProductIdAndQuantityGreaterThanAndBatch_ExpirationDateAfterOrderByBatch_ExpirationDateAsc(
    // storeId, product.getProductId(), 0, LocalDate.now());
    //
    // int remainingQty = quantity;
    //
    // for (Inventory inv : inventories) {
    // if (remainingQty <= 0) break;
    //
    // int usableQty = Math.min(inv.getQuantity(), remainingQty);
    //
    // OrderItem orderItem = new OrderItem();
    // orderItem.setOrder(null); // gán sau khi tạo order
    // orderItem.setProduct(product);
    // orderItem.setQuantity(usableQty);
    // orderItem.setPrice(discountedPrice.multiply(BigDecimal.valueOf(usableQty)));
    // orderItem.setDiscount(unitPrice.subtract(discountedPrice).multiply(BigDecimal.valueOf(usableQty)));
    // orderItem.setBatch(inv.getBatch());
    //
    // allOrderItems.add(orderItem);
    //
    // // Trừ tồn kho
    // inv.setQuantity(inv.getQuantity() - usableQty);
    // inventoryRepository.save(inv);
    //
    // // Ghi nhận chuyển động tồn kho
    // StockMovement stockMovement = new StockMovement();
    // stockMovement.setWarehouse(inv.getWarehouse());
    // stockMovement.setStore(storeRepository.findByStoreId(storeId));
    // stockMovement.setProduct(product);
    // stockMovement.setBatch(inv.getBatch());
    // stockMovement.setMovementType(MovementType.SALE); // Giảm tồn kho
    // stockMovement.setQuantity(usableQty);
    // stockMovement.setNote("Đặt hàng cho sản phẩm: " + product.getName());
    // stockMovementRepository.save(stockMovement);
    //
    // remainingQty -= usableQty;
    // }
    //
    // if (remainingQty > 0) {
    // throw new IllegalArgumentException("Không đủ hàng cho sản phẩm: " +
    // product.getName() + " tại cửa hàng.");
    // }
    //
    // // Cộng tổng tiền
    // totalPrice =
    // totalPrice.add(discountedPrice.multiply(BigDecimal.valueOf(quantity)));
    // }
    //
    // // Tính giảm giá từ các voucher (nếu có)
    // BigDecimal totalDiscount = BigDecimal.ZERO;
    // for (Long voucherId : voucherIds) {
    // Voucher voucher = voucherRepository.findById(voucherId)
    // .orElseThrow(() -> new IllegalArgumentException("Voucher không hợp lệ."));
    //
    // if (voucher.getStartDate().isAfter(LocalDateTime.now()) ||
    // voucher.getEndDate().isBefore(LocalDateTime.now())) {
    // throw new IllegalArgumentException("Voucher đã hết hạn hoặc chưa bắt đầu.");
    // }
    //
    // BigDecimal voucherDiscount = BigDecimal.ZERO;
    //
    // if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType().toString())) {
    // // Giảm giá phần trăm: tính trên giá trị đã giảm sau các voucher trước đó
    // voucherDiscount = totalPrice.multiply(voucher.getDiscountAmount()
    // .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    // } else if ("AMOUNT".equalsIgnoreCase(voucher.getDiscountType().toString())) {
    // // Giảm giá tiền mặt
    // voucherDiscount = voucher.getDiscountAmount();
    // }
    //
    // // Kiểm tra nếu giảm giá quá giá trị tổng đơn hàng thì chỉ giảm giá bằng tổng
    // giá trị đơn hàng
    // if (voucherDiscount.compareTo(totalPrice) > 0) {
    // voucherDiscount = totalPrice;
    // }
    //
    // // Cộng dồn giảm giá từ các voucher
    // totalDiscount = totalDiscount.add(voucherDiscount);
    // }
    //
    // // Áp dụng tổng giảm giá vào totalPrice
    // totalPrice = totalPrice.subtract(totalDiscount);
    //
    // // Tạo đơn hàng
    // Order order = new Order();
    // order.setUser(user);
    // order.setOrderDate(LocalDateTime.now());
    // order.setTotalPrice(totalPrice);
    // order.setVouchers(voucherRepository.findByVoucherIdIn(voucherIds));
    // order.setVoucherDiscount(totalDiscount);
    // order.setShippingAddress(shippingAddressRepository.findByAddressId(shippingAddressId));
    // order.setStore(storeRepository.findByStoreId(storeId));
    //
    // order = orderRepository.save(order);
    //
    // // Gán order cho từng order item và lưu
    // for (OrderItem orderItem : allOrderItems) {
    // orderItem.setOrder(order);
    // orderItemRepository.save(orderItem);
    // }
    //
    // // Xử lý thanh toán
    //
    // // Xóa cart items đã xử lý
    // cartItemRepository.deleteAll(cartItems);
    //
    // return order;
    // }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        // 1. Xác định User từ dto hoặc từ authentication
        User user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(Long.parseLong(dto.getUserId()))
                    .orElseThrow(() -> new RuntimeException("User not found with ID " + dto.getUserId()));
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = (String) auth.getPrincipal();
            user = userRepository.findByUsername(username).orElse(user);
        }

        // 2. Tạo đối tượng Order
        Order order = orderMapper.toEntity(dto);
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderCode(OrderCodeGenerator.generateOrderCode());

        Store store = storeService.getStoreById(dto.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found with ID " + dto.getStoreId()));
        order.setStore(store);

        // 3. Chuẩn bị dữ liệu voucher
        Map<Long, Voucher> voucherMap = new HashMap<>();
        if (dto.getVoucherIds() != null) {
            for (Long voucherId : dto.getVoucherIds()) {
                Voucher voucher = voucherRepository.findById(voucherId)
                        .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + voucherId));
                voucherMap.put(voucherId, voucher);
            }
        }

        // 4. Xử lý từng cart item => tạo OrderItem + tính giảm giá
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItemDTO itemDTO : dto.getCartItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            ProductUnit productUnit = productUnitRepository.findById(itemDTO.getProductUnitId()).orElseThrow();

            BigDecimal originalPrice = itemDTO.getPrice();
            BigDecimal bestDiscount = BigDecimal.ZERO;

            for (Voucher voucher : voucherMap.values()) {
                if (voucher.getStartDate().isAfter(LocalDateTime.now())
                        || voucher.getEndDate().isBefore(LocalDateTime.now())) {
                    continue;
                }

                boolean isApplicable = voucher.getApplicableProducts().contains(product)
                        || voucher.getApplicableCategories().contains(product.getCategory());

                if (!isApplicable)
                    continue;

                if (voucher.getDiscountType() == DiscountType.PERCENT && voucher.getDiscountPercent() != null) {
                    BigDecimal discount = originalPrice.multiply(voucher.getDiscountPercent())
                            .divide(BigDecimal.valueOf(100));
                    if (discount.compareTo(bestDiscount) > 0)
                        bestDiscount = discount;
                } else if (voucher.getDiscountType() == DiscountType.AMOUNT && voucher.getDiscountAmount() != null) {
                    if (voucher.getDiscountAmount().compareTo(bestDiscount) > 0)
                        bestDiscount = voucher.getDiscountAmount();
                }
            }

            BigDecimal finalPrice = originalPrice.subtract(bestDiscount).max(BigDecimal.ZERO);
            BigDecimal totalItemPrice = finalPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            BigDecimal totalDiscount = bestDiscount.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductUnit(productUnit);
            item.setQuantity(itemDTO.getQuantity());
            item.setQuantityInBase(itemDTO.getQuantity());
            item.setPrice(totalItemPrice);
            item.setDiscount(totalDiscount);

            orderItems.add(item);
            totalPrice = totalPrice.add(totalItemPrice);
        }

        // 5. Lưu order và orderItems
        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);
        if (!voucherMap.isEmpty()) {
            order.setVouchers(new ArrayList<>(voucherMap.values()));
        }

        order = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // 6. Nếu cần hóa đơn
        if (dto.isNeedInvoice() && dto.getInvoiceInfo() != null) {
            Invoice invoice = new Invoice();
            invoice.setOrder(order);
            invoice.setInvoiceCompanyName(dto.getInvoiceInfo().getCompanyName());
            invoice.setInvoiceEmail(dto.getInvoiceInfo().getEmail());
            invoice.setInvoiceTaxCode(dto.getInvoiceInfo().getTaxCode());
            invoice.setInvoiceCompanyAddress(dto.getInvoiceInfo().getCompanyAddress());
            invoice.setIssuedAt(LocalDateTime.now());
            invoice.setTotalAmount(order.getTotalPrice());

            try {
                byte[] pdfBytes = invoiceService.generateInvoicePdf(order, invoice);
                String invoiceUrl = s3Service.uploadInvoice(order.getOrderCode(), pdfBytes);
                invoice.setInvoiceUrl(invoiceUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi tạo hoặc upload PDF hóa đơn", e);
            }

            order.setInvoice(invoice);
            invoiceRepository.save(invoice);
        }

        orderRepository.save(order); // Cập nhật invoice nếu có

        // Gửi thông báo tới người dùng và cửa hàng
        notificationService.sendNotificationToUsers(
                Arrays.asList(user.getUserId(), store.getManager().getUserId()),
                NotificationType.ORDER,
                "Đơn hàng" + order.getOrderCode() + " đã được tạo: ");

        return orderMapper.toDto(order);
    }

    @Transactional
    @Override
    public CreatePaymentResponseDTO handleOnlinePayment(OrderResponseDTO order) {
        Long amount = order.getTotalPrice().longValue();
        String description = "Thanh toán đơn hàng";
        return paymentService.createPayment(Long.parseLong(order.getOrderCode()), amount, description);
    }

    @Override
    @Transactional
    public void markOrderAsPaid(Long orderCode) {
        Optional<Order> optionalOrder = orderRepository.findByOrderCode(orderCode.toString());
        if (optionalOrder.isEmpty())
            return;

        Order order = optionalOrder.get();

        // Idempotent: nếu đã PAID rồi thì không trừ tiếp
        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        // Trừ tồn kho theo từng OrderItem, ưu tiên lô gần hết hạn (FEFO)
        for (OrderItem item : order.getOrderItems()) {
            if (item.getProduct() == null)
                continue;

            int remainingQty = item.getQuantity(); // hoặc item.getQuantityInBase() nếu inventory tính theo base unit

            // Lấy list tồn kho tại cửa hàng theo sản phẩm, còn hàng, chưa hết hạn, sort
            // theo expiry asc
            List<Inventory> inventories = inventoryRepository
                    .findByStore_StoreIdAndProduct_ProductIdAndQuantityGreaterThanAndBatch_ExpirationDateAfterOrderByBatch_ExpirationDateAsc(
                            order.getStore().getStoreId(),
                            item.getProduct().getProductId(),
                            0,
                            java.time.LocalDate.now());

            for (Inventory inv : inventories) {
                if (remainingQty <= 0)
                    break;

                int available = inv.getQuantity();
                if (available <= 0)
                    continue;

                int used = Math.min(available, remainingQty);

                // Trừ tồn kho
                inv.setQuantity(available - used);
                inventoryRepository.save(inv);

                // Ghi nhận chuyển động kho
                StockMovement movement = new StockMovement();
                movement.setWarehouse(inv.getWarehouse()); // nếu Inventory có warehouse
                movement.setStore(order.getStore());
                movement.setProduct(item.getProduct());
                movement.setBatch(inv.getBatch());
                movement.setMovementType(MovementType.SALE); // Giảm tồn kho do bán
                movement.setQuantity(used);
                movement.setNote("Bán đơn " + order.getOrderCode());
                stockMovementRepository.save(movement);

                remainingQty -= used;
            }

            // Nếu vẫn thiếu => ném lỗi (hoặc bạn có thể rollback toàn bộ)
            if (remainingQty > 0) {
                throw new IllegalArgumentException(
                        "Not enough stock for product ID: " + item.getProduct().getProductId()
                                + " at store ID: " + order.getStore().getStoreId());
            }
        }

        // Cuối cùng: set trạng thái PAID
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }

    @Override
    public Order createStoreOrder(StoreOrderDTO dto) {
        System.out.println("Creating store order with DTO: " + dto);
        if (dto.getStore_id() == null) {
            throw new IllegalArgumentException("Store ID must not be null");
        }
        Order order = new Order();
        order.setStore(storeRepository.findById(dto.getStore_id()).orElseThrow());
        order.setFullName(dto.getFull_name());
        order.setPhone(dto.getPhone());
        order.setAddress("In-store order");
        order.setDeliveryDate(LocalDateTime.now().toString());
        order.setDeliverySlot(LocalDateTime.now().toString());
        order.setPaymentMethod(PaymentMethod.valueOf(dto.getPayment_method()));
        order.setStatus(OrderStatus.valueOf(dto.getStatus()));
        order.setNeedInvoice(dto.getNeed_invoice());
        order.setTotalPrice(dto.getTotal_price());
        order.setVoucherDiscount(dto.getVoucher_discount());
        order.setNote(dto.getNote());
        order.setShippingFee(dto.getShipping_fee());
        order.setFinalPrice(dto.getFinal_price());
        order.setOrderCode(OrderCodeGenerator.generateOrderCode());

        List<OrderItem> items = new ArrayList<>();
        for (StoreOrderDTO.OrderItemDTO itemDTO : dto.getOrder_items()) {
            int remainingQty = itemDTO.getQuantity();
            List<Inventory> inventories = inventoryRepository
                    .findByStore_StoreIdAndProduct_ProductIdAndQuantityGreaterThanAndBatch_ExpirationDateAfterOrderByBatch_ExpirationDateAsc(
                            dto.getStore_id(),
                            itemDTO.getProduct_id(),
                            0,
                            java.time.LocalDate.now());

            for (Inventory inventory : inventories) {
                if (remainingQty <= 0)
                    break;
                int availableQty = inventory.getQuantity();
                if (availableQty <= 0)
                    continue;

                int usedQty = Math.min(availableQty, remainingQty);

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProduct(itemDTO.getProduct_id() == null ? null
                        : productRepository.findById(itemDTO.getProduct_id())
                                .orElseThrow(() -> new RuntimeException(
                                        "Product not found with ID " + itemDTO.getProduct_id())));
                item.setProductUnit(itemDTO.getProduct_unit_id() == null ? null
                        : productUnitRepository.findById(itemDTO.getProduct_unit_id())
                                .orElseThrow(() -> new RuntimeException(
                                        "Product unit not found with ID " + itemDTO.getProduct_unit_id())));
                item.setDiscount(itemDTO.getDiscount());
                item.setQuantity(usedQty);
                item.setQuantityInBase(itemDTO.getQuantity_in_base());
                item.setPrice(itemDTO.getPrice());
                item.setBatch(inventory.getBatch()); // Set the batch from inventory

                items.add(item);

                // Update inventory quantity
                inventory.setQuantity(availableQty - usedQty);
                inventoryRepository.save(inventory);

                remainingQty -= usedQty;
            }

            if (remainingQty > 0) {
                throw new IllegalArgumentException("Not enough stock for product ID: " + itemDTO.getProduct_id());
            }
        }
        order.setOrderItems(items);

        return orderRepository.save(order);
    }

}
