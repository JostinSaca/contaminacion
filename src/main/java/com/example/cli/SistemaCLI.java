package com.example.cli;

import com.example.Modelos.*;
import com.example.Exceptions.*;
import java.io.*;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SistemaCLI {

    private Zona[] zonas;
    private final String FILE_DAT = "zonas.dat";
    private final String FILE_PREDICCIONES = "predicciones.dat";

    public SistemaCLI() {
        // Inicialización de las 5 zonas del sistema
        zonas = new Zona[5];
        zonas[0] = new Zona("Centro");
        zonas[1] = new Zona("Norte");
        zonas[2] = new Zona("Sur");
        zonas[3] = new Zona("Valle");
        zonas[4] = new Zona("Quitumbe");
    }

    /* ================= VALIDADORES ROBUSTOS DE ENTRADA ================= */

    // 1. Validador base: Evita que el programa se rompa si ingresan letras
    public double validarDouble() {
        Scanner sc = new Scanner(System.in);
        double num = 0;
        boolean flag = true;
        do {
            try {
                num = sc.nextDouble();
                flag = false;
            } catch (InputMismatchException ex1) {
                System.out.println("Error: el dato ingresado es incorrecto.");
                System.out.print(">> ");
            } finally {
                sc.nextLine(); // Limpiar el búfer de entrada
            }
        } while (flag);
        return num;
    }

    // 2. Sobrecarga: Protege rangos numéricos específicos (adiós números negativos)
    public double validarDouble(double min, double max) {
        while (true) {
            double valor = validarDouble(); // Invoca la validación de tipo
            if (valor >= min && valor <= max) {
                return valor;
            }
            System.out.printf("Error: Ingrese un valor valido entre %.1f y %.1f\n", min, max);
            System.out.print(">> ");
        }
    }

    /* ================= CONTROL DE EXCEPCIONES DE FLUJO ================= */

    private void verificarDatosBase() throws DatosNoPrecargadosException {
        File f = new File(FILE_DAT);
        if (!f.exists() || f.length() == 0) {
            throw new DatosNoPrecargadosException("ERROR: No se han precargado los datos historicos base. Seleccione la Opcion 1 primero.");
        }
    }

    private void verificarPrediccionesExistentes() throws SinPrediccionesException {
        File f = new File(FILE_PREDICCIONES);
        if (!f.exists() || f.length() == 0) {
            throw new SinPrediccionesException("ERROR: El registro de predicciones esta vacio. Debe generar una prediccion en la Opcion 3 antes de generar reportes.");
        }
    }

    /* ================= COMPONENTES DE MENÚS ================= */

    public int menu() {
        System.out.println("\n--- SISTEMA DE MONITOREO AMBIENTAL ---");
        System.out.println("1. Precargar datos");
        System.out.println("2. Mostrar datos historicos");
        System.out.println("3. Ingresar nuevo dia y predecir");
        System.out.println("4. Ver Recomendaciones guardadas");
        System.out.println("5. Reportes");
        System.out.println("6. Salir");
        System.out.print("Opcion: ");
        return (int) validarDouble(1, 6);
    }

    public int menuReportes() {
        System.out.println("\n--- MENU DE REPORTES ---");
        System.out.println("1. Reporte historico (30 dias)");
        System.out.println("2. Reporte actual (predicciones)");
        System.out.println("3. Volver");
        System.out.print("Opcion: ");
        return (int) validarDouble(1, 3);
    }

    public int seleccionarZona() {
        System.out.println("\nSeleccione la Zona:");
        System.out.println("1.Centro 2.Norte 3.Sur 4.Valle 5.Quitumbe 6.Salir");
        System.out.print(">> ");
        return (int) validarDouble(1, 6);
    }

    public int seleccionarMes() {
        System.out.print("Mes (1-12, 0 salir): ");
        return (int) validarDouble(0, 12);
    }

    /* ================= OPERACIONES DE DATOS Y PERSISTENCIA ================= */

    public void precargarDatos() {
        for (Zona z : zonas) {
            for (Mes m : z.getMeses()) {
                for (Dia d : m.getDias()) {
                    d.setNa(40 + Math.random() * 70);
                    d.setNo2(15 + Math.random() * 20);
                    d.setSo2(10 + Math.random() * 15);
                    d.setCo2(1 + Math.random() * 4);
                    d.setPm25(10 + Math.random() * 15);
                    d.setTemperatura(14 + Math.random() * 12);
                    d.setHumedad(50 + Math.random() * 40);
                    d.setViento(1 + Math.random() * 8);
                }
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_DAT))) {
            oos.writeObject(zonas);
            System.out.println("Datos historicos base cargados y guardados con exito en '" + FILE_DAT + "'.");
        } catch (IOException e) {
            System.out.println("Error al guardar precarga: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void leerDAT() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_DAT))) {
            zonas = (Zona[]) ois.readObject();
        } catch (Exception e) {
            System.out.println("Error de lectura en base binaria: " + e.getMessage());
        }
    }

    public void mostrarDatosHistoricos() {
        leerDAT();
        int zOpt = seleccionarZona(); if (zOpt == 6) return; int zIndex = zOpt - 1;
        int mOpt = seleccionarMes(); if (mOpt == 0) return; int mIndex = mOpt - 1;

        System.out.println("\nDatos de la zona: " + zonas[zIndex].getNombre());
        System.out.println("Dia | NA    | NO2   | SO2   | CO2   | PM2.5 ");
        System.out.println("--------------------------------------------");

        Dia[] dias = zonas[zIndex].getMeses()[mIndex].getDias();
        for (int i = 0; i < 30; i++) {
            Dia d = dias[i];
            System.out.printf("%2d  | %5.1f | %5.1f | %5.1f | %5.1f | %5.1f\n",
                    (i + 1), d.getNa(), d.getNo2(), d.getSo2(), d.getCo2(), d.getPm25());
        }
    }

    public void ejecutarPrediccion() {
        leerDAT();
        int zOpt = seleccionarZona(); if (zOpt == 6) return; int zIndex = zOpt - 1;
        int mOpt = seleccionarMes(); if (mOpt == 0) return; int mIndex = mOpt - 1;

        System.out.println("\n--- INGRESO DE NUEVOS PARAMETROS METEOROLOGICOS ---");
        System.out.print("NA µg/m³: "); double na = validarDouble(0, 5000);
        System.out.print("NO2 µg/m³: "); double no2 = validarDouble(0, 5000);
        System.out.print("SO2 µg/m³: "); double so2 = validarDouble(0, 5000);
        System.out.print("CO2 µg/m³: "); double co2 = validarDouble(0, 5000);
        System.out.print("PM2.5 µg/m³: "); double pm25 = validarDouble(0, 5000);
        System.out.print("Temperatura C°: "); double temp = validarDouble(-50, 60);
        System.out.print("Humedad (%%): "); double hum = validarDouble(0, 100);
        System.out.print("Viento (m/s): "); double vien = validarDouble(0, 150);

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

        System.out.println("\n--- RESULTADO DE LA PREDICCION ---");
        System.out.printf("Indice de Contaminacion (IC) calculado: %.2f\n", r.getIC());

        if (zonas[zIndex].evaluarAlerta(r)) {
            System.out.println("[ALERTA AMBIENTAL DETECTADA]");
            imprimirRecomendacionesEms(r);
        } else {
            System.out.println("Calidad del aire dentro de los limites.");
        }
        guardarPrediccion(r);
    }

    private void imprimirRecomendacionesEms(ResultadoPrediccion r) {
        if (r.getpNA() > 100) System.out.println("-> NA Elevado: Limitar actividades al aire libre.");
        if (r.getpNO2() > 25) System.out.println("-> NO2 Elevado: Reducir uso de vehiculos combustibles.");
        if (r.getpPM25() > 15) System.out.println("-> PM2.5 Critico: Usar mascarilla obligatoria.");
    }

    private void guardarPrediccion(ResultadoPrediccion r) {
        File file = new File(FILE_PREDICCIONES);
        boolean append = file.exists();
        try (FileOutputStream fos = new FileOutputStream(file, true);
             ObjectOutputStream oos = append ? new ObjectOutputStream(fos) {
                 @Override protected void writeStreamHeader() throws IOException { reset(); }
             } : new ObjectOutputStream(fos)) {
            oos.writeObject(r);
            System.out.println("Prediccion guardada en historial binario.");
        } catch (IOException e) {
            System.out.println("Error guardando prediccion: " + e.getMessage());
        }
    }

    /* ================= HISTORIAL COMPLETO CON GASES Y RECOMENDACIONES (OPCIÓN 4) ================= */

    public void mostrarPrediccionesGuardadas() {
        int zOpt = electromagneticZone(); if (zOpt == 6) return; int zIndex = zOpt - 1;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PREDICCIONES))) {
            System.out.println("\n=== HISTORICO DE MONITORIZACION Y RECOMENDACIONES ===");
            System.out.println("-----------------------------------------------------------------------");
            boolean hayDatos = false;

            while (true) {
                try {
                    ResultadoPrediccion r = (ResultadoPrediccion) ois.readObject();
                    if (r.getZonaIndex() == zIndex) {
                        hayDatos = true;
                        System.out.printf("Mes: %2d | Dia: %2d | IC: %5.2f\n", (r.getMes() + 1), r.getDia(), r.getIC());
                        System.out.printf("   [Gases] NA: %.1f | NO2: %.1f | SO2: %.1f | CO2: %.1f | PM2.5: %.1f\n",
                                r.getpNA(), r.getpNO2(), r.getpSO2(), r.getpCO2(), r.getpPM25());

                        System.out.print("   [Recomendaciones]: ");
                        boolean tieneAlerta = false;
                        if (r.getpNA() > 100) { System.out.print("Limitar actividades exteriores. "); tieneAlerta = true; }
                        if (r.getpNO2() > 25) { System.out.print("Reducir uso de transporte vehicular a gasolina. "); tieneAlerta = true; }
                        if (r.getpPM25() > 15) { System.out.print("Usar mascarilla obligatoria en la zona. "); tieneAlerta = true; }

                        if (!tieneAlerta) {
                            System.out.print("Calidad del aire aceptable. Sin restricciones activas.");
                        }
                        System.out.println("\n-----------------------------------------------------------------------");
                    }
                } catch (EOFException e) {
                    break;
                }
            }
            if (!hayDatos) System.out.println("No se encontraron predicciones registradas para esta zona.");
        } catch (Exception e) {
            System.out.println("Error procesando registros binarios: " + e.getMessage());
        }
    }

    private int electromagneticZone() {
        return seleccionarZona();
    }

    /* ================= REPORTE EN TEXTO ACUMULATIVO COMPLETO (OPCIÓN 5 -> 2) ================= */

    public void generarReporteActualTxt() {
        leerDAT();
        String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        File file = new File("reporte_actual.txt");
        boolean esNuevo = !file.exists();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PREDICCIONES));
             PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {

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
                    break;
                }
            }
            System.out.println("Reporte consolidado guardado con exito en 'reporte_actual.txt'.");
        } catch (Exception e) {
            System.out.println("Error generando el reporte de texto: " + e.getMessage());
        }
    }

    /* ================= BUCLE PRINCIPAL CON FILTROS DE EXCEPCION ================= */

    public void inicio() {
        int opc;
        do {
            opc = menu();
            try {
                switch (opc) {
                    case 1 -> precargarDatos();
                    case 2 -> {
                        verificarDatosBase();
                        mostrarDatosHistoricos();
                    }
                    case 3 -> {
                        verificarDatosBase();
                        ejecutarPrediccion();
                    }
                    case 4 -> {
                        verificarPrediccionesExistentes();
                        mostrarPrediccionesGuardadas();
                    }
                    case 5 -> {
                        int opR = menuReportes();
                        if (opR == 1) {
                            verificarDatosBase();
                            System.out.println("Reporte historico seleccionado.");
                        } else if (opR == 2) {
                            verificarDatosBase();
                            verificarPrediccionesExistentes();
                            generarReporteActualTxt();
                        }
                    }
                    case 6 -> System.out.println("Finalizando aplicacion...");
                    default -> System.out.println("Opcion no valida.");
                }
            } catch (DatosNoPrecargadosException | SinPrediccionesException ex) {
                System.out.println("\n[VALIDACION] " + ex.getMessage());
            }
        } while (opc != 6);
    }
}