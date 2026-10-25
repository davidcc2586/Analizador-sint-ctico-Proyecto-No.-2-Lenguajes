package org.example.Sintactico;

import org.example.AST.*;
import org.example.Datos.ErrorCompilacion;
import org.example.Datos.TipoToken;
import org.example.Datos.Token;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Parser descendente recursivo escrito a mano.
 * Cada no terminal de la gramática tiene su método (el BNF va sobre cada método).
 * Recuperación de errores en modo pánico: se descartan tokens hasta uno de sincronización.
 */
public class ParserSintactico {

    private static final Set<TipoToken> SINCRONIZACION = EnumSet.of(
            TipoToken.AGENTE, TipoToken.LLAVE_C, TipoToken.COMANDO_IA,
            TipoToken.VARIABLE, TipoToken.EJECUTAR, TipoToken.EXPORTAR, TipoToken.EOF);

    private final List<Token> tokens;
    private int pos = 0;
    private Token actual;
    private final List<ErrorCompilacion> errores = new ArrayList<>();
    private int ultimaFilaError = -1, ultimaColError = -1;

    public ParserSintactico(List<Token> tokensEntrada) {
        this.tokens = new ArrayList<>(tokensEntrada);
        if (tokens.isEmpty() || tokens.get(tokens.size() - 1).getTipo() != TipoToken.EOF) {
            int fila = tokens.isEmpty() ? 1 : tokens.get(tokens.size() - 1).getFila();
            tokens.add(new Token(TipoToken.EOF, "EOF", fila, 1));
        }
        this.actual = tokens.get(0);
    }

    public List<ErrorCompilacion> getErrores() { return errores; }

    //Punto de entrada: analiza todo el programa y devuelve el AST.
    public NodoPrograma parsear() {
        return programa();
    }

    // Utilidades
    private boolean es(TipoToken tipo) { return actual.getTipo() == tipo; }

    private void avanzar() {
        if (actual.getTipo() != TipoToken.EOF) {
            pos++;
            actual = tokens.get(pos);
        }
    }

    //Si el token actual es del tipo esperado lo consume y lo devuelve; si no, reporta error y devuelve null
    private Token consumir(TipoToken tipo, String esperado) {
        if (es(tipo)) {
            Token t = actual;
            avanzar();
            return t;
        }
        error(esperado);
        return null;
    }

    private void error(String esperado) {
        // Evita reportar dos errores distintos en la misma posición
        if (actual.getFila() == ultimaFilaError && actual.getColumna() == ultimaColError) return;
        ultimaFilaError = actual.getFila();
        ultimaColError = actual.getColumna();
        errores.add(new ErrorCompilacion(
                ErrorCompilacion.TipoError.SINTACTICO,
                actual.getLexema(),
                "Se esperaba " + esperado + " pero se encontró '" + actual.getLexema() + "' (" + actual.getTipo() + ")",
                actual.getFila(), actual.getColumna()));
    }

    // Modo pánico: descarta tokens hasta encontrar uno de sincronización (EOF siempre lo es)
    private void sincronizar() {
        while (!SINCRONIZACION.contains(actual.getTipo())) {
            avanzar();
        }
    }

