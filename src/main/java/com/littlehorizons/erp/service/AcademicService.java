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

    private void validateStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Los datos del estudiante no pueden ser nulos.");
        }
        if (!ValidationUtil.isValidPersonName(student.getFirstName())) {
            throw new IllegalArgumentException("El primer nombre del estudiante es obligatorio y no puede contener números.");
        }
        if (!ValidationUtil.isValidPersonName(student.getFirstLastName())) {
            throw new IllegalArgumentException("El primer apellido del estudiante es obligatorio y no puede contener números.");
        }
        if (ValidationUtil.isNotEmpty(student.getSecondName()) && !ValidationUtil.isValidPersonName(student.getSecondName())) {
            throw new IllegalArgumentException("El segundo nombre del estudiante no puede contener números ni caracteres inválidos.");
        }
        if (ValidationUtil.isNotEmpty(student.getSecondLastName()) && !ValidationUtil.isValidPersonName(student.getSecondLastName())) {
            throw new IllegalArgumentException("El segundo apellido del estudiante no puede contener números ni caracteres inválidos.");
        }
        if (!ValidationUtil.isValidDocumentType(student.getDocumentType(), true)) {
            throw new IllegalArgumentException("Tipo de documento no válido para estudiante. Tipos admitidos: " + ValidationUtil.getStudentDocumentTypes());
        }
        if (!ValidationUtil.isValidDocumentNumber(student.getDocumentType(), student.getDocumentNumber())) {
            throw new IllegalArgumentException("El número de documento no es válido para el tipo: " + student.getDocumentType());
        }
        if (student.getBirthDate() != null && !ValidationUtil.isValidStudentBirthDate(student.getBirthDate())) {
            throw new IllegalArgumentException("La fecha de nacimiento debe corresponder a un estudiante entre 3 y 25 años.");
        }
        if (ValidationUtil.isNotEmpty(student.getEmail()) && !ValidationUtil.isValidEmail(student.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico del estudiante no tiene un formato válido.");
        }
        if (ValidationUtil.isNotEmpty(student.getPhone()) && !ValidationUtil.isValidPhone(student.getPhone())) {
            throw new IllegalArgumentException("El teléfono del estudiante no tiene un formato numérico válido.");
        }
        if (ValidationUtil.isNotEmpty(student.getBloodType()) && !ValidationUtil.isValidBloodType(student.getBloodType())) {
            throw new IllegalArgumentException("El tipo de sangre no es válido (ej: O+, O-, A+, B+, AB+).");
        }
    }

    private void validateGuardian(Guardian guardian) {
        if (guardian == null) {
            throw new IllegalArgumentException("Los datos del acudiente no pueden ser nulos.");
        }
        if (!ValidationUtil.isValidPersonName(guardian.getFirstName())) {
            throw new IllegalArgumentException("El primer nombre del acudiente es obligatorio y no puede contener números.");
        }
        if (!ValidationUtil.isValidPersonName(guardian.getFirstLastName())) {
            throw new IllegalArgumentException("El primer apellido del acudiente es obligatorio y no puede contener números.");
        }
        if (ValidationUtil.isNotEmpty(guardian.getSecondName()) && !ValidationUtil.isValidPersonName(guardian.getSecondName())) {
            throw new IllegalArgumentException("El segundo nombre del acudiente no puede contener números ni caracteres inválidos.");
        }
        if (ValidationUtil.isNotEmpty(guardian.getSecondLastName()) && !ValidationUtil.isValidPersonName(guardian.getSecondLastName())) {
            throw new IllegalArgumentException("El segundo apellido del acudiente no puede contener números ni caracteres inválidos.");
        }
        if (!ValidationUtil.isValidDocumentType(guardian.getDocumentType(), false)) {
            throw new IllegalArgumentException("Tipo de documento no válido para acudiente. Tipos admitidos: " + ValidationUtil.getAdultDocumentTypes());
        }
        if (!ValidationUtil.isValidDocumentNumber(guardian.getDocumentType(), guardian.getDocumentNumber())) {
            throw new IllegalArgumentException("El número de documento no es válido para el tipo: " + guardian.getDocumentType());
        }
        if (!ValidationUtil.isValidPhone(guardian.getMainPhone())) {
            throw new IllegalArgumentException("El teléfono principal del acudiente es obligatorio y debe ser numérico.");
        }
        if (ValidationUtil.isNotEmpty(guardian.getSecondaryPhone()) && !ValidationUtil.isValidPhone(guardian.getSecondaryPhone())) {
            throw new IllegalArgumentException("El teléfono secundario del acudiente debe ser numérico.");
        }
        if (ValidationUtil.isNotEmpty(guardian.getEmail()) && !ValidationUtil.isValidEmail(guardian.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico del acudiente no tiene un formato válido.");
        }
    }

    private void validateTeacher(Teacher teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("Los datos del profesor no pueden ser nulos.");
        }
        if (!ValidationUtil.isValidPersonName(teacher.getFirstName())) {
            throw new IllegalArgumentException("El primer nombre del profesor es obligatorio y no puede contener números.");
        }
        if (!ValidationUtil.isValidPersonName(teacher.getFirstLastName())) {
            throw new IllegalArgumentException("El primer apellido del profesor es obligatorio y no puede contener números.");
        }
        if (ValidationUtil.isNotEmpty(teacher.getSecondName()) && !ValidationUtil.isValidPersonName(teacher.getSecondName())) {
            throw new IllegalArgumentException("El segundo nombre del profesor no puede contener números ni caracteres inválidos.");
        }
        if (ValidationUtil.isNotEmpty(teacher.getSecondLastName()) && !ValidationUtil.isValidPersonName(teacher.getSecondLastName())) {
            throw new IllegalArgumentException("El segundo apellido del profesor no puede contener números ni caracteres inválidos.");
        }
        if (!ValidationUtil.isValidDocumentType(teacher.getDocumentType(), false)) {
            throw new IllegalArgumentException("Tipo de documento no válido para profesor. Tipos admitidos: " + ValidationUtil.getAdultDocumentTypes());
        }
        if (!ValidationUtil.isValidDocumentNumber(teacher.getDocumentType(), teacher.getDocumentNumber())) {
            throw new IllegalArgumentException("El número de documento no es válido para el tipo: " + teacher.getDocumentType());
        }
        if (!ValidationUtil.isValidEmail(teacher.getEmail())) {
            throw new IllegalArgumentException("El correo institucional del profesor es obligatorio y debe tener formato válido.");
        }
        if (ValidationUtil.isNotEmpty(teacher.getPhone()) && !ValidationUtil.isValidPhone(teacher.getPhone())) {
            throw new IllegalArgumentException("El teléfono del profesor debe ser numérico.");
        }
    }

    public Integer registerStudent(Student student) {
        validateStudent(student);
        return studentDAO.save(student);
    }

    public boolean updateStudent(Student student) {
        validateStudent(student);
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
        validateGuardian(guardian);
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

    public Integer registerTeacher(Teacher teacher) {
        validateTeacher(teacher);
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

    public boolean recordGrade(AcademicRecord record) {
        if (!ValidationUtil.isValidGrade(record.getGradeValue())) {
            throw new IllegalArgumentException("La nota debe estar entre 0.0 y 5.0 según el estándar colombiano.");
        }
        return academicRecordDAO.saveOrUpdate(record);
    }

    public List<AcademicRecord> getAcademicHistory(int studentId) {
        return academicRecordDAO.findByStudent(studentId);
    }

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

    public boolean registerAttendance(Attendance attendance) {
        return attendanceDAO.recordAttendance(attendance);
    }

    public List<Attendance> getStudentAttendance(int studentId) {
        return attendanceDAO.findByStudent(studentId);
    }

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
