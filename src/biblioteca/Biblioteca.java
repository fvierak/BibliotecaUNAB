package biblioteca;

import java.io.IOException;
import java.util.ArrayList;
import biblioteca.model.Usuario;
import biblioteca.data.ArchivoManager;
import biblioteca.model.Libro;

// TODO: Implementar lo pertinente a libro

public class Biblioteca {

    // Listas que almacenan los objetos durante la ejecución
    private ArrayList<Usuario> usuarios;
    private ArrayList<Libro> libros;

    public Biblioteca() {

        usuarios = new ArrayList<>();
        libros = new ArrayList<>();
    }

    // Comprueba si un RUN ya está registrado
    public boolean chequearRun(String run) {

        for (Usuario usuario : usuarios) {

            if (usuario.getRun().equals(run)) {
                return true;
            }
        }

        return false;
    }

    // Agrega un usuario si su RUN no está registrado
    public boolean agregarUsuario(Usuario usuario) throws IOException{

        // Verificamos si el RUN no está registrado
        if (chequearRun(usuario.getRun())) {
            return false;
        }

        // Preparamos una lista actualizada
        ArrayList<Usuario> nuevosUsuarios = new ArrayList<>(usuarios);

        nuevosUsuarios.add(usuario);

        ArchivoManager archivoManager = new ArchivoManager();

        // Guardamos los cambios antes de actualizar la lista original
        archivoManager.guardarUsuarios(nuevosUsuarios);

        usuarios = nuevosUsuarios;

        return true;
    }

    public Usuario buscarUsuario(String run) {

        for (Usuario usuario : usuarios) {

            if (usuario.getRun().equals(run)) {
                return usuario;
            }
        }

        return null;
    }

    public boolean eliminarUsuario(String run) throws IOException {

        // Buscamos el usuario
        Usuario usuario = buscarUsuario(run);

        if (usuario == null) {
            return false;
        }

        // No eliminamos usuarios que tengan un libro prestado
        if (!usuario.getPrestamo().equals("0")) {
            return false;
        }

        // Creamos una copia de la lista actual
        ArrayList<Usuario> usuariosActualizados = new ArrayList<>(usuarios);

        // Eliminamos el usuario solamente de la copia
        usuariosActualizados.remove(usuario);

        ArchivoManager archivoManager = new ArchivoManager();

        // Intentamos guardar primero los cambios
        archivoManager.guardarUsuarios(usuariosActualizados);

        // Si el guardado fue exitoso, actualizamos la lista en memoria
        usuarios = usuariosActualizados;

        return true;
    }
}