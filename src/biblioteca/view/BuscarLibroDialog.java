
package biblioteca.view;

import biblioteca.Biblioteca;
import biblioteca.model.Libro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;

public class BuscarLibroDialog extends JDialog {

    public BuscarLibroDialog(JFrame parent, Biblioteca biblioteca) {

        super(parent, "Buscar libro", true);

        setSize(400, 180);
        setLocationRelativeTo(parent);

        // Panel para ingresar el ISBN
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtIsbn = new JTextField();

        JButton btnBuscar = new JButton("Buscar");
        JButton btnCancelar = new JButton("Cancelar");

        panel.add(new JLabel("ISBN:"));
        panel.add(txtIsbn);
        panel.add(btnBuscar);
        panel.add(btnCancelar);

        add(panel);

        // Cerramos la ventana sin realizar ninguna operación
        btnCancelar.addActionListener(e -> dispose());

        // Buscamos el libro según el ISBN ingresado
        btnBuscar.addActionListener(e -> {

            String isbn = txtIsbn.getText().trim();

            Libro libro = biblioteca.buscarLibro(isbn);

            if (libro == null) {

                JOptionPane.showMessageDialog(
                    this,
                    "No se encontró un libro con ese ISBN.",
                    "Libro no encontrado",
                    JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            // Mostramos los datos del libro encontrado
            String informacion =
                "ISBN: " + libro.getIsbn()
                + "\nTítulo: " + libro.getTitulo()
                + "\nAutor: " + libro.getAutor()
                + "\nCantidad total: " + libro.getCantidadBiblioteca()
                + "\nCantidad disponible: " + libro.getCantidadDisponible()
                + "\nImagen: " + libro.getImagen();

            JOptionPane.showMessageDialog(
                this,
                informacion,
                "Información del libro",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
}
