package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.response.OrderResponseDTO;

public interface OrderAvailabilityService {
    OrderResponseDTO getOrderDetailWithAvailability(Long orderId);
}
