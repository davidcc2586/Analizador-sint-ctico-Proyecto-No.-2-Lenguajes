package org.example;

import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error al darle estilo al sistema.");
        }

        InterfazGrafica ventana = new InterfazGrafica();
        ventana.setVisible(true);
    }
}