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
import com.example.Modelos.Zona;
import java.io.*;

@Route(value = "historico", layout = MainView.class)
@PageTitle("Datos Históricos | Sistema Ambiental")
public class HistoricoView extends VerticalLayout {

    private final String FILE_DAT = "zonas.dat";
    private ComboBox<String> comboZonas;
    private VerticalLayout panelHistorico;

    public HistoricoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("📊 Análisis de Promedios Históricos"));
        add(new Paragraph("Selecciona una zona urbana para calcular el promedio de los últimos 30 días de contaminación y contrastarlo con los límites de la OMS."));

        comboZonas = new ComboBox<>("Seleccionar Zona");
        comboZonas.setPlaceholder("Zonas...");
        cargarNombresZonas();

        Button btnCalcular = new Button("Calcular Promedios", event -> mostrarHistoricoZona());
        panelHistorico = new VerticalLayout();

        add(comboZonas, btnCalcular, panelHistorico);
    }

    private void cargarNombresZonas() {
        File f = new File(FILE_DAT);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_DAT))) {
                Zona[] zonasGuardadas = (Zona[]) ois.readObject();
                String[] nombres = new String[zonasGuardadas.length];
                for (int i = 0; i < zonasGuardadas.length; i++) {
                    nombres[i] = zonasGuardadas[i].getNombre();
                }
                comboZonas.setItems(nombres);
            } catch (Exception e) {
                comboZonas.setItems("Centro", "Norte", "Sur", "Valle", "Quitumbe");
            }
        } else {
            comboZonas.setItems("Centro", "Norte", "Sur", "Valle", "Quitumbe");
        }
    }

    private void mostrarHistoricoZona() {
        if (comboZonas.getValue() == null) {
            Notification.show("Por favor, selecciona una zona.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        panelHistorico.removeAll();

        File f = new File(FILE_DAT);
        if (!f.exists()) {
            Notification.show("Primero debes ir a la pestaña 'Precargar Datos' y ejecutar la precarga.", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_DAT))) {
            Zona[] zonasGuardadas = (Zona[]) ois.readObject();
            Zona zonaSeleccionada = null;

            for (Zona z : zonasGuardadas) {
                if (z.getNombre().equals(comboZonas.getValue())) {
                    zonaSeleccionada = z;
                    break;
                }
            }

            if (zonaSeleccionada != null) {
                // Aquí calculas tus promedios recorriendo los meses y días de la zona
                double sumaPM25 = 0;
                int contadorDias = 0;

                if (zonaSeleccionada.getMeses() != null && zonaSeleccionada.getMeses().length > 0) {
                    var primerMes = zonaSeleccionada.getMeses()[0]; // Recorremos un mes representativo (30 días)
                    for (var dia : primerMes.getDias()) {
                        sumaPM25 += dia.getPm25();
                        contadorDias++;
                    }
                }

                double promedioPM25 = contadorDias > 0 ? (sumaPM25 / contadorDias) : 0.0;

                panelHistorico.add(new H3("Zona: " + zonaSeleccionada.getNombre()));
                panelHistorico.add(new Span("• Registros analizados: " + contadorDias + " días."));
                panelHistorico.add(new Span("• Promedio PM2.5 obtenido: " + String.format("%.2f", promedioPM25) + " µg/m³"));

                // Comparación directa contra límites OMS (Ej: Límite OMS diario/anual de PM2.5 es 15)
                if (promedioPM25 > 15.0) {
                    panelHistorico.add(new Paragraph("⚠️ ¡Atención! El promedio histórico de PM2.5 supera el límite sugerido por la OMS (15.0 µg/m³)."));
                } else {
                    panelHistorico.add(new Paragraph("✅ El promedio histórico cumple con los parámetros saludables de la OMS."));
                }
            }
        } catch (Exception e) {
            panelHistorico.add(new Paragraph("Error al procesar el archivo histórico: " + e.getMessage()));
        }
    }
}