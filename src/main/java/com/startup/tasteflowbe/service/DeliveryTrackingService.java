package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.DeliveryTracking;

import java.util.List;

public interface DeliveryTrackingService {
    DeliveryTracking createDeliveryTracking(DeliveryTracking deliveryTracking);
    DeliveryTracking getDeliveryTrackingById(Long trackingId);
    List<DeliveryTracking> getAllDeliveryTrackings();
}
