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

        if (user == null) {
            throw new RuntimeException("User not authenticated or not found");
        }

        // Nếu là địa chỉ mặc định
        if (Boolean.TRUE.equals(shippingAddressRequestDTO.getIsDefault())) {
            List<ShippingAddress> allAddresses = shippingAddressRepository
                    .getShippingAddressesByUser_UserId(user.getUserId());

            if (allAddresses != null && !allAddresses.isEmpty()) {
                for (ShippingAddress addr : allAddresses) {
                    addr.setIsDefault(false);
                }
                shippingAddressRepository.saveAll(allAddresses);
            }
        }

        // Convert DTO -> Entity
        ShippingAddress address = shippingAddressMapper.toEntity(shippingAddressRequestDTO, user);

        return shippingAddressRepository.save(address);
    }


    @Override
    public ShippingAddress updateShippingAddress(Long id, ShippingAddress shippingAddress) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = (String) authentication.getPrincipal();
            user = userRepository.findByUsername(username).orElse(null);
        }

        if (user == null) {
            throw new RuntimeException("User not authenticated or not found");
        }
        if (Boolean.TRUE.equals(shippingAddress.getIsDefault())) {
            List<ShippingAddress> allAddresses = shippingAddressRepository
                    .getShippingAddressesByUser_UserId(user.getUserId());

            if (allAddresses != null && !allAddresses.isEmpty()) {
                for (ShippingAddress addr : allAddresses) {
                    addr.setIsDefault(false);
                }
                shippingAddressRepository.saveAll(allAddresses);
            }
        }

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

    @Override
    public ShippingAddress setDefaultShippingAddress(Long id) {
        ShippingAddress address = shippingAddressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping address not found with id " + id));
        List<ShippingAddress> allAddresses = shippingAddressRepository.getShippingAddressesByUser_UserId(address.getUser().getUserId());
        if (allAddresses != null && !allAddresses.isEmpty()) {
            for (ShippingAddress addr : allAddresses) {
                addr.setIsDefault(false);
                shippingAddressRepository.save(addr);
            }
            address.setIsDefault(true);
            return shippingAddressRepository.save(address);
        } else {
            throw new RuntimeException("No shipping addresses found for user with id " + address.getUser().getUserId());
        }
    }
}
