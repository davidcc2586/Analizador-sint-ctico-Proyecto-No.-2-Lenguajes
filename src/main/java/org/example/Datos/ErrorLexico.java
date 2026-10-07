package org.example.Datos;

public class ErrorLexico {
    private String lexema;
    private String descripcion;
    private int fila;
    private int columna;

    public ErrorLexico(String lexema, String descripcion, int fila, int columna) {
        this.lexema = lexema;
        this.descripcion = descripcion;
        this.fila = fila;
        this.columna = columna;
    }

    public String getLexema() {
        return lexema;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return String.format("Lexema: '%s' | Descripción: %s | Fila: %d | Col: %d", lexema, descripcion, fila, columna);
    }
}