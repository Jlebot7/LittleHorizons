package com.littlehorizons.erp;

import com.littlehorizons.erp.util.CufeGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CufeGeneratorTest {

    @Test
    @DisplayName("Should generate a valid 64-character hex CUFE")
    void testCufeLengthAndFormat() {
        String invoiceNum = "FE-LH-2024-0001";
        LocalDate date = LocalDate.of(2024, 2, 15);
        BigDecimal amount = new BigDecimal("420000.00");
        String docNumber = "1018456201";

        String cufe = CufeGenerator.generateCufe(invoiceNum, date, amount, docNumber);

        assertNotNull(cufe);
        assertEquals(64, cufe.length(), "CUFE must be a 64-character SHA-256 hexadecimal string");
        assertTrue(cufe.matches("^[a-f0-9]{64}$"), "CUFE must only contain lowercase hexadecimal characters");
    }

    @Test
    @DisplayName("Identical inputs must generate identical CUFE (deterministic hash)")
    void testCufeDeterministic() {
        String cufe1 = CufeGenerator.generateCufe("FE-LH-2024-0002", LocalDate.of(2024, 3, 1), new BigDecimal("500000.00"), "1020456202");
        String cufe2 = CufeGenerator.generateCufe("FE-LH-2024-0002", LocalDate.of(2024, 3, 1), new BigDecimal("500000.00"), "1020456202");

        assertEquals(cufe1, cufe2);
    }
}
