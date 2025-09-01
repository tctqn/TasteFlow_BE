package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.request.ReturnItemRequestDTO;
import com.startup.tasteflowbe.dto.request.ReturnRequestRequestDTO;
import com.startup.tasteflowbe.dto.response.ReturnAttachmentResponseDTO;
import com.startup.tasteflowbe.dto.response.ReturnItemResponseDTO;
import com.startup.tasteflowbe.dto.response.ReturnRequestResponseDTO;
import com.startup.tasteflowbe.model.ReturnAttachment;
import com.startup.tasteflowbe.model.ReturnItem;
import com.startup.tasteflowbe.model.ReturnRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReturnMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(com.startup.tasteflowbe.enums.ReturnStatus.PENDING)")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "items", source = "items") // <-- đừng ignore
    @Mapping(target = "attachments", ignore = true)
    ReturnRequest toEntity(ReturnRequestRequestDTO req);
    @Mapping(target = "quantity", source = "qty")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "returnRequest", ignore = true)
    ReturnItem toEntity(ReturnItemRequestDTO req);

    @AfterMapping
    default void setParent(@MappingTarget ReturnRequest entity) {
        if (entity.getItems() != null) {
            for (ReturnItem i : entity.getItems()) {
                i.setReturnRequest(entity);
            }
        }
        // Nếu sau này map attachments từ request:
        // if (entity.getAttachments() != null) {
        //   for (ReturnAttachment a : entity.getAttachments()) {
        //       a.setReturnRequest(entity);
        //   }
        // }
    }

    // ===== Entity -> Response DTO =====
    @Mapping(target = "items", source = "items")
    @Mapping(target = "attachments", source = "attachments")
    ReturnRequestResponseDTO toResponse(ReturnRequest entity);

    @Mapping(target = "qty", source = "quantity")
    ReturnItemResponseDTO toItemResponse(ReturnItem entity);

    @Mapping(target = "returnItemId", source = "returnItem.id")
    ReturnAttachmentResponseDTO toAttachmentResponse(ReturnAttachment entity);

    List<ReturnRequestResponseDTO> toResponseList(List<ReturnRequest> list);
}