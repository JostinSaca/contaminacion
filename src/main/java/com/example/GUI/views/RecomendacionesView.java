package com.example.GUI.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.example.GUI.MainView;
import com.example.GUI.views.MongoService;
import com.example.Modelos.ResultadoPrediccion;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Route(value = "recomendaciones", layout = MainView.class)
@PageTitle("Recomendaciones | Sistema Ambiental")
public class RecomendacionesView extends VerticalLayout {

    @Autowired
    private MongoService mongoService;

    private ComboBox<String> comboZonas;
    private VerticalLayout contenedorHistorial;

    public RecomendacionesView() {
        setSpacing(true);
        setPadding(true);

        add(new H2(" Histórico de Monitorización y Recomendaciones (MongoDB)"));
        add(new Paragraph("Seleccione una zona urbana para descargar las predicciones guardadas en la nube y revisar sus respectivas advertencias médicas."));

        comboZonas = new ComboBox<>("Filtrar por Zona");
        comboZonas.setItems("Centro", "Norte", "Sur", "Valle", "Quitumbe");
        comboZonas.setPlaceholder("Seleccione zona...");

        Button btnCargar = new Button("Ver Historial desde la Nube", event -> cargarRecomendacionesMongo());
        contenedorHistorial = new VerticalLayout();
        contenedorHistorial.setSpacing(true);

        add(comboZonas, btnCargar, contenedorHistorial);
    }

    private void cargarRecomendacionesMongo() {
        if (comboZonas.getValue() == null) {
            Notification.show("️ Por favor, seleccione una zona.");
            return;
        }

        contenedorHistorial.removeAll();

        // LEER DESDE MONGO: Descarga el historial directamente de la nube
        List<ResultadoPrediccion> historial = mongoService.leerPredicciones();

        if (historial == null || historial.isEmpty()) {
            Notification.show("El registro de predicciones en MongoDB Atlas está vacío.");
            return;
        }

        int zIndexTarget = obtenerZonaIndex(comboZonas.getValue());
        boolean hayDatos = false;

        for (ResultadoPrediccion r : historial) {
            if (r.getZonaIndex() == zIndexTarget) {
                hayDatos = true;

                VerticalLayout tarjeta = new VerticalLayout();
                tarjeta.getStyle().set("background-color", "#f8f9fa")
                        .set("border-radius", "6px")
                        .set("padding", "15px")
                        .set("border", "1px solid #e9ecef");

                tarjeta.add(new H3(" Mes: " + (r.getMes() + 1) + " | Día: " + r.getDia() + " — IC Calculado: " + String.format("%.2f", r.getIC())));
                tarjeta.add(new Span(" [Gases] NA: " + String.format("%.1f", r.getpNA()) +
                        " | NO2: " + String.format("%.1f", r.getpNO2()) +
                        " | SO2: " + String.format("%.1f", r.getpSO2()) +
                        " | CO2: " + String.format("%.1f", r.getpCO2()) +
                        " | PM2.5: " + String.format("%.1f", r.getpPM25())));

                StringBuilder alertasStr = new StringBuilder();
                boolean tieneAlerta = false;

                if (r.getpNA() > 100) { alertasStr.append("• Limitar actividades exteriores. "); tieneAlerta = true; }
                if (r.getpNO2() > 25) { alertasStr.append("• Reducir uso de transporte vehicular a gasolina. "); tieneAlerta = true; }
                if (r.getpPM25() > 15) { alertasStr.append("• Usar mascarilla obligatoria en la zona. "); tieneAlerta = true; }

                Paragraph pRecomendacion = new Paragraph();
                if (tieneAlerta) {
                    pRecomendacion.setText(" [Recomendaciones]: " + alertasStr.toString());
                    tarjeta.getStyle().set("border-left", "4px solid #ffc107"); // Borde Amarillo visual sin usar LUMO
                } else {
                    pRecomendacion.setText(" [Recomendaciones]: Calidad del aire aceptable. Sin restricciones activas.");
                    tarjeta.getStyle().set("border-left", "4px solid #28a745"); // Borde Verde visual sin usar LUMO
                }

                tarjeta.add(pRecomendacion);
                contenedorHistorial.add(tarjeta);
            }
        }

        if (!hayDatos) {
            contenedorHistorial.add(new Paragraph("No se encontraron predicciones registradas en la nube para esta zona."));
        } else {
            Notification.show("✓ Historial descargado con éxito.");
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