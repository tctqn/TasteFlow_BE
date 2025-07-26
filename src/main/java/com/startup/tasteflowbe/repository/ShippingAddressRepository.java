package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    ShippingAddress findByAddressId(Long addressId);
    List<ShippingAddress> getShippingAddressesByUser_UserId(Long userUserId);
}
