package com.alura.literalura.principal;

import com.alura.literalura.dto.AutorDTO;
import com.alura.literalura.dto.LibroDTO;
import com.alura.literalura.model.*;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;

    private List<Libro> libros;
    private List<Autor> autores;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository){
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {
        System.out.println("""
                 *************************************************
                🌟         ¡EXPLORA LITERALURA!                 🌟 \s
                 *   Tu portal hacia historias sin límites.      * \s
                 *   Descubre, lee y vive cada página.           * \s
                 *************************************************
                """);
        int option = -1;
        while (option != 0){
            mostrarMenu();
            option = obtenerOpcionMenu();
            procesarOpcionMenu(option);
        }
    }

    private void mostrarMenu() {
        System.out.println("""
                      MENÚ - LITERALURA                   
                1. Buscar libro por título
                2. Listar libros registrados
                3. Buscar autor por nombre
                4. Listar autores registrados
                5. Buscar libros por idiomas
                6. Buscar autores vivos en determinado año
                7. Buscar autores fallecidos en determinado año
                8. Top 10 libros más descargados
                9. Generando estadisticas
                0. Salir                                                                                       \s
                ────────────────────────────────────────────────
                Elige una opción del menú:\s""");
    }

    private int obtenerOpcionMenu() {
        String optionMenu = teclado.nextLine();
        try {
            return Integer.parseInt(optionMenu);
        } catch (NumberFormatException e) {
            System.out.println("Por favor ingrese una opción válida");
            return -1;
        }
    }

    private void procesarOpcionMenu(int option){
        switch (option) {
            case 1 -> buscarLibroPorTitulo();
            case 2 -> listarLibrosRegistrados();
            case 3 -> buscarAutoresPorNombre();
            case 4 -> listarAutoresRegistrados();
            case 5 -> buscarLibrosIdioma();
            case 6 -> buscarAutoresVivosAño();
            case 7 -> buscarAutoresFallecidosAño();
            case 8 -> top10LibrosMasDescargados();
            case 9 -> mostrarEstadisticas();
            case 0 -> System.out.println("Cerrando aplicación");
            default -> System.out.println("Opción inválida");
        }
    }

    //Creando metodos
    private void buscarLibroPorTitulo() {
        DatosLibros datosLibros = getDatosLibros();
        if (datosLibros == null) {
            System.out.println("""
                    ┌──────────────────────────────────────────────┐
                    *                LIBRO NO ENCONTRADO           *
                    └──────────────────────────────────────────────┘
                    """);
            pausa();
            return;
        }

        Optional<Libro> libroExistente = libroRepository.findByTitulo(datosLibros.titulo());
        if (libroExistente.isPresent()) {
            mostrarLibroEncontrado(libroExistente.get());
            return;
        }

        List<Autor> autores = obtetenerAutores(datosLibros.autor());
        //Validar datos del libro
        String tituloValidado = truncarSiEsNecesario(datosLibros.titulo(), 255);
        String idiomasValidados = truncarSiEsNecesario(String.join(", ", datosLibros.idiomas()), 255);

        Libro libro = new Libro(datosLibros);
        libro.setTitulo(tituloValidado);
        libro.setIdiomas(Arrays.asList(idiomasValidados.split(", ")));
        libro.setAutores(autores);
        libroRepository.save(libro);

        mostrarLibroEncontrado(libro);
    }

    private void mostrarLibroEncontrado(Libro libro){
        LibroDTO libroDTO = convertirALibroDTO(libro);
        System.out.printf("""
                ┌──────────────────────────────────────────────┐
                *                LIBRO ENCONTRADO              *
                └──────────────────────────────────────────────┘
                Título: %s
                Autor: %s
                Idioma: %s
                N° Descargas: %.2f%n""", libroDTO.titulo(),
                libroDTO.autores().stream().map(AutorDTO::nombre).collect(Collectors.joining(", ")),
                libroDTO.idiomas(),
                libroDTO.numeroDeDescargas());
        System.out.println("────────────────────────────────────────────────");
        pausa();
    }
    private LibroDTO convertirALibroDTO(Libro libro) {
        List<AutorDTO> autoresDTO = libro.getAutores().stream()
                .map(autor -> new AutorDTO(autor.getId(), autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento()))
                .collect(Collectors.toList());
        return new LibroDTO(
                libro.getId(),
                libro.getTitulo(),
                autoresDTO,
                String.join(", ", libro.getIdiomas()),
                libro.getNumeroDeDescargas()
        );
    }

    private List<Autor> obtetenerAutores(List<DatosAutor> datosAutores){
        return datosAutores.stream()
                .map(datosAutor -> autorRepository.findByNombre(datosAutor.nombre())
                    .orElseGet(() -> crearNuevoAutor(datosAutor)))
                .collect(Collectors.toList());
    }

    private Autor crearNuevoAutor(DatosAutor datosAutor){
        Autor nuevoAutor = new Autor();
        nuevoAutor.setNombre(truncarSiEsNecesario(datosAutor.nombre(), 255));
        nuevoAutor.setFechaNacimiento(datosAutor.fechaNacimiento());
        nuevoAutor.setFechaFallecimiento(datosAutor.fechaFallecimiento());
        autorRepository.save(nuevoAutor);
        return nuevoAutor;
    }

    private DatosLibros getDatosLibros(){
        System.out.println("Ingresa el nombre del libro que deseas buscar: ");
        var nombreLibro = teclado.nextLine();
        String json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombreLibro.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        return datosBusqueda.resultados().stream()
                .filter(datosLibros -> datosLibros.titulo().toUpperCase().contains(nombreLibro.toUpperCase()))
                .findFirst()
                .orElse(null);
    }

    private void listarLibrosRegistrados(){
        libros = libroRepository.findAllWithAutores();
 //       libros = libroRepository.findAll();

        if(libros.isEmpty()){
            System.out.println("No hay libros registrados con ese nombre en el sistema");
            pausa();
            return;
        }
        System.out.printf("""
        ┌──────────────────────────────────────────────┐
        *            %d LIBROS REGISTRADOS             *
        └──────────────────────────────────────────────┘
        """, libros.size());
        mostrarLibros(libros);
        pausa();
    }

    private void buscarAutoresPorNombre(){
        System.out.println("Ingresa el nombre del autor que deseas buscar: ");
        var nombreAutor = teclado.nextLine().toLowerCase();

        List<Autor> autoresBuscados = autorRepository.findByNombreContainingIgnoreCase(nombreAutor);
        System.out.printf("""
                Busqueda de autor por nombre:
                '%s'
               ──────────────────────────────%n
                """, nombreAutor);
        if (autoresBuscados.isEmpty()){
            System.out.println("""
                ┌─────────────────────────────────────────────────┐
                 NO SE ENCONTRARON AUTORES CON EL NOMBRE INGRESADO
                └─────────────────────────────────────────────────┘
                """);
            pausa();
        }else {
            mostrarAutores(autoresBuscados);
            pausa();
        }
    }

    private void listarAutoresRegistrados() {
        autores = autorRepository.findAllWithLibros();
//        autores = autorRepository.findAll();

        if (autores.isEmpty()) {
            System.out.println("""
                ┌──────────────────────────────────────────────┐
                *  NO HAY AUTORES REGISTRADOS EN EL SISTEMA    *
                └──────────────────────────────────────────────┘""");
            pausa();
            return;
        }
        System.out.printf("""
                ┌──────────────────────────────────────────────┐
                              %d AUTORES REGISTRADOS
                └──────────────────────────────────────────────┘
                %n""", autores.size());
        mostrarAutores(autores);
        pausa();
    }

    private void mostrarAutores(List<Autor> autorList){
        autorList.forEach(autor -> {
            AutorDTO autorDTO = new AutorDTO(
                    autor.getId(),
                    autor.getNombre(),
                    autor.getFechaNacimiento(),
                    autor.getFechaFallecimiento()
            );
            List<String> librosDelAutor = autor.getLibrosDelAutor().stream()
                    .map(Libro::getTitulo)
                    .collect(Collectors.toList());
            System.out.printf("""
                    Nombre: %s
                    Fecha de Nacimiento: %s
                    Fecha de Fallecimiento: %s
                    Libros: %s%n""", autorDTO.nombre(),
                    autorDTO.fechaNacimiento() != null ? autorDTO.fechaNacimiento() : "N/A",
                    autorDTO.fechaFallecimiento() != null ? autorDTO.fechaFallecimiento() : "N/A",
                    librosDelAutor
            );
            System.out.println("────────────────────────────────────────────────");
        });
    }

    private void buscarLibrosIdioma(){
        var menuIdiomas = """
                ┌───────────────────────────────────┐
                *              IDIOMAS              *
                └───────────────────────────────────┘
                            es - Español 
                            it - Italiano               
                            en - Inglés                 
                            fr - Francés 
                            pt - Portugués               
                            zh - Chino Mandarín
                            ja - Japonés          
                """;
        String idiomaLibro;
        do {
            System.out.println(menuIdiomas);
            System.out.println("Ingresa el idioma del libro a buscar [ej: es]: ");
            idiomaLibro = teclado.nextLine().toLowerCase();

            if (!idiomaLibro.matches("^[a-z]{2}$")){
                System.out.println("""
                        *******************************************
                                     CODIGO INVALIDO
                        Por favor, ingrese un código de dos letras.
                        *******************************************
                        """);
            }
        }while (!idiomaLibro.matches("^[a-z]{2}$"));
        List<Libro> librosIdioma = libroRepository.findByIdiomasContaining(idiomaLibro);
        if (librosIdioma.isEmpty()){
            System.out.println("""
                ┌──────────────────────────────────────────────┐
                 NO SE ENCONTRARON LIBROS EN EL IDIOMA BUSCADO
                └──────────────────────────────────────────────┘
                """);
            pausa();
        }else {
            if (librosIdioma.size()== 1){
                System.out.printf("""
                ┌────────────────────────────────────┐
                *     %d LIBRO EN EL IDIOMA '%S'     *
                └────────────────────────────────────┘
                %n""", librosIdioma.size(), idiomaLibro.toUpperCase());
            }else {
                System.out.printf("""
                ┌────────────────────────────────────┐
                *     %d LIBROS EN EL IDIOMA '%S'     *
                └────────────────────────────────────┘
                %n""", librosIdioma.size(), idiomaLibro.toUpperCase());
            }
            mostrarLibros(librosIdioma);
            pausa();
        }
    }

    private void buscarAutoresVivosAño(){
        boolean valorVerdadero = false;
        String autorAñoVivo;
        do {
            System.out.println("Por favor ingrese el año para buscar autores vivos: ");
            autorAñoVivo = teclado.nextLine();

            if (!validarAñoDigitos(autorAñoVivo)){
                añoNoValido();
                continue;
            }
            valorVerdadero = true;
        }while (!valorVerdadero);

        int año = Integer.parseInt(autorAñoVivo);
        List<Autor> autoresVivos = autorRepository.findAutoresVivosEnAño(String.valueOf(año));

        if (autoresVivos.isEmpty()){
            System.out.println("""
                    ┌──────────────────────────────────────────────┐
                      NO HAY AUTORES VIVOS REGISTRADOS EN ESE AÑO
                    └──────────────────────────────────────────────┘
                    """);
        }else {
            String mensaje = autoresVivos.size() == 1
                    ? "1 AUTOR VIVO REGISTRADO EN EL AÑO"
                    : autoresVivos.size() + " AUTORES VIVOS REGISTRADOS EN EL AÑO";
            System.out.printf("""
                ┌──────────────────────────────────────────────┐
                  %s %d 
                └──────────────────────────────────────────────┘
                """, mensaje, año);
            mostrarAutores(autoresVivos);
        }
        pausa();
    }

    private void buscarAutoresFallecidosAño(){
        boolean valorVerdadero = false;
        String autorAñoMuerto;
        do {
            System.out.println("Por favor ingrese el año para buscar autores fallecidos: ");
            autorAñoMuerto = teclado.nextLine();

            if (!validarAñoDigitos(autorAñoMuerto)){
                añoNoValido();
                continue;
            }
            valorVerdadero = true;
        }while (!valorVerdadero);

        int año = Integer.parseInt(autorAñoMuerto);
        List<Autor> autoresFallecidos = autorRepository.findAutoresFallecidosEnAño(String.valueOf(año));

        if (autoresFallecidos.isEmpty()){
            System.out.println("""
                    ┌────────────────────────────────────────────────┐
                     NO HAY AUTORES FALLECIDOS REGISTRADOS EN ESE AÑO
                    └────────────────────────────────────────────────┘
                    """);
        }else {
            String mensaje = autoresFallecidos.size() == 1
                    ? "1 AUTOR FALLECIDO REGISTRADO EN "
                    : autoresFallecidos.size() + " AUTORES FALLECIDOS REGISTRADOS EN";
            System.out.printf("""
                ┌──────────────────────────────────────────────┐
                  %s %d
                └──────────────────────────────────────────────┘
                """, mensaje, año);
            mostrarAutores(autoresFallecidos);
        }
        pausa();
    }

    private void mostrarLibros(List<Libro> libroList){
        libroList.forEach(libro -> {
            LibroDTO libroDTO = convertirALibroDTO(libro);
            System.out.printf("""
                    ────────────────────────────────────────────────
                    Título: %s
                    Autor: %s
                    Idioma: %s
                    N° Descargas: %.2f%n""", libroDTO.titulo(),
                    libroDTO.autores().stream().map(AutorDTO::nombre).collect(Collectors.joining(", ")),
                    String.join(", ", libro.getIdiomas()),
                    libroDTO.numeroDeDescargas());
            System.out.println("────────────────────────────────────────────────");
        });
    }



        //Top 10 libros más decargados
    private void top10LibrosMasDescargados(){
        List<Libro> top10Libros = libroRepository.findTop10ByOrderByNumeroDeDescargasDesc();
        System.out.println("""
                     ┌──────────────────────────────────────────────┐
                              TOP 10 LIBROS MAS DESCARGADOS
                     └──────────────────────────────────────────────┘
                     """);
        int contador = 1; //Inicializa el contador
        for (Libro libro : top10Libros){
            System.out.printf("%d. %s - Descargas: %.0f%n",
                            contador,
                            libro.getTitulo().toUpperCase(),
                            libro.getNumeroDeDescargas());
            contador++;
        }
        System.out.println("────────────────────────────────────────────────");
        pausa();
    }

        //Trabajando con estadisticas
    private void mostrarEstadisticas(){
//        System.out.println("Estadististicas de libros más descargados");
        mostrarEstadisticasLibrosRegistrados();
        pausa();
    }
    private void mostrarEstadisticasLibrosRegistrados(){
        DoubleSummaryStatistics estadisticasDescargas = libroRepository.findAll().stream()
                        .mapToDouble(Libro::getNumeroDeDescargas)
                                .summaryStatistics();
        System.out.println("""
             ┌──────────────────────────────────────────────┐
                ESTADISTICAS DE LIBROS MAS DESCARGADOS
             └──────────────────────────────────────────────┘
             """);
        System.out.printf("Total de libros registrados: %d%n", estadisticasDescargas.getCount());
        System.out.printf("Descargas totales: %.2f%n", estadisticasDescargas.getSum());
        System.out.printf("Descargas máximas en un libro: %.2f%n", estadisticasDescargas.getMax());
        System.out.printf("Descargas mínimas en un libro: %.2f%n", estadisticasDescargas.getMin());
        System.out.println("────────────────────────────────────────────────");
    }




    private void pausa(){
        System.out.println("\nPresione 'Enter' para continuar...");
    }

    private boolean validarAñoDigitos(String año){
        return año.matches("\\d{4}");
    }

    private void añoNoValido(){
        System.out.println("""
                Año no válido.Por favor, ingrese un año en 4 digitos.
                """);
    }

    private String truncarSiEsNecesario(String valor, int longitudMaxima){
        return (valor != null && valor.length() > longitudMaxima) ? valor.substring(0, longitudMaxima) : valor;
    }
}



