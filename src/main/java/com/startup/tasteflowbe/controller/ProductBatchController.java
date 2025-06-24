package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.HandleImportProductBatch;
import com.startup.tasteflowbe.dto.ProductBatchDTO;
import com.startup.tasteflowbe.dto.response.ProductBatchResponseDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;
import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.ProductBatch;
import com.startup.tasteflowbe.service.InventoryService;
import com.startup.tasteflowbe.service.ProductBatchService;
import com.startup.tasteflowbe.service.StockMovementService;

import aj.org.objectweb.asm.Handle;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<List<ProductBatchResponseDTO>> getAllProductBatches() {
        List<ProductBatchResponseDTO> dtoList = productBatchService.getAllProductBatches()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductBatch> getProductBatchById(@PathVariable Long id) {
        return productBatchService.getProductBatchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductBatchResponseDTO> createProductBatch(@RequestBody ProductBatchDTO productBatchDTO) {
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
        productBatch.setStatus("PENDING");

        ProductBatch createdBatch = productBatchService.createProductBatch(productBatch);
        ProductBatchResponseDTO responseDTO = convertToDto(createdBatch);
        return ResponseEntity.ok(responseDTO);
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

    @PutMapping("/update/{id}")
    public ResponseEntity<ProductBatchResponseDTO> updateProductBatchStatus(@PathVariable Long id,
            @RequestBody HandleImportProductBatch handleImportProductBatch) {
        String status = handleImportProductBatch.getStatus();
        BigDecimal importPrice = handleImportProductBatch.getImportPrice();
        LocalDate expirationDate = handleImportProductBatch.getExpirationDate();
        if (status == null || importPrice == null || expirationDate == null) {
            return ResponseEntity.badRequest().body(null);
        }
        ProductBatch productBatch = productBatchService.getProductBatchById(id)
                .orElseThrow(() -> new IllegalArgumentException("ProductBatch not found with ID: " + id));
        productBatch.setStatus(status);
        productBatch.setImportPrice(importPrice);
        productBatch.setExpirationDate(expirationDate);
        ProductBatch updatedBatch = productBatchService.updateProductBatch(id, productBatch);
        ProductBatchResponseDTO responseDTO = convertToDto(updatedBatch);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductBatch(@PathVariable Long id) {
        productBatchService.deleteProductBatch(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/warehouse/{id}")
    public ResponseEntity<List<ProductBatchResponseDTO>> getProductBatchByWarehouseId(@PathVariable Long id) {
        List<ProductBatchResponseDTO> dtoList = productBatchService.getProductBatchByWarehouseId(id)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    private ProductBatchResponseDTO convertToDto(ProductBatch productBatch) {
        ProductBatchResponseDTO dto = new ProductBatchResponseDTO();
        dto.setBatchId(productBatch.getBatchId());
        dto.setQuantity(productBatch.getQuantity());
        dto.setManufactureDate(productBatch.getManufactureDate());
        dto.setExpirationDate(productBatch.getExpirationDate());
        dto.setReceivedDate(productBatch.getReceivedDate());
        dto.setStatus(productBatch.getStatus());
        dto.setImportPrice(productBatch.getImportPrice());
        dto.setNote(productBatch.getNote());
        dto.setUnitName(productBatch.getUnit().getName());
        if (productBatch.getProduct() != null) {
            ProductResponseDTO productDto = new ProductResponseDTO();
            Product product = productBatch.getProduct();
            productDto.setProductId(product.getProductId());
            productDto.setName(product.getName());
    //        productDto.setPrice(product.getPrice());
            productDto.setSku(product.getSku());
            productDto.setImageUrl(product.getImageUrl());
            dto.setProduct(productDto);
        }
        if (productBatch.getSupplier() != null) {
            dto.setSupplierId(productBatch.getSupplier());
        }
        if (productBatch.getWarehouse() != null) {
            dto.setWarehouseId(productBatch.getWarehouse());
        }
        return dto;
    }
}
