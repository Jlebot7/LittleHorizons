package com.littlehorizons.erp.dao.resource;

import com.littlehorizons.erp.model.resource.Resource;
import com.littlehorizons.erp.model.resource.ResourceCategory;
import com.littlehorizons.erp.model.resource.StudentLoan;
import com.littlehorizons.erp.model.resource.TeacherLoan;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ResourceRepository {
    Integer saveResource(Resource resource);
    List<Resource> findAll();
    List<ResourceCategory> findAllCategories();
    Optional<Resource> findById(int id);
    boolean loanToStudent(int resourceId, int studentId, LocalDate loanDate, LocalDate returnExpected, String obs);
    boolean returnStudentLoan(int loanId);
    boolean loanToTeacher(int resourceId, int teacherId, LocalDate loanDate, LocalDate returnExpected, String obs);
    boolean returnTeacherLoan(int loanId);
    List<StudentLoan> findActiveStudentLoans();
    List<TeacherLoan> findActiveTeacherLoans();
}
