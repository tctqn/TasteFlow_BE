package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.CartItemDTO;
import com.startup.tasteflowbe.dto.request.OrderRequestDTO;
import com.startup.tasteflowbe.dto.request.StoreOrderDTO;
import com.startup.tasteflowbe.dto.request.UpdateOrderStatusDTO;
import com.startup.tasteflowbe.dto.response.CreatePaymentResponseDTO;
import com.startup.tasteflowbe.dto.response.FulfillmentDTO;
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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

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

    // Auto-allocation FEFO theo Inventory
    private final InventoryRepository inventoryRepository;

    // Allocation theo FE gửi: thao tác trực tiếp lên ProductBatch
    private final ProductBatchRepository productBatchRepository;

    // ====================== CRUD/GETTERS ======================

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

    // ====================== UPDATE STATUS (COD vs ONLINE) ======================

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, UpdateOrderStatusDTO request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));

        OrderStatus current = order.getStatus();
        OrderStatus target = OrderStatus.valueOf(request.getStatus());
        String notes = request.getNotes();

        boolean hasFulfillment = request.getFulfillment() != null
                && request.getFulfillment().getItems() != null
                && !request.getFulfillment().getItems().isEmpty();

        // PENDING -> CONFIRMED (COD): allocate/reserve
        if (target == OrderStatus.CONFIRMED
                && current == OrderStatus.PENDING
                && order.getPaymentMethod() == PaymentMethod.COD) {

            if (hasFulfillment) {
                applyAllocationsFromRequest(order, request.getFulfillment(), target);
            } else {
                allocateOrderItemsByFEFO(order); // fallback
            }

            notifyUserAndManager(order, "Đơn hàng " + order.getOrderCode() + " đã được xác nhận");

            // Nếu finalPrice = 0 => coi như PAID ngay
            if (order.getFinalPrice().compareTo(BigDecimal.ZERO) == 0) {
                target = OrderStatus.PAID;
            }
        }

        // ONLINE: allocate tại PAID
        if (target == OrderStatus.PAID
                && current != OrderStatus.PAID
                && order.getPaymentMethod() != PaymentMethod.COD) {
            if (hasFulfillment) {
                applyAllocationsFromRequest(order, request.getFulfillment(), target);
            } else {
                allocateOrderItemsByFEFO(order);
            }
        }

        // DELIVERED: thưởng điểm
        if (target == OrderStatus.DELIVERED) {
            rewardAndNotifyDelivered(order);
        }

        // CANCELLED: trả kho đúng batch nếu đã allocate (CONFIRMED/PAID)
        if (target == OrderStatus.CANCELLED
                && (current == OrderStatus.CONFIRMED || current == OrderStatus.PAID)) {
            returnAllocatedStockForCancelledOrder(order);
            notifyUserAndManager(order, "Đơn hàng " + order.getOrderCode() + " đã bị hủy.");
        }

        // Cập nhật trạng thái + ghi chú
        order.setStatus(target);
        if (notes != null) {
            order.setNote(notes);
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    // ====================== LIST BY USER / DELETE ======================

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

    // ====================== CREATE ORDER ======================

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

        // ===== A. Subtotal gốc trước KM
        BigDecimal subtotalGross = BigDecimal.ZERO;
        for (CartItemDTO ci : dto.getCartItems()) {
            BigDecimal line = ci.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            subtotalGross = subtotalGross.add(line);
        }

        // 3) Chuẩn bị voucher
        Map<Long, Voucher> voucherMap = new HashMap<>();
        boolean hasFreeShippingVoucher = false;
        if (dto.getVoucherIds() != null && !dto.getVoucherIds().isEmpty()) {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            for (Long voucherId : dto.getVoucherIds()) {
                Voucher voucher = voucherRepository.findById(voucherId)
                        .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + voucherId));
                if (voucher.getStartDate().isAfter(now) || voucher.getEndDate().isBefore(now)) {
                    throw new RuntimeException("Voucher " + voucher.getCode() + " hiện không còn hiệu lực");
                }
                BigDecimal minOrder = Optional.ofNullable(voucher.getMinOrderAmount()).orElse(BigDecimal.ZERO);
                if (subtotalGross.compareTo(minOrder) < 0) {
                    throw new RuntimeException("Đơn chưa đạt tối thiểu để dùng voucher " + voucher.getCode());
                }
                if (voucher.getQuantity() != null) {
                    int claimed = userVoucherRepository.countClaimed(voucher);
                    if (claimed >= voucher.getQuantity()) {
                        throw new RuntimeException("Voucher " + voucher.getCode() + " đã hết số lượng");
                    }
                }
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

        // 4) Tính giá từng item (best discount)
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal itemsSubtotalAfterDiscount = BigDecimal.ZERO;

        for (CartItemDTO itemDTO : dto.getCartItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDTO.getProductId()));
            ProductUnit productUnit = productUnitRepository.findById(itemDTO.getProductUnitId())
                    .orElseThrow(() -> new RuntimeException("Product unit not found: " + itemDTO.getProductUnitId()));

            BigDecimal originalUnitPrice = itemDTO.getPrice();
            BigDecimal bestDiscountPerUnit = BigDecimal.ZERO;

            for (Voucher voucher : voucherMap.values()) {
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
            item.setDiscount(lineDiscount);

            orderItems.add(item);
            itemsSubtotalAfterDiscount = itemsSubtotalAfterDiscount.add(lineAmount);
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(subtotalGross);

        // 5) Gán vouchers vào order
        if (!voucherMap.isEmpty()) {
            order.setVouchers(new ArrayList<>(voucherMap.values()));
        }

        // ===== B. Ship & giảm ship (freeShip)
        BigDecimal shippingFee = Optional.ofNullable(dto.getShippingFee()).orElse(BigDecimal.ZERO);
        BigDecimal shippingDiscount = hasFreeShippingVoucher ? shippingFee : BigDecimal.ZERO;

        // Tổng trước điểm
        BigDecimal totalBeforePoints = itemsSubtotalAfterDiscount
                .add(shippingFee)
                .subtract(shippingDiscount)
                .max(BigDecimal.ZERO);

        // 6) Áp dụng điểm
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

        // finalPrice
        BigDecimal finalPrice = totalBeforePoints.subtract(BigDecimal.valueOf(pointsApplied)).max(BigDecimal.ZERO);
        order.setShippingFee(shippingFee);
        order.setFinalPrice(finalPrice);

        // 7) Lưu order + items
        order = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // 8) Đánh dấu dùng voucher
        if (user != null && !voucherMap.isEmpty()) {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            for (Voucher v : voucherMap.values()) {
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
                uv.setUsed(true);
                userVoucherRepository.save(uv);

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

    // ====================== PAYMENT ======================

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

        // Idempotent
        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        // Online: allocate nếu chưa allocate
        allocateOrderItemsByFEFO(order);

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        User user = order.getUser();
        Store store = order.getStore();
        if (user != null && store.getManager() != null) {
            notificationService.sendNotificationToUsers(
                    Arrays.asList(user.getUserId(), store.getManager().getUserId()),
                    NotificationType.ORDER,
                    "Đơn hàng " + order.getOrderCode() + " đã được thanh toán");
        }
    }

    // ====================== STORE-ORDER (BÁN TẠI QUẦY) ======================

    @Override
    @Transactional
    public Order createStoreOrder(StoreOrderDTO dto) {
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
                            LocalDate.now());

            for (Inventory inventory : inventories) {
                if (remainingQty <= 0) break;

                int availableQty = inventory.getQuantity();
                if (availableQty <= 0) continue;

                int usedQty = Math.min(availableQty, remainingQty);

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProduct(itemDTO.getProduct_id() == null ? null
                        : productRepository.findById(itemDTO.getProduct_id())
                        .orElseThrow(() -> new RuntimeException("Product not found with ID " + itemDTO.getProduct_id())));
                item.setProductUnit(itemDTO.getProduct_unit_id() == null ? null
                        : productUnitRepository.findById(itemDTO.getProduct_unit_id())
                        .orElseThrow(() -> new RuntimeException("Product unit not found with ID " + itemDTO.getProduct_unit_id())));
                item.setDiscount(itemDTO.getDiscount());
                item.setQuantity(usedQty);
                item.setQuantityInBase(itemDTO.getQuantity_in_base());
                item.setPrice(itemDTO.getPrice());
                item.setBatch(inventory.getBatch());

                orderItemRepository.save(item);
                items.add(item);

                // Trừ tồn kho
                inventory.setQuantity(availableQty - usedQty);
                inventoryRepository.save(inventory);

                // Movement
                StockMovement movement = new StockMovement();
                movement.setWarehouse(inventory.getWarehouse());
                movement.setStore(order.getStore());
                movement.setProduct(item.getProduct());
                movement.setBatch(inventory.getBatch());
                movement.setMovementType(MovementType.SALE);
                movement.setQuantity(usedQty);
                movement.setMovementDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                movement.setNote("Bán tại quầy đơn " + order.getOrderCode());
                stockMovementRepository.save(movement);

                remainingQty -= usedQty;
            }

            if (remainingQty > 0) {
                throw new IllegalArgumentException("Not enough stock for product ID: " + itemDTO.getProduct_id());
            }
        }

        if (user != null) {
            int point = user.getPoints() != null ? user.getPoints() : 0;
            point += order.getFinalPrice().intValue() / 10000;
            user.setPoints(point);
            userRepository.save(user);
        }

        return order;
    }

    // ====================== FEFO ALLOCATION & RETURN HELPERS ======================

    /**
     * Auto cấp phát theo FEFO (Inventory) cho trường hợp không có fulfillment từ FE.
     * Nếu 1 item cần nhiều lô -> tách nhiều record OrderItem.
     */
    @Transactional
    public void allocateOrderItemsByFEFO(Order order) {
        boolean alreadyAllocated = order.getOrderItems()
                .stream().allMatch(oi -> oi.getBatch() != null);
        if (alreadyAllocated) return;

        List<OrderItem> snapshot = new ArrayList<>(order.getOrderItems());

        for (OrderItem oi : snapshot) {
            if (oi.getProduct() == null) continue;

            int remaining = (oi.getQuantityInBase() != null) ? oi.getQuantityInBase() : oi.getQuantity();

            List<Inventory> invs = inventoryRepository
                    .findByStore_StoreIdAndProduct_ProductIdAndQuantityGreaterThanAndBatch_ExpirationDateAfterOrderByBatch_ExpirationDateAsc(
                            order.getStore().getStoreId(),
                            oi.getProduct().getProductId(),
                            0,
                            LocalDate.now());

            boolean firstAllocated = false;

            for (Inventory inv : invs) {
                if (remaining <= 0) break;

                int available = inv.getQuantity();
                if (available <= 0) continue;

                int take = Math.min(available, remaining);
                if (take <= 0) continue;

                if (!firstAllocated) {
                    oi.setBatch(inv.getBatch());
                    oi.setQuantity(take);
                    oi.setQuantityInBase(take);
                    orderItemRepository.save(oi);
                    firstAllocated = true;
                } else {
                    OrderItem split = new OrderItem();
                    split.setOrder(order);
                    split.setProduct(oi.getProduct());
                    split.setProductUnit(oi.getProductUnit());
                    split.setPrice(oi.getPrice());
                    split.setDiscount(BigDecimal.ZERO);
                    split.setQuantity(take);
                    split.setQuantityInBase(take);
                    split.setBatch(inv.getBatch());
                    orderItemRepository.save(split);
                    order.getOrderItems().add(split);
                }

                inv.setQuantity(available - take);
                inventoryRepository.save(inv);

                StockMovement mv = new StockMovement();
                mv.setMovementDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                mv.setMovementType(MovementType.SALE);
                mv.setStore(order.getStore());
                mv.setWarehouse(inv.getWarehouse());
                mv.setProduct(oi.getProduct());
                mv.setBatch(inv.getBatch());
                mv.setQuantity(take);
                mv.setNote("Chuẩn bị hàng bán cho đơn hàng " + order.getOrderCode());
                stockMovementRepository.save(mv);

                remaining -= take;
            }

            if (remaining > 0) {
                throw new IllegalArgumentException(
                        "Not enough stock for product ID: " + oi.getProduct().getProductId()
                                + " at store ID: " + order.getStore().getStoreId());
            }
        }
    }

    /**
     * Hoàn kho theo đúng batch cho đơn bị hủy (đã allocate trước đó).
     */
    @Transactional
    public void returnAllocatedStockForCancelledOrder(Order order) {
        for (OrderItem oi : order.getOrderItems()) {
            if (oi.getBatch() == null) continue;

            int qty = (oi.getQuantityInBase() != null) ? oi.getQuantityInBase() : oi.getQuantity();

            Inventory inv = inventoryRepository
                    .findByStore_StoreIdAndProduct_ProductIdAndBatch_BatchId(
                            order.getStore().getStoreId(),
                            oi.getProduct().getProductId(),
                            oi.getBatch().getBatchId()
                    )
                    .orElseThrow(() -> new IllegalStateException("Inventory row missing for batch return"));

            inv.setQuantity(inv.getQuantity() + qty);
            inventoryRepository.save(inv);

            StockMovement mv = new StockMovement();
            mv.setMovementDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            mv.setMovementType(MovementType.RETURN_FROM_CUSTOMER);
            mv.setStore(order.getStore());
            mv.setWarehouse(inv.getWarehouse());
            mv.setProduct(oi.getProduct());
            mv.setBatch(oi.getBatch());
            mv.setQuantity(qty);
            mv.setNote("Hủy đơn hàng " + order.getOrderCode());
            stockMovementRepository.save(mv);
        }
    }

    /**
     * Áp dụng allocations FE gửi bằng ProductBatch:
     *  - Validate: orderItem thuộc đơn, productId khớp, batch thuộc đúng product
     *  - Allocation đầu: dùng dòng OrderItem hiện tại
     *  - Allocation tiếp theo: tạo thêm dòng OrderItem
     *  - Trừ tồn trên ProductBatch.quantity
     *  - Ghi StockMovement theo target status
     */
    @Transactional
    protected void applyAllocationsFromRequest(Order order,
                                               UpdateOrderStatusDTO.FulfillmentDTO fulfillment,
                                               OrderStatus targetStatus) {
        Map<Long, OrderItem> itemById = order.getOrderItems().stream()
                .collect(Collectors.toMap(OrderItem::getOrderItemId, it -> it));

        for (UpdateOrderStatusDTO.FulfillmentItemDTO reqItem : fulfillment.getItems()) {
            Long orderItemId = reqItem.getOrderItemId();
            Long productId   = reqItem.getProductId();
            int  requested   = nz(reqItem.getRequestedQty());
            List<UpdateOrderStatusDTO.AllocationDTO> allocations =
                    Optional.ofNullable(reqItem.getAllocations()).orElse(List.of());

            OrderItem baseItem = itemById.get(orderItemId);
            if (baseItem == null || baseItem.getProduct() == null) {
                throw new IllegalArgumentException("orderItem không hợp lệ: " + orderItemId);
            }
            if (!Objects.equals(baseItem.getProduct().getProductId(), productId)) {
                throw new IllegalArgumentException("orderItem " + orderItemId + " không khớp productId " + productId);
            }

            int totalAlloc = allocations.stream().mapToInt(a -> nz(a.getQuantity())).sum();
            if (totalAlloc > requested) {
                throw new IllegalArgumentException("Tổng phân bổ vượt requestedQty cho orderItemId=" + orderItemId);
            }

            boolean usedBaseLine = false;

            for (UpdateOrderStatusDTO.AllocationDTO al : allocations) {
                Long batchId = al.getBatchId();
                int  qty     = nz(al.getQuantity());
                if (qty <= 0) continue;

                ProductBatch pbatch = productBatchRepository.findById(batchId)
                        .orElseThrow(() -> new IllegalArgumentException("ProductBatch không tồn tại: " + batchId));

                if (pbatch.getProduct() == null ||
                        !Objects.equals(pbatch.getProduct().getProductId(), productId)) {
                    throw new IllegalArgumentException("Batch " + batchId + " không thuộc product " + productId);
                }

                int available = getAvailableFromProductBatch(pbatch);
                if (available < qty) {
                    throw new IllegalArgumentException("Batch " + batchId + " thiếu tồn. Cần " + qty + ", còn " + available);
                }

                if (!usedBaseLine) {
                    baseItem.setBatch(pbatch);
                    baseItem.setQuantity(qty);
                    baseItem.setQuantityInBase(qty);
                    orderItemRepository.save(baseItem);
                    usedBaseLine = true;
                } else {
                    OrderItem split = new OrderItem();
                    split.setOrder(order);
                    split.setProduct(baseItem.getProduct());
                    split.setProductUnit(baseItem.getProductUnit());
                    split.setPrice(baseItem.getPrice());
                    // nếu cần, phân bổ chiết khấu theo tỉ lệ thay vì 0
                    split.setDiscount(BigDecimal.ZERO);
                    split.setQuantity(qty);
                    split.setQuantityInBase(qty);
                    split.setBatch(pbatch);
                    orderItemRepository.save(split);
                    order.getOrderItems().add(split);
                }

                // trừ tồn ProductBatch
                reserveOrAllocateProductBatch(pbatch, qty, targetStatus);

                // movement
                StockMovement mv = new StockMovement();
                mv.setMovementDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                mv.setMovementType(movementTypeFor(targetStatus));
                mv.setStore(order.getStore());
                mv.setWarehouse(pbatch.getWarehouse());
                mv.setProduct(baseItem.getProduct());
                mv.setBatch(pbatch);
                mv.setQuantity(qty);
                mv.setNote("Bán cho đơn hàng " + order.getOrderCode());
                stockMovementRepository.save(mv);
            }

            // Nếu bắt buộc đủ: if (totalAlloc != requested) throw ...
        }
    }

    private MovementType movementTypeFor(OrderStatus target) {
        return MovementType.SALE;
    }

    /** Lấy tồn khả dụng từ ProductBatch — hiện tại model chỉ có quantity */
    private int getAvailableFromProductBatch(ProductBatch pbatch) {
        // Nếu sau này có available/reserved: return pbatch.getAvailable() - pbatch.getReserved();
        return Optional.ofNullable(pbatch.getQuantity()).orElse(0);
    }

    /** Trừ tồn trên ProductBatch theo target status (CONFIRMED/PAID/DELIVERED) */
    private void reserveOrAllocateProductBatch(ProductBatch pbatch, int qty, OrderStatus targetStatus) {
        // Nếu CONFIRMED bạn muốn "reserve" thì cần thêm trường reserved; tạm thời trừ thẳng quantity:
        int cur = Optional.ofNullable(pbatch.getQuantity()).orElse(0);
        pbatch.setQuantity(cur - qty);
        productBatchRepository.save(pbatch);
    }

    private void notifyUserAndManager(Order order, String message) {
        User user = order.getUser();
        Store store = order.getStore();
        if (user != null && store.getManager() != null) {
            notificationService.sendNotificationToUsers(
                    Arrays.asList(user.getUserId(), store.getManager().getUserId()),
                    NotificationType.ORDER,
                    message
            );
        }
    }

    private void rewardAndNotifyDelivered(Order order) {
        User user = order.getUser();
        Store store = order.getStore();
        if (user != null && store.getManager() != null) {
            int pointsToAdd = order.getFinalPrice()
                    .divide(BigDecimal.valueOf(1000), RoundingMode.HALF_UP)
                    .intValue();
            notificationService.sendNotificationToUsers(
                    Arrays.asList(user.getUserId(), store.getManager().getUserId()),
                    NotificationType.ORDER,
                    "Đơn hàng " + order.getOrderCode() + " đã được giao thành công. Bạn nhận được " + pointsToAdd + " điểm thưởng từ đơn này"
            );
            user.setPointsUsed(user.getPointsUsed() + pointsToAdd);
            user.setPoints(user.getPoints() + pointsToAdd);
            userRepository.save(user);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public FulfillmentDTO getOrderAllocations(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + orderId));

        // Nhóm theo productId để tạo "mục hiển thị" cho FE
        Map<Long, List<OrderItem>> byProduct = Optional.ofNullable(order.getOrderItems())
                .orElse(List.of())
                .stream()
                .filter(oi -> oi.getProduct() != null)
                .collect(groupingBy(oi -> oi.getProduct().getProductId()));

        List<FulfillmentDTO.Item> items = new ArrayList<>();

        for (Map.Entry<Long, List<OrderItem>> e : byProduct.entrySet()) {
            Long productId = e.getKey();
            List<OrderItem> lines = e.getValue();

            // Tổng requestedQty cho product này
            int requestedQty = lines.stream()
                    .mapToInt(oi -> (oi.getQuantityInBase() != null ? oi.getQuantityInBase() : oi.getQuantity()))
                    .sum();

            // Chọn 1 id đại diện (id nhỏ nhất trong nhóm)
            Long representativeId = lines.stream()
                    .map(OrderItem::getOrderItemId)
                    .filter(Objects::nonNull)
                    .min(Long::compareTo)
                    .orElse(null);

            // Gộp allocations theo batchId (chỉ lấy dòng đã có batch)
            Map<Long, Integer> byBatch = lines.stream()
                    .filter(oi -> oi.getBatch() != null) // ProductBatch
                    .collect(groupingBy(
                            oi -> oi.getBatch().getBatchId(),
                            summingInt(oi -> (oi.getQuantityInBase() != null ? oi.getQuantityInBase() : oi.getQuantity()))
                    ));

            List<FulfillmentDTO.Allocation> allocations = byBatch.entrySet().stream()
                    .map(a -> FulfillmentDTO.Allocation.builder()
                            .batchId(a.getKey())
                            .quantity(a.getValue())
                            .build())
                    .toList();

            items.add(FulfillmentDTO.Item.builder()
                    .orderItemIdRepresentative(representativeId)
                    .productId(productId)
                    .requestedQty(requestedQty)
                    .allocations(allocations)
                    .build());
        }

        return FulfillmentDTO.builder()
                .mode("FEFO")
                .items(items)
                .build();
    }

    private int nz(Integer v) { return v == null ? 0 : v; }
}
