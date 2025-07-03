package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.request.BulkApprovalRequestDTO;
import com.startup.tasteflowbe.dto.request.CreateWarehouseRequestDTO;
import com.startup.tasteflowbe.dto.request.ItemApprovalDTO;
import com.startup.tasteflowbe.model.WarehouseRequest;
import com.startup.tasteflowbe.model.WarehouseRequestItem;

import java.util.List;
import java.util.Optional;

public interface WarehouseRequestService {

    WarehouseRequest createWarehouseRequest(CreateWarehouseRequestDTO dto);

    Optional<WarehouseRequest> findRequestById(Integer id);

    List<WarehouseRequest> findAllRequests();

    boolean deleteRequest(Integer id);

    WarehouseRequest updateRequestStatus(Integer requestId, BulkApprovalRequestDTO dto);

    WarehouseRequestItem processRequestItem(Integer itemId, ItemApprovalDTO approvalDTO);
}
