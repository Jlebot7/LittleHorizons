package com.littlehorizons.erp.dao.extracurricular;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.model.extracurricular.Club;
import com.littlehorizons.erp.model.extracurricular.ClubMembership;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Extracurricular Clubs and Memberships.
 */
public class ClubDAO {

    public Integer save(Club club) {
        String sql = "INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, horario_reunion, esta_activo) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, club.getClubName());
            ps.setString(2, club.getDescription());
            if (club.getIdSupervisorTeacher() != null) {
                ps.setInt(3, club.getIdSupervisorTeacher());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, club.getMaxCapacity() != null ? club.getMaxCapacity() : 30);
            ps.setString(5, club.getMeetingSchedule());
            ps.setBoolean(6, club.getIsActive() != null ? club.getIsActive() : true);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        club.setIdClub(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Failed to save club: " + club.getClubName(), e);
        }
        return null;
    }

    public Optional<Club> findById(int id) {
        String sql = "SELECT c.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor, " +
                "(SELECT COUNT(*) FROM membresias_club mc WHERE mc.id_club = c.id_club AND mc.estado_membresia = 'Activa') AS inscritos " +
                "FROM clubes c " +
                "LEFT JOIN profesores p ON c.id_profesor_supervisor = p.id_profesor " +
                "WHERE c.id_club = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToClub(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding club by ID: " + id, e);
        }
        return Optional.empty();
    }

    public List<Club> findAll() {
        List<Club> list = new ArrayList<>();
        String sql = "SELECT c.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor, " +
                "(SELECT COUNT(*) FROM membresias_club mc WHERE mc.id_club = c.id_club AND mc.estado_membresia = 'Activa') AS inscritos " +
                "FROM clubes c " +
                "LEFT JOIN profesores p ON c.id_profesor_supervisor = p.id_profesor " +
                "ORDER BY c.nombre_club";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToClub(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error fetching all clubs", e);
        }
        return list;
    }

    public int getActiveMembersCount(int clubId) {
        String sql = "SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado_membresia = 'Activa'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error counting active members in club " + clubId, e);
        }
        return 0;
    }

    public boolean enrollStudent(int studentId, int clubId) {
        String sql = "INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, estado_membresia) " +
                "VALUES (?, ?, CURRENT_DATE, 'Activa')";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, clubId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error(String.format("Error enrolling student %d into club %d", studentId, clubId), e);
            return false;
        }
    }

    public boolean withdrawStudent(int membershipId, String reason) {
        String sql = "UPDATE membresias_club SET estado_membresia = 'Retirada', fecha_retiro = CURRENT_DATE, motivo_retiro = ? " +
                "WHERE id_membresia = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reason);
            ps.setInt(2, membershipId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error withdrawing membership " + membershipId, e);
            return false;
        }
    }

    public List<ClubMembership> findMembershipsByClub(int clubId) {
        List<ClubMembership> list = new ArrayList<>();
        String sql = "SELECT mc.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, c.nombre_club " +
                "FROM membresias_club mc " +
                "JOIN estudiantes e ON mc.id_estudiante = e.id_estudiante " +
                "JOIN clubes c ON mc.id_club = c.id_club " +
                "WHERE mc.id_club = ? ORDER BY mc.fecha_ingreso DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ClubMembership cm = new ClubMembership();
                    cm.setIdMembership(rs.getInt("id_membresia"));
                    cm.setIdStudent(rs.getInt("id_estudiante"));
                    cm.setIdClub(rs.getInt("id_club"));
                    Date d = rs.getDate("fecha_ingreso");
                    if (d != null) cm.setJoinDate(d.toLocalDate());
                    cm.setStatus(rs.getString("estado_membresia"));
                    cm.setStudentName(rs.getString("nom_estudiante"));
                    cm.setClubName(rs.getString("nombre_club"));
                    list.add(cm);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding memberships for club " + clubId, e);
        }
        return list;
    }

    private Club mapResultSetToClub(ResultSet rs) throws SQLException {
        Club c = new Club();
        c.setIdClub(rs.getInt("id_club"));
        c.setClubName(rs.getString("nombre_club"));
        c.setDescription(rs.getString("descripcion"));
        c.setIdSupervisorTeacher(rs.getInt("id_profesor_supervisor"));
        c.setMaxCapacity(rs.getInt("capacidad_maxima"));
        c.setMeetingSchedule(rs.getString("horario_reunion"));
        c.setIsActive(rs.getBoolean("esta_activo"));
        c.setSupervisorName(rs.getString("nom_profesor"));
        c.setEnrolledCount(rs.getInt("inscritos"));
        return c;
    }
}
