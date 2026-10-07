package org.example.Datos;

public class ErrorCompilacion {
    public enum TipoError { LEXICO, SINTACTICO, SEMANTICO }

    private TipoError tipo;
    private String lexema;
    private String descripcion;
    private int fila;
    private int columna;

    public ErrorCompilacion(TipoError tipo, String lexema, String descripcion, int fila, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.descripcion = descripcion;
        this.fila = fila;
        this.columna = columna;
    }

    public TipoError getTipo() { return tipo; }
    public String getLexema() { return lexema; }
    public String getDescripcion() { return descripcion; }
    public int getFila() { return fila; }
    public int getColumna() { return columna; }
}