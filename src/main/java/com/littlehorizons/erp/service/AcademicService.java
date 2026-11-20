package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.academic.*;
import com.littlehorizons.erp.model.academic.*;
import com.littlehorizons.erp.util.ValidationUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class AcademicService implements IAcademicService {

    private final StudentRepository studentDAO;
    private final GuardianRepository guardianDAO;
    private final TeacherRepository teacherDAO;
    private final SubjectRepository subjectDAO;
    private final AcademicRecordRepository academicRecordDAO;
    private final AttendanceRepository attendanceDAO;

    public AcademicService() {
        this(new StudentDAO(), new GuardianDAO(), new TeacherDAO(),
                new SubjectDAO(), new AcademicRecordDAO(), new AttendanceDAO());
    }

    public AcademicService(StudentRepository studentDAO,
                           GuardianRepository guardianDAO,
                           TeacherRepository teacherDAO,
                           SubjectRepository subjectDAO,
                           AcademicRecordRepository academicRecordDAO,
                           AttendanceRepository attendanceDAO) {
        this.studentDAO = studentDAO;
        this.guardianDAO = guardianDAO;
        this.teacherDAO = teacherDAO;
        this.subjectDAO = subjectDAO;
        this.academicRecordDAO = academicRecordDAO;
        this.attendanceDAO = attendanceDAO;
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

    @Override
    public Integer registerStudent(Student student) {
        validateStudent(student);
        return studentDAO.save(student);
    }

    @Override
    public boolean updateStudent(Student student) {
        validateStudent(student);
        return studentDAO.update(student);
    }

    @Override
    public Optional<Student> getStudentById(int id) {
        return studentDAO.findById(id);
    }

    @Override
    public Optional<Student> getStudentByDocument(String docNumber) {
        return studentDAO.findByDocument(docNumber);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentDAO.findAll();
    }

    @Override
    public Integer registerGuardian(Guardian guardian) {
        validateGuardian(guardian);
        return guardianDAO.save(guardian);
    }

    @Override
    public List<Guardian> getAllGuardians() {
        return guardianDAO.findAll();
    }

    @Override
    public boolean linkStudentToGuardian(int studentId, int guardianId, boolean isPrimary, boolean isEmergency) {
        return studentDAO.associateGuardian(studentId, guardianId, isPrimary, isEmergency);
    }

    @Override
    public boolean enrollStudentInGrade(int studentId, int gradeId, int year) {
        return studentDAO.enrollInGrade(studentId, gradeId, year);
    }

    @Override
    public Integer registerTeacher(Teacher teacher) {
        validateTeacher(teacher);
        return teacherDAO.save(teacher);
    }

    @Override
    public List<Teacher> getAllTeachers() {
        return teacherDAO.findAll();
    }

    @Override
    public Integer registerSubject(Subject subject) {
        return subjectDAO.save(subject);
    }

    @Override
    public List<Subject> getAllSubjects() {
        return subjectDAO.findAll();
    }

    @Override
    public List<Subject> getSubjectsByGrade(int gradeId) {
        return subjectDAO.findByGrade(gradeId);
    }

    @Override
    public List<AcademicGrade> getAllGrades() {
        return subjectDAO.findAllGrades();
    }

    @Override
    public boolean assignTeacherToSubject(int teacherId, int subjectId, int year, String section) {
        return teacherDAO.assignToSubject(teacherId, subjectId, year, section);
    }

    @Override
    public boolean enrollStudentInSubject(int studentId, int subjectId, int year, String period, String section) {
        return subjectDAO.enrollStudent(studentId, subjectId, year, period, section);
    }

    @Override
    public boolean recordGrade(AcademicRecord record) {
        if (!ValidationUtil.isValidGrade(record.getGradeValue())) {
            throw new IllegalArgumentException("La nota debe estar entre 0.0 y 5.0 según el estándar colombiano.");
        }
        return academicRecordDAO.saveOrUpdate(record);
    }

    @Override
    public List<AcademicRecord> getAcademicHistory(int studentId) {
        return academicRecordDAO.findByStudent(studentId);
    }

    @Override
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

    @Override
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

    @Override
    public boolean registerAttendance(Attendance attendance) {
        return attendanceDAO.recordAttendance(attendance);
    }

    @Override
    public List<Attendance> getStudentAttendance(int studentId) {
        return attendanceDAO.findByStudent(studentId);
    }

    @Override
    public Map<Integer, Long> getStudentsWithExcessiveAbsences(long threshold) {
        List<Attendance> allRecent = attendanceDAO.findAllRecent();

        return allRecent.stream()
                .filter(a -> "Ausente".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.groupingBy(Attendance::getIdStudent, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public List<Student> getHonorRoll(int year, String period, double minAverage) {
        List<Student> allStudents = studentDAO.findAll();
        return allStudents.stream()
                .filter(s -> "Activo".equalsIgnoreCase(s.getStatus()))
                .filter(s -> calculateStudentPeriodAverage(s.getIdStudent(), year, period).doubleValue() >= minAverage)
                .sorted(Comparator.comparingDouble((Student s) -> calculateStudentPeriodAverage(s.getIdStudent(), year, period).doubleValue()).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getGradeDistribution(int subjectId, int year, String period) {
        List<AcademicRecord> records = academicRecordDAO.findBySubjectAndPeriod(subjectId, year, period);
        return records.stream()
                .filter(r -> r.getGradeValue() != null)
                .collect(Collectors.groupingBy(r -> {
                    double val = r.getGradeValue().doubleValue();
                    if (val >= 4.6) return "Sobresaliente";
                    if (val >= 4.0) return "Alto";
                    if (val >= 3.0) return "Básico";
                    return "Bajo";
                }, Collectors.counting()));
    }

    @Override
    public DoubleSummaryStatistics getSubjectPerformanceStatistics(int subjectId, int year, String period) {
        List<AcademicRecord> records = academicRecordDAO.findBySubjectAndPeriod(subjectId, year, period);
        return records.stream()
                .map(AcademicRecord::getGradeValue)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .summaryStatistics();
    }

    @Override
    public Map<Integer, Long> getTeacherAssignedSubjectsCount(int year) {
        List<Subject> allSubjects = subjectDAO.findAll();
        return allSubjects.stream()
                .filter(s -> s.getIdGrade() != null)
                .collect(Collectors.groupingBy(Subject::getIdGrade, Collectors.counting()));
    }

    @Override
    public Map<Boolean, List<Student>> partitionStudentsByPerformance(int year, String period, BigDecimal threshold) {
        List<Student> allStudents = studentDAO.findAll();
        return allStudents.stream()
                .filter(s -> "Activo".equalsIgnoreCase(s.getStatus()))
                .collect(Collectors.partitioningBy(s ->
                        calculateStudentPeriodAverage(s.getIdStudent(), year, period).compareTo(threshold) >= 0));
    }
}
