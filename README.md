# Little Horizons ERP - Sistema de Gestión Educativa Integral

![Java](https://img.shields.io/badge/Java-21%20%2F%2027-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0%20%7C%208.4%2B-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVC%20%2B%20DAO-blue?style=for-the-badge)
![Security Audit](https://img.shields.io/badge/Audit-Passed%20100%25-brightgreen?style=for-the-badge)

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
* **Desarrollador Principal:** Juan Camilo Leal ([@Jlebot7](https://github.com/Jlebot7))
* **Institución:** Colegio Internacional Little Horizons S.A.S.
* **Contacto Técnico:** `juancamiloleal07@gmail.com` | `desarrollo@littlehorizons.edu.co`

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
  * `org.junit.jupiter:junit-jupiter:5.10.2`: Pruebas unitarias y de integración automatizadas.

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
git clone https://github.com/Jlebot7/LittleHorizons.git
cd LittleHorizons
```

### 7.2 Abrir en el IDE
1. Abra IntelliJ IDEA.
2. Seleccione **File -> Open...** y elija la carpeta del proyecto.
3. El IDE detectará el archivo `pom.xml` y descargará las dependencias.

### 7.3 Compilar y Ejecutar Pruebas Automatizadas
```bash
mvn clean test
```

### 7.4 Ejecutar la Aplicación

**Opción A — Ejecutable Standalone (Fat JAR generado con Maven Shade):**
```bash
mvn clean package
java -jar target/LittleHorizons-1.0-SNAPSHOT.jar
```

**Opción B — Mediante Maven Exec Plugin:**
```bash
mvn compile exec:java -Dexec.mainClass="com.littlehorizons.erp.Main"
```

**Opción C — Desde el IDE:**
Ejecutar la clase principal `com.littlehorizons.erp.Main`.

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
│   │   ├── service/        // Lógica de negocio (Streams, PDF DIAN, validaciones)
│   │   ├── util/           // CUFE, Logger centralizado, validador estricto, lector de consola
│   │   ├── view/           // Vistas de consola e interfaz de usuario
│   │   └── Main.java       // Clase punto de entrada
│   └── resources/
│       └── database.properties // Parámetros del pool HikariCP y MySQL
└── test/
    └── java/com/littlehorizons/erp/
        ├── CufeGeneratorTest.java          // Validación SHA-256 DIAN
        ├── DatabaseConnectivityTest.java   // Verificación pool y persistencia MySQL en vivo
        ├── FullSystemIntegrationTest.java  // Flujo E2E pago -> factura PDF -> MySQL
        ├── InvoicePdfGenerationTest.java   // Generación física de PDF y QR
        ├── SecurityIntegrityAuditTest.java // Auditoría de código limpio, PreparedStatement y límites
        ├── StreamCalculationTest.java      // Promedios y agrupaciones con Stream API
        └── ValidationUtilTest.java         // Validación estricta de fechas, notas y regex
```

---

## 9. Módulos y Funcionalidades Principales

### 9.1 Gestión Académica
* Registro completo de estudiantes, acudientes y docentes.
* Matrículas por grado y asociación acudiente-estudiante (parentesco, contacto de emergencia).
* Calificaciones con escala oficial colombiana `0.0` a `5.0`.
* Cálculo de promedios ponderados y ranking de estudiantes destacados mediante **Java Stream API** (`mapToDouble`, `average`).
* Control diario de asistencia y detección funcional de estudiantes con ausencias críticas acumuladas.

### 9.2 Facturación Electrónica DIAN y Gestión Financiera
* Registro transaccional de pagos por conceptos (Matrícula, Pensión, Biblioteca, Talleres, etc.).
* Generación instantánea de **Factura Electrónica en PDF** con diseño institucional.
* Algoritmo de cálculo de **CUFE** (Código Único de Facturación Electrónica) con hashing seguro **SHA-256** en hexadecimal de 64 caracteres.
* Código de barras bidimensional **QR** incrustado en el PDF codificando la información tributaria exigida por la DIAN.
* Identificación funcional de estudiantes en mora de pago mediante Streams.

### 9.3 Actividades Extracurriculares
* Gestión de clubes con cupos máximos y validación para evitar sobrecupos.
* Control de membresías activas, suspendidas y finalizadas.
* Organización de eventos intercolegiales y seguimiento de resultados (posiciones obtenidas).

### 9.4 Préstamos de Recursos Escolares
* Catálogo de inventario para biblioteca y laboratorios con seguimiento de ejemplares disponibles.
* Registro de préstamos y devoluciones para estudiantes y profesores.
* Alertas preventivas para préstamos vencidos no devueltos.

---

## 10. Cumplimiento de Buenas Prácticas y Seguridad
* **Inyección SQL Imposible:** 100% de consultas SQL construidas con `PreparedStatement` y paso de parámetros por posición (`?`).
* **Higiene de Recursos:** Cierre determinista de conexiones JDBC, statements y streams mediante `try-with-resources`.
* **Código Limpio:** Código fuente 100% libre de comentarios residuales en clases de producción, con nomenclatura autodocumentada en inglés.
* **Auditoría Formal:** Informe de auditoría técnica disponible en [docs/SECURITY_AUDIT.md](docs/SECURITY_AUDIT.md).

---

## 11. Manejo de Errores y Logging
* **Sistema de Logging:** Sincronizado y seguro ante concurrencia en `LoggerUtil`.
* **Ubicación:** `error_log.txt` en la raíz del proyecto.
* **Seguridad de Información:** Mensajes limpios y amigables hacia la consola, canalizando stacktraces técnicos únicamente al archivo de log interno.

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
* La aplicación está diseñada como una aplicación de consola estándar Java para alta portabilidad y rendimiento.
* La firma digital y el CUFE de la factura electrónica son simulaciones algorítmicas criptográficas basadas en el estándar técnico de la DIAN para propósitos de demostración funcional y evaluación.

---

## 14. Futuras Mejoras
* Interfaz gráfica web moderna con Spring Boot y frontend React.
* Envío automatizado de facturas electrónicas PDF por correo electrónico vía SMTP.
* Integración con pasarelas de pago colombianas en línea (Wompi / PSE / PayU).

---

## 15. Licencia
Este proyecto fue desarrollado bajo la Licencia MIT.

---

## 16. Contacto
* **Colegio Internacional Little Horizons S.A.S.**  
* **Desarrollador:** Juan Camilo Leal ([@Jlebot7](https://github.com/Jlebot7))  
* **Correo:** `juancamiloleal07@gmail.com`
