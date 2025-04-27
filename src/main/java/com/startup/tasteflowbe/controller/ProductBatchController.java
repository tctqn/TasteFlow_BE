package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.ProductBatch;
import com.startup.tasteflowbe.service.ProductBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-batches")
@RequiredArgsConstructor
public class ProductBatchController {

    private final ProductBatchService productBatchService;

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
    public ResponseEntity<ProductBatch> createProductBatch(@RequestBody ProductBatch productBatch) {
        return ResponseEntity.ok(productBatchService.createProductBatch(productBatch));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductBatch> updateProductBatch(@PathVariable Long id, @RequestBody ProductBatch productBatch) {
        return ResponseEntity.ok(productBatchService.updateProductBatch(id, productBatch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductBatch(@PathVariable Long id) {
        productBatchService.deleteProductBatch(id);
        return ResponseEntity.noContent().build();
    }
}
