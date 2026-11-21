package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.academic.AcademicRecord;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.academic.Subject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface IAcademicView {
    void showMenu();
    void showRecordGradeHeader();
    void showAcademicHistoryHeader();
    void showAcademicHistory(String studentName, List<AcademicRecord> history);
    void showStudentPeriodAverage(String studentName, int year, String period, BigDecimal average);
    void showCourseSubjectAverage(int subjectId, int year, String period, BigDecimal courseAvg);
    void showRecordAttendanceHeader();
    void showAttendanceStates();
    void showExcessiveAbsencesReport(Map<Integer, Long> absences, Function<Integer, Optional<Student>> studentResolver);
    void showEnrollSubjectHeader();
    void showSubjects(List<Subject> subjects);
    void showAttendanceReport(String reportText, String savedFilePath);
    void showSuccess(String message);
    void showError(String message);
    void showWarning(String message);
}
