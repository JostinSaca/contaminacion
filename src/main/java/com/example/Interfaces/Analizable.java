package com.example.Interfaces;

import com.example.Modelos.ResultadoPrediccion;

public interface Analizable {
    ResultadoPrediccion calcularPrediccion(int mesIndex);
    boolean evaluarAlerta(ResultadoPrediccion r);
}