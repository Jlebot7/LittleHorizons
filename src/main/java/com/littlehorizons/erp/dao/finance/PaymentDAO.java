package com.littlehorizons.erp.dao.finance;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;
import com.littlehorizons.erp.util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDAO {

    public Integer savePayment(Payment payment) {
        String sql = "INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, " +
                "metodo_pago, numero_recibo, año_academico, periodo_academico, notas) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, payment.getIdStudent());
            if (payment.getIdGuardian() != null) {
                ps.setInt(2, payment.getIdGuardian());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, payment.getIdConcept());
            ps.setBigDecimal(4, payment.getAmountPaid());
            ps.setDate(5, Date.valueOf(payment.getPaymentDate()));
            ps.setString(6, payment.getPaymentMethod());
            ps.setString(7, payment.getReceiptNumber());
            if (payment.getAcademicYear() != null) {
                ps.setInt(8, payment.getAcademicYear());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setString(9, payment.getAcademicPeriod());
            ps.setString(10, payment.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        payment.setIdPago(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Failed to save payment for student " + payment.getIdStudent(), e);
        }
        return null;
    }

    public Optional<Payment> findPaymentById(int id) {
        String sql = "SELECT p.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acudiente, c.nombre_concepto " +
                "FROM pagos p " +
                "JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente " +
                "JOIN conceptos_pago c ON p.id_concepto = c.id_concepto " +
                "WHERE p.id_pago = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding payment by ID: " + id, e);
        }
        return Optional.empty();
    }

    public List<Payment> findPaymentsByStudent(int studentId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acudiente, c.nombre_concepto " +
                "FROM pagos p " +
                "JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente " +
                "JOIN conceptos_pago c ON p.id_concepto = c.id_concepto " +
                "WHERE p.id_estudiante = ? ORDER BY p.fecha_pago DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding payments for student " + studentId, e);
        }
        return list;
    }

    public List<Payment> findPaymentsByGuardian(int guardianId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acudiente, c.nombre_concepto " +
                "FROM pagos p " +
                "JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente " +
                "JOIN conceptos_pago c ON p.id_concepto = c.id_concepto " +
                "WHERE p.id_acudiente = ? ORDER BY p.fecha_pago DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, guardianId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding payments for guardian " + guardianId, e);
        }
        return list;
    }

    public List<Payment> findAllPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acudiente, c.nombre_concepto " +
                "FROM pagos p " +
                "JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente " +
                "JOIN conceptos_pago c ON p.id_concepto = c.id_concepto " +
                "ORDER BY p.id_pago DESC LIMIT 150";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToPayment(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding all payments", e);
        }
        return list;
    }

    public List<PaymentConcept> findAllConcepts() {
        List<PaymentConcept> list = new ArrayList<>();
        String sql = "SELECT * FROM conceptos_pago ORDER BY id_concepto";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new PaymentConcept(
                        rs.getInt("id_concepto"),
                        rs.getString("codigo_concepto"),
                        rs.getString("nombre_concepto"),
                        rs.getString("descripcion"),
                        rs.getBigDecimal("monto_base"),
                        rs.getBoolean("es_recurrente"),
                        rs.getBoolean("esta_activo")
                ));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error fetching payment concepts", e);
        }
        return list;
    }

    public Optional<PaymentConcept> findConceptById(int id) {
        String sql = "SELECT * FROM conceptos_pago WHERE id_concepto = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new PaymentConcept(
                            rs.getInt("id_concepto"),
                            rs.getString("codigo_concepto"),
                            rs.getString("nombre_concepto"),
                            rs.getString("descripcion"),
                            rs.getBigDecimal("monto_base"),
                            rs.getBoolean("es_recurrente"),
                            rs.getBoolean("esta_activo")
                    ));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error finding concept by ID: " + id, e);
        }
        return Optional.empty();
    }

    public boolean saveInvoice(Invoice invoice) {
        String sql = "INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe, nombre_archivo_pdf, ruta_archivo) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, invoice.getInvoiceNumber());
            ps.setInt(2, invoice.getIdPayment());
            ps.setDate(3, Date.valueOf(invoice.getIssueDate()));
            ps.setString(4, invoice.getCufe());
            ps.setString(5, invoice.getPdfFileName());
            ps.setString(6, invoice.getFilePath());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        invoice.setIdInvoice(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error saving invoice " + invoice.getInvoiceNumber(), e);
        }
        return false;
    }

    public List<Invoice> findAllInvoices() {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT f.*, p.monto_pagado, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_estudiante, " +
                "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acudiente, c.nombre_concepto " +
                "FROM facturas f " +
                "JOIN pagos p ON f.id_pago = p.id_pago " +
                "JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente " +
                "JOIN conceptos_pago c ON p.id_concepto = c.id_concepto " +
                "ORDER BY f.id_factura DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Invoice inv = new Invoice();
                inv.setIdInvoice(rs.getInt("id_factura"));
                inv.setInvoiceNumber(rs.getString("numero_factura"));
                inv.setIdPayment(rs.getInt("id_pago"));
                Date d = rs.getDate("fecha_emision");
                if (d != null) inv.setIssueDate(d.toLocalDate());
                inv.setCufe(rs.getString("cufe"));
                inv.setPdfFileName(rs.getString("nombre_archivo_pdf"));
                inv.setFilePath(rs.getString("ruta_archivo"));
                inv.setAmount(rs.getBigDecimal("monto_pagado"));
                inv.setStudentName(rs.getString("nom_estudiante"));
                inv.setGuardianName(rs.getString("nom_acudiente"));
                inv.setConceptName(rs.getString("nombre_concepto"));
                list.add(inv);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error fetching all invoices", e);
        }
        return list;
    }

    public int getNextInvoiceSequence() {
        String sql = "SELECT COUNT(*) FROM facturas";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error reading invoice sequence", e);
        }
        return 1;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setIdPago(rs.getInt("id_pago"));
        p.setIdStudent(rs.getInt("id_estudiante"));
        p.setIdGuardian(rs.getInt("id_acudiente"));
        p.setIdConcept(rs.getInt("id_concepto"));
        p.setAmountPaid(rs.getBigDecimal("monto_pagado"));
        Date d = rs.getDate("fecha_pago");
        if (d != null) p.setPaymentDate(d.toLocalDate());
        p.setPaymentMethod(rs.getString("metodo_pago"));
        p.setReceiptNumber(rs.getString("numero_recibo"));
        p.setAcademicYear(rs.getInt("año_academico"));
        p.setAcademicPeriod(rs.getString("periodo_academico"));
        p.setNotes(rs.getString("notas"));
        p.setStudentName(rs.getString("nom_estudiante"));
        p.setGuardianName(rs.getString("nom_acudiente"));
        p.setConceptName(rs.getString("nombre_concepto"));
        return p;
    }
}
