package com.littlehorizons.erp.service;

import com.littlehorizons.erp.model.academic.*;

import java.math.BigDecimal;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IAcademicService {
    Integer registerStudent(Student student);
    boolean updateStudent(Student student);
    Optional<Student> getStudentById(int id);
    Optional<Student> getStudentByDocument(String docNumber);
    List<Student> getAllStudents();
    Integer registerGuardian(Guardian guardian);
    List<Guardian> getAllGuardians();
    boolean linkStudentToGuardian(int studentId, int guardianId, boolean isPrimary, boolean isEmergency);
    boolean enrollStudentInGrade(int studentId, int gradeId, int year);
    Integer registerTeacher(Teacher teacher);
    List<Teacher> getAllTeachers();
    Integer registerSubject(Subject subject);
    List<Subject> getAllSubjects();
    List<Subject> getSubjectsByGrade(int gradeId);
    List<AcademicGrade> getAllGrades();
    boolean assignTeacherToSubject(int teacherId, int subjectId, int year, String section);
    boolean enrollStudentInSubject(int studentId, int subjectId, int year, String period, String section);
    boolean recordGrade(AcademicRecord record);
    List<AcademicRecord> getAcademicHistory(int studentId);
    BigDecimal calculateStudentPeriodAverage(int studentId, int year, String period);
    BigDecimal calculateSubjectCourseAverage(int subjectId, int year, String period);
    boolean registerAttendance(Attendance attendance);
    List<Attendance> getStudentAttendance(int studentId);
    Map<Integer, Long> getStudentsWithExcessiveAbsences(long threshold);
    List<Student> getHonorRoll(int year, String period, double minAverage);
    Map<String, Long> getGradeDistribution(int subjectId, int year, String period);
    DoubleSummaryStatistics getSubjectPerformanceStatistics(int subjectId, int year, String period);
    Map<Integer, Long> getTeacherAssignedSubjectsCount(int year);
    Map<Boolean, List<Student>> partitionStudentsByPerformance(int year, String period, BigDecimal threshold);
}
