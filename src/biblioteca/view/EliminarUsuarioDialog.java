package biblioteca.view;

import biblioteca.Biblioteca;
import biblioteca.model.Usuario;
import java.io.IOException;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class EliminarUsuarioDialog extends JDialog {

    public EliminarUsuarioDialog(JFrame parent, Biblioteca biblioteca) {
        super(parent, "Eliminar usuario", true);
        setSize(430, 130);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        JTextField run = new JTextField();
        JButton eliminar = new JButton("Eliminar");
        JButton cancelar = new JButton("Cancelar");
        panel.add(new JLabel("RUN:"));
        panel.add(run);
        panel.add(eliminar);
        panel.add(cancelar);
        add(panel);

        cancelar.addActionListener(e -> dispose());
        eliminar.addActionListener(e -> {
            try {
                Usuario usuario = biblioteca.buscarUsuario(run.getText().trim());
                if (usuario == null) {
                    JOptionPane.showMessageDialog(this, "No se encontró ese usuario.",
                        "Usuario no encontrado", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int respuesta = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar este usuario?\n\n" + usuario,
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (respuesta != JOptionPane.YES_OPTION) {
                    return;
                }
                if (!biblioteca.eliminarUsuario(usuario.getRun())) {
                    JOptionPane.showMessageDialog(this,
                        "No se puede eliminar un usuario con un préstamo activo.",
                        "Operación rechazada", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(this, "Usuario eliminado correctamente.");
                dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "RUN inválido",
                    JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el usuario:\n"
                    + ex.getMessage(), "Error de persistencia", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
