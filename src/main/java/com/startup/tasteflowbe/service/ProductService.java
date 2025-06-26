package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.response.ProductDetailDTO;
import com.startup.tasteflowbe.dto.response.ProductListItemDTO;
import com.startup.tasteflowbe.dto.request.ProductRequestDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    // ✅ Phần quản trị CRUD
    List<ProductResponseDTO> getAllProducts();
    Optional<ProductResponseDTO> getProductById(Long id);
    ProductResponseDTO createProduct(ProductRequestDTO dto);
    ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto);
    void deleteProduct(Long id);

    // ✅ Phần Multi-Unit Inventory support
    List<ProductListItemDTO> getAllProductForList();  // dùng cho FE list card
    ProductDetailDTO getProductDetail(Long productId); // dùng cho FE detail
    Integer countByCategoryId(Long categoryId);
}
