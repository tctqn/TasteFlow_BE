package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.request.ProductRequestDTO;
import com.startup.tasteflowbe.dto.response.ProductDetailDTO;
import com.startup.tasteflowbe.dto.response.ProductListItemDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;
import com.startup.tasteflowbe.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductListItemDTO>> getAllProductsForList() {
        return ResponseEntity.ok(productService.getAllProductForList());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ProductDetailDTO> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductDetail(id));
    }

    @GetMapping("/count-by-category/{categoryId}")
    public ResponseEntity<Integer> countProductsByCategory(@PathVariable("categoryId") Long categoryId) {
        return ResponseEntity.ok(productService.countByCategoryId(categoryId));
    }
}
