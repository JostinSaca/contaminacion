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
import com.example.Modelos.Zona;
import java.io.*;

@Route(value = "precarga", layout = MainView.class)
@PageTitle("Precarga de Datos | Sistema Ambiental")
public class PrecargaView extends VerticalLayout {

    private final String FILE_DAT = "zonas.dat";
    private Zona[] zonas;

    public PrecargaView() {
        add(new H2("📥 Precarga de Datos Históricos"));
        add(new Paragraph("Genera datos aleatorios para todas las zonas."));

        inicializarZonas();

        Button btnPrecargar = new Button("Ejecutar Precarga", event -> precargarDatos());
        Button btnVerificar = new Button("Verificar Archivo", event -> verificarArchivo());

        add(btnPrecargar, btnVerificar);
    }

    private void inicializarZonas() {
        zonas = new Zona[5];
        zonas[0] = new Zona("Centro");
        zonas[1] = new Zona("Norte");
        zonas[2] = new Zona("Sur");
        zonas[3] = new Zona("Valle");
        zonas[4] = new Zona("Quitumbe");
    }

    private void precargarDatos() {
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

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_DAT))) {
                oos.writeObject(zonas);
            }

            Notification n = new Notification("✓ Datos precargados exitosamente", 3000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            n.open();
        } catch (IOException e) {
            Notification n = new Notification("✗ Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            n.open();
        }
    }

    private void verificarArchivo() {
        File f = new File(FILE_DAT);
        if (f.exists() && f.length() > 0) {
            Notification n = new Notification("✓ Archivo encontrado (" + f.length() + " bytes)", 3000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            n.open();
        } else {
            Notification n = new Notification("✗ Archivo no encontrado", 3000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            n.open();
        }
    }
}
