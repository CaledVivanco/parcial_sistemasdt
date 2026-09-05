package servidor;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

/**
 * Servidor de calculadora remota.
 *
 * Conceptos de la Unidad 1 aplicados:
 *  - Uso de ServerSocket para escuchar conexiones entrantes en un puerto TCP.
 *  - Uso de Socket para representar cada conexión aceptada.
 *  - Concurrencia: cada cliente se atiende en un Thread independiente,
 *    de modo que el servidor puede procesar varias solicitudes al mismo tiempo.
 *  - Manejo de excepciones de E/S (IOException).
 */
public class ServidorApp {

    public static final int PUERTO = 5000;

    public static void main(String[] args) {
        // Fuerza salida de consola en UTF-8 para que tildes y "ñ" se vean bien en el log.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        System.out.println("=== Servidor de Calculadora Remota ===");
        System.out.println("Iniciando servidor en el puerto " + PUERTO + " ...");

        // ServerSocket queda escuchando peticiones de conexión en el puerto indicado.
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor listo. Esperando clientes...");

            // Bucle infinito: el servidor sigue vivo aceptando clientes uno tras otro.
            while (true) {
                // accept() bloquea la ejecución hasta que llega un cliente nuevo.
                Socket socketCliente = serverSocket.accept();
                log("Nueva conexion aceptada desde " + socketCliente.getInetAddress().getHostAddress()
                        + ":" + socketCliente.getPort());

                // Cada cliente se procesa en su propio hilo para no bloquear a los demás.
                Thread hilo = new Thread(new ManejadorCliente(socketCliente));
                hilo.start();
            }

        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    /** Imprime un mensaje de log con marca de tiempo, útil para la sustentación en video. */
    public static void log(String mensaje) {
        System.out.println("[" + LocalTime.now().withNano(0) + "] " + mensaje);
    }
}
