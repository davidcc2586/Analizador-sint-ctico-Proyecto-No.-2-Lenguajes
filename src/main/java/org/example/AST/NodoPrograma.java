package org.example.AST;

import java.util.ArrayList;
import java.util.List;

public class NodoPrograma extends NodoAST {
    private List<NodoDirectiva> directivas = new ArrayList<>();
    private List<NodoAgente> agentes = new ArrayList<>();
    private List<NodoReferencia> ejecuciones = new ArrayList<>();
    private List<NodoReferencia> exportaciones = new ArrayList<>();

    public NodoPrograma(int fila, int columna) {
        super(fila, columna);
    }

    public List<NodoDirectiva> getDirectivas() {
        return directivas;
    }

    public List<NodoAgente> getAgentes() {
        return agentes;
    }

    public List<NodoReferencia> getEjecuciones() {
        return ejecuciones;
    }

    public List<NodoReferencia> getExportaciones() {
        return exportaciones;
    }

    public void agregarDirectiva(NodoDirectiva nodoDirectiva) {
        this.directivas.add(nodoDirectiva);
    }

    public void agregarAgente(NodoAgente nodoAgente) {
        this.agentes.add(nodoAgente);
    }

    public void agregarEjecucion(NodoReferencia nodoReferencia) {
        this.ejecuciones.add(nodoReferencia);
    }
    public void agregarExportacion(NodoReferencia nodoReferencia) {
        this.exportaciones.add(nodoReferencia);
    }

    private static String nombres(List<NodoReferencia> lista) {
        List<String> nombre = new ArrayList<>();
        for (NodoReferencia r : lista) {
            nombre.add(r.getNombre());
        }
        return String.join(", ", nombre);
    }

    @Override
    public String toStringArbol(String prefijo) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Raiz Programa\n");
        for (NodoDirectiva directiva : directivas){
            stringBuilder.append(directiva.toStringArbol("│   "));
        }
        for (NodoAgente nodoAgente : agentes){
            stringBuilder.append(nodoAgente.toStringArbol("│   "));
        }
        stringBuilder.append("│   ├── Ejecuciones: ").append(nombres(ejecuciones)).append("\n");
        stringBuilder.append("│   └── Exportaciones: ").append(nombres(exportaciones)).append("\n");
        return stringBuilder.toString();
    }
}