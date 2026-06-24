package com.example.GUI.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.example.GUI.MainView;
import com.example.GUI.views.MongoService;
import com.example.Modelos.Zona;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "precarga", layout = MainView.class)
@PageTitle("Precarga de Datos | Sistema Ambiental")
public class PrecargaView extends VerticalLayout {

    @Autowired
    private MongoService mongoService;

    private Zona[] zonas;

    public PrecargaView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("📥 Precarga de Datos Históricos en MongoDB"));
        add(new Paragraph("Genera y sube datos aleatorios iniciales para todas las zonas directamente a la base de datos NoSQL en la nube."));

        inicializarZonas();

        Button btnPrecargar = new Button("Ejecutar Precarga en la Nube", event -> precargarDatosMongo());

        add(btnPrecargar);
    }

    private void inicializarZonas() {
        zonas = new Zona[5];
        zonas[0] = new Zona("Centro");
        zonas[1] = new Zona("Norte");
        zonas[2] = new Zona("Sur");
        zonas[3] = new Zona("Valle");
        zonas[4] = new Zona("Quitumbe");
    }

    private void precargarDatosMongo() {
        try {
            for (Zona z : zonas) {
                for (var mes : z.getMeses()) {
                    for (var dia : mes.getDias()) {
                        dia.setNa(40 + Math.random() * 70);
                        dia.setNo2(15 + Math.random() * 20);
                        dia.setSo2(10 + Math.random() * 15);
                        dia.setCo2(1 + Math.random() * 4);
                        dia.setPm25(10 + Math.random() * 15);
                        dia.setTemperatura(14 + Math.random() * 12);
                        dia.setHumedad(50 + Math.random() * 40);
                        dia.setViento(1 + Math.random() * 8);
                    }
                }
            }

            // AQUÍ SE CONECTA A MONGO: Guardamos usando el servicio
            mongoService.guardarZonas(zonas);

            Notification n = new Notification("✓ Datos precargados exitosamente en MongoDB Atlas", 3000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            n.open();
        } catch (Exception e) {
            Notification n = new Notification("✗ Error al conectar con MongoDB: " + e.getMessage(), 4000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            n.open();
        }
    }
}