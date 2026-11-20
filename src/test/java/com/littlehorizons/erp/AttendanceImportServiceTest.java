package com.littlehorizons.erp;

import com.littlehorizons.erp.dao.academic.AttendanceRepository;
import com.littlehorizons.erp.dao.academic.StudentRepository;
import com.littlehorizons.erp.dao.academic.SubjectRepository;
import com.littlehorizons.erp.model.academic.Attendance;
import com.littlehorizons.erp.model.academic.AttendanceImportResult;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.academic.Subject;
import com.littlehorizons.erp.service.AttendanceImportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceImportServiceTest {

    @Test
    @DisplayName("Should return specific error when file does not exist")
    void testFileDoesNotExist() {
        AttendanceImportService service = new AttendanceImportService();
        AttendanceImportResult result = service.importFromCsv(Path.of("non_existent_file.csv"));

        assertTrue(result.hasError());
        assertEquals("Error: El archivo 'asistencias_importar.csv' no se encuentra en la carpeta raíz del proyecto.", result.errorMessage());
        assertEquals(0, result.totalLines());
    }

    @Test
    @DisplayName("Should return specific error when header is invalid")
    void testInvalidHeader(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("asistencias_importar.csv");
        Files.writeString(csvFile, "estudiante;materia;fecha;asistencia\n1;5;2024-10-07;Presente\n");

        AttendanceImportService service = new AttendanceImportService();
        AttendanceImportResult result = service.importFromCsv(csvFile);

        assertTrue(result.hasError());
        assertEquals("Error: El archivo no tiene el formato correcto. La cabecera debe ser: id_estudiante;id_materia;fecha;estado", result.errorMessage());
        assertEquals(0, result.totalLines());
    }

    @Test
    @DisplayName("Should process line by line and count total, success and errors correctly")
    void testLineByLineValidationAndProcessing(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("asistencias_importar.csv");
        String content = """
                id_estudiante;id_materia;fecha;estado
                1;5;2024-10-07;Presente
                999;5;2024-10-07;Ausente
                
                3;5;2024-10-07;Presente
                4;99;2024-10-07;Tardanza
                5;8;2024-10-07;presente
                6;8;2024-10-07;Excusa Médica
                7;8;2024-02-30;Presente
                8;8;2024-10-07;Presente
                """;
        Files.writeString(csvFile, content);

        StudentRepository mockStudentRepo = new StudentRepository() {
            public Integer save(Student s) { return 1; }
            public boolean update(Student s) { return true; }
            public Optional<Student> findById(int id) {
                return (id >= 1 && id <= 10 && id != 999) ? Optional.of(new Student()) : Optional.empty();
            }
            public Optional<Student> findByDocument(String doc) { return Optional.empty(); }
            public java.util.List<Student> findAll() { return java.util.List.of(); }
            public boolean associateGuardian(int stId, int gId, boolean p, boolean e) { return true; }
            public boolean enrollInGrade(int stId, int grId, int yr) { return true; }
        };

        SubjectRepository mockSubjectRepo = new SubjectRepository() {
            public Integer save(Subject s) { return 1; }
            public Optional<Subject> findById(int id) {
                return (id != 99) ? Optional.of(new Subject()) : Optional.empty();
            }
            public java.util.List<Subject> findAll() { return java.util.List.of(); }
            public java.util.List<Subject> findByGrade(int grId) { return java.util.List.of(); }
            public java.util.List<com.littlehorizons.erp.model.academic.AcademicGrade> findAllGrades() { return java.util.List.of(); }
            public boolean enrollStudent(int stId, int subId, int yr, String p, String sec) { return true; }
        };

        AttendanceRepository mockAttendanceRepo = new AttendanceRepository() {
            public boolean recordAttendance(Attendance a) { return true; }
            public java.util.List<Attendance> findByStudent(int stId) { return java.util.List.of(); }
            public java.util.List<Attendance> findAllRecent() { return java.util.List.of(); }
        };

        AttendanceImportService service = new AttendanceImportService(mockAttendanceRepo, mockStudentRepo, mockSubjectRepo);
        AttendanceImportResult result = service.importFromCsv(csvFile);

        assertFalse(result.hasError());
        assertEquals(8, result.totalLines());
        assertEquals(4, result.successLines());
        assertEquals(4, result.errorLines());
        assertEquals(result.totalLines(), result.successLines() + result.errorLines());
    }
}
