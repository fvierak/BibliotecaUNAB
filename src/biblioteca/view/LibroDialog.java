package biblioteca.view;

import biblioteca.Biblioteca;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;

public class LibroDialog extends JDialog {

    public LibroDialog(JFrame parent, Biblioteca biblioteca) {

        super(parent, "Gestión de libros", true);

        setSize(350, 300);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JButton btnRegistrar = new JButton("Registrar libro");
        JButton btnBuscar = new JButton("Buscar libro");
        JButton btnEliminar = new JButton("Eliminar libro");

        panel.add(btnRegistrar);
        panel.add(btnBuscar);
        panel.add(btnEliminar);

        add(panel);

        btnRegistrar.addActionListener(e -> {

            RegistrarLibroDialog dialog =
                new RegistrarLibroDialog(parent, biblioteca);

            dialog.setVisible(true);
        });

        btnBuscar.addActionListener(e -> {

            BuscarLibroDialog dialog =
                new BuscarLibroDialog(parent, biblioteca);

            dialog.setVisible(true);
        });

        btnEliminar.addActionListener(e -> {

            EliminarLibroDialog dialog =
                new EliminarLibroDialog(parent, biblioteca);

            dialog.setVisible(true);
        });
    }
}