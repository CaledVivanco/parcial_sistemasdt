# Actividad: Cliente–Servidor con Java, Swing y Sockets

Calculadora remota: el **cliente** (Swing) envía dos números y una operación,
el **servidor** (Socket/ServerSocket) la calcula y devuelve el resultado.

Ya está probado y funcionando (compilación + pruebas de todas las operaciones
y de los errores: división por cero, datos no numéricos, operación inválida,
y varias conexiones simultáneas).

## Estructura

```
cliente-servidor-java/
├── servidor-app/
│   └── src/servidor/
│       ├── ServidorApp.java      -> main(), abre el ServerSocket (puerto 5000)
│       └── ManejadorCliente.java -> atiende cada cliente en un hilo (Runnable)
└── cliente-app/
    └── src/cliente/
        └── ClienteApp.java       -> interfaz Swing + Socket hacia el servidor
```

Cada carpeta (`servidor-app`, `cliente-app`) es un **proyecto independiente**,
tal como pide el enunciado. Puedes importarlas en Eclipse/IntelliJ/NetBeans
como "Java Project" apuntando la carpeta `src` como código fuente, o compilar
por línea de comandos como se explica abajo.

## Protocolo de comunicación

El cliente envía una línea de texto:

```
OPERACION;NUM1;NUM2      (OPERACION = SUMA | RESTA | MULTIPLICACION | DIVISION)
```

El servidor responde:

```
OK;resultado
ERROR;mensaje de error   (ej: división por cero, formato inválido, etc.)
```

## Cómo ejecutar (línea de comandos)

**1. Compilar y arrancar el servidor** (déjalo corriendo en una terminal):
```bash
cd servidor-app
javac -encoding UTF-8 -d bin src/servidor/*.java
chcp 65001 >nul
java -cp bin servidor.ServidorApp
```
Verás en consola cada conexión, cada petición y cada respuesta (útil para
mostrarlo en el video de sustentación).

**2. En otra terminal, compilar y ejecutar el cliente:**
```bash
cd cliente-app
javac -encoding UTF-8 -d bin src/cliente/*.java
java -cp bin cliente.ClienteApp
```
Se abre la ventana Swing: escribe la IP (`localhost` si es en el mismo
computador), el puerto (`5000`), los dos números y la operación, y presiona
**Calcular**. El resultado y el historial de peticiones se muestran en la
misma ventana.

Puedes abrir varias instancias del cliente para demostrar que el servidor
atiende múltiples clientes a la vez (concurrencia con hilos).

> Nota sobre codificación en Windows: los archivos fuente están en UTF-8 y la
> consola debe usar la página de códigos `65001` (`chcp 65001`) para que las
> tildes y la "ñ" se vean correctamente en el log del servidor.

## Conceptos de la Unidad 1 que se evidencian en el código

- **Socket / ServerSocket**: `ServidorApp` crea el `ServerSocket`; el cliente
  crea un `Socket` para conectarse.
- **Hilos (Thread/Runnable)**: `ManejadorCliente` atiende cada conexión en un
  hilo aparte, permitiendo varios clientes simultáneos.
- **Flujos de E/S**: `BufferedReader`/`PrintWriter` sobre los streams del
  socket para enviar y recibir texto.
- **Swing**: `JFrame`, `JPanel`, `JTextField`, `JComboBox`, `JButton`,
  `JTextArea`, layouts (`BorderLayout`, `FlowLayout`, `GridLayout`).
- **SwingWorker**: la llamada de red se hace fuera del hilo de la interfaz
  (Event Dispatch Thread) para no congelar la ventana.
- **Manejo de excepciones**: `IOException`, `NumberFormatException`,
  validación de división por cero y de formato de la petición.

## Para la entrega en PDF

1. **Portada** (nombre, curso, actividad, fecha).
2. **Enlace de GitHub**: sube las dos carpetas (`servidor-app` y
   `cliente-app`) como repositorio(s) — puedes usar un mismo repo con dos
   carpetas o dos repos separados.
3. **Enlace de video de sustentación**: graba pantalla mostrando:
   - Arranque del servidor (consola).
   - Arranque de uno o más clientes (ventana Swing).
   - Pruebas con operaciones válidas y con errores (división por cero, texto
     no numérico).
   - Explicación breve del código relacionándolo con los conceptos de la
     Unidad 1 (lista de arriba).