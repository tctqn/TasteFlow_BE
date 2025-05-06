package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    ShippingAddress findByAddressId(Long addressId);
}
