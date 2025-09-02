package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.ReturnItemRequestDTO;
import com.startup.tasteflowbe.dto.request.ReturnRequestRequestDTO;
import com.startup.tasteflowbe.dto.response.ReturnRequestResponseDTO;
import com.startup.tasteflowbe.enums.ReturnResolution;
import com.startup.tasteflowbe.enums.ReturnStatus;
import com.startup.tasteflowbe.enums.NotificationType;
import com.startup.tasteflowbe.mapper.ReturnMapper;
import com.startup.tasteflowbe.model.ReturnAttachment;
import com.startup.tasteflowbe.model.ReturnItem;
import com.startup.tasteflowbe.model.ReturnRequest;
import com.startup.tasteflowbe.model.Store;
import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.repository.ReturnAttachmentRepository;
import com.startup.tasteflowbe.repository.ReturnItemRepository;
import com.startup.tasteflowbe.repository.ReturnRequestRepository;
import com.startup.tasteflowbe.repository.StoreRepository;
import com.startup.tasteflowbe.repository.UserRepository;
import com.startup.tasteflowbe.service.NotificationService;
import com.startup.tasteflowbe.service.ProductService;
import com.startup.tasteflowbe.service.ReturnRequestService;
import com.startup.tasteflowbe.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnItemRepository returnItemRepository;
    private final ProductService productService;
    private final ReturnAttachmentRepository attachmentRepository;
    private final ReturnMapper mapper;
    private final S3Service s3Service;

    // NEW: giống bên OrderServiceImpl
    private final NotificationService notificationService;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    /* ========== Public APIs ========== */

    @Override
    public List<ReturnRequestResponseDTO> getAllReturnRequests() {
        List<ReturnRequest> all = returnRequestRepository.findAll();
        return mapper.toResponseList(all);
    }

    @Override
    public ReturnRequestResponseDTO getReturnRequestById(Long id) {
        ReturnRequest entity = returnRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest not found: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public List<ReturnRequestResponseDTO> getReturnRequestsByOriginalOrderCode(String orderCode) {
        return returnRequestRepository.findByOriginalOrderCode(orderCode)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public ReturnRequestResponseDTO createReturnRequest(ReturnRequestRequestDTO dto, MultipartFile image) throws IOException {
        ReturnRequest entity = mapper.toEntity(dto);

        attachItemsFromDto(entity, dto.getItems());

        ReturnRequest saved = returnRequestRepository.save(entity);

        if (image != null && !image.isEmpty()) {
            String url = uploadToS3(image);
            ReturnAttachment att = new ReturnAttachment();
            att.setReturnRequest(saved);
            att.setFileUrl(url);
            att.setFileType(image.getContentType());
            attachmentRepository.save(att);
            saved.getAttachments().add(att);
        }

        // NEW: Notify khi tạo
        notifyReturn(saved.getStoreId(), saved.getCustomerId(),
                "Yêu cầu trả hàng #" + saved.getId() + " cho đơn " + saved.getOriginalOrderCode() + " đã được tạo.");

        return mapper.toResponse(saved);
    }

    @Override
    public ReturnRequestResponseDTO updateReturnRequest(Long id, ReturnRequestRequestDTO dto, MultipartFile image) throws IOException {
        ReturnRequest entity = returnRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest not found: " + id));

        // Cập nhật các trường cơ bản (giữ createdAt/createdBy)
        entity.setOriginalOrderCode(dto.getOriginalOrderCode());
        entity.setStoreId(dto.getStoreId());
        entity.setCustomerId(dto.getCustomerId());
        entity.setReasonCode(dto.getReasonCode());
        entity.setNotes(dto.getNotes());

        // Nếu muốn reset status khi cập nhật, bỏ comment:
        // entity.setStatus(ReturnStatus.PENDING);

        // Thay items
        replaceItems(entity, dto.getItems());

        // Ảnh mới -> thêm attachment (không xoá ảnh cũ để giữ lịch sử)
        if (image != null && !image.isEmpty()) {
            String url = uploadToS3(image);
            ReturnAttachment att = new ReturnAttachment();
            att.setReturnRequest(entity);
            att.setFileUrl(url);
            att.setFileType(image.getContentType());
            attachmentRepository.save(att);
            entity.getAttachments().add(att);
        }

        ReturnRequest saved = returnRequestRepository.save(entity);

        // NEW: Notify khi cập nhật
        notifyReturn(saved.getStoreId(), saved.getCustomerId(),
                "Yêu cầu trả hàng #" + saved.getId() + " đã được cập nhật.");

        return mapper.toResponse(saved);
    }

    @Override
    public void deleteReturnRequest(Long id) {
        ReturnRequest entity = returnRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest not found: " + id));
        returnRequestRepository.delete(entity);
    }

    @Override
    public ReturnRequestResponseDTO approveReturnRequest(Long id) {
        ReturnRequest rr = returnRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return request not found"));

        if (rr.getStatus() != ReturnStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING requests can be approved");
        }

        // Nếu có REFUND thì yêu cầu đủ thông tin ngân hàng
        boolean hasRefund = rr.getItems().stream()
                .anyMatch(i -> i.getResolution() == ReturnResolution.REFUND);
        if (hasRefund && (isBlank(rr.getBankName()) || isBlank(rr.getBankAccount()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing bank info for REFUND request");
        }

        rr.setStatus(ReturnStatus.APPROVED);
        returnRequestRepository.save(rr);

        // NEW: Notify khi duyệt
        notifyReturn(rr.getStoreId(), rr.getCustomerId(),
                "Yêu cầu trả hàng #" + rr.getId() + " đã được chấp nhận.");

        return mapper.toResponse(rr);
    }

    @Override
    public ReturnRequestResponseDTO rejectReturnRequest(Long id, String reason) {
        ReturnRequest rr = returnRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return request not found"));

        if (rr.getStatus() != ReturnStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING requests can be rejected");
        }

        rr.setStatus(ReturnStatus.REJECTED);
        if (!isBlank(reason)) {
            String note = (rr.getNotes() == null ? "" : rr.getNotes() + "\n");
            rr.setNotes(note + "Lý do từ chối: " + reason);
        }
        returnRequestRepository.save(rr);

        // NEW: Notify khi từ chối
        notifyReturn(rr.getStoreId(), rr.getCustomerId(),
                "Yêu cầu trả hàng #" + rr.getId() + " đã bị từ chối" + (isBlank(reason) ? "." : (": " + reason)));

        return mapper.toResponse(rr);
    }

    /* ========== Helpers ========== */

    private void attachItemsFromDto(ReturnRequest entity, List<ReturnItemRequestDTO> itemDTOs) {
        if (itemDTOs == null || itemDTOs.isEmpty()) {
            throw new IllegalArgumentException("Return items must not be empty");
        }
        List<ReturnItem> items = new ArrayList<>(itemDTOs.size());
        for (ReturnItemRequestDTO itemDTO : itemDTOs) {
            ReturnItem item = mapper.toEntity(itemDTO);
            // map thêm tên sản phẩm để hiển thị nhanh
            item.setProductName(productService.getProductById(itemDTO.getProductId()).get().getName());
            item.setQuantity(itemDTO.getQty());
            item.setReturnRequest(entity);
            items.add(item);
        }
        entity.getItems().clear();
        entity.getItems().addAll(items);
    }

    private void replaceItems(ReturnRequest entity, List<ReturnItemRequestDTO> itemDTOs) {
        entity.getItems().clear(); // orphanRemoval=true: JPA tự xoá items cũ
        attachItemsFromDto(entity, itemDTOs);
    }

    private String uploadToS3(MultipartFile file) throws IOException {
        return s3Service.uploadImage(file);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Thu thập người nhận và gửi notify (khách hàng + quản lý cửa hàng).
     * Dùng NotificationType.ORDER để thống nhất với OrderServiceImpl.
     */
    private void notifyReturn(Long storeId, Long customerId, String message) {
        List<Long> recipients = new ArrayList<>(2);

        if (customerId != null) {
            userRepository.findById(customerId).ifPresent(u -> recipients.add(u.getUserId()));
        }
        if (storeId != null) {
            storeRepository.findById(storeId).ifPresent(store -> {
                User manager = store.getManager();
                if (manager != null) recipients.add(manager.getUserId());
            });
        }

        if (!recipients.isEmpty()) {
            notificationService.sendNotificationToUsers(recipients, NotificationType.ORDER, message);
        }
    }
}
