package biblioteca.view;

import biblioteca.model.Prestamo;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.print.PrinterException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

public class TarjetaPrestamoDialog extends JDialog {

        public TarjetaPrestamoDialog(JFrame parent, Prestamo prestamo, String tituloLibro,
            String nombreUsuario) {
        super(parent, "Tarjeta de préstamo", true);
        setSize(430, 330);
        setLocationRelativeTo(parent);

        JTextPane tarjeta = new JTextPane();
        tarjeta.setEditable(false);
        tarjeta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        tarjeta.setText(prestamo.generarTarjeta(tituloLibro, nombreUsuario));
        tarjeta.setBorder(new EmptyBorder(18, 18, 18, 18));

        JButton imprimir = new JButton("Imprimir");
        JButton cerrar = new JButton("Cerrar");
        JPanel acciones = new JPanel(new GridLayout(1, 2, 8, 8));
        acciones.setBorder(new EmptyBorder(8, 12, 12, 12));
        acciones.add(imprimir);
        acciones.add(cerrar);

        add(tarjeta, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
        cerrar.addActionListener(e -> dispose());
        imprimir.addActionListener(e -> {
            try {
                boolean impreso = tarjeta.print();
                if (impreso) {
                    JOptionPane.showMessageDialog(this, "Tarjeta enviada a impresión.");
                }
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo imprimir la tarjeta:\n"
                    + ex.getMessage(), "Error de impresión", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
