Contexto del Requerimiento
La profesora Andrea González, coordinadora del departamento de Control Académico del Colegio Internacional "Little Horizons", enfrenta un problema operativo cada semana. Los 40 profesores del colegio registran la asistencia de sus estudiantes en planillas físicas durante sus clases. Al finalizar la semana, la profesora Andrea debe recopilar todas estas planillas y transcribir manualmente más de 2,000 registros de asistencia al sistema ERP.



Este proceso manual le consume aproximadamente 6 horas cada viernes por la tarde, tiempo en el que debe revisar cada planilla, verificar que los datos sean legibles, buscar cada estudiante en el sistema, localizar la materia correspondiente, y finalmente registrar si el estudiante estuvo presente, ausente, llegó tarde, o tuvo excusa médica. Además, este proceso es propenso a errores: nombres mal interpretados, materias confundidas, o estados de asistencia registrados incorrectamente.



Los profesores han propuesto una solución: si pudieran entregar sus planillas de asistencia en un archivo de texto simple con un formato estándar (que pueden crear fácilmente en Excel o cualquier editor de texto), el sistema podría leerlas automáticamente y cargar todas las asistencias de una sola vez. Esto no solo ahorraría horas de trabajo, sino que eliminaría los errores de transcripción manual.



La dirección del colegio ha aprobado esta iniciativa y te ha asignado la tarea de desarrollar esta funcionalidad de importación masiva que transformará el proceso semanal de registro de asistencias.



Descripción Detallada del Requerimiento
Tu tarea es implementar una nueva funcionalidad en el ERP que permita importar masivamente registros de asistencia desde un archivo de texto en formato CSV. Esta funcionalidad debe validar cada línea individualmente y procesar solo las líneas válidas, generando un reporte final del resultado de la importación.



1. Integración en el Menú Principal
   Debes añadir una nueva opción en el menú principal del sistema (el primer menú que aparece al ejecutar la aplicación), claramente identificada con el prefijo TEST.

El nombre de la opción debe ser: "TEST - Importar Asistencias desde CSV"

Sobre el inicio de sesión (si existe en tu implementación):

Si tu sistema tiene un módulo de login o autenticación al inicio, debes implementar una funcionalidad que permita omitir el login presionando Enter (sin ingresar usuario ni contraseña) y que esto lleve directamente al menú principal. Esto facilita las pruebas rápidas durante el desarrollo y evaluación del examen.

Si tu sistema NO tiene login, simplemente añade la opción TEST en el menú principal que aparece al iniciar la aplicación.



2. Ubicación y Nombre del Archivo
   La funcionalidad debe buscar y procesar un archivo con el nombre exacto asistencias_importar.csv. Este archivo deberá estar ubicado en la carpeta raíz del proyecto (el mismo directorio donde se encuentra el archivo README.md o donde se ejecuta la aplicación).



3. Formato Estricto del Archivo de Importación
   El archivo de entrada es un texto plano en formato CSV con una estructura muy específica y obligatoria. El archivo debe cumplir con las siguientes reglas sin excepción:

Regla 1 - Línea de Cabecera:

La primera línea del archivo DEBE ser exactamente la siguiente cabecera (respetando mayúsculas y minúsculas):

id_estudiante;id_materia;fecha;estado
Esta línea de cabecera es obligatoria y tu sistema debe verificar que exista y sea exacta antes de procesar cualquier dato.

Regla 2 - Líneas de Datos:

A partir de la segunda línea, cada línea representa un registro de asistencia a importar. Los campos de datos en cada línea están separados por un punto y coma (;). La estructura exacta de cada línea es:

id_estudiante;id_materia;fecha;estado
Donde:

id_estudiante: Número entero que corresponde al ID de un estudiante existente en la tabla estudiantes
id_materia: Número entero que corresponde al ID de una materia existente en la tabla materias
fecha: Fecha en formato YYYY-MM-DD (ej: 2024-10-07)
estado: Texto que indica el estado de asistencia. Solo puede ser uno de estos cuatro valores exactos:
Presente: El estudiante asistió normalmente a clase
Ausente: El estudiante no asistió a clase
Tardanza: El estudiante llegó tarde a clase
Excusa Médica: El estudiante no asistió pero presentó justificación médica


IMPORTANTE sobre el campo estado: Estos cuatro valores son los únicos permitidos según la definición de la tabla asistencias en la base de datos (campo estado_asistencia tipo ENUM). El valor debe escribirse exactamente como se muestra arriba, respetando mayúsculas, minúsculas, espacios y tildes. Por ejemplo, "presente" (todo minúsculas) o "Excusa Medica" (sin tilde) NO son válidos.



