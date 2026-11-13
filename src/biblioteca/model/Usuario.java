
package biblioteca.model;

import java.util.Locale;

// Clase base para los distintos tipos de usuarios de la biblioteca.
// Es abstracta porque los usuarios concretos serán estudiantes o docentes.
public abstract class Usuario {

    // Datos que comparten todos los usuarios.
    private String nombreCompleto;
    private String run;
    private char genero;
    private String prestamo;

    // Constructor: utiliza los setters para validar los datos al crear el usuario.
    public Usuario(String nombreCompleto, String run, char genero) {
        setNombre(nombreCompleto);
        setRun(run);
        setGenero(genero);
        setPrestamo("0"); // El usuario comienza sin préstamos activos.
    }

    // Comprueba si un RUN es válido sin lanzar excepciones al código que lo llama.
    public static boolean validarRun(String run) {
        try {
            normalizarRun(run);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Valida el formato y el dígito verificador del RUN.
    // Si es correcto, devuelve una versión sin puntos y con guion.
    public static String normalizarRun(String run) {
        if (run == null) {
            throw new IllegalArgumentException("El RUN es obligatorio");
        }

        // Quitamos espacios externos y pasamos la K a mayúscula.
        String runNormalizado = run.trim().toUpperCase(Locale.ROOT);

        // Aceptamos RUN con o sin puntos, pero siempre con guion.
        String formatoValido = "(?:[1-9][0-9]{6,7}|[1-9][0-9]{0,1}(?:\\.[0-9]{3}){2})-[0-9K]";
        if (!runNormalizado.matches(formatoValido)) {
            throw new IllegalArgumentException("El formato del RUN no es válido");
        }

        // Separamos el número principal del dígito verificador.
        String runSinPuntos = runNormalizado.replace(".", "");
        String[] partes = runSinPuntos.split("-");
        String cuerpo = partes[0];

        if (cuerpo.length() > 8) {
            throw new IllegalArgumentException("El RUN no puede tener más de ocho dígitos");
        }

        // Calculamos el dígito verificador usando el módulo 11.
        // Los multiplicadores van del 2 al 7 y se repiten de derecha a izquierda.
        int suma = 0;
        int multiplicador = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.digit(cuerpo.charAt(i), 10) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }

        // Convertimos el resultado al dígito correspondiente: 0, K o número.
        int resultado = 11 - suma % 11;
        char digitoEsperado = resultado == 11 ? '0'
            : resultado == 10 ? 'K' : (char) ('0' + resultado);

        // Rechazamos el RUN si el dígito ingresado no coincide con el calculado.
        if (partes[1].charAt(0) != digitoEsperado) {
            throw new IllegalArgumentException("El dígito verificador del RUN no es válido");
        }

        // Todos los RUN válidos quedan almacenados con el mismo formato.
        return cuerpo + "-" + digitoEsperado;
    }

    // Por ahora, el sistema solo admite los valores M y F.
    public static boolean validarGenero(char genero) {
        char generoNormalizado = Character.toUpperCase(genero);
        return generoNormalizado == 'M' || generoNormalizado == 'F';
    }

    // Getters: permiten consultar los atributos privados del usuario.
    public abstract String getTipo() {
        
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

    // Setters: permiten modificar los datos aplicando las validaciones necesarias.
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

    // Cada tipo de usuario debe indicar cuántos días puede mantener un préstamo.
    public abstract int getMaxDiasPrestamo();

    // Representación en texto de los datos del usuario, útil para depuración.
    @Override
    public String toString() {
        return "Usuario{nombreCompleto='" + nombreCompleto + "', run='" + run
            + "', genero=" + genero + ", prestamo='" + prestamo + "'}";
    }
}
