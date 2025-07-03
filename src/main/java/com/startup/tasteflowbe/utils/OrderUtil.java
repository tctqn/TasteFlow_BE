//package com.startup.tasteflowbe.utils;
//
//import com.startup.tasteflowbe.dto.request.CartItemDTO;
//import com.startup.tasteflowbe.dto.request.InvoiceInfoDTO;
//import com.startup.tasteflowbe.model.*;
//import com.startup.tasteflowbe.repository.VoucherRepository;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//public class OrderUtil {
//
//    public static void fillInvoiceInfo(Order order, InvoiceInfoDTO invoice) {
//        order.setInvoiceCompanyName(invoice.getCompanyName());
//        order.setInvoiceEmail(invoice.getEmail());
//        order.setInvoiceTaxCode(invoice.getTaxCode());
//        order.setInvoiceCompanyAddress(invoice.getCompanyAddress());
//    }
//
//    public static OrderItem createOrderItemFromCart(Product product, CartItemDTO dto, List<Long> voucherIds, VoucherRepository voucherRepo) {
//        BigDecimal originalPrice = dto.getPrice();
//        BigDecimal finalPrice = originalPrice;
//        BigDecimal discount = BigDecimal.ZERO;
//
//        if (voucherIds != null) {
//            for (Long voucherId : voucherIds) {
//                Voucher voucher = voucherRepo.findById(voucherId).orElse(null);
//                if (voucher == null || voucher.getQuantity() <= 0) continue;
//                if (voucher.getStartDate().isAfter(LocalDateTime.now()) || voucher.getEndDate().isBefore(LocalDateTime.now())) continue;
//
//                if (voucher.getApplicableProducts().contains(product)
//                        || voucher.getApplicableCategories().contains(product.getCategory())) {
//                    if (voucher.getDiscountType().name().equals("PERCENT")) {
//                        BigDecimal percent = voucher.getDiscountPercent() != null ? voucher.getDiscountPercent() : BigDecimal.ZERO;
//                        BigDecimal currentDiscount = originalPrice.multiply(percent).divide(BigDecimal.valueOf(100));
//                        if (currentDiscount.compareTo(discount) > 0) {
//                            discount = currentDiscount;
//                        }
//                    } else if (voucher.getDiscountType().name().equals("AMOUNT")) {
//                        if (voucher.getDiscountAmount().compareTo(discount) > 0) {
//                            discount = voucher.getDiscountAmount();
//                        }
//                    }
//                    voucher.setQuantity(voucher.getQuantity() - 1);
//                    voucher.setClaimedCount(voucher.getClaimedCount() + 1);
//                    voucherRepo.save(voucher);
//                }
//            }
//        }
//
//        finalPrice = originalPrice.subtract(discount);
//        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) finalPrice = BigDecimal.ZERO;
//
//        OrderItem item = new OrderItem();
//        item.setProduct(product);
//        item.setProductUnit(product.getBaseUnit());
//        item.setQuantity(dto.getQuantity());
//        item.setPrice(finalPrice.multiply(BigDecimal.valueOf(dto.getQuantity())));
//        item.setDiscount(discount.multiply(BigDecimal.valueOf(dto.getQuantity())));
//        item.setQuantityInBase(dto.getQuantity());
//
//        return item;
//    }
//}