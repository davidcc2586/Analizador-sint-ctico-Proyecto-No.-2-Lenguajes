package org.example.Lexema;

import org.example.Datos.ErrorLexico;
import org.example.Datos.TipoToken;
import org.example.Datos.Token;

import java.util.ArrayList;
import java.util.List;

%%

%public
%class Lexer
%unicode
%line
%column
%type Token

%{
    private int contador = 1;
    private final List<ErrorLexico> errores = new ArrayList<>();

    public List<ErrorLexico> getErrores() {
        return errores;
    }

    /** Crea un token con fila y columna empezando en 1. */
    private Token token(TipoToken tipo) {
        return new Token(contador++, yytext(), tipo, yyline + 1, yycolumn + 1);
    }

    /** Registra un error léxico y el análisis continúa. */
    private void error(String descripcion) {
        errores.add(new ErrorLexico(yytext(), descripcion, yyline + 1, yycolumn + 1));
    }

    /** Lee todo el texto y devuelve la lista de tokens (el último es EOF). */
    public List<Token> analizarTodo() throws java.io.IOException {
        List<Token> lista = new ArrayList<>();
        Token t;
        do {
            t = yylex();
            lista.add(t);
        } while (t.getTipo() != TipoToken.EOF);
        return lista;
    }
%}

%eofval{
    return new Token(contador++, "EOF", TipoToken.EOF, yyline + 1, yycolumn + 1);
%eofval}

Letra            = [a-zA-Z]
Digito           = [0-9]
Espacio          = [ \t\r\n\f]
ComentarioLinea  = "//" [^\r\n]*
ComentarioBloque = "/*" ~"*/"
Id               = ({Letra}|_)({Letra}|{Digito}|_)*
Cadena           = \" [^\"\r\n]* \"
Numero           = {Digito}+ ("." {Digito}+)?

%%

// ---------- Se descartan: nunca llegan al parser ----------
{Espacio}+          { /* ignorar */ }
{ComentarioLinea}   { /* ignorar */ }
{ComentarioBloque}  { /* ignorar */ }
"/*"                { error("Comentario de bloque sin cerrar"); }

// ---------- Directivas ----------
"@modelo" | "@rol" | "@formato"   { return token(TipoToken.DIRECTIVA); }
"@" {Letra}*                      { error("Directiva desconocida o inválida"); }

// ---------- Palabras reservadas de estructura ----------
"AGENTE"            { return token(TipoToken.AGENTE); }
"contexto"          { return token(TipoToken.CONTEXTO); }
"variable"          { return token(TipoToken.VARIABLE); }
"EJECUTAR"          { return token(TipoToken.EJECUTAR); }
"EXPORTAR"          { return token(TipoToken.EXPORTAR); }

// ---------- Comandos de IA (para agregar uno nuevo, ej. CODIFICAR, solo añadirlo aquí) ----------
"PREGUNTAR" | "GENERAR" | "RESUMIR" | "ANALIZAR" | "TRADUCIR" | "CLASIFICAR" | "EXTRAER"
                    { return token(TipoToken.COMANDO_IA); }

// ---------- Función del sistema (terminal propio) ----------
"CARGAR"            { return token(TipoToken.CARGAR); }

// ---------- Conectores ----------
"SOBRE" | "DESDE" | "EN" | "COMO"   { return token(TipoToken.CONECTOR); }

// ---------- Símbolos ----------
"->"                { return token(TipoToken.FLECHA); }
"="                 { return token(TipoToken.IGUAL); }
"+"                 { return token(TipoToken.MAS); }
"{"                 { return token(TipoToken.LLAVE_A); }
"}"                 { return token(TipoToken.LLAVE_C); }
"("                 { return token(TipoToken.PAR_A); }
")"                 { return token(TipoToken.PAR_C); }
","                 { return token(TipoToken.COMA); }

// ---------- Literales e identificadores ----------
{Cadena}            { return token(TipoToken.CADENA); }
\" [^\"\r\n]*       { error("Cadena sin cerrar"); }
{Numero}            { return token(TipoToken.NUMERO); }
{Id}                { return token(TipoToken.ID); }

// ---------- Cualquier otro carácter ----------
[^]                 { error("Carácter no reconocido"); }