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

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
    private final UnitRepository unitRepository;
    private final ProductMapper productMapper;

    // ✅ Phần CRUD Admin cũ giữ nguyên
    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<ProductResponseDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse);
    }

    @Override
    public void saveAll(List<ProductRequestDTO> dtos) {
        List<Product> products = dtos.stream()
                .map(productMapper::toEntity)
                .collect(Collectors.toList());

        productRepository.saveAll(products);
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        Product product = productMapper.toEntity(dto);

        Category category = dto.getCategory();

        List<Promotion> promotions = promotionRepository.findAllById(
                dto.getPromotionIds() != null ? dto.getPromotionIds() : Collections.emptyList());

        product.setCategory(category);
        product.setPromotions(promotions);

        // Lưu product để có ID sinh ra từ DB
        productRepository.saveAndFlush(product); // Lúc này product.getProductId() đã có

        // Tạo ProductUnit
        ProductUnit productUnit = new ProductUnit();
        productUnit.setProduct(product);
        productUnit.setPrice(dto.getPrice());
        productUnit.setUnit(dto.getUnit());
        productUnit.setSku(dto.getSku());
        productUnit.setImageUrl(dto.getImageUrl());
        productUnit.setDescription(dto.getDescription());
        productUnit.setConversionRate(1);
        productUnit.setIsBaseUnit(true);

        // Gán ProductUnit vào danh sách
        List<ProductUnit> units = new ArrayList<>();
        units.add(productUnit);
        product.setProductUnits(units);

        // Lưu ProductUnit
        productUnitRepository.save(productUnit);

        return productMapper.toResponse(product);
    }

    @Override
    public void readProductsFromExcel(MultipartFile file) throws IOException, java.io.IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                continue; // Bỏ dòng tiêu đề

            try {
                Product product = new Product();

                // Cột 0: Tên sản phẩm
                Cell nameCell = row.getCell(0);
                String name = (nameCell != null && nameCell.getCellType() == CellType.STRING)
                        ? nameCell.getStringCellValue()
                        : nameCell != null && nameCell.getCellType() == CellType.NUMERIC
                                ? String.valueOf((long) nameCell.getNumericCellValue())
                                : "";
                product.setName(name);

                // Cột 1: ID category
                Cell categoryCell = row.getCell(1);
                if (categoryCell == null || categoryCell.getCellType() != CellType.NUMERIC) {
                    throw new IllegalArgumentException("Category ID must be numeric at row " + row.getRowNum());
                }
                long categoryId = (long) categoryCell.getNumericCellValue();
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found with id " + categoryId));
                product.setCategory(category);
                product.setPromotions(Collections.emptyList());

                // Cột 2: isDraft
                Cell draftCell = row.getCell(2);
                Boolean isDraft = (draftCell != null && draftCell.getCellType() == CellType.BOOLEAN)
                        ? draftCell.getBooleanCellValue()
                        : draftCell != null && draftCell.getCellType() == CellType.STRING
                                ? Boolean.parseBoolean(draftCell.getStringCellValue())
                                : false;
                product.setIsDraft(isDraft);

                productRepository.saveAndFlush(product);

                ProductUnit productUnit = new ProductUnit();
                productUnit.setProduct(product);

                // Cột 3: Price
                Cell priceCell = row.getCell(3);
                double price = (priceCell != null && priceCell.getCellType() == CellType.NUMERIC)
                        ? priceCell.getNumericCellValue()
                        : Double.parseDouble(priceCell.getStringCellValue());
                productUnit.setPrice(BigDecimal.valueOf(price));

                // Cột 4: Unit ID
                Cell unitCell = row.getCell(4);
                if (unitCell == null || unitCell.getCellType() != CellType.NUMERIC) {
                    throw new IllegalArgumentException("Unit ID must be numeric at row " + row.getRowNum());
                }
                long unitId = (long) unitCell.getNumericCellValue();
                Unit unit = unitRepository.findById(unitId)
                        .orElseThrow(() -> new RuntimeException("Unit not found with id " + unitId));
                productUnit.setUnit(unit);

                // Cột 5: SKU
                Cell skuCell = row.getCell(5);
                String sku = (skuCell != null)
                        ? (skuCell.getCellType() == CellType.STRING
                                ? skuCell.getStringCellValue()
                                : String.valueOf((long) skuCell.getNumericCellValue()))
                        : "";
                productUnit.setSku(sku);

                // Cột 6: Image URL
                Cell imageCell = row.getCell(6);
                String imageUrl = (imageCell != null)
                        ? (imageCell.getCellType() == CellType.STRING
                                ? imageCell.getStringCellValue()
                                : String.valueOf(imageCell.getNumericCellValue()))
                        : "";
                productUnit.setImageUrl(imageUrl);

                // Cột 7: Mô tả
                Cell descCell = row.getCell(7);
                String description = (descCell != null)
                        ? (descCell.getCellType() == CellType.STRING
                                ? descCell.getStringCellValue()
                                : String.valueOf(descCell.getNumericCellValue()))
                        : "";
                productUnit.setDescription(description);

                productUnit.setConversionRate(1);
                productUnit.setIsBaseUnit(true);

                product.setProductUnits(Collections.singletonList(productUnit));

                productUnitRepository.save(productUnit);
            } catch (Exception e) {
                throw new RuntimeException("Error at row " + row.getRowNum() + ": " + e.getMessage(), e);
            }
        }

        workbook.close();
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        return productRepository.findById(id)
                .map(existing -> {
                    productMapper.updateEntityFromDTO(dto, existing);
                    Category category = dto.getCategory();
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

    @Override
    public List<ProductListItemDTO> getAllProductForList() {
        List<ProductUnit> allUnits = productUnitRepository.findAll();
        Map<Long, ProductBatch> productToLatestBatch = new HashMap<>();

        return allUnits.stream().map(unit -> {
            ProductListItemDTO dto = productMapper.productUnitToProductListItemDTO(unit);

            Long productId = unit.getProduct().getProductId();

            ProductBatch batch = productToLatestBatch.computeIfAbsent(productId, pid -> productBatchRepository
                    .findTopByProductOrderByReceivedDateDesc(unit.getProduct()).orElse(null));

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
