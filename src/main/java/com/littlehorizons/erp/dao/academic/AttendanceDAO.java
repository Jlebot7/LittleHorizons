package com.littlehorizons.erp.dao.academic;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.model.academic.Attendance;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public boolean recordAttendance(Attendance attendance) {
        String sql = "INSERT INTO asistencias (id_estudiante, id_materia, fecha_asistencia, estado_asistencia, observaciones, registrado_por) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE estado_asistencia = VALUES(estado_asistencia), observaciones = VALUES(observaciones)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, attendance.getIdStudent());
            ps.setInt(2, attendance.getIdSubject());
            ps.setDate(3, Date.valueOf(attendance.getAttendanceDate()));
            ps.setString(4, attendance.getStatus());
            ps.setString(5, attendance.getObservations());
            if (attendance.getRecordedBy() != null) {
                ps.setInt(6, attendance.getRecordedBy());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error recording attendance for student " + attendance.getIdStudent(), e);
            return false;
        }
    }

    public List<Attendance> findByStudent(int studentId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "m.nombre_materia " +
                "FROM asistencias a " +
                "JOIN estudiantes e ON a.id_estudiante = e.id_estudiante " +
                "JOIN materias m ON a.id_materia = m.id_materia " +
                "WHERE a.id_estudiante = ? ORDER BY a.fecha_asistencia DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding attendance for student " + studentId, e);
        }
        return list;
    }

    public List<Attendance> findAllRecent() {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "m.nombre_materia " +
                "FROM asistencias a " +
                "JOIN estudiantes e ON a.id_estudiante = e.id_estudiante " +
                "JOIN materias m ON a.id_materia = m.id_materia " +
                "ORDER BY a.fecha_asistencia DESC LIMIT 100";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToAttendance(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding recent attendance records", e);
        }
        return list;
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setIdAttendance(rs.getInt("id_asistencia"));
        a.setIdStudent(rs.getInt("id_estudiante"));
        a.setIdSubject(rs.getInt("id_materia"));
        Date d = rs.getDate("fecha_asistencia");
        if (d != null) {
            a.setAttendanceDate(d.toLocalDate());
        }
        a.setStatus(rs.getString("estado_asistencia"));
        a.setObservations(rs.getString("observaciones"));
        a.setRecordedBy(rs.getInt("registrado_por"));
        a.setStudentName(rs.getString("nom_estudiante"));
        a.setSubjectName(rs.getString("nombre_materia"));
        return a;
    }
}
