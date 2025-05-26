package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.Refund;
import com.startup.tasteflowbe.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public Refund createRefund(@RequestBody Refund refund) {
        return refundService.createRefund(refund);
    }

    @GetMapping("/{id}")
    public Refund getRefundById(@PathVariable("id") Long id) {
        return refundService.getRefundById(id);
    }

    @GetMapping
    public List<Refund> getAllRefunds() {
        return refundService.getAllRefunds();
    }
}
