package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.Refund;
import com.startup.tasteflowbe.repository.RefundRepository;
import com.startup.tasteflowbe.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RefundServiceImpl implements RefundService {

    @Autowired
    private RefundRepository refundRepository;

    @Override
    public Refund createRefund(Refund refund) {
        return refundRepository.save(refund);
    }

    @Override
    public Refund getRefundById(Long refundId) {
        Optional<Refund> refund = refundRepository.findById(refundId);
        return refund.orElse(null);
    }

    @Override
    public List<Refund> getAllRefunds() {
        return refundRepository.findAll();
    }
}
