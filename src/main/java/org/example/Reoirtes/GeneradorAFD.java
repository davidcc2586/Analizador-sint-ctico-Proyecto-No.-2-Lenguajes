package org.example.Reoirtes;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class GeneradorAFD {

    public static String generarCodigoDOT() {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph AFD_PromptZal {\n");
        dot.append("    rankdir=LR;\n");
        dot.append("    node [shape = circle, fontname=\"Arial\"];\n");
        dot.append("    edge [fontname=\"Arial\"];\n\n");

        // Estados de Aceptación (Doble Círculo)
        dot.append("    node [shape = doublecircle];\n");
        dot.append("    S1 [label=\"ID / PR\"];\n");
        dot.append("    S2 [label=\"ENTERO\"];\n");
        dot.append("    S4 [label=\"DECIMAL\"];\n");
        dot.append("    S6 [label=\"CADENA\"];\n");
        dot.append("    S8 [label=\"DIRECTIVA\"];\n");
        dot.append("    S10 [label=\"FLECHA (->)\"];\n");
        dot.append("    S11 [label=\"DELIMITADOR\"];\n\n");

        // Estados No Finales
        dot.append("    node [shape = circle];\n");
        dot.append("    S0 [label=\"S0 (Inicio)\"];\n");
        dot.append("    S3 [label=\"Punto (.)\"];\n");
        dot.append("    S5 [label=\"En Cadena\"];\n");
        dot.append("    S7 [label=\"En Dir (@)\"];\n");
        dot.append("    S9 [label=\"Guion (-)\"];\n\n");

        // Transiciones Formales sin lambda (λ)
        // Identificadores y Palabras Reservadas
        dot.append("    S0 -> S1 [label=\"[a-zA-Z_]\"];\n");
        dot.append("    S1 -> S1 [label=\"[a-zA-Z0-9_]\"];\n\n");

        // Números Enteros y Decimales
        dot.append("    S0 -> S2 [label=\"[0-9]\"];\n");
        dot.append("    S2 -> S2 [label=\"[0-9]\"];\n");
        dot.append("    S2 -> S3 [label=\".\"];\n");
        dot.append("    S3 -> S4 [label=\"[0-9]\"];\n");
        dot.append("    S4 -> S4 [label=\"[0-9]\"];\n\n");

        // Cadenas
        dot.append("    S0 -> S5 [label=\"\\\"\"];\n");
        dot.append("    S5 -> S5 [label=\"[^\\\"\n]\"];\n");
        dot.append("    S5 -> S6 [label=\"\\\"\"];\n\n");

        // Directivas (@modelo, @rol, @formato)
        dot.append("    S0 -> S7 [label=\"@\"];\n");
        dot.append("    S7 -> S8 [label=\"[a-zA-Z]\"];\n");
        dot.append("    S8 -> S8 [label=\"[a-zA-Z]\"];\n\n");

        // Operador Flecha
        dot.append("    S0 -> S9 [label=\"-\"];\n");
        dot.append("    S9 -> S10 [label=\">\"];\n\n");

        // Delimitadores y Simbolos Simples
        dot.append("    S0 -> S11 [label=\"{ }, ( ) = +\"];\n");

        dot.append("}\n");
        return dot.toString();
    }

    public static boolean exportarImagenAFD(String rutaDot, String rutaPng) {
        String codigoDot = generarCodigoDOT();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaDot))) {
            bw.write(codigoDot);
        } catch (IOException e) {
            System.err.println("Error al escribir archivo .dot: " + e.getMessage());
            return false;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", rutaDot, "-o", rutaPng);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("Error al ejecutar Graphviz: " + e.getMessage());
            return false;
        }
    }
}