package biblioteca.view;

import biblioteca.Biblioteca;
import biblioteca.model.Libro;
import biblioteca.model.Prestamo;
import biblioteca.model.Usuario;
import java.awt.GridLayout;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

public class RegistrarPrestamoDialog extends JDialog {

    public RegistrarPrestamoDialog(JFrame parent, Biblioteca biblioteca) {
        super(parent, "Registrar préstamo", true);
        setSize(440, 210);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        JTextField run = new JTextField();
        JTextField isbn = new JTextField();
        JSpinner dias = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        JButton registrar = new JButton("Registrar");
        JButton cancelar = new JButton("Cancelar");
        panel.add(new JLabel("RUN del usuario:"));
        panel.add(run);
        panel.add(new JLabel("ISBN del libro:"));
        panel.add(isbn);
        panel.add(new JLabel("Duración (días):"));
        panel.add(dias);
        panel.add(registrar);
        panel.add(cancelar);
        add(panel);

        cancelar.addActionListener(e -> dispose());
        registrar.addActionListener(e -> {
            try {
                String runIngresado = run.getText().trim();
                String isbnIngresado = isbn.getText().trim();
                Usuario usuario = biblioteca.buscarUsuario(runIngresado);
                Libro libro = biblioteca.buscarLibro(isbnIngresado);
                if (usuario == null) {
                    throw new IllegalArgumentException("No existe un usuario con ese RUN.");
                }
                if (libro == null) {
                    throw new IllegalArgumentException("No existe un libro con ese ISBN.");
                }

                Prestamo prestamo = biblioteca.realizarPrestamo(runIngresado, isbnIngresado,
                    (Integer) dias.getValue());
                dispose();
                new TarjetaPrestamoDialog(parent, prestamo, libro.getTitulo(), usuario.getNombre())
                    .setVisible(true);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Préstamo rechazado",
                    JOptionPane.WARNING_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo guardar el préstamo:\n"
                    + ex.getMessage(), "Error de persistencia", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
