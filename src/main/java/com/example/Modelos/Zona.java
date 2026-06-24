package com.example.Modelos;

import com.example.Interfaces.Analizable;
import java.io.Serializable;

public class Zona implements Analizable, Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private Mes[] meses;

    public Zona(String nombre) {
        this.nombre = nombre;
        this.meses = new Mes[12];
        for (int i = 0; i < 12; i++) {
            this.meses[i] = new Mes();
        }
    }

    @Override
    public ResultadoPrediccion calcularPrediccion(int mesIndex) {
        ResultadoPrediccion r = new ResultadoPrediccion();
        r.setMes(mesIndex);

        // Simulación del nuevo día (Se capturará en el CLI y se pasará, o se puede setear externamente)
        // Usaremos de base los pesos temporales que definiste en tu código de C:
        double[] pesos = {5, 5, 5, 6, 6, 7, 8, 10, 12, 14, 22}; // Suma = 100

        double sumaNA = 0, sumaNO2 = 0, sumaSO2 = 0, sumaCO2 = 0, sumaPM25 = 0;
        Mes mesSeleccionado = this.meses[mesIndex];

        // Sumar últimos 10 días históricos (índices 20 al 29 en Java)
        for (int i = 0; i < 10; i++) {
            int d = 20 + i;
            Dia diaHist = mesSeleccionado.getDias()[d];
            sumaNA   += diaHist.getNa()   * pesos[i];
            sumaNO2  += diaHist.getNo2()  * pesos[i];
            sumaSO2  += diaHist.getSo2()  * pesos[i];
            sumaCO2  += diaHist.getCo2()  * pesos[i];
            sumaPM25 += diaHist.getPm25() * pesos[i];
        }

        // Nota: Los valores del día ingresado "nuevo" (pesos[10]) se deben calcular ponderados
        // promediando dividiendo para 100 como lo hacías en C.

        return r;
    }

    @Override
    public boolean evaluarAlerta(ResultadoPrediccion r) {
        return (r.getpNA() > 100 || r.getpNO2() > 25 || r.getpSO2() > 20 || r.getpCO2() > 4 || r.getpPM25() > 15);
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Mes[] getMeses() { return meses; }
}