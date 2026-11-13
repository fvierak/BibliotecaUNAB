package biblioteca.view;

import biblioteca.Biblioteca;
import java.awt.GridLayout;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class DevolucionDialog extends JDialog {

    public DevolucionDialog(JFrame parent, Biblioteca biblioteca) {
        super(parent, "Registrar devolución", true);
        setSize(430, 160);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        JTextField run = new JTextField();
        JTextField isbn = new JTextField();
        JButton devolver = new JButton("Registrar devolución");
        JButton cancelar = new JButton("Cancelar");
        panel.add(new JLabel("RUN del usuario:"));
        panel.add(run);
        panel.add(new JLabel("ISBN del libro:"));
        panel.add(isbn);
        panel.add(devolver);
        panel.add(cancelar);
        add(panel);

        cancelar.addActionListener(e -> dispose());
        devolver.addActionListener(e -> {
            try {
                long multa = biblioteca.realizarDevolucion(run.getText().trim(),
                    isbn.getText().trim());
                String multaFormateada = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CL"))
                    .format(multa);
                JOptionPane.showMessageDialog(this,
                    multa == 0 ? "Devolución registrada. No hay multa pendiente."
                        : "Devolución registrada. Multa: $" + multaFormateada + " CLP.",
                    "Devolución completada", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Devolución rechazada",
                    JOptionPane.WARNING_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo guardar la devolución:\n"
                    + ex.getMessage(), "Error de persistencia", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
