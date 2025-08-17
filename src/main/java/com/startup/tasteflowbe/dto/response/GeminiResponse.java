package com.startup.tasteflowbe.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiResponse {
    private JsonNode output;
    private String error;
}
