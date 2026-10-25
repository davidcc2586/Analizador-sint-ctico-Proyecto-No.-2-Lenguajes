package org.example.Semantico;

import java.util.ArrayList;
import java.util.List;

//Guarda agentes y variables. Los errores semánticos los genera AnalizadorSemantico.
public class TablaSimbolos {
    public static final String GLOBAL = "GLOBAL";

    private final List<Simbolo> simbolos = new ArrayList<>();

    // Devuelve false si ya existe ese nombre en ese ámbito (no lo agrega).
    public boolean agregarSimbolo(String nombre, TipoSimbolo tipo, String ambito, int linea) {
        if (existe(nombre, ambito)){
            return false;
        }
        simbolos.add(new Simbolo(nombre, tipo, ambito, linea));
        return true;
    }

    // Busca un nombre dentro de un ámbito (el lenguaje distingue mayúsculas).
    public boolean existe(String nombre, String ambito) {
        for (Simbolo simbolo : simbolos) {
            if (simbolo.getNombre().equals(nombre) && simbolo.getAmbito().equals(ambito)) {
                return true;
            }
        }
        return false;
    }

    public boolean existeAgente(String nombre) {
        for (Simbolo simbolo : simbolos) {
            if (simbolo.getTipo() == TipoSimbolo.AGENTE && simbolo.getNombre().equals(nombre))
                return true;
        }
        return false;
    }

    public List<Simbolo> getSimbolos() {
        return simbolos;
    }
}
