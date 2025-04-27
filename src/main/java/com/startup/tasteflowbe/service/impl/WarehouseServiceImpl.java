package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.Warehouse;
import com.startup.tasteflowbe.repository.WarehouseRepository;
import com.startup.tasteflowbe.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Override
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    @Override
    public Optional<Warehouse> getWarehouseById(Long id) {
        return warehouseRepository.findById(id);
    }

    @Override
    public Warehouse createWarehouse(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    @Override
    public Warehouse updateWarehouse(Long id, Warehouse updatedWarehouse) {
        return warehouseRepository.findById(id)
                .map(warehouse -> {
                    warehouse.setName(updatedWarehouse.getName());
                    warehouse.setLocation(updatedWarehouse.getLocation());
                    warehouse.setManagerName(updatedWarehouse.getManagerName());
                    warehouse.setPhone(updatedWarehouse.getPhone());
                    return warehouseRepository.save(warehouse);
                })
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id " + id));
    }

    @Override
    public void deleteWarehouse(Long id) {
        warehouseRepository.deleteById(id);
    }
}
