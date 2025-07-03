package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.request.ShippingAddressRequestDTO;
import com.startup.tasteflowbe.model.ShippingAddress;

import java.util.List;
import java.util.Optional;

public interface ShippingAddressService {
    List<ShippingAddress> getAllShippingAddresses();
    List<ShippingAddress> getShippingAddressesByUserId(Long userId);
    Optional<ShippingAddress> getShippingAddressById(Long id);
    ShippingAddress createShippingAddress(ShippingAddressRequestDTO shippingAddressRequestDTO);
    ShippingAddress updateShippingAddress(Long id, ShippingAddress shippingAddress);
    void deleteShippingAddress(Long id);
}
