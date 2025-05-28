import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.*;

class ConversorMonedas extends JFrame {

    private final JTextField inputMonto;
    private final JComboBox<String> comboMoneda;
    private final JLabel etiquetaResultado;

    private static final String API_KEY = "730c8b3c4451634c1222a9e8";
    private static final String BASE_CURRENCY = "USD";

    private static final String[] MONEDAS_DISPONIBLES = {
            "ARS", "BOB", "BRL", "CLP", "COP", "MXN", "USD"
    };

    public ConversorMonedas() {
        setTitle("Conversor de Monedas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null); // Centrar ventana
        setLayout(new BorderLayout(10, 10));

        // Panel superior: mensaje de bienvenida
        JLabel mensajeBienvenida = new JLabel("Sea Bienvenido/a al conversor de moneda 😊", SwingConstants.CENTER);
        mensajeBienvenida.setFont(new Font("SansSerif", Font.BOLD, 16));
        mensajeBienvenida.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mensajeBienvenida, BorderLayout.NORTH);

        // Panel central: ingreso de datos
        JPanel panelCentro = new JPanel(new GridLayout(4, 1, 10, 10));
        panelCentro.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        panelCentro.add(new JLabel("Ingrese monto en USD:"));

        inputMonto = new JTextField();
        panelCentro.add(inputMonto);

        panelCentro.add(new JLabel("Seleccione moneda destino:"));
        comboMoneda = new JComboBox<>(MONEDAS_DISPONIBLES);
        panelCentro.add(comboMoneda);

        add(panelCentro, BorderLayout.CENTER);

        // Panel inferior: botones y resultado
        JPanel panelInferior = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel panelBotones = new JPanel(new FlowLayout());

        JButton botonConvertir = new JButton("Convertir");
        JButton botonSalir = new JButton("Salir");

        panelBotones.add(botonConvertir);
        panelBotones.add(botonSalir);

        etiquetaResultado = new JLabel("Resultado: ", SwingConstants.CENTER);
        etiquetaResultado.setFont(new Font("SansSerif", Font.PLAIN, 14));

        panelInferior.add(etiquetaResultado);
        panelInferior.add(panelBotones);

        add(panelInferior, BorderLayout.SOUTH);

        // Acción al presionar "Convertir"
        botonConvertir.addActionListener(e -> convertirMoneda());

        // Acción al presionar "Salir"
        botonSalir.addActionListener(e -> System.exit(0));
    }

    private void convertirMoneda() {
        try {
            double monto = Double.parseDouble(inputMonto.getText());
            String monedaDestino = (String) comboMoneda.getSelectedItem();

            // 🔽 [INICIO DE CONEXIÓN API]
            JsonObject tasas = obtenerTasasDesdeAPI();

            if (tasas == null || !tasas.has(monedaDestino)) {
                etiquetaResultado.setText("Error: No se pudo obtener la tasa.");
                return;
            }

            double tasa = tasas.get(monedaDestino).getAsDouble();
            double resultado = monto * tasa;

            etiquetaResultado.setText(
                    String.format("Resultado: %.2f USD = %.2f %s", monto, resultado, monedaDestino)
            );

            // Pregunta al usuario si desea hacer otra conversión
            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "¿Desea hacer otra conversión?",
                    "Continuar",
                    JOptionPane.YES_NO_OPTION
            );

            if (respuesta == JOptionPane.NO_OPTION) {
                System.exit(0);
            } else {
                inputMonto.setText("");
                comboMoneda.setSelectedIndex(0);
            }

        } catch (NumberFormatException ex) {
            etiquetaResultado.setText("⚠ Ingrese un número válido.");
        } catch (Exception ex) {
            etiquetaResultado.setText("Error: " + ex.getMessage());
        }
    }

    private JsonObject obtenerTasasDesdeAPI() throws Exception {
        String url = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/" + BASE_CURRENCY;
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.getAsJsonObject("conversion_rates");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConversorMonedas app = new ConversorMonedas();
            app.setVisible(true);
        });
    }
}