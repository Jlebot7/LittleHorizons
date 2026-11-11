# Little Horizons ERP - Sistema de Gestión Educativa Integral

![Java](https://img.shields.io/badge/Java-21%20%2F%2027-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0%20%7C%208.4%2B-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVC%20%2B%20DAO-blue?style=for-the-badge)

Sistema de Planificación de Recursos Empresariales (ERP) desarrollado para el **Colegio Internacional "Little Horizons"**, diseñado bajo altos estándares de ingeniería de software, arquitectura limpia Modelo-Vista-Controlador (MVC), patrón DAO (Data Access Object), Principios SOLID, prevención estricta de Inyección SQL y generación de facturación electrónica DIAN con CUFE y código QR.

---

## 1. Título del Proyecto
**Little Horizons ERP (Enterprise Resource Planning Educativo)**  
*Sistema Centralizado de Gestión Académica, Extracurricular, Administrativa y Financiera.*

---

## 2. Descripción

### Resumen del Propósito
El Colegio Internacional "Little Horizons" atiende a más de 500 estudiantes desde preescolar hasta grado 11°, cuenta con una planta de más de 40 docentes y una amplia oferta curricular y extracurricular. Este ERP actúa como el núcleo unificado que automatiza y centraliza la captura de datos, cálculo de calificaciones, cobros, facturación electrónica y préstamos de inventario.

### Problemas que Resuelve
* **Pérdida de Información Académica:** Reemplaza cuadernos de notas y planillas dispersas con persistencia relacional transaccional, cálculos automatizados de promedios ponderados y control de integridad referencial.
* **Desorganización en Actividades Extracurriculares:** Control estricto de cupos máximos en clubes y seguimiento histórico de participación y puestos obtenidos en eventos competitivos.
* **Caos en la Facturación:** Automatización del registro de pagos, recibos consecutivos y generación instantánea de **Factura Electrónica en PDF** con firma digital simulada, CUFE (SHA-256) y código QR para cumplimiento DIAN.
* **Desperdicio de Recursos Escolares:** Trazabilidad completa de préstamos de libros, microscopios, balanzas y tablets tanto para estudiantes como para docentes, con alertas de préstamos vencidos.
* **Detección Temprana de Deserción y Ausentismo:** Reportes automatizados de inasistencias acumuladas y cartera morosa mediante Java Stream API.

---

## 3. Autores
* **Desarrollador Principal:** Ingeniero de Software / Java Backend Developer
* **Institución:** Colegio Internacional Little Horizons S.A.S.
* **Contacto Técnico:** `desarrollo@littlehorizons.edu.co` | `soporte.erp@littlehorizons.edu.co`

---

## 4. Tecnologías Utilizadas
* **Lenguaje de Programación:** Java 21 LTS / Java 27 (OpenJDK 64-Bit Server VM).
* **Entorno de Desarrollo (IDE):** IntelliJ IDEA Ultimate / Community 2026.2+.
* **Gestor de Dependencias y Construcción:** Apache Maven 3.9+.
* **Motor de Base de Datos:** MySQL 8.0 o superior (compatible con MySQL 8.4 LTS) con soporte InnoDB, UTF8MB4 y restricciones `CHECK`.
* **Librerías Externas Principales:**
  * `com.mysql:mysql-connector-j:8.4.0`: Driver JDBC oficial para MySQL.
  * `com.zaxxer:HikariCP:5.1.0`: Pool de conexiones JDBC ultrarrápido y seguro.
  * `com.github.librepdf:openpdf:2.0.3`: Generación de documentos PDF de facturación DIAN.
  * `com.google.zxing:core:3.5.3` y `javase:3.5.3`: Motor de codificación y renderizado gráfico de códigos QR.
  * `org.slf4j:slf4j-simple:2.0.13`: Logging estándar para pool de conexiones.
  * `org.junit.jupiter:junit-jupiter:5.10.2`: Pruebas unitarias automatizadas.

---

## 5. Requerimientos Previos
Para ejecutar y desplegar este proyecto se requiere:
1. **Java Development Kit (JDK):** Versión 21 o superior debidamente configurada (`JAVA_HOME`).
2. **Apache Maven:** Versión 3.9.0 o superior instalada (o uso de Maven bundled en IntelliJ).
3. **MySQL Server:** **Requerido MySQL 8.0 o superior**. El esquema hace uso intensivo de validaciones de integridad mediante restricciones `CHECK` (ej: notas entre `0.0` y `5.0`), que en versiones previas a MySQL 8.0 eran ignoradas por el motor.

---

## 6. Configuración de la Base de Datos

### 6.1 Creación de la Base de Datos
El proyecto incluye el script maestro en [docs/database.sql](docs/database.sql), el cual contiene la sección DDL (creación de base de datos y 22 tablas con llaves foráneas e índices) y la sección DML (datos de prueba coherentes con más de 30 registros en cada tabla principal).

Para crear y poblar la base de datos en su servidor MySQL local o remoto:
```bash
# Mediante consola MySQL o PowerShell:
mysql -u root -p < docs/database.sql
```

O desde MySQL Workbench / DBeaver / DataGrip:
1. Abra el archivo `docs/database.sql`.
2. Ejecute el script completo (Ctrl + Shift + Enter).
3. Se creará la base de datos `erp_little_horizons` con todas las tablas y datos precargados.

### 6.2 Configuración de Credenciales
El archivo de propiedades se encuentra en `src/main/resources/database.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8
db.user=root
db.password=root
db.pool.maximumPoolSize=10
db.pool.minimumIdle=2
db.pool.idleTimeout=30000
db.pool.connectionTimeout=10000
```
*También es posible configurar variables de entorno en el sistema operativo:* `DB_URL`, `DB_USER` y `DB_PASSWORD`.

### 6.3 Driver JDBC
El driver oficial `mysql-connector-j` versión 8.4.0 se encuentra integrado y gestionado automáticamente por Maven en el archivo `pom.xml`.

---

## 7. Instalación y Ejecución

### 7.1 Clonar el Repositorio
```bash
git clone https://github.com/usuario/com.littlehorizons.erp.git
cd com.littlehorizons.erp
```

### 7.2 Abrir en el IDE
1. Abra IntelliJ IDEA.
2. Seleccione **File -> Open...** y elija la carpeta del proyecto.
3. El IDE detectará el archivo `pom.xml` y descargará las dependencias.

### 7.3 Compilar y Ejecutar Pruebas
```bash
mvn clean test
```

### 7.4 Ejecutar la Aplicación
```bash
mvn compile exec:java -Dexec.mainClass="com.littlehorizons.erp.Main"
```
O directamente desde IntelliJ ejecutando la clase [com.littlehorizons.erp.Main](src/main/java/com/littlehorizons/erp/Main.java).

---

## 8. Estructura del Proyecto (Arquitectura MVC + DAO)

El código fuente respeta estrictamente las convenciones de Java y la separación de responsabilidades:

```
src/
├── main/
│   ├── java/com/littlehorizons/erp/
│   │   ├── config/         // Configuración de infraestructura (HikariCP, pool JDBC)
│   │   ├── controller/     // Controladores de flujo y orquestación
│   │   ├── dao/            // Capa de persistencia JDBC pura con PreparedStatement
│   │   │   ├── academic/
│   │   │   ├── extracurricular/
│   │   │   ├── finance/
│   │   │   └── resource/
│   │   ├── model/          // Entidades de dominio encapsuladas
│   │   │   ├── academic/
│   │   │   ├── extracurricular/
│   │   │   ├── finance/
│   │   │   └── resource/
│   │   ├── service/        // Lógica de negocio, cálculos con Stream API y facturación PDF
│   │   ├── util/           // Logging (error_log.txt), CUFE DIAN, validaciones seguras
│   │   ├── view/           // Interfaz de consola profesional y formateadores ASCII
│   │   └── Main.java       // Punto de entrada de la aplicación
│   └── resources/
│       └── database.properties
├── test/java/com/littlehorizons/erp/ // Pruebas unitarias de integridad
docs/
└── database.sql            // DDL completo y DML con más de 30 registros por tabla
invoices/                   // Carpeta donde se generan las facturas PDF electrónicas
error_log.txt               // Registro persistente de errores y auditoría del sistema
```

---

## 9. Principios y Patrones de Diseño Aplicados

### 9.1 Principios SOLID
1. **Single Responsibility Principle (SRP):** Cada clase tiene una única responsabilidad bien acotada. `StudentDAO` únicamente gestiona persistencia SQL de estudiantes; `AcademicService` procesa cálculos académicos; `InvoicePdfService` genera exclusivamente facturas PDF.
2. **Open/Closed Principle (OCP):** El sistema permite incorporar nuevos métodos de pago, formatos de exportación o categorías de recursos extendiendo los servicios sin modificar el núcleo de datos.
3. **Liskov Substitution Principle (LSP):** Las entidades y DAOs respetan contratos coherentes sin alterar el comportamiento de las jerarquías.
4. **Interface Segregation Principle (ISP):** Clases y métodos desacoplados que exponen únicamente las operaciones requeridas por cada capa.
5. **Dependency Inversion Principle (DIP):** Los controladores dependen de servicios de alto nivel y la conexión a base de datos se centraliza en `DatabaseConfig`.

### 9.2 Patrones de Diseño Implementados
* **Data Access Object (DAO):** Desacopla la lógica de negocio de las sentencias SQL JDBC. Cada consulta utiliza `PreparedStatement` para prevenir inyección SQL.
* **Singleton Pattern:** Implementado en `DatabaseConfig` para compartir un único pool de conexiones HikariCP administrado y thread-safe.
* **Factory / Builder:** Aplicado en la construcción de documentos PDF en `InvoicePdfService` y generación de códigos QR con ZXing.

---

## 10. Funcionalidades Principales por Módulo

1. **Gestión de Estudiantes y Matrículas:** Registro de estudiantes, vinculación de acudientes múltiples (hermanos) y matrícula en grados académicos (Preescolar a Once).
2. **Gestión Académica:**
   * Registro de notas en escala colombiana `0.0 - 5.0` por materia y período (`I`, `II`, `III`, `IV`, `Final`).
   * **Cálculo automático de promedios con Stream API:** Promedio ponderado por período y promedio general de grupo.
   * Registro diario de asistencia (`Presente`, `Ausente`, `Tardanza`, `Excusa Médica`).
   * Alertas tempranas de estudiantes con 3 o más inasistencias.
3. **Gestión de Profesores:** Directorio docente y asignación de carga académica por materia, sección y año lectivo.
4. **Actividades Extracurriculares:**
   * Catálogo de 30 clubes con validación de aforo máximo (`capacidad_maxima`).
   * Calendario de eventos escolares y registro de participantes con roles y posiciones obtenidas (1er Lugar, 2do Lugar, etc.).
5. **Gestión Financiera y Facturación Electrónica DIAN:**
   * Catálogo de conceptos de pago con opción de descuentos o becas personalizadas.
   * Registro de pagos y recibos de caja únicos.
   * **Generación automática de Factura Electrónica en PDF:** Información institucional de Little Horizons S.A.S., NIT `900.543.210-9`, desglose de conceptos, CUFE calculado con SHA-256 y código QR embebido.
   * Reporte de cartera morosa (estudiantes con pensiones pendientes mediante Stream API).
6. **Gestión de Recursos (Biblioteca y Laboratorios):**
   * Inventario categorizado de libros, microscopios, cronómetros y tablets.
   * Préstamos diferenciados para estudiantes (`prestamos_estudiante`) y docentes (`prestamos_profesor`).
   * Transición automática de estados (`Disponible` ↔ `Prestado`).
   * Alerta de préstamos vencidos no devueltos a la fecha.
7. **Reportes y Estadísticas:** Tablero ejecutivo con indicadores clave (KPIs) en tiempo real.

---

## 11. Manejo de Errores y Logs

* **Sistema de Logging:** Implementado en `LoggerUtil`, sincronizado y thread-safe.
* **Ubicación del Archivo de Logs:** Se almacena en la raíz del proyecto como `error_log.txt`.
* **Estrategia de Excepciones:** Todas las operaciones de base de datos capturan `SQLException` de manera controlada, registrando la traza completa con timestamp en `error_log.txt` sin exponer información sensible al usuario final.
* **Manejo de Recursos:** Bloques `try-with-resources` en todas las aperturas de `Connection`, `PreparedStatement`, `ResultSet` y streams de archivos PDF para evitar fugas de memoria o descriptores.

---

## 12. Capturas y Evidencia de Funcionamiento en Consola

```
╔══════════════════════════════════════════════════════╗
║             ERP Colegio Little Horizons              ║
║        Sistema de Gestión Educativa Integral         ║
╚══════════════════════════════════════════════════════╝

1. Gestión de Estudiantes y Matrículas
2. Gestión Académica (Calificaciones y Asistencia)
3. Gestión de Profesores y Carga Académica
4. Actividades Extracurriculares (Clubes y Eventos)
5. Gestión Financiera y Facturación Electrónica DIAN
6. Gestión de Recursos (Biblioteca y Laboratorios)
7. Reportes y Estadísticas Institucionales
8. Diagnóstico y Configuración de Base de Datos
9. Salir

Seleccione una opción: _
```

---

## 13. Problemas Conocidos y Limitaciones
* La aplicación está optimizada como aplicación de consola profesional estándar Java.
* La firma digital y el CUFE de la factura electrónica son simulaciones algorítmicas criptográficas basadas en el estándar de la DIAN para propósitos académicos y de demostración funcional.

---

## 14. Futuras Mejoras
* Interfaz gráfica web moderna mediante Spring Boot y React.
* Módulo de envío automático de facturas PDF por correo electrónico vía SMTP.
* Pasarela de pagos en línea integrada (Wompi / PSE / PayU).

---

## 15. Licencia
Este proyecto fue desarrollado con fines educativos bajo la licencia MIT.

---

## 16. Contacto
* **Colegio Internacional Little Horizons S.A.S.**  
* **Dirección:** Calle 127 # 45-12, Bogotá D.C., Colombia  
* **Línea de Atención:** +57 (601) 745-8900  
* **Correo:** `info@littlehorizons.edu.co`
