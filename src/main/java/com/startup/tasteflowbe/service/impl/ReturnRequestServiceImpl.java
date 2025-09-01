package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.ReturnItemRequestDTO;
import com.startup.tasteflowbe.dto.request.ReturnRequestRequestDTO;
import com.startup.tasteflowbe.dto.response.ReturnRequestResponseDTO;
import com.startup.tasteflowbe.enums.ReturnStatus;
import com.startup.tasteflowbe.mapper.ReturnMapper;
import com.startup.tasteflowbe.model.ReturnAttachment;
import com.startup.tasteflowbe.model.ReturnItem;
import com.startup.tasteflowbe.model.ReturnRequest;
import com.startup.tasteflowbe.repository.ReturnAttachmentRepository;
import com.startup.tasteflowbe.repository.ReturnItemRepository;
import com.startup.tasteflowbe.repository.ReturnRequestRepository;
import com.startup.tasteflowbe.service.ProductService;
import com.startup.tasteflowbe.service.ReturnRequestService;
import com.startup.tasteflowbe.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        // Nếu muốn reset status khi cập nhật, bỏ comment dòng dưới:
        // entity.setStatus(ReturnStatus.PENDING);

        // Thay thế toàn bộ items bằng danh sách mới từ DTO
        replaceItems(entity, dto.getItems());

        // Ảnh mới (tùy chọn) -> thêm attachment mới (không xóa cái cũ để giữ lịch sử)
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
        return mapper.toResponse(saved);
    }

    @Override
    public void deleteReturnRequest(Long id) {
        ReturnRequest entity = returnRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest not found: " + id));
        // orphanRemoval=true ở cả items và attachments nên chỉ cần delete request
        returnRequestRepository.delete(entity);
    }

    /* ------------- Helpers ------------- */

    private void attachItemsFromDto(ReturnRequest entity, List<ReturnItemRequestDTO> itemDTOs) {
        if (itemDTOs == null || itemDTOs.isEmpty()) {
            throw new IllegalArgumentException("Return items must not be empty");
        }
        List<ReturnItem> items = new ArrayList<>(itemDTOs.size());
        for (ReturnItemRequestDTO itemDTO : itemDTOs) {
            ReturnItem item = mapper.toEntity(itemDTO);
            item.setProductName(productService.getProductById(itemDTO.getProductId()).get().getName());
            item.setQuantity(itemDTO.getQty());
            item.setReturnRequest(entity);
            items.add(item);
        }
        entity.getItems().clear();
        entity.getItems().addAll(items);
    }

    private void replaceItems(ReturnRequest entity, List<ReturnItemRequestDTO> itemDTOs) {
        // Do có orphanRemoval=true, chỉ cần clear và add mới, JPA sẽ xóa cũ
        entity.getItems().clear();
        attachItemsFromDto(entity, itemDTOs);
    }

    private String uploadToS3(MultipartFile file) throws IOException {
        return s3Service.uploadImage(file);
    }

    private String guessExtension(String filename) {
        if (filename == null) return null;
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return null;
        return filename.substring(dot + 1);
    }
}
