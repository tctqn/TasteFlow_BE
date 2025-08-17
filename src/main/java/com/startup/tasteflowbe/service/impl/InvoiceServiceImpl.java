package com.startup.tasteflowbe.service.impl;

import com.lowagie.text.pdf.BaseFont;
import com.startup.tasteflowbe.model.Invoice;
import com.startup.tasteflowbe.model.Order;
import com.startup.tasteflowbe.repository.InvoiceRepository;
import com.startup.tasteflowbe.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Override
    public Invoice createInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice getInvoiceById(Long invoiceId) {
        Optional<Invoice> invoice = invoiceRepository.findById(invoiceId);
        return invoice.orElse(null);
    }

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Override
    public byte[] generateInvoicePdf(Order order, Invoice invoice) throws IOException {
        Context context = new Context();
        context.setVariable("orderId", order.getOrderCode());
        context.setVariable("staff", "Truong Cong Trinh");
        context.setVariable("createdAt", DATE_TIME_FORMATTER.format(invoice.getIssuedAt()));
        context.setVariable("companyName", invoice.getInvoiceCompanyName());
        context.setVariable("phone", invoice.getOrder().getPhone());
        context.setVariable("taxCode", invoice.getInvoiceTaxCode());
        context.setVariable("email", invoice.getInvoiceEmail());
        context.setVariable("address", invoice.getInvoiceCompanyAddress());
        context.setVariable("items", order.getOrderItems());
        context.setVariable("totalPrice", order.getFinalPrice());

        String html = templateEngine.process("invoice-template", context);

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);

        // Load font từ resources
        String fontResource = "fonts/DejaVuSans.ttf";
        try (InputStream fontStream = getClass().getClassLoader().getResourceAsStream(fontResource)) {
            if (fontStream == null) {
                throw new IOException("Font not found in resources: " + fontResource);
            }
            Path tempFontFile = Files.createTempFile("DejaVuSans", ".ttf");
            Files.copy(fontStream, tempFontFile, StandardCopyOption.REPLACE_EXISTING);

            renderer.getFontResolver().addFont(tempFontFile.toAbsolutePath().toString(),
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        }

        renderer.layout();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        renderer.createPDF(baos);
        return baos.toByteArray();
    }


    @Override
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    @Override
    public Invoice getInvoiceByOrder(Order order) {
        return invoiceRepository.findByOrder_OrderId(order.getOrderId())
                .orElseThrow(() -> new RuntimeException("No invoice found for order with ID: " + order.getOrderId()));
    }
}
