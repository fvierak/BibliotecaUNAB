package biblioteca.tests;

import biblioteca.Biblioteca;
import biblioteca.data.ArchivoManager;
import biblioteca.model.Docente;
import biblioteca.model.Estudiante;
import biblioteca.model.Usuario;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class BibliotecaUsuariosTest {
    private static int comprobaciones;

    public static void main(String[] args) throws Exception {
        probarValidacionRun();
        probarValidacionGenero();
        probarCrudUsuarios();
        probarCargaRechazaRunsDuplicados();
        probarErrorDeGuardadoNoActualizaMemoria();
        System.out.println("Pruebas de usuarios correctas: " + comprobaciones + " comprobaciones.");
    }

    private static void probarValidacionRun() {
        comprobar(Usuario.validarRun("12345678-5"), "RUN válido sin puntos");
        comprobar(Usuario.validarRun("12.345.678-5"), "RUN válido con puntos");
        comprobar(Usuario.validarRun("91.111.111-k"), "RUN válido con K minúscula");
        comprobar("91111111-K".equals(Usuario.normalizarRun("91.111.111-k")),
            "Normalización del RUN");
        comprobar(!Usuario.validarRun("123456785"), "Formato sin guion rechazado");
        comprobar(!Usuario.validarRun("123456-0"), "RUN demasiado corto rechazado");
        comprobar(!Usuario.validarRun("12345678-4"), "Dígito verificador incorrecto rechazado");
        comprobar(!Usuario.validarRun("1.2345.678-5"), "Agrupación de puntos incorrecta rechazada");
        comprobar(!Usuario.validarRun(null), "RUN nulo rechazado");

        esperarError(() -> new Estudiante("Prueba", "12345678-4", 'M', "Informática"),
            "Constructor rechaza RUN inválido");
    }

    private static void probarValidacionGenero() {
        Estudiante estudiante = new Estudiante("Prueba", "12345678-5", 'm', "Informática");
        comprobar(estudiante.getGenero() == 'M', "Género minúsculo normalizado");
        comprobar(!Usuario.validarGenero('X'), "Género fuera de M/F rechazado");
        esperarError(() -> new Docente("Prueba", "98765432-5", 'X', "Docencia", "Magíster"),
            "Constructor rechaza género inválido");
        esperarError(() -> estudiante.setGenero('X'), "Setter rechaza género inválido");
        esperarError(() -> estudiante.setRun("12345678-4"), "Setter rechaza RUN inválido");
        comprobar(estudiante.getRun().equals("12345678-5"), "Setter inválido no altera el RUN");
    }

    private static void probarCrudUsuarios() throws Exception {
        Path directorioTemporal = Files.createTempDirectory("biblioteca-usuarios-test-");
        try {
            ArchivoManager archivoManager = new ArchivoManager(directorioTemporal);
            Biblioteca biblioteca = new Biblioteca(archivoManager);
            Estudiante estudiante = new Estudiante(
                "Ana Pérez", "12345678-5", 'F', "Ingeniería Informática"
            );
            Docente docente = new Docente(
                "Luis Soto", "98765432-5", 'M', "Ingeniería", "Doctorado"
            );

            comprobar(biblioteca.agregarUsuario(estudiante), "Crear estudiante");
            comprobar(!biblioteca.agregarUsuario(new Estudiante(
                "Duplicada", "12.345.678-5", 'F', "Otra carrera"
            )), "RUN duplicado con formato equivalente rechazado");
            comprobar(biblioteca.agregarUsuario(docente), "Crear docente");
            comprobar(biblioteca.buscarUsuario("12.345.678-5") == estudiante,
                "Búsqueda acepta RUN con puntos");
            comprobar(!biblioteca.editarUsuario("91111111-K", estudiante),
                "Editar RUN inexistente rechazado");

            estudiante.setPrestamo("9780000000000");
            Estudiante estudianteEditado = new Estudiante(
                "Ana Pérez Actualizada", "87654321-4", 'f', "Ciencias"
            );
            comprobar(biblioteca.editarUsuario("12345678-5", estudianteEditado),
                "Editar usuario existente");
            comprobar(estudianteEditado.getPrestamo().equals("9780000000000"),
                "Edición conserva el préstamo activo");
            comprobar(biblioteca.buscarUsuario("12345678-5") == null,
                "RUN anterior deja de estar registrado");
            comprobar(biblioteca.buscarUsuario("87654321-4") instanceof Estudiante,
                "Búsqueda recupera el subtipo estudiante");
            comprobar(!biblioteca.editarUsuario("87654321-4", new Docente(
                "Duplicado", "98765432-5", 'M', "Ingeniería", "Doctorado"
            )), "Edición con RUN duplicado rechazada");

            comprobar(!biblioteca.eliminarUsuario("87654321-4"),
                "Eliminación con préstamo activo bloqueada");
            comprobar(!biblioteca.eliminarUsuario("91111111-K"),
                "Eliminar RUN inexistente no modifica datos");

            List<Usuario> usuariosPersistidos = archivoManager.cargarUsuarios();
            comprobar(usuariosPersistidos.size() == 2, "Persistencia conserva ambos usuarios");
            comprobar(usuariosPersistidos.get(0) instanceof Estudiante,
                "Persistencia conserva el subtipo estudiante");
            comprobar(((Estudiante) usuariosPersistidos.get(0)).getCarrera().equals("Ciencias"),
                "Persistencia conserva la carrera editada");
            comprobar(usuariosPersistidos.get(1) instanceof Docente,
                "Persistencia conserva el subtipo docente");
            comprobar(((Docente) usuariosPersistidos.get(1)).getGradosAcademicos().equals("Doctorado"),
                "Persistencia conserva los grados académicos");

            estudianteEditado.setPrestamo("0");
            comprobar(biblioteca.eliminarUsuario("87654321-4"),
                "Eliminar usuario sin préstamo activo");
            comprobar(biblioteca.eliminarUsuario("98765432-5"), "Eliminar docente existente");
            comprobar(archivoManager.cargarUsuarios().isEmpty(),
                "Eliminaciones persisten y dejan la lista vacía");
        } finally {
            eliminarDirectorioTemporal(directorioTemporal);
        }
    }

    private static void probarCargaRechazaRunsDuplicados() throws Exception {
        Path directorioTemporal = Files.createTempDirectory("biblioteca-duplicados-test-");
        try {
            String encabezado = "tipo,nombreCompleto,run,genero,prestamo,carrera,profesion,gradosAcademicos";
            String registro = "ESTUDIANTE,Prueba,12345678-5,F,0,Informática,,";
            Files.writeString(directorioTemporal.resolve("usuarios.csv"),
                encabezado + System.lineSeparator() + registro + System.lineSeparator()
                    + registro + System.lineSeparator());

            try {
                new Biblioteca(new ArchivoManager(directorioTemporal));
                throw new AssertionError("La carga aceptó RUN duplicados");
            } catch (IOException e) {
                comprobar(e.getMessage().contains("RUN duplicado"),
                    "Carga rechaza RUN duplicado en CSV");
            }
        } finally {
            eliminarDirectorioTemporal(directorioTemporal);
        }
    }

    private static void probarErrorDeGuardadoNoActualizaMemoria() throws Exception {
        Path directorioTemporal = Files.createTempDirectory("biblioteca-guardado-test-");
        try {
            Biblioteca biblioteca = new Biblioteca(new ArchivoManager(directorioTemporal));
            Files.createDirectory(directorioTemporal.resolve("usuarios.csv"));

            try {
                biblioteca.agregarUsuario(new Estudiante(
                    "Prueba", "12345678-5", 'F', "Informática"
                ));
                throw new AssertionError("El guardado debió fallar al escribir sobre un directorio");
            } catch (IOException e) {
                comprobar(biblioteca.buscarUsuario("12345678-5") == null,
                    "Error de guardado no altera la colección en memoria");
            }
        } finally {
            eliminarDirectorioTemporal(directorioTemporal);
        }
    }

    private static void esperarError(Runnable operacion, String descripcion) {
        try {
            operacion.run();
        } catch (IllegalArgumentException e) {
            comprobar(true, descripcion);
            return;
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
}
