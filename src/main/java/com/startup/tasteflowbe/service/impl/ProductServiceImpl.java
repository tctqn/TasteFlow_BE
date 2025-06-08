package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.ProductRequestDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;
import com.startup.tasteflowbe.mapper.ProductMapper;
import com.startup.tasteflowbe.model.Category;
import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.Promotion;
import com.startup.tasteflowbe.repository.CategoryRepository;
import com.startup.tasteflowbe.repository.ProductRepository;
import com.startup.tasteflowbe.repository.PromotionRepository;
import com.startup.tasteflowbe.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<ProductResponseDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse);
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        Product product = productMapper.toEntity(dto);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id " + dto.getCategoryId()));

        List<Promotion> promotions = promotionRepository.findAllById(dto.getPromotionIds());

        product.setCategory(category);
        product.setPromotions(promotions);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        return productRepository.findById(id)
                .map(existing -> {
                    productMapper.updateEntityFromDTO(dto, existing); // dùng @MappingTarget
                    Category category = categoryRepository.findById(dto.getCategoryId())
                            .orElseThrow(() -> new RuntimeException("Category not found"));

                    List<Promotion> promotions = promotionRepository.findAllById(dto.getPromotionIds());

                    existing.setCategory(category);
                    existing.setPromotions(promotions);

                    return productMapper.toResponse(productRepository.save(existing));
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
