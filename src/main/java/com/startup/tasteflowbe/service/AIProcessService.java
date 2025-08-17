package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.request.GeminiRequest;
import com.startup.tasteflowbe.dto.response.GeminiResponse;

public interface AIProcessService {
    GeminiResponse getTopSellingProductsReport(Long storeId, String season);

    GeminiResponse processPrompt(GeminiRequest request);

    public GeminiResponse customerAdvisor(Long customerId);

    public GeminiResponse storeManagerAdvisor(Long storeId, String prompt);

    public GeminiResponse warehouseManagerAdvisor(Long warehouseId, String prompt);

    public GeminiResponse adminAdvisor(String period);

    public GeminiResponse budgetProductAdvisor(Number budget);

    public GeminiResponse mealSuggestionByProduct(Long context);

    public GeminiResponse analyzeResponse(String prompt);

    public String buildWarehouseJsonInput(Long warehouseId, Long productId);

    public GeminiResponse warehouseReplenishmentAdvisor(String jsonInput);
}
