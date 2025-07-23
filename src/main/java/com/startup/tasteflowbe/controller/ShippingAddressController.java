package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.request.ShippingAddressRequestDTO;
import com.startup.tasteflowbe.model.ShippingAddress;
import com.startup.tasteflowbe.service.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipping-addresses")
@RequiredArgsConstructor
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    @GetMapping
    public ResponseEntity<List<ShippingAddress>> getAllShippingAddresses() {
        return ResponseEntity.ok(shippingAddressService.getAllShippingAddresses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShippingAddress> getShippingAddressById(@PathVariable Long id) {
        return shippingAddressService.getShippingAddressById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ShippingAddress>> getShippingAddressesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(shippingAddressService.getShippingAddressesByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<ShippingAddress> createShippingAddress(@RequestBody ShippingAddressRequestDTO shippingAddressRequestDTO) {
        return ResponseEntity.ok(shippingAddressService.createShippingAddress(shippingAddressRequestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShippingAddress> updateShippingAddress(@PathVariable Long id, @RequestBody ShippingAddress shippingAddress) {
        return ResponseEntity.ok(shippingAddressService.updateShippingAddress(id, shippingAddress));
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<ShippingAddress> setDefaultShippingAddress(@PathVariable Long id) {
        ShippingAddress updated = shippingAddressService.setDefaultShippingAddress(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShippingAddress(@PathVariable Long id) {
        shippingAddressService.deleteShippingAddress(id);
        return ResponseEntity.noContent().build();
    }
}
