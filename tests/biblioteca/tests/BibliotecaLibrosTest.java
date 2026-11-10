package biblioteca.tests;

import biblioteca.Biblioteca;
import biblioteca.data.ArchivoManager;
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

public class BibliotecaLibrosTest {
    private static int comprobaciones;

    public static void main(String[] args) throws Exception {
        probarGestionYPersistencia();
        probarValidacionesDeCreacion();
        probarEliminacionConPrestamoActivo();
        probarFalloDeGuardado();
        System.out.println("Pruebas de libros correctas: " + comprobaciones + " comprobaciones.");
    }

    private static void probarGestionYPersistencia() throws Exception {
        Path directorio = Files.createTempDirectory("biblioteca-libros-test-");
        try {
            ArchivoManager manager = new ArchivoManager(directorio);
            Biblioteca biblioteca = new Biblioteca(manager);
            Libro libro = new Libro(" 9780000000001 ", "Título, uno", "Autora", 3, 2, "portada.png");

            comprobar(biblioteca.agregarLibro(libro), "Crear libro válido");
            comprobar("9780000000001".equals(libro.getIsbn()), "ISBN se guarda normalizado");
            comprobar(!biblioteca.agregarLibro(new Libro(
                "9780000000001", "Duplicado", "Otro autor", 1, 1, ""
            )), "ISBN duplicado rechazado");
            comprobar(biblioteca.buscarLibro(" 9780000000001 ") == libro,
                "Búsqueda por ISBN tolera espacios externos");
            comprobar(biblioteca.buscarLibro(null) == null, "Búsqueda con ISBN nulo retorna vacío");

            Biblioteca reiniciada = new Biblioteca(new ArchivoManager(directorio));
            Libro recuperado = reiniciada.buscarLibro("9780000000001");
            comprobar(recuperado != null, "Libro se recupera después de reiniciar");
            comprobar(recuperado.getTitulo().equals("Título, uno"), "CSV conserva comas en el título");
            comprobar(recuperado.getImagen().equals("portada.png"), "CSV conserva la imagen");
            comprobar(recuperadaDisponible(recuperado), "CSV conserva las cantidades");
            comprobar(!reiniciada.eliminarLibro("9789999999999"),
                "ISBN inexistente no se elimina");
            comprobar(reiniciada.eliminarLibro("9780000000001"), "Eliminar libro existente");
            comprobar(new ArchivoManager(directorio).cargarLibros().isEmpty(),
                "Eliminación se persiste en CSV");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarValidacionesDeCreacion() throws Exception {
        Path directorio = Files.createTempDirectory("biblioteca-libros-validacion-test-");
        try {
            Biblioteca biblioteca = new Biblioteca(new ArchivoManager(directorio));
            esperarError(() -> biblioteca.agregarLibro(new Libro(
                "9780000000001", "Total cero", "Autor", 0, 0, ""
            )), "Cantidad total cero rechazada");
            esperarError(() -> biblioteca.agregarLibro(new Libro(
                "9780000000002", "Sin disponibles", "Autor", 2, 0, ""
            )), "Cantidad disponible cero al crear rechazada");
            esperarError(() -> biblioteca.agregarLibro(new Libro(
                "9780000000003", "Exceso", "Autor", 2, 3, ""
            )), "Disponibilidad mayor que el total rechazada");
            esperarError(() -> biblioteca.agregarLibro(new Libro(
                "", "Sin ISBN", "Autor", 1, 1, ""
            )), "ISBN vacío rechazado");
            comprobar(new ArchivoManager(directorio).cargarLibros().isEmpty(),
                "Libros inválidos no alteran la persistencia");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarEliminacionConPrestamoActivo() throws Exception {
        Path directorio = Files.createTempDirectory("biblioteca-libros-prestados-test-");
        try {
            ArchivoManager manager = new ArchivoManager(directorio);
            Usuario usuario = new Estudiante("Prueba", "12345678-5", 'F', "Informática");
            usuario.setPrestamo("9780000000001");
            Libro libro = new Libro("9780000000001", "Libro prestado", "Autor", 1, 0, "");
            Prestamo prestamo = new Prestamo("12345678-5", "9780000000001",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10), true);
            manager.guardarUsuarios(new ArrayList<>(List.of(usuario)));
            manager.guardarLibros(new ArrayList<>(List.of(libro)));
            manager.guardarPrestamos(new ArrayList<>(List.of(prestamo)));

            Biblioteca biblioteca = new Biblioteca(manager);
            comprobar(!biblioteca.eliminarLibro("9780000000001"),
                "Libro con préstamo activo no se elimina");
            comprobar(biblioteca.buscarLibro("9780000000001") != null,
                "Libro prestado permanece en memoria");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarFalloDeGuardado() throws Exception {
        Path directorio = Files.createTempDirectory("biblioteca-libros-guardado-test-");
        try {
            Biblioteca biblioteca = new Biblioteca(new ArchivoManager(directorio));
            Path destino = directorio.resolve("libros.csv");
            Files.createDirectory(destino);
            Files.writeString(destino.resolve("respaldo.txt"), "contenido original");

            try {
                biblioteca.agregarLibro(new Libro(
                    "9780000000001", "Libro", "Autor", 1, 1, "imagen.png"
                ));
                throw new AssertionError("El guardado debía fallar");
            } catch (IOException e) {
                comprobar(biblioteca.buscarLibro("9780000000001") == null,
                    "Fallo de guardado no actualiza la lista en memoria");
            }
            comprobar(Files.readString(destino.resolve("respaldo.txt")).equals("contenido original"),
                "Fallo de escritura conserva el destino");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static boolean recuperadaDisponible(Libro libro) {
        return libro.getCantidadBiblioteca() == 3 && libro.getCantidadDisponible() == 2;
    }

    private static void esperarError(OperacionConError operacion, String descripcion) {
        try {
            operacion.ejecutar();
        } catch (IllegalArgumentException e) {
            comprobar(true, descripcion);
            return;
        } catch (IOException e) {
            throw new AssertionError(descripcion + ": error inesperado de persistencia", e);
        }
        throw new AssertionError(descripcion + ": no se rechazó la operación");
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
