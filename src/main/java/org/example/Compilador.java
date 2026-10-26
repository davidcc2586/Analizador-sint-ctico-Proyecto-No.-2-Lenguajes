package org.example;

import org.example.AST.NodoPrograma;
import org.example.Datos.ErrorCompilacion;
import org.example.Datos.ErrorLexico;
import org.example.Datos.Token;
import org.example.Lexema.Lexer;
import org.example.Reoirtes.GeneradorPromptzal;
import org.example.Semantico.AnalizadorSemantico;
import org.example.Semantico.TablaSimbolos;
import org.example.Sintactico.ParserSintactico;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

//Ejecuta todas las fases: léxico (JFlex), sintáctico, semántico y generación del promptzal.
public class Compilador {
    private List<Token> tokens = new ArrayList<>();
    private List<ErrorCompilacion> errores = new ArrayList<>();
    private NodoPrograma nodoPrograma;
    private TablaSimbolos tabla = new TablaSimbolos();
    private String promptzal = "";

    public void compilar(String codigo) {
        tokens = new ArrayList<>();
        errores = new ArrayList<>();
        tabla = new TablaSimbolos();
        promptzal = "";

        // 1. Léxico
        Lexer lexer = new Lexer(new StringReader(codigo));
        try {
            tokens = lexer.analizarTodo();
        } catch (IOException e) {
            errores.add(new ErrorCompilacion(ErrorCompilacion.TipoError.LEXICO, "", "Error de lectura: " + e.getMessage(), 1, 1));
            return;
        }
        for (ErrorLexico errorLexico : lexer.getErrores()) {
            errores.add(new ErrorCompilacion(ErrorCompilacion.TipoError.LEXICO, errorLexico.getLexema(), errorLexico.getDescripcion(), errorLexico.getFila(), errorLexico.getColumna()));
        }

        // 2. Sintáctico
        ParserSintactico parser = new ParserSintactico(tokens);
        nodoPrograma = parser.parsear();
        errores.addAll(parser.getErrores());

        // 3. Semántico
        AnalizadorSemantico semantico = new AnalizadorSemantico();
        semantico.analizar(nodoPrograma);
        tabla = semantico.getTabla();
        errores.addAll(semantico.getErrores());

        // 4. Promptzal (se genera aunque haya errores; quedan reportados)
        GeneradorPromptzal generadorPromptzal = new GeneradorPromptzal();
        promptzal = generadorPromptzal.generar(nodoPrograma);
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<ErrorCompilacion> getErrores() {
        return errores;
    }

    public NodoPrograma getNodoPrograma() {
        return nodoPrograma;
    }

    public TablaSimbolos getTabla() {
        return tabla;
    }

    public String getPromptzal() {
        return promptzal;
    }
}