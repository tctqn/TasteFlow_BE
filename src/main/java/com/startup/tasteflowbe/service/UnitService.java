package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.Unit;

import java.util.List;
import java.util.Optional;

public interface UnitService {
    List<Unit> getAllUnits();
    Optional<Unit> getUnitById(Long id);
    Unit createUnit(Unit unit);
    Unit updateUnit(Long id, Unit unit);
    void deleteUnit(Long id);
}
