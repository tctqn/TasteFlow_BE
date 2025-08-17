package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.request.GeminiRequest;
import com.startup.tasteflowbe.dto.response.GeminiResponse;
import com.startup.tasteflowbe.service.AIProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIProcessController {

    private final AIProcessService aiProcessService;

    @PostMapping("/generate")
    public GeminiResponse generate(@RequestBody GeminiRequest request) {
        return aiProcessService.analyzeResponse(request.getPrompt());
    }

    @GetMapping("/top-selling-products/{storeId}/{season}")
    public GeminiResponse getTopSellingProductsReport(
            @PathVariable Long storeId,
            @PathVariable String season) {
        return aiProcessService.getTopSellingProductsReport(storeId, season);
    }

    @GetMapping("/customer-advisor/{customerId}")
    public GeminiResponse customerAdvisor(@PathVariable Long customerId) {
        return aiProcessService.customerAdvisor(customerId);
    }

    @PostMapping("/store-manager-advisor/{storeId}")
    public GeminiResponse storeManagerAdvisor(@PathVariable Long storeId, @RequestBody String prompt) {
        return aiProcessService.storeManagerAdvisor(storeId, prompt);
    }

    @PostMapping("/warehouse-manager-advisor/{warehouseId}")
    public GeminiResponse warehouseManagerAdvisor(@PathVariable Long warehouseId, @RequestBody String prompt) {
        return aiProcessService.warehouseManagerAdvisor(warehouseId, prompt);
    }

    @GetMapping("/admin-advisor/{period}")
    public GeminiResponse adminAdvisor(@PathVariable String period) {
        return aiProcessService.adminAdvisor(period);
    }

    @GetMapping("/budget-product-advisor/{budget}")
    public GeminiResponse budgetProductAdvisor(@PathVariable Number budget) {
        return aiProcessService.budgetProductAdvisor(budget);
    }

    @GetMapping("/meal-suggestion-by-product/{context}")
    public GeminiResponse mealSuggestionByProduct(@PathVariable Long context) {
        return aiProcessService.mealSuggestionByProduct(context);
    }

    @GetMapping("/prediction-stock/{warehouseId}/{productId}")
    public GeminiResponse getPredictionStockOfWarehouse(@PathVariable Long warehouseId, @PathVariable Long productId) {
        String inputPrompt = aiProcessService.buildWarehouseJsonInput(warehouseId, productId);
        return aiProcessService.warehouseReplenishmentAdvisor(inputPrompt);
    }

}
