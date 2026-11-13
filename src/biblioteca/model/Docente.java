package biblioteca.model;

public class Docente extends Usuario{

    private String tipo = "DOCENTE";
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

        setProfesion(profesion);
        setGradosAcademicos(gradosAcademicos);
    }

    @Override
    public String getTipo() {
        return "Docente";
    }

    public String getProfesion() {
        return profesion;
    }

    public String getGradosAcademicos() {
        return gradosAcademicos;
    }

    public void setProfesion(String profesion) {
        if (profesion == null || profesion.isBlank()) {
            throw new IllegalArgumentException("La profesión es obligatoria");
        }
        this.profesion = profesion;
    }

    public void setGradosAcademicos(String gradosAcademicos) {
        if (gradosAcademicos == null || gradosAcademicos.isBlank()) {
            throw new IllegalArgumentException("Los grados académicos son obligatorios");
        }
        this.gradosAcademicos = gradosAcademicos;
    }

    @Override
    public String toString() {
        return "Docente{" + super.toString() + ", profesion='" + profesion
            + "', gradosAcademicos='" + gradosAcademicos + "'}";
    }

    @Override
    public int getMaxDiasPrestamo() {
        return 20;
    }
}
