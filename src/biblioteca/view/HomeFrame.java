package biblioteca.view;

import javax.swing.border.EmptyBorder;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.GridLayout;

public class HomeFrame extends JFrame{

    public HomeFrame() {

        // Config. básica de la ventana
        setTitle("Biblioteca UNAB");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel que contendrá los componentes
        JPanel panel = new JPanel();

        // Agregamos márgenes internos al panel
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));   // EmptyBorder(arriba, izquierda, abajo, derecha);

        // Organizamos loss componentes en 5 filas de la interfaz
        panel.setLayout(new GridLayout(5, 1, 10, 10));

        // Creamos los elementos de la interfaz
        JLabel titulo = new  JLabel("Sistema de Biblioteca UNAB");

        JButton btnUsuarios = new JButton("Ususarios");
        JButton btnLibros = new JButton("Libros");
        JButton btnPrestamos = new JButton("Préstamos");
        JButton btnDevoluciones = new JButton("Devoluciones");

        // Agregamos los elementos al panel
        panel.add(titulo);
        panel.add(btnUsuarios);
        panel.add(btnLibros);
        panel.add(btnPrestamos);
        panel.add(btnDevoluciones);

        // Incorporamos el panel a la ventana
        add(panel);
    }

}
