package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.ShippingAddress;

import java.util.List;
import java.util.Optional;

public interface ShippingAddressService {
    List<ShippingAddress> getAllShippingAddresses();
    Optional<ShippingAddress> getShippingAddressById(Long id);
    ShippingAddress createShippingAddress(ShippingAddress shippingAddress);
    ShippingAddress updateShippingAddress(Long id, ShippingAddress shippingAddress);
    void deleteShippingAddress(Long id);
}
