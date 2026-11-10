package biblioteca.tests;

import biblioteca.Biblioteca;
import biblioteca.data.ArchivoManager;
import biblioteca.model.Docente;
import biblioteca.model.Estudiante;
import biblioteca.model.Libro;
import biblioteca.model.Prestamo;
import biblioteca.model.Usuario;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ArchivoManagerPersistenciaTest {
    private static int comprobaciones;

    public static void main(String[] args) throws Exception {
        probarArchivosInexistentesYVacios();
        probarPersistenciaYReinicio();
        probarRegistrosInvalidos();
        probarErroresDeLecturaYEscritura();
        probarCoherenciaEntreEntidades();
        probarEdicionNoRompePrestamoActivo();
        System.out.println("Pruebas de persistencia correctas: " + comprobaciones + " comprobaciones.");
    }

    private static void probarArchivosInexistentesYVacios() throws Exception {
        Path directorio = Files.createTempDirectory("biblioteca-vacia-test-");
        try {
            ArchivoManager manager = new ArchivoManager(directorio);
            comprobar(manager.cargarUsuarios().isEmpty(), "Archivos de usuarios inexistentes");
            comprobar(manager.cargarLibros().isEmpty(), "Archivo de libros inexistente");
            comprobar(manager.cargarPrestamos().isEmpty(), "Archivo de préstamos inexistente");
            new Biblioteca(manager);

            manager.guardarUsuarios(new ArrayList<>());
            manager.guardarLibros(new ArrayList<>());
            manager.guardarPrestamos(new ArrayList<>());
            comprobar(manager.cargarUsuarios().isEmpty(), "CSV de usuarios solo con encabezado");
            comprobar(manager.cargarLibros().isEmpty(), "CSV de libros solo con encabezado");
            comprobar(manager.cargarPrestamos().isEmpty(), "CSV de préstamos solo con encabezado");

            Files.writeString(directorio.resolve("usuarios.csv"), "");
            comprobar(manager.cargarUsuarios().isEmpty(), "CSV vacío se interpreta como colección vacía");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarPersistenciaYReinicio() throws Exception {
        Path directorio = Files.createTempDirectory("biblioteca-reinicio-test-");
        try {
            ArchivoManager manager = new ArchivoManager(directorio);
            Estudiante estudiante = new Estudiante(
                "Ana Pérez", "12345678-5", 'F', "Ingeniería, Informática\nCampus Norte"
            );
            estudiante.setPrestamo("9780000000001");
            Docente docente = new Docente(
                "Luis Soto", "98765432-5", 'M', "Ingeniería", "Magíster, Doctorado"
            );
            Libro libroPrestado = new Libro(
                "9780000000001", "Título, con coma\ny segunda línea", "Autor Uno", 2, 1, ""
            );
            Libro libroDisponible = new Libro(
                "9780000000002", "Libro disponible", "Autora Dos", 1, 1, "portada.png"
            );
            Prestamo prestamo = new Prestamo("12345678-5", "9780000000001",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 11), true);

            manager.guardarUsuarios(new ArrayList<>(List.of(estudiante, docente)));
            manager.guardarLibros(new ArrayList<>(List.of(libroPrestado, libroDisponible)));
            manager.guardarPrestamos(new ArrayList<>(List.of(prestamo)));

            ArchivoManager managerTrasReinicio = new ArchivoManager(directorio);
            ArrayList<Usuario> usuarios = managerTrasReinicio.cargarUsuarios();
            ArrayList<Libro> libros = managerTrasReinicio.cargarLibros();
            ArrayList<Prestamo> prestamos = managerTrasReinicio.cargarPrestamos();
            managerTrasReinicio.validarCoherencia(usuarios, libros, prestamos);
            comprobar(usuarios.size() == 2, "Se recuperan todos los usuarios");
            comprobar(usuarios.get(0) instanceof Estudiante, "Se recupera el subtipo estudiante");
            comprobar(((Estudiante) usuarios.get(0)).getCarrera().equals(estudiante.getCarrera()),
                "Se recupera carrera con coma y salto de línea");
            comprobar(usuarios.get(1) instanceof Docente, "Se recupera el subtipo docente");
            comprobar(((Docente) usuarios.get(1)).getGradosAcademicos().equals("Magíster, Doctorado"),
                "Se recuperan grados con coma");
            comprobar(libros.get(0).getTitulo().equals(libroPrestado.getTitulo()),
                "Se recupera título con coma y salto de línea");
            comprobar(prestamos.size() == 1 && prestamos.get(0).isActivo(),
                "Se recupera el préstamo activo");
            comprobar("12345678-5".equals(usuarios.get(0).getRun()), "RUN persiste normalizado");
            new Biblioteca(managerTrasReinicio);
            comprobar(true, "Biblioteca reinicia con datos coherentes");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarRegistrosInvalidos() throws Exception {
        Path directorio = Files.createTempDirectory("biblioteca-invalidos-test-");
        try {
            ArchivoManager manager = new ArchivoManager(directorio);
            Path usuarios = directorio.resolve("usuarios.csv");
            Files.writeString(usuarios,
                "tipo,nombreCompleto,run,genero,prestamo,carrera,profesion,gradosAcademicos\n"
                    + "ESTUDIANTE,Registro incompleto,12345678-5,F\n");
            esperarError(() -> manager.cargarUsuarios(), "Registro incompleto de usuario");

            Files.writeString(usuarios,
                "tipo,nombreCompleto,run,genero,prestamo,carrera,profesion,gradosAcademicos\n"
                    + "ESTUDIANTE,Usuario,12345678-5,F,0,Carrera,,\n"
                    + "ESTUDIANTE,Usuario Dos,87654321-3,F,0,Carrera,,\n");
            esperarError(() -> manager.cargarUsuarios(), "Dígito verificador inválido en usuario");

            Path libros = directorio.resolve("libros.csv");
            Files.writeString(libros,
                "isbn,titulo,autor,cantidadBiblioteca,cantidadDisponible,imagen\n"
                    + "9780000000001,Libro,Autor,abc,1,\n");
            esperarError(() -> manager.cargarLibros(), "Cantidad no numérica en libro");
            Files.writeString(libros,
                "isbn,titulo,autor,cantidadBiblioteca,cantidadDisponible,imagen\n"
                    + "9780000000001,Libro,Autor,2,3,\n");
            esperarError(() -> manager.cargarLibros(), "Disponibles mayores que el total");

            Path prestamos = directorio.resolve("prestamos.csv");
            Files.writeString(prestamos,
                "run,isbn,fechaPrestamo,fechaDevolucion,activo\n"
                    + "12345678-5,9780000000001,2026-11-01,fecha,true\n");
            esperarError(() -> manager.cargarPrestamos(), "Fecha de préstamo inválida");
            Files.writeString(prestamos,
                "run,isbn,fechaPrestamo,fechaDevolucion,activo\n"
                    + "12345678-5,9780000000001,2026-11-01,2026-11-10,quizás\n");
            esperarError(() -> manager.cargarPrestamos(), "Estado no booleano en préstamo");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarErroresDeLecturaYEscritura() throws Exception {
        Path directorio = Files.createTempDirectory("biblioteca-errores-test-");
        try {
            ArchivoManager manager = new ArchivoManager(directorio);
            Path archivoUsuarios = directorio.resolve("usuarios.csv");
            Files.createDirectory(archivoUsuarios);
            Files.writeString(archivoUsuarios.resolve("respaldo.txt"), "dato original");
            esperarError(() -> manager.cargarUsuarios(), "Error de lectura propagado");
            esperarError(() -> manager.guardarUsuarios(new ArrayList<>()),
                "Error de escritura propagado");
            comprobar(Files.readString(archivoUsuarios.resolve("respaldo.txt")).equals("dato original"),
                "Fallo de escritura conserva el destino original");
            try (Stream<Path> rutas = Files.list(directorio)) {
                comprobar(rutas.noneMatch(ruta -> ruta.getFileName().toString().endsWith(".tmp")),
                    "Fallo de escritura limpia el archivo temporal");
            }
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarCoherenciaEntreEntidades() throws Exception {
        Path directorio = Files.createTempDirectory("biblioteca-coherencia-test-");
        try {
            ArchivoManager manager = new ArchivoManager(directorio);
            Usuario usuario = new Estudiante("Prueba", "12345678-5", 'F', "Informática");
            Libro libro = new Libro("9780000000001", "Libro", "Autor", 2, 1, "");
            Prestamo activo = new Prestamo("12345678-5", "9780000000001",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10), true);
            usuario.setPrestamo("9780000000001");
            manager.validarCoherencia(List.of(usuario), List.of(libro), List.of(activo));
            comprobar(true, "Referencias e inventario coherentes aceptados");

            esperarError(() -> manager.validarCoherencia(List.of(usuario), List.of(libro),
                List.of(new Prestamo("87654321-4", "9780000000001", activo.getFechaPrestamo(),
                    activo.getFechaDevolucion(), true))), "Préstamo activo con RUN inexistente");
            esperarError(() -> manager.validarCoherencia(List.of(usuario), List.of(libro),
                List.of(new Prestamo("12345678-5", "9780000000002", activo.getFechaPrestamo(),
                    activo.getFechaDevolucion(), true))), "Préstamo activo con ISBN inexistente");
            usuario.setPrestamo("0");
            esperarError(() -> manager.validarCoherencia(List.of(usuario), List.of(libro),
                List.of(activo)), "Préstamo activo no coincide con usuario");
            usuario.setPrestamo("9780000000001");
            libro.setCantidadDisponible(2);
            esperarError(() -> manager.validarCoherencia(List.of(usuario), List.of(libro),
                List.of(activo)), "Disponibilidad no coincide con préstamos activos");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarEdicionNoRompePrestamoActivo() throws Exception {
        Path directorio = Files.createTempDirectory("biblioteca-edicion-activa-test-");
        try {
            ArchivoManager manager = new ArchivoManager(directorio);
            Usuario usuario = new Estudiante("Prueba", "12345678-5", 'F', "Informática");
            usuario.setPrestamo("9780000000001");
            Libro libro = new Libro("9780000000001", "Libro", "Autor", 1, 0, "");
            Prestamo prestamo = new Prestamo("12345678-5", "9780000000001",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10), true);
            manager.guardarUsuarios(new ArrayList<>(List.of(usuario)));
            manager.guardarLibros(new ArrayList<>(List.of(libro)));
            manager.guardarPrestamos(new ArrayList<>(List.of(prestamo)));

            Biblioteca biblioteca = new Biblioteca(manager);
            comprobar(!biblioteca.editarUsuario("12345678-5", new Estudiante(
                "RUN cambiado", "87654321-4", 'F', "Informática"
            )), "Servicio bloquea cambiar RUN con préstamo activo");
            comprobar(biblioteca.editarUsuario("12345678-5", new Estudiante(
                "Nombre actualizado", "12345678-5", 'F', "Ciencias"
            )), "Servicio permite editar datos manteniendo RUN activo");
            new Biblioteca(new ArchivoManager(directorio));
            comprobar(true, "Edición activa persiste coherente al reiniciar");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void esperarError(OperacionConError operacion, String descripcion) {
        try {
            operacion.ejecutar();
        } catch (IOException e) {
            comprobar(true, descripcion);
            return;
        }
        throw new AssertionError(descripcion + ": no se informó el error");
    }

    private static void comprobar(boolean condicion, String descripcion) {
        if (!condicion) {
            throw new AssertionError(descripcion);
        }
        comprobaciones++;
    }

    private static void eliminarDirectorioTemporal(Path directorio) throws IOException {
        try (Stream<Path> rutas = Files.walk(directorio)) {
            rutas.sorted(Comparator.reverseOrder()).forEach(ruta -> {
                try {
                    Files.deleteIfExists(ruta);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @FunctionalInterface
    private interface OperacionConError {
        void ejecutar() throws IOException;
    }
}
