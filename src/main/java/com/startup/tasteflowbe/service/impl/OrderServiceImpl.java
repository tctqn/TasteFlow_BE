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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final UserVoucherRepository userVoucherRepository;

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
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, String status, String notes) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));

        OrderStatus current = order.getStatus();
        OrderStatus target = OrderStatus.valueOf(status);

        if (target == OrderStatus.CONFIRMED
                && (current == OrderStatus.PENDING || current == OrderStatus.PAID)) {
            commitInventoryForOrder(order);
            User user = order.getUser();
            Store store = order.getStore();
            if (user != null && store.getManager() != null) {
                notificationService.sendNotificationToUsers(
                        Arrays.asList(user.getUserId(), store.getManager().getUserId()),
                        NotificationType.ORDER,
                        "Đơn hàng " + order.getOrderCode() + " đã được xác nhận");
            }
            if(order.getFinalPrice().compareTo(BigDecimal.ZERO) == 0){
                target = OrderStatus.PAID;
            }
        }

        if(target == OrderStatus.DELIVERED) {
            User user = order.getUser();
            Store store = order.getStore();
            if (user != null && store.getManager() != null) {
                int pointsToAdd = order.getFinalPrice()
                        .divide(BigDecimal.valueOf(1000), RoundingMode.HALF_UP)
                        .intValue();
                notificationService.sendNotificationToUsers(
                        Arrays.asList(user.getUserId(), store.getManager().getUserId()),
                        NotificationType.ORDER,
                        "Đơn hàng " + order.getOrderCode() + " đã được giao thành công. Bạn nhân được " + pointsToAdd + " điểm thưởng từ đơn hàng này");
                user.setPointsUsed(user.getPointsUsed() + pointsToAdd);
                user.setPoints(user.getPoints() + pointsToAdd);
                userRepository.save(user);
            }
        }

        if(target == OrderStatus.CANCELLED) {
            User user = order.getUser();
            Store store = order.getStore();
            if (user != null && store.getManager() != null) {
                notificationService.sendNotificationToUsers(
                        Arrays.asList(user.getUserId(), store.getManager().getUserId()),
                        NotificationType.ORDER,
                        "Đơn hàng " + order.getOrderCode() + " đã bị hủy.");
            }
        }

        order.setStatus(target);
        order.setNote(notes);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    /**
     * Trừ tồn kho theo từng OrderItem, ưu tiên lô gần hết hạn (FEFO).
     * Idempotent theo trạng thái: chỉ nên gọi khi từ PENDING/PAID -> CONFIRMED.
     */
    private void commitInventoryForOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            if (item.getProduct() == null)
                continue;

            // Số lượng cần trừ (ưu tiên quantityInBase nếu hệ thống quản lý theo base unit)
            int remainingQty = item.getQuantityInBase() != null ? item.getQuantityInBase() : item.getQuantity();

            // Lấy list tồn kho tại cửa hàng theo sản phẩm, còn hàng, chưa hết hạn, sort
            // expiry asc (FEFO)
            List<Inventory> inventories = inventoryRepository
                    .findByStore_StoreIdAndProduct_ProductIdAndQuantityGreaterThanAndBatch_ExpirationDateAfterOrderByBatch_ExpirationDateAsc(
                            order.getStore().getStoreId(),
                            item.getProduct().getProductId(),
                            0,
                            LocalDate.now());

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
                movement.setWarehouse(inv.getWarehouse());
                movement.setStore(order.getStore());
                movement.setProduct(item.getProduct());
                movement.setBatch(inv.getBatch());
                movement.setMovementType(MovementType.SALE); // giảm tồn kho do bán/xác nhận
                movement.setQuantity(used);
                movement.setNote("Xác nhận đơn " + order.getOrderCode());
                stockMovementRepository.save(movement);

                remainingQty -= used;
            }

            // Nếu vẫn thiếu => ném lỗi để rollback toàn bộ giao dịch
            if (remainingQty > 0) {
                throw new IllegalArgumentException(
                        "Not enough stock for product ID: " + item.getProduct().getProductId()
                                + " at store ID: " + order.getStore().getStoreId());
            }
        }
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

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        // 1) Resolve user
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

        // 2) Tạo order cơ bản từ DTO
        Order order = orderMapper.toEntity(dto);
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        order.setOrderCode(OrderCodeGenerator.generateOrderCode());

        Store store = storeService.getStoreById(dto.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found with ID " + dto.getStoreId()));
        order.setStore(store);

        // ===== A. Tính subtotal gốc (TRƯỚC khuyến mãi) để dùng check minOrderAmount =====
        BigDecimal subtotalGross = BigDecimal.ZERO; // tổng tiền hàng trước mọi voucher
        for (CartItemDTO ci : dto.getCartItems()) {
            BigDecimal line = ci.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            subtotalGross = subtotalGross.add(line);
        }

        // 3) Chuẩn bị voucher (kiểm tra hạn dùng, số lượng, maxPerUser, minOrderAmount)
        Map<Long, Voucher> voucherMap = new HashMap<>();
        boolean hasFreeShippingVoucher = false;

        if (dto.getVoucherIds() != null && !dto.getVoucherIds().isEmpty()) {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            for (Long voucherId : dto.getVoucherIds()) {
                Voucher voucher = voucherRepository.findById(voucherId)
                        .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + voucherId));

                // hạn dùng
                if (voucher.getStartDate().isAfter(now) || voucher.getEndDate().isBefore(now)) {
                    throw new RuntimeException("Voucher " + voucher.getCode() + " hiện không còn hiệu lực");
                }

                // min order (dùng subtotalGross)
                BigDecimal minOrder = Optional.ofNullable(voucher.getMinOrderAmount()).orElse(BigDecimal.ZERO);
                if (subtotalGross.compareTo(minOrder) < 0) {
                    throw new RuntimeException("Đơn chưa đạt tối thiểu để dùng voucher " + voucher.getCode());
                }

                // số lượng tổng (quantity vs tất cả claim trong user_vouchers)
                if (voucher.getQuantity() != null) {
                    int claimed = userVoucherRepository.countClaimed(voucher);
                    if (claimed >= voucher.getQuantity()) {
                        throw new RuntimeException("Voucher " + voucher.getCode() + " đã hết số lượng");
                    }
                }

                // maxPerUser (đếm số claim used=true của user)
                if (user != null && voucher.getMaxPerUser() != null) {
                    int usedByUser = userVoucherRepository.countUsedByUserAndVoucher(user, voucher);
                    if (usedByUser >= voucher.getMaxPerUser()) {
                        throw new RuntimeException("Bạn đã dùng hết lượt cho voucher " + voucher.getCode());
                    }
                }

                voucherMap.put(voucherId, voucher);
                if (Boolean.TRUE.equals(voucher.getFreeShipping())) {
                    hasFreeShippingVoucher = true;
                }
            }
        }

        // 4) Tính giá từng item (best discount theo voucher áp dụng theo sản phẩm / danh mục)
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal itemsSubtotalAfterDiscount = BigDecimal.ZERO; // tổng tiền hàng SAU khuyến mãi sản phẩm

        BigDecimal originalUnitPrice = null;
        for (CartItemDTO itemDTO : dto.getCartItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDTO.getProductId()));
            ProductUnit productUnit = productUnitRepository.findById(itemDTO.getProductUnitId())
                    .orElseThrow(() -> new RuntimeException("Product unit not found: " + itemDTO.getProductUnitId()));

            originalUnitPrice = itemDTO.getPrice();
            BigDecimal bestDiscountPerUnit = BigDecimal.ZERO;

            for (Voucher voucher : voucherMap.values()) {
                // freeShipping không tác động vào giá sản phẩm
                if (Boolean.TRUE.equals(voucher.getFreeShipping())) continue;

                if (voucher.getDiscountType() == DiscountType.PERCENT && voucher.getDiscountPercent() != null) {
                    BigDecimal discount = originalUnitPrice
                            .multiply(voucher.getDiscountPercent())
                            .divide(BigDecimal.valueOf(100));
                    if (discount.compareTo(bestDiscountPerUnit) > 0) bestDiscountPerUnit = discount;
                } else if (voucher.getDiscountType() == DiscountType.AMOUNT && voucher.getDiscountAmount() != null) {
                    if (voucher.getDiscountAmount().compareTo(bestDiscountPerUnit) > 0) {
                        bestDiscountPerUnit = voucher.getDiscountAmount();
                    }
                }
            }

            BigDecimal finalUnitPrice = originalUnitPrice.subtract(bestDiscountPerUnit).max(BigDecimal.ZERO);
            BigDecimal lineAmount = finalUnitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            BigDecimal lineDiscount = bestDiscountPerUnit.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductUnit(productUnit);
            item.setQuantity(itemDTO.getQuantity());
            item.setQuantityInBase(itemDTO.getQuantity());
            item.setPrice(originalUnitPrice);
            item.setDiscount(lineDiscount);// tổng giảm cho line

            orderItems.add(item);
            itemsSubtotalAfterDiscount = itemsSubtotalAfterDiscount.add(lineAmount);
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(subtotalGross);

        // 5) Gán vouchers vào order (ghi ra order_vouchers)
        if (!voucherMap.isEmpty()) {
            order.setVouchers(new ArrayList<>(voucherMap.values()));
        }

        // ===== B. Tính phí ship & giảm ship (freeShipping) =====
        BigDecimal shippingFee = Optional.ofNullable(dto.getShippingFee()).orElse(BigDecimal.ZERO);
        BigDecimal shippingDiscount = BigDecimal.ZERO;
        if (hasFreeShippingVoucher && shippingFee.compareTo(BigDecimal.ZERO) > 0) {
            // freeShip: giảm tối đa bằng phí ship hiện có
            shippingDiscount = shippingFee;
        }

        // Tổng trước khi trừ điểm = (tổng hàng sau KM) + ship - giảm ship
        BigDecimal totalBeforePoints = itemsSubtotalAfterDiscount
                .add(shippingFee)
                .subtract(shippingDiscount)
                .max(BigDecimal.ZERO);

        // 6) Áp dụng điểm theo DTO (giới hạn theo user.points và totalBeforePoints)
        int pointsApplied = 0;
        if (user != null && dto.getPointsApplied() != null && dto.getPointsApplied() > 0) {
            long userPts = (user.getPoints() == null) ? 0L : user.getPoints();
            long wantApply = dto.getPointsApplied().longValue();

            long maxApplicable = Math.min(userPts, totalBeforePoints.longValue());
            long actualApplied = Math.max(0, Math.min(wantApply, maxApplicable));

            if (actualApplied > 0) {
                pointsApplied = (int) actualApplied;
                user.setPoints((int) (userPts - actualApplied));
                userRepository.save(user);
            }
        }
        order.setPointsApplied(pointsApplied);

        // finalPrice = tổng trước điểm - điểm
        BigDecimal finalPrice = totalBeforePoints.subtract(BigDecimal.valueOf(pointsApplied)).max(BigDecimal.ZERO);
        order.setShippingFee(shippingFee);
        order.setFinalPrice(finalPrice);

        // 7) Lưu order + items
        order = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // 8) Đánh dấu dùng voucher trong user_vouchers
        if (user != null && !voucherMap.isEmpty()) {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            for (Voucher v : voucherMap.values()) {
                // re-check capacity ngay trước khi tạo claim mới để tránh race
                if (v.getQuantity() != null) {
                    int claimed = userVoucherRepository.countClaimed(v);
                    if (claimed >= v.getQuantity()) {
                        throw new RuntimeException("Voucher " + v.getCode() + " vừa hết số lượng. Vui lòng thử lại.");
                    }
                }

                final User finalUser = user;
                UserVoucher uv = userVoucherRepository
                        .findTopByUserAndVoucherAndUsedFalseOrderByClaimedAtAsc(user, v)
                        .orElseGet(() -> {
                            UserVoucher created = new UserVoucher();
                            created.setUser(finalUser);
                            created.setVoucher(v);
                            created.setClaimedAt(now);
                            created.setUsed(false);
                            return userVoucherRepository.save(created);
                        });

                // Mark used
                uv.setUsed(true);
                userVoucherRepository.save(uv);

                // Cập nhật voucher.claimedCount
                v.setClaimedCount(v.getClaimedCount() + 1);
                voucherRepository.save(v);
            }
        }


        // 9) Hóa đơn (nếu có)
        if (dto.isNeedInvoice() && dto.getInvoiceInfo() != null) {
            Invoice invoice = new Invoice();
            invoice.setOrder(order);
            invoice.setInvoiceCompanyName(dto.getInvoiceInfo().getCompanyName());
            invoice.setInvoiceEmail(dto.getInvoiceInfo().getEmail());
            invoice.setInvoiceTaxCode(dto.getInvoiceInfo().getTaxCode());
            invoice.setInvoiceCompanyAddress(dto.getInvoiceInfo().getCompanyAddress());
            invoice.setIssuedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
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
            orderRepository.save(order);
        }

        // 10) Notify
        if (user != null && store.getManager() != null) {
            notificationService.sendNotificationToUsers(
                    Arrays.asList(user.getUserId(), store.getManager().getUserId()),
                    NotificationType.ORDER,
                    "Đơn hàng " + order.getOrderCode() + " đã được tạo");
        }

        return orderMapper.toDto(order);
    }




    @Transactional
    @Override
    public CreatePaymentResponseDTO handleOnlinePayment(OrderResponseDTO order) {
        Long amount = order.getFinalPrice().longValue();
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
    @Transactional
    public Order createStoreOrder(StoreOrderDTO dto) {
        System.out.println("Creating store order with DTO: " + dto);
        if (dto.getStore_id() == null) {
            throw new IllegalArgumentException("Store ID must not be null");
        }

        User user = null;
        if (dto.getUser_id() != null) {
            user = userRepository.findById(dto.getUser_id())
                    .orElseThrow(() -> new RuntimeException("User not found with ID " + dto.getUser_id()));
        }

        Order order = new Order();
        order.setStore(storeRepository.findById(dto.getStore_id()).orElseThrow());
        order.setFullName(dto.getFull_name());
        order.setUser(user);
        order.setPhone(dto.getPhone());
        order.setAddress("In-store order");
        order.setDeliveryDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString());
        order.setDeliverySlot(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString());
        order.setPaymentMethod(PaymentMethod.valueOf(dto.getPayment_method()));
        order.setStatus(OrderStatus.valueOf(dto.getStatus()));
        order.setNeedInvoice(dto.getNeed_invoice());
        order.setTotalPrice(dto.getTotalPrice());
        order.setVoucherDiscount(dto.getVoucher_discount());
        order.setNote(dto.getNote());
        order.setShippingFee(dto.getShippingFee());
        order.setFinalPrice(dto.getFinalPrice());
        order.setOrderCode(OrderCodeGenerator.generateOrderCode());
        orderRepository.save(order);

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
                item.setBatch(inventory.getBatch());

                orderItemRepository.save(item);
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

        int point = user.getPoints() != null ? user.getPoints() : 0;
        point += order.getFinalPrice().intValue() / 10000;
        user.setPoints(point);
        userRepository.save(user);

        return order;
    }

}
