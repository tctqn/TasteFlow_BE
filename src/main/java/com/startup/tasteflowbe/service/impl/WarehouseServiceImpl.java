package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.Warehouse;
import com.startup.tasteflowbe.repository.InventoryRepository;
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

    private final InventoryRepository inventoryRepository;

    @Override
    public List<Warehouse> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        for (Warehouse warehouse : warehouses) {
            Integer total = inventoryRepository.getTotalProductByWarehouseId(warehouse.getWarehouseId()).orElse(null);
            warehouse.setTotalProduct(total != null ? total : 0);
        }
        return warehouses;
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
                    warehouse.setRegion(updatedWarehouse.getRegion());
                    warehouse.setManagerName(updatedWarehouse.getManagerName());
                    warehouse.setPhone(updatedWarehouse.getPhone());
                    warehouse.setStatus(updatedWarehouse.getStatus());
                    warehouse.setCapacity(updatedWarehouse.getCapacity());
                    warehouse.setTotalProduct(updatedWarehouse.getTotalProduct());
                    return warehouseRepository.save(warehouse);
                })
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id " + id));
    }

    @Override
    public void deleteWarehouse(Long id) {
        warehouseRepository.deleteById(id);
    }

    @Override
    public Warehouse getWarehouseByManager(String username) {
        return warehouseRepository.findByManagerName(username)
                .orElseThrow(() -> new RuntimeException("No warehouses found for manager: " + username));
    }
}
