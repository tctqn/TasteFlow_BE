package com.startup.tasteflowbe.service.impl;

import org.springframework.stereotype.Service;
import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GoogleSearch;
import com.google.genai.types.Part;
import com.google.genai.types.SafetySetting;
import com.google.genai.types.ThinkingConfig;
import com.google.genai.types.Tool;
import com.startup.tasteflowbe.dto.request.GeminiRequest;
import com.startup.tasteflowbe.dto.response.GeminiResponse;
import com.startup.tasteflowbe.model.Inventory;
import com.startup.tasteflowbe.model.Order;
import com.startup.tasteflowbe.model.OrderItem;
import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.ProductBatch;
import com.startup.tasteflowbe.model.ProductUnit;
import com.startup.tasteflowbe.model.Promotion;
import com.startup.tasteflowbe.model.Store;
import com.startup.tasteflowbe.model.Voucher;
import com.startup.tasteflowbe.repository.CategoryRepository;
import com.startup.tasteflowbe.repository.InventoryRepository;
import com.startup.tasteflowbe.repository.OrderItemRepository;
import com.startup.tasteflowbe.repository.OrderRepository;
import com.startup.tasteflowbe.repository.ProductBatchRepository;
import com.startup.tasteflowbe.repository.ProductRepository;
import com.startup.tasteflowbe.repository.ProductUnitRepository;
import com.startup.tasteflowbe.repository.PromotionRepository;
import com.startup.tasteflowbe.repository.StoreRepository;
import com.startup.tasteflowbe.repository.VoucherRepository;
import com.startup.tasteflowbe.service.AIProcessService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AIProcessServiceImpl implements AIProcessService {

    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductBatchRepository productBatchRepository;
    private final ProductUnitRepository productUnitRepository;
    private final VoucherRepository voucherRepository;
    private final Client client;

    @Override
    public GeminiResponse analyzeResponse(String prompt) {
        List<Product> products = productRepository.findAll();
        List<Promotion> promotions = promotionRepository.findAll();
        List<Store> stores = storeRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        List<Inventory> inventories = inventoryRepository.findAll();

        String promtBuild = String.format("""
                NHIỆM VỤ: Phân tích dữ liệu và đưa ra gợi ý cho người dùng dựa trên promt đưa vào

                THÔNG TIN:
                - Promt: %s
                - Sản phẩm: %d
                - Khuyến mãi: %d
                - Cửa hàng: %d
                - Đơn hàng: %d
                - Tồn kho: %d

                YÊU CẦU OUTPUT:
                1. Viết đoạn văn tiếng Việt (100-120 từ)
                2. Cấu trúc: Tóm tắt dữ liệu → Phân tích insights → 2-3 gợi ý hành động
                3. Tone: Chuyên nghiệp, thực tế, actionable
                4. Không sử dụng markdown hay bullet points
                """,
                prompt, products.size(), promotions.size(), stores.size(), orders.size(), inventories.size());

        GeminiRequest geminiRequest = new GeminiRequest();
        geminiRequest.setPrompt(promtBuild);
        return processPrompt(geminiRequest);
    }

    @Override
    public GeminiResponse getTopSellingProductsReport(Long storeId, String season) {
        List<Map<String, Object>> data = storeRepository.findTopSellingProductsInSeason(storeId, 3, season);
        if (data.isEmpty()) {
            GeminiResponse response = new GeminiResponse();
            response.setOutput("Không có dữ liệu bán hàng cho mùa " + season + " từ 3 năm trước.");
            return response;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("NHIỆM VỤ: Phân tích dữ liệu bán hàng và đưa ra báo cáo chuyên nghiệp cho quản lý cửa hàng.\n\n");

        prompt.append("DỮ LIỆU: Top 5 sản phẩm bán chạy nhất mùa ")
                .append(season).append(" (3 năm trước) - Cửa hàng ID: ").append(storeId).append("\n");

        for (Map<String, Object> row : data) {
            prompt.append("• ").append(row.get("productName"))
                    .append(": ").append(row.get("totalSold")).append(" đơn vị\n");
        }

        prompt.append("\nYÊU CẦU OUTPUT:\n");
        prompt.append("1. Viết bằng tiếng Việt, văn phong chuyên nghiệp\n");
        prompt.append("2. Cấu trúc: Tóm tắt xu hướng → Phân tích chi tiết → 3 khuyến nghị cụ thể\n");
        prompt.append("3. Độ dài: 150-200 từ\n");
        prompt.append("4. Không sử dụng markdown, bullet points hay ký tự đặc biệt\n");
        prompt.append("5. Tập trung vào actionable insights cho quản lý\n\n");

        GeminiRequest geminiRequest = new GeminiRequest();
        geminiRequest.setPrompt(prompt.toString());
        return processPrompt(geminiRequest);
    }

    @Override
    public GeminiResponse processPrompt(GeminiRequest request) {
        List<Tool> tools = new ArrayList<>();
        tools.add(Tool.builder()
                .googleSearch(GoogleSearch.builder().build())
                .build());

        String model = "gemini-2.5-flash";
        List<Content> contents = ImmutableList.of(
                Content.builder()
                        .role("user")
                        .parts(ImmutableList.of(Part.fromText(request.getPrompt())))
                        .build());

        GenerateContentConfig config = GenerateContentConfig.builder()
                .thinkingConfig(ThinkingConfig.builder().thinkingBudget(0).build())
                .safetySettings(ImmutableList.of(
                        SafetySetting.builder()
                                .category("HARM_CATEGORY_DANGEROUS_CONTENT")
                                .threshold("BLOCK_ONLY_HIGH")
                                .build()))
                .tools(tools)
                .build();

        List<String> results = new ArrayList<>();
        try (ResponseStream<GenerateContentResponse> responseStream = client.models.generateContentStream(model,
                contents, config)) {

            for (GenerateContentResponse res : responseStream) {
                if (res.candidates().isEmpty()
                        || res.candidates().get().get(0).content().isEmpty()
                        || res.candidates().get().get(0).content().get().parts().isEmpty()) {
                    continue;
                }
                for (Part part : res.candidates().get().get(0).content().get().parts().get()) {
                    part.text().ifPresent(text -> {
                        String cleaned = text
                                .replace("*", "")
                                .replaceAll("\\r?\\n", " ")
                                .replaceAll("\\t", " ")
                                .replace("\\", "")
                                .replace("/", "")
                                .replace("\"", "")
                                .replaceAll("\\s{2,}", " ")
                                .trim();
                        results.add(cleaned);
                    });
                }
            }
        }

        GeminiResponse response = new GeminiResponse();
        response.setOutput(String.join("", results));
        return response;
    }

    @Override
    public GeminiResponse customerAdvisor(Long customerId) {
        List<Order> orders = orderRepository.findOrdersByUser_UserId(customerId);

        int totalOrders = orders != null ? orders.size() : 0;
        int totalOrderItems = 0;
        double totalSpent = 0.0;

        if (orders != null) {
            for (Order o : orders) {
                List<OrderItem> oi = orderItemRepository.findByOrder_OrderId(o.getOrderId());
                totalOrderItems += (oi != null ? oi.size() : 0);
                if (o.getTotalPrice() != null) {
                    totalSpent += o.getTotalPrice().doubleValue();
                }
            }
        }

        long activePromotions = promotionRepository != null ? promotionRepository.count() : 0L;

        String prompt = String.format("""
                NHIỆM VỤ: Tư vấn cá nhân hóa cho khách hàng TasteFlow

                THÔNG TIN KHÁCH HÀNG:
                - ID: %d
                - Tổng đơn hàng: %d
                - Tổng sản phẩm đã mua: %d
                - Tổng chi tiêu: %.0f VND
                - Khuyến mãi hiện tại: %d chương trình

                YÊU CẦU OUTPUT:
                1. Viết 1 đoạn văn tiếng Việt (120-150 từ)
                2. Cấu trúc: Đánh giá khách hàng → 2 gợi ý sản phẩm/combo → 2 mẹo sử dụng → Call-to-action
                3. Tone: Thân thiện, cá nhân hóa, khuyến khích
                4. Không markdown, không bullet points
                5. Dựa trên data thực tế để đưa insights
                """,
                customerId, totalOrders, totalOrderItems, totalSpent, activePromotions);

        GeminiRequest geminiRequest = new GeminiRequest();
        geminiRequest.setPrompt(prompt.toString());
        return processPrompt(geminiRequest);
    }

    @Override
    public GeminiResponse storeManagerAdvisor(Long storeId, String prompto) {
        List<Order> orders = orderRepository.findByStore_StoreId(storeId);
        List<Inventory> inventory = inventoryRepository.findByStore_StoreId(storeId);

        int totalOrdersCount = orders != null ? orders.size() : 0;
        int totalInventoryLots = inventory != null ? inventory.size() : 0;
        long activePromotions = promotionRepository != null ? promotionRepository.count() : 0L;

        // Tính thêm metrics quan trọng
        double totalRevenue = 0.0;
        int lowStockItems = 0;

        if (orders != null) {
            totalRevenue = orders.stream()
                    .filter(o -> o.getTotalPrice() != null)
                    .mapToDouble(o -> o.getTotalPrice().doubleValue())
                    .sum();
        }

        if (inventory != null) {
            lowStockItems = (int) inventory.stream()
                    .filter(inv -> inv.getQuantity() <= inv.getReorderLevel())
                    .count();
        }

        String prompt = String.format("""
                NHIỆM VỤ: Tư vấn quản lý cửa hàng TasteFlow - Phân tích hiệu suất & đề xuất hành động dựa trên prompt

                METRICS CỬA HÀNG (ID: %d):
                - Prompt: %s
                - Tổng đơn hàng: %d
                - Doanh thu: %.0f VND
                - Lô hàng tồn kho: %d
                - Sản phẩm gần hết: %d
                - Chương trình KM: %d

                YÊU CẦU OUTPUT:
                1. Đoạn văn tiếng Việt (150-180 từ)
                2. Cấu trúc: Đánh giá tổng quan → 3 hành động ưu tiên → Đề xuất KM cho 7 ngày tới
                3. Tone: Chuyên nghiệp, thực tế, hướng hành động
                4. Không markdown, tập trung vào ROI và impact
                5. Gợi ý cụ thể về inventory management và sales optimization
                """,
                storeId, prompto, totalOrdersCount, totalRevenue, totalInventoryLots, lowStockItems, activePromotions);

        GeminiRequest geminiRequest = new GeminiRequest();
        geminiRequest.setPrompt(prompt.toString());
        return processPrompt(geminiRequest);
    }

    @Override
    public GeminiResponse warehouseManagerAdvisor(Long warehouseId, String prompto) {
        List<Inventory> inventoryLots = inventoryRepository.findByWarehouse_WarehouseId(warehouseId);
        List<ProductBatch> productBatches = productBatchRepository.findByWarehouseWarehouseId(warehouseId);

        int inventoryLotsCount = inventoryLots != null ? inventoryLots.size() : 0;
        int productBatchesCount = productBatches != null ? productBatches.size() : 0;
        long productsCount = productRepository != null ? productRepository.count() : 0L;

        // Thêm metrics quan trọng cho warehouse
        int expiringBatches = 0;
        int lowStockItems = 0;

        if (inventoryLots != null) {
            lowStockItems = (int) inventoryLots.stream()
                    .filter(inv -> inv.getQuantity() <= inv.getReorderLevel())
                    .count();
        }

        String prompt = String.format("""
                NHIỆM VỤ: Tư vấn quản lý kho TasteFlow - Tối ưu inventory & procurement planning dựa trên prompt

                WAREHOUSE METRICS (ID: %d):
                - Prompt: %s
                - Lô hàng tồn: %d
                - Batch sản phẩm: %d
                - Tổng SKUs: %d
                - Items cần nhập: %d
                - Batch gần hết hạn: %d

                YÊU CẦU OUTPUT:
                1. Đoạn văn tiếng Việt (140-170 từ)
                2. Cấu trúc: Tình hình tồn kho → Kế hoạch procurement → Risk management
                3. Focus: Prevent stockout, minimize waste, optimize turnover
                4. Tone: Phân tích, chiến lược, data-driven
                5. Đề xuất timeline cụ thể cho procurement actions
                """,
                warehouseId, prompto, inventoryLotsCount, productBatchesCount, productsCount, lowStockItems,
                expiringBatches);

        GeminiRequest geminiRequest = new GeminiRequest();
        geminiRequest.setPrompt(prompt.toString());
        return processPrompt(geminiRequest);
    }

    @Override
    public GeminiResponse adminAdvisor(String period) {
        List<Store> stores = storeRepository.findAll();
        List<Order> orders = orderRepository.findAll();

        int storesCount = stores != null ? stores.size() : 0;
        int ordersCount = orders != null ? orders.size() : 0;

        // Tính metrics quan trọng cho admin
        double totalSystemRevenue = 0.0;
        int activeStores = 0;

        if (orders != null) {
            totalSystemRevenue = orders.stream()
                    .filter(o -> o.getTotalPrice() != null)
                    .mapToDouble(o -> o.getTotalPrice().doubleValue())
                    .sum();
        }

        if (stores != null) {
            activeStores = (int) stores.stream()
                    .filter(s -> "OPEN".equals(s.getStatus()))
                    .count();
        }

        String prompt = String.format("""
                NHIỆM VỤ: Báo cáo điều hành TasteFlow - Executive Dashboard & Strategic Priorities

                SYSTEM OVERVIEW (%s):
                - Tổng cửa hàng: %d (Hoạt động: %d)
                - Tổng đơn hàng: %d
                - Doanh thu hệ thống: %.0f VND
                - Tỷ lệ stores hoạt động: %.1f%%

                YÊU CẦU OUTPUT:
                1. Đoạn văn tiếng Việt (160-200 từ)
                2. Cấu trúc: Executive summary → 3 strategic priorities → Risk alerts → Next actions
                3. Tone: Executive level, strategic thinking, data-backed decisions
                4. Focus: System performance, growth opportunities, operational risks
                5. Timeline-specific recommendations với measurable outcomes
                """,
                period, storesCount, activeStores, ordersCount, totalSystemRevenue,
                storesCount > 0 ? (double) activeStores / storesCount * 100 : 0.0);

        GeminiRequest geminiRequest = new GeminiRequest();
        geminiRequest.setPrompt(prompt.toString());
        return processPrompt(geminiRequest);
    }

    @Override
    public GeminiResponse budgetProductAdvisor(Number budget) {
        List<ProductUnit> productUnits = productUnitRepository.findAll();
        List<Promotion> promos = promotionRepository.findAll();
        List<Voucher> vouchers = voucherRepository.findAll();

        double budgetValue = budget.doubleValue();

        // Lấy sản phẩm phù hợp với budget (có thể mua được)
        StringBuilder affordableProducts = new StringBuilder();
        StringBuilder budgetCombos = new StringBuilder();

        if (productUnits != null) {
            // Lọc sản phẩm có thể mua được với budget
            List<ProductUnit> affordableItems = productUnits.stream()
                    .filter(pu -> pu.getPrice() != null && pu.getPrice().doubleValue() <= budgetValue)
                    .sorted((a, b) -> a.getPrice().compareTo(b.getPrice()))
                    .limit(10) // Lấy 10 sản phẩm rẻ nhất trong tầm
                    .toList();

            for (ProductUnit pu : affordableItems) {
                if (pu.getProduct() != null) {
                    affordableProducts.append(String.format("• %s (%s): %,.0f VND\n",
                            pu.getProduct().getName(),
                            pu.getUnit() != null ? pu.getUnit().getName() : "đơn vị",
                            pu.getPrice().doubleValue()));
                }
            }

            // Tạo combo suggestions trong budget
            double remainingBudget = budgetValue;
            for (ProductUnit pu : affordableItems) {
                if (remainingBudget >= pu.getPrice().doubleValue()) {
                    budgetCombos.append(String.format("+ %s: %,.0f VND | ",
                            pu.getProduct().getName(), pu.getPrice().doubleValue()));
                    remainingBudget -= pu.getPrice().doubleValue();
                    if (budgetCombos.length() > 200)
                        break; // Giới hạn độ dài
                }
            }
        }

        // Thông tin về promotions
        StringBuilder activePromotions = new StringBuilder();
        if (promos != null && !promos.isEmpty()) {
            promos.stream().limit(3).forEach(promo -> {
                String discountInfo = "";
                if (promo.getDiscountType() != null) {
                    if ("PERCENT".equals(promo.getDiscountType()) && promo.getDiscountPercentage() != null) {
                        discountInfo = String.format("giảm %.0f%%", promo.getDiscountPercentage().doubleValue());
                    } else if ("AMOUNT".equals(promo.getDiscountType()) && promo.getDiscountAmount() != null) {
                        discountInfo = String.format("giảm %,.0f VND", promo.getDiscountAmount().doubleValue());
                    }
                }
                activePromotions.append(String.format("• %s: %s\n", promo.getName(), discountInfo));
            });
        }

        // Thông tin vouchers
        StringBuilder availableVouchers = new StringBuilder();
        if (vouchers != null && !vouchers.isEmpty()) {
            vouchers.stream()
                    .filter(v -> v.getMinOrderAmount() == null ||
                            v.getMinOrderAmount().doubleValue() <= budgetValue)
                    .limit(3)
                    .forEach(voucher -> {
                        String voucherInfo = "";
                        if ("PERCENT".equals(voucher.getDiscountType()) && voucher.getDiscountPercent() != null) {
                            voucherInfo = String.format("giảm %.0f%%", voucher.getDiscountPercent().doubleValue());
                        } else if ("AMOUNT".equals(voucher.getDiscountType()) && voucher.getDiscountAmount() != null) {
                            voucherInfo = String.format("giảm %,.0f VND", voucher.getDiscountAmount().doubleValue());
                        }
                        availableVouchers.append(String.format("• %s: %s\n", voucher.getCode(), voucherInfo));
                    });
        }

        // Phân loại budget ranges
        String budgetRange = "";
        if (budgetValue < 100000)
            budgetRange = "tiết kiệm";
        else if (budgetValue < 500000)
            budgetRange = "trung bình";
        else
            budgetRange = "cao cấp";

        String prompt = String.format("""
                NHIỆM VỤ: Tư vấn shopping TasteFlow - Smart budget optimization với sản phẩm cụ thể

                NGÂN SÁCH: %,.0f VND (Phân khúc: %s)

                SẢN PHẨM CÓ THỂ MUA:
                %s

                COMBO GỢI Ý TRONG BUDGET:
                %s

                KHUYẾN MÃI HIỆN TẠI:
                %s

                VOUCHERS KHẢ DỤNG:
                %s

                YÊU CẦU OUTPUT:
                1. Đoạn văn tiếng Việt (150-200 từ)
                2. Cấu trúc: Phân tích budget → 2-3 combo cụ thể với giá → Cách tối ưu (voucher/KM) → CTA mạnh
                3. Tone: Personal shopper chuyên nghiệp, thực tế, value-focused
                4. Đề xuất combo CỤ THỂ với tên sản phẩm và giá thật
                5. Gợi ý cách stack promotions + vouchers để maximize value
                6. Kêu gọi hành động ngay với lý do thuyết phục
                """,
                budgetValue, budgetRange,
                affordableProducts.length() > 0 ? affordableProducts.toString()
                        : "Không có sản phẩm phù hợp trong tầm giá này.",
                budgetCombos.length() > 0 ? budgetCombos.toString() : "Cần tăng ngân sách để có combo đa dạng hơn.",
                activePromotions.length() > 0 ? activePromotions.toString() : "Chưa có khuyến mãi đặc biệt.",
                availableVouchers.length() > 0 ? availableVouchers.toString() : "Chưa có voucher phù hợp.");

        GeminiRequest geminiRequest = new GeminiRequest();
        geminiRequest.setPrompt(prompt);
        return processPrompt(geminiRequest);
    }

    @Override
    public GeminiResponse mealSuggestionByProduct(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        Product product = productOpt.orElse(null);

        long categories = categoryRepository != null ? categoryRepository.count() : 0L;
        long inventories = inventoryRepository != null ? inventoryRepository.count() : 0L;
        long promos = promotionRepository != null ? promotionRepository.count() : 0L;

        String productName = product != null ? product.getName() : "Sản phẩm ID " + productId;
        String categoryInfo = product != null && product.getCategory() != null
                ? " (Danh mục: " + product.getCategory().getName() + ")"
                : "";

        String prompt = String.format("""
                NHIỆM VỤ: Gợi ý món ăn TasteFlow - Creative culinary consultant & meal planning

                PRODUCT FOCUS:
                - Sản phẩm chính: %s%s
                - Product ID: %d
                - Categories available: %d
                - Inventory items: %d
                - Active promotions: %d

                YÊU CẦU OUTPUT:
                1. Đoạn văn tiếng Việt (120-150 từ)
                2. Cấu trúc: Product highlight → 2 món ăn suggestions → Ingredient pairing → Action steps
                3. Tone: Food enthusiast, inspiring, practical
                4. Focus: Easy recipes, ingredient synergy, seasonal cooking
                5. Include shopping suggestions cho complementary items
                """,
                productName, categoryInfo, productId, categories, inventories, promos);

        GeminiRequest geminiRequest = new GeminiRequest();
        geminiRequest.setPrompt(prompt.toString());
        return processPrompt(geminiRequest);
    }
}