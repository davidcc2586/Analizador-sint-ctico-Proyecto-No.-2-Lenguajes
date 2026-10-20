package org.example.Semantico;

import org.example.AST.*;
import org.example.Datos.ErrorCompilacion;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

//Recorre el AST, llena la tabla de símbolos y valida el significado del programa.
public class AnalizadorSemantico {
    private final TablaSimbolos tabla = new TablaSimbolos();
    private final List<ErrorCompilacion> errores = new ArrayList<>();

    public TablaSimbolos getTabla() {
        return tabla;
    }

    public List<ErrorCompilacion> getErrores() {
        return errores;
    }

    public void analizar(NodoPrograma programa) {
        // Agentes
        for (NodoAgente agente : programa.getAgentes()) {
            boolean nuevo = tabla.agregarSimbolo(agente.getNombre(), TipoSimbolo.AGENTE, TablaSimbolos.GLOBAL, agente.getFila());
            if (!nuevo) {
                error(agente.getNombre(), "Ya existe un agente con el nombre '" + agente.getNombre() + "'", agente.getFila(), agente.getColumna());
                continue;
            }
            analizarAgente(agente);
        }

        // EJECUTAR solo puede referirse a agentes que existan
        Set<String> ejecutados = new LinkedHashSet<>();
        for (NodoReferencia nodoReferencia : programa.getEjecuciones()) {
            if (tabla.existeAgente(nodoReferencia.getNombre())) {
                ejecutados.add(nodoReferencia.getNombre());
            } else {
                error(nodoReferencia.getNombre(), "EJECUTAR hace referencia al agente '" + nodoReferencia.getNombre() + "', que no existe", nodoReferencia.getFila(), nodoReferencia.getColumna());
            }
        }

        // 3. EXPORTAR solo puede referirse a variables que existan en algún agente ejecutado
        for (NodoReferencia referencia : programa.getExportaciones()) {
            boolean existe = false;
            for (String agente : ejecutados) {
                if (tabla.existe(referencia.getNombre(), agente)) { existe = true; break; }
            }
            if (!existe) {
                error(referencia.getNombre(), "EXPORTAR hace referencia a '" + referencia.getNombre() + "', que no existe en ningún agente ejecutado", referencia.getFila(), referencia.getColumna());
            }
        }
    }

    private void analizarAgente(NodoAgente agente) {
        String ambito = agente.getNombre();

        for (NodoVariable nodoVariable : agente.getVariables()) {
            validarExpresion(nodoVariable.getExpresion(), ambito);
            boolean nueva = tabla.agregarSimbolo(nodoVariable.getNombre(), TipoSimbolo.VARIABLE_DECLARADA, ambito, nodoVariable.getFila());
            if (!nueva) {
                error(nodoVariable.getNombre(), "La variable '" + nodoVariable.getNombre() + "' ya fue declarada en el agente '" + ambito + "'", nodoVariable.getFila(), nodoVariable.getColumna());
            }
        }

        for (NodoComando nodoComando : agente.getComandos()) {
            NodoExpresion arg = nodoComando.getArgumentoPrincipal();
            // Excepción: en GENERAR, un identificador como primer argumento es el tipo de contenido
            boolean tipoDeContenido = nodoComando.getNombreComando().equals("GENERAR") && arg != null && arg.getTipoExpresion().equals("IDENTIFICADOR");
            if (!tipoDeContenido){
                validarExpresion(arg, ambito);
            }

            for (NodoConector nodoConector : nodoComando.getConectores()) {
                validarExpresion(nodoConector.getExpresion(), ambito);
            }

            // El destino de -> declara la variable (si ya existía, solo se reasigna)
            String destino = nodoComando.getVariableDestino();
            if (destino != null && !tabla.existe(destino, ambito)) {
                tabla.agregarSimbolo(destino, TipoSimbolo.RESULTADO_COMANDO, ambito, nodoComando.getFila());
            }
        }
    }

    //Un identificador usado en una expresión debe estar declarado antes, dentro del mismo agente.
    private void validarExpresion(NodoExpresion nodoExpresion, String ambito) {
        if (nodoExpresion == null) return;
        switch (nodoExpresion.getTipoExpresion()) {
            case "IDENTIFICADOR":
                if (!tabla.existe(nodoExpresion.getValor(), ambito)) {
                    error(nodoExpresion.getValor(), "El identificador '" + nodoExpresion.getValor() + "' se usa sin haber sido declarado en el agente '" + ambito + "'", nodoExpresion.getFila(), nodoExpresion.getColumna());
                }
                break;
            case "CONCATENACION":
                for (NodoExpresion nodoExpresion1 : nodoExpresion.getTerminos()) {
                    validarExpresion(nodoExpresion1, ambito);
                }
                break;
            default:
                break;
        }
    }

    private void error(String lexema, String descripcion, int fila, int columna) {
        errores.add(new ErrorCompilacion(ErrorCompilacion.TipoError.SEMANTICO, lexema, descripcion, fila, columna));
    }
}