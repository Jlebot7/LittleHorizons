package com.littlehorizons.erp.service;

import com.littlehorizons.erp.model.resource.Resource;
import com.littlehorizons.erp.model.resource.ResourceCategory;
import com.littlehorizons.erp.model.resource.StudentLoan;
import com.littlehorizons.erp.model.resource.TeacherLoan;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IResourceService {
    Integer registerResource(Resource resource);
    List<Resource> getAllResources();
    List<ResourceCategory> getAllCategories();
    Optional<Resource> getResourceById(int id);
    boolean loanToStudent(int resourceId, int studentId, LocalDate loanDate, LocalDate returnExpected, String obs);
    boolean returnStudentLoan(int loanId);
    boolean loanToTeacher(int resourceId, int teacherId, LocalDate loanDate, LocalDate returnExpected, String obs);
    boolean returnTeacherLoan(int loanId);
    List<StudentLoan> getActiveStudentLoans();
    List<TeacherLoan> getActiveTeacherLoans();
    List<StudentLoan> getOverdueStudentLoans();
    List<TeacherLoan> getOverdueTeacherLoans();
    Map<Integer, Long> getResourceCountByCategory();
    Map<String, Long> getResourceCountByStatus();
    Map<Boolean, List<StudentLoan>> partitionStudentLoansByOverdue();
    Map<Boolean, List<TeacherLoan>> partitionTeacherLoansByOverdue();
    List<Resource> searchResourcesByKeyword(String keyword);
}
