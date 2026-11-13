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

public class BibliotecaPrestamosTest {
    private static int comprobaciones;

    public static void main(String[] args) throws Exception {
        probarPrestamoYPersistencia();
        probarRechazos();
        probarRollbackSiFallaElGuardado();
        probarDevolucionEnPlazo();
        probarMultaPorAtraso();
        probarRechazosDeDevolucion();
        probarRollbackDeDevolucion();
        System.out.println("Pruebas de préstamos correctas: " + comprobaciones + " comprobaciones.");
    }

    private static void probarPrestamoYPersistencia() throws Exception {
        Path directorio = crearDirectorioConDatosIniciales();
        try {
            Biblioteca biblioteca = new Biblioteca(new ArchivoManager(directorio));
            Prestamo prestamo = biblioteca.realizarPrestamo(" 12.345.678-5 ", " 9780000000001 ", 7);

            comprobar(prestamo.isActivo(), "El préstamo creado queda activo");
            comprobar(prestamo.getRun().equals("12345678-5"), "El RUN se guarda normalizado");
            comprobar(prestamo.getIsbn().equals("9780000000001"), "El ISBN se guarda normalizado");
            comprobar(prestamo.getFechaPrestamo().equals(LocalDate.now()), "Fecha de préstamo automática");
            comprobar(prestamo.getFechaDevolucion().equals(LocalDate.now().plusDays(7)),
                "Vencimiento según duración pedida");
            comprobar(biblioteca.buscarUsuario("12345678-5").getPrestamo().equals("9780000000001"),
                "El usuario queda asociado al ISBN");
            comprobar(biblioteca.buscarLibro("9780000000001").getCantidadDisponible() == 1,
                "Se descuenta un ejemplar disponible");

            String tarjeta = prestamo.generarTarjeta();
            comprobar(tarjeta.contains("12345678-5") && tarjeta.contains("9780000000001"),
                "La tarjeta contiene RUN e ISBN");
            comprobar(tarjeta.contains(prestamo.getFechaPrestamo().toString())
                && tarjeta.contains(prestamo.getFechaDevolucion().toString()),
                "La tarjeta contiene las dos fechas");
            String tarjetaImprimible = prestamo.generarTarjeta("Libro de prueba");
            comprobar(tarjetaImprimible.contains("Libro de prueba")
                && tarjetaImprimible.contains("Plazo: 7 días"),
                "La tarjeta contiene título y plazo para impresión");
            comprobar(prestamo.generarTarjeta("Libro de prueba", "Ana Pérez")
                .contains("Usuario: Ana Pérez"), "La tarjeta identifica al usuario por nombre");

            esperarError(() -> biblioteca.realizarPrestamo("12345678-5", "9780000000001", 1),
                "Segundo préstamo activo del usuario rechazado");

            Biblioteca reiniciada = new Biblioteca(new ArchivoManager(directorio));
            comprobar(reiniciada.buscarUsuario("12345678-5").getPrestamo().equals("9780000000001"),
                "El estado del usuario se recupera al reiniciar");
            comprobar(reiniciada.buscarLibro("9780000000001").getCantidadDisponible() == 1,
                "La disponibilidad se recupera al reiniciar");
            List<Prestamo> guardados = new ArchivoManager(directorio).cargarPrestamos();
            comprobar(guardados.size() == 1 && guardados.get(0).isActivo(),
                "El préstamo activo se recupera al reiniciar");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarRechazos() throws Exception {
        Path directorio = crearDirectorioConDatosIniciales();
        try {
            Biblioteca biblioteca = new Biblioteca(new ArchivoManager(directorio));
            esperarError(() -> biblioteca.realizarPrestamo("12345678-5", "9789999999999", 1),
                "Libro inexistente rechazado");
            esperarError(() -> biblioteca.realizarPrestamo("87654321-4", "9780000000001", 1),
                "Usuario inexistente rechazado");
            esperarError(() -> biblioteca.realizarPrestamo("12345678-5", "9780000000001", 0),
                "Duración cero rechazada");
            esperarError(() -> biblioteca.realizarPrestamo("12345678-5", "9780000000001", 11),
                "Estudiante no puede superar diez días");
            comprobar(biblioteca.buscarLibro("9780000000001").getCantidadDisponible() == 2,
                "Los rechazos no modifican disponibilidad");
            comprobar(biblioteca.buscarUsuario("12345678-5").getPrestamo().equals("0"),
                "Los rechazos no modifican el usuario");

            Usuario docente = new Docente("Docente", "98765432-5", 'F', "Docencia", "Magíster");
            comprobar(biblioteca.agregarUsuario(docente), "Agregar docente para probar límite");
            comprobar(biblioteca.realizarPrestamo("98765432-5", "9780000000001", 20).isActivo(),
                "Docente puede solicitar veinte días");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarRollbackSiFallaElGuardado() throws Exception {
        Path directorio = crearDirectorioConDatosIniciales();
        try {
            Biblioteca biblioteca = new Biblioteca(new ArchivoManager(directorio));
            Path archivoPrestamos = directorio.resolve("prestamos.csv");
            Files.delete(archivoPrestamos);
            Files.createDirectory(archivoPrestamos);
            Files.writeString(archivoPrestamos.resolve("respaldo.txt"), "no modificar");

            try {
                biblioteca.realizarPrestamo("12345678-5", "9780000000001", 5);
                throw new AssertionError("El guardado de préstamos debía fallar");
            } catch (IOException e) {
                comprobar(biblioteca.buscarUsuario("12345678-5").getPrestamo().equals("0"),
                    "El usuario se restaura en memoria");
                comprobar(biblioteca.buscarLibro("9780000000001").getCantidadDisponible() == 2,
                    "El inventario se restaura en memoria");
                comprobar(new ArchivoManager(directorio).cargarUsuarios().get(0)
                    .getPrestamo().equals("0"), "El CSV de usuarios se restaura");
                comprobar(new ArchivoManager(directorio).cargarLibros().get(0)
                    .getCantidadDisponible() == 2, "El CSV de libros se restaura");
                comprobar(Files.readString(archivoPrestamos.resolve("respaldo.txt")).equals("no modificar"),
                    "El destino que causó el fallo se conserva");
            }
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarDevolucionEnPlazo() throws Exception {
        Path directorio = crearDirectorioConPrestamoActivo(LocalDate.now());
        try {
            Biblioteca biblioteca = new Biblioteca(new ArchivoManager(directorio));
            long multa = biblioteca.realizarDevolucion("12345678-5", "9780000000001");

            comprobar(multa == 0, "Devolución en plazo no genera multa");
            comprobar(biblioteca.buscarUsuario("12345678-5").getPrestamo().equals("0"),
                "Devolución libera al usuario");
            comprobar(biblioteca.buscarLibro("9780000000001").getCantidadDisponible() == 3,
                "Devolución incrementa disponibilidad");

            Biblioteca reiniciada = new Biblioteca(new ArchivoManager(directorio));
            comprobar(reiniciada.buscarUsuario("12345678-5").getPrestamo().equals("0"),
                "Usuario liberado persiste al reiniciar");
            comprobar(reiniciada.buscarLibro("9780000000001").getCantidadDisponible() == 3,
                "Disponibilidad recuperada al reiniciar");
            comprobar(!new ArchivoManager(directorio).cargarPrestamos().get(0).isActivo(),
                "Préstamo queda inactivo en el CSV");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarMultaPorAtraso() throws Exception {
        Path directorio = crearDirectorioConPrestamoActivo(LocalDate.now().minusDays(3));
        try {
            Biblioteca biblioteca = new Biblioteca(new ArchivoManager(directorio));
            long multa = biblioteca.realizarDevolucion("12345678-5", "9780000000001");
            comprobar(multa == 3000, "Tres días de atraso generan multa de 3000 CLP");
            comprobar(biblioteca.buscarUsuario("12345678-5").getPrestamo().equals("0"),
                "Usuario queda liberado aunque tenga multa");
            comprobar(biblioteca.buscarLibro("9780000000001").getCantidadDisponible() == 3,
                "Ejemplar atrasado vuelve al inventario");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarRechazosDeDevolucion() throws Exception {
        Path directorio = crearDirectorioConPrestamoActivo(LocalDate.now());
        try {
            Biblioteca biblioteca = new Biblioteca(new ArchivoManager(directorio));
            esperarError(() -> biblioteca.realizarDevolucion("87654321-4", "9780000000001"),
                "RUN inexistente en devolución rechazado");
            esperarError(() -> biblioteca.realizarDevolucion("12345678-5", "9789999999999"),
                "ISBN inexistente en devolución rechazado");
            esperarError(() -> biblioteca.realizarDevolucion("12345678-5", "9780000000002"),
                "ISBN distinto al préstamo activo rechazado");
            comprobar(biblioteca.buscarUsuario("12345678-5").getPrestamo().equals("9780000000001"),
                "Devoluciones rechazadas conservan el préstamo");
            comprobar(biblioteca.buscarLibro("9780000000001").getCantidadDisponible() == 2,
                "Devoluciones rechazadas no alteran inventario");
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static void probarRollbackDeDevolucion() throws Exception {
        Path directorio = crearDirectorioConPrestamoActivo(LocalDate.now());
        try {
            ArchivoManager managerConFallo = new ArchivoManager(directorio) {
                @Override
                public void guardarPrestamos(ArrayList<Prestamo> prestamos) throws IOException {
                    throw new IOException("Fallo de escritura simulado");
                }
            };
            Biblioteca biblioteca = new Biblioteca(managerConFallo);

            try {
                biblioteca.realizarDevolucion("12345678-5", "9780000000001");
                throw new AssertionError("El guardado de devolución debía fallar");
            } catch (IOException e) {
                comprobar(biblioteca.buscarUsuario("12345678-5").getPrestamo()
                    .equals("9780000000001"), "Rollback conserva préstamo del usuario");
                comprobar(biblioteca.buscarLibro("9780000000001").getCantidadDisponible() == 2,
                    "Rollback restaura disponibilidad en memoria");
                comprobar(new ArchivoManager(directorio).cargarUsuarios().get(0)
                    .getPrestamo().equals("9780000000001"), "Rollback restaura CSV de usuarios");
                comprobar(new ArchivoManager(directorio).cargarLibros().get(0)
                    .getCantidadDisponible() == 2, "Rollback restaura CSV de libros");
                comprobar(new ArchivoManager(directorio).cargarPrestamos()
                    .get(0).isActivo(), "El préstamo activo queda sin cambios en el CSV");
            }
        } finally {
            eliminarDirectorioTemporal(directorio);
        }
    }

    private static Path crearDirectorioConPrestamoActivo(LocalDate fechaDevolucion)
            throws IOException {
        Path directorio = Files.createTempDirectory("biblioteca-devolucion-test-");
        ArchivoManager manager = new ArchivoManager(directorio);
        Usuario estudiante = new Estudiante("Estudiante", "12345678-5", 'F', "Informática");
        estudiante.setPrestamo("9780000000001");
        Libro libro = new Libro("9780000000001", "Libro", "Autor", 3, 2, "portada.png");
        Prestamo prestamo = new Prestamo("12345678-5", "9780000000001",
            fechaDevolucion.minusDays(10), fechaDevolucion, true);
        manager.guardarUsuarios(new ArrayList<>(List.of(estudiante)));
        manager.guardarLibros(new ArrayList<>(List.of(libro)));
        manager.guardarPrestamos(new ArrayList<>(List.of(prestamo)));
        return directorio;
    }

    private static Path crearDirectorioConDatosIniciales() throws IOException {
        Path directorio = Files.createTempDirectory("biblioteca-prestamo-test-");
        ArchivoManager manager = new ArchivoManager(directorio);
        Usuario estudiante = new Estudiante("Estudiante", "12345678-5", 'F', "Informática");
        Libro libro = new Libro("9780000000001", "Libro", "Autor", 3, 2, "portada.png");
        manager.guardarUsuarios(new ArrayList<>(List.of(estudiante)));
        manager.guardarLibros(new ArrayList<>(List.of(libro)));
        manager.guardarPrestamos(new ArrayList<>());
        return directorio;
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
