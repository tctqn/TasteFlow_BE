package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.DeliveryTracking;
import com.startup.tasteflowbe.service.DeliveryTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-trackings")
public class DeliveryTrackingController {

    @Autowired
    private DeliveryTrackingService deliveryTrackingService;

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
