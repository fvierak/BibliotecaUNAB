package biblioteca.view;

import biblioteca.Biblioteca;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class UsuariosDialog extends JDialog {

    public UsuariosDialog(JFrame parent, Biblioteca biblioteca) {
        super(parent, "Gestión de usuarios", true);
        setSize(360, 300);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JButton btnRegistrar = new JButton("Registrar usuario");
        JButton btnBuscar = new JButton("Buscar usuario");
        JButton btnEditar = new JButton("Editar usuario");
        JButton btnEliminar = new JButton("Eliminar usuario");
        panel.add(btnRegistrar);
        panel.add(btnBuscar);
        panel.add(btnEditar);
        panel.add(btnEliminar);
        add(panel);

        btnRegistrar.addActionListener(e ->
            new RegistrarUsuarioDialog(parent, biblioteca).setVisible(true));
        btnBuscar.addActionListener(e ->
            new BuscarUsuarioDialog(parent, biblioteca).setVisible(true));
        btnEditar.addActionListener(e ->
            new EditarUsuarioDialog(parent, biblioteca).setVisible(true));
        btnEliminar.addActionListener(e ->
            new EliminarUsuarioDialog(parent, biblioteca).setVisible(true));
    }
}
