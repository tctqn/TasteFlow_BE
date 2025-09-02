package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.response.ProductDetailDTO;
import com.startup.tasteflowbe.dto.response.ProductListItemDTO;
import com.startup.tasteflowbe.dto.response.ProductUnitDTO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.startup.tasteflowbe.dto.request.ProductRequestDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;
import com.startup.tasteflowbe.enums.DiscountType;
import com.startup.tasteflowbe.mapper.ProductMapper;
import com.startup.tasteflowbe.model.*;
import com.startup.tasteflowbe.repository.*;
import com.startup.tasteflowbe.service.ProductService;

import io.jsonwebtoken.io.IOException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductUnitRepository productUnitRepository;
    private final ProductBatchRepository productBatchRepository;
    private final UnitRepository unitRepository;
    private final ProductMapper productMapper;
    private final S3ServiceImpl s3Service;
    private final PromotionRepository promotionRepository;
    private final InventoryRepository inventoryRepository;

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

        if (dto.getIsDraft() != null && dto.getIsDraft()) {
            product.setIsDraft(true);
        } else {
            product.setIsDraft(false);
        }

        Category category = dto.getCategory();

        product.setCategory(category);

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

        // Tạo QR code và upload lên S3
        String qrUrl = generateAndUploadQrCode(dto.getSku());
        productUnit.setQrCodeUrl(qrUrl);

        // Lưu ProductUnit
        productUnitRepository.save(productUnit);

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public void readProductsFromExcel(MultipartFile file) throws IOException, java.io.IOException {
        System.out.println("Reading products from Excel file: " + file.getOriginalFilename());
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Bỏ qua dòng tiêu đề

                try {
                    // Đọc productId (Cột 0) để xác định là sản phẩm mới hay cũ
                    Cell productIdCell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                    Product product;
                    if (productIdCell == null || productIdCell.getCellType() == CellType.BLANK) {
                        // TRƯỜNG HỢP 1: TẠO SẢN PHẨM MỚI (productId trống)
                        product = createNewProductFromRow(row);
                    } else {
                        // TRƯỜNG HỢP 2: THÊM UNIT CHO SẢN PHẨM CŨ (có productId)
                        long productId = (long) getNumericCellValue(productIdCell, "Product ID");
                        product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found with id " + productId));
                    }

                    // Sau khi có 'product', tạo 'ProductUnit' từ các thông tin còn lại của dòng
                    createProductUnitFromRow(row, product);

                } catch (Exception e) {
                    // Ghi lại lỗi chi tiết của từng dòng và tiếp tục xử lý các dòng tiếp theo
                    System.err.println("Error processing row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                    // Hoặc throw new RuntimeException("Error at row " + row.getRowNum() + ": " +
                    // e.getMessage(), e); nếu muốn dừng hẳn
                }
            }
        }
    }

    private Product createNewProductFromRow(Row row) {
        Product newProduct = new Product();

        // SKU
        String sku = getStringCellValue(row.getCell(5), "SKU");
        if (productUnitRepository.findBySku(sku).isPresent()) {
            throw new IllegalArgumentException("SKU '" + sku + "' already exists. Please use a unique SKU.");
        }

        // Cột 1: Tên sản phẩm
        String name = getStringCellValue(row.getCell(1), "Product Name");
        newProduct.setName(name);
        System.out.println("Processing new product: " + name);

        // Cột 2: ID category
        long categoryId = (long) getNumericCellValue(row.getCell(2), "Category ID");
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id " + categoryId));
        newProduct.setCategory(category);
        newProduct.setIsDraft(false); // Mặc định là không phải bản nháp

        // Lưu product để có ID, cần thiết để ProductUnit tham chiếu
        return productRepository.save(newProduct);
    }

    private void createProductUnitFromRow(Row row, Product product) {
        ProductUnit productUnit = new ProductUnit();
        productUnit.setProduct(product);

        // Cột 3: Unit ID
        long unitId = (long) getNumericCellValue(row.getCell(3), "Unit ID");
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unit not found with id " + unitId));
        productUnit.setUnit(unit);

        // Cột 4: Price
        double price = getNumericCellValue(row.getCell(4), "Price");
        productUnit.setPrice(BigDecimal.valueOf(price));

        // Cột 5: SKU
        String sku = getStringCellValue(row.getCell(5), "SKU");
        if (productUnitRepository.findBySku(sku).isPresent()) {
            throw new IllegalArgumentException("SKU '" + sku + "' already exists. Please use a unique SKU.");
        }
        productUnit.setSku(sku);

        // Cột 6: Image URL
        productUnit.setImageUrl(getStringCellValue(row.getCell(6), "Image URL"));

        // Cột 7: Description
        productUnit.setDescription(getStringCellValue(row.getCell(7), "Description"));

        // Cột 8: Conversion Rate
        double conversionRateDouble = getNumericCellValue(row.getCell(8), "Conversion Rate");
        productUnit.setConversionRate((int) conversionRateDouble);

        // Cột 9: Is Base Unit
        boolean isBaseUnit = getBooleanCellValue(row.getCell(9), "Is Base Unit");
        productUnit.setIsBaseUnit(isBaseUnit);

        // Logic kiểm tra nếu là base unit mới cho sản phẩm mới thì Conversion Rate phải
        // là 1
        if (product.getProductUnits() == null || product.getProductUnits().isEmpty()) { // Đây là unit đầu tiên
            if (!isBaseUnit) {
                throw new IllegalArgumentException(
                        "The first unit for a new product must be a base unit (isBaseUnit=TRUE).");
            }
            if (productUnit.getConversionRate() != 1) {
                throw new IllegalArgumentException("The base unit must have a conversion rate of 1.");
            }
        } else { // Sản phẩm đã có unit từ trước
            if (isBaseUnit) { // Kiểm tra không cho thêm base unit thứ 2
                boolean hasBaseUnit = product.getProductUnits().stream().anyMatch(ProductUnit::getIsBaseUnit);
                if (hasBaseUnit) {
                    throw new IllegalArgumentException("Product already has a base unit. Cannot add another one.");
                }
            }
        }

        productUnitRepository.save(productUnit);
    }

    // --- Helper methods to read cell values safely ---

    private String getStringCellValue(Cell cell, String columnName) {
        if (cell == null || cell.getCellType() == CellType.BLANK)
            return "";
        if (cell.getCellType() == CellType.STRING)
            return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC)
            return String.valueOf(cell.getNumericCellValue());
        return "";
    }

    private double getNumericCellValue(Cell cell, String columnName) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            throw new IllegalArgumentException(columnName + " is required and must be numeric.");
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        columnName + " has invalid numeric value: " + cell.getStringCellValue());
            }
        }
        throw new IllegalArgumentException(columnName + " must be a numeric value.");
    }

    private boolean getBooleanCellValue(Cell cell, String columnName) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            throw new IllegalArgumentException(columnName + " is required and must be TRUE or FALSE.");
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            return Boolean.parseBoolean(cell.getStringCellValue());
        }
        throw new IllegalArgumentException(columnName + " must be a boolean (TRUE/FALSE) value.");
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductDetailDTO dto) {
        return productRepository.findById(id)
                .map(existing -> {
                    productMapper.updateEntityFromDTO(dto, existing);
                    Category category = dto.getCategory();

                    existing.setCategory(category);

                    return productMapper.toResponse(productRepository.save(existing));
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    @Override
    public ProductUnitDTO updateProductUnit(Long id, ProductUnitDTO dto) {
        ProductUnit productUnit = productUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductUnit not found with id " + id));
        productMapper.updateProductUnitEntityFromDTO(dto, productUnit);
        productUnitRepository.save(productUnit);

        return productMapper.productUnitToProductUnitDTO(productUnit);
    }

    @Override
    public void deleteProduct(Long id) {
        ProductUnit unitToDelete = productUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductUnit not found with id " + id));

        Product product = unitToDelete.getProduct();

        // Xóa unit khỏi danh sách units của product
        product.getProductUnits().remove(unitToDelete);
        productUnitRepository.delete(unitToDelete);

        // Nếu không còn unit nào khác → xóa luôn product
        if (product.getProductUnits().isEmpty()) {
            productRepository.delete(product);
            return;
        }

        // Nếu unit bị xóa là baseUnit → gán lại baseUnit khác
        if (Boolean.TRUE.equals(unitToDelete.getIsBaseUnit())) {
            // Chọn unit đầu tiên còn lại và set isBaseUnit = true
            ProductUnit newBaseUnit = product.getProductUnits().get(0);
            newBaseUnit.setIsBaseUnit(true);
            productUnitRepository.save(newBaseUnit);
        }

        // Đảm bảo tất cả các unit khác không bị đánh dấu baseUnit sai
        for (ProductUnit pu : product.getProductUnits()) {
            if (!pu.getProductUnitId().equals(id) && !pu.equals(product.getProductUnits().get(0))) {
                pu.setIsBaseUnit(false);
            }
        }
    }

    @Override
    public List<ProductListItemDTO> getAllProductForList(Long storeId) {
        // 1) Lấy tất cả base units (toàn hệ thống)
        final List<ProductUnit> units = productUnitRepository.findAllBaseUnitsWithProduct();

        // 2) Nếu cần promotion theo store, load trước (để tránh gọi vòng lặp)
        final List<Promotion> activePromos = (storeId == null)
                ? Collections.emptyList()
                : promotionRepository.findActiveForStore(storeId, LocalDateTime.now());

        // 3) Map lô mới nhất để lấy supplier (ƯU TIÊN lô còn hạn)
        final Map<Long, ProductBatch> productToLatestBatch = new HashMap<>();

        // 4) Nếu có storeId, gom danh sách productId -> query tồn kho 1 phát
        Map<Long, Integer> stockMap;
        if (storeId != null && !units.isEmpty()) {
            List<Long> productIds = units.stream()
                    .map(u -> u.getProduct().getProductId())
                    .distinct()
                    .toList();

            // CHỈ tính tồn từ lô còn hạn
            List<ProductStockAgg> rows = inventoryRepository
                    .sumQtyByStoreAndProductIdsNotExpired(
                            storeId,
                            productIds,
                            LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                    );

            stockMap = rows.stream().collect(Collectors.toMap(
                    ProductStockAgg::getProductId,
                    r -> Optional.ofNullable(r.getQty()).orElse(0)
            ));
        } else {
            stockMap = Collections.emptyMap();
        }

        // 5) Duyệt và map DTO
        return units.stream().map(u -> {
            ProductListItemDTO dto = productMapper.productUnitToProductListItemDTO(u);

            Long productId = u.getProduct().getProductId();

            // supplier từ lô mới nhất CÒN HẠN; nếu không có, fallback về lô mới nhất bất kỳ
            ProductBatch latest = productToLatestBatch.computeIfAbsent(
                    productId,
                    __ -> productBatchRepository
                            .findTopByProductAndExpirationDateAfterOrderByReceivedDateDesc(
                                    u.getProduct(),
                                    LocalDate.now()
                            )
                            .orElseGet(() -> productBatchRepository
                                    .findTopByProductOrderByReceivedDateDesc(u.getProduct())
                                    .orElse(null))
            );
            if (latest != null && latest.getSupplier() != null) {
                dto.setSupplierName(latest.getSupplier().getName());
            }

            // Giá khuyến mãi theo store (nếu có)
            BigDecimal base = dto.getPrice() == null ? BigDecimal.ZERO : dto.getPrice();
            BigDecimal best = base;
            String badge = null;
            Long promoId = null;

            if (!activePromos.isEmpty()) {
                Long catId = (u.getProduct().getCategory() != null)
                        ? u.getProduct().getCategory().getCategoryId()
                        : null;

                for (Promotion p : activePromos) {
                    boolean matchProduct = p.getApplicableProducts().isEmpty() ||
                            p.getApplicableProducts().stream().anyMatch(pp -> pp.getProductId().equals(productId));
                    boolean matchCategory = p.getApplicableCategories().isEmpty() ||
                            (catId != null && p.getApplicableCategories().stream().anyMatch(cc -> cc.getCategoryId().equals(catId)));

                    if (!(matchProduct || matchCategory)) continue;

                    BigDecimal discounted = base;
                    if (p.isPercentage()) {
                        BigDecimal pct = Optional.ofNullable(p.getDiscountPercentage()).orElse(BigDecimal.ZERO);
                        discounted = base.subtract(base.multiply(pct).divide(BigDecimal.valueOf(100)));
                        if (discounted.compareTo(best) < 0) {
                            best = discounted.max(BigDecimal.ZERO);
                            badge = "Giảm " + pct.stripTrailingZeros().toPlainString() + "%";
                            promoId = p.getPromotionId();
                        }
                    } else if (p.isAmount()) {
                        BigDecimal amt = Optional.ofNullable(p.getDiscountAmount()).orElse(BigDecimal.ZERO);
                        discounted = base.subtract(amt);
                        if (discounted.compareTo(best) < 0) {
                            best = discounted.max(BigDecimal.ZERO);
                            badge = "-" + amt.stripTrailingZeros().toPlainString();
                            promoId = p.getPromotionId();
                        }
                    }
                }
            }

            dto.setDiscountedPrice(best);
            dto.setPromoBadge(badge);
            dto.setAppliedPromotionId(promoId);

            // Tồn kho theo store (nếu có storeId); nếu null => admin, không set
            if (storeId != null) {
                int qty = stockMap.getOrDefault(productId, 0);
                dto.setAvailableQty(qty);
                dto.setOutOfStock(qty <= 0); // chỉ còn lô hết hạn -> qty = 0
            }

            return dto;
        }).toList();
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

    public String generateAndUploadQrCode(String sku) {
        try {
            int width = 300;
            int height = 300;

            BitMatrix bitMatrix = new MultiFormatWriter().encode(sku, BarcodeFormat.QR_CODE, width, height);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            baos.close();

            // Upload và trả về URL
            return s3Service.uploadImageFromBytes(imageBytes, sku + "_qr.png");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate or upload QR code", e);
        }
    }

}
