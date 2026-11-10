package biblioteca.model;

public class Docente extends Usuario{

    private String profesion;
    private String gradosAcademicos;    // "Magíster", "Doctorado" o "Magíster y Doctorado"

    public Docente(
        String nombreCompleto,
        String run,
        char genero,
        String profesion,
        String gradosAcademicos
    ) {
        super(nombreCompleto, run, genero);

        this.profesion = profesion;
        this.gradosAcademicos = gradosAcademicos;
    }

    public String getProfesion() {
        return profesion;
    }

    public String getGradosAcademicos() {
        return gradosAcademicos;
    }

    @Override
    public int getMaxDiasPrestamo() {
        return 20;
    }
}
