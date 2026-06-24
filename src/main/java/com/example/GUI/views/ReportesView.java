package com.example.GUI.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.example.GUI.MainView;
import com.example.GUI.views.MongoService;
import com.example.Modelos.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "reportes", layout = MainView.class)
@PageTitle("Reportes | Sistema Ambiental")
public class ReportesView extends VerticalLayout {

    @Autowired
    private MongoService mongoService;

    private Grid<ResultadoPrediccion> tablaPredicciones;
    private Zona[] zonas;

    public ReportesView() {
        setSpacing(true);
        setPadding(true);

        add(new H2(" Módulo de Reportes Consolidados (MongoDB)"));
        add(new Paragraph("Consulte el historial de alertas almacenado en la nube de MongoDB Atlas o expórte el reporte consolidado en un archivo físico de texto plano."));

        tablaPredicciones = new Grid<>(ResultadoPrediccion.class, false);
        tablaPredicciones.addColumn(r -> obtenerNombreZona(r.getZonaIndex())).setHeader("Zona");
        tablaPredicciones.addColumn(r -> (r.getMes() + 1)).setHeader("Mes");
        tablaPredicciones.addColumn(ResultadoPrediccion::getDia).setHeader("Día");
        tablaPredicciones.addColumn(r -> String.format("%.2f", r.getIC())).setHeader("IC Calculado");
        tablaPredicciones.addColumn(r -> String.format("%.1f", r.getpPM25())).setHeader("PM2.5");
        tablaPredicciones.addColumn(r -> String.format("%.1f", r.getpCO2())).setHeader("CO2");

        Button btnCargarTabla = new Button("Consultar datos de MongoDB", VaadinIcon.DATABASE.create());
        btnCargarTabla.addClickListener(e -> actualizarTablaDesdeMongo());

        Button btnGenerarTxt = new Button("Exportar Reporte Acumulado a .TXT", VaadinIcon.FILE_TEXT.create());
        btnGenerarTxt.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGenerarTxt.addClickListener(event -> ejecutarExportacionReporteTxt());

        add(btnCargarTabla, tablaPredicciones, btnGenerarTxt);
    }

    private String obtenerNombreZona(int index) {
        return switch (index) {
            case 0 -> "Centro";
            case 1 -> "Norte";
            case 2 -> "Sur";
            case 3 -> "Valle";
            case 4 -> "Quitumbe";
            default -> "Desconocida";
        };
    }

    private void actualizarTablaDesdeMongo() {
        List<ResultadoPrediccion> historial = mongoService.leerPredicciones();
        if (historial == null || historial.isEmpty()) {
            Notification.show("No se encontraron predicciones en MongoDB Atlas.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        } else {
            tablaPredicciones.setItems(historial);
            Notification.show("✓ Datos sincronizados desde la nube.", 2000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }

    private void ejecutarExportacionReporteTxt() {
        List<ResultadoPrediccion> historial = mongoService.leerPredicciones();

        if (historial == null || historial.isEmpty()) {
            Notification.show("❌ ERROR: No hay datos en MongoDB para exportar. Ejecute una predicción primero.", 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        File txtFile = new File("reporte_actual.txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(txtFile, true))) {
            writer.println("\n--- Bloque de Reporte Descargado desde MongoDB el " + fechaActual + " ---");
            writer.println("Zona\t\tMes\tDia\tIC\tNA\tNO2\tSO2\tCO2\tPM2.5\tAlertas y Recomendaciones Medicas");
            writer.println("------------------------------------------------------------------------------------------------------------------------");

            for (ResultadoPrediccion r : historial) {
                StringBuilder alertas = new StringBuilder();
                if (r.getpNA() > 100) alertas.append("Limitar actividades exteriores. ");
                if (r.getpNO2() > 25) alertas.append("Reducir uso vehicular. ");
                if (r.getpPM25() > 15) alertas.append("Uso de mascarilla obligatorio. ");
                if (alertas.length() == 0) alertas.append("Calidad del aire aceptable.");

                writer.printf("%s\t\t%d\t%d\t%.2f\t%.1f\t%.1f\t%.1f\t%.1f\t%.1f\t%s\n",
                        obtNombreZona(r.getZonaIndex()), (r.getMes() + 1), r.getDia(), r.getIC(),
                        r.getpNA(), r.getpNO2(), r.getpSO2(), r.getpCO2(), r.getpPM25(), alertas.toString());
            }

            Notification.show("✓ Reporte exportado con éxito a 'reporte_actual.txt' usando datos de MongoDB Atlas.", 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show("Error al escribir el archivo: " + e.getMessage(), 4000, Notification.Position.BOTTOM_START);
        }
    }

    private String obtNombreZona(int index) {
        return switch (index) {
            case 0 -> "Centro"; case 1 -> "Norte"; case 2 -> "Sur";
            case 3 -> "Valle"; case 4 -> "Quitumbe"; default -> "Centro";
        };
    }
}