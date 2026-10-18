package org.example.AST;

public abstract class NodoAST {
    private int fila;
    private int columna;

    public NodoAST(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    public abstract String toStringArbol(String prefijo);
}