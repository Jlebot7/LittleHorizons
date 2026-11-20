package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.academic.Teacher;

import java.util.List;

public interface ITeacherView {
    void showMenu();
    void showRegisterTeacherHeader();
    void showTeachers(List<Teacher> teachers);
    void showAssignSubjectHeader();
    void showSuccess(String message);
    void showError(String message);
    void showWarning(String message);
}
