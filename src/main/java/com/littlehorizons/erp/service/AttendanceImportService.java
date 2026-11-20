package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.academic.*;
import com.littlehorizons.erp.model.academic.Attendance;
import com.littlehorizons.erp.model.academic.AttendanceImportResult;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.ValidationUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;

public class AttendanceImportService implements IAttendanceImportService {

    public static final String DEFAULT_FILE_NAME = "asistencias_importar.csv";
    public static final String EXPECTED_HEADER = "id_estudiante;id_materia;fecha;estado";

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "Presente", "Ausente", "Tardanza", "Excusa Médica"
    );

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public AttendanceImportService() {
        this(new AttendanceDAO(), new StudentDAO(), new SubjectDAO());
    }

    public AttendanceImportService(AttendanceRepository attendanceRepository,
                                   StudentRepository studentRepository,
                                   SubjectRepository subjectRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public AttendanceImportResult importDefaultCsv() {
        return importFromCsv(Path.of(DEFAULT_FILE_NAME));
    }

    @Override
    public AttendanceImportResult importFromCsv(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            return new AttendanceImportResult(0, 0, 0,
                    "Error: El archivo '" + DEFAULT_FILE_NAME + "' no se encuentra en la carpeta raíz del proyecto.");
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return new AttendanceImportResult(0, 0, 0,
                        "Error: El archivo no tiene el formato correcto. La cabecera debe ser: " + EXPECTED_HEADER);
            }

            if (header.startsWith("\uFEFF")) {
                header = header.substring(1);
            }

            if (!EXPECTED_HEADER.equals(header.trim())) {
                return new AttendanceImportResult(0, 0, 0,
                        "Error: El archivo no tiene el formato correcto. La cabecera debe ser: " + EXPECTED_HEADER);
            }

            int totalLines = 0;
            int successLines = 0;
            int errorLines = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                totalLines++;

                String[] parts = line.split(";", -1);
                if (parts.length != 4) {
                    errorLines++;
                    continue;
                }

                String studentIdStr = parts[0].trim();
                String subjectIdStr = parts[1].trim();
                String dateStr = parts[2].trim();
                String statusStr = parts[3].trim();

                int studentId;
                try {
                    studentId = Integer.parseInt(studentIdStr);
                    if (studentRepository.findById(studentId).isEmpty()) {
                        errorLines++;
                        continue;
                    }
                } catch (Exception e) {
                    errorLines++;
                    continue;
                }

                int subjectId;
                try {
                    subjectId = Integer.parseInt(subjectIdStr);
                    if (subjectRepository.findById(subjectId).isEmpty()) {
                        errorLines++;
                        continue;
                    }
                } catch (Exception e) {
                    errorLines++;
                    continue;
                }

                if (!ValidationUtil.isValidDate(dateStr)) {
                    errorLines++;
                    continue;
                }

                LocalDate attendanceDate;
                try {
                    attendanceDate = LocalDate.parse(dateStr);
                } catch (Exception e) {
                    errorLines++;
                    continue;
                }

                if (!ALLOWED_STATUSES.contains(statusStr)) {
                    errorLines++;
                    continue;
                }

                try {
                    Attendance attendance = new Attendance(null, studentId, subjectId, attendanceDate, statusStr, null, null);
                    boolean recorded = attendanceRepository.recordAttendance(attendance);
                    if (recorded) {
                        successLines++;
                    } else {
                        errorLines++;
                    }
                } catch (Exception e) {
                    LoggerUtil.error("Failed importing attendance line: " + line, e);
                    errorLines++;
                }
            }

            return new AttendanceImportResult(totalLines, successLines, errorLines, null);

        } catch (IOException e) {
            LoggerUtil.error("Error reading CSV file " + filePath, e);
            return new AttendanceImportResult(0, 0, 0, "Error de E/S al leer el archivo: " + e.getMessage());
        }
    }
}
