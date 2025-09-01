package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.request.ReturnRequestRequestDTO;
import com.startup.tasteflowbe.dto.response.ReturnRequestResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ReturnRequestService {
    List<ReturnRequestResponseDTO> getAllReturnRequests();

    ReturnRequestResponseDTO getReturnRequestById(Long id);

    List<ReturnRequestResponseDTO> getReturnRequestsByOriginalOrderCode(String orderCode);

    ReturnRequestResponseDTO createReturnRequest(ReturnRequestRequestDTO returnRequest, MultipartFile image) throws IOException;

    ReturnRequestResponseDTO updateReturnRequest(Long id, ReturnRequestRequestDTO returnRequest, MultipartFile image) throws IOException;

    void deleteReturnRequest(Long id);
    ReturnRequestResponseDTO approveReturnRequest(Long id);
    ReturnRequestResponseDTO rejectReturnRequest(Long id, String reason);
}
