package com.littlehorizons.erp.dao.academic;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.model.academic.Teacher;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Teacher entities and academic assignments.
 */
public class TeacherDAO {

    public Integer save(Teacher teacher) {
        String sql = "INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, segundo_nombre, " +
                "primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, teacher.getDocumentType());
            ps.setString(2, teacher.getDocumentNumber());
            ps.setString(3, teacher.getFirstName());
            ps.setString(4, teacher.getSecondName());
            ps.setString(5, teacher.getFirstLastName());
            ps.setString(6, teacher.getSecondLastName());
            ps.setString(7, teacher.getEmail());
            ps.setString(8, teacher.getPhone());
            ps.setString(9, teacher.getSpecialization());
            ps.setDate(10, Date.valueOf(teacher.getHireDate()));
            ps.setString(11, teacher.getStatus() != null ? teacher.getStatus() : "Activo");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        teacher.setIdTeacher(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Failed to save teacher: " + teacher.getDocumentNumber(), e);
        }
        return null;
    }

    public Optional<Teacher> findById(int id) {
        String sql = "SELECT * FROM profesores WHERE id_profesor = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTeacher(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding teacher by ID: " + id, e);
        }
        return Optional.empty();
    }

    public List<Teacher> findAll() {
        List<Teacher> list = new ArrayList<>();
        String sql = "SELECT * FROM profesores ORDER BY primer_apellido, primer_nombre";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToTeacher(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error fetching all teachers", e);
        }
        return list;
    }

    public boolean assignToSubject(int teacherId, int subjectId, int year, String section) {
        String sql = "INSERT INTO asignaciones_profesor (id_profesor, id_materia, año_academico, seccion) " +
                "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE seccion = VALUES(seccion)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teacherId);
            ps.setInt(2, subjectId);
            ps.setInt(3, year);
            ps.setString(4, section != null ? section : "A");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error(String.format("Error assigning teacher %d to subject %d", teacherId, subjectId), e);
            return false;
        }
    }

    private Teacher mapResultSetToTeacher(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setIdTeacher(rs.getInt("id_profesor"));
        t.setDocumentType(rs.getString("tipo_documento"));
        t.setDocumentNumber(rs.getString("numero_documento"));
        t.setFirstName(rs.getString("primer_nombre"));
        t.setSecondName(rs.getString("segundo_nombre"));
        t.setFirstLastName(rs.getString("primer_apellido"));
        t.setSecondLastName(rs.getString("segundo_apellido"));
        t.setEmail(rs.getString("correo_electronico"));
        t.setPhone(rs.getString("telefono"));
        t.setSpecialization(rs.getString("especializacion"));
        Date hd = rs.getDate("fecha_contratacion");
        if (hd != null) {
            t.setHireDate(hd.toLocalDate());
        }
        t.setStatus(rs.getString("estado"));
        return t;
    }
}
