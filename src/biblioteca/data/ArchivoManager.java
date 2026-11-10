package biblioteca.data;

import biblioteca.model.Usuario;
import biblioteca.model.Docente;
import biblioteca.model.Estudiante;
import biblioteca.model.Libro;
import biblioteca.model.Prestamo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.time.LocalDate;

public class ArchivoManager {

    public ArrayList<Usuario> cargarUsuarios() {

        // Creamos una lista vacía donde guardaremos los usuarios que leamos del CSV
        ArrayList<Usuario> usuarios = new ArrayList<>();

        // Indicamos la ubicación del archivo que queremos leer
        Path ruta = Paths.get("data", "usuarios.csv");

        // Abrimos el archivo para lectura, usando UTF-8 para los caracteres especiales
        // El try-with-resources se encarga de cerrar el archivo automáticamente
        try (BufferedReader br = Files.newBufferedReader(
            ruta, StandardCharsets.UTF_8)) {

                String linea;

                // Leemos la primera línea para saltarnos el encabezado del CSV
                br.readLine();

                // Seguimos leyendo una línea a la vez hasta llegar al final del archivo (final = null)
                while ((linea = br.readLine()) != null) {

                    // Separamos los valores usando la coma como delimitador
                    // Cada posición del array corresponde a una columna del CSV
                    String[] datos = linea.split(",", -1);

                    if (datos.length != 8) {
                        throw new IOException("Fila de usuario inválida: " + linea);
                    }

                    // Guardamos los datos de cada columna en variables
                    String tipo = datos[0];
                    String nombreCompleto = datos[1];
                    String run = datos[2];
                    char genero = datos[3].charAt(0);
                    String prestamo = datos[4];

                    Usuario usuario;

                    if (tipo.equals("ESTUDIANTE")) {

                        String carrera = datos[5];

                        usuario = new Estudiante(
                            nombreCompleto, 
                            run, 
                            genero, 
                            carrera
                        );
                    } else if (tipo.equals("DOCENTE")) {

                        String profesion = datos[6];
                        String grados = datos[7];

                        usuario = new Docente(
                            nombreCompleto,
                            run,
                            genero,
                            profesion,
                            grados
                        );
                    } else {
                        throw new IOException(
                            "Tipo de usuario desconocido: " + tipo
                        );
                    }

                    // Recuperamos el préstamo que tenía guardado el usuario
                    usuario.setPrestamo(prestamo);

                    // Agregamos el objeto reconstruido a la lista
                    usuarios.add(usuario);
                }

        } catch (IOException | NumberFormatException e) {

            // Capturamos posibles errores al leer el archivo o convertir datos numéricos
            System.out.println("Error: " + e.getMessage());
        }

        // Devolvemos la lista con todos los usuarios que logramos cargar
        return usuarios;
    }

    // public ArrayList<String> cargarRun() {

    //     ArrayList<String> runLista = new ArrayList<>();

    //     Path ruta = Paths.get("data", "usuarios.csv");

    //     try (BufferedReader br = Files.newBufferedReader(
    //         ruta, StandardCharsets.UTF_8)) {

    //             String linea;

    //             br.readLine();

    //             while ((linea = br.readLine()) != null) {

    //                 // Ignoramos posibles líneas vacías
    //                 if (linea.isBlank()) {
    //                     continue;
    //                 }

    //                 String[] datos = linea.split(",");

    //                 // Comprobamos que exista la columna RUN
    //                 if (datos.length < 2) {
    //                     continue;
    //                 }

    //                 // Obtenemos el RUN y eliminamos posibles espacios
    //                 String run = datos[1].trim();

    //                 runLista.add(run);
    //             }
    //     }  catch (IOException e) {

    //         System.out.println("Error: " + e.getMessage());
    //     }

    //     return runLista;
    // }


