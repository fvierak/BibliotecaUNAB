
package biblioteca.view;

import biblioteca.Biblioteca;
import biblioteca.model.Libro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.io.IOException;

public class EliminarLibroDialog extends JDialog {

    public EliminarLibroDialog(JFrame parent, Biblioteca biblioteca) {

        super(parent, "Eliminar libro", true);

        setSize(400, 180);
        setLocationRelativeTo(parent);

        // Panel para ingresar el ISBN
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtIsbn = new JTextField();

        JButton btnEliminar = new JButton("Eliminar");
        JButton btnCancelar = new JButton("Cancelar");

        panel.add(new JLabel("ISBN:"));
        panel.add(txtIsbn);
        panel.add(btnEliminar);
        panel.add(btnCancelar);

        add(panel);

        btnCancelar.addActionListener(e -> dispose());

        btnEliminar.addActionListener(e -> {

            String isbn = txtIsbn.getText().trim();

            // Primero comprobamos que el libro existe
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

            // Mostramos qué libro se eliminará
            String mensaje =
                "¿Estás seguro de eliminar este libro?\n\n"
                + "Título: " + libro.getTitulo()
                + "\nAutor: " + libro.getAutor()
                + "\nISBN: " + libro.getIsbn();

            int respuesta = JOptionPane.showConfirmDialog(
                this,
                mensaje,
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            // Si el usuario no confirma, no hacemos nada
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            try {

                // Eliminamos mediante la lógica de Biblioteca
                boolean eliminado = biblioteca.eliminarLibro(isbn);

                if (eliminado) {

                    JOptionPane.showMessageDialog(
                        this,
                        "Libro eliminado correctamente."
                    );

                    dispose();

                } else {

                    JOptionPane.showMessageDialog(
                        this,
                        "No fue posible eliminar el libro.",
                        "Operación rechazada",
                        JOptionPane.WARNING_MESSAGE
                    );
                }

            } catch (IllegalArgumentException ex) {

                JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Datos inválidos",
                    JOptionPane.ERROR_MESSAGE
                );

            } catch (IOException ex) {

                JOptionPane.showMessageDialog(
                    this,
                    "Error al guardar los cambios:\n" + ex.getMessage(),
                    "Error de persistencia",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
