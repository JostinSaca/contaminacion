package com.example.Exceptions;

// Se lanza si intentan ver reportes o históricos de predicciones y el archivo está vacío
public class SinPrediccionesException extends Exception {
    public SinPrediccionesException(String mensaje) {
        super(mensaje);
    }
}