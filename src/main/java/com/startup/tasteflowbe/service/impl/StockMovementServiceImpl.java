package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.*;
import com.startup.tasteflowbe.dto.StoreTransferParam;
import com.startup.tasteflowbe.dto.request.StockMovementRequestDTO;
import com.startup.tasteflowbe.dto.request.DamageStockRequestDTO;
import com.startup.tasteflowbe.enums.MovementType;
import com.startup.tasteflowbe.enums.NotificationType;
import com.startup.tasteflowbe.repository.*;
import com.startup.tasteflowbe.service.NotificationService;
import com.startup.tasteflowbe.service.StockMovementService;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.startup.tasteflowbe.dto.response.StockMovementDTO;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final WarehouseRepository warehouseRepository;

    private final ProductRepository productRepository;

    private final ProductBatchRepository productBatchRepository;

    private final InventoryRepository inventoryRepository;

    private final StockMovementRepository stockMovementRepository;

    private final StoreRepository storeRepository;

    private final StoreRequestRepository storeRequestRepository;

    private final NotificationService notificationService;

    private final StoreRequestItemRepository storeRequestItemRepository;
        
    private final JavaMailSender mailSender;


    @Override
    public StockMovement createStockMovement(StockMovementRequestDTO dto) {
        // Lấy các entity liên quan từ DB bằng ID
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductId()));

        // Các trường khác có thể null, nên cần kiểm tra trước khi lấy
        Warehouse warehouse = null;
        if (dto.getWarehouseId() != null) {
            warehouse = warehouseRepository.findById(dto.getWarehouseId())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found: " + dto.getWarehouseId()));
        }

        ProductBatch batch = null;
        if (dto.getBatchId() != null) {
            batch = productBatchRepository.findById(dto.getBatchId())
                    .orElseThrow(() -> new RuntimeException("Batch not found: " + dto.getBatchId()));
        }

        // Tạo đối tượng StockMovement mới
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setBatch(batch);
        movement.setMovementType(dto.getMovementType());
        movement.setQuantity(dto.getQuantity());
        movement.setNote(dto.getNote());
        movement.setMovementDate(java.time.LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))); // Set ngày giờ hiện tại

        // Lưu và trả về
        return stockMovementRepository.save(movement);
    }

    @Override
    public StockMovement getStockMovementById(Long movementId) {
        Optional<StockMovement> stockMovement = stockMovementRepository.findById(movementId);
        return stockMovement.orElse(null);
    }

    @Override
    public List<StockMovementDTO> getAllStockMovements() {
        List<StockMovement> movements = stockMovementRepository.findAll();
        return movements.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private StockMovementDTO toDTO(StockMovement m) {
        StockMovementDTO dto = new StockMovementDTO();
        dto.setMovementId(m.getMovementId());

        // Product
        StockMovementDTO.ProductInfo productInfo = new StockMovementDTO.ProductInfo();
        if (m.getProduct() != null) {
            productInfo.setProductId(m.getProduct().getProductId());
            productInfo.setName(m.getProduct().getName());
        }
        dto.setProduct(productInfo);

        // Batch
        StockMovementDTO.BatchInfo batchInfo = new StockMovementDTO.BatchInfo();
        if (m.getBatch() != null) {
            batchInfo.setBatchId(m.getBatch().getBatchId());
        }
        dto.setBatch(batchInfo);

        dto.setMovementType(m.getMovementType());
        dto.setQuantity(m.getQuantity());
        dto.setMovementDate(m.getMovementDate());

        // Warehouse
        if (m.getWarehouse() != null) {
            StockMovementDTO.WarehouseInfo warehouseInfo = new StockMovementDTO.WarehouseInfo();
            warehouseInfo.setWarehouseId(m.getWarehouse().getWarehouseId());
            warehouseInfo.setName(m.getWarehouse().getName());
            dto.setWarehouse(warehouseInfo);
        }

        // Store
        if (m.getStore() != null) {
            StockMovementDTO.StoreInfo storeInfo = new StockMovementDTO.StoreInfo();
            storeInfo.setStoreId(m.getStore().getStoreId());
            storeInfo.setName(m.getStore().getName());
            dto.setStore(storeInfo);
        }

        dto.setNote(m.getNote());
        return dto;
    }

    @Override
    public List<StockMovement> getStockMovementsToStore(Long storeId) {
        return stockMovementRepository.findByStore_StoreId(storeId);
    }

    @Override
    @Transactional
    public void transferToStores(Long requestId, Long warehouseId, Long productId,
            List<StoreTransferParam> transferList) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        int totalQuantity = transferList.stream()
                .mapToInt(StoreTransferParam::getQuantity)
                .sum();

        List<Inventory> inventories = inventoryRepository
                .findByWarehouse_WarehouseIdAndProduct_ProductIdAndStoreIsNullOrderByBatch_ExpirationDateAsc(
                        warehouseId, productId)
                .stream()
                .filter(inv -> inv.getBatch().getExpirationDate() == null
                        || !inv.getBatch().getExpirationDate().isBefore(LocalDate.now()))
                .filter(inv -> inv.getQuantity() != null && inv.getQuantity() > 0)
                .collect(Collectors.toList());

        if (inventories.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy tồn kho cho sản phẩm này trong kho.");
        }

        int available = inventories.stream().mapToInt(Inventory::getQuantity).sum();
        if (available < totalQuantity) {
            throw new IllegalArgumentException("Không đủ hàng trong kho để chuyển.");
        }

        int remaining = totalQuantity;
        for (Inventory inv : inventories) {
            if (remaining <= 0)
                break;

            int deduct = Math.min(inv.getQuantity(), remaining);
            inv.setQuantity(inv.getQuantity() - deduct);
            inventoryRepository.save(inv);

            for (StoreTransferParam param : transferList) {
                Store store = storeRepository.findById(param.getStoreId()).orElseThrow();
                int storeQty = param.getQuantity();
                if (storeQty <= 0)
                    continue;

                int usedFromThisBatch = Math.min(storeQty, deduct);
                if (usedFromThisBatch > 0) {
                    StockMovement movement = new StockMovement();
                    movement.setWarehouse(warehouse);
                    movement.setStore(store);
                    movement.setProduct(product);
                    movement.setBatch(inv.getBatch());
                    movement.setQuantity(usedFromThisBatch);
                    movement.setMovementDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                    movement.setMovementType(MovementType.TRANSFER_TO_STORE);
                    movement.setStoreRequest(storeRequestRepository.findById(requestId).orElseThrow());
                    movement.setNote("Chuyển " + usedFromThisBatch + " sản phẩm từ lô "
                            + inv.getBatch().getBatchId() + " đến cửa hàng " + param.getStoreId());
                    stockMovementRepository.save(movement);

                    param.setQuantity(storeQty - usedFromThisBatch);
                    remaining -= usedFromThisBatch;

                    notificationService.sendNotificationToUsers(
                            Arrays.asList(store.getManager().getUserId(), warehouse.getManager().getUserId()),
                            NotificationType.ALERT,
                            "Chuyển " + usedFromThisBatch + " sản phẩm từ lô "
                                    + inv.getBatch().getBatchId() + " đến cửa hàng " + store.getName());

                    String message = String.format(
                        "Chào %s,%n%n" +
                        "Chuyển %d sản phẩm từ lô %s đến cửa hàng %s%n%n" +
                        "Trân trọng,%nTasteFlow",
                        store.getManager().getFirstName() + " " + store.getManager().getLastName(),
                        usedFromThisBatch,
                        inv.getBatch().getBatchId(),
                        store.getName()
                    );

                    SimpleMailMessage mail = new SimpleMailMessage();
                    mail.setTo(store.getManager().getEmail());
                    mail.setSubject("Chuyển hàng về cửa hàng " + store.getName());
                    mail.setText(message);
                    mailSender.send(mail);
                }

            }

//            remaining -= deduct;
        }

        StoreRequestItem item = storeRequestItemRepository.findByStoreRequest_RequestIdAndProductId(requestId,
                productId);
        int actualQuantity = item.getActualQuantity() != null ? item.getActualQuantity().intValue() : 0;
        if (totalQuantity + actualQuantity < item.getQuantity()) {
            item.setActualQuantity(Long.valueOf(totalQuantity));
            item.setStatus("Partial");
            storeRequestItemRepository.save(item);
        } else {
            item.setActualQuantity(Long.valueOf(totalQuantity + actualQuantity));
            item.setStatus("Processing");
            storeRequestItemRepository.save(item);
        }

        StoreRequest request = storeRequestRepository.findById(requestId).orElseThrow();
        updateParentRequestStatus(request);
    }

    @Override
    @Transactional
    public void rejectStoreRequestItem(Long requestId, Long productId) {
        StoreRequestItem item = storeRequestItemRepository.findByStoreRequest_RequestIdAndProductId(requestId,
                productId);
        if (item == null) {
            throw new IllegalArgumentException("Không tìm thấy mục yêu cầu cho sản phẩm và yêu cầu đã cho.");
        }
        item.setStatus("Rejected");
        storeRequestItemRepository.save(item);

        StoreRequest request = storeRequestRepository.findById(requestId).orElseThrow();
        updateParentRequestStatus(request);
    }

    private void updateParentRequestStatus(StoreRequest request) {
        long totalItems = request.getItems().size();
        long approvedCount = request.getItems().stream().filter(i -> "Approved".equals(i.getStatus())).count();
        long rejectedCount = request.getItems().stream().filter(i -> "Rejected".equals(i.getStatus())).count();
        long partiallyApprovedCount = request.getItems().stream()
                .filter(i -> "Partial".equals(i.getStatus())).count();

        System.out.println("Status Update - Total: " + totalItems + ", Approved: " + approvedCount +
                ", Rejected: " + rejectedCount + ", Partially Approved: " + partiallyApprovedCount);

        if (approvedCount == totalItems) {
            request.setStatus("Approved");
        } else if (rejectedCount == totalItems) {
            request.setStatus("Rejected");
        } else if (rejectedCount + approvedCount == totalItems) {
            request.setStatus("Completed");
        } else if (approvedCount > 0 || partiallyApprovedCount > 0) {
            request.setStatus("Partial");
        } else {
            request.setStatus("Processing");
        }

        storeRequestRepository.save(request);
    }

    @Override
    @Transactional
    public String damageStock(DamageStockRequestDTO dto) {
        Inventory inventory;
        if (dto.getStoreId() != null) {
            // Tìm tồn kho tại cửa hàng
            Optional<Inventory> inventoryOpt = inventoryRepository
                    .findByStore_StoreIdAndProduct_ProductIdAndBatch_BatchId(
                            dto.getStoreId(), dto.getProductId(), dto.getBatchId());
            if (inventoryOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy tồn kho phù hợp trong cửa hàng.");
            }
            inventory = inventoryOpt.get();
        } else {
            // Luôn tìm theo warehouseId
            List<Inventory> inventories = inventoryRepository
                    .findByWarehouse_WarehouseIdAndProduct_ProductIdAndBatch_BatchId(
                            dto.getWarehouseId(), dto.getProductId(), dto.getBatchId());
            if (inventories.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy tồn kho phù hợp trong kho.");
            }
            inventory = inventories.get(0);
        }

        if (inventory.getQuantity() < dto.getDamageQuantity()) {
            throw new IllegalArgumentException("Số lượng hỏng/hết hạn lớn hơn tồn kho hiện tại.");
        }
        // Trừ số lượng hỏng/hết hạn khỏi tồn kho
        inventory.setQuantity(inventory.getQuantity() - dto.getDamageQuantity());
        inventoryRepository.save(inventory);

        // Ghi nhận lịch sử movement
        StockMovement movement = new StockMovement();
        movement.setWarehouse(inventory.getWarehouse());
        if (inventory != null) {
            movement.setStore(inventory.getStore());
        }
        movement.setProduct(inventory.getProduct());
        movement.setBatch(inventory.getBatch());
        movement.setQuantity(dto.getDamageQuantity());
        movement.setMovementType(dto.getMovementType());
        movement.setMovementDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        movement.setNote(dto.getNote());
        stockMovementRepository.save(movement);

        return "Đã cập nhật tồn kho và ghi nhận lịch sử hỏng/hết hạn.";
    }

    @Override
    public List<StockMovement> getDamagedAndExpired() {
        return stockMovementRepository.findMovementsByTypes(Arrays.asList("DAMAGE", "EXPIRED"));
    }

    @Override
    public List<StockMovement> getDamagedAndExpiredInStore(Long storeId) {
        return stockMovementRepository.findMovementsByTypesInStore(Arrays.asList("DAMAGE", "EXPIRED"), storeId);
    }
}