Regla 3 - Sin Líneas Vacías:

El archivo NO debe contener líneas vacías. Si tu implementación encuentra una línea vacía, debe ignorarla y continuar procesando las siguientes líneas.



Regla 4 - Todos los Campos son Obligatorios:

Cada línea de datos DEBE tener exactamente 4 campos separados por punto y coma. Si una línea tiene más o menos campos, se considera inválida.



4. Lógica de Validación y Procesamiento Línea por Línea
   IMPORTANTE: A diferencia de un proceso tradicional que valida todo el archivo antes de insertar, este sistema debe procesar el archivo línea por línea de manera independiente. Esto significa que:

Si una línea es válida → se inserta inmediatamente en la base de datos
Si una línea es inválida → se registra el error y se continúa con la siguiente línea
Al final se muestra un resumen con el total de líneas procesadas, exitosas y fallidas
Tu implementación debe seguir este flujo exacto:



Paso 1 - Verificar existencia del archivo:

Verificar que el archivo asistencias_importar.csv existe en la carpeta raíz del proyecto
Si NO existe: Mostrar el mensaje "Error: El archivo 'asistencias_importar.csv' no se encuentra en la carpeta raíz del proyecto." y regresar al menú principal
Si existe: Continuar con el Paso 2


Paso 2 - Validar cabecera del archivo:

Abrir el archivo y leer la primera línea (cabecera)
Verificar que la primera línea sea exactamente: id_estudiante;id_materia;fecha;estado
Si NO coincide: Mostrar el mensaje "Error: El archivo no tiene el formato correcto. La cabecera debe ser: id_estudiante;id_materia;fecha;estado" y regresar al menú principal
Si coincide: Continuar con el Paso 3


Paso 3 - Procesar cada línea de datos individualmente:

Para cada línea del archivo (a partir de la línea 2, después de la cabecera):

3.1. Ignorar líneas vacías:

Si la línea está vacía o contiene solo espacios en blanco, ignorarla y continuar con la siguiente línea
3.2. Validar estructura de la línea:

Dividir el texto usando el punto y coma como separador
Verificar que la línea tenga exactamente 4 campos
Si NO tiene 4 campos: Registrar la línea como fallida (contador de errores) y continuar con la siguiente línea
3.3. Validar que el estudiante exista:

Verificar que el id_estudiante corresponda a un estudiante existente en la tabla estudiantes
Si NO existe: Registrar la línea como fallida y continuar con la siguiente línea
3.4. Validar que la materia exista:

Verificar que el id_materia corresponda a una materia existente en la tabla materias
Si NO existe: Registrar la línea como fallida y continuar con la siguiente línea
3.5. Validar formato y validez de la fecha:

Verificar que el campo fecha tenga el formato correcto YYYY-MM-DD
Verificar que la fecha sea válida (no fechas imposibles como 2024-02-30)
Si NO es válida: Registrar la línea como fallida y continuar con la siguiente línea
3.6. Validar que el estado sea permitido:

Verificar que el campo estado sea exactamente uno de estos cuatro valores: Presente, Ausente, Tardanza, Excusa Médica
Si NO es válido: Registrar la línea como fallida y continuar con la siguiente línea
3.7. Insertar registro si todas las validaciones fueron exitosas:

Si la línea pasó todas las validaciones anteriores, insertar el registro en la tabla asistencias
Los campos a insertar son: id_estudiante, id_materia, fecha_asistencia, estado_asistencia
El campo registrado_por puede quedar NULL o puedes usar un valor por defecto
Si la inserción es exitosa: Incrementar contador de registros exitosos
Si la inserción falla por cualquier razón: Registrar la línea como fallida


Paso 4 - Mostrar reporte final del proceso:

Al terminar de procesar todas las líneas del archivo, mostrar en consola un resumen completo:

╔══════════════════════════════════════════════════════════════════╗
║           REPORTE DE IMPORTACIÓN DE ASISTENCIAS                  ║
╚══════════════════════════════════════════════════════════════════╝

Total de líneas en el archivo: X
Líneas procesadas exitosamente: Y
Líneas con errores (no procesadas): Z

═══════════════════════════════════════════════════════════════════
Presione Enter para volver al menú principal...
═══════════════════════════════════════════════════════════════════
Donde:

X = Total de líneas de datos en el archivo (sin contar la cabecera ni líneas vacías)
Y = Número de registros que se insertaron exitosamente en la base de datos
Z = Número de líneas que tuvieron algún error de validación o inserción
Nota importante sobre los contadores:

