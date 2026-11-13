package biblioteca.view;

import biblioteca.Biblioteca;
import biblioteca.model.Docente;
import biblioteca.model.Estudiante;
import biblioteca.model.Usuario;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class EditarUsuarioDialog extends JDialog {

    private final Biblioteca biblioteca;
    private final JTextField run = new JTextField();
    private final JTextField nombre = new JTextField();
    private final JComboBox<String> genero = new JComboBox<>(new String[] {"M", "F"});
    private final JTextField carrera = new JTextField();
    private final JTextField profesion = new JTextField();
    private final JTextField grados = new JTextField();
    private final JPanel datos = new JPanel(new GridLayout(0, 2, 8, 8));
    private Usuario usuarioEncontrado;

    public EditarUsuarioDialog(JFrame parent, Biblioteca biblioteca) {
        super(parent, "Editar usuario", true);
        this.biblioteca = biblioteca;
        setSize(500, 200);
        setLocationRelativeTo(parent);

        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.setBorder(new EmptyBorder(16, 20, 16, 20));
        JPanel busqueda = new JPanel(new GridLayout(1, 3, 8, 8));
        JButton buscar = new JButton("Cargar usuario");
        busqueda.add(new JLabel("RUN actual:"));
        busqueda.add(run);
        busqueda.add(buscar);
        contenido.add(busqueda, BorderLayout.NORTH);
        contenido.add(datos, BorderLayout.CENTER);

        JButton guardar = new JButton("Guardar cambios");
        JButton cancelar = new JButton("Cancelar");
        JPanel acciones = new JPanel(new GridLayout(1, 2, 8, 8));
        acciones.add(guardar);
        acciones.add(cancelar);
        contenido.add(acciones, BorderLayout.SOUTH);
        add(contenido);
        mostrarCamposComunes();

        buscar.addActionListener(e -> cargarUsuario());
        cancelar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> guardarCambios());
    }

    private void cargarUsuario() {
        try {
            usuarioEncontrado = biblioteca.buscarUsuario(run.getText().trim());
            if (usuarioEncontrado == null) {
                JOptionPane.showMessageDialog(this, "No se encontró ese usuario.",
                    "Usuario no encontrado", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            nombre.setText(usuarioEncontrado.getNombre());
            genero.setSelectedItem(String.valueOf(usuarioEncontrado.getGenero()));
            mostrarCamposComunes();
            if (usuarioEncontrado instanceof Estudiante estudiante) {
                carrera.setText(estudiante.getCarrera());
                agregarCampo("Carrera:", carrera);
            } else if (usuarioEncontrado instanceof Docente docente) {
                profesion.setText(docente.getProfesion());
                grados.setText(docente.getGradosAcademicos());
                agregarCampo("Profesión:", profesion);
                agregarCampo("Grados académicos:", grados);
            }
            datos.revalidate();
            datos.repaint();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "RUN inválido",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarCamposComunes() {
        datos.removeAll();
        agregarCampo("Nombre completo:", nombre);
        agregarCampo("Género:", genero);
    }

    private void agregarCampo(String etiqueta, java.awt.Component campo) {
        datos.add(new JLabel(etiqueta));
        datos.add(campo);
    }

    private void guardarCambios() {
        if (usuarioEncontrado == null) {
            JOptionPane.showMessageDialog(this, "Primero carga un usuario mediante su RUN.",
                "Falta seleccionar usuario", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Usuario actualizado;
            char generoSeleccionado = ((String) genero.getSelectedItem()).charAt(0);
            if (usuarioEncontrado instanceof Estudiante) {
                actualizado = new Estudiante(nombre.getText().trim(), usuarioEncontrado.getRun(),
                    generoSeleccionado, carrera.getText().trim());
            } else {
                actualizado = new Docente(nombre.getText().trim(), usuarioEncontrado.getRun(),
                    generoSeleccionado, profesion.getText().trim(), grados.getText().trim());
            }
            if (!biblioteca.editarUsuario(usuarioEncontrado.getRun(), actualizado)) {
                JOptionPane.showMessageDialog(this,
                    "No se pudieron aplicar los cambios. Revisa duplicados o el préstamo activo.",
                    "Edición rechazada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Usuario actualizado correctamente.");
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos",
                JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron guardar los cambios:\n"
                + ex.getMessage(), "Error de persistencia", JOptionPane.ERROR_MESSAGE);
        }
    }
}
