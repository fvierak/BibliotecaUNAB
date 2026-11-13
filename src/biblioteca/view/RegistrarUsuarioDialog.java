package biblioteca.view;

import biblioteca.Biblioteca;
import biblioteca.model.Docente;
import biblioteca.model.Estudiante;
import biblioteca.model.Usuario;
import java.awt.BorderLayout;
import java.awt.CardLayout;
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

public class RegistrarUsuarioDialog extends JDialog {

    public RegistrarUsuarioDialog(JFrame parent, Biblioteca biblioteca) {
        super(parent, "Registrar usuario", true);
        setSize(520, 260);
        setLocationRelativeTo(parent);

        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.setBorder(new EmptyBorder(16, 20, 16, 20));
        JPanel campos = new JPanel(new GridLayout(0, 2, 8, 8));

        JComboBox<String> tipo = new JComboBox<>(new String[] {"Estudiante", "Docente"});
        JTextField nombre = new JTextField();
        JTextField run = new JTextField();
        JComboBox<String> genero = new JComboBox<>(new String[] {"M", "F"});
        campos.add(new JLabel("Tipo de usuario:"));
        campos.add(tipo);
        campos.add(new JLabel("Nombre completo:"));
        campos.add(nombre);
        campos.add(new JLabel("RUN:"));
        campos.add(run);
        campos.add(new JLabel("Género:"));
        campos.add(genero);

        CardLayout tarjetas = new CardLayout();
        JPanel camposTipo = new JPanel(tarjetas);
        JPanel camposEstudiante = new JPanel(new GridLayout(1, 2, 8, 8));
        JTextField carrera = new JTextField();
        camposEstudiante.add(new JLabel("Carrera:"));
        camposEstudiante.add(carrera);
        JPanel camposDocente = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField profesion = new JTextField();
        JTextField grados = new JTextField();
        camposDocente.add(new JLabel("Profesión:"));
        camposDocente.add(profesion);
        camposDocente.add(new JLabel("Grados académicos:"));
        camposDocente.add(grados);
        camposTipo.add(camposEstudiante, "Estudiante");
        camposTipo.add(camposDocente, "Docente");
        tarjetas.show(camposTipo, "Estudiante");
        tipo.addActionListener(e -> tarjetas.show(camposTipo, (String) tipo.getSelectedItem()));

        JPanel formulario = new JPanel(new BorderLayout(8, 8));
        formulario.add(campos, BorderLayout.NORTH);
        formulario.add(camposTipo, BorderLayout.CENTER);
        contenido.add(formulario, BorderLayout.CENTER);

        JButton guardar = new JButton("Guardar");
        JButton cancelar = new JButton("Cancelar");
        JPanel acciones = new JPanel(new GridLayout(1, 2, 8, 8));
        acciones.add(guardar);
        acciones.add(cancelar);
        contenido.add(acciones, BorderLayout.SOUTH);
        add(contenido);

        cancelar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> {
            try {
                String nombreIngresado = nombre.getText().trim();
                String runIngresado = run.getText().trim();
                char generoIngresado = ((String) genero.getSelectedItem()).charAt(0);
                Usuario nuevo;
                if ("Estudiante".equals(tipo.getSelectedItem())) {
                    if (carrera.getText().isBlank()) {
                        throw new IllegalArgumentException("La carrera es obligatoria.");
                    }
                    nuevo = new Estudiante(nombreIngresado, runIngresado, generoIngresado,
                        carrera.getText().trim());
                } else {
                    if (profesion.getText().isBlank() || grados.getText().isBlank()) {
                        throw new IllegalArgumentException(
                            "La profesión y los grados académicos son obligatorios.");
                    }
                    nuevo = new Docente(nombreIngresado, runIngresado, generoIngresado,
                        profesion.getText().trim(), grados.getText().trim());
                }

                if (!biblioteca.agregarUsuario(nuevo)) {
                    JOptionPane.showMessageDialog(this, "El RUN ya está registrado.",
                        "Usuario duplicado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(this, "Usuario registrado correctamente.");
                dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos",
                    JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo guardar el usuario:\n"
                    + ex.getMessage(), "Error de persistencia", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
