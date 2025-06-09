package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.CreateVoucherRequest;
import com.startup.tasteflowbe.model.Voucher;
import com.startup.tasteflowbe.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Voucher> getVoucherById(@PathVariable Long id) {
        return voucherService.getVoucherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Voucher> createVoucher(@ModelAttribute CreateVoucherRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .discountAmount(new BigDecimal(request.getDiscountAmount()))
                .discountType(request.getDiscountType())
                .startDate(LocalDate.parse(request.getStartDate(), formatter).atStartOfDay())
                .endDate(LocalDate.parse(request.getEndDate(), formatter).atStartOfDay())
                .quantity(request.getQuantity())
                .claimedCount(0)
                .build();

        return ResponseEntity.ok(voucherService.createVoucher(voucher));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(@PathVariable Long id, @RequestBody Voucher voucher) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, voucher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }
}
