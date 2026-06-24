package com.example.Modelos;

import java.io.Serializable;

public class ResultadoPrediccion implements Serializable {
    private static final long serialVersionUID = 1L;

    private int zonaIndex;
    private int mes;
    private int dia = 31;
    private double pNA, pNO2, pSO2, pCO2, pPM25;
    private double IC;

    public ResultadoPrediccion() {}

    // Getters y Setters
    public int getZonaIndex() { return zonaIndex; }
    public void setZonaIndex(int zonaIndex) { this.zonaIndex = zonaIndex; }
    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }
    public int getDia() { return dia; }
    public double getpNA() { return pNA; }
    public void setpNA(double pNA) { this.pNA = pNA; }
    public double getpNO2() { return pNO2; }
    public void setpNO2(double pNO2) { this.pNO2 = pNO2; }
    public double getpSO2() { return pSO2; }
    public void setpSO2(double pSO2) { this.pSO2 = pSO2; }
    public double getpCO2() { return pCO2; }
    public void setpCO2(double pCO2) { this.pCO2 = pCO2; }
    public double getpPM25() { return pPM25; }
    public void setpPM25(double pPM25) { this.pPM25 = pPM25; }
    public double getIC() { return IC; }
    public void setIC(double IC) { this.IC = IC; }
}
