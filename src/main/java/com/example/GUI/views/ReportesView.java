package com.example.GUI.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.example.GUI.MainView;
import com.example.Modelos.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Route(value = "reportes", layout = MainView.class)
@PageTitle("Reportes | Sistema Ambiental")
public class ReportesView extends VerticalLayout {

    private final String FILE_DAT = "zonas.dat";
    private final String FILE_PREDICCIONES = "predicciones.dat";
    private Zona[] zonas;

    public ReportesView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("📋 Módulo de Generación de Reportes"));
        add(new Paragraph("Presione el botón para procesar de forma acumulativa todas las predicciones almacenadas y exportar el documento consolidado en formato de texto plano (TXT) para auditorías externas."));

        Button btnGenerarReporte = new Button("Generar Reporte Actual (.txt)", VaadinIcon.FILE_TEXT.create());
        btnGenerarReporte.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        btnGenerarReporte.addClickListener(event -> ejecutarExportacionReporte());

        add(btnGenerarReporte);
    }

    private void leerDAT() {
        File f = new File(FILE_DAT);
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_DAT))) {
            zonas = (Zona[]) ois.readObject();
        } catch (Exception e) {
            // Error silencioso controlado
        }
    }

    private void ejecutarExportacionReporte() {
        leerDAT();
        File predFile = new File(FILE_PREDICCIONES);

        if (!predFile.exists() || predFile.length() == 0) {
            Notification.show("❌ ERROR: No se puede generar el reporte porque el registro de predicciones está vacío.", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (zonas == null) {
            // Inicialización de emergencia si no se leyó el .dat para evitar NullPointerException con los nombres de las zonas
            zonas = new Zona[5];
            zonas[0] = new Zona("Centro"); zonas[1] = new Zona("Norte"); zonas[2] = new Zona("Sur");
            zonas[3] = new Zona("Valle"); zonas[4] = new Zona("Quitumbe");
        }

        String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        File txtFile = new File("reporte_actual.txt");
        boolean esNuevo = !txtFile.exists();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PREDICCIONES));
             PrintWriter writer = new PrintWriter(new FileWriter(txtFile, true))) {

            if (esNuevo) {
                writer.println("===== REPORTE ACUMULADO DE PREDICCIONES AMBIENTALES =====");
            }

            writer.println("\n--- Bloque de Reporte Actualizado el " + fechaActual + " ---");
            writer.println("Zona\t\tMes\tDia\tIC\tNA\tNO2\tSO2\tCO2\tPM2.5\tAlertas y Recomendaciones Medicas");
            writer.println("------------------------------------------------------------------------------------------------------------------------");

            while (true) {
                try {
                    ResultadoPrediccion r = (ResultadoPrediccion) ois.readObject();

                    StringBuilder alertas = new StringBuilder();
                    if (r.getpNA() > 100) alertas.append("Limitar actividades exteriores. ");
                    if (r.getpNO2() > 25) alertas.append("Reducir uso vehicular. ");
                    if (r.getpPM25() > 15) alertas.append("Uso de mascarilla obligatorio. ");
                    if (alertas.length() == 0) alertas.append("Calidad del aire aceptable.");

                    writer.printf("%s\t\t%d\t%d\t%.2f\t%.1f\t%.1f\t%.1f\t%.1f\t%.1f\t%s\n",
                            zonas[r.getZonaIndex()].getNombre(), (r.getMes() + 1), r.getDia(), r.getIC(),
                            r.getpNA(), r.getpNO2(), r.getpSO2(), r.getpCO2(), r.getpPM25(), alertas.toString());
                } catch (EOFException e) {
                    break; // Fin del stream binario
                }
            }

            Notification n = new Notification("✓ Reporte consolidado guardado con éxito en 'reporte_actual.txt'", 4000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            n.open();

        } catch (Exception e) {
            Notification.show("Error generando el reporte físico de texto: " + e.getMessage(), 4000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}