package org.example.AST;

public class NodoReferencia extends NodoAST {
    // Nombre usado en EJECUTAR o EXPORTAR, con su posición (para reportar errores semánticos).
    private final String nombre;

    public NodoReferencia(String nombre, int fila, int columna) {
        super(fila, columna);
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toStringArbol(String prefijo) {
        return prefijo + "├── Referencia: " + nombre + "\n";
    }
}
 