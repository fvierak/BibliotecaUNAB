package biblioteca.data;

import biblioteca.model.Usuario;
import biblioteca.model.Docente;
import biblioteca.model.Estudiante;
import biblioteca.model.Libro;
import biblioteca.model.Prestamo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PushbackReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ArchivoManager {

    private final Path directorioDatos;

    public ArchivoManager() {
        this(Paths.get("data"));
    }

    public ArchivoManager(Path directorioDatos) {
        if (directorioDatos == null) {
            throw new IllegalArgumentException("El directorio de datos es obligatorio");
        }
        this.directorioDatos = directorioDatos;
    }

    public ArrayList<Usuario> cargarUsuarios() throws IOException {
        Path ruta = directorioDatos.resolve("usuarios.csv");
        ArrayList<Usuario> usuarios = new ArrayList<>();
        Set<String> runsRegistrados = new HashSet<>();
        List<List<String>> registros = leerRegistros(ruta,
            List.of("tipo", "nombreCompleto", "run", "genero", "prestamo",
                "carrera", "profesion", "gradosAcademicos"));

        for (int i = 0; i < registros.size(); i++) {
            List<String> datos = registros.get(i);
            int numeroLinea = i + 2;
            exigirColumnas(datos, 8, ruta, numeroLinea);

            try {
                Usuario usuario;
                String tipo = datos.get(0).trim();
                if (tipo.equals("ESTUDIANTE")) {
                    exigirVacio(datos.get(6), ruta, numeroLinea, "profesion");
                    exigirVacio(datos.get(7), ruta, numeroLinea, "gradosAcademicos");
                    exigirTexto(datos.get(5), ruta, numeroLinea, "carrera");
                    usuario = new Estudiante(
                        datos.get(1), datos.get(2), caracter(datos.get(3), ruta, numeroLinea),
                        datos.get(5)
                    );
                } else if (tipo.equals("DOCENTE")) {
                    exigirVacio(datos.get(5), ruta, numeroLinea, "carrera");
                    exigirTexto(datos.get(6), ruta, numeroLinea, "profesion");
                    exigirTexto(datos.get(7), ruta, numeroLinea, "gradosAcademicos");
                    usuario = new Docente(
                        datos.get(1), datos.get(2), caracter(datos.get(3), ruta, numeroLinea),
                        datos.get(6), datos.get(7)
                    );
                } else {
                    throw errorRegistro(ruta, numeroLinea, "tipo de usuario desconocido");
                }

                String prestamo = datos.get(4);
                if (prestamo.isBlank()) {
                    throw errorRegistro(ruta, numeroLinea, "atributo préstamo vacío");
                }
                usuario.setPrestamo(prestamo);
                if (!runsRegistrados.add(usuario.getRun())) {
                    throw errorRegistro(ruta, numeroLinea, "RUN duplicado");
                }
                usuarios.add(usuario);
            } catch (IllegalArgumentException e) {
                throw new IOException("Datos inválidos en " + ruta + ", línea " + numeroLinea
                    + ": " + e.getMessage(), e);
            }
        }
        return usuarios;
    }

    public void guardarUsuarios(ArrayList<Usuario> usuarios) throws IOException {
        ArrayList<List<String>> registros = new ArrayList<>();
        Set<String> runsRegistrados = new HashSet<>();
        for (Usuario usuario : usuarios) {
            if (usuario == null || !runsRegistrados.add(usuario.getRun())) {
                throw new IOException("La lista contiene un usuario nulo o un RUN duplicado");
            }
            validarUsuario(usuario);

            if (usuario instanceof Estudiante estudiante) {
                registros.add(List.of("ESTUDIANTE", estudiante.getNombre(), estudiante.getRun(),
                    String.valueOf(estudiante.getGenero()), estudiante.getPrestamo(),
                    estudiante.getCarrera(), "", ""));
            } else if (usuario instanceof Docente docente) {
                registros.add(List.of("DOCENTE", docente.getNombre(), docente.getRun(),
                    String.valueOf(docente.getGenero()), docente.getPrestamo(), "",
                    docente.getProfesion(), docente.getGradosAcademicos()));
            } else {
                throw new IOException("Tipo de usuario no reconocido: " + usuario.getClass().getName());
            }
        }

        escribirRegistros(directorioDatos.resolve("usuarios.csv"),
            List.of("tipo", "nombreCompleto", "run", "genero", "prestamo", "carrera",
                "profesion", "gradosAcademicos"), registros);
    }

    public ArrayList<Libro> cargarLibros() throws IOException {
        ArrayList<Libro> libros = new ArrayList<>();
        Path ruta = directorioDatos.resolve("libros.csv");
        List<List<String>> registros = leerRegistros(ruta,
            List.of("isbn", "titulo", "autor", "cantidadBiblioteca",
                "cantidadDisponible", "imagen"));
        Set<String> isbnsRegistrados = new HashSet<>();

        for (int i = 0; i < registros.size(); i++) {
            List<String> datos = registros.get(i);
            int numeroLinea = i + 2;
            exigirColumnas(datos, 6, ruta, numeroLinea);
            try {
                String isbn = datos.get(0).trim();
                int total = Integer.parseInt(datos.get(3));
                int disponibles = Integer.parseInt(datos.get(4));
                if (isbn.isEmpty() || datos.get(1).isBlank() || datos.get(2).isBlank()) {
                    throw errorRegistro(ruta, numeroLinea, "ISBN, título y autor son obligatorios");
                }
                if (total <= 0 || disponibles < 0 || disponibles > total) {
                    throw errorRegistro(ruta, numeroLinea, "cantidades de ejemplares inválidas");
                }
                if (!isbnsRegistrados.add(isbn)) {
                    throw errorRegistro(ruta, numeroLinea, "ISBN duplicado");
                }
                libros.add(new Libro(isbn, datos.get(1), datos.get(2), total,
                    disponibles, datos.get(5)));
            } catch (NumberFormatException e) {
                throw new IOException("Cantidad inválida en " + ruta + ", línea " + numeroLinea, e);
            }
        }
        return libros;
    }

    public void guardarLibros(ArrayList<Libro> libros) throws IOException {
        ArrayList<List<String>> registros = new ArrayList<>();
        Set<String> isbnsRegistrados = new HashSet<>();
        for (Libro libro : libros) {
            if (libro == null || !isbnsRegistrados.add(libro.getIsbn())) {
                throw new IOException("La lista contiene un libro nulo o un ISBN duplicado");
            }
            validarLibro(libro);
            registros.add(List.of(libro.getIsbn(), libro.getTitulo(), libro.getAutor(),
                String.valueOf(libro.getCantidadBiblioteca()),
                String.valueOf(libro.getCantidadDisponible()),
                libro.getImagen() == null ? "" : libro.getImagen()));
        }

        escribirRegistros(directorioDatos.resolve("libros.csv"),
            List.of("isbn", "titulo", "autor", "cantidadBiblioteca", "cantidadDisponible", "imagen"),
            registros);
    }

    public ArrayList<Prestamo> cargarPrestamos() throws IOException {
        ArrayList<Prestamo> prestamos = new ArrayList<>();
        Path ruta = directorioDatos.resolve("prestamos.csv");
        List<List<String>> registros = leerRegistros(ruta,
            List.of("run", "isbn", "fechaPrestamo", "fechaDevolucion", "activo"));
        Set<String> usuariosConPrestamoActivo = new HashSet<>();

        for (int i = 0; i < registros.size(); i++) {
            List<String> datos = registros.get(i);
            int numeroLinea = i + 2;
            exigirColumnas(datos, 5, ruta, numeroLinea);
            try {
                String run = Usuario.normalizarRun(datos.get(0));
                String isbn = datos.get(1).trim();
                LocalDate fechaPrestamo = LocalDate.parse(datos.get(2));
                LocalDate fechaDevolucion = LocalDate.parse(datos.get(3));
                String activoTexto = datos.get(4).trim();
                if (isbn.isEmpty() || !fechaDevolucion.isAfter(fechaPrestamo)) {
                    throw errorRegistro(ruta, numeroLinea, "ISBN o fechas del préstamo inválidos");
                }
                if (!activoTexto.equals("true") && !activoTexto.equals("false")) {
                    throw errorRegistro(ruta, numeroLinea, "estado activo debe ser true o false");
                }
                boolean activo = Boolean.parseBoolean(activoTexto);
                if (activo && !usuariosConPrestamoActivo.add(run)) {
                    throw errorRegistro(ruta, numeroLinea, "usuario con más de un préstamo activo");
                }
                prestamos.add(new Prestamo(run, isbn, fechaPrestamo, fechaDevolucion, activo));
            } catch (IllegalArgumentException | DateTimeParseException e) {
                throw new IOException("Datos inválidos en " + ruta + ", línea " + numeroLinea
                    + ": " + e.getMessage(), e);
            }
        }
        return prestamos;
    }

    public void guardarPrestamos(ArrayList<Prestamo> prestamos) throws IOException {
        ArrayList<List<String>> registros = new ArrayList<>();
        Set<String> usuariosConPrestamoActivo = new HashSet<>();
        for (Prestamo prestamo : prestamos) {
            if (prestamo == null) {
                throw new IOException("La lista contiene un préstamo nulo");
            }
            String run;
            try {
                run = Usuario.normalizarRun(prestamo.getRun());
            } catch (IllegalArgumentException e) {
                throw new IOException("RUN inválido en la lista de préstamos", e);
            }
            if (prestamo.getIsbn() == null || prestamo.getIsbn().isBlank()
                    || prestamo.getFechaPrestamo() == null || prestamo.getFechaDevolucion() == null
                    || !prestamo.getFechaDevolucion().isAfter(prestamo.getFechaPrestamo())) {
                throw new IOException("La lista contiene un préstamo con datos inválidos");
            }
            if (prestamo.isActivo() && !usuariosConPrestamoActivo.add(run)) {
                throw new IOException("Un usuario tiene más de un préstamo activo");
            }
            registros.add(List.of(run, prestamo.getIsbn(), prestamo.getFechaPrestamo().toString(),
                prestamo.getFechaDevolucion().toString(), String.valueOf(prestamo.isActivo())));
        }

        escribirRegistros(directorioDatos.resolve("prestamos.csv"),
            List.of("run", "isbn", "fechaPrestamo", "fechaDevolucion", "activo"), registros);
    }

    public void validarCoherencia(List<Usuario> usuarios, List<Libro> libros,
            List<Prestamo> prestamos) throws IOException {
        Map<String, Usuario> usuariosPorRun = new HashMap<>();
        for (Usuario usuario : usuarios) {
            if (usuariosPorRun.put(usuario.getRun(), usuario) != null) {
                throw new IOException("RUN duplicado al validar coherencia: " + usuario.getRun());
            }
        }

        Map<String, Libro> librosPorIsbn = new HashMap<>();
        Map<String, Integer> prestamosActivosPorIsbn = new HashMap<>();
        Set<String> runsConPrestamoActivo = new HashSet<>();
        for (Libro libro : libros) {
            if (librosPorIsbn.put(libro.getIsbn(), libro) != null) {
                throw new IOException("ISBN duplicado al validar coherencia: " + libro.getIsbn());
            }
        }

        for (Prestamo prestamo : prestamos) {
            if (!prestamo.isActivo()) {
                continue;
            }
            Usuario usuario = usuariosPorRun.get(prestamo.getRun());
            Libro libro = librosPorIsbn.get(prestamo.getIsbn());
            if (usuario == null || libro == null) {
                throw new IOException("Préstamo activo referencia usuario o libro inexistente: "
                    + prestamo.getRun() + ", " + prestamo.getIsbn());
            }
            if (!prestamo.getIsbn().equals(usuario.getPrestamo())) {
                throw new IOException("Préstamo activo no coincide con el usuario " + prestamo.getRun());
            }
            if (!runsConPrestamoActivo.add(prestamo.getRun())) {
                throw new IOException("Usuario con más de un préstamo activo: " + prestamo.getRun());
            }
            prestamosActivosPorIsbn.merge(prestamo.getIsbn(), 1, Integer::sum);
        }

        for (Usuario usuario : usuarios) {
            boolean tienePrestamo = !"0".equals(usuario.getPrestamo());
            if (tienePrestamo != runsConPrestamoActivo.contains(usuario.getRun())) {
                throw new IOException("Estado de préstamo inconsistente para el usuario " + usuario.getRun());
            }
        }

        for (Libro libro : libros) {
            // Los disponibles y los préstamos activos no pueden superar el total.
            int activos = prestamosActivosPorIsbn.getOrDefault(libro.getIsbn(), 0);
            if (libro.getCantidadDisponible() + activos > libro.getCantidadBiblioteca()) {
                throw new IOException("Los préstamos activos superan el stock del libro "
                    + libro.getIsbn());
            }
        }
    }

    private List<List<String>> leerRegistros(Path ruta, List<String> encabezadoEsperado)
            throws IOException {
        if (Files.notExists(ruta)) {
            return List.of();
        }
        if (!Files.isRegularFile(ruta)) {
            throw new IOException("La ruta de datos no es un archivo regular: " + ruta);
        }
        if (Files.size(ruta) == 0) {
            return List.of();
        }

        try (Reader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            List<List<String>> filas = leerCsv(lector, ruta);
            if (filas.isEmpty()) {
                throw new IOException("Falta el encabezado en " + ruta);
            }
            List<String> encabezado = new ArrayList<>(filas.remove(0));
            if (!encabezado.isEmpty() && encabezado.get(0).startsWith("\uFEFF")) {
                encabezado.set(0, encabezado.get(0).substring(1));
            }
            if (!encabezadoEsperado.equals(encabezado)) {
                throw new IOException("Encabezado inválido en " + ruta + ": " + encabezado);
            }
            return filas;
        }
    }

    private List<List<String>> leerCsv(Reader lector, Path ruta) throws IOException {
        List<List<String>> filas = new ArrayList<>();
        List<String> fila = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        try (PushbackReader lectorConRetroceso = new PushbackReader(lector, 1)) {
            boolean entreComillas = false;
            boolean despuesDeComilla = false;
            int caracter;
            while ((caracter = lectorConRetroceso.read()) != -1) {
                char actual = (char) caracter;
                if (entreComillas) {
                    if (actual == '"') {
                        int siguiente = lectorConRetroceso.read();
                        if (siguiente == '"') {
                            campo.append('"');
                        } else {
                            entreComillas = false;
                            despuesDeComilla = true;
                            if (siguiente != -1) {
                                lectorConRetroceso.unread(siguiente);
                            }
                        }
                    } else {
                        campo.append(actual);
                    }
                } else if (actual == ',' ) {
                    fila.add(campo.toString());
                    campo.setLength(0);
                    despuesDeComilla = false;
                } else if (actual == '\n' || actual == '\r') {
                    if (actual == '\r') {
                        int siguiente = lectorConRetroceso.read();
                        if (siguiente != '\n' && siguiente != -1) {
                            lectorConRetroceso.unread(siguiente);
                        }
                    }
                    fila.add(campo.toString());
                    agregarFila(filas, fila);
                    fila = new ArrayList<>();
                    campo.setLength(0);
                    despuesDeComilla = false;
                } else if (actual == '"') {
                    if (campo.length() != 0 || despuesDeComilla) {
                        throw new IOException("Comillas inválidas en " + ruta);
                    }
                    entreComillas = true;
                } else {
                    if (despuesDeComilla) {
                        throw new IOException("Texto después de comilla de cierre en " + ruta);
                    }
                    campo.append(actual);
                }
            }

            if (entreComillas) {
                throw new IOException("Campo entre comillas sin cierre en " + ruta);
            }
            if (campo.length() > 0 || !fila.isEmpty() || despuesDeComilla) {
                fila.add(campo.toString());
                agregarFila(filas, fila);
            }
        }
        return filas;
    }

    private void agregarFila(List<List<String>> filas, List<String> fila) {
        if (!(fila.size() == 1 && fila.get(0).isEmpty())) {
            filas.add(fila);
        }
    }

    private void escribirRegistros(Path ruta, List<String> encabezado,
            List<List<String>> registros) throws IOException {
        Files.createDirectories(ruta.getParent());
        Path temporal = Files.createTempFile(ruta.getParent(), ruta.getFileName().toString(), ".tmp");
        boolean reemplazado = false;
        try {
            try (BufferedWriter escritor = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
                escribirFila(escritor, encabezado);
                for (List<String> registro : registros) {
                    escribirFila(escritor, registro);
                }
            }

            try {
                Files.move(temporal, ruta, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, ruta, StandardCopyOption.REPLACE_EXISTING);
            }
            reemplazado = true;
        } finally {
            if (!reemplazado) {
                Files.deleteIfExists(temporal);
            }
        }
    }

    private void escribirFila(BufferedWriter escritor, List<String> campos) throws IOException {
        for (int i = 0; i < campos.size(); i++) {
            if (i > 0) {
                escritor.write(',');
            }
            String campo = campos.get(i) == null ? "" : campos.get(i);
            if (campo.contains(",") || campo.contains("\"")
                    || campo.contains("\n") || campo.contains("\r")) {
                escritor.write('"');
                escritor.write(campo.replace("\"", "\"\""));
                escritor.write('"');
            } else {
                escritor.write(campo);
            }
        }
        escritor.newLine();
    }

    private void validarLibro(Libro libro) throws IOException {
        if (libro.getIsbn() == null || libro.getIsbn().isBlank()
                || libro.getTitulo() == null || libro.getTitulo().isBlank()
                || libro.getAutor() == null || libro.getAutor().isBlank()
                || libro.getCantidadBiblioteca() <= 0
                || libro.getCantidadDisponible() < 0
                || libro.getCantidadDisponible() > libro.getCantidadBiblioteca()) {
            throw new IOException("Libro con datos o cantidades inválidas: " + libro.getIsbn());
        }
    }

    private void validarUsuario(Usuario usuario) throws IOException {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()
                || !Usuario.validarRun(usuario.getRun())
                || !Usuario.validarGenero(usuario.getGenero())
                || usuario.getPrestamo() == null || usuario.getPrestamo().isBlank()) {
            throw new IOException("Usuario con datos inválidos: " + usuario.getRun());
        }
        if (usuario instanceof Estudiante estudiante
                && (estudiante.getCarrera() == null || estudiante.getCarrera().isBlank())) {
            throw new IOException("Estudiante sin carrera: " + usuario.getRun());
        }
        if (usuario instanceof Docente docente
                && (docente.getProfesion() == null || docente.getProfesion().isBlank()
                    || docente.getGradosAcademicos() == null
                    || docente.getGradosAcademicos().isBlank())) {
            throw new IOException("Docente con profesión o grados académicos vacíos: "
                + usuario.getRun());
        }
    }

    private char caracter(String valor, Path ruta, int linea) throws IOException {
        if (valor.length() != 1) {
            throw errorRegistro(ruta, linea, "género inválido");
        }
        return valor.charAt(0);
    }

    private void exigirColumnas(List<String> datos, int cantidad, Path ruta, int linea)
            throws IOException {
        if (datos.size() != cantidad) {
            throw errorRegistro(ruta, linea, "se esperaban " + cantidad + " columnas y hay "
                + datos.size());
        }
    }

    private void exigirVacio(String valor, Path ruta, int linea, String columna)
            throws IOException {
        if (!valor.isEmpty()) {
            throw errorRegistro(ruta, linea, "la columna " + columna + " no corresponde al tipo");
        }
    }

    private void exigirTexto(String valor, Path ruta, int linea, String columna)
            throws IOException {
        if (valor.isBlank()) {
            throw errorRegistro(ruta, linea, "la columna " + columna + " es obligatoria");
        }
    }

    private IOException errorRegistro(Path ruta, int linea, String mensaje) {
        return new IOException("Registro inválido en " + ruta + ", línea " + linea + ": " + mensaje);
    }
}

