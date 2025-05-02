package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.*;
import com.startup.tasteflowbe.model.dto.StoreTransferParam;
import com.startup.tasteflowbe.model.enums.MovementType;
import com.startup.tasteflowbe.repository.*;
import com.startup.tasteflowbe.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final WarehouseRepository warehouseRepository;

    private final ProductRepository productRepository;

    private final ProductBatchRepository productBatchRepository;

    private final InventoryRepository inventoryRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;
    @Autowired
    private StoreRepository storeRepository;

    @Override
    public StockMovement createStockMovement(StockMovement stockMovement) {
        return stockMovementRepository.save(stockMovement);
    }

    @Override
    public StockMovement getStockMovementById(Long movementId) {
        Optional<StockMovement> stockMovement = stockMovementRepository.findById(movementId);
        return stockMovement.orElse(null);
    }

    @Override
    public List<StockMovement> getAllStockMovements() {
        return stockMovementRepository.findAll();
    }

    @Override
    @Transactional
    public void transferToStores(Long warehouseId, Long productId, Long batchId, List<StoreTransferParam> transferList) {
        // Lấy thông tin kho, sản phẩm, và lô hàng từ database
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();
        ProductBatch batch = productBatchRepository.findById(batchId).orElseThrow();

        // Tính tổng số lượng cần chuyển đến các cửa hàng
        int totalQuantity = transferList.stream()
                .mapToInt(StoreTransferParam::getQuantity)
                .sum();

        // Lấy tồn kho hiện tại tại kho đối với sản phẩm và lô hàng tương ứng
        Inventory warehouseInventory = inventoryRepository
                .findByWarehouse_WarehouseIdAndProduct_ProductIdAndBatch_BatchId(warehouseId, productId, batchId)
                .orElseThrow();

        // Kiểm tra xem kho có đủ hàng để chuyển không
        if (warehouseInventory.getQuantity() < totalQuantity) {
            throw new IllegalArgumentException("Không đủ hàng trong kho để chuyển.");
        }

        // Trừ số lượng đã chuyển khỏi kho
        warehouseInventory.setQuantity(warehouseInventory.getQuantity() - totalQuantity);
        inventoryRepository.save(warehouseInventory);

        // Duyệt qua từng cửa hàng trong danh sách chuyển
        for (StoreTransferParam param : transferList) {
            Long storeId = param.getStoreId();
            Integer quantity = param.getQuantity();

            // Tìm tồn kho tại cửa hàng theo sản phẩm và lô hàng
            Inventory storeInventory = inventoryRepository
                    .findByStore_StoreIdAndProduct_ProductIdAndBatch_BatchId(storeId, productId, batchId)
                    .orElse(null);

            // Nếu chưa có tồn kho cho sản phẩm và lô hàng tại cửa hàng, tạo mới
            if (storeInventory == null) {
                storeInventory = new Inventory();
                storeInventory.setStore(storeRepository.findById(storeId).orElseThrow());
                storeInventory.setProduct(product);
                storeInventory.setBatch(batch);
                storeInventory.setQuantity(0); // Bắt đầu từ 0
                storeInventory.setReorderLevel(10); // Mức cảnh báo mặc định
            }

            // Cộng số lượng hàng được chuyển vào tồn kho của cửa hàng
            storeInventory.setQuantity(storeInventory.getQuantity() + quantity);
            inventoryRepository.save(storeInventory);

            // Ghi lại lịch sử di chuyển kho
            StockMovement movement = new StockMovement();
            movement.setWarehouse(warehouse);
            movement.setStore(storeRepository.findById(storeId).orElseThrow());
            movement.setProduct(product);
            movement.setBatch(batch);
            movement.setQuantity(quantity);
            movement.setMovementDate(LocalDateTime.now());
            movement.setMovementType(MovementType.TRANSFER_TO_STORE);
            movement.setNote("Chuyển " + quantity + " sản phẩm đến cửa hàng " + storeId);
            stockMovementRepository.save(movement);
        }
    }


}
