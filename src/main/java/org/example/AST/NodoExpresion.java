package org.example.AST;

import java.util.ArrayList;
import java.util.List;

public class NodoExpresion extends NodoAST {
    private String valor;
    private String tipoExpresion; // LITERAL, ID, CARGAR, CONCATENACION
    private String unidad;        // Para casos como "100 palabras"
    private List<NodoExpresion> terminos = new ArrayList<>();

    public NodoExpresion(String valor, String tipoExpresion, int fila, int columna) {
        super(fila, columna);
        this.valor = valor;
        this.tipoExpresion = tipoExpresion;
    }

    public String getValor() {
        return valor;
    }

    public String getTipoExpresion() {
        return tipoExpresion;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public List<NodoExpresion> getTerminos() {
        return terminos;
    }

    public void agregarTermino(NodoExpresion termino) {
        this.terminos.add(termino);
    }

    @Override
    public String toStringArbol(String prefijo) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefijo).append("├── Expresion (").append(tipoExpresion).append("): ").append(valor);
        if (unidad != null) {
            stringBuilder.append(" [Unidad: ").append(unidad).append("]");
        }
        stringBuilder.append("\n");
        for (NodoExpresion nodoExpresion : terminos) {
            stringBuilder.append(nodoExpresion.toStringArbol(prefijo + "│   "));
        }
        return stringBuilder.toString();
    }
}