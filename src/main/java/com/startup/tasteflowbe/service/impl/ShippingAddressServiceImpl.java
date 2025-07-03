package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.ShippingAddressRequestDTO;
import com.startup.tasteflowbe.mapper.ShippingAddressMapper;
import com.startup.tasteflowbe.model.ShippingAddress;
import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.repository.ShippingAddressRepository;
import com.startup.tasteflowbe.repository.UserRepository;
import com.startup.tasteflowbe.service.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShippingAddressServiceImpl implements ShippingAddressService {

    private final ShippingAddressRepository shippingAddressRepository;
    private final ShippingAddressMapper shippingAddressMapper;
    private final UserRepository userRepository;

    @Override
    public List<ShippingAddress> getAllShippingAddresses() {
        return shippingAddressRepository.findAll();
    }

    @Override
    public List<ShippingAddress> getShippingAddressesByUserId(Long userId) {
        return shippingAddressRepository.getShippingAddressesByUser_UserId(userId);
    }

    @Override
    public Optional<ShippingAddress> getShippingAddressById(Long id) {
        return shippingAddressRepository.findById(id);
    }

    @Override
    public ShippingAddress createShippingAddress(ShippingAddressRequestDTO shippingAddressRequestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = (String) authentication.getPrincipal();
            user = userRepository.findByUsername(username).orElse(null);
        }
        ShippingAddress shippingAddress = new ShippingAddress();
        ShippingAddress address = shippingAddressMapper.toEntity(shippingAddressRequestDTO, user);
        shippingAddressRepository.save(address);

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
