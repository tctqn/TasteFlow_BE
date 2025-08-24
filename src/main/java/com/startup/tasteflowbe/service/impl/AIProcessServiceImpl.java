package com.startup.tasteflowbe.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.startup.tasteflowbe.repository.WarehouseRepository;
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
    private final WarehouseRepository warehouseRepository;
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
            response.setError("Không có dữ liệu bán hàng cho mùa " + season + " từ 3 năm trước.");
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
        response.setError(String.join("", results));
        return response;
    }

    @Override
    public GeminiResponse customerAdvisor(Long customerId) {
        List<Order> orders = orderRepository.findOrdersByUser_UserId(customerId);
        List<Product> products = productRepository.findAll();
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

        String productList = products.stream()
                .map(Product::getName)
                .collect(Collectors.joining(" "));

        long activePromotions = promotionRepository != null ? promotionRepository.count() : 0L;

        String prompt = String.format(
                """
                        NHIỆM VỤ: Tư vấn cá nhân hóa cho khách hàng TasteFlow

                        THÔNG TIN KHÁCH HÀNG:
                        - ID: %d
                        - Tổng đơn hàng: %d
                        - Tổng sản phẩm đã mua: %d
                        - Tổng chi tiêu: %.0f VND
                        - Khuyến mãi hiện tại: %d chương trình
                        - Các sản phẩm có trong hệ thống: %s

                        YÊU CẦU OUTPUT:
                        1. Viết 1 đoạn văn tiếng Việt (50-80 từ)
                        2. Cấu trúc: Đánh giá khách hàng → 2 gợi ý sản phẩm/combo có trong hệ thống → 2 mẹo sử dụng → Call-to-action
                        3. Tone: Thân thiện, cá nhân hóa, khuyến khích
                        4. Không markdown, không bullet points
                        5. Dựa trên data thực tế để đưa insights
                        """,
                customerId, totalOrders, totalOrderItems, totalSpent, activePromotions, productList);

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

    @Override
    public String buildWarehouseJsonInput(Long warehouseId, Long productId) {
        final int M = 1;
        final int nearExpiryDays = 7;
        final double targetServiceLevel = 0.95;

        ObjectMapper om = new ObjectMapper();
        ObjectNode root = om.createObjectNode();
        root.put("as_of_date", LocalDate.now().toString());

        ObjectNode wh = om.createObjectNode();
        wh.put("id", warehouseId);
        String wname = warehouseRepository.findWarehouseNameById(warehouseId);
        if (wname != null)
            wh.put("name", wname);
        root.set("warehouse", wh);

        ObjectNode params = om.createObjectNode();
        params.put("time_window_months", M);
        params.put("near_expiry_days", nearExpiryDays);
        params.put("target_service_level", targetServiceLevel);
        params.putNull("budget_cap");
        params.putArray("priority_categories");
        root.set("params", params);

        double onHand = Optional.ofNullable(warehouseRepository.onHandOfProduct(warehouseId, productId)).orElse(0d);
        double reorderLevel = Optional.ofNullable(warehouseRepository.reorderLevelOfProduct(warehouseId, productId))
                .orElse(0d);
        double avgIn = Optional.ofNullable(warehouseRepository.avgMonthlyInboundOfProduct(warehouseId, productId, M))
                .orElse(0d);
        double avgOut = Optional
                .ofNullable(warehouseRepository.avgMonthlyOutboundToStoreOfProduct(warehouseId, productId, M))
                .orElse(0d);
        double nearExpiryOnHand = Optional
                .ofNullable(warehouseRepository.nearExpiryOnHandOfProduct(warehouseId, productId, nearExpiryDays))
                .orElse(0d);
        Integer minDte = warehouseRepository.minDaysToExpiryOfProduct(warehouseId, productId);
        double avgExpired = Optional
                .ofNullable(warehouseRepository.avgMonthlyExpiredEstimateOfProduct(warehouseId, productId, M))
                .orElse(0d);

        ArrayNode products = om.createArrayNode();
        ObjectNode pnode = om.createObjectNode();
        pnode.put("product_id", productId);
        pnode.putNull("sku");
        pnode.putNull("name");
        pnode.putNull("category");
        pnode.putNull("supplier_id");
        pnode.putNull("lead_time_days");

        pnode.put("reorder_level", reorderLevel);
        pnode.put("case_pack", 1); // tránh 0 để rounding đúng
        pnode.put("moq", 0);
        pnode.put("on_hand_qty", onHand);
        pnode.put("on_order_qty", 0);
        pnode.put("avg_monthly_inbound", avgIn);
        pnode.put("avg_monthly_outbound_to_store", avgOut);
        pnode.put("avg_monthly_expired", avgExpired);
        pnode.put("near_expiry_on_hand", nearExpiryOnHand);
        if (minDte != null)
            pnode.put("min_days_to_expiry", minDte);
        else
            pnode.putNull("min_days_to_expiry");
        pnode.putNull("store_group_monthly_demand");
        pnode.put("recent_trend", "flat");
        pnode.putNull("notes");

        products.add(pnode);
        root.set("products", products);
        return root.toPrettyString();
    }

    @Override
    public GeminiResponse warehouseReplenishmentAdvisor(String jsonInput) {
        try {
            ObjectMapper om = new ObjectMapper();
            om.findAndRegisterModules();
            JsonNode inputJson = om.readTree(jsonInput);
            JsonNode paramsNode = inputJson.path("params");
            int timeWindowWeeks = paramsNode.path("time_window_weeks").asInt(1);
            double targetServiceLevel = paramsNode.path("target_service_level").asDouble(0.95);
            Double budgetCap = paramsNode.hasNonNull("budget_cap") ? paramsNode.get("budget_cap").asDouble() : null;

            JsonNode warehouseNode = inputJson.path("warehouse");
            long warehouseId = warehouseNode.path("id").asLong();
            String warehouseName = warehouseNode.path("name").asText("Unknown Warehouse");

            JsonNode productsArray = inputJson.path("products");
            if (!productsArray.isArray() || productsArray.size() == 0) {
                GeminiResponse errorResponse = new GeminiResponse();
                errorResponse.setError("{\"error\": \"No products found in input JSON\"}");
                return errorResponse;
            }

            ObjectNode reportRoot = om.createObjectNode();
            ObjectNode report = om.createObjectNode();

            report.put("analysis_date", inputJson.path("as_of_date").asText(LocalDate.now().toString()));
            report.put("warehouse_id", warehouseId);
            report.put("warehouse_name", warehouseName);
            report.put("service_level_target", targetServiceLevel);
            report.put("planning_horizon_weeks", timeWindowWeeks);
            report.put("analysis_scope", "Đề xuất điều chỉnh lượng nhập hàng");

            ArrayNode productAnalysis = om.createArrayNode();
            double totalRecommendedOrderValue = 0.0;
            int increaseItems = 0;
            int decreaseItems = 0;
            int maintainItems = 0;

            for (JsonNode productNode : productsArray) {
                ObjectNode productReport = processProductReplenishmentOptimized(productNode, timeWindowWeeks,
                        targetServiceLevel, om);

                productAnalysis.add(productReport);

                double orderQty = productReport.path("recommendation").path("order_quantity").asDouble(0);
                double unitCost = productReport.path("financial_analysis").path("estimated_unit_cost").asDouble(0);
                totalRecommendedOrderValue += orderQty * unitCost;

                String adjustmentType = productReport.path("recommendation").path("adjustment_type").asText();
                switch (adjustmentType) {
                    case "INCREASE" -> increaseItems++;
                    case "DECREASE" -> decreaseItems++;
                    case "MAINTAIN" -> maintainItems++;
                }
            }

            report.set("product_analysis", productAnalysis);

            ObjectNode executiveSummary = om.createObjectNode();
            executiveSummary.put("total_products_analyzed", productsArray.size());
            executiveSummary.put("increase_quantity_items", increaseItems);
            executiveSummary.put("decrease_quantity_items", decreaseItems);
            executiveSummary.put("maintain_quantity_items", maintainItems);
            executiveSummary.put("total_recommended_order_value", Math.round(totalRecommendedOrderValue));
            executiveSummary.put("budget_utilization_pct",
                    budgetCap != null ? Math.min(100, (totalRecommendedOrderValue / budgetCap) * 100) : null);

            report.set("executive_summary", executiveSummary);

            ArrayNode strategicRecommendations = om.createArrayNode();
            generateOptimizedStrategicRecommendations(strategicRecommendations, increaseItems,
                    decreaseItems, maintainItems, totalRecommendedOrderValue, budgetCap);
            report.set("strategic_recommendations", strategicRecommendations);

            reportRoot.set("warehouse_replenishment_report", report);
            GeminiResponse response = new GeminiResponse();
            response.setOutput(reportRoot);
            return response;

        } catch (Exception e) {
            GeminiResponse errorResponse = new GeminiResponse();
            errorResponse.setError("{\"error\": \"Replenishment analysis failed: " + e.getMessage() + "\"}");
            return errorResponse;
        }
    }

    private ObjectNode processProductReplenishmentOptimized(JsonNode productNode, int timeWindowWeeks,
            double targetServiceLevel, ObjectMapper om) {
        ObjectNode productReport = om.createObjectNode();
        long productId = productNode.path("product_id").asLong();
        double onHand = productNode.path("on_hand_qty").asDouble(0);
        double onOrder = productNode.path("on_order_qty").asDouble(0);
        double avgMonthlyInbound = productNode.path("avg_monthly_inbound").asDouble(0);
        double avgMonthlyOutbound = productNode.path("avg_monthly_outbound_to_store").asDouble(0);
        double reorderLevel = productNode.path("reorder_level").asDouble(0);

        int casePack = Math.max(1, productNode.path("case_pack").asInt(1));
        int moq = productNode.path("moq").asInt(0);
        int leadTimeDays = productNode.path("lead_time_days").asInt(7);

        productReport.put("product_id", productId);

        // Tính toán demand
        ObjectNode demandAnalysis = om.createObjectNode();
        double weeklyDemandFromOutbound = avgMonthlyOutbound / 4.33;
        double weeklyDemandFromInbound = avgMonthlyInbound / 4.33;
        double primaryWeeklyDemand = weeklyDemandFromOutbound > 0 ? weeklyDemandFromOutbound : weeklyDemandFromInbound;
        double dailyDemand = primaryWeeklyDemand / 7.0;

        demandAnalysis.put("weekly_demand", Math.round(primaryWeeklyDemand * 100.0) / 100.0);
        demandAnalysis.put("daily_demand", Math.round(dailyDemand * 100.0) / 100.0);
        demandAnalysis.put("trend", analyzeDemandTrend(weeklyDemandFromInbound, weeklyDemandFromOutbound));
        productReport.set("demand_analysis", demandAnalysis);

        // Tính toán inventory position
        ObjectNode inventoryPosition = om.createObjectNode();
        double totalPosition = onHand + onOrder;
        double daysOnHand = dailyDemand > 0 ? totalPosition / dailyDemand : (totalPosition > 0 ? 999 : 0);

        inventoryPosition.put("on_hand", onHand);
        inventoryPosition.put("on_order", onOrder);
        inventoryPosition.put("total_position", totalPosition);
        inventoryPosition.put("days_on_hand", Math.round(daysOnHand * 10.0) / 10.0);
        inventoryPosition.put("reorder_level", reorderLevel);
        productReport.set("inventory_position", inventoryPosition);

        // Tính toán optimal order quantity
        double zScore = getZScoreForServiceLevel(targetServiceLevel);
        double leadTimeWeeks = leadTimeDays / 7.0;
        double weeklyVariability = Math.max(0.15,
                Math.abs(weeklyDemandFromInbound - weeklyDemandFromOutbound) / Math.max(1, primaryWeeklyDemand));
        double weeklyDemandStdDev = primaryWeeklyDemand * weeklyVariability;
        double weeklySafetyStock = zScore * Math.sqrt(leadTimeWeeks) * weeklyDemandStdDev;
        double leadTimeDemand = dailyDemand * leadTimeDays;
        double optimalReorderPoint = leadTimeDemand + weeklySafetyStock;
        double optimalTargetStock = optimalReorderPoint + (primaryWeeklyDemand * timeWindowWeeks);

        // Tính gap và làm tròn theo bội số của 5
        double rawGap = Math.max(0, optimalTargetStock - totalPosition);
        long recommendedOrderQty = roundToMultipleOfFive(rawGap);

        // Đảm bảo MOQ
        if (recommendedOrderQty > 0 && recommendedOrderQty < moq) {
            recommendedOrderQty = roundToMultipleOfFive(moq);
        }

        // Xác định loại adjustment
        String adjustmentType = determineAdjustmentType(recommendedOrderQty, avgMonthlyInbound);
        String adjustmentReason = buildAdjustmentReason(adjustmentType, recommendedOrderQty, daysOnHand, leadTimeDays,
                reorderLevel, totalPosition);

        ObjectNode recommendation = om.createObjectNode();
        recommendation.put("adjustment_type", adjustmentType);
        recommendation.put("order_quantity", recommendedOrderQty);
        recommendation.put("previous_avg_monthly", Math.round(avgMonthlyInbound));
        recommendation.put("adjustment_reason", adjustmentReason);
        recommendation.put("priority", determinePriority(daysOnHand, leadTimeDays, totalPosition, reorderLevel));
        productReport.set("recommendation", recommendation);

        // Financial analysis
        ObjectNode financialAnalysis = om.createObjectNode();
        double estimatedUnitCost = estimateUnitCost(avgMonthlyInbound, productId);
        double orderValue = recommendedOrderQty * estimatedUnitCost;

        financialAnalysis.put("estimated_unit_cost", Math.round(estimatedUnitCost * 100.0) / 100.0);
        financialAnalysis.put("recommended_order_value", Math.round(orderValue * 100.0) / 100.0);
        productReport.set("financial_analysis", financialAnalysis);

        return productReport;
    }

    private long roundToMultipleOfFive(double value) {
        if (value <= 0)
            return 0;
        return Math.round(value / 5.0) * 5;
    }

    private String determineAdjustmentType(long orderQty, double currentAvgMonthly) {
        if (orderQty == 0)
            return "MAINTAIN";

        double monthlyEquivalent = orderQty * 1.0; // Giả sử order hàng tuần
        if (monthlyEquivalent > currentAvgMonthly * 1.2) {
            return "INCREASE";
        } else if (monthlyEquivalent < currentAvgMonthly * 0.8) {
            return "DECREASE";
        } else {
            return "MAINTAIN";
        }
    }

    private String buildAdjustmentReason(String adjustmentType, long orderQty, double daysOnHand,
            int leadTimeDays, double reorderLevel, double totalPosition) {
        switch (adjustmentType) {
            case "INCREASE":
                if (daysOnHand < leadTimeDays) {
                    return String.format("Tăng lên %d đơn vị - Tồn kho thấp (%,.1f ngày), dưới lead time %d ngày",
                            orderQty, daysOnHand, leadTimeDays);
                } else if (totalPosition < reorderLevel) {
                    return String.format("Tăng lên %d đơn vị - Dưới mức reorder point (%,.0f)",
                            orderQty, reorderLevel);
                } else {
                    return String.format("Tăng lên %d đơn vị - Tối ưu hóa service level", orderQty);
                }
            case "DECREASE":
                return String.format("Giảm xuống %d đơn vị - Tồn kho đủ dùng (%,.1f ngày)",
                        orderQty, daysOnHand);
            case "MAINTAIN":
                return String.format("Giữ nguyên mức hiện tại - Tồn kho ổn định (%,.1f ngày)", daysOnHand);
            default:
                return "Đề xuất duy trì mức nhập hiện tại";
        }
    }

    private String determinePriority(double daysOnHand, int leadTimeDays, double totalPosition, double reorderLevel) {
        if (daysOnHand < leadTimeDays * 0.5) {
            return "HIGH";
        } else if (totalPosition < reorderLevel) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private String analyzeDemandTrend(double weeklyInbound, double weeklyOutbound) {
        if (weeklyOutbound > weeklyInbound * 1.15)
            return "Tăng trưởng";
        if (weeklyOutbound < weeklyInbound * 0.85)
            return "Giảm dần";
        return "Ổn định";
    }

    private void generateOptimizedStrategicRecommendations(ArrayNode recommendations, int increaseItems,
            int decreaseItems, int maintainItems, double totalOrderValue, Double budgetCap) {

        if (increaseItems > 0) {
            recommendations.add(
                    String.format("📈 TĂNG LƯỢNG NHẬP: %d sản phẩm cần tăng số lượng nhập để đảm bảo service level",
                            increaseItems));
        }

        if (decreaseItems > 0) {
            recommendations
                    .add(String.format("📉 GIẢM LƯỢNG NHẬP: %d sản phẩm có thể giảm số lượng nhập để tối ưu chi phí",
                            decreaseItems));
        }

        if (maintainItems > 0) {
            recommendations.add(String.format("📊 DUY TRÌ HIỆN TẠI: %d sản phẩm đang ở mức tối ưu",
                    maintainItems));
        }

        recommendations.add(String.format("💰 TỔNG GIÁ TRỊ ĐỀ XUẤT: %,.0f VND cho chu kỳ tiếp theo",
                totalOrderValue));

        if (budgetCap != null) {
            double utilizationPct = (totalOrderValue / budgetCap) * 100;
            if (utilizationPct > 100) {
                recommendations.add(String.format(
                        "⚠️ VƯỢT NGÂN SÁCH: Cần %,.0f VND nhưng chỉ có %,.0f VND. Ưu tiên items HIGH priority",
                        totalOrderValue, budgetCap));
            } else {
                recommendations.add(String.format("✅ NGÂN SÁCH: Sử dụng %.1f%% ngân sách khả dụng", utilizationPct));
            }
        }
    }

    private double estimateUnitCost(double avgInbound, long productId) {
        return Math.max(10, avgInbound * 0.1 + productId % 100);
    }

    private double getZScoreForServiceLevel(double serviceLevel) {
        if (serviceLevel >= 0.99)
            return 2.33;
        if (serviceLevel >= 0.98)
            return 2.05;
        if (serviceLevel >= 0.95)
            return 1.65;
        if (serviceLevel >= 0.90)
            return 1.28;
        return 1.0;
    }
}
