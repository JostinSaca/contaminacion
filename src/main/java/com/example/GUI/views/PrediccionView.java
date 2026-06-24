package com.example.GUI.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.example.GUI.MainView;
import com.example.Modelos.*;
import java.io.*;

@Route(value = "prediccion", layout = MainView.class)
@PageTitle("Nueva Predicción | Sistema Ambiental")
public class PrediccionView extends VerticalLayout {

    private final String FILE_DAT = "zonas.dat";
    private final String FILE_PREDICCIONES = "predicciones.dat";
    private Zona[] zonas;

    private ComboBox<String> comboZonas;
    private ComboBox<Integer> comboMeses;

    // Campos de Gases del CLI
    private NumberField txtNa, txtNo2, txtSo2, txtCo2, txtPm25;
    // Campos Meteorológicos
    private NumberField txtTemp, txtHum, txtVien;

    private VerticalLayout panelResultados;

    public PrediccionView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("🔮 Ingreso de Nuevos Parámetros Meteorológicos"));
        add(new Paragraph("Establezca la zona, el mes y las lecturas actuales para calcular el Índice de Contaminación (IC) y registrar las recomendaciones médicas correspondientes."));

        // Selectores de ubicación y tiempo
        comboZonas = new ComboBox<>("Zona");
        comboZonas.setItems("Centro", "Norte", "Sur", "Valle", "Quitumbe");
        comboZonas.setPlaceholder("Seleccione zona...");

        comboMeses = new ComboBox<>("Mes de Evaluación");
        comboMeses.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        comboMeses.setPlaceholder("Mes (1-12)");

        HorizontalLayout filaUbicacion = new HorizontalLayout(comboZonas, comboMeses);

        // Bloque de captura de Gases (Primera Fila)
        txtNa = new NumberField("NA µg/m³");
        txtNo2 = new NumberField("NO2 µg/m³");
        txtSo2 = new NumberField("SO2 µg/m³");
        txtCo2 = new NumberField("CO2 µg/m³");
        txtPm25 = new NumberField("PM2.5 µg/m³");
        HorizontalLayout filaGases = new HorizontalLayout(txtNa, txtNo2, txtSo2, txtCo2, txtPm25);
        filaGases.setWidthFull();

        // Bloque de parámetros meteorológicos (Segunda Fila)
        txtTemp = new NumberField("Temperatura °C");
        txtHum = new NumberField("Humedad (%)");
        txtVien = new NumberField("Viento (m/s)");
        HorizontalLayout filaClima = new HorizontalLayout(txtTemp, txtHum, txtVien);

        // Botón de Ejecución
        Button btnCalcular = new Button("Calcular Predicción");
        btnCalcular.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        panelResultados = new VerticalLayout();
        panelResultados.setVisible(false);

        btnCalcular.addClickListener(event -> ejecutarPrediccionReal());

        add(filaUbicacion, new H3("Métricas de Gases"), filaGases, new H3("Variables del Clima"), filaClima, btnCalcular, panelResultados);
    }

    private void leerDAT() {
        File f = new File(FILE_DAT);
        if (!f.exists() || f.length() == 0) {
            Notification.show("❌ ERROR: No se han precargado los datos históricos base. Use la pestaña de Precarga primero.", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_DAT))) {
            zonas = (Zona[]) ois.readObject();
        } catch (Exception e) {
            Notification.show("Error leyendo base binaria: " + e.getMessage(), 3000, Notification.Position.BOTTOM_START);
        }
    }

    private void ejecutarPrediccionReal() {
        leerDAT();
        if (zonas == null) return;

        // Validación de entradas vacías
        if (comboZonas.getValue() == null || comboMeses.getValue() == null ||
                txtNa.getValue() == null || txtNo2.getValue() == null || txtSo2.getValue() == null ||
                txtCo2.getValue() == null || txtPm25.getValue() == null ||
                txtTemp.getValue() == null || txtHum.getValue() == null || txtVien.getValue() == null) {

            Notification.show("⚠️ Por favor, llene todos los campos numéricos.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        panelResultados.removeAll();
        panelResultados.setVisible(true);

        // Mapeo exacto de índices del CLI
        int zIndex = obtenerZonaIndex(comboZonas.getValue());
        int mIndex = comboMeses.getValue() - 1;

        // Recuperación de variables
        double na = txtNa.getValue();
        double no2 = txtNo2.getValue();
        double so2 = txtSo2.getValue();
        double co2 = txtCo2.getValue();
        double pm25 = txtPm25.getValue();
        double temp = txtTemp.getValue();
        double hum = txtHum.getValue();
        double vien = txtVien.getValue();

        // === LÓGICA COPIADA EXACTAMENTE DE SISTEMACLI.JAVA ===
        ResultadoPrediccion r = zonas[zIndex].calcularPrediccion(mIndex);
        r.setZonaIndex(zIndex);

        double[] pesos = {5, 5, 5, 6, 6, 7, 8, 10, 12, 14, 22};
        r.setpNA(((r.getpNA() * 78) + (na * pesos[10])) / 100);
        r.setpNO2(((r.getpNO2() * 78) + (no2 * pesos[10])) / 100);
        r.setpSO2(((r.getpSO2() * 78) + (so2 * pesos[10])) / 100);
        r.setpCO2(((r.getpCO2() * 78) + (co2 * pesos[10])) / 100);
        r.setpPM25(((r.getpPM25() * 78) + (pm25 * pesos[10])) / 100);

        double factor = 1.0;
        if (temp > 30) factor *= 1.10;
        if (hum > 70) factor *= 1.05;
        if (vien < 3) factor *= 1.10;

        r.setpNA(r.getpNA() * factor);
        r.setpNO2(r.getpNO2() * factor);
        r.setpSO2(r.getpSO2() * factor);
        r.setpCO2(r.getpCO2() * factor);
        r.setpPM25(r.getpPM25() * factor);

        r.setIC((0.10 * r.getpNA()) + (0.30 * r.getpNO2()) + (0.15 * r.getpSO2()) + (0.15 * r.getpCO2()) + (0.30 * r.getpPM25()));

        // === DESPLIEGUE EN LA INTERFAZ DE VAADIN ===
        panelResultados.add(new H3("--- RESULTADO DE LA PREDICCIÓN AMBIENTAL ---"));
        panelResultados.add(new Span("Índice de Contaminación (IC) calculado: " + String.format("%.2f", r.getIC())));

        if (zonas[zIndex].evaluarAlerta(r)) {
            VerticalLayout panelAlerta = new VerticalLayout();
            panelAlerta.getStyle().set("background-color", "#fdf2f2").set("border-left", "5px solid #de3545");
            panelAlerta.add(new H3("⚠️ [ALERTA AMBIENTAL DETECTADA]"));

            // Evaluamos alertas específicas médicas de tu CLI
            if (r.getpNA() > 100) panelAlerta.add(new Span("-> NA Elevado: Limitar actividades al aire libre."));
            if (r.getpNO2() > 25) panelAlerta.add(new Span("-> NO2 Elevado: Reducir uso de vehículos combustibles."));
            if (r.getpPM25() > 15) panelAlerta.add(new Span("-> PM2.5 Crítico: Usar mascarilla obligatoria."));

            panelResultados.add(panelAlerta);
        } else {
            panelResultados.add(new Paragraph("✅ Calidad del aire dentro de los límites aceptables."));
        }

        guardarPrediccionUI(r);
    }

    private int obtenerZonaIndex(String nombre) {
        return switch (nombre) {
            case "Centro" -> 0;
            case "Norte" -> 1;
            case "Sur" -> 2;
            case "Valle" -> 3;
            case "Quitumbe" -> 4;
            default -> 0;
        };
    }

    private void guardarPrediccionUI(ResultadoPrediccion r) {
        File file = new File(FILE_PREDICCIONES);
        boolean append = file.exists();
        try (FileOutputStream fos = new FileOutputStream(file, true);
             ObjectOutputStream oos = append ? new ObjectOutputStream(fos) {
                 @Override protected void writeStreamHeader() throws IOException { reset(); }
             } : new ObjectOutputStream(fos)) {
            oos.writeObject(r);
            Notification.show("✓ Predicción guardada con éxito en el historial.", 2500, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (IOException e) {
            Notification.show("Error guardando registro binario: " + e.getMessage(), 3000, Notification.Position.BOTTOM_START);
        }
    }
}