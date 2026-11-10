package biblioteca.model;

import java.util.Locale;

public abstract class Usuario {
    private String nombreCompleto;
    private String run;
    private char genero;
    private String prestamo;

    public Usuario(String nombreCompleto, String run, char genero) {
        setNombre(nombreCompleto);
        setRun(run);
        setGenero(genero);
        setPrestamo("0");
    }

    public static boolean validarRun(String run) {
        try {
            normalizarRun(run);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String normalizarRun(String run) {
        if (run == null) {
            throw new IllegalArgumentException("El RUN es obligatorio");
        }

        String runNormalizado = run.trim().toUpperCase(Locale.ROOT);
        String formatoValido = "(?:[1-9][0-9]{6,7}|[1-9][0-9]{0,1}(?:\\.[0-9]{3}){2})-[0-9K]";
        if (!runNormalizado.matches(formatoValido)) {
            throw new IllegalArgumentException("El formato del RUN no es válido");
        }

        String runSinPuntos = runNormalizado.replace(".", "");
        String[] partes = runSinPuntos.split("-");
        String cuerpo = partes[0];
        if (cuerpo.length() > 8) {
            throw new IllegalArgumentException("El RUN no puede tener más de ocho dígitos");
        }

        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.digit(cuerpo.charAt(i), 10) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }

        int resultado = 11 - suma % 11;
        char digitoEsperado = resultado == 11 ? '0'
            : resultado == 10 ? 'K' : (char) ('0' + resultado);
        if (partes[1].charAt(0) != digitoEsperado) {
            throw new IllegalArgumentException("El dígito verificador del RUN no es válido");
        }

        return cuerpo + "-" + digitoEsperado;
    }

    public static boolean validarGenero(char genero) {
        char generoNormalizado = Character.toUpperCase(genero);
        return generoNormalizado == 'M' || generoNormalizado == 'F';
    }

    public String getNombre() {
        return nombreCompleto;
    }

    public String getRun() {
        return run;
    }

    public char getGenero() {
        return genero;
    }

    public String getPrestamo() {
        return prestamo;
    }

    public void setNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }
        this.nombreCompleto = nombreCompleto;
    }

    public void setRun(String run) {
        this.run = normalizarRun(run);
    }

    public void setGenero(char genero) {
        if (!validarGenero(genero)) {
            throw new IllegalArgumentException("El género debe ser M o F");
        }
        this.genero = Character.toUpperCase(genero);
    }

    public void setPrestamo(String prestamo) {
        this.prestamo = prestamo;
    }

    public abstract int getMaxDiasPrestamo();

    @Override
    public String toString() {
        return "Usuario{nombreCompleto='" + nombreCompleto + "', run='" + run
            + "', genero=" + genero + ", prestamo='" + prestamo + "'}";
    }
}
