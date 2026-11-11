package com.littlehorizons.erp;

import com.littlehorizons.erp.util.ValidationUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecurityIntegrityAuditTest {

    private static final Path MAIN_JAVA_DIR = Paths.get("src", "main", "java");

    @Test
    void testZeroCommentsInMainSourceCode() throws IOException {
        Pattern commentPattern = Pattern.compile("^\\s*//|^\\s*/\\*|\\*/");

        try (Stream<Path> paths = Files.walk(MAIN_JAVA_DIR)) {
            List<Path> javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());

            assertFalse(javaFiles.isEmpty(), "Main Java files should be present");

            for (Path file : javaFiles) {
                List<String> lines = Files.readAllLines(file);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    boolean hasComment = commentPattern.matcher(line).find();
                    assertFalse(hasComment, "File " + file.getFileName() + " at line " + (i + 1) + " contains a comment: " + line);
                }
            }
        }
    }

    @Test
    void testSqlInjectionPreventionInDAOs() throws IOException {
        Path daoDir = MAIN_JAVA_DIR.resolve(Paths.get("com", "littlehorizons", "erp", "dao"));
        Pattern dangerousSqlConcat = Pattern.compile("WHERE\\s+.*\\+\\s*[a-zA-Z]|VALUES\\s*\\(.*\\+\\s*[a-zA-Z]", Pattern.CASE_INSENSITIVE);

        try (Stream<Path> paths = Files.walk(daoDir)) {
            List<Path> daoFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith("DAO.java"))
                    .collect(Collectors.toList());

            assertTrue(daoFiles.size() >= 7, "All DAO classes must be scanned");

            for (Path dao : daoFiles) {
                String content = Files.readString(dao);
                assertFalse(dangerousSqlConcat.matcher(content).find(),
                        "DAO " + dao.getFileName() + " has dynamic SQL concatenation potential vulnerability!");

                assertTrue(content.contains("prepareStatement") || content.contains("Statement"),
                        "DAO " + dao.getFileName() + " must use prepared statements");

                assertTrue(content.contains("try (Connection"),
                        "DAO " + dao.getFileName() + " must manage connections with try-with-resources");
            }
        }
    }

    @Test
    void testValidationSecurityBoundaries() {
        assertFalse(ValidationUtil.isValidDate("2024-02-30"));
        assertFalse(ValidationUtil.isValidDate("2024-13-01"));
        assertFalse(ValidationUtil.isValidDate("invalid-date"));
        assertTrue(ValidationUtil.isValidDate("2024-02-29"));

        assertFalse(ValidationUtil.isValidGrade(new BigDecimal("-0.1")));
        assertFalse(ValidationUtil.isValidGrade(new BigDecimal("5.01")));
        assertTrue(ValidationUtil.isValidGrade(new BigDecimal("0.0")));
        assertTrue(ValidationUtil.isValidGrade(new BigDecimal("5.0")));
        assertTrue(ValidationUtil.isValidGrade(new BigDecimal("3.5")));

        assertFalse(ValidationUtil.isValidEmail("invalid-email"));
        assertFalse(ValidationUtil.isValidEmail("@school.com"));
        assertTrue(ValidationUtil.isValidEmail("rectoria@littlehorizons.edu.co"));

        assertFalse(ValidationUtil.isValidDocumentNumber(""));
        assertFalse(ValidationUtil.isValidDocumentNumber("   "));
        assertFalse(ValidationUtil.isValidDocumentNumber("abc"));
        assertTrue(ValidationUtil.isValidDocumentNumber("1098765432"));
    }
}
