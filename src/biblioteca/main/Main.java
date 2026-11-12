package biblioteca.main;

import biblioteca.model.Docente;
import biblioteca.model.Estudiante;
import biblioteca.model.Usuario;
import biblioteca.view.HomeFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        // SwingUtilities ejecuta la interfaz en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {

            HomeFrame home = new HomeFrame();

            home.setVisible(true);
        });

        Usuario estudiante = new Estudiante(
            "Estudiante de prueba", "12345678-5", 'M', "Ingeniería"
        );
        Usuario docente = new Docente(
            "Docente de prueba", "98765432-5", 'F', "Docencia", "Doctorado"
        );

        System.out.println(
            estudiante.getNombre() + ": máximo " + estudiante.getMaxDiasPrestamo() + " días"
        );
        System.out.println(
            docente.getNombre() + ": máximo " + docente.getMaxDiasPrestamo() + " días"
        );
    }
}
