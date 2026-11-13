package biblioteca.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Prestamo {

    private String run;
    private String isbn;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private boolean activo;

    public Prestamo(String run, String isbn, LocalDate fechaPrestamo,
            LocalDate fechaDevolucion, boolean activo) {
        this.run = run;
        this.isbn = isbn;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.activo = activo;
    }

    public String getRun() { return run; }
    public void setRun(String run) { this.run = run; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String generarTarjeta() {
        return generarTarjeta("No informado");
    }

    public String generarTarjeta(String tituloLibro) {
        return generarTarjeta(tituloLibro, "Usuario con RUN " + run);
    }

    public String generarTarjeta(String tituloLibro, String nombreUsuario) {
        long diasPrestamo = ChronoUnit.DAYS.between(fechaPrestamo, fechaDevolucion);
        return "=== TARJETA DE PRESTAMO ===\n"
            + "Título: " + tituloLibro + "\n"
            + "Usuario: " + nombreUsuario + "\n"
            + "RUN: " + run + "\n"
            + "ISBN: " + isbn + "\n"
            + "Fecha de prestamo: " + fechaPrestamo + "\n"
            + "Plazo: " + diasPrestamo + " días\n"
            + "Fecha limite de devolucion: " + fechaDevolucion;
    }
}