    private static String sinComillas(String s) {
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    // =====================================================================
    // <programa> -> <directivas> <agentes> <ejecuciones> <exportar> EOF
    // =====================================================================
    private NodoPrograma programa() {
        NodoPrograma prog = new NodoPrograma(actual.getFila(), actual.getColumna());
        directivas(prog);
        agentes(prog);
        ejecuciones(prog);
        exportar(prog);
        // EOF: todo lo que sobre es un error (por ejemplo un segundo EXPORTAR)
        while (!es(TipoToken.EOF)) {
            error("el fin del archivo (solo se permite un EXPORTAR y debe ir al final)");
            avanzar();
            sincronizar();
        }
        return prog;
    }

    // <directivas> -> DIRECTIVA CADENA <directivas> | ε
    private void directivas(NodoPrograma prog) {
        while (es(TipoToken.DIRECTIVA)) {
            Token dir = actual;
            avanzar();
            Token valor = consumir(TipoToken.CADENA, "CADENA (valor de la directiva)");
            if (valor == null) {
                sincronizar();
                continue;
            }
            prog.agregarDirectiva(new NodoDirectiva(dir.getLexema(), sinComillas(valor.getLexema()),
                    dir.getFila(), dir.getColumna()));
        }
    }

    // <agentes> -> <agente> <mas_agentes>
    private void agentes(NodoPrograma prog) {
        if (es(TipoToken.AGENTE)) {
            NodoAgente a = agente();
            if (a != null) prog.agregarAgente(a);
        } else {
            error("AGENTE (el programa requiere al menos un agente)");
            sincronizar();
        }
        masAgentes(prog);
    }

    // <mas_agentes> -> <agente> <mas_agentes> | ε
    private void masAgentes(NodoPrograma prog) {
        while (es(TipoToken.AGENTE)) {
            NodoAgente a = agente();
            if (a != null) prog.agregarAgente(a);
        }
    }

    // <agente> -> AGENTE ID LLAVE_A <contexto> <variables> <comandos> LLAVE_C
    private NodoAgente agente() {
        Token kw = actual;
        avanzar(); // AGENTE
        Token id = consumir(TipoToken.ID, "ID (nombre del agente)");
        NodoAgente agente = new NodoAgente(id != null ? id.getLexema() : "?", kw.getFila(), kw.getColumna());
        consumir(TipoToken.LLAVE_A, "'{'");
        contexto(agente);
        variables(agente);
        comandos(agente);
        if (consumir(TipoToken.LLAVE_C, "'}'") == null) {
            sincronizar();
        }
        return id == null ? null : agente;
    }

    // <contexto> -> CONTEXTO IGUAL CADENA
    private void contexto(NodoAgente agente) {
        if (!es(TipoToken.CONTEXTO)) {
            error("CONTEXTO (el agente debe iniciar con su contexto)");
            sincronizar();
            return;
        }
        avanzar();
        if (consumir(TipoToken.IGUAL, "'='") == null) { sincronizar(); return; }
        Token c = consumir(TipoToken.CADENA, "CADENA (texto del contexto)");
        if (c == null) { sincronizar(); return; }
        agente.setContexto(sinComillas(c.getLexema()));
    }

    // <variables> -> VARIABLE ID IGUAL <expresion> <variables> | ε
    private void variables(NodoAgente agente) {
        while (es(TipoToken.VARIABLE)) {
            Token kw = actual;
            avanzar();
            Token id = consumir(TipoToken.ID, "ID (nombre de la variable)");
            if (id == null) { sincronizar(); continue; }
            if (consumir(TipoToken.IGUAL, "'='") == null) { sincronizar(); continue; }
            NodoExpresion e = expresion();
            if (e == null) { sincronizar(); continue; }
            agente.agregarVariable(new NodoVariable(id.getLexema(), e, kw.getFila(), kw.getColumna()));
        }
    }

    // <comandos> -> <comando> <mas_comandos>
    private void comandos(NodoAgente agente) {
        if (es(TipoToken.COMANDO_IA)) {
            NodoComando c = comando();
            if (c != null) agente.agregarComando(c);
        } else {
            error("COMANDO_IA (el agente requiere al menos un comando)");
            sincronizar();
        }
        masComandos(agente);
    }

    // <mas_comandos> -> <comando> <mas_comandos> | ε
    private void masComandos(NodoAgente agente) {
        while (es(TipoToken.COMANDO_IA)) {
            NodoComando c = comando();
            if (c != null) agente.agregarComando(c);
        }
    }

    // <comando> -> COMANDO_IA <expresion> <conectores> FLECHA ID
    private NodoComando comando() {
        Token cmd = actual;
        avanzar(); // COMANDO_IA
        NodoExpresion arg = expresion();
        if (arg == null) { sincronizar(); return null; }

        NodoComando nodo = new NodoComando(cmd.getLexema(), arg, null, cmd.getFila(), cmd.getColumna());
        if (!conectores(nodo)) return null;

        if (consumir(TipoToken.FLECHA, "'->'") == null) { sincronizar(); return null; }
        Token destino = consumir(TipoToken.ID, "ID (variable donde se guarda el resultado)");
        if (destino == null) { sincronizar(); return null; }

        nodo.setVariableDestino(destino.getLexema());
        return nodo;
    }

    // <conectores> -> CONECTOR <expresion> <conectores> | ε     (devuelve false si hubo error)
    private boolean conectores(NodoComando comando) {
        while (es(TipoToken.CONECTOR)) {
            Token con = actual;
            avanzar();
            NodoExpresion e = expresion();
            if (e == null) { sincronizar(); return false; }
            comando.agregarConector(new NodoConector(con.getLexema(), e, con.getFila(), con.getColumna()));
        }
        return true;
    }

    // <expresion> -> <termino> <mas_terminos>
    private NodoExpresion expresion() {
        NodoExpresion primero = termino();
        if (primero == null) return null;
        return masTerminos(primero);
    }

    // <mas_terminos> -> MAS <termino> <mas_terminos> | ε
    private NodoExpresion masTerminos(NodoExpresion izquierda) {
        NodoExpresion resultado = izquierda;
        while (es(TipoToken.MAS)) {
            avanzar();
            NodoExpresion derecha = termino();
            if (derecha == null) return null;
            NodoExpresion concat = new NodoExpresion("+", "CONCATENACION", izquierda.getFila(), izquierda.getColumna());
            concat.agregarTermino(resultado);
            concat.agregarTermino(derecha);
            resultado = concat;
        }
        return resultado;
    }

    // <termino> -> CADENA | ID | NUMERO <unidad> | <cargar>
    private NodoExpresion termino() {
        Token t = actual;
        switch (actual.getTipo()) {
            case CADENA:
                avanzar();
                return new NodoExpresion(sinComillas(t.getLexema()), "LITERAL_CADENA", t.getFila(), t.getColumna());
            case ID:
                avanzar();
                return new NodoExpresion(t.getLexema(), "IDENTIFICADOR", t.getFila(), t.getColumna());
            case NUMERO:
                avanzar();
                NodoExpresion num = new NodoExpresion(t.getLexema(), "LITERAL_NUMERO", t.getFila(), t.getColumna());
                num.setUnidad(unidad());
                return num;
            case CARGAR:
                return cargar();
            default:
                error("una expresión (CADENA, ID, NUMERO o CARGAR)");
                return null;
        }
    }

    // <unidad> -> ID | ε
    private String unidad() {
        if (es(TipoToken.ID)) {
            String u = actual.getLexema();
            avanzar();
            return u;
        }
        return null;
    }

    // <cargar> -> CARGAR PAR_A CADENA PAR_C
    private NodoExpresion cargar() {
        Token kw = actual;
        avanzar(); // CARGAR
        if (consumir(TipoToken.PAR_A, "'('") == null) return null;
        Token ruta = consumir(TipoToken.CADENA, "CADENA (ruta del archivo)");
        if (ruta == null) return null;
        if (consumir(TipoToken.PAR_C, "')'") == null) return null;
        return new NodoExpresion(sinComillas(ruta.getLexema()), "CARGAR_ARCHIVO", kw.getFila(), kw.getColumna());
    }

    // <ejecuciones> -> EJECUTAR ID <mas_ejecuciones>
    private void ejecuciones(NodoPrograma prog) {
        if (es(TipoToken.EJECUTAR)) {
            ejecucion(prog);
        } else {
            error("EJECUTAR (se requiere al menos una ejecución)");
            sincronizar();
        }
        masEjecuciones(prog);
    }

    // <mas_ejecuciones> -> EJECUTAR ID <mas_ejecuciones> | ε
    private void masEjecuciones(NodoPrograma prog) {
        while (es(TipoToken.EJECUTAR)) {
            ejecucion(prog);
        }
    }

    private void ejecucion(NodoPrograma prog) {
        avanzar(); // EJECUTAR
        Token id = consumir(TipoToken.ID, "ID (nombre del agente a ejecutar)");
        if (id == null) { sincronizar(); return; }
        prog.agregarEjecucion(new NodoReferencia(id.getLexema(), id.getFila(), id.getColumna()));
    }

    // <exportar> -> EXPORTAR ID <mas_ids>
    private void exportar(NodoPrograma prog) {
        if (!es(TipoToken.EXPORTAR)) {
            error("EXPORTAR (el programa requiere exactamente un EXPORTAR)");
            sincronizar();
            if (!es(TipoToken.EXPORTAR)) return;
        }
        avanzar(); // EXPORTAR
        Token id = consumir(TipoToken.ID, "ID (resultado a exportar)");
        if (id == null) { sincronizar(); return; }
        prog.agregarExportacion(new NodoReferencia(id.getLexema(), id.getFila(), id.getColumna()));
        masIds(prog);
    }

    // <mas_ids> -> COMA ID <mas_ids> | ε
    private void masIds(NodoPrograma prog) {
        while (es(TipoToken.COMA)) {
            avanzar();
            Token id = consumir(TipoToken.ID, "ID (resultado a exportar)");
            if (id == null) { sincronizar(); return; }
            prog.agregarExportacion(new NodoReferencia(id.getLexema(), id.getFila(), id.getColumna()));
        }
    }
}