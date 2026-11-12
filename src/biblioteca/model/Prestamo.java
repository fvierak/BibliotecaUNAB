package biblioteca.model;

import java.time.LocalDate;

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
        return "=== TARJETA DE PRESTAMO ===\n"
            + "RUN: " + run + "\n"
            + "ISBN: " + isbn + "\n"
            + "Fecha de prestamo: " + fechaPrestamo + "\n"
            + "Fecha limite de devolucion: " + fechaDevolucion;
    }
}
