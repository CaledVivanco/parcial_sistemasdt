package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Runnable que atiende UNA conexión de cliente.
 *
 * Protocolo de comunicación (texto plano, una petición por línea):
 *   Petición del cliente:  OPERACION;NUM1;NUM2
 *     OPERACION = SUMA | RESTA | MULTIPLICACION | DIVISION
 *   Respuesta del servidor: OK;resultado
 *                       o   ERROR;mensaje de error
 *
 * El cliente puede enviar varias peticiones por la misma conexión;
 * la conexión finaliza cuando el cliente envía "SALIR" o cierra el socket.
 */
public class ManejadorCliente implements Runnable {

    private final Socket socket;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String direccion = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();

        // try-with-resources cierra automáticamente streams y socket al terminar.
        try (
                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String linea;
            // El servidor sigue leyendo líneas mientras el cliente no cierre la conexión.
            while ((linea = entrada.readLine()) != null) {
                ServidorApp.log("Peticion de " + direccion + " -> " + linea);

                if ("SALIR".equalsIgnoreCase(linea.trim())) {
                    ServidorApp.log("Cliente " + direccion + " solicitó cerrar la conexión.");
                    break;
                }

                String respuesta = procesarPeticion(linea);
                salida.println(respuesta);
                ServidorApp.log("Respuesta a " + direccion + " -> " + respuesta);
            }

        } catch (IOException e) {
            System.err.println("Error de comunicación con " + direccion + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            ServidorApp.log("Conexión cerrada con " + direccion);
        }
    }

    /**
     * Interpreta la línea recibida con formato OPERACION;NUM1;NUM2
     * y calcula el resultado correspondiente.
     */
    private String procesarPeticion(String peticion) {
        try {
            String[] partes = peticion.split(";");
            if (partes.length != 3) {
                return "ERROR;Formato inválido. Se esperaba OPERACION;NUM1;NUM2";
            }

            String operacion = partes[0].trim().toUpperCase();
            double num1 = Double.parseDouble(partes[1].trim());
            double num2 = Double.parseDouble(partes[2].trim());
            double resultado;

            switch (operacion) {
                case "SUMA":
                    resultado = num1 + num2;
                    break;
                case "RESTA":
                    resultado = num1 - num2;
                    break;
                case "MULTIPLICACION":
                    resultado = num1 * num2;
                    break;
                case "DIVISION":
                    if (num2 == 0) {
                        return "ERROR;No es posible dividir por cero";
                    }
                    resultado = num1 / num2;
                    break;
                default:
                    return "ERROR;Operación no reconocida: " + operacion;
            }

            return "OK;" + resultado;

        } catch (NumberFormatException e) {
            return "ERROR;Los valores enviados no son números válidos";
        } catch (Exception e) {
            return "ERROR;Ocurrió un problema procesando la petición";
        }
    }
}
