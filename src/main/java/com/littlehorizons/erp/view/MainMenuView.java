package com.littlehorizons.erp.view;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.controller.*;
import com.littlehorizons.erp.model.academic.AttendanceImportResult;
import com.littlehorizons.erp.model.report.AttendanceSummaryReport;
import com.littlehorizons.erp.service.*;
import com.littlehorizons.erp.util.ConsoleInputReader;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainMenuView implements IMainMenuView {

    private final StudentController studentController;
    private final AcademicController academicController;
    private final TeacherController teacherController;
    private final ExtracurricularController extracurricularController;
    private final FinanceController financeController;
    private final ResourceController resourceController;
    private final ReportsController reportsController;
    private final IAttendanceImportService attendanceImportService;
    private final IAttendanceReportService attendanceReportService;
    private final IDatabaseBootstrapService databaseBootstrapService;

    public MainMenuView() {
        this(new AttendanceImportService(), new AttendanceReportService(), new DatabaseBootstrapService());
    }

    public MainMenuView(IAttendanceImportService attendanceImportService) {
        this(attendanceImportService, new AttendanceReportService(), new DatabaseBootstrapService());
    }

    public MainMenuView(IAttendanceImportService attendanceImportService,
                        IAttendanceReportService attendanceReportService,
                        IDatabaseBootstrapService databaseBootstrapService) {
        IAcademicService academicService = new AcademicService();
        IExtracurricularService extracurricularService = new ExtracurricularService();
        IFinanceService financeService = new FinanceService();
        IResourceService resourceService = new ResourceService();
        IReportsService reportsService = new ReportsService(academicService, extracurricularService, financeService, resourceService);

        this.attendanceImportService = attendanceImportService;
        this.attendanceReportService = attendanceReportService;
        this.databaseBootstrapService = databaseBootstrapService;

        this.studentController = new StudentController(academicService, new StudentConsoleView());
        this.academicController = new AcademicController(academicService, new AcademicConsoleView(), attendanceReportService);
        this.teacherController = new TeacherController(academicService, new TeacherConsoleView());
        this.extracurricularController = new ExtracurricularController(extracurricularService, new ExtracurricularConsoleView());
        this.financeController = new FinanceController(financeService, new FinanceConsoleView());
        this.resourceController = new ResourceController(resourceService, new ResourceConsoleView());
        this.reportsController = new ReportsController(reportsService, new ReportsConsoleView());
    }

    @Override
    public void start() {
        boolean exit = false;
        while (!exit) {
            ConsoleHelper.printHeader("ERP Colegio Little Horizons", "Sistema de Gestión Educativa Integral");
            System.out.println("1. Gestión de Estudiantes y Matrículas");
            System.out.println("2. Gestión Académica (Calificaciones y Asistencia)");
            System.out.println("3. Gestión de Profesores y Carga Académica");
            System.out.println("4. Actividades Extracurriculares (Clubes y Eventos)");
            System.out.println("5. Gestión Financiera y Facturación Electrónica DIAN");
            System.out.println("6. Gestión de Recursos (Biblioteca y Laboratorios)");
            System.out.println("7. Reportes y Estadísticas Institucionales");
            System.out.println("8. Diagnóstico y Auto-Levantamiento de Base de Datos");
            System.out.println("9. TEST - Importar Asistencias desde CSV");
            System.out.println("10. Generar y Exportar Reporte de Asistencias (TXT)");
            System.out.println("11. Salir");
            System.out.println();

            int choice = ConsoleInputReader.readInt("Seleccione una opción: ", 1, 11);
            switch (choice) {
                case 1 -> studentController.run();
                case 2 -> academicController.run();
                case 3 -> teacherController.run();
                case 4 -> extracurricularController.run();
                case 5 -> financeController.run();
                case 6 -> resourceController.run();
                case 7 -> reportsController.run();
                case 8 -> showDatabaseDiagnostic();
                case 9 -> importAttendanceFromCsv();
                case 10 -> generateAndExportAttendanceReport();
                case 11 -> {
                    exit = true;
                    System.out.println("\nCerrando Little Horizons ERP... ¡Hasta pronto!");
                    DatabaseConfig.close();
                }
            }
        }
    }

    private void importAttendanceFromCsv() {
        AttendanceImportResult result = attendanceImportService.importDefaultCsv();
        if (result.hasError()) {
            System.out.println(result.errorMessage());
            return;
        }

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║           REPORTE DE IMPORTACIÓN DE ASISTENCIAS                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Total de líneas en el archivo: " + result.totalLines());
        System.out.println("Líneas procesadas exitosamente: " + result.successLines());
        System.out.println("Líneas con errores (no procesadas): " + result.errorLines());
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.println("Presione Enter para volver al menú principal...");
        System.out.println("═══════════════════════════════════════════════════════════════════");
        ConsoleInputReader.waitForEnter();
    }

    private void generateAndExportAttendanceReport() {
        try {
            List<AttendanceSummaryReport> reports = attendanceReportService.generateConsolidatedReport();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String targetPath = "reportes/reporte_asistencias_" + timestamp + ".txt";
            File exported = attendanceReportService.exportReportToText(reports, targetPath);

            ConsoleHelper.printSubHeader("REPORTE CONSOLIDADO DE ASISTENCIAS");
            System.out.println(attendanceReportService.formatReportAsText(reports));
            ConsoleHelper.printSuccess("Reporte exportado exitosamente a archivo TXT:");
            System.out.println("  -> " + exported.getAbsolutePath());
        } catch (Exception e) {
            ConsoleHelper.printError("Error al generar reporte de asistencia: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void showDatabaseDiagnostic() {
        ConsoleHelper.printSubHeader("DIAGNÓSTICO Y CONEXIÓN A BASE DE DATOS");
        System.out.println("URL Configurada : " + DatabaseConfig.getConfiguredUrl());
        System.out.println("Usuario         : " + DatabaseConfig.getConfiguredUser());
        System.out.print("Probando conectividad con el servidor MySQL... ");

        boolean connected = DatabaseConfig.testConnection();
        if (connected) {
            ConsoleHelper.printSuccess("¡Conexión establecida exitosamente con MySQL!");
        } else {
            ConsoleHelper.printError("Fallo de conexión con la base de datos.");
            System.out.println("  El servicio de MySQL podría estar detenido o las tablas no inicializadas.");
            boolean tryAuto = ConsoleInputReader.readBoolean("¿Desea auto-levantar servicio e inicializar tablas desde 'docs/database.sql'?");
            if (tryAuto) {
                System.out.println("Intentando auto-arranque...");
                boolean bootOk = databaseBootstrapService.bootstrapDatabase();
                if (bootOk) {
                    ConsoleHelper.printSuccess("¡Base de datos auto-levantada e inicializada correctamente!");
                    ConsoleInputReader.waitForEnter();
                    return;
                } else {
                    ConsoleHelper.printWarning("El auto-arranque no pudo conectar automáticamente.");
                }
            }

            boolean reconf = ConsoleInputReader.readBoolean("¿Desea ingresar credenciales personalizadas en este momento?");
            if (reconf) {
                String host = ConsoleInputReader.readString("Host (ej: localhost): ");
                int port = ConsoleInputReader.readInt("Puerto (ej: 3306): ", 1, 65535);
                String dbName = ConsoleInputReader.readString("Nombre de base de datos (ej: erp_little_horizons): ");
                String user = ConsoleInputReader.readString("Usuario: ");
                String pass = ConsoleInputReader.readOptionalString("Contraseña: ");
                String newUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota", host, port, dbName);

                boolean ok = DatabaseConfig.reconfigure(newUrl, user, pass);
                if (ok) {
                    ConsoleHelper.printSuccess("Conexión reconfigurada con éxito.");
                } else {
                    ConsoleHelper.printError("No se pudo conectar con los datos suministrados.");
                }
            }
        }
        ConsoleInputReader.waitForEnter();
    }
}
