package com.littlehorizons.erp.dao.academic;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.model.academic.Guardian;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuardianDAO {

    public Integer save(Guardian guardian) {
        String sql = "INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, " +
                "primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, " +
                "correo_electronico, direccion, ocupacion, es_responsable_financiero) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, guardian.getDocumentType());
            ps.setString(2, guardian.getDocumentNumber());
            ps.setString(3, guardian.getFirstName());
            ps.setString(4, guardian.getSecondName());
            ps.setString(5, guardian.getFirstLastName());
            ps.setString(6, guardian.getSecondLastName());
            ps.setString(7, guardian.getRelationship());
            ps.setString(8, guardian.getMainPhone());
            ps.setString(9, guardian.getSecondaryPhone());
            ps.setString(10, guardian.getEmail());
            ps.setString(11, guardian.getAddress());
            ps.setString(12, guardian.getOccupation());
            ps.setBoolean(13, guardian.getIsFinancialResponsible() != null ? guardian.getIsFinancialResponsible() : true);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        guardian.setIdGuardian(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Failed to save guardian: " + guardian.getDocumentNumber(), e);
        }
        return null;
    }

    public Optional<Guardian> findById(int id) {
        String sql = "SELECT * FROM acudientes WHERE id_acudiente = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuardian(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding guardian by ID: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Guardian> findByDocument(String docNumber) {
        String sql = "SELECT * FROM acudientes WHERE numero_documento = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, docNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuardian(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding guardian by doc: " + docNumber, e);
        }
        return Optional.empty();
    }

    public List<Guardian> findAll() {
        List<Guardian> list = new ArrayList<>();
        String sql = "SELECT * FROM acudientes ORDER BY primer_apellido, primer_nombre";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToGuardian(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error fetching all guardians", e);
        }
        return list;
    }

    public List<Student> findStudentsByGuardian(int guardianId) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT e.* FROM estudiantes e " +
                "INNER JOIN estudiante_acudiente ea ON e.id_estudiante = ea.id_estudiante " +
                "WHERE ea.id_acudiente = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, guardianId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student();
                    s.setIdStudent(rs.getInt("id_estudiante"));
                    s.setDocumentType(rs.getString("tipo_documento"));
                    s.setDocumentNumber(rs.getString("numero_documento"));
                    s.setFirstName(rs.getString("primer_nombre"));
                    s.setSecondName(rs.getString("segundo_nombre"));
                    s.setFirstLastName(rs.getString("primer_apellido"));
                    s.setSecondLastName(rs.getString("segundo_apellido"));
                    students.add(s);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding students for guardian: " + guardianId, e);
        }
        return students;
    }

    private Guardian mapResultSetToGuardian(ResultSet rs) throws SQLException {
        Guardian g = new Guardian();
        g.setIdGuardian(rs.getInt("id_acudiente"));
        g.setDocumentType(rs.getString("tipo_documento"));
        g.setDocumentNumber(rs.getString("numero_documento"));
        g.setFirstName(rs.getString("primer_nombre"));
        g.setSecondName(rs.getString("segundo_nombre"));
        g.setFirstLastName(rs.getString("primer_apellido"));
        g.setSecondLastName(rs.getString("segundo_apellido"));
        g.setRelationship(rs.getString("parentesco"));
        g.setMainPhone(rs.getString("telefono_principal"));
        g.setSecondaryPhone(rs.getString("telefono_secundario"));
        g.setEmail(rs.getString("correo_electronico"));
        g.setAddress(rs.getString("direccion"));
        g.setOccupation(rs.getString("ocupacion"));
        g.setIsFinancialResponsible(rs.getBoolean("es_responsable_financiero"));
        return g;
    }
}
