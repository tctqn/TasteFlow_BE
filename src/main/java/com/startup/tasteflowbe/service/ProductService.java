package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.response.ProductDetailDTO;
import com.startup.tasteflowbe.dto.response.ProductListItemDTO;
import com.startup.tasteflowbe.dto.request.ProductRequestDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;
import com.startup.tasteflowbe.model.Product;

import io.jsonwebtoken.io.IOException;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    // ✅ Phần quản trị CRUD
    List<Product> getAllProducts();

    Optional<ProductResponseDTO> getProductById(Long id);

    void saveAll(List<ProductRequestDTO> dtos); // dùng để import từ file Excel

    ProductResponseDTO createProduct(ProductRequestDTO dto);

    void readProductsFromExcel(MultipartFile file) throws IOException, java.io.IOException;

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto);

    void deleteProduct(Long id);

    // ✅ Phần Multi-Unit Inventory support
    List<ProductListItemDTO> getAllProductForList(); // dùng cho FE list card

    ProductDetailDTO getProductDetail(Long productId); // dùng cho FE detail

    Integer countByCategoryId(Long categoryId);
}
