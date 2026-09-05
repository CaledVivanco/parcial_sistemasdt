package cliente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Cliente con interfaz gráfica Swing para la Calculadora Remota.
 *
 * Conceptos de la Unidad 1 aplicados:
 *  - Interfaz gráfica construida con Swing (JFrame, JPanel, JTextField,
 *    JComboBox, JButton, JTextArea, Layouts).
 *  - Uso de Socket para conectarse a un servidor TCP (ServerSocket del lado servidor).
 *  - Envío y recepción de datos mediante PrintWriter / BufferedReader.
 *  - Uso de SwingWorker para no bloquear el hilo de la interfaz (EDT)
 *    mientras se realiza la operación de red.
 *  - Manejo de excepciones (conexión rechazada, host desconocido, etc.).
 *  - Panel personalizado (override de paintComponent) para tarjetas redondeadas.
 */
public class ClienteApp extends JFrame {

    /* ---- Paleta de diseño ---- */
    private static final Color COLOR_FONDO = new Color(0xF4, 0xF6, 0xFA);
    private static final Color COLOR_TARJETA = Color.WHITE;
    private static final Color COLOR_ACCENTO = new Color(0x3B, 0x5B, 0xDB);
    private static final Color COLOR_ACCENTO_OSCURO = new Color(0x2E, 0x4B, 0xB5);
    private static final Color COLOR_TEXTO = new Color(0x1F, 0x29, 0x37);
    private static final Color COLOR_MUTED = new Color(0x6B, 0x72, 0x80);
    private static final Color COLOR_OK = new Color(0x16, 0xA3, 0x4A);
    private static final Color COLOR_ERROR = new Color(0xDC, 0x26, 0x26);
    private static final Color COLOR_BORDE = new Color(0xE2, 0xE8, 0xF0);
    private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FUENTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FUENTE_ETIQUETA = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FUENTE_RESULTADO = new Font("Segoe UI", Font.BOLD, 18);

    private final JTextField campoIp = new JTextField("localhost", 12);
    private final JTextField campoPuerto = new JTextField("5000", 6);
    private final JComboBox<String> comboOperacion = new JComboBox<>(
            new String[]{"SUMA", "RESTA", "MULTIPLICACION", "DIVISION"});
    private final JTextField campoNum1 = new JTextField(8);
    private final JTextField campoNum2 = new JTextField(8);
    private final JButton botonCalcular = new JButton("Calcular");
    private final JLabel etiquetaResultado = new JLabel("Esperando una operación...");
    private final JLabel etiquetaIcono = new JLabel("=");
    private final JTextArea areaHistorial = new JTextArea();
    private final JPanel panelResultado = new RoundedPanel(16);

    public ClienteApp() {
        super("Cliente - Calculadora Remota (Socket)");
        construirInterfaz();
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearContenido(), BorderLayout.CENTER);

        botonCalcular.addActionListener(e -> onCalcular());

        // Enter dentro de los campos numéricos dispara el cálculo.
        campoNum1.addActionListener(e -> onCalcular());
        campoNum2.addActionListener(e -> onCalcular());

