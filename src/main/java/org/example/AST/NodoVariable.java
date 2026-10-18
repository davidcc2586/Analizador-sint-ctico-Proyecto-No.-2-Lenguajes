package org.example.AST;

public class NodoVariable extends NodoAST {
    private String nombre;
    private NodoExpresion expresion;

    public NodoVariable(String nombre, NodoExpresion expresion, int fila, int columna) {
        super(fila, columna);
        this.nombre = nombre;
        this.expresion = expresion;
    }

    public String getNombre() {
        return nombre;
    }

    public NodoExpresion getExpresion() {
        return expresion;
    }

    @Override
    public String toStringArbol(String prefijo) {
        return prefijo + "├── Variable: " + nombre + "\n" + expresion.toStringArbol(prefijo + "│   ");
    }
}