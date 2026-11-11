package com.littlehorizons.erp.dao.academic;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.model.academic.AcademicRecord;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AcademicRecordDAO {

    public boolean saveOrUpdate(AcademicRecord record) {
        String sql = "INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, " +
                "año_academico, periodo_academico, valor_nota, observaciones, fecha_registro) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_DATE) " +
                "ON DUPLICATE KEY UPDATE valor_nota = VALUES(valor_nota), observaciones = VALUES(observaciones), " +
                "id_profesor = VALUES(id_profesor), fecha_registro = CURRENT_DATE";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, record.getIdStudent());
            ps.setInt(2, record.getIdSubject());
            ps.setInt(3, record.getIdTeacher());
            ps.setInt(4, record.getAcademicYear());
            ps.setString(5, record.getAcademicPeriod());
            ps.setBigDecimal(6, record.getGradeValue());
            ps.setString(7, record.getObservations());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error saving academic record for student " + record.getIdStudent(), e);
            return false;
        }
    }

    public List<AcademicRecord> findByStudent(int studentId) {
        List<AcademicRecord> list = new ArrayList<>();
        String sql = "SELECT r.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "m.nombre_materia, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor " +
                "FROM registros_academicos r " +
                "JOIN estudiantes e ON r.id_estudiante = e.id_estudiante " +
                "JOIN materias m ON r.id_materia = m.id_materia " +
                "JOIN profesores p ON r.id_profesor = p.id_profesor " +
                "WHERE r.id_estudiante = ? ORDER BY r.año_academico DESC, r.periodo_academico";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding academic records for student " + studentId, e);
        }
        return list;
    }

    public List<AcademicRecord> findBySubjectAndPeriod(int subjectId, int year, String period) {
        List<AcademicRecord> list = new ArrayList<>();
        String sql = "SELECT r.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "m.nombre_materia, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor " +
                "FROM registros_academicos r " +
                "JOIN estudiantes e ON r.id_estudiante = e.id_estudiante " +
                "JOIN materias m ON r.id_materia = m.id_materia " +
                "JOIN profesores p ON r.id_profesor = p.id_profesor " +
                "WHERE r.id_materia = ? AND r.año_academico = ? AND r.periodo_academico = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, subjectId);
            ps.setInt(2, year);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding grades by subject and period", e);
        }
        return list;
    }

    public List<AcademicRecord> findAll() {
        List<AcademicRecord> list = new ArrayList<>();
        String sql = "SELECT r.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "m.nombre_materia, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor " +
                "FROM registros_academicos r " +
                "JOIN estudiantes e ON r.id_estudiante = e.id_estudiante " +
                "JOIN materias m ON r.id_materia = m.id_materia " +
                "JOIN profesores p ON r.id_profesor = p.id_profesor " +
                "ORDER BY r.id_registro DESC LIMIT 200";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding all academic records", e);
        }
        return list;
    }

    private AcademicRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        AcademicRecord r = new AcademicRecord();
        r.setIdRecord(rs.getInt("id_registro"));
        r.setIdStudent(rs.getInt("id_estudiante"));
        r.setIdSubject(rs.getInt("id_materia"));
        r.setIdTeacher(rs.getInt("id_profesor"));
        r.setAcademicYear(rs.getInt("año_academico"));
        r.setAcademicPeriod(rs.getString("periodo_academico"));
        r.setGradeValue(rs.getBigDecimal("valor_nota"));
        r.setObservations(rs.getString("observaciones"));
        Date d = rs.getDate("fecha_registro");
        if (d != null) {
            r.setRecordDate(d.toLocalDate());
        }
        r.setStudentName(rs.getString("nom_estudiante"));
        r.setSubjectName(rs.getString("nombre_materia"));
        r.setTeacherName(rs.getString("nom_profesor"));
        return r;
    }
}
