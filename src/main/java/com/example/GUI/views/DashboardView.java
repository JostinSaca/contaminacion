package com.example.GUI.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.example.GUI.MainView;

@Route(value = "dashboard", layout = MainView.class)
@PageTitle("Panel de Control | Sistema Ambiental")
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("📊 Panel de Control Ambiental"));
        add(new Paragraph("Bienvenido. Ejecute las acciones del flujo secuencial utilizando los accesos rápidos de abajo:"));

        // Botones de accesos rápidos directamente asociados a los casos del bucle inicio()
        Button btnIrPrecarga = new Button("1. Precargar Sistema", VaadinIcon.DOWNLOAD.create());
        btnIrPrecarga.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btnIrPrecarga.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("precarga")));

        Button btnIrHistoricos = new Button("2. Ver Históricos", VaadinIcon.BAR_CHART.create());
        btnIrHistoricos.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("historico")));

        Button btnIrPrediccion = new Button("3. Calcular Predicción", VaadinIcon.MAGIC.create());
        btnIrPrediccion.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnIrPrediccion.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("prediccion")));

        Button btnIrRecomendaciones = new Button("4. Recomendaciones Guardadas", VaadinIcon.DOCTOR.create());
        btnIrRecomendaciones.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("recomendaciones")));

        Button btnIrReportes = new Button("5. Generar Reportes Txt", VaadinIcon.FILE_TEXT.create());
        btnIrReportes.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("reportes")));

        // Distribución limpia en layouts organizados
        HorizontalLayout fila1 = new HorizontalLayout(btnIrPrecarga, btnIrHistoricos, btnIrPrediccion);
        HorizontalLayout fila2 = new HorizontalLayout(btnIrRecomendaciones, btnIrReportes);

        add(fila1, fila2);
    }
}