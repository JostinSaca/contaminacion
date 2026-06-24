package com.example.Modelos;

import java.io.Serializable;

public class Dia implements Serializable {
    private static final long serialVersionUID = 1L;

    private double na, no2, so2, co2, pm25;
    private double temperatura, humedad, viento;

    // Constructor vacío por defecto
    public Dia() {}

    public Dia(double na, double no2, double so2, double co2, double pm25, double temperatura, double humedad, double viento) {
        this.na = na;
        this.no2 = no2;
        this.so2 = so2;
        this.co2 = co2;
        this.pm25 = pm25;
        this.temperatura = temperatura;
        this.humedad = humedad;
        this.viento = viento;
    }

    // Getters y Setters
    public double getNa() { return na; }
    public void setNa(double na) { this.na = na; }
    public double getNo2() { return no2; }
    public void setNo2(double no2) { this.no2 = no2; }
    public double getSo2() { return so2; }
    public void setSo2(double so2) { this.so2 = so2; }
    public double getCo2() { return co2; }
    public void setCo2(double co2) { this.co2 = co2; }
    public double getPm25() { return pm25; }
    public void setPm25(double pm25) { this.pm25 = pm25; }
    public double getTemperatura() { return temperatura; }
    public void setTemperatura(double temperatura) { this.temperatura = temperatura; }
    public double getHumedad() { return humedad; }
    public void setHumedad(double humedad) { this.humedad = humedad; }
    public double getViento() { return viento; }
    public void setViento(double viento) { this.viento = viento; }
}
