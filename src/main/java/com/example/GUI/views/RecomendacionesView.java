package com.example.GUI.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.example.GUI.MainView;
import com.example.Modelos.ResultadoPrediccion;
import java.io.*;

@Route(value = "recomendaciones", layout = MainView.class)
@PageTitle("Recomendaciones | Sistema Ambiental")
public class RecomendacionesView extends VerticalLayout {

    private final String FILE_PREDICCIONES = "predicciones.dat";
    private ComboBox<String> comboZonas;
    private VerticalLayout contenedorHistorial;

    public RecomendacionesView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("💡 Histórico de Monitorización y Recomendaciones"));
        add(new Paragraph("Seleccione una zona urbana para revisar el histórico de predicciones calculadas junto con sus respectivas restricciones y advertencias de salud."));

        comboZonas = new ComboBox<>("Filtrar por Zona");
        comboZonas.setItems("Centro", "Norte", "Sur", "Valle", "Quitumbe");
        comboZonas.setPlaceholder("Seleccione zona...");

        Button btnCargar = new Button("Ver Historial", event -> cargarRecomendacionesGuardadas());
        contenedorHistorial = new VerticalLayout();
        contenedorHistorial.setSpacing(true);

        add(comboZonas, btnCargar, contenedorHistorial);
    }

    private void cargarRecomendacionesGuardadas() {
        if (comboZonas.getValue() == null) {
            Notification.show("⚠️ Por favor, seleccione una zona.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        contenedorHistorial.removeAll();
        File file = new File(FILE_PREDICCIONES);

        if (!file.exists() || file.length() == 0) {
            Notification.show("ERROR: El registro de predicciones está vacío. Genere una predicción primero.", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        int zIndexTarget = obtenerZonaIndex(comboZonas.getValue());
        boolean hayDatos = false;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PREDICCIONES))) {
            while (true) {
                try {
                    ResultadoPrediccion r = (ResultadoPrediccion) ois.readObject();

                    if (r.getZonaIndex() == zIndexTarget) {
                        hayDatos = true;

                        VerticalLayout tarjeta = new VerticalLayout();
                        tarjeta.getStyle().set("background-color", "#f8f9fa")
                                .set("border-radius", "6px")
                                .set("padding", "15px")
                                .set("border", "1px solid #e9ecef");

                        tarjeta.add(new H3("📅 Mes: " + (r.getMes() + 1) + " | Día: " + r.getDia() + " — IC calculated: " + String.format("%.2f", r.getIC())));
                        tarjeta.add(new Span("💨 [Gases] NA: " + String.format("%.1f", r.getpNA()) +
                                " | NO2: " + String.format("%.1f", r.getpNO2()) +
                                " | SO2: " + String.format("%.1f", r.getpSO2()) +
                                " | CO2: " + String.format("%.1f", r.getpCO2()) +
                                " | PM2.5: " + String.format("%.1f", r.getpPM25())));

                        // Lógica exacta de evaluación de alertas médicas del CLI
                        StringBuilder alertasStr = new StringBuilder();
                        boolean tieneAlerta = false;

                        if (r.getpNA() > 100) { alertasStr.append("• Limitar actividades exteriores. "); tieneAlerta = true; }
                        if (r.getpNO2() > 25) { alertasStr.append("• Reducir uso de transporte vehicular a gasolina. "); tieneAlerta = true; }
                        if (r.getpPM25() > 15) { alertasStr.append("• Usar mascarilla obligatoria en la zona. "); tieneAlerta = true; }

                        Paragraph pRecomendacion = new Paragraph();
                        if (tieneAlerta) {
                            pRecomendacion.setText("📋 [Recomendaciones]: " + alertasStr.toString());
                            tarjeta.getStyle().set("border-left", "4px solid #ffc107"); // Color de advertencia amarillo
                        } else {
                            pRecomendacion.setText("📋 [Recomendaciones]: Calidad del aire aceptable. Sin restricciones activas.");
                            tarjeta.getStyle().set("border-left", "4px solid #28a745"); // Color verde estable
                        }

                        tarjeta.add(pRecomendacion);
                        contenedorHistorial.add(tarjeta);
                    }
                } catch (EOFException e) {
                    break; // Fin del archivo binario secuencial
                }
            }

            if (!hayDatos) {
                contenedorHistorial.add(new Paragraph("No se encontraron predicciones registradas para esta zona."));
            }

        } catch (Exception e) {
            Notification.show("Error procesando registros binarios: " + e.getMessage(), 4000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
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