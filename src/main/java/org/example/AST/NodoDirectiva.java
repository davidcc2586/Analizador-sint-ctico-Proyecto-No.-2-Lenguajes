package org.example.AST;

public class NodoDirectiva extends NodoAST {
    private String tipoDirectiva;
    private String valor;

    public NodoDirectiva(String tipoDirectiva, String valor, int fila, int columna) {
        super(fila, columna);
        this.tipoDirectiva = tipoDirectiva;
        this.valor = valor;
    }

    public String getTipoDirectiva() {
        return tipoDirectiva;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String toStringArbol(String prefijo) {
        return prefijo + "├── Directiva: " + tipoDirectiva + " = \"" + valor + "\"\n";
    }
}