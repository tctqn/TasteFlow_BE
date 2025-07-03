package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.Invoice;
import com.startup.tasteflowbe.model.Order;

import java.io.IOException;
import java.util.List;

public interface InvoiceService {
    Invoice createInvoice(Invoice invoice);
    Invoice getInvoiceById(Long invoiceId);
    List<Invoice> getAllInvoices();
    Invoice getInvoiceByOrder(Order order);
    byte[] generateInvoicePdf(Order order, Invoice invoice) throws IOException;
}
