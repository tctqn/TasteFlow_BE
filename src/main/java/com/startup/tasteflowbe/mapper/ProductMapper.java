package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.response.ProductDetailDTO;
import com.startup.tasteflowbe.dto.response.ProductListItemDTO;
import com.startup.tasteflowbe.dto.response.ProductUnitDTO;
import com.startup.tasteflowbe.dto.request.ProductRequestDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;
import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.ProductUnit;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = PromotionMapper.class)
public interface ProductMapper {

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "imageUrl", expression = "java(getBaseUnitImageUrl(product))")
    @Mapping(target = "description", expression = "java(getBaseUnitDescription(product))")
    @Mapping(target = "price", expression = "java(getBaseUnitPrice(product))")
    @Mapping(target = "sku", expression = "java(getBaseUnitSku(product))")
    ProductResponseDTO toResponse(Product product);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequestDTO dto);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromDTO(ProductRequestDTO dto, @MappingTarget Product entity);

    ProductDetailDTO productToProductDetailDTO(Product product);

    @Mapping(source = "unit.name", target = "unitName")
    @Mapping(source = "sku", target = "sku")
    @Mapping(source = "imageUrl", target = "imageUrl")
    @Mapping(source = "description", target = "description")
    ProductUnitDTO productUnitToProductUnitDTO(ProductUnit unit);

    @Mapping(source = "product.productId", target = "productId")
    @Mapping(source = "unit.name", target = "unitName")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "imageUrl", target = "imageUrl")
    @Mapping(source = "product.category.name", target = "categoryName")
    ProductListItemDTO productUnitToProductListItemDTO(ProductUnit unit);

    List<ProductUnitDTO> productUnitListToProductUnitDTOList(List<ProductUnit> units);

    List<ProductListItemDTO> productUnitListToProductListItemDTOList(List<ProductUnit> units);

    // Helper methods for mapping fields from base unit
    default String getBaseUnitImageUrl(Product product) {
        if (product == null || product.getProductUnits() == null)
            return null;
        return product.getProductUnits().stream()
                .filter(ProductUnit::getIsBaseUnit)
                .findFirst()
                .map(ProductUnit::getImageUrl)
                .orElse(null);
    }

    default String getBaseUnitDescription(Product product) {
        if (product == null || product.getProductUnits() == null)
            return null;
        return product.getProductUnits().stream()
                .filter(ProductUnit::getIsBaseUnit)
                .findFirst()
                .map(ProductUnit::getDescription)
                .orElse(null);
    }

    default java.math.BigDecimal getBaseUnitPrice(Product product) {
        if (product == null || product.getProductUnits() == null)
            return null;
        return product.getProductUnits().stream()
                .filter(ProductUnit::getIsBaseUnit)
                .findFirst()
                .map(ProductUnit::getPrice)
                .orElse(null);
    }

    default String getBaseUnitSku(Product product) {
        if (product == null || product.getProductUnits() == null)
            return null;
        return product.getProductUnits().stream()
                .filter(ProductUnit::getIsBaseUnit)
                .findFirst()
                .map(ProductUnit::getSku)
                .orElse(null);
    }
}
