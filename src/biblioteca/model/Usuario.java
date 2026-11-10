package biblioteca.model;

public abstract class Usuario {
    private String nombreCompleto;
    private String run;
    private char genero;
    private String prestamo;


    // TODO: REVISAR CONSTRUCTOR
    // AGREGAR ARGUMENTOS PARA PROFESORES Y ESTUDIANTES
    public Usuario(String nombreCompleto, String run, char genero) {

        setNombre(nombreCompleto);

        if (validarGenero(genero) == true) {

            setGenero(genero);

        } else if (validarGenero(genero) != true) {

            System.out.println("Genero no válido");
        }

        if (validarRun(run) == true) {
            setRun(run);
        }
        
        // Usuario se inicializa sin prestamos. Es decir, habilitado para recibir prestamos.
        setPrestamo("0");
    }

    public boolean validarRun(String run) {
        // validar Run
            // validar formato y digito verificador

        // Para validar que penúltimo carácter sea un guión:
        boolean guion = false;
        
        // Para validar que último carácter este entre los permitidos:
        String permitidos = "0123456789Kk";
        boolean ultimoCaracter = false;
        
        // Para validar largo del run:
        boolean largoRun = false;

        // for loop para iterar sobre caracteres del run,
        // para así validar si guión está en posición correcta y que último carácter sea permitido.
        for (int i = 0; i < run.length(); i++) {
            
            char caracter = run.charAt(i);

            if(i == run.length() - 2 && caracter == '-') {
                
                guion = true;
                System.out.println("RUN: guión en posición válida");
            }

            // Validamos si último dígito está entre 0 al 9 o es una k
            if (
                i == run.length() - 1 && 
                (permitidos.indexOf(caracter) != -1)    // .indexof() devuelve la posición en la que se encuentra el carácter. Si no lo encuentra, devuelve -1
            ) {

                ultimoCaracter = true;
                System.out.println("RUN: último dígito válido");
                break;
            }

        }
        // Validamos largo del run para que sea igual a 9 o 10 caracteres.
        if(run.length() >= 9 || run.length() <= 10) {
            largoRun = true;
            System.out.println("RUN: largo de run válido");
        }
        // Validamos que se cumplan las tres condiciones anteriores.
        if (guion == true && ultimoCaracter == true && largoRun == true) {
            
            System.out.println("RUN: OK!");    
            return true;
        } else {
            
            System.out.println("RUN: NO válido");
            return false;
        }
    }

    public boolean validarGenero(char genero) {

        String permitidos = "MF";
        genero = Character.toUpperCase(genero);

        if (permitidos.indexOf(genero) != -1) {
            System.out.println("Genero: OK!");
            return true;
        } else {
            System.out.println("Genero: NO válido!");
            return false;
        }
    }

    public String getNombre() {

        return this.nombreCompleto;
    }
    
    public String getRun() {

        return this.run;
    }

    public char getGenero() {

        return this.genero;
    }

    public String getPrestamo() {
        
        return this.prestamo;
    }

    public void setNombre(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public void setGenero(char genero) {
        this.genero = genero;
    }

    public void setPrestamo(String prestamo) {
        this.prestamo = prestamo;
    }

    // Cada tipo de usuario define su máximo de días
    public abstract int getMaxDiasPrestamo();
}
