package biblioteca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import biblioteca.model.Usuario;
import biblioteca.data.ArchivoManager;
import biblioteca.model.Libro;

// TODO: Implementar lo pertinente a libro

public class Biblioteca {

    // Listas que almacenan los objetos durante la ejecución
    private final ArrayList<Usuario> usuarios;
    private final ArrayList<Libro> libros;
    private final ArchivoManager archivoManager;

    public Biblioteca() throws IOException {
        this(new ArchivoManager());
    }

    public Biblioteca(ArchivoManager archivoManager) throws IOException {
        this.archivoManager = Objects.requireNonNull(archivoManager);
        usuarios = archivoManager.cargarUsuarios();
        libros = new ArrayList<>();
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
        if (chequearRun(usuario.getRun())) {
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

    private void guardarUsuariosActualizados(ArrayList<Usuario> usuariosActualizados)
            throws IOException {
        archivoManager.guardarUsuarios(usuariosActualizados);
        usuarios.clear();
        usuarios.addAll(usuariosActualizados);
    }
}