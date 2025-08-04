package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.enums.Role;
import com.startup.tasteflowbe.model.Store;
import com.startup.tasteflowbe.model.StoreStaff;
import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.repository.StoreRepository;
import com.startup.tasteflowbe.repository.StoreStaffRepository;
import com.startup.tasteflowbe.repository.UserRepository;
import com.startup.tasteflowbe.service.StoreStaffService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StoreStaffServiceImpl implements StoreStaffService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreStaffRepository storeStaffRepository;

    @Override
    public void assignUserToMyStore(Long managerId, Long staffUserId) {
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy quản lý"));
        if (manager.getRole() != Role.STORE_MANAGER) {
            throw new IllegalStateException("Chỉ Store Manager mới có quyền gán nhân viên");
        }

        Store store = storeRepository.findByManager_UserId(managerId)
            .orElseThrow(() -> new IllegalStateException("Bạn không quản lý cửa hàng nào"));

        User staff = userRepository.findById(staffUserId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy nhân viên"));

        if (staff.getRole() != Role.STORE_STAFF) {
            throw new IllegalArgumentException("Chỉ có thể thêm nhân viên thuộc vai trò STORE_STAFF");
        }

        boolean alreadyAssigned = storeStaffRepository.existsByUserAndActiveTrue(staff);
        if (alreadyAssigned) {
            throw new IllegalStateException("Nhân viên này đang làm việc ở cửa hàng khác");
        }

        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setUser(staff);
        storeStaff.setStore(store);
        storeStaff.setActive(true);
        storeStaff.setAssignedDate(LocalDateTime.now());

        storeStaffRepository.save(storeStaff);
    }
}
