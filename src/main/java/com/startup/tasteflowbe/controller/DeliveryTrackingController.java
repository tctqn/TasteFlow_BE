package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.DeliveryTracking;
import com.startup.tasteflowbe.service.DeliveryTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/delivery-trackings")
public class DeliveryTrackingController {

    private final DeliveryTrackingService deliveryTrackingService;

    @PostMapping
    public DeliveryTracking createDeliveryTracking(@RequestBody DeliveryTracking deliveryTracking) {
        return deliveryTrackingService.createDeliveryTracking(deliveryTracking);
    }

    @GetMapping("/{id}")
    public DeliveryTracking getDeliveryTrackingById(@PathVariable("id") Long id) {
        return deliveryTrackingService.getDeliveryTrackingById(id);
    }

    @GetMapping
    public List<DeliveryTracking> getAllDeliveryTrackings() {
        return deliveryTrackingService.getAllDeliveryTrackings();
    }
}
