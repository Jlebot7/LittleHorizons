package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.academic.AcademicGrade;
import com.littlehorizons.erp.model.academic.Student;

import java.util.List;

public interface IStudentView {
    void showMenu();
    void showRegisterStudentHeader();
    void showStudents(List<Student> students);
    void showSearchStudentHeader();
    void showStudentDetails(Student student);
    void showStudentNotFound(String docNumber);
    void showRegisterGuardianHeader();
    void showLinkGuardianPrompt(String studentName);
    void showEnrollGradeHeader();
    void showAvailableGrades(List<AcademicGrade> grades);
    void showSuccess(String message);
    void showError(String message);
    void showWarning(String message);
}