X debe ser igual a Y + Z (el total es la suma de exitosas más fallidas)
No contar la línea de cabecera en ningún contador
No contar líneas vacías en ningún contador


5. Estructura de la Tabla de Asistencias
   Tu tabla de asistencias en la base de datos ya tiene la siguiente estructura (según el DDL proporcionado):

id_asistencia (clave primaria, autoincremental)
id_estudiante (clave foránea a estudiantes)
id_materia (clave foránea a materias)
fecha_asistencia (tipo DATE)
estado_asistencia (ENUM: 'Presente', 'Ausente', 'Tardanza', 'Excusa Médica')
observaciones (opcional, puede quedar NULL)
registrado_por (clave foránea a profesores, puede quedar NULL)
fecha_creacion (timestamp automático)


Tu implementación debe mapear correctamente los datos del archivo CSV a estas columnas de la tabla.



6. Ejemplo del Archivo asistencias_importar.csv
   Para que tu funcionalidad sea probada, deberás crear un archivo de ejemplo con este formato exacto:

id_estudiante;id_materia;fecha;estado
1;5;2024-10-07;Presente
2;5;2024-10-07;Ausente
3;5;2024-10-07;Presente
1;8;2024-10-07;Tardanza
4;8;2024-10-07;Presente
2;8;2024-10-07;Excusa Médica
5;12;2024-10-08;Presente
6;12;2024-10-08;Ausente
Explicación del ejemplo:

Línea 1: Cabecera obligatoria
Línea 2: Estudiante con ID 1 en la materia con ID 5, fecha 7 de octubre de 2024, estado Presente
Línea 3: Estudiante con ID 2 en la materia con ID 5, fecha 7 de octubre de 2024, estado Ausente
Y así sucesivamente...

Nota importante: Los IDs de estudiantes y materias deben corresponder a registros reales que existen en tu base de datos. Si usas el archivo de ejemplo, asegúrate de que los IDs 1, 2, 3, 4, 5, 6 existan en la tabla estudiantes, y los IDs 5, 8, 12 existan en la tabla materias.



7. Ejemplo Completo de Procesamiento
   Para ilustrar mejor cómo debe funcionar el procesamiento línea por línea, considera este ejemplo:

Archivo de entrada (asistencias_importar.csv):

id_estudiante;id_materia;fecha;estado
1;5;2024-10-07;Presente
999;5;2024-10-07;Ausente
3;5;2024-10-07;Presente
4;99;2024-10-07;Tardanza
5;8;2024-10-07;presente
6;8;2024-10-07;Excusa Médica
7;8;2024-10-07;Presente
Análisis de cada línea:

Línea 1 (cabecera): Válida, continuar procesando
Línea 2 (1;5;2024-10-07;Presente): ✅ Todas las validaciones OK → Se inserta
Línea 3 (999;5;2024-10-07;Ausente): ❌ Estudiante ID 999 no existe → No se inserta
Línea 4 (3;5;2024-10-07;Presente): ✅ Todas las validaciones OK → Se inserta
Línea 5 (4;99;2024-10-07;Tardanza): ❌ Materia ID 99 no existe → No se inserta
Línea 6 (5;8;2024-10-07;presente): ❌ Estado "presente" (minúsculas) no es válido → No se inserta
Línea 7 (6;8;2024-10-07;Excusa Médica): ✅ Todas las validaciones OK → Se inserta
Línea 8 (7;8;2024-10-07;Presente): ✅ Todas las validaciones OK → Se inserta
Salida esperada en consola:

╔══════════════════════════════════════════════════════════════════╗
║           REPORTE DE IMPORTACIÓN DE ASISTENCIAS                  ║
╚══════════════════════════════════════════════════════════════════╝

Total de líneas en el archivo: 7
Líneas procesadas exitosamente: 4
Líneas con errores (no procesadas): 3

═══════════════════════════════════════════════════════════════════
Presione Enter para volver al menú principal...
═══════════════════════════════════════════════════════════════════
Explicación de los números:

Total: 7 líneas de datos (sin contar la cabecera)
Exitosas: 4 líneas (líneas 2, 4, 7, 8)
Con errores: 3 líneas (líneas 3, 5, 6)
8. Casos Adicionales de Ejemplo
   Ejemplo 1 - Archivo con cabecera incorrecta:

estudiante;materia;fecha;asistencia
1;5;2024-10-07;Presente
Resultado esperado:

Error: El archivo no tiene el formato correcto. La cabecera debe ser: id_estudiante;id_materia;fecha;estado
(El proceso se detiene inmediatamente, no se procesa ninguna línea)



Ejemplo 2 - Todos los registros son válidos:

