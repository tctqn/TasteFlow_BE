package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.request.ProductRequestDTO;
import com.startup.tasteflowbe.dto.response.ProductDetailDTO;
import com.startup.tasteflowbe.dto.response.ProductListItemDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;
import com.startup.tasteflowbe.dto.response.ProductUnitDTO;
import com.startup.tasteflowbe.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductListItemDTO>> getAllProductsForList() {
        return ResponseEntity.ok(productService.getAllProductForList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductRequestDTO requestDTO) {
        ProductResponseDTO created = productService.createProduct(requestDTO);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadProductExcel(@RequestParam("file") MultipartFile file) {
        try {
            productService.readProductsFromExcel(file);

            return ResponseEntity.ok("Uploaded and processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDetailDTO requestDTO) {
        ProductResponseDTO updated = productService.updateProduct(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/productUnit")
    public ResponseEntity<ProductUnitDTO> updateProductUnit(
            @PathVariable Long id,
            @RequestBody ProductUnitDTO requestDTO) {
        return ResponseEntity.ok(productService.updateProductUnit(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}