package org.example.Reoirtes;

import org.example.AST.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeneradorPromptzal {
    // Recorre el AST y produce el prompt final en texto

    public String generar(NodoPrograma programa) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("# Prompt generado con PromptZal\n");

        String modelo = directiva(programa, "@modelo");
        String rol = directiva(programa, "@rol");
        String formato = directiva(programa, "@formato");
        if (modelo != null){
            stringBuilder.append("Modelo destino: ").append(modelo).append("\n");
        }
        if (rol != null){
            stringBuilder.append("Rol general: ").append(rol).append("\n");
        }
        if (formato != null){
            stringBuilder.append("Formato de respuesta: ").append(formato).append("\n");
        }

        Map<String, NodoAgente> agentes = new LinkedHashMap<>();
        for (NodoAgente nodoAgente : programa.getAgentes()){
            agentes.putIfAbsent(nodoAgente.getNombre(), nodoAgente);
        }

        // Solo los agentes que aparecen en EJECUTAR, en ese orden
        for (NodoReferencia nodoReferencia : programa.getEjecuciones()) {
            NodoAgente nodoAgente = agentes.get(nodoReferencia.getNombre());
            if (nodoAgente != null){
                escribirAgente(stringBuilder, nodoAgente);
            }
        }

        stringBuilder.append("\n## Resultado solicitado\n");
        for (NodoReferencia nodoReferencia : programa.getExportaciones()) {
            stringBuilder.append("- ").append(nodoReferencia.getNombre()).append("\n");
        }
        return stringBuilder.toString();
    }

    private String directiva(NodoPrograma nodoPrograma, String nombre) {
        for (NodoDirectiva nodoDirectiva : nodoPrograma.getDirectivas()) {
            if (nodoDirectiva.getTipoDirectiva().equals(nombre)) {
                return nodoDirectiva.getValor();
            }
        }
        return null;
    }

    private void escribirAgente(StringBuilder stringBuilder, NodoAgente nodoAgente) {
        stringBuilder.append("\n## Agente: ").append(nodoAgente.getNombre()).append("\n");
        if (nodoAgente.getContexto() != null){
            stringBuilder.append("Contexto: ").append(nodoAgente.getContexto()).append("\n");
        }

        if (!nodoAgente.getVariables().isEmpty()) {
            stringBuilder.append("Variables disponibles:\n");
            for (NodoVariable nodoVariable : nodoAgente.getVariables()) {
                stringBuilder.append("- ").append(nodoVariable.getNombre()).append(": ").append(origen(nodoVariable.getExpresion())).append("\n");
            }
        }

        if (!nodoAgente.getComandos().isEmpty()) {
            stringBuilder.append("Instrucciones:\n");
            int n = 1;
            for (NodoComando nodoComando : nodoAgente.getComandos()) {
                stringBuilder.append(n++).append(". ").append(instruccion(nodoComando)).append("\n");
            }
        }
    }

    //Origen del valor de una variable: literal o archivo indicado en CARGAR
    private String origen(NodoExpresion nodoExpresion) {
        switch (nodoExpresion.getTipoExpresion()) {
            case "CARGAR_ARCHIVO":
                return "contenido del archivo \"" + nodoExpresion.getValor() + "\" (adjuntar el archivo)";
            case "LITERAL_CADENA":
                return "\"" + nodoExpresion.getValor() + "\"";
            default:
                return describir(nodoExpresion);
        }
    }

    //Texto legible de una expresión
    private String describir(NodoExpresion nodoExpresion) {
        if (nodoExpresion == null){
            return "";
        }
        switch (nodoExpresion.getTipoExpresion()) {
            case "LITERAL_NUMERO":
                return nodoExpresion.getUnidad() != null ? nodoExpresion.getValor() + " " + nodoExpresion.getUnidad() : nodoExpresion.getValor();
            case "CARGAR_ARCHIVO":
                return "el contenido del archivo \"" + nodoExpresion.getValor() + "\"";
            case "CONCATENACION":
                List<NodoExpresion> terminos = nodoExpresion.getTerminos();
                StringBuilder stringBuilder = new StringBuilder();
                for (NodoExpresion parte : terminos) {
                    stringBuilder.append(describir(parte)); // los literales ya traen sus propios espacios
                }
                return stringBuilder.toString();
            default: // LITERAL_CADENA, IDENTIFICADOR
                return nodoExpresion.getValor();
        }
    }

    private String instruccion(NodoComando nodoComando) {
        String argumento = describir(nodoComando.getArgumentoPrincipal());
        String destino = nodoComando.getVariableDestino();
        NodoExpresion sobre = conector(nodoComando, "SOBRE");
        NodoExpresion desde = conector(nodoComando, "DESDE");
        NodoExpresion en = conector(nodoComando, "EN");
        NodoExpresion como = conector(nodoComando, "COMO");

        String base;
        String guardar = "Guarda el resultado como " + destino + ".";

        switch (nodoComando.getNombreComando()) {
            case "PREGUNTAR":
                if (sobre != null) {
                    base = "Basándote en " + describir(sobre) + ", responde: " + argumento;
                } else {
                    base = "Responde: " + argumento;
                }
                guardar = "Guarda la respuesta como " + destino + ".";
                break;
            case "GENERAR":
                if (desde != null) {
                    base = "Genera " + argumento + " a partir de: " + describir(desde);
                } else {
                    base = "Genera " + argumento;
                }
                break;
            case "RESUMIR":
                if (en != null) {
                    base = "Resume " + argumento + " en un máximo de " + cantidad(en);
                } else {
                    base = "Resume " + argumento;
                }
                break;
            case "ANALIZAR":
                if (sobre != null) {
                    base = "Analiza " + argumento + " considerando: " + describir(sobre);
                } else {
                    base = "Analiza " + argumento;
                }
                break;
            case "TRADUCIR":
                if (en != null) {
                    base = "Traduce " + argumento + " al idioma " + describir(en);
                } else {
                    base = "Traduce " + argumento;
                }
                break;
            case "CLASIFICAR":
                if (en != null) {
                    base = "Clasifica " + argumento + " en las categorías: " + describir(en);
                } else {
                    base = "Clasifica " + argumento;
                }
                break;
            case "EXTRAER":
                if (desde != null) {
                    base = "Extrae " + argumento + " a partir de " + describir(desde);
                } else {
                    base = "Extrae " + argumento;
                }
                break;
            default: // comando agregado en clase: instrucción genérica
                StringBuilder stringBuilder = new StringBuilder("Ejecuta " + nodoComando.getNombreComando() + " con: " + argumento);
                for (NodoConector nodoConector : nodoComando.getConectores()) {
                    stringBuilder.append(" ").append(nodoConector.getConector()).append(" ").append(describir(nodoConector.getExpresion()));
                }
                base = stringBuilder.toString();
        }

        String texto = punto(base) + " " + guardar;
        if (como != null) {
            texto += " Entrega el resultado en formato " + describir(como) + ".";
        }
        return texto;
    }

    //"100" -> "100 palabras"; "100 palabras" se deja igual
    private String cantidad(NodoExpresion nodoExpresion) {
        String describir = describir(nodoExpresion);
        if (nodoExpresion.getTipoExpresion().equals("LITERAL_NUMERO") && nodoExpresion.getUnidad() == null){
            describir += " palabras";
        }
        return describir;
    }

    private NodoExpresion conector(NodoComando nodoComando, String nombre) {
        for (NodoConector nodoConector : nodoComando.getConectores()) {
            if (nodoConector.getConector().equals(nombre)) return nodoConector.getExpresion();
        }
        return null;
    }

    private String punto(String string) {
        if (string.endsWith(".") || string.endsWith("?") || string.endsWith("!")) {
            return string;
        } else {
            return string + ".";
        }
    }
}