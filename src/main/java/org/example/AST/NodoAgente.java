package org.example.AST;

import java.util.ArrayList;
import java.util.List;

public class NodoAgente extends NodoAST {
    private String nombre;
    private String contexto;
    private List<NodoVariable> variables = new ArrayList<>();
    private List<NodoComando> comandos = new ArrayList<>();

    public NodoAgente(String nombre, int fila, int columna) {
        super(fila, columna);
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContexto() {
        return contexto;
    }

    public void setContexto(String contexto) {
        this.contexto = contexto;
    }

    public List<NodoVariable> getVariables() {
        return variables;
    }
    public List<NodoComando> getComandos() {
        return comandos;
    }

    public void agregarVariable(NodoVariable nodoVariable) {
        this.variables.add(nodoVariable);
    }

    public void agregarComando(NodoComando nodoComando) {
        this.comandos.add(nodoComando);
    }

    @Override
    public String toStringArbol(String prefijo) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefijo).append("├── Agente: ").append(nombre).append("\n");
        stringBuilder.append(prefijo).append("│   ├── Contexto: \"").append(contexto).append("\"\n");
        for (NodoVariable nodoVariable : variables) {
            stringBuilder.append(nodoVariable.toStringArbol(prefijo + "│   "));
        }
        for (NodoComando nodoComando : comandos) {
            stringBuilder.append(nodoComando.toStringArbol(prefijo + "│   "));
        }
        return stringBuilder.toString();
    }
}