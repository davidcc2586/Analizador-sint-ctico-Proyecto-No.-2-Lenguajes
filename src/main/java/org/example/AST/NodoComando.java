package org.example.AST;

import java.util.ArrayList;
import java.util.List;

public class NodoComando extends NodoAST {
    private String nombreComando;
    private NodoExpresion argumentoPrincipal;
    private List<NodoConector> conectores = new ArrayList<>();
    private String variableDestino;

    public NodoComando(String nombreComando, NodoExpresion argumentoPrincipal, String variableDestino, int fila, int columna) {
        super(fila, columna);
        this.nombreComando = nombreComando;
        this.argumentoPrincipal = argumentoPrincipal;
        this.variableDestino = variableDestino;
    }

    public String getNombreComando() {
        return nombreComando;
    }

    public NodoExpresion getArgumentoPrincipal() {
        return argumentoPrincipal;
    }

    public List<NodoConector> getConectores() {
        return conectores;
    }

    public String getVariableDestino() {
        return variableDestino;
    }

    public void setVariableDestino(String variableDestino) {
        this.variableDestino = variableDestino;
    }

    public void agregarConector(NodoConector conector) {
        this.conectores.add(conector);
    }

    @Override
    public String toStringArbol(String prefijo) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefijo).append("├── Comando: ").append(nombreComando).append(" -> ").append(variableDestino).append("\n");
        if (argumentoPrincipal != null) {
            stringBuilder.append(argumentoPrincipal.toStringArbol(prefijo + "│   "));
        }
        for (NodoConector nodoConector : conectores) {
            stringBuilder.append(nodoConector.toStringArbol(prefijo + "│   "));
        }
        return stringBuilder.toString();
    }
}