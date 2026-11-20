package com.littlehorizons.erp.dao.academic;

import com.littlehorizons.erp.model.academic.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    Integer save(Student student);
    boolean update(Student student);
    Optional<Student> findById(int id);
    Optional<Student> findByDocument(String docNumber);
    List<Student> findAll();
    boolean associateGuardian(int studentId, int guardianId, boolean isPrimary, boolean isEmergency);
    boolean enrollInGrade(int studentId, int gradeId, int year);
}
