package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.BulkApprovalRequestDTO;
import com.startup.tasteflowbe.dto.request.CreateWarehouseRequestDTO;
import com.startup.tasteflowbe.dto.request.ItemApprovalDTO;
import com.startup.tasteflowbe.dto.request.RequestItemDTO;
import com.startup.tasteflowbe.enums.NotificationType;
import com.startup.tasteflowbe.enums.Role;
import com.startup.tasteflowbe.model.ProductBatch;
import com.startup.tasteflowbe.model.ProductUnit;
import com.startup.tasteflowbe.model.Supplier;
import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.model.Warehouse;
import com.startup.tasteflowbe.model.WarehouseRequest;
import com.startup.tasteflowbe.model.WarehouseRequestItem;
import com.startup.tasteflowbe.repository.ProductBatchRepository;
import com.startup.tasteflowbe.repository.ProductUnitRepository;
import com.startup.tasteflowbe.repository.SupplierRepository;
import com.startup.tasteflowbe.repository.UserRepository;
import com.startup.tasteflowbe.repository.WarehouseRepository;
import com.startup.tasteflowbe.repository.WarehouseRequestItemRepository;
import com.startup.tasteflowbe.repository.WarehouseRequestRepository;
import com.startup.tasteflowbe.service.NotificationService;
import com.startup.tasteflowbe.service.WarehouseRequestService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseRequestServiceImpl implements WarehouseRequestService {

    private final WarehouseRequestRepository requestRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseRequestItemRepository itemRepository;
    private final ProductUnitRepository productUnitRepository;
    private final SupplierRepository supplierRepository;
    private final ProductBatchRepository productBatchRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WarehouseRequest createWarehouseRequest(CreateWarehouseRequestDTO dto) {

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kho với ID: " + dto.getWarehouseId()));

        WarehouseRequest request = new WarehouseRequest();
        request.setWarehouseId(dto.getWarehouseId());
        request.setCreatedBy(dto.getCreatedBy());
        request.setNotes(dto.getNotes());
        request.setStatus("PENDING");

        List<WarehouseRequestItem> items = new ArrayList<>();

        for (RequestItemDTO itemDto : dto.getItems()) {
            if (Boolean.TRUE.equals(itemDto.isDirectInput())) {
                ProductUnit productUnit = productUnitRepository.findByProductUnitId(itemDto.getProductUnitId());
                Supplier supplier = supplierRepository.findBySupplierId(itemDto.getSupplierId());
                BigDecimal qt = BigDecimal.valueOf(itemDto.getQuantity());

                ProductBatch batch = new ProductBatch();
                batch.setProduct(productUnit.getProduct());
                batch.setWarehouse(warehouse);
                batch.setQuantity(itemDto.getQuantity());
                batch.setImportPrice(productUnit.getPrice().multiply(qt));
                batch.setReceivedDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
                batch.setStatus("SHIPPED");
                batch.setUnit(productUnit.getUnit());
                batch.setSupplier(supplier);
                batch.setExpirationDate(LocalDate.now().plusDays(1));
                batch.setManufactureDate(LocalDate.now());

                productBatchRepository.save(batch);

                WarehouseRequestItem item = new WarehouseRequestItem();
                item.setProductUnitId(itemDto.getProductUnitId().intValue());
                item.setQuantity(itemDto.getQuantity());
                item.setNote(itemDto.getNote());
                item.setStatus("DIRECT_TRANSFER");
                item.setWarehouseRequest(request);
                items.add(item);

                continue;
            }

            WarehouseRequestItem item = new WarehouseRequestItem();
            item.setProductUnitId(itemDto.getProductUnitId().intValue());
            item.setQuantity(itemDto.getQuantity());
            item.setNote(itemDto.getNote());
            item.setStatus("PENDING");
            item.setWarehouseRequest(request);
            items.add(item);
        }

        request.setItems(items);

        // Lấy tất cả Admins
        List<Long> adminIds = userRepository.findByRole(Role.ADMIN)
                .stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        // Thêm createdBy vào danh sách
        adminIds.add(dto.getCreatedBy());

        notificationService.sendNotificationToUsers(
                adminIds,
                NotificationType.ALERT,
                "Yêu cầu nhập hàng mới đã được tạo từ " + warehouse.getName());

        return requestRepository.save(request);
    }

    @Override
    public Optional<WarehouseRequest> findRequestById(Integer id) {
        return requestRepository.findById(id);
    }

    @Override
    public List<WarehouseRequest> findAllRequests() {
        return requestRepository.findAll();
    }

    @Override
    @Transactional
    public boolean deleteRequest(Integer id) {
        if (requestRepository.existsById(id)) {
            requestRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public WarehouseRequest updateRequestStatus(Integer requestId, BulkApprovalRequestDTO dto) {
        WarehouseRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phiếu yêu cầu với ID: " + requestId));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Chỉ có thể duyệt/từ chối phiếu yêu cầu ở trạng thái PENDING.");
        }

        // Xử lý từng item trong request
        for (BulkApprovalRequestDTO.ApprovalItemInfo itemInfo : dto.getItems()) {
            WarehouseRequestItem item = request.getItems().stream()
                    .filter(dbItem -> dbItem.getRequestItemId().equals(itemInfo.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Item không tồn tại: " + itemInfo.getItemId()));

            if ("APPROVED".equals(dto.getStatus())) {
                // Validate approved quantity
                if (itemInfo.getApprovedQuantity() == null || itemInfo.getApprovedQuantity() <= 0) {
                    throw new IllegalArgumentException("Số lượng duyệt phải lớn hơn 0.");
                }

                if (itemInfo.getApprovedQuantity() > item.getQuantity()) {
                    throw new IllegalArgumentException("Số lượng duyệt không thể lớn hơn số lượng yêu cầu.");
                }

                // Set fulfilled quantity and determine item status
                item.setFulfilledQuantity(itemInfo.getApprovedQuantity());

                // Fix logic: Compare với quantity (requested), không phải fulfilledQuantity
                if (itemInfo.getApprovedQuantity().equals(item.getQuantity())) {
                    item.setStatus("APPROVED"); // Fully approved
                } else {
                    item.setStatus("PARTIALLY_APPROVED"); // Partially approved
                }

                // Create ProductBatch for approved items
                createProductBatch(item, itemInfo);

            } else if ("REJECTED".equals(dto.getStatus())) {
                item.setStatus("REJECTED");
                item.setFulfilledQuantity(0);
            }

            itemRepository.save(item);
        }

        // Update parent request status based on item statuses
        updateParentRequestStatus(request);

        notificationService.sendNotificationToUsers(
                Arrays.asList(request.getCreatedBy()),
                NotificationType.ALERT,
                "Yêu cầu nhập hàng tới " + request.getWarehouse().getName() + " đã được duyệt");

        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public WarehouseRequestItem processRequestItem(Integer itemId, ItemApprovalDTO approvalDTO) {
        WarehouseRequestItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy item với ID: " + itemId));

        if (!"PENDING".equals(item.getStatus()) && !"PARTIALLY_APPROVED".equals(item.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xử lý item ở trạng thái PENDING hoặc PARTIALLY_APPROVED.");
        }

        // Xử lý trường hợp PARTIALLY_APPROVED - có thể approve thêm quantity
        if ("PARTIALLY_APPROVED".equals(item.getStatus()) && "APPROVED".equals(approvalDTO.getStatus())) {
            if (approvalDTO.getApprovedQuantity() == null || approvalDTO.getApprovedQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng duyệt phải lớn hơn 0.");
            }

            int currentFulfilled = item.getFulfilledQuantity() != null ? item.getFulfilledQuantity() : 0;
            int totalApproved = currentFulfilled + approvalDTO.getApprovedQuantity();

            if (totalApproved > item.getQuantity()) {
                throw new IllegalArgumentException("Tổng số lượng duyệt không thể lớn hơn số lượng yêu cầu.");
            }

            item.setFulfilledQuantity(totalApproved);

            if (totalApproved == item.getQuantity()) {
                item.setStatus("APPROVED"); // Fully approved
            } else {
                item.setStatus("PARTIALLY_APPROVED"); // Still partially approved
            }

            // Create ProductBatch for additional approved quantity
            createProductBatchFromApproval(item, approvalDTO, approvalDTO.getApprovedQuantity());

        } else if ("REJECTED".equals(approvalDTO.getStatus())) {
            System.out.println("Rejecting item with ID: " + itemId);
            item.setStatus("REJECTED");
            item.setFulfilledQuantity(0);

        } else if ("APPROVED".equals(approvalDTO.getStatus())) {
            // Validate approved quantity
            if (approvalDTO.getApprovedQuantity() == null || approvalDTO.getApprovedQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng duyệt phải lớn hơn 0.");
            }

            if (approvalDTO.getApprovedQuantity() > item.getQuantity()) {
                throw new IllegalArgumentException("Số lượng duyệt không thể lớn hơn số lượng yêu cầu.");
            }

            // Set fulfilled quantity
            item.setFulfilledQuantity(approvalDTO.getApprovedQuantity());

            // Fix logic: Compare với quantity (requested)
            if (approvalDTO.getApprovedQuantity().equals(item.getQuantity())) {
                item.setStatus("APPROVED"); // Fully approved
            } else {
                item.setStatus("PARTIALLY_APPROVED"); // Partially approved
            }

            // Create ProductBatch for approved quantity
            createProductBatchFromApproval(item, approvalDTO, approvalDTO.getApprovedQuantity());
        }

        WarehouseRequestItem savedItem = itemRepository.save(item);

        WarehouseRequest parentRequest = requestRepository
                .findById(savedItem.getWarehouseRequest().getRequestId().intValue())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy warehouse request"));

        // Send notification
        if (parentRequest.getWarehouse() != null && parentRequest.getWarehouse().getManager() != null) {
            notificationService.sendNotificationToUsers(
                    Arrays.asList(savedItem.getWarehouseRequest().getWarehouse().getManager().getUserId()),
                    NotificationType.ALERT,
                    "Yêu cầu nhập hàng tới " + savedItem.getWarehouseRequest().getWarehouse().getName()
                            + " đã được duyệt");

        }

        // Update parent request status with properly loaded request
        updateParentRequestStatus(parentRequest);

        return savedItem;
    }

    private void createProductBatch(WarehouseRequestItem item, BulkApprovalRequestDTO.ApprovalItemInfo itemInfo) {
        if (item.getFulfilledQuantity() <= 0) {
            return; // Don't create batch for zero quantity
        }

        ProductUnit productUnit = productUnitRepository.findById(item.getProductUnitId().longValue())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy ProductUnit: " + item.getProductUnitId()));

        ProductBatch productBatch = new ProductBatch();
        productBatch.setProduct(productUnit.getProduct());
        productBatch.setUnit(productUnit.getUnit());
        productBatch.setQuantity(item.getFulfilledQuantity());
        productBatch.setWarehouse(item.getWarehouseRequest().getWarehouse());
        productBatch.setSupplier(itemInfo.getSupplierId() != null
                ? supplierRepository.findById(itemInfo.getSupplierId().longValue()).orElse(null)
                : null);
        productBatch.setExpirationDate(itemInfo.getExpiresAt());
        productBatch.setRequestItem(item);
        productBatch.setStatus("SHIPPED");
        productBatch.setImportPrice(
                productUnit.getPrice().multiply(BigDecimal.valueOf(item.getFulfilledQuantity())));
        productBatch.setManufactureDate(LocalDate.now());

        System.out.println("Creating ProductBatch: " + productBatch.getStatus() + " for Product: "
                + productBatch.getProduct().getName() + " with Quantity: " + productBatch.getQuantity());

        productBatchRepository.save(productBatch);
    }

    private void createProductBatchFromApproval(WarehouseRequestItem item, ItemApprovalDTO approvalDTO,
            Integer batchQuantity) {
        if (batchQuantity == null || batchQuantity <= 0) {
            return; // Don't create batch for zero quantity
        }

        ProductUnit productUnit = productUnitRepository.findById(item.getProductUnitId().longValue())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy ProductUnit: " + item.getProductUnitId()));

        ProductBatch productBatch = new ProductBatch();
        productBatch.setProduct(productUnit.getProduct());
        productBatch.setUnit(productUnit.getUnit());
        productBatch.setQuantity(batchQuantity); // Use the specific batch quantity, not total fulfilled
        productBatch.setWarehouse(item.getWarehouseRequest().getWarehouse());
        productBatch.setSupplier(approvalDTO.getSupplierId() != null
                ? supplierRepository.findById(approvalDTO.getSupplierId().longValue()).orElse(null)
                : null);
        productBatch.setExpirationDate(approvalDTO.getExpiresAt());
        productBatch.setRequestItem(item);
        productBatch.setStatus("SHIPPED");
        productBatch.setImportPrice(
                productUnit.getPrice().multiply(BigDecimal.valueOf(batchQuantity)));
        productBatch.setManufactureDate(LocalDate.now());

        System.out.println("Creating ProductBatch: " + productBatch.getStatus() + " for Product: "
                + productBatch.getProduct().getName() + " with Quantity: " + productBatch.getQuantity());

        productBatchRepository.save(productBatch);
    }

    // Overloaded method for backward compatibility
    private void createProductBatchFromApproval(WarehouseRequestItem item, ItemApprovalDTO approvalDTO) {
        createProductBatchFromApproval(item, approvalDTO, approvalDTO.getApprovedQuantity());
    }

    private void updateParentRequestStatus(WarehouseRequest request) {
        long totalItems = request.getItems().size();
        long approvedCount = request.getItems().stream().filter(i -> "APPROVED".equals(i.getStatus())).count();
        long rejectedCount = request.getItems().stream().filter(i -> "REJECTED".equals(i.getStatus())).count();
        long directCount = request.getItems().stream().filter(i -> "DIRECT_TRANSFER".equals(i.getStatus())).count();
        long partiallyApprovedCount = request.getItems().stream()
                .filter(i -> "PARTIALLY_APPROVED".equals(i.getStatus())).count();

        System.out.println("Status Update - Total: " + totalItems + ", Approved: " + approvedCount +
                ", Rejected: " + rejectedCount + ", Partially Approved: " + partiallyApprovedCount);

        if (approvedCount == totalItems) {
            request.setStatus("APPROVED");
        } else if (rejectedCount == totalItems) {
            request.setStatus("REJECTED");
        } else if (rejectedCount + approvedCount + directCount == totalItems) {
            request.setStatus("COMPLETED");
        } else if (approvedCount > 0 || partiallyApprovedCount > 0) {
            request.setStatus("PARTIALLY_APPROVED");
        } else {
            request.setStatus("PROCESSING");
        }

        requestRepository.save(request);
    }
}