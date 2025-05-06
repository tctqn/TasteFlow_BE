package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.Unit;
import com.startup.tasteflowbe.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @GetMapping
    public ResponseEntity<List<Unit>> getAllUnits() {
        return ResponseEntity.ok(unitService.getAllUnits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Unit> getUnitById(@PathVariable Long id) {
        return unitService.getUnitById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Unit> createUnit(@RequestBody Unit unit) {
        return ResponseEntity.ok(unitService.createUnit(unit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Unit> updateUnit(@PathVariable Long id, @RequestBody Unit unit) {
        return ResponseEntity.ok(unitService.updateUnit(id, unit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id) {
        unitService.deleteUnit(id);
        return ResponseEntity.noContent().build();
    }
}