        pack();
        setLocationRelativeTo(null);
    }

    /** Barra superior con el título de la aplicación. */
    private JPanel crearEncabezado() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_ACCENTO);
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel titulo = new JLabel("Calculadora Remota");
        titulo.setFont(FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Cliente TCP · Socket · Java Swing");
        subtitulo.setFont(FUENTE_SUBTITULO);
        subtitulo.setForeground(new Color(0xD8, 0xE0, 0xFB));

        JPanel detalle = new JPanel(new BorderLayout(0, 2));
        detalle.setOpaque(false);
        detalle.add(titulo, BorderLayout.NORTH);
        detalle.add(subtitulo, BorderLayout.SOUTH);

        JLabel emblema = new JLabel("≡");
        emblema.setFont(new Font("Segoe UI", Font.BOLD, 34));
        emblema.setForeground(Color.WHITE);
        emblema.setBorder(new EmptyBorder(0, 12, 0, 0));

        header.add(detalle, BorderLayout.CENTER);
        header.add(emblema, BorderLayout.EAST);
        return header;
    }

    /** Cuerpo principal: tarjeta de entrada, resultado e historial. */
    private JScrollPane crearContenido() {
        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setBackground(COLOR_FONDO);
        cuerpo.setBorder(new EmptyBorder(16, 16, 16, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);

        gbc.gridy = 0;
        cuerpo.add(crearTarjetaEntrada(), gbc);

        gbc.gridy = 1;
        cuerpo.add(crearBannerResultado(), gbc);

        gbc.gridy = 2;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        cuerpo.add(crearTarjetaHistorial(), gbc);

        JScrollPane scroll = new JScrollPane(cuerpo);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** Fila de IP y puerto + fila de operandos y operación. */
    private JPanel crearTarjetaEntrada() {
        RoundedPanel tarjeta = new RoundedPanel(16);
        tarjeta.setLayout(new GridBagLayout());
        tarjeta.setBorder(new EmptyBorder(16, 18, 16, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 6, 8, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // --- Fila 1: conexión ---
        int fila = 0;
        agregarEtiqueta(tarjeta, gbc, "IP del servidor", 0, fila);
        estiloCampo(campoIp);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        tarjeta.add(campoIp, gbc);
        gbc.gridwidth = 1;

        agregarEtiqueta(tarjeta, gbc, "Puerto", 3, fila);
        estiloCampo(campoPuerto);
        campoPuerto.setColumns(6);
        gbc.gridx = 4;
        tarjeta.add(campoPuerto, gbc);

        // --- Fila 2: operandos y operación ---
        fila = 1;
        agregarEtiqueta(tarjeta, gbc, "Número 1", 0, fila);
        estiloCampo(campoNum1);
        gbc.gridx = 1;
        tarjeta.add(campoNum1, gbc);

        agregarEtiqueta(tarjeta, gbc, "Operación", 2, fila);
        comboOperacion.setFont(FUENTE_CAMPO);
        comboOperacion.setBackground(COLOR_TARJETA);
        comboOperacion.setForeground(COLOR_TEXTO);
        gbc.gridx = 3;
        tarjeta.add(comboOperacion, gbc);

        agregarEtiqueta(tarjeta, gbc, "Número 2", 4, fila);
        estiloCampo(campoNum2);
        gbc.gridx = 5;
        tarjeta.add(campoNum2, gbc);

        // --- Botón ---
        estiloBoton(botonCalcular);
        gbc.gridx = 6;
        gbc.gridy = fila;
        gbc.anchor = GridBagConstraints.EAST;
        tarjeta.add(botonCalcular, gbc);

        return tarjeta;
    }

    private void agregarEtiqueta(JPanel tarjeta, GridBagConstraints gbc,
                                 String texto, int col, int fila) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_ETIQUETA);
        etiqueta.setForeground(COLOR_MUTED);
        gbc.gridx = col;
        gbc.gridy = fila;
        tarjeta.add(etiqueta, gbc);
    }

    private void estiloCampo(JTextField campo) {
        campo.setFont(FUENTE_CAMPO);
        campo.setForeground(COLOR_TEXTO);
        campo.setBackground(COLOR_TARJETA);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE),
                new EmptyBorder(6, 8, 6, 8)));
    }

    private void estiloBoton(JButton boton) {
        boton.setFont(FUENTE_BOTON);
        boton.setForeground(Color.WHITE);
        boton.setBackground(COLOR_ACCENTO);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(new EmptyBorder(9, 18, 9, 18));

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (boton.isEnabled()) {
                    boton.setBackground(COLOR_ACCENTO_OSCURO);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(COLOR_ACCENTO);
            }
        });
    }

    /** Banner (tarjeta inferior clara) que muestra el resultado. */
    private JPanel crearBannerResultado() {
        panelResultado.setLayout(new BorderLayout(12, 0));
        panelResultado.setBorder(new EmptyBorder(14, 18, 14, 18));
        panelResultado.setBackground(new Color(0xEC, 0xF0, 0xFD));

        etiquetaIcono.setFont(new Font("Segoe UI", Font.BOLD, 22));
        etiquetaIcono.setForeground(COLOR_ACCENTO);

        etiquetaResultado.setFont(FUENTE_RESULTADO);
        etiquetaResultado.setForeground(COLOR_TEXTO);

        JLabel titulo = new JLabel("Resultado");
        titulo.setFont(FUENTE_ETIQUETA);
        titulo.setForeground(COLOR_MUTED);

        JPanel etiquetas = new JPanel();
        etiquetas.setOpaque(false);
        etiquetas.setLayout(new BoxLayout(etiquetas, BoxLayout.Y_AXIS));
        etiquetas.add(titulo);
        etiquetas.add(etiquetaResultado);

        panelResultado.add(etiquetaIcono, BorderLayout.WEST);
        panelResultado.add(etiquetas, BorderLayout.CENTER);
        return panelResultado;
    }

    private JPanel crearTarjetaHistorial() {
        RoundedPanel tarjeta = new RoundedPanel(16);
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel titulo = new JLabel("Historial de peticiones");
        titulo.setFont(FUENTE_ETIQUETA);
        titulo.setForeground(COLOR_MUTED);
        titulo.setBorder(new EmptyBorder(0, 0, 10, 0));

        areaHistorial.setEditable(false);
        areaHistorial.setFont(new Font("Consolas", Font.PLAIN, 13));
        areaHistorial.setForeground(COLOR_TEXTO);
        areaHistorial.setBackground(new Color(0xFA, 0xFB, 0xFD));
        areaHistorial.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        areaHistorial.setLineWrap(true);
        areaHistorial.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(areaHistorial);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDE));
        scroll.setPreferredSize(new Dimension(0, 180));

        tarjeta.add(titulo, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        return tarjeta;
    }

    /* ================= Lógica de red (sin cambios) ================= */

    private void onCalcular() {
        String ip = campoIp.getText().trim();
        String puertoTexto = campoPuerto.getText().trim();
        String operacion = (String) comboOperacion.getSelectedItem();
        String num1Texto = campoNum1.getText().trim();
        String num2Texto = campoNum2.getText().trim();

        // Validación básica en el cliente antes de ir a la red.
        int puerto;
        double num1, num2;
        try {
            puerto = Integer.parseInt(puertoTexto);
            num1 = Double.parseDouble(num1Texto);
            num2 = Double.parseDouble(num2Texto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Verifique que el puerto y los números sean válidos.",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        botonCalcular.setEnabled(false);
        pintarResultado("consultando al servidor...", COLOR_MUTED, "…");
        String peticion = operacion + ";" + num1 + ";" + num2;

        // SwingWorker: la comunicación por red se hace en un hilo aparte
        // para no congelar la interfaz gráfica (Event Dispatch Thread).
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return enviarPeticion(ip, puerto, peticion);
            }

            @Override
            protected void done() {
                botonCalcular.setEnabled(true);
                try {
                    String respuesta = get();
                    mostrarRespuesta(peticion, respuesta);
                } catch (Exception ex) {
                    pintarResultado("error inesperado", COLOR_ERROR, "!");
                    areaHistorial.append("Error inesperado: " + ex.getMessage() + "\n");
                }
            }
        };
        worker.execute();
    }

    /**
     * Abre un Socket hacia el servidor, envía la petición y lee una línea de respuesta.
     * Se abre y cierra una conexión por cada cálculo (protocolo simple de petición/respuesta).
     */
    private String enviarPeticion(String ip, int puerto, String peticion) {
        try (Socket socket = new Socket(ip, puerto);
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {

            salida.println(peticion);
            String respuesta = entrada.readLine();
            salida.println("SALIR"); // avisa al servidor que esta conexión terminó
            return respuesta != null ? respuesta : "ERROR;Sin respuesta del servidor";

        } catch (IOException e) {
            return "ERROR;No fue posible conectar con el servidor (" + e.getMessage() + ")";
        }
    }

    private void mostrarRespuesta(String peticion, String respuesta) {
        String[] partes = respuesta.split(";", 2);
        String estado = partes[0];
        String detalle = partes.length > 1 ? partes[1] : "";

        if ("OK".equals(estado)) {
            pintarResultado(detalle, COLOR_OK, "=");
        } else {
            pintarResultado("ERROR", COLOR_ERROR, "!");
            JOptionPane.showMessageDialog(this, detalle, "Error del servidor",
                    JOptionPane.ERROR_MESSAGE);
        }

        areaHistorial.append("Petición: " + peticion + "  ->  Respuesta: " + respuesta + "\n");
    }

    private void pintarResultado(String texto, Color color, String icono) {
        etiquetaResultado.setText(texto);
        etiquetaResultado.setForeground(color);
        etiquetaIcono.setText(icono);
        etiquetaIcono.setForeground(color);
        panelResultado.setBackground(color == COLOR_MUTED
                ? new Color(0xEC, 0xF0, 0xFD)
                : new Color(color.getRed(), color.getGreen(), color.getBlue(), 18));
        panelResultado.revalidate();
        panelResultado.repaint();
    }

    public static void main(String[] args) {
        // Buenas prácticas de Swing: construir y mostrar la GUI en el Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            ClienteApp ventana = new ClienteApp();
            ventana.setVisible(true);
        });
    }

    /** Panel con fondo blanco y esquinas redondeadas. */
    private static class RoundedPanel extends JPanel {
        private final int radio;

        RoundedPanel(int radio) {
            this.radio = radio;
            setOpaque(false);
            setBackground(COLOR_TARJETA);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}