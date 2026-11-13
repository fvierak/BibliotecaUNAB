package biblioteca.main;

import biblioteca.Biblioteca;
import biblioteca.model.Docente;
import biblioteca.model.Estudiante;
import biblioteca.model.Usuario;
import biblioteca.view.HomeFrame;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        // SwingUtilities ejecuta la interfaz en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {

            try {

                // Cargamos la biblioteca y sus datos persistentes
                Biblioteca biblioteca = new Biblioteca(); 

                HomeFrame home = new HomeFrame(biblioteca);
                home.setVisible(true);

            } catch (IOException e) {

                JOptionPane.showMessageDialog(
                    null, 
                    "No se pudo cargar la biblioteca:\n" + e.getMessage(),
                    "Error de inicio",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Usuario estudiante = new Estudiante(
        //     "Estudiante de prueba", "12345678-5", 'M', "Ingeniería"
        // );
        // Usuario docente = new Docente(
        //     "Docente de prueba", "98765432-5", 'F', "Docencia", "Doctorado"
        // );

        // System.out.println(
        //     estudiante.getNombre() + ": máximo " + estudiante.getMaxDiasPrestamo() + " días"
        // );
        // System.out.println(
        //     docente.getNombre() + ": máximo " + docente.getMaxDiasPrestamo() + " días"
        // );
    }
}


// 978607,Un Mago de Terramar,Ursula K. Le Guin,1,1,magoTerramar.jpg
