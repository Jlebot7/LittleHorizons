package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.resource.ResourceDAO;
import com.littlehorizons.erp.dao.resource.ResourceRepository;
import com.littlehorizons.erp.model.resource.Resource;
import com.littlehorizons.erp.model.resource.ResourceCategory;
import com.littlehorizons.erp.model.resource.StudentLoan;
import com.littlehorizons.erp.model.resource.TeacherLoan;
import com.littlehorizons.erp.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceService implements IResourceService {

    private final ResourceRepository resourceDAO;

    public ResourceService() {
        this(new ResourceDAO());
    }

    public ResourceService(ResourceRepository resourceDAO) {
        this.resourceDAO = resourceDAO;
    }

    @Override
    public Integer registerResource(Resource resource) {
        if (!ValidationUtil.isNotEmpty(resource.getResourceCode()) ||
                !ValidationUtil.isNotEmpty(resource.getResourceName())) {
            throw new IllegalArgumentException("Código y nombre del recurso son obligatorios.");
        }
        return resourceDAO.saveResource(resource);
    }

    @Override
    public List<Resource> getAllResources() {
        return resourceDAO.findAll();
    }

    @Override
    public List<ResourceCategory> getAllCategories() {
        return resourceDAO.findAllCategories();
    }

    @Override
    public Optional<Resource> getResourceById(int id) {
        return resourceDAO.findById(id);
    }

    @Override
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

    @Override
    public boolean returnStudentLoan(int loanId) {
        return resourceDAO.returnStudentLoan(loanId);
    }

    @Override
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

    @Override
    public boolean returnTeacherLoan(int loanId) {
        return resourceDAO.returnTeacherLoan(loanId);
    }

    @Override
    public List<StudentLoan> getActiveStudentLoans() {
        return resourceDAO.findActiveStudentLoans();
    }

    @Override
    public List<TeacherLoan> getActiveTeacherLoans() {
        return resourceDAO.findActiveTeacherLoans();
    }

    @Override
    public List<StudentLoan> getOverdueStudentLoans() {
        LocalDate today = LocalDate.now();
        return resourceDAO.findActiveStudentLoans().stream()
                .filter(l -> l.getExpectedReturnDate() != null && l.getExpectedReturnDate().isBefore(today))
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherLoan> getOverdueTeacherLoans() {
        LocalDate today = LocalDate.now();
        return resourceDAO.findActiveTeacherLoans().stream()
                .filter(l -> l.getExpectedReturnDate() != null && l.getExpectedReturnDate().isBefore(today))
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, Long> getResourceCountByCategory() {
        return getAllResources().stream()
                .filter(r -> r.getIdCategory() != null)
                .collect(Collectors.groupingBy(Resource::getIdCategory, Collectors.counting()));
    }

    @Override
    public Map<String, Long> getResourceCountByStatus() {
        return getAllResources().stream()
                .filter(r -> r.getStatus() != null)
                .collect(Collectors.groupingBy(Resource::getStatus, Collectors.counting()));
    }

    @Override
    public Map<Boolean, List<StudentLoan>> partitionStudentLoansByOverdue() {
        LocalDate today = LocalDate.now();
        return getActiveStudentLoans().stream()
                .collect(Collectors.partitioningBy(l -> l.getExpectedReturnDate() != null && l.getExpectedReturnDate().isBefore(today)));
    }

    @Override
    public Map<Boolean, List<TeacherLoan>> partitionTeacherLoansByOverdue() {
        LocalDate today = LocalDate.now();
        return getActiveTeacherLoans().stream()
                .collect(Collectors.partitioningBy(l -> l.getExpectedReturnDate() != null && l.getExpectedReturnDate().isBefore(today)));
    }

    @Override
    public List<Resource> searchResourcesByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllResources();
        }
        String lower = keyword.toLowerCase();
        return getAllResources().stream()
                .filter(r -> (r.getResourceName() != null && r.getResourceName().toLowerCase().contains(lower)) ||
                        (r.getResourceCode() != null && r.getResourceCode().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
    }
}
