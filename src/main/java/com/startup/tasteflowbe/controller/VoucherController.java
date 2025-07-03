package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.response.VoucherResponseDTO;
import com.startup.tasteflowbe.dto.request.CreateVoucherRequest;
import com.startup.tasteflowbe.enums.DiscountType;
import com.startup.tasteflowbe.enums.DistributionType;
import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.model.Voucher;
import com.startup.tasteflowbe.repository.UserRepository;
import com.startup.tasteflowbe.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;
    private final UserRepository userRepository;

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

    @GetMapping("/available")
    public ResponseEntity<List<VoucherResponseDTO>> getAvailableVouchers(@RequestParam BigDecimal totalPrice) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            // Anonymous user → chỉ trả voucher PUBLIC
            return ResponseEntity.ok(voucherService.getPublicVouchers(totalPrice));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        return ResponseEntity.ok(voucherService.getAvailableVouchers(user, totalPrice));
    }

    @PostMapping
    public ResponseEntity<Voucher> createVoucher(@RequestBody CreateVoucherRequest request) {
        return ResponseEntity.ok(voucherService.createVoucher(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(@PathVariable Long id, @RequestBody CreateVoucherRequest voucher) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, voucher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }
}
