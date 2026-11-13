package biblioteca.view;

import biblioteca.Biblioteca;
import biblioteca.model.Libro;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class LibroDialog extends JDialog{

    public LibroDialog(JFrame parent, Biblioteca biblioteca) {

        // Indicamos la ventana padre, títullo y modalidad
        super(parent, "Gestión de libros", true);

        setSize(450, 400);
        setLocationRelativeTo(parent);

        // Panel principal con márgenes
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
    
        // 6 filas y 2 columnas
        panel.setLayout(new GridLayout(6, 2, 10, 10));

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
        JButton btnBuscar = new JButton("Buscar");

        // Creamos un panel separado para los botones
        JPanel panelBotones = new JPanel();

        panelBotones.setLayout(new GridLayout(1, 3, 10, 10));

        // añadimos márgenes al panel inferior sin modificar los del formulario
        panelBotones.setBorder(new EmptyBorder(0, 20, 20, 20));

        // Agregamos los botones al nuevo panel
        panelBotones.add(btnBuscar);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        // El formulario ocupará el centro de la ventana
        add(panel, BorderLayout.CENTER);

        // Los botones quedarán en la parte inferior
        add(panelBotones, BorderLayout.SOUTH);

        // Cierra solamente el diálogo, no la aplicación
        btnCancelar.addActionListener(e -> dispose());

        btnBuscar.addActionListener(e -> {

            // Recuperamos el ISBN ingresado
            String isbn = txtIsbn.getText().trim();

            // Buscamos el libro en la biblioteca
            Libro libro = biblioteca.buscarLibro(isbn);

            // Si no existe, mostramos un mensaje y terminamos
            if (libro == null) {

                JOptionPane.showMessageDialog(
                    this, 
                    "No se encontró un libro con ese ISBN.",
                    "Libro no encontrado",
                    JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            // Si existe, completamos los campos con sus atributos
            txtTitulo.setText(libro.getTitulo());
            txtAutor.setText(libro.getAutor());

            // Convertimos los números a String para mostrarlos
            txtCantidadTotal.setText(
                String.valueOf(libro.getCantidadBiblioteca())
            );

            txtCantidadDisponible.setText(
                String.valueOf(libro.getCantidadDisponible())
            );

            txtImagen.setText(
                libro.getImagen() == null ? "" : libro.getImagen()  // Operador ternario: Si la imagen es nell, mostrar texto vacío. De lo contrario, mostrar su ruta.
            );
        });

        btnGuardar.addActionListener(e -> {

            // Recuperamos los valores ingresados en el formulario
            String isbn = txtIsbn.getText().trim();
            String titulo = txtTitulo.getText().trim();
            String autor = txtAutor.getText().trim();
            Integer cantidadTotal = Integer.parseInt(txtCantidadTotal.getText().trim());
            Integer cantidadDisponible = Integer.parseInt(txtCantidadDisponible.getText().trim());
            String imagen = txtImagen.getText().trim();

            try {
            
                // Creamos el objeto Libro con los datos del formulario
                Libro nuevoLibro = new Libro(
                isbn, 
                titulo, 
                autor, 
                cantidadTotal, 
                cantidadDisponible, 
                imagen
                );

                // Intentamos registrar el libro en la biblioteca
                boolean creado = biblioteca.agregarLibro(nuevoLibro);

                if (creado) {

                    JOptionPane.showMessageDialog(
                        this,
                        "Libro registrado correctamente."
                    );

                    dispose();

                } else {

                    JOptionPane.showMessageDialog(
                        this, 
                        "Ya existe un libro con ese ISBN.",
                        "ISBN duplicado",
                        JOptionPane.WARNING_MESSAGE
                    );
                }  

            } catch (NumberFormatException ex) {

                // Ocurre cuando una cantidad no es un entero válido
                JOptionPane.showMessageDialog(
                    this, 
                    "Las cantidades deben ser números enteros.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );

            } catch (IllegalArgumentException ex) {

                // Capturamos errores de validación de los datos
                JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Datos inválidos",
                    JOptionPane.ERROR_MESSAGE
                );

            }catch (IOException ex) {

                // Capturamos problemas al guardar los archivos CSV
                JOptionPane.showMessageDialog(
                    this, 
                    "Error al guardar el archivo:\n" + ex.getMessage(),
                    "Error de persistencia",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
