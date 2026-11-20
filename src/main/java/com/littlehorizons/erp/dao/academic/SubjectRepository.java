package com.littlehorizons.erp.dao.academic;

import com.littlehorizons.erp.model.academic.AcademicGrade;
import com.littlehorizons.erp.model.academic.Subject;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository {
    Integer save(Subject subject);
    Optional<Subject> findById(int id);
    List<Subject> findAll();
    List<Subject> findByGrade(int gradeId);
    List<AcademicGrade> findAllGrades();
    boolean enrollStudent(int studentId, int subjectId, int year, String period, String section);
}
