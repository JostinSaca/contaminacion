package com.example.Modelos;



import java.io.Serializable;
public class Mes implements Serializable {
    private static final long serialVersionUID = 1L;

    private Dia[] dias;

    public Mes() {
        this.dias = new Dia[30];
        for (int i = 0; i < 30; i++) {
            this.dias[i] = new Dia(); // Inicializa cada objeto para evitar NullPointerException
        }
    }

    public Dia[] getDias() { return dias; }
    public void setDias(Dia[] dias) { this.dias = dias; }
}
