package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.Unit;
import com.startup.tasteflowbe.repository.UnitRepository;
import com.startup.tasteflowbe.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;

    @Override
    public List<Unit> getAllUnits() {
        return unitRepository.findAll();
    }

    @Override
    public Optional<Unit> getUnitById(Long id) {
        return unitRepository.findById(id);
    }

    @Override
    public Unit createUnit(Unit unit) {
        return unitRepository.save(unit);
    }

    @Override
    public Unit updateUnit(Long id, Unit updatedUnit) {
        return unitRepository.findById(id)
                .map(unit -> {
                    unit.setName(updatedUnit.getName());
                    return unitRepository.save(unit);
                })
                .orElseThrow(() -> new RuntimeException("Unit not found with id " + id));
    }

    @Override
    public void deleteUnit(Long id) {
        unitRepository.deleteById(id);
    }
}
