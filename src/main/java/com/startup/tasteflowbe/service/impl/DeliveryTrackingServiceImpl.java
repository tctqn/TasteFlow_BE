package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.DeliveryTracking;
import com.startup.tasteflowbe.repository.DeliveryTrackingRepository;
import com.startup.tasteflowbe.service.DeliveryTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeliveryTrackingServiceImpl implements DeliveryTrackingService {

    @Autowired
    private DeliveryTrackingRepository deliveryTrackingRepository;

    @Override
    public DeliveryTracking createDeliveryTracking(DeliveryTracking deliveryTracking) {
        return deliveryTrackingRepository.save(deliveryTracking);
    }

    @Override
    public DeliveryTracking getDeliveryTrackingById(Long trackingId) {
        Optional<DeliveryTracking> deliveryTracking = deliveryTrackingRepository.findById(trackingId);
        return deliveryTracking.orElse(null);
    }

    @Override
    public List<DeliveryTracking> getAllDeliveryTrackings() {
        return deliveryTrackingRepository.findAll();
    }
}
