package org.example.Datos;

public enum TipoToken {
    DIRECTIVA,      // @modelo, @rol, @formato
    AGENTE,
    CONTEXTO,
    VARIABLE,
    EJECUTAR,
    EXPORTAR,
    COMANDO_IA,     // PREGUNTAR, GENERAR, RESUMIR, ANALIZAR, TRADUCIR, CLASIFICAR, EXTRAER
    CARGAR,         // propio terminal, no es COMANDO_IA
    CONECTOR,       // SOBRE, DESDE, EN, COMO
    FLECHA,         // ->  (no es CONECTOR)
    IGUAL,
    MAS,
    LLAVE_A, LLAVE_C,
    PAR_A, PAR_C,
    COMA,
    ID,
    CADENA,
    NUMERO,
    EOF
}