package biblioteca.view;

import biblioteca.Biblioteca;
import biblioteca.model.Docente;
import biblioteca.model.Estudiante;
import biblioteca.model.Usuario;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class BuscarUsuarioDialog extends JDialog {

    public BuscarUsuarioDialog(JFrame parent, Biblioteca biblioteca) {
        super(parent, "Buscar usuario", true);
        setSize(430, 140);
        setLocationRelativeTo(parent);

        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.setBorder(new EmptyBorder(18, 20, 18, 20));
        JPanel formulario = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField run = new JTextField();
        JButton buscar = new JButton("Buscar");
        JButton cerrar = new JButton("Cerrar");
        formulario.add(new JLabel("RUN:"));
        formulario.add(run);
        formulario.add(buscar);
        formulario.add(cerrar);
        contenido.add(formulario, BorderLayout.CENTER);
        add(contenido);

        cerrar.addActionListener(e -> dispose());
        buscar.addActionListener(e -> {
            try {
                Usuario usuario = biblioteca.buscarUsuario(run.getText().trim());
                if (usuario == null) {
                    JOptionPane.showMessageDialog(this, "No se encontró ese usuario.",
                        "Usuario no encontrado", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Mostramos los datos del usuario encontrado
                String informacion =
                    "Nombre: " + usuario.getNombre()
                    + "\nRun: " + usuario.getRun()
                    + "\nTipo de usuario: " + usuario.getTipo()
                    + "\nPrestamo: " + usuario.getPrestamo();
                    
                JOptionPane.showMessageDialog(
                    this,
                    informacion,
                    "Información del usuario",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // String tipo = usuario instanceof Estudiante ? "Estudiante"
                //     : usuario instanceof Docente ? "Docente" : "Usuario";
                // String situacion = "0".equals(usuario.getPrestamo())
                //     ? "Sin préstamo activo" : "Préstamo activo: " + usuario.getPrestamo();
                // JOptionPane.showMessageDialog(this,
                //     "Tipo: " + tipo + "\n" + usuario + "\nSituación: " + situacion,
                //     "Datos del usuario", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "RUN inválido",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
