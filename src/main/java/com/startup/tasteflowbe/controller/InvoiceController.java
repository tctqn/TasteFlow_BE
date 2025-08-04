package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.Invoice;
import com.startup.tasteflowbe.model.Order;
import com.startup.tasteflowbe.service.InvoiceService;
import com.startup.tasteflowbe.service.OrderService;
import com.startup.tasteflowbe.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    private final OrderService orderService;
    private final S3Service s3Service;

    @GetMapping("/presigned/{orderId}")
    public ResponseEntity<String> getPresignedInvoiceUrl(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Invoice invoice = invoiceService.getInvoiceByOrder(order);
        String key = invoice.getInvoiceUrl().replace("https://tasteflow.s3.ap-southeast-2.amazonaws.com/", "");

        String presignedUrl = s3Service.generatePresignedUrl(key);
        return ResponseEntity.ok(presignedUrl);
    }


    @PostMapping
    public Invoice createInvoice(@RequestBody Invoice invoice) {
        return invoiceService.createInvoice(invoice);
    }

    @GetMapping("/{id}")
    public Invoice getInvoiceById(@PathVariable("id") Long id) {
        return invoiceService.getInvoiceById(id);
    }

    @GetMapping
    public List<Invoice> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }
}
