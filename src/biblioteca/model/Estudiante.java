package biblioteca.model;

public class Estudiante extends Usuario{

    private String carrera;

    public Estudiante(
        String nombreCompleto,
        String run,
        char genero,
        String carrera
    ) {
        // Inicializamos los atributos heredados
        super(nombreCompleto, run, genero);

        this.carrera = carrera;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    @Override
    public int getMaxDiasPrestamo() {
        return 10;
    }
}
