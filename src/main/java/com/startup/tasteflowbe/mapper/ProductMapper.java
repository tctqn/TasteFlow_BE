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

    // ✅ Đã có sẵn từ trước:
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "promotions", target = "promotions")
    ProductResponseDTO toResponse(Product product);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    Product toEntity(ProductRequestDTO dto);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    void updateEntityFromDTO(ProductRequestDTO dto, @MappingTarget Product entity);

    ProductDetailDTO productToProductDetailDTO(Product product);

    @Mapping(source = "unit.name", target = "unitName")
    ProductUnitDTO productUnitToProductUnitDTO(ProductUnit unit);

    @Mapping(source = "product.productId", target = "productId")
    @Mapping(source = "unit.name", target = "unitName")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.imageUrl", target = "imageUrl")
    ProductListItemDTO productUnitToProductListItemDTO(ProductUnit unit);

    List<ProductUnitDTO> productUnitListToProductUnitDTOList(List<ProductUnit> units);
    List<ProductListItemDTO> productUnitListToProductListItemDTOList(List<ProductUnit> units);
}
