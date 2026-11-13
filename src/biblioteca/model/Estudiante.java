package biblioteca.model;

public class Estudiante extends Usuario{

    private String carrera;
    private String tipo = "ESTUDIANTE";

    public Estudiante(
        String nombreCompleto,
        String run,
        char genero,
        String carrera
    ) {
        // Inicializamos los atributos heredados
        super(nombreCompleto, run, genero);

        setCarrera(carrera);
    }

    @Override
    public String getTipo() {
        return "Estudiante";
    }
    
    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        if (carrera == null || carrera.isBlank()) {
            throw new IllegalArgumentException("La carrera es obligatoria");
        }
        this.carrera = carrera;
    }

    @Override
    public String toString() {
        return "Estudiante{" + super.toString() + ", carrera='" + carrera + "'}";
    }

    @Override
    public int getMaxDiasPrestamo() {
        return 10;
    }
}
