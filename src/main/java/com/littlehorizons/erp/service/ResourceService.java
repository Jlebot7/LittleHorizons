package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.resource.ResourceDAO;
import com.littlehorizons.erp.model.resource.Resource;
import com.littlehorizons.erp.model.resource.ResourceCategory;
import com.littlehorizons.erp.model.resource.StudentLoan;
import com.littlehorizons.erp.model.resource.TeacherLoan;
import com.littlehorizons.erp.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service orchestrating Library and Laboratory Resources,
 * Student & Teacher Loans, and Overdue Loan tracking.
 */
public class ResourceService {

    private final ResourceDAO resourceDAO;

    public ResourceService() {
        this.resourceDAO = new ResourceDAO();
    }

    public Integer registerResource(Resource resource) {
        if (!ValidationUtil.isNotEmpty(resource.getResourceCode()) ||
                !ValidationUtil.isNotEmpty(resource.getResourceName())) {
            throw new IllegalArgumentException("Código y nombre del recurso son obligatorios.");
        }
        return resourceDAO.saveResource(resource);
    }

    public List<Resource> getAllResources() {
        return resourceDAO.findAll();
    }

    public List<ResourceCategory> getAllCategories() {
        return resourceDAO.findAllCategories();
    }

    public Optional<Resource> getResourceById(int id) {
        return resourceDAO.findById(id);
    }

    /**
     * Loans a resource to a student, verifying that the resource is currently Available.
     */
    public boolean loanToStudent(int resourceId, int studentId, LocalDate loanDate, LocalDate returnExpected, String obs) {
        Optional<Resource> resourceOpt = resourceDAO.findById(resourceId);
        if (resourceOpt.isEmpty()) {
            throw new IllegalArgumentException("El recurso solicitado no existe.");
        }

        Resource r = resourceOpt.get();
        if (!"Disponible".equalsIgnoreCase(r.getStatus())) {
            throw new IllegalStateException(String.format("El recurso '%s' no está disponible para préstamo (Estado actual: %s).",
                    r.getResourceName(), r.getStatus()));
        }

        return resourceDAO.loanToStudent(resourceId, studentId, loanDate, returnExpected, obs);
    }

    public boolean returnStudentLoan(int loanId) {
        return resourceDAO.returnStudentLoan(loanId);
    }

    /**
     * Loans a resource to a teacher, verifying that the resource is currently Available.
     */
    public boolean loanToTeacher(int resourceId, int teacherId, LocalDate loanDate, LocalDate returnExpected, String obs) {
        Optional<Resource> resourceOpt = resourceDAO.findById(resourceId);
        if (resourceOpt.isEmpty()) {
            throw new IllegalArgumentException("El recurso solicitado no existe.");
        }

        Resource r = resourceOpt.get();
        if (!"Disponible".equalsIgnoreCase(r.getStatus())) {
            throw new IllegalStateException(String.format("El recurso '%s' no está disponible para préstamo (Estado actual: %s).",
                    r.getResourceName(), r.getStatus()));
        }

        return resourceDAO.loanToTeacher(resourceId, teacherId, loanDate, returnExpected, obs);
    }

    public boolean returnTeacherLoan(int loanId) {
        return resourceDAO.returnTeacherLoan(loanId);
    }

    public List<StudentLoan> getActiveStudentLoans() {
        return resourceDAO.findActiveStudentLoans();
    }

    public List<TeacherLoan> getActiveTeacherLoans() {
        return resourceDAO.findActiveTeacherLoans();
    }

    /**
     * Finds overdue loans to students using Java Stream API.
     */
    public List<StudentLoan> getOverdueStudentLoans() {
        LocalDate today = LocalDate.now();
        return resourceDAO.findActiveStudentLoans().stream()
                .filter(l -> l.getExpectedReturnDate() != null && l.getExpectedReturnDate().isBefore(today))
                .collect(Collectors.toList());
    }

    /**
     * Finds overdue loans to teachers using Java Stream API.
     */
    public List<TeacherLoan> getOverdueTeacherLoans() {
        LocalDate today = LocalDate.now();
        return resourceDAO.findActiveTeacherLoans().stream()
                .filter(l -> l.getExpectedReturnDate() != null && l.getExpectedReturnDate().isBefore(today))
                .collect(Collectors.toList());
    }
}