    public void guardarUsuarios(ArrayList<Usuario> usuarios) throws IOException {

        // Indicamos dónde vamos a guardar los usuarios
        Path ruta = Paths.get("data", "usuarios.csv");

        // Nos aseguramos de que exista la carpeta data antes de escribir el archivo
        Files.createDirectories(ruta.getParent());

        // Abrimos el archivo para escritura
        // Si ya existe, su contenido anterior será reemplazado
        // El archivo se cierra automáticamente al salir del try
        try (BufferedWriter bw = Files.newBufferedWriter(
            ruta, StandardCharsets.UTF_8)) {

            // Primero escribimos los nombres de las columnas
            bw.write("nombreCompleto, run, genero, prestamo");
            bw.newLine();

            // Recorremos la lista de usuarios para guardar cada uno en el archivo
            for (Usuario usuario : usuarios) {

                String linea;

                if (usuario instanceof Estudiante) {    // Esto devuelve true cuando usuario es un objeto Estudiante.

                    // Convertimos la referencia Usuario a Estudiante (casting)
                    Estudiante estudiante = (Estudiante) usuario;

                    // Construimos una línea de texto con los atributos del usuario
                    // Separamos cada atributo con una coma para mantener el formato CSV
                    linea = 
                        "ESTUDIANTE," +
                        estudiante.getNombre() + "," +
                        estudiante.getRun() + "," +
                        estudiante.getGenero() + "," +
                        estudiante.getPrestamo() + "," +
                        estudiante.getCarrera() + ",,";
                } else if (usuario instanceof Docente) {
                    Docente docente = (Docente) usuario;

                    linea = 
                        "DOCENTE," +
                        docente.getNombre() + "," +
                        docente.getRun() + "," +
                        docente.getGenero() + "," +
                        docente.getPrestamo() + ",," +
                        docente.getProfesion() + "," +
                        docente.getGradosAcademicos();

                } else {
                    throw new IllegalArgumentException(
                        "Tipo de usuario no reconocido"
                    );
                }

                // Escribimos la línea en el archivo
                bw.write(linea);

                // Pasamos a la siguiente línea para escribir el próximo usuario
                bw.newLine();
            }
        }
    }

    public ArrayList<Libro> cargarLibros() {

        ArrayList<Libro> libros = new ArrayList<>();
        Path ruta = Paths.get("data", "libros.csv");

        try (BufferedReader br = Files.newBufferedReader(
            ruta, StandardCharsets.UTF_8)) {

            String linea;
            br.readLine();

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",", -1);

                Libro libro = new Libro(
                    datos[0],
                    datos[1],
                    datos[2],
                    Integer.parseInt(datos[3]),
                    Integer.parseInt(datos[4]),
                    datos.length > 5 ? datos[5] : ""
                );
                libros.add(libro);
            }

        } catch (IOException | NumberFormatException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return libros;
    }

    public void guardarLibros(ArrayList<Libro> libros) throws IOException {

        Path ruta = Paths.get("data", "libros.csv");
        Files.createDirectories(ruta.getParent());

        try (BufferedWriter bw = Files.newBufferedWriter(
            ruta, StandardCharsets.UTF_8)) {

            bw.write("isbn,titulo,autor,cantidadBiblioteca,cantidadDisponible,imagen");
            bw.newLine();

            for (Libro libro : libros) {
                String linea =
                    libro.getIsbn() + "," +
                    libro.getTitulo() + "," +
                    libro.getAutor() + "," +
                    libro.getCantidadBiblioteca() + "," +
                    libro.getCantidadDisponible() + "," +
                    libro.getImagen();

                bw.write(linea);
                bw.newLine();
            }
        }
    }

    public ArrayList<Prestamo> cargarPrestamos() {

        ArrayList<Prestamo> prestamos = new ArrayList<>();
        Path ruta = Paths.get("data", "prestamos.csv");

        try (BufferedReader br = Files.newBufferedReader(
            ruta, StandardCharsets.UTF_8)) {

            String linea;
            br.readLine();

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",", -1);

                Prestamo prestamo = new Prestamo(
                    datos[0],
                    datos[1],
                    LocalDate.parse(datos[2]),
                    LocalDate.parse(datos[3]),
                    Boolean.parseBoolean(datos[4])
                );
                prestamos.add(prestamo);
            }

        } catch (IOException | RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return prestamos;
    }

    public void guardarPrestamos(ArrayList<Prestamo> prestamos) throws IOException {

        Path ruta = Paths.get("data", "prestamos.csv");
        Files.createDirectories(ruta.getParent());

        try (BufferedWriter bw = Files.newBufferedWriter(
            ruta, StandardCharsets.UTF_8)) {

            bw.write("run,isbn,fechaPrestamo,fechaDevolucion,activo");
            bw.newLine();

            for (Prestamo prestamo : prestamos) {
                String linea =
                    prestamo.getRun() + "," +
                    prestamo.getIsbn() + "," +
                    prestamo.getFechaPrestamo() + "," +
                    prestamo.getFechaDevolucion() + "," +
                    prestamo.isActivo();

                bw.write(linea);
                bw.newLine();
            }
        }
    }
}

