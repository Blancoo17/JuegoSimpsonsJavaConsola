import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class JuegoSimpsons {

    private static final int TAMANO_TABLERO = 6;

    // Los códigos de color para dibujar los caracteres en el tablero

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_BLACK = "\u001B[30m";

    private static boolean victoriaJugador1 = false;
    private static boolean victoriaJugador2 = false;

    private static int vidasJugador1 = 3;
    private static int vidasJugador2 = 3;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        // Selección de dificultad, y el número de enemigos en el tablero para cada una
        String[] nivelesDificultad = {"Fácil", "Normal", "Difícil"};
        int[] enemigosPorDificultad = {8, 12, 16};
        System.out.println("\nElige un nivel de dificultad:");
        for (int i = 0; i < nivelesDificultad.length; i++) {
            System.out.println((i + 1) + ". " + nivelesDificultad[i]);
        }
        int dificultadSeleccionada = seleccionarOpcion(scanner, nivelesDificultad.length);
        int numeroEnemigos = enemigosPorDificultad[dificultadSeleccionada - 1];

        // Opciones de personajes y enemigos entre los que elegir
        String[] personajes = {"Bart", "Homer", "Marge", "Abuelo"};
        String[] enemigos = {"Krusty", "Flanders", "Wiggun", "Otto"};

        // Elección del personaje y el enemigo del jugador 1. 
        // Quito de las posibles opciones a las que elija, para que el jugador 2 no pueda escoger las mismas 
        System.out.println("\nJugador 1, elige tu personaje:");
        mostrarOpciones(personajes);
        int personaje1Index = seleccionarOpcion(scanner, personajes.length) - 1;
        String personaje1 = personajes[personaje1Index];
        eliminarOpcion(personajes, personaje1Index);

        System.out.println("\nJugador 1, elige a tu enemigo:");
        mostrarOpciones(enemigos);
        int enemigo1Index = seleccionarOpcion(scanner, enemigos.length) - 1;
        String enemigo1 = enemigos[enemigo1Index];
        eliminarOpcion(enemigos, enemigo1Index);

        // Elección del personaje y el enemigo del jugador 2. 
        // No puede elegir los mismos que el jugador 1
        System.out.println("\nJugador 2, elige tu personaje:");
        mostrarOpciones(personajes);
        int personaje2Index = seleccionarOpcion(scanner, personajes.length) - 1;
        String personaje2 = personajes[personaje2Index];
        
        System.out.println("\nJugador 2, elige a tu enemigo:");
        mostrarOpciones(enemigos);
        int enemigo2Index = seleccionarOpcion(scanner, enemigos.length) - 1;
        String enemigo2 = enemigos[enemigo2Index];
        
        // Creo un tablero para cada jugador
        char[][] tablero1 = crearTablero(random, personaje1.charAt(0), enemigo1.charAt(0),numeroEnemigos);
        char[][] tablero2 = crearTablero(random, personaje2.charAt(0), enemigo2.charAt(0), numeroEnemigos);
        
        boolean turnoJugador1 = true;

        // Mientras que no pierda o gane uno, se juega indefinidamente alternando los turnos
        while (!victoriaJugador1 && !victoriaJugador2) {
            if (turnoJugador1) {
                ejecutarTurno(scanner, random, tablero1, personaje1, enemigo1, turnoJugador1);
                turnoJugador1 = false;
            } else {
                ejecutarTurno(scanner, random, tablero2, personaje2, enemigo2, turnoJugador1);
                turnoJugador1 = true;
            }
        }

        producirSonido("salir.wav");
        scanner.close();
    }

    /**
     * Función para procesar la selección de la opción de dificultad por parte del usuario.
     * Mientras que no elija una de las que se le dan, se le volverá a pedir que introduzca el número
     */
    private static int seleccionarOpcion(Scanner scanner, int maxOpciones) {
        int opcion;
        while (true) {
            System.out.print("Ingresa el número de tu elección: ");
            if (scanner.hasNextInt()) {
                opcion = scanner.nextInt();
                if (opcion >= 1 && opcion <= maxOpciones) {
                    return opcion;
                } else {
                    System.out.println("Por favor, selecciona un número válido.");
                }
            } else {
                System.out.println("Entrada no válida. Por favor, ingresa un número.");
                scanner.next();
            }
        }
    }

    /*
     * Función para mostrar al usuario un serie de opciones (usado para listar la dificultad)
     */
    private static void mostrarOpciones(String[] opciones) {
        for (int i = 0; i < opciones.length; i++) {
            if (opciones[i] != null) {
                System.out.println((i + 1) + ". " + opciones[i]);
            }
        }
    }

    /*
     * Función para eliminar la opción que está en el índice index del array
     */
    private static void eliminarOpcion(String[] opciones, int index) {
        opciones[index] = null;
    }

    /*
     * Función para crear el tablero de un jugador, solo necesita la inicial de su personaje, la del enemigo y el número de estos
     */
    private static char[][] crearTablero(Random random, char inicialPersonaje,char inicialEnemigo, int numeroEnemigos) {
        char[][] tablero = new char[TAMANO_TABLERO][TAMANO_TABLERO];

        // Empezamos llenando el tablero de 'L', que ya escribiremos encima cuando sea necesario
        for (int i = 0; i < TAMANO_TABLERO; i++) {
            for (int j = 0; j < TAMANO_TABLERO; j++) {
                tablero[i][j] = 'L';
            }
        }

        // Ponemos al personaje en una posición aleatoria
        int xPersonaje = random.nextInt(TAMANO_TABLERO);
        int yPersonaje = random.nextInt(TAMANO_TABLERO);
        tablero[xPersonaje][yPersonaje] = inicialPersonaje;

        // Y dos pociones de vida en otras dos posiciones aleatorias, aunque a partir de este caso
        // hay que tener cuidado con no poner los nuevos elementos en una casilla que ya contenga otros.
        // Para eso he creado la función colocarElementoAleatorio
        colocarElementoAleatorioConCuidado(tablero, random, 'V');
        colocarElementoAleatorioConCuidado(tablero, random, 'V');

        // Lo mismo con la salida
        colocarElementoAleatorioConCuidado(tablero, random, 'S');

        // Y con los enemigos
        for (int i = 0; i < numeroEnemigos; i++) {
            colocarElementoAleatorioConCuidado(tablero, random, inicialEnemigo);
        }

        return tablero;
    }

    /*
     * El objetivo de esta función es colocar un elemento (su inicial, concretamente) en una posición aleatoria del
     * tablero teniendo cuidado de no chafar otro elemento que ya esté colocado previamente. Sólo lo ponemos en una 
     * casilla que tenga una "L" dentro
     */
    private static void colocarElementoAleatorioConCuidado(char[][] tablero, Random random, char elemento) {
        int x, y;
        do {
            x = random.nextInt(TAMANO_TABLERO);
            y = random.nextInt(TAMANO_TABLERO);
        } while (tablero[x][y] != 'L');
        tablero[x][y] = elemento;
    }

    /*
     * Función para procesar el turno de un jugador.
     */
    private static void ejecutarTurno(Scanner scanner, Random random, char[][] tablero, String jugador, String enemigo, boolean turnoJugador1) {
        
        // Primero mostramos a quién le toca jugar, sus vidas restantes y su tablero
        System.out.println("\nTurno de " + jugador);
        if (turnoJugador1) {
            System.out.println("Vidas restantes: " + vidasJugador1);
        } else {
            System.out.println("Vidas restantes: " + vidasJugador2);
        }
        mostrarTablero(tablero, false, jugador, enemigo);

        // Si el jugador introduce una T se activa el modo trucos y podrá ver los enemigos que hay en las casillas, aunque pasará el turno
        System.out.println("Introduce el movimiento que deseas realizar (maximo de 3 casillas, introduce T para el modo trucos): ");
        String movimiento = scanner.next().toUpperCase();

        if (movimiento.equals("T")) {
            System.out.println("Modo trucos activado. Mostrando enemigos:");
            mostrarTablero(tablero, true, jugador, enemigo);
        } else {
            // Si no era una V, procesamos el movimiento elegido por el jugador
            int pasos = Character.getNumericValue(movimiento.charAt(0));
            char direccion = movimiento.charAt(1);
    
            moverJugador(tablero, pasos, direccion, jugador, enemigo, turnoJugador1);
            mostrarTablero(tablero, false, jugador, enemigo);
        }
        
        // Ponemos una pequeña "pausa" para que el jugador pueda ver el resultado de sus acciones
        System.out.println("Pulsa intro para continuar...");
        scanner.nextLine();
        scanner.nextLine();
    }

    /*
     * Función para dibujar el tablero en la consola, con todos sus elementos actuales
     */
    private static void mostrarTablero(char[][] tablero, boolean modoTrucosActivado, String jugador, String enemigo) {
        for (int i = 0; i < TAMANO_TABLERO; i++) {
            for (int j = 0; j < TAMANO_TABLERO; j++) {
                // Recorremos una a una las celdas del tablero y dibujamos en la
                // consola la letra que tienen guardada con el color correspondiente
                char celda = tablero[i][j];
                switch (celda) {
                    case 'L':
                        System.out.print(ANSI_GREEN + "L" + ANSI_RESET + " ");
                        break;
                    case 'V':
                        System.out.print(ANSI_RED + "V" + ANSI_RESET + " ");
                        break;
                    case 'S':
                        System.out.print(ANSI_BLUE + "S" + ANSI_RESET + " ");
                        break;
                    default:
                        // Aquí procesamos los casos de las letras "variables", ya que no 
                        // se puede usar una variable directamente en una cláusula "case" 
                        if ((celda == enemigo.charAt(0)) && modoTrucosActivado) {
                            System.out.print(ANSI_BLACK + enemigo.charAt(0) + ANSI_RESET + " ");
                            break;
                        } else if (celda == jugador.charAt(0)) {
                            System.out.print(ANSI_YELLOW + jugador.charAt(0) + ANSI_RESET + " ");
                            break;
                        } else {
                            System.out.print(ANSI_GREEN + "L" + ANSI_RESET + " ");
                            break;
                        }                        
                }
            }
            System.out.println();
        }
    }

    /*
     * Función para procesar el movimiento del jugador, sustituyendo el contenido de las 
     * correspondientes celdas del tablero conforme haga falta y emitiendo sonidos según toque
     */
    private static void moverJugador(char[][] tablero, int pasos, char direccion, String jugador, String enemigo, boolean turnoJugador1) {
        int xJugador = -1, yJugador = -1;

        // Primero evitamos que el jugador pueda moverse más o menos 
        // de la cuenta, ajustando el movimiento a algo "legal"
        if (pasos > 3) {
            pasos = 3;
        } else if (pasos < 1) {
            pasos = 1;
        }

        // Buscamos las coordenadas en las que está el jugador antes de moverse
        for (int i = 0; i < TAMANO_TABLERO; i++) {
            for (int j = 0; j < TAMANO_TABLERO; j++) {
                if (tablero[i][j] == jugador.charAt(0)) {
                    xJugador = i;
                    yJugador = j;
                    break;
                }
            }
        }

        // Hacemos que la casilla desde la que se mueve se quede vacía
        tablero[xJugador][yJugador] = 'L';

        // Actualizamos las coordenadas del jugador, teniendo en cuenta
        // que si se sale por un lado tiene que aparecer por el opuesto
        for (int i = 0; i < pasos; i++) {
            switch (direccion) {
                case 'W':
                    xJugador = (xJugador - 1 + TAMANO_TABLERO) % TAMANO_TABLERO;
                    break;
                case 'S':
                    xJugador = (xJugador + 1) % TAMANO_TABLERO;
                    break;
                case 'A':
                    yJugador = (yJugador - 1 + TAMANO_TABLERO) % TAMANO_TABLERO;
                    break;
                case 'D':
                    yJugador = (yJugador + 1) % TAMANO_TABLERO;
                    break;
            }
        }

        // Para cada jugador, tenemos en cuenta si en la casilla a la que se va a mover hay un enemigo, una poción de vida, 
        // la salida o nada, para proceder a actualizar sus vidas, que suene el sonido correspondiente o darle la victoria
        if (turnoJugador1) {
            if (tablero[xJugador][yJugador] == enemigo.charAt(0)) {                
                vidasJugador1--;
                System.out.println("¡Te has encontrado con tu enemigo " + enemigo + " y has perdido una vida!");
                producirSonido("encuentroEnemigo.wav");
            } else if (tablero[xJugador][yJugador] == 'V') {
                vidasJugador1++;
                System.out.println("¡Cogiste una poción y has ganado una vida!");
                producirSonido("cogerPocion.wav");
            } else if (tablero[xJugador][yJugador] == 'S') {
                victoriaJugador1 = true;
                System.out.println("El jugador 1, " + jugador + ", ha conseguido llegar a la salida. ¡Juego terminado!");
                producirSonido("victoria.wav");
            } else {
                producirSonido("moverse.wav");
            }
        } else {
            if (tablero[xJugador][yJugador] == enemigo.charAt(0)) {                
                vidasJugador2--;
                System.out.println("¡Te has encontrado con tu enemigo " + enemigo + " y has perdido una vida!");
                producirSonido("encuentroEnemigo.wav");
            } else if (tablero[xJugador][yJugador] == 'V') {
                vidasJugador2++;
                System.out.println("¡Cogiste una poción y has ganado una vida!");
                producirSonido("cogerPocion.wav");
            } else if (tablero[xJugador][yJugador] == 'S') {
                victoriaJugador2 = true;
                System.out.println("El jugador 2, " + jugador + ", ha conseguido llegar a la salida. ¡Juego terminado!");
                producirSonido("victoria.wav");
            } else {
                producirSonido("moverse.wav");
            }
        }

        //Si tras el movimiento el jugador ha perdido su última vida, se le da la victoria al rival 
        if (vidasJugador1 <= 0) {
            victoriaJugador2 = true;
            System.out.println("El jugador 1, " + jugador + ", ha perdido todas sus vidas. ¡Juego terminado!");
            producirSonido("derrota.wav");
        }

        if (vidasJugador2 <= 0) {
            victoriaJugador1 = true;
            System.out.println("El jugador 2, " + jugador + ", ha perdido todas sus vidas. ¡Juego terminado!");
            producirSonido("derrota.wav");
        }

        // Finalmente, ponemos al jugador en su nueva casilla
        tablero[xJugador][yJugador] = jugador.charAt(0);

    }

    /*
     * Función para producir un cierto sonido a partir del nombre del fichero en el que está
     */
    private static void producirSonido(String nombreFichero) {
        try {
            // Cargamos el fichero y los datos de audio que contiene
            File soundFile = new File(nombreFichero);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            // Iniciamos el sonido y hacemos que el programa espere mientras se escucha
            clip.start();            
            Thread.sleep(clip.getMicrosecondLength() / 1000);
            clip.close();

            audioInputStream.close();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}