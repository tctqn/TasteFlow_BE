package com.startup.tasteflowbe.utils;

import java.util.concurrent.ThreadLocalRandom;

public class OrderCodeGenerator {

    public static String generateOrderCode() {
        long timePart = System.currentTimeMillis();
        long randomPart = ThreadLocalRandom.current().nextLong(100, 999); // 3 chữ số random
        long orderCode = timePart * 1000 + randomPart;
        return String.valueOf(orderCode);
    }
}
