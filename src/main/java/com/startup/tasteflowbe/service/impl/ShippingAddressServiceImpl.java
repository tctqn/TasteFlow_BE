package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.ShippingAddress;
import com.startup.tasteflowbe.repository.ShippingAddressRepository;
import com.startup.tasteflowbe.service.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShippingAddressServiceImpl implements ShippingAddressService {

    private final ShippingAddressRepository shippingAddressRepository;

    @Override
    public List<ShippingAddress> getAllShippingAddresses() {
        return shippingAddressRepository.findAll();
    }

    @Override
    public Optional<ShippingAddress> getShippingAddressById(Long id) {
        return shippingAddressRepository.findById(id);
    }

    @Override
    public ShippingAddress createShippingAddress(ShippingAddress shippingAddress) {
        return shippingAddressRepository.save(shippingAddress);
    }

    @Override
    public ShippingAddress updateShippingAddress(Long id, ShippingAddress shippingAddress) {
        return shippingAddressRepository.findById(id)
                .map(existingAddress -> {
                    existingAddress.setRecipientName(shippingAddress.getRecipientName());
                    existingAddress.setPhone(shippingAddress.getPhone());
                    existingAddress.setAddressLine(shippingAddress.getAddressLine());
                    existingAddress.setIsDefault(shippingAddress.getIsDefault());
                    return shippingAddressRepository.save(existingAddress);
                })
                .orElseThrow(() -> new RuntimeException("Shipping address not found with id " + id));
    }

    @Override
    public void deleteShippingAddress(Long id) {
        shippingAddressRepository.deleteById(id);
    }
}
