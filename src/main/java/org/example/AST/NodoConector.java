package org.example.AST;

public class NodoConector extends NodoAST {
    private String conector;
    private NodoExpresion expresion;

    public NodoConector(String conector, NodoExpresion expresion, int fila, int columna) {
        super(fila, columna);
        this.conector = conector;
        this.expresion = expresion;
    }

    public String getConector() {
        return conector;
    }

    public NodoExpresion getExpresion() {
        return expresion;
    }

    @Override
    public String toStringArbol(String prefijo) {
        return prefijo + "├── Conector: " + conector + "\n" + expresion.toStringArbol(prefijo + "│   ");
    }
}