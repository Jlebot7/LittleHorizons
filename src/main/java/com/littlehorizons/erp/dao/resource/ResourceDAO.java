package com.littlehorizons.erp.dao.resource;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.model.resource.Resource;
import com.littlehorizons.erp.model.resource.ResourceCategory;
import com.littlehorizons.erp.model.resource.StudentLoan;
import com.littlehorizons.erp.model.resource.TeacherLoan;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResourceDAO {

    public Integer saveResource(Resource resource) {
        String sql = "INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, descripcion, " +
                "fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, resource.getResourceCode());
            ps.setString(2, resource.getResourceName());
            ps.setInt(3, resource.getIdCategory());
            ps.setString(4, resource.getDescription());
            if (resource.getAcquisitionDate() != null) {
                ps.setDate(5, Date.valueOf(resource.getAcquisitionDate()));
            } else {
                ps.setDate(5, Date.valueOf(LocalDate.now()));
            }
            ps.setString(6, resource.getPhysicalLocation());
            ps.setString(7, resource.getStatus() != null ? resource.getStatus() : "Disponible");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        resource.setIdResource(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Failed to save resource " + resource.getResourceCode(), e);
        }
        return null;
    }

    public boolean updateResourceStatus(int resourceId, String newStatus) {
        String sql = "UPDATE recursos SET estado_recurso = ? WHERE id_recurso = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error updating resource status for ID " + resourceId, e);
            return false;
        }
    }

    public Optional<Resource> findById(int id) {
        String sql = "SELECT r.*, c.nombre_categoria FROM recursos r " +
                "JOIN categorias_recurso c ON r.id_categoria = c.id_categoria " +
                "WHERE r.id_recurso = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToResource(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding resource by ID: " + id, e);
        }
        return Optional.empty();
    }

    public List<Resource> findAll() {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT r.*, c.nombre_categoria FROM recursos r " +
                "JOIN categorias_recurso c ON r.id_categoria = c.id_categoria " +
                "ORDER BY r.id_categoria, r.nombre_recurso";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToResource(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error fetching all resources", e);
        }
        return list;
    }

    public List<ResourceCategory> findAllCategories() {
        List<ResourceCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM categorias_recurso ORDER BY id_categoria";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ResourceCategory(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre_categoria"),
                        rs.getString("descripcion")
                ));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error fetching resource categories", e);
        }
        return list;
    }

    public boolean loanToStudent(int resourceId, int studentId, LocalDate loanDate, LocalDate expectedReturn, String obs) {
        String sqlLoan = "INSERT INTO prestamos_estudiante (id_recurso, id_estudiante, fecha_prestamo, fecha_devolucion_esperada, estado_prestamo, observaciones) " +
                "VALUES (?, ?, ?, ?, 'Activo', ?)";
        String sqlUpdateRes = "UPDATE recursos SET estado_recurso = 'Prestado' WHERE id_recurso = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psLoan = conn.prepareStatement(sqlLoan);
                 PreparedStatement psRes = conn.prepareStatement(sqlUpdateRes)) {

                psLoan.setInt(1, resourceId);
                psLoan.setInt(2, studentId);
                psLoan.setDate(3, Date.valueOf(loanDate));
                psLoan.setDate(4, Date.valueOf(expectedReturn));
                psLoan.setString(5, obs);
                psLoan.executeUpdate();

                psRes.setInt(1, resourceId);
                psRes.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                LoggerUtil.error("Transaction rolled back for student loan", ex);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error creating loan to student", e);
            return false;
        }
    }

    public boolean returnStudentLoan(int loanId) {
        String sqlFind = "SELECT id_recurso FROM prestamos_estudiante WHERE id_prestamo = ?";
        String sqlUpdateLoan = "UPDATE prestamos_estudiante SET estado_prestamo = 'Devuelto', fecha_devolucion_real = CURRENT_DATE WHERE id_prestamo = ?";
        String sqlUpdateRes = "UPDATE recursos SET estado_recurso = 'Disponible' WHERE id_recurso = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            int resourceId = 0;
            try (PreparedStatement psFind = conn.prepareStatement(sqlFind)) {
                psFind.setInt(1, loanId);
                try (ResultSet rs = psFind.executeQuery()) {
                    if (rs.next()) {
                        resourceId = rs.getInt("id_recurso");
                    }
                }
            }
            if (resourceId == 0) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement psLoan = conn.prepareStatement(sqlUpdateLoan);
                 PreparedStatement psRes = conn.prepareStatement(sqlUpdateRes)) {

                psLoan.setInt(1, loanId);
                psLoan.executeUpdate();

                psRes.setInt(1, resourceId);
                psRes.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                LoggerUtil.error("Transaction rolled back for returning student loan", ex);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error returning student loan", e);
            return false;
        }
    }

    public boolean loanToTeacher(int resourceId, int teacherId, LocalDate loanDate, LocalDate expectedReturn, String obs) {
        String sqlLoan = "INSERT INTO prestamos_profesor (id_recurso, id_profesor, fecha_prestamo, fecha_devolucion_esperada, estado_prestamo, observaciones) " +
                "VALUES (?, ?, ?, ?, 'Activo', ?)";
        String sqlUpdateRes = "UPDATE recursos SET estado_recurso = 'Prestado' WHERE id_recurso = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psLoan = conn.prepareStatement(sqlLoan);
                 PreparedStatement psRes = conn.prepareStatement(sqlUpdateRes)) {

                psLoan.setInt(1, resourceId);
                psLoan.setInt(2, teacherId);
                psLoan.setDate(3, Date.valueOf(loanDate));
                psLoan.setDate(4, Date.valueOf(expectedReturn));
                psLoan.setString(5, obs);
                psLoan.executeUpdate();

                psRes.setInt(1, resourceId);
                psRes.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                LoggerUtil.error("Transaction rolled back for teacher loan", ex);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error creating loan to teacher", e);
            return false;
        }
    }

    public boolean returnTeacherLoan(int loanId) {
        String sqlFind = "SELECT id_recurso FROM prestamos_profesor WHERE id_prestamo = ?";
        String sqlUpdateLoan = "UPDATE prestamos_profesor SET estado_prestamo = 'Devuelto', fecha_devolucion_real = CURRENT_DATE WHERE id_prestamo = ?";
        String sqlUpdateRes = "UPDATE recursos SET estado_recurso = 'Disponible' WHERE id_recurso = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            int resourceId = 0;
            try (PreparedStatement psFind = conn.prepareStatement(sqlFind)) {
                psFind.setInt(1, loanId);
                try (ResultSet rs = psFind.executeQuery()) {
                    if (rs.next()) {
                        resourceId = rs.getInt("id_recurso");
                    }
                }
            }
            if (resourceId == 0) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement psLoan = conn.prepareStatement(sqlUpdateLoan);
                 PreparedStatement psRes = conn.prepareStatement(sqlUpdateRes)) {

                psLoan.setInt(1, loanId);
                psLoan.executeUpdate();

                psRes.setInt(1, resourceId);
                psRes.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                LoggerUtil.error("Transaction rolled back for returning teacher loan", ex);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error returning teacher loan", e);
            return false;
        }
    }

    public List<StudentLoan> findActiveStudentLoans() {
        List<StudentLoan> list = new ArrayList<>();
        String sql = "SELECT pl.*, r.nombre_recurso, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante " +
                "FROM prestamos_estudiante pl " +
                "JOIN recursos r ON pl.id_recurso = r.id_recurso " +
                "JOIN estudiantes e ON pl.id_estudiante = e.id_estudiante " +
                "WHERE pl.estado_prestamo IN ('Activo', 'Vencido') ORDER BY pl.fecha_devolucion_esperada";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                StudentLoan l = new StudentLoan();
                l.setIdLoan(rs.getInt("id_prestamo"));
                l.setIdResource(rs.getInt("id_recurso"));
                l.setIdStudent(rs.getInt("id_estudiante"));
                Date ld = rs.getDate("fecha_prestamo");
                if (ld != null) l.setLoanDate(ld.toLocalDate());
                Date ed = rs.getDate("fecha_devolucion_esperada");
                if (ed != null) l.setExpectedReturnDate(ed.toLocalDate());
                Date rd = rs.getDate("fecha_devolucion_real");
                if (rd != null) l.setActualReturnDate(rd.toLocalDate());
                l.setStatus(rs.getString("estado_prestamo"));
                l.setObservations(rs.getString("observaciones"));
                l.setResourceName(rs.getString("nombre_recurso"));
                l.setStudentName(rs.getString("nom_estudiante"));
                list.add(l);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding active student loans", e);
        }
        return list;
    }

    public List<TeacherLoan> findActiveTeacherLoans() {
        List<TeacherLoan> list = new ArrayList<>();
        String sql = "SELECT pl.*, r.nombre_recurso, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_profesor " +
                "FROM prestamos_profesor pl " +
                "JOIN recursos r ON pl.id_recurso = r.id_recurso " +
                "JOIN profesores p ON pl.id_profesor = p.id_profesor " +
                "WHERE pl.estado_prestamo IN ('Activo', 'Vencido') ORDER BY pl.fecha_devolucion_esperada";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TeacherLoan l = new TeacherLoan();
                l.setIdLoan(rs.getInt("id_prestamo"));
                l.setIdResource(rs.getInt("id_recurso"));
                l.setIdTeacher(rs.getInt("id_profesor"));
                Date ld = rs.getDate("fecha_prestamo");
                if (ld != null) l.setLoanDate(ld.toLocalDate());
                Date ed = rs.getDate("fecha_devolucion_esperada");
                if (ed != null) l.setExpectedReturnDate(ed.toLocalDate());
                Date rd = rs.getDate("fecha_devolucion_real");
                if (rd != null) l.setActualReturnDate(rd.toLocalDate());
                l.setStatus(rs.getString("estado_prestamo"));
                l.setObservations(rs.getString("observaciones"));
                l.setResourceName(rs.getString("nombre_recurso"));
                l.setTeacherName(rs.getString("nom_profesor"));
                list.add(l);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding active teacher loans", e);
        }
        return list;
    }

    private Resource mapResultSetToResource(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setIdResource(rs.getInt("id_recurso"));
        r.setResourceCode(rs.getString("codigo_recurso"));
        r.setResourceName(rs.getString("nombre_recurso"));
        r.setIdCategory(rs.getInt("id_categoria"));
        r.setDescription(rs.getString("descripcion"));
        Date d = rs.getDate("fecha_adquisicion");
        if (d != null) r.setAcquisitionDate(d.toLocalDate());
        r.setPhysicalLocation(rs.getString("ubicacion_fisica"));
        r.setStatus(rs.getString("estado_recurso"));
        r.setCategoryName(rs.getString("nombre_categoria"));
        return r;
    }
}
