package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.service.StoreStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store-staff")
@RequiredArgsConstructor
public class StoreStaffController {
    private final StoreStaffService storeStaffService;

    @PostMapping("/assign")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    public ResponseEntity<?> assignUserToMyStore(@RequestParam Long managerId, @RequestParam Long staffUserId) {
        storeStaffService.assignUserToMyStore(managerId, staffUserId);
        return ResponseEntity.ok().build();
    }
}
