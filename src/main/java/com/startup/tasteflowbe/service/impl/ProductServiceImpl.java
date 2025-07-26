package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.response.ProductDetailDTO;
import com.startup.tasteflowbe.dto.response.ProductListItemDTO;
import com.startup.tasteflowbe.dto.response.ProductUnitDTO;
import com.startup.tasteflowbe.dto.request.ProductRequestDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;
import com.startup.tasteflowbe.mapper.ProductMapper;
import com.startup.tasteflowbe.model.*;
import com.startup.tasteflowbe.repository.*;
import com.startup.tasteflowbe.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;
    private final ProductUnitRepository productUnitRepository;
    private final ProductBatchRepository productBatchRepository;
    private final ProductMapper productMapper;

    // ✅ Phần CRUD Admin cũ giữ nguyên
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


        product.setCategory(category);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        return productRepository.findById(id)
                .map(existing -> {
                    productMapper.updateEntityFromDTO(dto, existing);
                    Category category = categoryRepository.findById(dto.getCategoryId())
                            .orElseThrow(() -> new RuntimeException("Category not found"));

                    existing.setCategory(category);

                    return productMapper.toResponse(productRepository.save(existing));
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<ProductListItemDTO> getAllProductForList() {
        List<ProductUnit> allUnits = productUnitRepository.findAll();
        Map<Long, ProductBatch> productToLatestBatch = new HashMap<>();

        return allUnits.stream().map(unit -> {
            ProductListItemDTO dto = productMapper.productUnitToProductListItemDTO(unit);

            Long productId = unit.getProduct().getProductId();

            ProductBatch batch = productToLatestBatch.computeIfAbsent(productId, pid ->
                    productBatchRepository.findTopByProductOrderByReceivedDateDesc(unit.getProduct()).orElse(null)
            );

            if (batch != null) {
                dto.setSupplierName(batch.getSupplier().getName());
            }

            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    public ProductDetailDTO getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductUnit> productUnits = productUnitRepository.findByProduct_ProductId(productId)
                .orElseThrow(() -> new RuntimeException("No product unit found"));

        ProductDetailDTO dto = productMapper.productToProductDetailDTO(product);
        List<ProductUnitDTO> unitDTOs = productMapper.productUnitListToProductUnitDTOList(productUnits);
        dto.setUnits(unitDTOs);

        return dto;
    }

    @Override
    public Integer countByCategoryId(Long categoryId) {
        return productRepository.countByCategory_CategoryId(categoryId);
    }
}
