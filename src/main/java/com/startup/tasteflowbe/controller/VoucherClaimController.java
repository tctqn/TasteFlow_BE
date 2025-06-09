package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.model.UserVoucher;
import com.startup.tasteflowbe.model.Voucher;
import com.startup.tasteflowbe.service.UserVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/voucher-claim")
@RequiredArgsConstructor
public class VoucherClaimController {

    private final UserVoucherService userVoucherService;

    @PostMapping("/claim")
    public ResponseEntity<?> claimVoucher(
            @RequestParam Long userId,
            @RequestParam Long voucherId) {
        try {
            UserVoucher claimed = userVoucherService.claimVoucher(userId, voucherId);
            return ResponseEntity.ok(claimed);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/use")
    public ResponseEntity<?> useVoucher(@RequestParam Long userVoucherId) {
        try {
            UserVoucher used = userVoucherService.useVoucher(userVoucherId);
            return ResponseEntity.ok(used);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 
