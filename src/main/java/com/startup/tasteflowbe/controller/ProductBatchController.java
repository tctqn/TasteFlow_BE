package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.ProductBatchDTO;
import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.ProductBatch;
import com.startup.tasteflowbe.service.InventoryService;
import com.startup.tasteflowbe.service.ProductBatchService;
import com.startup.tasteflowbe.service.StockMovementService;
import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.model.Warehouse;
import com.startup.tasteflowbe.model.Unit;
import com.startup.tasteflowbe.model.Supplier;
import com.startup.tasteflowbe.repository.UnitRepository;
import com.startup.tasteflowbe.repository.ProductRepository;
import com.startup.tasteflowbe.repository.SupplierRepository;
import com.startup.tasteflowbe.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/product-batches")
@RequiredArgsConstructor
public class ProductBatchController {

    private final ProductBatchService productBatchService;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final UnitRepository unitRepository;

    @GetMapping
    public ResponseEntity<List<ProductBatch>> getAllProductBatches() {
        return ResponseEntity.ok(productBatchService.getAllProductBatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductBatch> getProductBatchById(@PathVariable Long id) {
        return productBatchService.getProductBatchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductBatch> createProductBatch(@RequestBody ProductBatchDTO productBatchDTO) {
        ProductBatch productBatch = new ProductBatch();
        System.out.println("Received ProductBatchDTO: " + productBatchDTO);
        // Truy vấn thực thể từ cơ sở dữ liệu
        Product product = productRepository.findById(productBatchDTO.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found with ID: " + productBatchDTO.getProductId()));
        Supplier supplier = supplierRepository.findById(productBatchDTO.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Supplier not found with ID: " + productBatchDTO.getSupplierId()));
        Warehouse warehouse = warehouseRepository.findById(productBatchDTO.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Warehouse not found with ID: " + productBatchDTO.getWarehouseId()));
        Unit unit = unitRepository.findById(productBatchDTO.getUnitId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Unit not found with ID: " + productBatchDTO.getUnitId()));

        // Gán giá trị vào ProductBatch
        productBatch.setProduct(product);
        productBatch.setSupplier(supplier);
        productBatch.setWarehouse(warehouse);
        productBatch.setUnit(unit);
        productBatch.setQuantity(productBatchDTO.getQuantity());
        productBatch.setManufactureDate(productBatchDTO.getManufactureDate());
        productBatch.setNote(productBatchDTO.getNote());
        productBatch.setReceivedDate(LocalDateTime.now());
        productBatch.setImportPrice(BigDecimal.ZERO);
        productBatch.setStatus("CREATED");

        return ResponseEntity.ok(productBatchService.createProductBatch(productBatch));
    }

    @PostMapping("/add-batch")
    public ResponseEntity<ProductBatch> addNewBatch(@RequestBody ProductBatch productBatch) {
        productBatchService.addNewBatch(productBatch);
        return ResponseEntity.ok(productBatch);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductBatch> updateProductBatch(@PathVariable Long id,
            @RequestBody ProductBatch productBatch) {
        return ResponseEntity.ok(productBatchService.updateProductBatch(id, productBatch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductBatch(@PathVariable Long id) {
        productBatchService.deleteProductBatch(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/warehouse")
    public ResponseEntity<List<ProductBatch>> getProductBatchByWarehouseId(@RequestHeader Long managerId) {
        return ResponseEntity.ok(productBatchService.getProductBatchByWarehouseId(managerId));
    }
}
