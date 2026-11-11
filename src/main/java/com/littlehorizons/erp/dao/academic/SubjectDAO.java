package com.littlehorizons.erp.dao.academic;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.model.academic.AcademicGrade;
import com.littlehorizons.erp.model.academic.Subject;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Academic Subjects and Grades.
 */
public class SubjectDAO {

    public Integer save(Subject subject) {
        String sql = "INSERT INTO materias (codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, subject.getSubjectCode());
            ps.setString(2, subject.getSubjectName());
            ps.setString(3, subject.getDescription());
            ps.setInt(4, subject.getIdGrade());
            ps.setInt(5, subject.getWeeklyHours() != null ? subject.getWeeklyHours() : 3);
            ps.setBoolean(6, subject.getIsActive() != null ? subject.getIsActive() : true);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        subject.setIdSubject(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Failed to save subject: " + subject.getSubjectCode(), e);
        }
        return null;
    }

    public Optional<Subject> findById(int id) {
        String sql = "SELECT * FROM materias WHERE id_materia = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding subject by ID: " + id, e);
        }
        return Optional.empty();
    }

    public List<Subject> findByGrade(int gradeId) {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM materias WHERE id_grado = ? ORDER BY nombre_materia";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, gradeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding subjects by grade: " + gradeId, e);
        }
        return list;
    }

    public List<Subject> findAll() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM materias ORDER BY id_grado, nombre_materia";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error fetching all subjects", e);
        }
        return list;
    }

    public List<AcademicGrade> findAllGrades() {
        List<AcademicGrade> grades = new ArrayList<>();
        String sql = "SELECT * FROM grados ORDER BY nivel_numerico";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                grades.add(new AcademicGrade(
                        rs.getInt("id_grado"),
                        rs.getString("nombre_grado"),
                        rs.getInt("nivel_numerico")
                ));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error fetching all grades", e);
        }
        return grades;
    }

    public boolean enrollStudent(int studentId, int subjectId, int year, String period, String section) {
        String sql = "INSERT INTO inscripciones_materia (id_estudiante, id_materia, año_academico, periodo_academico, seccion, fecha_inscripcion, estado) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_DATE, 'Activa') ON DUPLICATE KEY UPDATE estado = 'Activa'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, year);
            ps.setString(4, period);
            ps.setString(5, section != null ? section : "A");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error(String.format("Error enrolling student %d in subject %d", studentId, subjectId), e);
            return false;
        }
    }

    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setIdSubject(rs.getInt("id_materia"));
        s.setSubjectCode(rs.getString("codigo_materia"));
        s.setSubjectName(rs.getString("nombre_materia"));
        s.setDescription(rs.getString("descripcion"));
        s.setIdGrade(rs.getInt("id_grado"));
        s.setWeeklyHours(rs.getInt("horas_semanales"));
        s.setIsActive(rs.getBoolean("esta_activa"));
        return s;
    }
}
