package biblioteca.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;
import java.awt.GridLayout;

public class LibroDialog extends JDialog{

    public LibroDialog(JFrame parent) {

        // Indicamos la ventana padre, títullo y modalidad
        super(parent, "Gestión de libros", true);

        setSize(450, 400);
        setLocationRelativeTo(parent);

        // Panel principal con márgenes
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
    
        // 7 filas y 2 columnas
        panel.setLayout(new GridLayout(7, 2, 10, 10));

        // Campos del formulario
        JTextField txtIsbn = new JTextField();
        JTextField txtTitulo = new JTextField();
        JTextField txtAutor = new JTextField();
        JTextField txtCantidadTotal = new JTextField();
        JTextField txtCantidadDisponible = new JTextField();
        JTextField txtImagen = new JTextField();

        // Etiquetas y campos
        panel.add(new JLabel("ISBN:"));
        panel.add(txtIsbn);

        panel.add(new JLabel("Título:"));
        panel.add(txtTitulo);

        panel.add(new JLabel("Autor:"));
        panel.add(txtAutor);

        panel.add(new JLabel("Cantidad total:"));
        panel.add(txtCantidadTotal);

        panel.add(new JLabel("Cantidad disponible:"));
        panel.add(txtCantidadDisponible);

        panel.add(new JLabel("Imagen (ruta):"));
        panel.add(txtImagen);

        // Botones
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        panel.add(btnGuardar);
        panel.add(btnCancelar);

        add(panel);

        // Cierra solamente el diálogo, no la aplicación
        btnCancelar.addActionListener(e -> dispose());

        btnGuardar.addActionListener(e -> {

            // Recuperamos los valores ingresados en el formulario
            String isbn = txtIsbn.getText().trim();
            String titulo = txtTitulo.getText().trim();
            String autor = txtAutor.getText().trim();
            String imagen = txtImagen.getText().trim();

            // Los JTextField siempre entregan texto,
            // por eso convertimos las cantidades a enteros
            try {
                int cantidadTotal = Integer.parseInt(
                    txtCantidadTotal.getText().trim()
                );

                int cantidadDisponible = Integer.parseInt(
                    txtCantidadDisponible.getText().trim()
                );

                System.out.println("ISBN: " + isbn);
                System.out.println("Título: " + titulo);
                System.out.println("Autor: " + autor);
                System.out.println("Cantidad total: " + cantidadTotal);
                System.out.println("Cantidad disponible: " + cantidadDisponible);
                System.out.println("Imagen: " + imagen);

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                    this, 
                    "Las cantidades deben ser números enteros.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
