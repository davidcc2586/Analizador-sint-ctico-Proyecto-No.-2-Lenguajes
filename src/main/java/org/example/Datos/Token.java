package org.example.Datos;

public class Token {
    private int id;
    private String lexema;
    private TipoToken tipo;
    private int fila;
    private int columna;

    public Token(int id, String lexema, TipoToken tipo, int fila, int columna) {
        this.id = id;
        this.lexema = lexema;
        this.tipo = tipo;
        this.fila = fila;
        this.columna = columna;
    }

    public Token(TipoToken tipo, String lexema, int fila, int columna) {
        this.lexema = lexema;
        this.tipo = tipo;
        this.fila = fila;
        this.columna = columna;
    }

    public int getId() {
        return id;
    }

    public String getLexema() {
        return lexema;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        if(id == 0){
            return String.format("[%s, '%s', Fila: %d, Col: %d]", tipo, lexema, fila, columna);
        }
        return String.format("#%d | Lexema: '%s' | Tipo: %s | Fila: %d | Col: %d", id, lexema, tipo, fila, columna);
    }
}