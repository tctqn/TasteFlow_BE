package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.InventoryRequestDTO;
import com.startup.tasteflowbe.dto.request.StoreInventoryRequestDTO;
import com.startup.tasteflowbe.dto.response.*;
import com.startup.tasteflowbe.model.*;
import com.startup.tasteflowbe.repository.InventoryRepository;
import com.startup.tasteflowbe.repository.ProductBatchRepository;
import com.startup.tasteflowbe.repository.ProductRepository;
import com.startup.tasteflowbe.repository.ProductUnitRepository;
import com.startup.tasteflowbe.repository.WarehouseRepository;
import com.startup.tasteflowbe.service.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    private final WarehouseService warehouseService;

    private final ProductBatchService productBatchService;

    private final StoreRequestService storeRequestService;

    private final StoreService storeService;
    private final ProductRepository productRepository;
    private final ProductUnitRepository productUnitRepository;
    private final ProductBatchRepository productBatchRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    public List<Inventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    @Override
    public Optional<Inventory> getInventoryById(Long id) {
        return inventoryRepository.findById(id);
    }

    @Override
    @Transactional
    public Inventory createInventory(InventoryRequestDTO inventoryRequestDTO) {
        // 1. Lấy các entity liên quan từ DB bằng ID trong DTO
        ProductBatch productBatch = productBatchRepository.findById(inventoryRequestDTO.getBatchId())
                .orElseThrow(() -> new RuntimeException(
                        "ProductBatch not found with id " + inventoryRequestDTO.getBatchId()));

        Warehouse warehouse = warehouseRepository.findById(inventoryRequestDTO.getWarehouseId())
                .orElseThrow(() -> new RuntimeException(
                        "Warehouse not found with id " + inventoryRequestDTO.getWarehouseId()));

        Product product = productRepository.findById(inventoryRequestDTO.getProductId())
                .orElseThrow(
                        () -> new RuntimeException("Product not found with id " + inventoryRequestDTO.getProductId()));

        // 2. Thực hiện logic nghiệp vụ
        productBatch.setStatus("STOCKED");
        if (productBatch.getSupplier() == null) {
            throw new IllegalArgumentException("Supplier must not be null for ProductBatch.");
        }
        productBatchRepository.save(productBatch);

        // 3. Tạo entity Inventory mới và set giá trị
        Inventory newInventory = new Inventory();
        newInventory.setWarehouse(warehouse);
        newInventory.setProduct(product);
        newInventory.setBatch(productBatch);
        newInventory.setQuantity(inventoryRequestDTO.getQuantity());
        newInventory.setReorderLevel(inventoryRequestDTO.getReorderLevel());

        // 4. Lưu và trả về entity mới
        return inventoryRepository.save(newInventory);
    }

    @Override
    public void createStoreInventory(StoreInventoryRequestDTO storeInventoryRequestDTO) {
        System.out.print("Data here tuyennq223:" + storeInventoryRequestDTO);
        Inventory inventory = new Inventory();
        ProductBatch productBatch = productBatchService.getProductBatchById(storeInventoryRequestDTO.getBatchId())
                .orElseThrow(() -> new RuntimeException(
                        "ProductBatch not found with id " + storeInventoryRequestDTO.getBatchId()));
        Warehouse warehouse = warehouseService.getWarehouseById(storeInventoryRequestDTO.getWarehouseId())
                .orElseThrow(() -> new RuntimeException(
                        "Warehouse not found with id " + storeInventoryRequestDTO.getWarehouseId()));
        StoreRequest storeRequest = storeRequestService.getStoreRequestById(storeInventoryRequestDTO.getRequestId())
                .orElseThrow(() -> new RuntimeException(
                        "Store request not found with id " + storeInventoryRequestDTO.getRequestId()));
        Store store = storeService.getStoreById(storeInventoryRequestDTO.getStoreId())
                .orElseThrow(() -> new RuntimeException(
                        "Store not found with id " + storeInventoryRequestDTO.getStoreId()));

        inventory.setBatch(productBatch);
        inventory.setProduct(productBatch.getProduct());
        inventory.setQuantity(storeInventoryRequestDTO.getQuantity());
        inventory.setStore(store);
        inventory.setReorderLevel(storeInventoryRequestDTO.getReorder_level());
        inventory.setWarehouse(warehouse);

        inventoryRepository.save(inventory);

        storeRequestService.updateStoreRequestStatus(storeRequest.getRequestId(), storeInventoryRequestDTO.getStatus());
    }

    @Override
    public Inventory updateInventory(Long id, Inventory updatedInventory) {
        return inventoryRepository.findById(id)
                .map(inventory -> {
                    inventory.setWarehouse(updatedInventory.getWarehouse());
                    inventory.setStore(updatedInventory.getStore());
                    inventory.setProduct(updatedInventory.getProduct());
                    inventory.setBatch(updatedInventory.getBatch());
                    inventory.setQuantity(updatedInventory.getQuantity());
                    inventory.setReorderLevel(updatedInventory.getReorderLevel());
                    return inventoryRepository.save(inventory);
                })
                .orElseThrow(() -> new RuntimeException("Inventory not found with id " + id));
    }

    @Override
    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }

    @Override
    public List<Inventory> findInventoriesByStoreId(Long storeId) {
        return inventoryRepository.findByStore_StoreId(storeId);
    }

    @Override
    @Transactional
    public List<ProductInventoryDTO> getInventoryAllUnitByStore(Long storeId) {
        // Lấy tất cả inventory của cửa hàng để suy ra danh sách productId có hàng tại store
        List<Inventory> allInvAtStore = inventoryRepository.findByStore_StoreId(storeId);

        Set<Long> productIds = allInvAtStore.stream()
                .map(Inventory::getProduct)
                .filter(Objects::nonNull)
                .map(Product::getProductId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LocalDate today = LocalDate.now();
        List<ProductInventoryDTO> result = new ArrayList<>();

        for (Long productId : productIds) {
            // CHỈ lấy tồn kho còn hàng và CHƯA HẾT HẠN, sort FEFO (expiry asc)
            List<Inventory> validInventories =
                    inventoryRepository
                            .findByStore_StoreIdAndProduct_ProductIdAndQuantityGreaterThanAndBatch_ExpirationDateAfterOrderByBatch_ExpirationDateAsc(
                                    storeId, productId, 0, today);

            // Tổng số lượng base từ các lô hợp lệ
            int baseQty = validInventories.stream()
                    .mapToInt(Inventory::getQuantity)
                    .sum();

            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) continue;

            List<ProductUnit> units = productUnitRepository
                    .findByProduct_ProductId(productId)
                    .orElse(Collections.emptyList());

            List<ProductUnitStockDTO> unitStocks = units.stream()
                    .map(unit -> new ProductUnitStockDTO(
                            unit.getUnit().getName(),
                            unit.getConversionRate(),
                            baseQty / unit.getConversionRate() // làm tròn xuống
                    ))
                    .collect(Collectors.toList());

            result.add(new ProductInventoryDTO(productId, product.getName(), unitStocks));
        }

        return result;
    }


    @Override
    public int getAvailableStock(Long storeId, Long productId, Long unitId) {
        return inventoryRepository.findAvailableQuantity(storeId, productId, unitId);
    }

    @Override
    public List<Inventory> findInventoriesByWarehouseId(Long warehouseId) {
        return inventoryRepository.findByWarehouse_WarehouseId(warehouseId);
    }

    @Override
    public List<WarehouseProductDTO> getWarehouseProductsByWarehouseId(Long warehouseId) {
        List<Product> products = inventoryRepository.findDistinctProductsByWarehouseId(warehouseId);
        List<WarehouseProductDTO> result = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        for (Product product : products) {
            List<Inventory> inventories = inventoryRepository.findByWarehouse_WarehouseId(warehouseId)
                    .stream().filter(inv -> inv.getProduct().getProductId().equals(product.getProductId())).toList();
            int totalQuantity = inventories.stream().mapToInt(Inventory::getQuantity).sum();
            if (inventories.isEmpty() || totalQuantity <= 0)
                continue;
            ProductUnit baseUnit = product.getProductUnits().stream()
                    .filter(ProductUnit::getIsBaseUnit)
                    .findFirst()
                    .orElse(product.getProductUnits().get(0));
            String sku = baseUnit.getSku();
            String unitName = baseUnit.getUnit().getName();
            Double salePrice = baseUnit.getPrice() != null ? baseUnit.getPrice().doubleValue() : null;
            int totalBatches = (int) inventories.stream()
                    .filter(inv -> inv.getQuantity() != null && inv.getQuantity() > 0).map(Inventory::getBatch)
                    .distinct().count();
            List<Double> importPrices = inventories.stream()
                    .map(inv -> inv.getBatch().getImportPrice())
                    .filter(java.util.Objects::nonNull)
                    .map(java.math.BigDecimal::doubleValue)
                    .toList();
            Double minImportPrice = importPrices.stream().min(Double::compareTo).orElse(null);
            Double maxImportPrice = importPrices.stream().max(Double::compareTo).orElse(null);
            Double avgImportPrice = importPrices.isEmpty() ? null
                    : importPrices.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            Integer reorderLevel = inventories.get(0).getReorderLevel();

            boolean hasExpiredBatch = false;
            boolean hasBatchBelow30 = false;
            boolean hasBatchBelow10 = false;
            List<Double> percentRemainings = new ArrayList<>();
            for (Inventory inv : inventories) {
                LocalDate manufactureDate = inv.getBatch().getManufactureDate();
                LocalDate expiryDate = inv.getBatch().getExpirationDate();
                if (manufactureDate != null && expiryDate != null) {
                    long totalDays = ChronoUnit.DAYS.between(manufactureDate, expiryDate);
                    long remainingDays = ChronoUnit.DAYS.between(currentDate, expiryDate);
                    double percentRemaining = totalDays > 0 ? (double) remainingDays / totalDays : 0.0;
                    percentRemainings.add(percentRemaining);
                    if (remainingDays < 0) {
                        hasExpiredBatch = true;
                    } else if (percentRemaining < 0.1) {
                        hasBatchBelow10 = true;
                    } else if (percentRemaining < 0.3) {
                        hasBatchBelow30 = true;
                    }
                }
            }
            String expiryStatus;
            if (hasExpiredBatch) {
                expiryStatus = "CÓ LÔ HẾT HẠN";
            } else if (hasBatchBelow10) {
                expiryStatus = "GẦN HẾT HẠN GẤP";
            } else if (hasBatchBelow30) {
                expiryStatus = "SẮP HẾT HẠN";
            } else {
                expiryStatus = "BÌNH THƯỜNG";
            }

            String status = totalQuantity <= reorderLevel ? "SẮP HẾT HÀNG" : "BÌNH THƯỜNG";
            WarehouseProductDTO dto = WarehouseProductDTO.builder()
                    .sku(sku)
                    .productId(product.getProductId())
                    .productName(product.getName())
                    .unitName(unitName)
                    .salePrice(salePrice)
                    .totalQuantity(totalQuantity)
                    .totalBatches(totalBatches)
                    .minImportPrice(minImportPrice)
                    .maxImportPrice(maxImportPrice)
                    .avgImportPrice(avgImportPrice)
                    .reorderLevel(reorderLevel)
                    .status(status)
                    .expiryStatus(expiryStatus)
                    .build();
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<BatchDetailDTO> getBatchDetailsByProductAndWarehouseOrStore(Long productId, Long warehouseId,
            Long storeId) {
        List<Inventory> inventories = inventoryRepository.findByProductAndWarehouseOrStore(productId, warehouseId,
                storeId);
        if (warehouseId != null) {
            inventories = inventories.stream().filter(inv -> inv.getStore() == null).toList();
        }
        List<BatchDetailDTO> batchDetails = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        for (Inventory inv : inventories) {
            if (inv.getQuantity() == null || inv.getQuantity() <= 0)
                continue;
            BatchDetailDTO dto = new BatchDetailDTO();
            dto.setProductId(inv.getProduct() != null ? inv.getProduct().getProductId() : null);
            dto.setWarehouseId(inv.getWarehouse() != null ? inv.getWarehouse().getWarehouseId() : null);
            dto.setStoreId(inv.getStore() != null ? inv.getStore().getStoreId() : null);
            dto.setBatchId(inv.getBatch().getBatchId());
            dto.setProductName(inv.getProduct().getName());
            dto.setQuantity(inv.getQuantity());
            dto.setReceivedDate(inv.getBatch().getReceivedDate());
            dto.setManufactureDate(inv.getBatch().getManufactureDate());
            dto.setExpirationDate(inv.getBatch().getExpirationDate());
            dto.setStatus(inv.getBatch().getStatus());
            dto.setNote(inv.getBatch().getNote());
            dto.setSupplierName(inv.getBatch().getSupplier() != null ? inv.getBatch().getSupplier().getName() : null);
            // Lấy đơn vị cơ bản của sản phẩm
            Product product = inv.getProduct();
            String baseUnitName = null;
            if (product != null && product.getProductUnits() != null && !product.getProductUnits().isEmpty()) {
                baseUnitName = product.getProductUnits().stream()
                        .filter(ProductUnit::getIsBaseUnit)
                        .findFirst()
                        .orElse(product.getProductUnits().get(0))
                        .getUnit().getName();
            }
            dto.setUnitName(baseUnitName);

            // Tính trạng thái hạn sử dụng cho từng batch
            LocalDate manufactureDate = inv.getBatch().getManufactureDate();
            LocalDate expiryDate = inv.getBatch().getExpirationDate();
            String expiryStatus = "BÌNH THƯỜNG";
            if (manufactureDate != null && expiryDate != null) {
                long totalDays = ChronoUnit.DAYS.between(manufactureDate, expiryDate);
                long remainingDays = ChronoUnit.DAYS.between(currentDate, expiryDate);
                double percentRemaining = totalDays > 0 ? (double) remainingDays / totalDays : 0.0;
                if (remainingDays < 0) {
                    expiryStatus = "HẾT HẠN";
                } else if (percentRemaining < 0.1) {
                    expiryStatus = "GẦN HẾT HẠN GẤP";
                } else if (percentRemaining < 0.3) {
                    expiryStatus = "SẮP HẾT HẠN";
                }
            }
            dto.setExpiryStatus(expiryStatus);
            batchDetails.add(dto);
        }
        return batchDetails;
    }

    @Override
    public List<StoreProductDTO> getStoreProductsByStoreId(Long storeId) {
        List<Product> products = inventoryRepository.findDistinctProductsByStoreId(storeId);
        List<StoreProductDTO> result = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        for (Product product : products) {
            List<Inventory> inventories = inventoryRepository.findByStore_StoreId(storeId)
                    .stream().filter(inv -> inv.getProduct().getProductId().equals(product.getProductId())).toList();
            int totalQuantity = inventories.stream().mapToInt(Inventory::getQuantity).sum();
            if (inventories.isEmpty() || totalQuantity <= 0)
                continue;
            // ...existing code...
            ProductUnit baseUnit = product.getProductUnits().stream()
                    .filter(ProductUnit::getIsBaseUnit)
                    .findFirst()
                    .orElse(product.getProductUnits().get(0));
            String sku = baseUnit.getSku();
            String unitName = baseUnit.getUnit().getName();
            Double salePrice = baseUnit.getPrice() != null ? baseUnit.getPrice().doubleValue() : null;
            int totalBatches = (int) inventories.stream()
                    .filter(inv -> inv.getQuantity() != null && inv.getQuantity() > 0).map(Inventory::getBatch)
                    .distinct().count();
            Integer reorderLevel = inventories.get(0).getReorderLevel();

            boolean hasExpiredBatch = false;
            boolean hasBatchBelow30 = false;
            boolean hasBatchBelow10 = false;
            for (Inventory inv : inventories) {
                LocalDate manufactureDate = inv.getBatch().getManufactureDate();
                LocalDate expiryDate = inv.getBatch().getExpirationDate();
                if (manufactureDate != null && expiryDate != null) {
                    long totalDays = ChronoUnit.DAYS.between(manufactureDate, expiryDate);
                    long remainingDays = ChronoUnit.DAYS.between(currentDate, expiryDate);
                    double percentRemaining = totalDays > 0 ? (double) remainingDays / totalDays : 0.0;
                    if (remainingDays < 0) {
                        hasExpiredBatch = true;
                    } else if (percentRemaining < 0.1) {
                        hasBatchBelow10 = true;
                    } else if (percentRemaining < 0.3) {
                        hasBatchBelow30 = true;
                    }
                }
            }
            String expiryStatus;
            if (hasExpiredBatch) {
                expiryStatus = "CÓ LÔ HẾT HẠN";
            } else if (hasBatchBelow10) {
                expiryStatus = "GẦN HẾT HẠN GẤP";
            } else if (hasBatchBelow30) {
                expiryStatus = "SẮP HẾT HẠN";
            } else {
                expiryStatus = "BÌNH THƯỜNG";
            }

            String status = totalQuantity <= reorderLevel ? "SẮP HẾT HÀNG" : "BÌNH THƯỜNG";
            StoreProductDTO dto = StoreProductDTO.builder()
                    .productId(product.getProductId())
                    .sku(sku)
                    .productName(product.getName())
                    .unitName(unitName)
                    .salePrice(salePrice)
                    .totalQuantity(totalQuantity)
                    .totalBatches(totalBatches)
                    .reorderLevel(reorderLevel)
                    .status(status)
                    .expiryStatus(expiryStatus)
                    .build();
            result.add(dto);
        }
        return result;
    }

    @Override
    public int updateReorderLevel(Long productId, Long warehouseId, Long storeId, Integer reorderLevel) {
        List<Inventory> inventories = inventoryRepository.findByProductAndWarehouseOrStore(productId, warehouseId,
                storeId);
        for (Inventory inv : inventories) {
            inv.setReorderLevel(reorderLevel);
        }
        inventoryRepository.saveAll(inventories);
        return inventories.size();
    }
}
