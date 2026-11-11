package com.littlehorizons.erp.dao.academic;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Student entities and student-guardian relationships.
 */
public class StudentDAO {

    public Integer save(Student student) {
        String sql = "INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, " +
                "primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, " +
                "correo_electronico, tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getDocumentType());
            ps.setString(2, student.getDocumentNumber());
            ps.setString(3, student.getFirstName());
            ps.setString(4, student.getSecondName());
            ps.setString(5, student.getFirstLastName());
            ps.setString(6, student.getSecondLastName());
            ps.setDate(7, Date.valueOf(student.getBirthDate()));
            ps.setString(8, student.getGender());
            ps.setString(9, student.getAddress());
            ps.setString(10, student.getPhone());
            ps.setString(11, student.getEmail());
            ps.setString(12, student.getBloodType());
            ps.setString(13, student.getAllergies());
            ps.setString(14, student.getMedicalConditions());
            ps.setDate(15, Date.valueOf(student.getAdmissionDate()));
            ps.setString(16, student.getStatus() != null ? student.getStatus() : "Activo");

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        student.setIdStudent(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Failed to save student: " + student.getDocumentNumber(), e);
        }
        return null;
    }

    public boolean update(Student student) {
        String sql = "UPDATE estudiantes SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, " +
                "segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, fecha_nacimiento = ?, " +
                "genero = ?, direccion = ?, telefono = ?, correo_electronico = ?, tipo_sangre = ?, " +
                "alergias = ?, condiciones_medicas = ?, estado = ? WHERE id_estudiante = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getDocumentType());
            ps.setString(2, student.getDocumentNumber());
            ps.setString(3, student.getFirstName());
            ps.setString(4, student.getSecondName());
            ps.setString(5, student.getFirstLastName());
            ps.setString(6, student.getSecondLastName());
            ps.setDate(7, Date.valueOf(student.getBirthDate()));
            ps.setString(8, student.getGender());
            ps.setString(9, student.getAddress());
            ps.setString(10, student.getPhone());
            ps.setString(11, student.getEmail());
            ps.setString(12, student.getBloodType());
            ps.setString(13, student.getAllergies());
            ps.setString(14, student.getMedicalConditions());
            ps.setString(15, student.getStatus());
            ps.setInt(16, student.getIdStudent());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Failed to update student ID: " + student.getIdStudent(), e);
            return false;
        }
    }

    public Optional<Student> findById(int id) {
        String sql = "SELECT * FROM estudiantes WHERE id_estudiante = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding student by ID: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Student> findByDocument(String documentNumber) {
        String sql = "SELECT * FROM estudiantes WHERE numero_documento = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding student by document: " + documentNumber, e);
        }
        return Optional.empty();
    }

    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes ORDER BY primer_apellido, primer_nombre";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error fetching all students", e);
        }
        return list;
    }

    public boolean associateGuardian(int studentId, int guardianId, boolean isPrimary, boolean isEmergency) {
        String sql = "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) " +
                "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE es_principal = VALUES(es_principal), es_contacto_emergencia = VALUES(es_contacto_emergencia)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, guardianId);
            ps.setBoolean(3, isPrimary);
            ps.setBoolean(4, isEmergency);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error(String.format("Error associating student %d with guardian %d", studentId, guardianId), e);
            return false;
        }
    }

    public boolean enrollInGrade(int studentId, int gradeId, int academicYear) {
        String sql = "INSERT INTO matriculas (id_estudiante, id_grado, año_academico, estado_matricula, fecha_matricula) " +
                "VALUES (?, ?, ?, 'Activa', CURRENT_DATE) ON DUPLICATE KEY UPDATE id_grado = VALUES(id_grado), estado_matricula = 'Activa'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, gradeId);
            ps.setInt(3, academicYear);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error(String.format("Error enrolling student %d in grade %d for year %d", studentId, gradeId, academicYear), e);
            return false;
        }
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setIdStudent(rs.getInt("id_estudiante"));
        s.setDocumentType(rs.getString("tipo_documento"));
        s.setDocumentNumber(rs.getString("numero_documento"));
        s.setFirstName(rs.getString("primer_nombre"));
        s.setSecondName(rs.getString("segundo_nombre"));
        s.setFirstLastName(rs.getString("primer_apellido"));
        s.setSecondLastName(rs.getString("segundo_apellido"));
        Date birth = rs.getDate("fecha_nacimiento");
        if (birth != null) {
            s.setBirthDate(birth.toLocalDate());
        }
        s.setGender(rs.getString("genero"));
        s.setAddress(rs.getString("direccion"));
        s.setPhone(rs.getString("telefono"));
        s.setEmail(rs.getString("correo_electronico"));
        s.setBloodType(rs.getString("tipo_sangre"));
        s.setAllergies(rs.getString("alergias"));
        s.setMedicalConditions(rs.getString("condiciones_medicas"));
        Date adm = rs.getDate("fecha_ingreso");
        if (adm != null) {
            s.setAdmissionDate(adm.toLocalDate());
        }
        s.setStatus(rs.getString("estado"));
        return s;
    }
}
