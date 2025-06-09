package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.Invoice;

import java.util.List;

public interface InvoiceService {
    Invoice createInvoice(Invoice invoice);
    Invoice getInvoiceById(Long invoiceId);
    List<Invoice> getAllInvoices();
}
