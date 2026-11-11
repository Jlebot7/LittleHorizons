package com.littlehorizons.erp.view;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.controller.*;
import com.littlehorizons.erp.service.*;
import com.littlehorizons.erp.util.ConsoleInputReader;

/**
 * Main application menu controller and navigation dispatcher.
 */
public class MainMenuView {

    private final StudentController studentController;
    private final AcademicController academicController;
    private final TeacherController teacherController;
    private final ExtracurricularController extracurricularController;
    private final FinanceController financeController;
    private final ResourceController resourceController;
    private final ReportsController reportsController;

    public MainMenuView() {
        AcademicService academicService = new AcademicService();
        ExtracurricularService extracurricularService = new ExtracurricularService();
        FinanceService financeService = new FinanceService();
        ResourceService resourceService = new ResourceService();

        this.studentController = new StudentController(academicService);
        this.academicController = new AcademicController(academicService);
        this.teacherController = new TeacherController(academicService);
        this.extracurricularController = new ExtracurricularController(extracurricularService);
        this.financeController = new FinanceController(financeService);
        this.resourceController = new ResourceController(resourceService);
        this.reportsController = new ReportsController(academicService, extracurricularService, financeService, resourceService);
    }

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
            System.out.println("8. Diagnóstico y Configuración de Base de Datos");
            System.out.println("9. Salir");
            System.out.println();

            int choice = ConsoleInputReader.readInt("Seleccione una opción: ", 1, 9);
            switch (choice) {
                case 1 -> studentController.run();
                case 2 -> academicController.run();
                case 3 -> teacherController.run();
                case 4 -> extracurricularController.run();
                case 5 -> financeController.run();
                case 6 -> resourceController.run();
                case 7 -> reportsController.run();
                case 8 -> showDatabaseDiagnostic();
                case 9 -> {
                    exit = true;
                    System.out.println("\nCerrando Little Horizons ERP... ¡Hasta pronto!");
                    DatabaseConfig.close();
                }
            }
        }
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
            System.out.println("  Asegúrese de haber ejecutado el script en docs/database.sql");
            System.out.println("  y que el servicio de MySQL 8.0+ esté en ejecución.");
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
