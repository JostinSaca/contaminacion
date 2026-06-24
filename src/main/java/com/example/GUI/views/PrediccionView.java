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
import com.example.GUI.views.MongoService;
import com.example.Modelos.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "prediccion", layout = MainView.class)
@PageTitle("Nueva Predicción | Sistema Ambiental")
public class PrediccionView extends VerticalLayout {

    @Autowired
    private MongoService mongoService;

    private Zona[] zonas;
    private ComboBox<String> comboZonas;
    private ComboBox<Integer> comboMeses;

    private NumberField txtNa, txtNo2, txtSo2, txtCo2, txtPm25;
    private NumberField txtTemp, txtHum, txtVien;
    private VerticalLayout panelResultados;

    public PrediccionView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("🔮 Predicción Ambiental con MongoDB"));
        add(new Paragraph("Establezca los parámetros. Los datos de las zonas se recuperarán en tiempo real desde la nube de MongoDB Atlas."));

        comboZonas = new ComboBox<>("Zona");
        comboZonas.setItems("Centro", "Norte", "Sur", "Valle", "Quitumbe");
        comboZonas.setPlaceholder("Seleccione zona...");

        comboMeses = new ComboBox<>("Mes de Evaluación");
        comboMeses.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        comboMeses.setPlaceholder("Mes (1-12)");

        HorizontalLayout filaUbicacion = new HorizontalLayout(comboZonas, comboMeses);

        txtNa = new NumberField("NA µg/m³");
        txtNo2 = new NumberField("NO2 µg/m³");
        txtSo2 = new NumberField("SO2 µg/m³");
        txtCo2 = new NumberField("CO2 µg/m³");
        txtPm25 = new NumberField("PM2.5 µg/m³");
        HorizontalLayout filaGases = new HorizontalLayout(txtNa, txtNo2, txtSo2, txtCo2, txtPm25);
        filaGases.setWidthFull();

        txtTemp = new NumberField("Temperatura °C");
        txtHum = new NumberField("Humedad (%)");
        txtVien = new NumberField("Viento (m/s)");
        HorizontalLayout filaClima = new HorizontalLayout(txtTemp, txtHum, txtVien);

        Button btnCalcular = new Button("Calcular Predicción");
        btnCalcular.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        panelResultados = new VerticalLayout();
        panelResultados.setVisible(false);

        btnCalcular.addClickListener(event -> ejecutarPrediccionRealMongo());

        add(filaUbicacion, new H3("Métricas de Gases"), filaGases, new H3("Variables del Clima"), filaClima, btnCalcular, panelResultados);
    }

    private void ejecutarPrediccionRealMongo() {
        // LEER DESDE MONGO: Recuperamos las zonas de la nube
        zonas = mongoService.leerZonas();

        if (zonas == null) {
            Notification.show("❌ ERROR: No hay datos en MongoDB. Ejecute la Precarga primero.", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (comboZonas.getValue() == null || comboMeses.getValue() == null ||
                txtNa.getValue() == null || txtNo2.getValue() == null || txtSo2.getValue() == null ||
                txtCo2.getValue() == null || txtPm25.getValue() == null ||
                txtTemp.getValue() == null || txtHum.getValue() == null || txtVien.getValue() == null) {

            Notification.show("Por favor, llene todos los campos numéricos.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        panelResultados.removeAll();
        panelResultados.setVisible(true);

        int zIndex = obtenerZonaIndex(comboZonas.getValue());
        int mIndex = comboMeses.getValue() - 1;

        double na = txtNa.getValue();
        double no2 = txtNo2.getValue();
        double so2 = txtSo2.getValue();
        double co2 = txtCo2.getValue();
        double pm25 = txtPm25.getValue();
        double temp = txtTemp.getValue();
        double hum = txtHum.getValue();
        double vien = txtVien.getValue();

        // Ejecución exacta de tu lógica matemática del CLI
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

        r.setpNA(r.getpNA() * factor); r.setpNO2(r.getpNO2() * factor);
        r.setpSO2(r.getpSO2() * factor); r.setpCO2(r.getpCO2() * factor);
        r.setpPM25(r.getpPM25() * factor);

        r.setIC((0.10 * r.getpNA()) + (0.30 * r.getpNO2()) + (0.15 * r.getpSO2()) + (0.15 * r.getpCO2()) + (0.30 * r.getpPM25()));

        panelResultados.add(new H3("--- RESULTADO DE LA PREDICCIÓN (DESDE MONGO) ---"));
        panelResultados.add(new Span("Índice de Contaminación (IC) calculado: " + String.format("%.2f", r.getIC())));

        if (zonas[zIndex].evaluarAlerta(r)) {
            VerticalLayout panelAlerta = new VerticalLayout();
            panelAlerta.getStyle().set("background-color", "#fdf2f2").set("border-left", "5px solid #de3545");
            panelAlerta.add(new H3(" [ALERTA AMBIENTAL DETECTADA]"));
            if (r.getpNA() > 100) panelAlerta.add(new Span("-> NA Elevado: Limitar actividades al aire libre."));
            if (r.getpNO2() > 25) panelAlerta.add(new Span("-> NO2 Elevado: Reducir uso de vehículos combustibles."));
            if (r.getpPM25() > 15) panelAlerta.add(new Span("-> PM2.5 Crítico: Usar mascarilla obligatoria."));
            panelResultados.add(panelAlerta);
        } else {
            panelResultados.add(new Paragraph(" Calidad del aire dentro de los límites."));
        }

        // GUARDAR EN MONGO: Registramos la predicción en su respectiva colección de la nube
        mongoService.guardarPrediccion(r);
        Notification.show("✓ Predicción sincronizada en MongoDB Atlas.", 2500, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
}