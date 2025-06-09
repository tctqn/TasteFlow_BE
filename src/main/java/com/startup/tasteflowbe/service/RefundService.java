package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.Refund;

import java.util.List;

public interface RefundService {
    Refund createRefund(Refund refund);
    Refund getRefundById(Long refundId);
    List<Refund> getAllRefunds();
}
