package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.ProductUnit;
import com.startup.tasteflowbe.service.ProductUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-units")
@RequiredArgsConstructor
public class ProductUnitController {

    private final ProductUnitService productUnitService;

    @GetMapping
    public ResponseEntity<List<ProductUnit>> getAllProductUnits() {
        return ResponseEntity.ok(productUnitService.getAllProductUnits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductUnit> getProductUnitById(@PathVariable Long id) {
        return productUnitService.getProductUnitById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductUnit> createProductUnit(@RequestBody ProductUnit productUnit) {
        return ResponseEntity.ok(productUnitService.createProductUnit(productUnit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductUnit> updateProductUnit(@PathVariable Long id, @RequestBody ProductUnit productUnit) {
        return ResponseEntity.ok(productUnitService.updateProductUnit(id, productUnit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductUnit(@PathVariable Long id) {
        productUnitService.deleteProductUnit(id);
        return ResponseEntity.noContent().build();
    }
}
