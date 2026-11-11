package com.littlehorizons.erp.dao.extracurricular;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.model.extracurricular.EventParticipant;
import com.littlehorizons.erp.model.extracurricular.SchoolEvent;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventDAO {

    public Integer save(SchoolEvent event) {
        String sql = "INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, " +
                "tiene_competencia, id_organizador, esta_activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, event.getEventName());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getEventType());
            ps.setDate(4, Date.valueOf(event.getEventDate()));
            ps.setString(5, event.getLocation());
            ps.setBoolean(6, event.getHasCompetition() != null ? event.getHasCompetition() : false);
            if (event.getIdOrganizer() != null) {
                ps.setInt(7, event.getIdOrganizer());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setBoolean(8, event.getIsActive() != null ? event.getIsActive() : true);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        event.setIdEvent(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Failed to save event: " + event.getEventName(), e);
        }
        return null;
    }

    public Optional<SchoolEvent> findById(int id) {
        String sql = "SELECT ev.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_organizador " +
                "FROM eventos ev " +
                "LEFT JOIN profesores p ON ev.id_organizador = p.id_profesor " +
                "WHERE ev.id_evento = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEvent(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding event by ID: " + id, e);
        }
        return Optional.empty();
    }

    public List<SchoolEvent> findAll() {
        List<SchoolEvent> list = new ArrayList<>();
        String sql = "SELECT ev.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_organizador " +
                "FROM eventos ev " +
                "LEFT JOIN profesores p ON ev.id_organizador = p.id_profesor " +
                "ORDER BY ev.fecha_evento DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEvent(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error fetching all events", e);
        }
        return list;
    }

    public boolean registerParticipant(int eventId, int studentId, String role, String position, String observations) {
        String sql = "INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, posicion_obtenida, observaciones) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE rol_participacion = VALUES(rol_participacion), " +
                "posicion_obtenida = VALUES(posicion_obtenida), observaciones = VALUES(observaciones)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            ps.setInt(2, studentId);
            ps.setString(3, role != null ? role : "Participante");
            ps.setString(4, position);
            ps.setString(5, observations);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error(String.format("Error registering participant %d in event %d", studentId, eventId), e);
            return false;
        }
    }

    public List<EventParticipant> findParticipantsByEvent(int eventId) {
        List<EventParticipant> list = new ArrayList<>();
        String sql = "SELECT pe.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, ev.nombre_evento " +
                "FROM participantes_evento pe " +
                "JOIN estudiantes e ON pe.id_estudiante = e.id_estudiante " +
                "JOIN eventos ev ON pe.id_evento = ev.id_evento " +
                "WHERE pe.id_evento = ? ORDER BY pe.posicion_obtenida DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EventParticipant ep = new EventParticipant();
                    ep.setIdParticipation(rs.getInt("id_participacion"));
                    ep.setIdEvent(rs.getInt("id_evento"));
                    ep.setIdStudent(rs.getInt("id_estudiante"));
                    ep.setParticipationRole(rs.getString("rol_participacion"));
                    ep.setPositionObtained(rs.getString("posicion_obtenida"));
                    ep.setObservations(rs.getString("observaciones"));
                    ep.setStudentName(rs.getString("nom_estudiante"));
                    ep.setEventName(rs.getString("nombre_evento"));
                    list.add(ep);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding participants for event " + eventId, e);
        }
        return list;
    }

    private SchoolEvent mapResultSetToEvent(ResultSet rs) throws SQLException {
        SchoolEvent ev = new SchoolEvent();
        ev.setIdEvent(rs.getInt("id_evento"));
        ev.setEventName(rs.getString("nombre_evento"));
        ev.setDescription(rs.getString("descripcion"));
        ev.setEventType(rs.getString("tipo_evento"));
        Date d = rs.getDate("fecha_evento");
        if (d != null) ev.setEventDate(d.toLocalDate());
        ev.setLocation(rs.getString("lugar"));
        ev.setHasCompetition(rs.getBoolean("tiene_competencia"));
        ev.setIdOrganizer(rs.getInt("id_organizador"));
        ev.setIsActive(rs.getBoolean("esta_activo"));
        ev.setOrganizerName(rs.getString("nom_organizador"));
        return ev;
    }
}