id_estudiante;id_materia;fecha;estado
1;5;2024-10-07;Presente
2;5;2024-10-07;Ausente
3;8;2024-10-07;Tardanza
Resultado esperado:

╔══════════════════════════════════════════════════════════════════╗
║           REPORTE DE IMPORTACIÓN DE ASISTENCIAS                  ║
╚══════════════════════════════════════════════════════════════════╝

Total de líneas en el archivo: 3
Líneas procesadas exitosamente: 3
Líneas con errores (no procesadas): 0

═══════════════════════════════════════════════════════════════════
Presione Enter para volver al menú principal...
═══════════════════════════════════════════════════════════════════
Ejemplo 3 - Archivo con líneas vacías (deben ignorarse):

id_estudiante;id_materia;fecha;estado
1;5;2024-10-07;Presente

2;5;2024-10-07;Ausente

3;8;2024-10-07;Tardanza
Resultado esperado:

╔══════════════════════════════════════════════════════════════════╗
║           REPORTE DE IMPORTACIÓN DE ASISTENCIAS                  ║
╚══════════════════════════════════════════════════════════════════╝

Total de líneas en el archivo: 3
Líneas procesadas exitosamente: 3
Líneas con errores (no procesadas): 0

═══════════════════════════════════════════════════════════════════
Presione Enter para volver al menú principal...
═══════════════════════════════════════════════════════════════════
(Las líneas vacías se ignoran y no se cuentan en el total)

9. Consideraciones Importantes
   Sobre el Procesamiento Línea por Línea:

Cada línea es independiente de las demás
Una línea con error NO debe detener el procesamiento de las líneas siguientes
Esto permite que los registros válidos se inserten aunque haya algunos con errores
Es responsabilidad del usuario revisar y corregir las líneas con errores para una nueva importación
Sobre los Valores del Campo Estado:

Los cuatro valores permitidos están definidos en el ENUM de la base de datos
Deben escribirse exactamente como se especifica: "Presente", "Ausente", "Tardanza", "Excusa Médica"
Cualquier variación (mayúsculas/minúsculas diferentes, sin tildes, abreviaciones) es inválida
Ejemplo de valores INCORRECTOS: "presente", "AUSENTE", "Excusa Medica", "Tarde", "Justificado"
Sobre el Manejo de Errores:

El programa debe continuar procesando incluso si encuentra líneas con errores
No mostrar mensajes de error individuales por cada línea fallida durante el proceso (esto haría lenta la importación)
Al final mostrar solo el resumen con los contadores
El programa no debe fallar con excepciones no controladas; todos los errores deben manejarse elegantemente
Sobre el Archivo de Ejemplo:

Debes incluir un archivo asistencias_importar.csv de ejemplo en la raíz de tu repositorio
Este archivo debe contener datos válidos que funcionen con tu base de datos
El archivo de ejemplo servirá para que el evaluador pueda probar tu funcionalidad inmediatamente




Resultado esperado


1. Código Fuente y Estructura del Proyecto
   Nuevas Clases o Modificaciones:

Se espera que toda la lógica para la importación de asistencias esté correctamente organizada en tu arquitectura MVC. Dependiendo de tu implementación, esto podría implicar:

Crear una nueva clase de servicio o controlador dentro del módulo académico (ej: AttendanceImportService, AttendanceImportController o similar)
Modificar la clase de vista del menú principal para añadir la opción "TEST - Importar Asistencias desde CSV"
Si tu sistema tiene login, modificar la lógica de autenticación para permitir omitir el login con Enter
Opcionalmente, crear clases auxiliares para validación de datos o procesamiento de CSV
La ubicación exacta dependerá de cómo hayas organizado tu arquitectura MVC, pero debe respetar la separación de responsabilidades.

Archivo de Ejemplo Obligatorio:

Debes incluir en la raíz de tu repositorio un archivo llamado asistencias_importar.csv con datos de prueba válidos que funcionen con tu base de datos. Este archivo debe contener al menos 10 registros de asistencia que puedan ser importados exitosamente.

Nota sobre el Procesamiento:

Aunque puedes resolver el procesamiento del archivo usando bucles tradicionales (for, while), el manejo de archivos debe seguir buenas prácticas: uso de try-with-resources para garantizar el cierre de recursos, manejo apropiado de excepciones de I/O, y validación robusta de datos.


2. Gestión de Versiones con Git y GitHub
   Debes crear una rama llamada feat/attendance-csv-import con dos commits y hacer merge de esta rama a la rama main o master.

Primer Commit:

feat: implement CSV attendance import with line-by-line validation
Segundo Commit:

feat: add attendance import to main menu and display results