package biblioteca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import biblioteca.model.Usuario;
import biblioteca.data.ArchivoManager;
import biblioteca.model.Libro;
import biblioteca.model.Prestamo;

public class Biblioteca {

    // Listas que almacenan los objetos durante la ejecución
    private final ArrayList<Usuario> usuarios;
    private final ArrayList<Libro> libros;
    private final ArrayList<Prestamo> prestamos;
    private final ArchivoManager archivoManager;

    public Biblioteca() throws IOException {
        this(new ArchivoManager());
    }

    public Biblioteca(ArchivoManager archivoManager) throws IOException {
        this.archivoManager = Objects.requireNonNull(archivoManager);
        usuarios = archivoManager.cargarUsuarios();
        libros = archivoManager.cargarLibros();
        prestamos = archivoManager.cargarPrestamos();
        archivoManager.validarCoherencia(usuarios, libros, prestamos);
    }

    // Comprueba si un RUN ya está registrado
    public boolean chequearRun(String run) {
        String runNormalizado = Usuario.normalizarRun(run);

        for (Usuario usuario : usuarios) {
            if (usuario.getRun().equals(runNormalizado)) {
                return true;
            }
        }

        return false;
    }

    // Agrega un usuario si su RUN no está registrado
    public boolean agregarUsuario(Usuario usuario) throws IOException {
        Objects.requireNonNull(usuario, "El usuario es obligatorio");
        if (chequearRun(usuario.getRun()) || !"0".equals(usuario.getPrestamo())) {
            return false;
        }

        ArrayList<Usuario> nuevosUsuarios = new ArrayList<>(usuarios);
        nuevosUsuarios.add(usuario);
        guardarUsuariosActualizados(nuevosUsuarios);
        return true;
    }

    public Usuario buscarUsuario(String run) {
        String runNormalizado = Usuario.normalizarRun(run);

        for (Usuario usuario : usuarios) {
            if (usuario.getRun().equals(runNormalizado)) {
                return usuario;
            }
        }

        return null;
    }

    public boolean editarUsuario(String runOriginal, Usuario usuarioActualizado) throws IOException {
        Objects.requireNonNull(usuarioActualizado, "El usuario actualizado es obligatorio");
        Usuario usuarioExistente = buscarUsuario(runOriginal);
        if (usuarioExistente == null) {
            return false;
        }

        if (!usuarioExistente.getRun().equals(usuarioActualizado.getRun())
                && !"0".equals(usuarioExistente.getPrestamo())) {
            return false;
        }

        if (!usuarioExistente.getRun().equals(usuarioActualizado.getRun())
                && chequearRun(usuarioActualizado.getRun())) {
            return false;
        }

        usuarioActualizado.setPrestamo(usuarioExistente.getPrestamo());
        ArrayList<Usuario> usuariosActualizados = new ArrayList<>(usuarios);
        usuariosActualizados.set(usuariosActualizados.indexOf(usuarioExistente), usuarioActualizado);
        guardarUsuariosActualizados(usuariosActualizados);
        return true;
    }

    public boolean eliminarUsuario(String run) throws IOException {
        Usuario usuario = buscarUsuario(run);
        if (usuario == null) {
            return false;
        }

        // Se conserva al usuario mientras tenga un ISBN asociado a su préstamo.
        if (!"0".equals(usuario.getPrestamo())) {
            return false;
        }

        ArrayList<Usuario> usuariosActualizados = new ArrayList<>(usuarios);
        usuariosActualizados.remove(usuario);
        guardarUsuariosActualizados(usuariosActualizados);
        return true;
    }

    public Libro buscarLibro(String isbn) {
        if (isbn == null) {
            return null;
        }

        String isbnBuscado = isbn.trim();
        for (Libro libro : libros) {
            if (libro.getIsbn().equals(isbnBuscado)) {
                return libro;
            }
        }
        return null;
    }

    public boolean agregarLibro(Libro libro) throws IOException {
        Objects.requireNonNull(libro, "El libro es obligatorio");
        validarLibroNuevo(libro);

        String isbn = libro.getIsbn().trim();
        if (buscarLibro(isbn) != null) {
            return false;
        }
        libro.setIsbn(isbn);

        ArrayList<Libro> librosActualizados = new ArrayList<>(libros);
        librosActualizados.add(libro);
        guardarLibrosActualizados(librosActualizados);
        return true;
    }

    public boolean eliminarLibro(String isbn) throws IOException {
        Libro libro = buscarLibro(isbn);
        if (libro == null) {
            return false;
        }

        for (Prestamo prestamo : prestamos) {
            if (prestamo.isActivo() && prestamo.getIsbn().equals(libro.getIsbn())) {
                return false;
            }
        }

        ArrayList<Libro> librosActualizados = new ArrayList<>(libros);
        librosActualizados.remove(libro);
        guardarLibrosActualizados(librosActualizados);
        return true;
    }

    private void validarLibroNuevo(Libro libro) {
        if (libro.getIsbn() == null || libro.getIsbn().isBlank()
                || libro.getTitulo() == null || libro.getTitulo().isBlank()
                || libro.getAutor() == null || libro.getAutor().isBlank()) {
            throw new IllegalArgumentException("ISBN, título y autor son obligatorios");
        }
        if (libro.getCantidadBiblioteca() <= 0) {
            throw new IllegalArgumentException("La cantidad total debe ser mayor que cero");
        }
        if (libro.getCantidadDisponible() <= 0
                || libro.getCantidadDisponible() > libro.getCantidadBiblioteca()) {
            throw new IllegalArgumentException(
                "La cantidad disponible debe ser mayor que cero y no superar el total"
            );
        }
    }

    private void guardarLibrosActualizados(ArrayList<Libro> librosActualizados)
            throws IOException {
        archivoManager.guardarLibros(librosActualizados);
        libros.clear();
        libros.addAll(librosActualizados);
    }

    private void guardarUsuariosActualizados(ArrayList<Usuario> usuariosActualizados)
            throws IOException {
        archivoManager.guardarUsuarios(usuariosActualizados);
        usuarios.clear();
        usuarios.addAll(usuariosActualizados);
    }
}