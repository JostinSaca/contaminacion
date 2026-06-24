package com.example.GUI;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends AppLayout {

    public MainView() {
        setPrimarySection(Section.DRAWER);
        addToDrawer(createDrawer());
        addToNavbar(new DrawerToggle());
        addToNavbar(createHeader());
    }

    private Component createDrawer() {
        SideNav sideNav = new SideNav();
        sideNav.setLabel("Menú de Navegación");
        
        sideNav.addItem(
            new SideNavItem("Panel de Control", "dashboard", new Icon(VaadinIcon.DASHBOARD)),
            new SideNavItem("Precargar Datos", "precarga", new Icon(VaadinIcon.DOWNLOAD)),
            new SideNavItem("Datos Históricos", "historico", new Icon(VaadinIcon.TABLE)),
            new SideNavItem("Nueva Predicción", "prediccion", new Icon(VaadinIcon.BAR_CHART)),
            new SideNavItem("Recomendaciones", "recomendaciones", new Icon(VaadinIcon.LIGHTBULB)),
            new SideNavItem("Reportes", "reportes", new Icon(VaadinIcon.FILE_TEXT))
        );
        
        return sideNav;
    }

    private Component createHeader() {
        H1 title = new H1("🌍 Sistema de Monitoreo Ambiental");
        title.getStyle().set("margin", "0");
        return title;
    }
}
