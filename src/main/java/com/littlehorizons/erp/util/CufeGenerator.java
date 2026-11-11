package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
public final class CufeGenerator {

    private static final String DEFAULT_NIT = "900543210-9";
    private static final String TECHNICAL_KEY = "DIAN-CLAVE-TECNICA-LITTLE-HORIZONS-2024-SECURE";

    private CufeGenerator() {
    }
    public static String generateCufe(String invoiceNumber, LocalDate issueDate, BigDecimal totalAmount, String buyerDocNumber) {
        String cleanInvoiceNum = invoiceNumber != null ? invoiceNumber.trim() : "FE-LH-0000";
        String dateStr = issueDate != null ? issueDate.toString() : LocalDate.now().toString();
        String totalStr = totalAmount != null ? totalAmount.setScale(2, java.math.RoundingMode.HALF_UP).toString() : "0.00";
        String buyerDoc = buyerDocNumber != null ? buyerDocNumber.trim() : "222222222222";

        String rawData = String.format("%s|%s|%s|0.00|0.00|%s|%s|%s|%s|1",
                cleanInvoiceNum,
                dateStr,
                totalStr,
                totalStr,
                DEFAULT_NIT,
                buyerDoc,
                TECHNICAL_KEY
        );

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LoggerUtil.error("Error generating CUFE with SHA-256 algorithm", e);
            return Integer.toHexString(rawData.hashCode()) + "0000000000000000";
        }
    }
}
