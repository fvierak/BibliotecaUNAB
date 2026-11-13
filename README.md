# Biblioteca UNAB

Aplicación académica de gestión bibliotecaria desarrollada en Java con POO, Swing y persistencia en archivos CSV.

## Requisitos

- JDK 17 o posterior.
- Visual Studio Code con soporte para Java, o terminal con `javac` y `java`.
- No requiere frameworks, base de datos ni dependencias externas.

## Estructura

```text
src/biblioteca/
  Biblioteca.java             Reglas y coordinación de usuarios, libros y préstamos
  data/ArchivoManager.java    Lectura, validación y escritura CSV
  main/Main.java              Inicio de Swing y carga de datos
  model/                      Usuario, Estudiante, Docente, Libro y Prestamo
  view/                       Ventanas y diálogos Swing
tests/biblioteca/tests/       Pruebas ejecutables sin framework
data/                         CSV utilizados por la aplicación
docs/                         Guía, fuentes oficiales e informes del proyecto
```

## Compilar y ejecutar

Desde la raíz del proyecto, en PowerShell:

```powershell
$salida = Join-Path $env:TEMP "BibliotecaUNAB-clases"
New-Item -ItemType Directory -Force $salida | Out-Null
$fuentes = Get-ChildItem src -Recurse -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 -d $salida $fuentes
java -cp $salida biblioteca.main.Main
```

La aplicación lee y escribe los CSV del directorio `data` relativo al directorio de trabajo. Verifica los datos al iniciar y muestra un error si encuentra archivos inválidos o referencias inconsistentes.

## Funcionalidades

- Usuarios estudiante/docente: alta, búsqueda, edición y eliminación con validación de RUN chileno, género y préstamo activo.
- Libros: alta, búsqueda y eliminación con ISBN único y cantidades válidas.
- Préstamos: duración ingresada por el usuario, con máximo de 10 días para estudiantes y 20 para docentes; fechas automáticas, actualización de disponibilidad y tarjeta imprimible.
- Devoluciones: actualización de usuario, inventario e historial; multa de CLP 1.000 por día de atraso.
- Persistencia CSV con validación de columnas, escape de comas/comillas/saltos de línea y reemplazo seguro mediante archivo temporal.

## Interfaz

El Home abre diálogos modales para las operaciones. Los formularios delegan las reglas de negocio a `Biblioteca`. La tarjeta del préstamo puede visualizarse e imprimirse mediante la impresión estándar de Swing.

## Pruebas

Las pruebas son clases Java ejecutables que crean directorios temporales; no deben escribirse datos ficticios en los CSV reales.

```powershell
$salida = Join-Path $env:TEMP "BibliotecaUNAB-pruebas"
New-Item -ItemType Directory -Force $salida | Out-Null
$fuentes = @(Get-ChildItem src -Recurse -Filter *.java | ForEach-Object FullName) + @(Get-ChildItem tests -Recurse -Filter *.java | ForEach-Object FullName)
javac -encoding UTF-8 -d $salida $fuentes
java -cp $salida biblioteca.tests.BibliotecaUsuariosTest
java -cp $salida biblioteca.tests.ArchivoManagerPersistenciaTest
java -cp $salida biblioteca.tests.BibliotecaLibrosTest
java -cp $salida biblioteca.tests.BibliotecaPrestamosTest
```

## Limitaciones y entrega

- La interfaz Swing debe probarse manualmente además de las pruebas automatizadas del núcleo.
- Los archivos de instrucciones oficiales piden una carpeta completa de proyecto generada por NetBeans. El desarrollo de este repositorio se mantiene en VS Code, según la decisión del equipo. No se migró; confirmar con el docente si se acepta el proyecto actual.
- La rúbrica menciona `customers`, arriendos y 14 aspectos no definidos para biblioteca. Esta discrepancia se documenta en `docs/matriz_cumplimiento.md`; no se implementaron funcionalidades ajenas al caso.
- Completar nombres reales del equipo en la plantilla de entrega antes de presentar.