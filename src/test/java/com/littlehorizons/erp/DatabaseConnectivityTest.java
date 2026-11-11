package com.littlehorizons.erp;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.dao.academic.StudentDAO;
import com.littlehorizons.erp.model.academic.Student;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseConnectivityTest {

    @Test
    void testLiveDatabaseConnection() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM estudiantes")) {
            assertTrue(rs.next());
            int count = rs.getInt(1);
            assertTrue(count >= 30, "Expected at least 30 students in database");
        } catch (Exception e) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "MySQL is not accessible: " + e.getMessage());
        }
    }

    @Test
    void testStudentDaoFindAll() {
        try {
            StudentDAO dao = new StudentDAO();
            List<Student> students = dao.findAll();
            assertNotNull(students);
            assertFalse(students.isEmpty());
            assertTrue(students.size() >= 30);
        } catch (Exception e) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "MySQL is not accessible: " + e.getMessage());
        }
    }
}
