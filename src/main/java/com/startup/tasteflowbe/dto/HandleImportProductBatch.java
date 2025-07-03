package com.startup.tasteflowbe.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HandleImportProductBatch {
    private String status;
    private BigDecimal importPrice;
    private LocalDate expirationDate;

    public HandleImportProductBatch() {
    }

    public HandleImportProductBatch(String status, BigDecimal importPrice, LocalDate expirationDate) {
        this.status = status;
        this.importPrice = importPrice;
        this.expirationDate = expirationDate;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}
