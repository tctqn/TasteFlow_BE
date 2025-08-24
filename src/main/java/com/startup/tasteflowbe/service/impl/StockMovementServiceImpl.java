package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.*;
import com.startup.tasteflowbe.dto.StoreTransferParam;
import com.startup.tasteflowbe.dto.request.StockMovementRequestDTO;
import com.startup.tasteflowbe.dto.request.DamageStockRequestDTO;
import com.startup.tasteflowbe.enums.MovementType;
import com.startup.tasteflowbe.repository.*;
import com.startup.tasteflowbe.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        movement.setMovementDate(java.time.LocalDateTime.now()); // Set ngày giờ hiện tại

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
        // Lấy warehouse và product
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        // Tính tổng quantity cần chuyển
        int totalQuantity = transferList.stream()
                .mapToInt(StoreTransferParam::getQuantity)
                .sum();

        // Lấy tất cả tồn kho theo warehouse + product (không filter batchId)
        List<Inventory> inventories = inventoryRepository
                .findByWarehouse_WarehouseIdAndProduct_ProductIdAndStoreIsNullOrderByBatch_ExpirationDateAsc(
                        warehouseId, productId);

        if (inventories.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy tồn kho cho sản phẩm này trong kho.");
        }

        // Tính tổng tồn kho khả dụng
        int available = inventories.stream().mapToInt(Inventory::getQuantity).sum();
        if (available < totalQuantity) {
            throw new IllegalArgumentException("Không đủ hàng trong kho để chuyển.");
        }

        // Trừ tồn kho từ nhiều batch (FIFO theo expiration_date)
        int remaining = totalQuantity;
        for (Inventory inv : inventories) {
            if (remaining <= 0)
                break;

            int deduct = Math.min(inv.getQuantity(), remaining);
            inv.setQuantity(inv.getQuantity() - deduct);
            inventoryRepository.save(inv);

            // Lưu log stock movement cho từng store dựa trên transferList
            for (StoreTransferParam param : transferList) {
                int storeQty = param.getQuantity();
                if (storeQty <= 0)
                    continue;

                int usedFromThisBatch = Math.min(storeQty, deduct);
                if (usedFromThisBatch > 0) {
                    StockMovement movement = new StockMovement();
                    movement.setWarehouse(warehouse);
                    movement.setStore(storeRepository.findById(param.getStoreId()).orElseThrow());
                    movement.setProduct(product);
                    movement.setBatch(inv.getBatch()); // batch được chọn động
                    movement.setQuantity(usedFromThisBatch);
                    movement.setMovementDate(LocalDateTime.now());
                    movement.setMovementType(MovementType.TRANSFER_TO_STORE);
                    movement.setStoreRequest(storeRequestRepository.findById(requestId).orElseThrow());
                    movement.setNote("Chuyển " + usedFromThisBatch + " sản phẩm từ batch "
                            + inv.getBatch().getBatchId() + " đến cửa hàng " + param.getStoreId());
                    stockMovementRepository.save(movement);

                    // Cập nhật lại số lượng còn cần cho cửa hàng này
                    param.setQuantity(storeQty - usedFromThisBatch);
                    deduct -= usedFromThisBatch;
                }
            }

            remaining -= deduct;
        }

        // Update trạng thái store_request
        StoreRequest storeRequest = storeRequestRepository.findById(requestId).orElseThrow();
        storeRequest.setStatus("Processing");
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
        movement.setMovementDate(LocalDateTime.now());
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
