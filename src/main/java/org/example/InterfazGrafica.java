package org.example;

import org.example.Datos.ErrorCompilacion;
import org.example.Datos.Token;
import org.example.Reoirtes.GeneradorAFD;
import org.example.Reoirtes.GeneradorReportes;
import org.example.Semantico.Simbolo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Consumer;

public class InterfazGrafica extends JFrame {

    private static final int TAB_TOKENS = 0, TAB_ERRORES = 1, TAB_SIMBOLOS = 2, TAB_AST = 3, TAB_PROMPT = 4;

    private final Compilador compilador = new Compilador();
    private boolean compilado = false;
    private int totalLineas = 0;
    private File archivoActual = null;

    private JTextArea editor;
    private JTextArea areaAst;
    private JTextArea areaPromptzal;
    private DefaultTableModel modeloTokens, modeloErrores, modeloSimbolos;
    private JTabbedPane pestanas;

    public InterfazGrafica() {
        setTitle("IDE PromptZal");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponentes();
    }

    // Construcción de la ventana
    private void initComponentes() {
        setJMenuBar(crearMenu());
        add(crearToolBar(), BorderLayout.NORTH);

        // Editor con numeración de líneas
        editor = new JTextArea();
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        editor.setTabSize(4);
        editor.setCaretColor(Color.WHITE);
        editor.setMargin(new Insets(4, 8, 4, 8));
        editor.addCaretListener(e -> actualizarPosicion());

        JScrollPane scrollEditor = new JScrollPane(editor);
        scrollEditor.setBorder(BorderFactory.createTitledBorder("Editor PromptZal"));

        // Paneles de resultados
        pestanas = new JTabbedPane();
        modeloTokens = crearModelo("#", "Lexema", "Tipo Token", "Fila", "Columna");
        modeloErrores = crearModelo("#", "Tipo", "Token / Lexema", "Descripción", "Fila", "Columna");
        modeloSimbolos = crearModelo("#", "Identificador", "Tipo", "Ámbito", "Línea");
        areaAst = crearAreaSalida();
        areaPromptzal = crearAreaSalida();

        pestanas.addTab("Tokens", new JScrollPane(new JTable(modeloTokens)));
        pestanas.addTab("Errores", new JScrollPane(configurarTablaErrores(new JTable(modeloErrores))));
        pestanas.addTab("Tabla de símbolos", new JScrollPane(new JTable(modeloSimbolos)));
        pestanas.addTab("AST", new JScrollPane(areaAst));
        pestanas.addTab("Promptzal", new JScrollPane(areaPromptzal));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollEditor, pestanas);
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);

        // Barra de estado
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEtchedBorder());
        add(barra, BorderLayout.SOUTH);
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        archivo.add(item("Abrir (.pz)", e -> abrirArchivo()));
        archivo.add(item("Guardar (.pz)", e -> guardarArchivo(false)));
        archivo.add(item("Guardar como...",  e -> guardarArchivo(true)));
        archivo.addSeparator();
        archivo.add(item("Guardar promptzal (.md / .txt)",  e -> guardarPromptzal()));
        barra.add(archivo);

        JMenu compilar = new JMenu("Compilar");
        JMenuItem itemCompilar = new JMenuItem("Compilar");
        itemCompilar.addActionListener(e -> compilar());
        compilar.add(itemCompilar);
        barra.add(compilar);

        JMenu reportes = new JMenu("Reportes");
        GeneradorReportes generadorReportes = new GeneradorReportes();
        reportes.add(item("Reporte de errores (HTML)", e -> exportar("reporte_errores", r -> generadorReportes.generarReporteErroresHTML(compilador.getErrores(), r))));
        reportes.add(item("Tabla de símbolos (HTML)",e -> exportar("reporte_simbolos", r -> generadorReportes.generarReporteTablaSimbolosHTML(compilador.getTabla().getSimbolos(), r))));
        reportes.add(item("AST (HTML)",e -> exportar("reporte_ast", r -> generadorReportes.generarReporteASTHTML(compilador.getNodoPrograma(), r))));
        reportes.add(item("Tokens (HTML)",e -> exportar("reporte_tokens", r -> generadorReportes.generarReporteTokensHTML(compilador.getTokens(), r))));
        reportes.add(item("Estadísticas (HTML)",e -> exportar("reporte_estadisticas", r -> generadorReportes.generarReporteEstadisticasHTML(compilador.getTokens(), compilador.getErrores(), totalLineas, r))));
        reportes.addSeparator();
        reportes.add(item("Exportar todos",e -> exportarTodos()));
        barra.add(reportes);

        JMenu herramientas = new JMenu("Herramientas");
        herramientas.add(item("Ver AFD (Proyecto 1)", e -> generarYMostrarAFD()));
        barra.add(herramientas);

        return barra;
    }

    private JToolBar crearToolBar() {
        JToolBar jToolBar = new JToolBar();
        jToolBar.setFloatable(false);

        JButton btnAbrir = new JButton("Abrir");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCompilar = new JButton("Compilar");
        JButton btnPrompt = new JButton("Guardar promptzal");

        btnAbrir.addActionListener(e -> abrirArchivo());
        btnGuardar.addActionListener(e -> guardarArchivo(false));
        btnCompilar.addActionListener(e -> compilar());
        btnPrompt.addActionListener(e -> guardarPromptzal());

        jToolBar.add(btnAbrir);
        jToolBar.addSeparator();
        jToolBar.add(btnGuardar);
        jToolBar.addSeparator();
        jToolBar.add(btnCompilar);
        jToolBar.addSeparator();
        jToolBar.add(btnPrompt);
        return jToolBar;
    }

    private JMenuItem item(String texto, java.awt.event.ActionListener accion) {
        JMenuItem it = new JMenuItem(texto);
        it.addActionListener(accion);
        return it;
    }

    private DefaultTableModel crearModelo(Object... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }

    private JTable configurarTablaErrores(JTable t) {
        t.getColumnModel().getColumn(3).setPreferredWidth(450);
        return t;
    }

    private JTextArea crearAreaSalida() {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        a.setMargin(new Insets(8, 10, 8, 10));
        return a;
    }

    private void actualizarPosicion() {
        try {
            int pos = editor.getCaretPosition();
            int linea = editor.getLineOfOffset(pos);
            int col = pos - editor.getLineStartOffset(linea);
        } catch (Exception ignorada) {
            // posición no disponible
        }
    }

    // Archivos
    private void abrirArchivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos PromptZal (.pz)", "pz"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        try {
            editor.setText(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            editor.setCaretPosition(0);
            archivoActual = file;
            setTitle("IDE PromptZal - " + file.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al abrir el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarArchivo(boolean comoNuevo) {
        File destino = archivoActual;
        if (destino == null || comoNuevo) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Archivos PromptZal (.pz)", "pz"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            destino = chooser.getSelectedFile();
            if (!destino.getName().endsWith(".pz")) destino = new File(destino.getAbsolutePath() + ".pz");
        }
        try {
            Files.write(destino.toPath(), editor.getText().getBytes(StandardCharsets.UTF_8));
            archivoActual = destino;
            setTitle("IDE PromptZal - " + destino.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarPromptzal() {
        if (!compilado || compilador.getPromptzal().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero compile un programa para generar el promptzal.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(".md", "md"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(".txt", "txt"));
        chooser.setSelectedFile(new File("promptzal"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION){
            return;
        }

        File destino = chooser.getSelectedFile();
        String nombre = destino.getName();
        if (!nombre.endsWith(".md") && !nombre.endsWith(".txt")) {
            String ext = ((FileNameExtensionFilter) chooser.getFileFilter()).getExtensions()[0];
            destino = new File(destino.getAbsolutePath() + "." + ext);
        }
        try {
            Files.write(destino.toPath(), compilador.getPromptzal().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar el promptzal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Compilación
    private void compilar() {
        String texto = editor.getText();
        if (texto.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El editor está vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        compilador.compilar(texto);
        compilado = true;
        totalLineas = texto.split("\r\n|\r|\n").length;

        // Tokens
        modeloTokens.setRowCount(0);
        for (Token token : compilador.getTokens()) {
            modeloTokens.addRow(new Object[]{token.getId(), token.getLexema(), token.getTipo(), token.getFila(), token.getColumna()});
        }

        // Errores (léxicos, sintácticos y semánticos)
        modeloErrores.setRowCount(0);
        int lexico = 0;
        int sintactico = 0;
        int semantico = 0;
        int n = 1;
        for (ErrorCompilacion errorCompilacion : compilador.getErrores()) {
            switch (errorCompilacion.getTipo()) {
                case LEXICO: lexico++; break;
                case SINTACTICO: sintactico++; break;
                case SEMANTICO: semantico++; break;
            }
            modeloErrores.addRow(new Object[]{n++, errorCompilacion.getTipo(), errorCompilacion.getLexema(), errorCompilacion.getDescripcion(), errorCompilacion.getFila(), errorCompilacion.getColumna()});
        }

        // Tabla de símbolos
        modeloSimbolos.setRowCount(0);
        int index = 1;
        for (Simbolo sim : compilador.getTabla().getSimbolos()) {
            modeloSimbolos.addRow(new Object[]{index++, sim.getNombre(), sim.getTipo(), sim.getAmbito(), sim.getLinea()});
        }

        // AST y promptzal
        areaAst.setText(compilador.getNodoPrograma() != null ? compilador.getNodoPrograma().toStringArbol("") : "");
        areaAst.setCaretPosition(0);
        areaPromptzal.setText(compilador.getPromptzal());
        areaPromptzal.setCaretPosition(0);

        int total = compilador.getErrores().size();
        pestanas.setTitleAt(TAB_ERRORES, "Errores (" + total + ")");
        pestanas.setSelectedIndex(total == 0 ? TAB_PROMPT : TAB_ERRORES);
    }

    // Reportes
    private boolean hayCompilacion() {
        if (!compilado) {
            JOptionPane.showMessageDialog(this, "Primero debe compilar un programa.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
        return compilado;
    }

    private String elegirRutaHtml(String sugerido) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(sugerido + ".html"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return null;
        String ruta = chooser.getSelectedFile().getAbsolutePath();
        return ruta.endsWith(".html") ? ruta : ruta + ".html";
    }

    private void exportar(String sugerido, Consumer<String> generador) {
        if (!hayCompilacion()) return;
        String ruta = elegirRutaHtml(sugerido);
        if (ruta == null) return;
        generador.accept(ruta);
        JOptionPane.showMessageDialog(this, "Reporte generado:\n" + ruta, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportarTodos() {
        if (!hayCompilacion()) return;
        String ruta = elegirRutaHtml("reporte_promptzal");
        if (ruta == null) return;
        String base = ruta.substring(0, ruta.length() - 5);
        GeneradorReportes generadorReportes = new GeneradorReportes();
        generadorReportes.generarReporteErroresHTML(compilador.getErrores(), base + "_errores.html");
        generadorReportes.generarReporteTablaSimbolosHTML(compilador.getTabla().getSimbolos(), base + "_simbolos.html");
        generadorReportes.generarReporteASTHTML(compilador.getNodoPrograma(), base + "_ast.html");
        generadorReportes.generarReporteTokensHTML(compilador.getTokens(), base + "_tokens.html");
        generadorReportes.generarReporteEstadisticasHTML(compilador.getTokens(), compilador.getErrores(), totalLineas, base + "_estadisticas.html");
        JOptionPane.showMessageDialog(this, "Se generaron los 5 reportes HTML.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void generarYMostrarAFD() {
        String rutaDot = "afd.dot";
        String rutaPng = "afd.png";
        if (GeneradorAFD.exportarImagenAFD(rutaDot, rutaPng)) {
            JFrame frame = new JFrame("Grafo del AFD - PromptZal");
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(this);
            frame.add(new JScrollPane(new JLabel(new ImageIcon(rutaPng))));
            frame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo generar la imagen con Graphviz.\nAsegúrese de tener instalado 'dot' en el PATH del sistema.", "Error Graphviz", JOptionPane.ERROR_MESSAGE);
        }
    }
}