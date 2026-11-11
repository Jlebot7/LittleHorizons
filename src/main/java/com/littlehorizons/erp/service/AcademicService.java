package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.academic.*;
import com.littlehorizons.erp.model.academic.*;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.ValidationUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service encapsulating business logic for academic management,
 * grade calculations using Stream API, and attendance tracking.
 */
public class AcademicService {

    private final StudentDAO studentDAO;
    private final GuardianDAO guardianDAO;
    private final TeacherDAO teacherDAO;
    private final SubjectDAO subjectDAO;
    private final AcademicRecordDAO academicRecordDAO;
    private final AttendanceDAO attendanceDAO;

    public AcademicService() {
        this.studentDAO = new StudentDAO();
        this.guardianDAO = new GuardianDAO();
        this.teacherDAO = new TeacherDAO();
        this.subjectDAO = new SubjectDAO();
        this.academicRecordDAO = new AcademicRecordDAO();
        this.attendanceDAO = new AttendanceDAO();
    }

    // --- Student & Guardian Operations ---

    public Integer registerStudent(Student student) {
        if (!ValidationUtil.isNotEmpty(student.getFirstName()) ||
                !ValidationUtil.isNotEmpty(student.getFirstLastName()) ||
                !ValidationUtil.isValidDocumentNumber(student.getDocumentNumber())) {
            throw new IllegalArgumentException("Nombre y número de documento son obligatorios y deben ser válidos.");
        }
        return studentDAO.save(student);
    }

    public boolean updateStudent(Student student) {
        return studentDAO.update(student);
    }

    public Optional<Student> getStudentById(int id) {
        return studentDAO.findById(id);
    }

    public Optional<Student> getStudentByDocument(String docNumber) {
        return studentDAO.findByDocument(docNumber);
    }

    public List<Student> getAllStudents() {
        return studentDAO.findAll();
    }

    public Integer registerGuardian(Guardian guardian) {
        if (!ValidationUtil.isNotEmpty(guardian.getFirstName()) ||
                !ValidationUtil.isNotEmpty(guardian.getFirstLastName()) ||
                !ValidationUtil.isValidDocumentNumber(guardian.getDocumentNumber())) {
            throw new IllegalArgumentException("Nombre y documento del acudiente son requeridos.");
        }
        return guardianDAO.save(guardian);
    }

    public List<Guardian> getAllGuardians() {
        return guardianDAO.findAll();
    }

    public boolean linkStudentToGuardian(int studentId, int guardianId, boolean isPrimary, boolean isEmergency) {
        return studentDAO.associateGuardian(studentId, guardianId, isPrimary, isEmergency);
    }

    public boolean enrollStudentInGrade(int studentId, int gradeId, int year) {
        return studentDAO.enrollInGrade(studentId, gradeId, year);
    }

    // --- Teacher & Subject Operations ---

    public Integer registerTeacher(Teacher teacher) {
        if (!ValidationUtil.isNotEmpty(teacher.getFirstName()) ||
                !ValidationUtil.isValidEmail(teacher.getEmail())) {
            throw new IllegalArgumentException("Nombre y correo válido del profesor son requeridos.");
        }
        return teacherDAO.save(teacher);
    }

    public List<Teacher> getAllTeachers() {
        return teacherDAO.findAll();
    }

    public Integer registerSubject(Subject subject) {
        return subjectDAO.save(subject);
    }

    public List<Subject> getAllSubjects() {
        return subjectDAO.findAll();
    }

    public List<Subject> getSubjectsByGrade(int gradeId) {
        return subjectDAO.findByGrade(gradeId);
    }

    public List<AcademicGrade> getAllGrades() {
        return subjectDAO.findAllGrades();
    }

    public boolean assignTeacherToSubject(int teacherId, int subjectId, int year, String section) {
        return teacherDAO.assignToSubject(teacherId, subjectId, year, section);
    }

    public boolean enrollStudentInSubject(int studentId, int subjectId, int year, String period, String section) {
        return subjectDAO.enrollStudent(studentId, subjectId, year, period, section);
    }

    // --- Grades & Calculation Logic with Java Stream API ---

    public boolean recordGrade(AcademicRecord record) {
        if (!ValidationUtil.isValidGrade(record.getGradeValue())) {
            throw new IllegalArgumentException("La nota debe estar entre 0.0 y 5.0 según el estándar colombiano.");
        }
        return academicRecordDAO.saveOrUpdate(record);
    }

    public List<AcademicRecord> getAcademicHistory(int studentId) {
        return academicRecordDAO.findByStudent(studentId);
    }

    /**
     * Calculates the overall GPA (Grade Point Average) for a student across all subjects
     * in a given period using Java Stream API.
     *
     * @param studentId Student ID
     * @param year Academic year
     * @param period Academic period (I, II, III, IV)
     * @return Calculated average BigDecimal (scale 2), or 0.00 if no records exist
     */
    public BigDecimal calculateStudentPeriodAverage(int studentId, int year, String period) {
        List<AcademicRecord> records = academicRecordDAO.findByStudent(studentId);

        return records.stream()
                .filter(r -> r.getAcademicYear() == year && r.getAcademicPeriod().equalsIgnoreCase(period))
                .map(AcademicRecord::getGradeValue)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .mapToDouble(Double::doubleValue)
                .average()
                .stream()
                .mapToObj(avg -> BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP))
                .findFirst()
                .orElse(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Calculates the course/class average for a specific subject in a period using Stream API.
     *
     * @param subjectId Subject ID
     * @param year Academic year
     * @param period Academic period
     * @return Subject course average
     */
    public BigDecimal calculateSubjectCourseAverage(int subjectId, int year, String period) {
        List<AcademicRecord> records = academicRecordDAO.findBySubjectAndPeriod(subjectId, year, period);

        return records.stream()
                .map(AcademicRecord::getGradeValue)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .mapToDouble(Double::doubleValue)
                .average()
                .stream()
                .mapToObj(avg -> BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP))
                .findFirst()
                .orElse(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    // --- Attendance Operations ---

    public boolean registerAttendance(Attendance attendance) {
        return attendanceDAO.recordAttendance(attendance);
    }

    public List<Attendance> getStudentAttendance(int studentId) {
        return attendanceDAO.findByStudent(studentId);
    }

    /**
     * Generates a report of students with excessive absences (>3 absences in recorded attendance)
     * using Java Streams and collectors.
     */
    public Map<Integer, Long> getStudentsWithExcessiveAbsences(long threshold) {
        List<Attendance> allRecent = attendanceDAO.findAllRecent();

        return allRecent.stream()
                .filter(a -> "Ausente".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.groupingBy(Attendance::getIdStudent, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
