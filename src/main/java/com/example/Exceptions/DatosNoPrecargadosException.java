package com.example.Exceptions;

// Se lanza si intentan usar funciones sin precargar datos base
public class DatosNoPrecargadosException extends Exception {
    public DatosNoPrecargadosException(String mensaje) {
        super(mensaje);
    }
}