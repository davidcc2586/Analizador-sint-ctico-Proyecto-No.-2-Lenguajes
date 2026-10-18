package org.example.Reoirtes;

import org.example.Datos.ErrorCompilacion;
import org.example.Datos.TipoToken;
import org.example.Datos.Token;

import java.io.*;
import java.util.List;

public class GeneradorReportes {

    public void generarReporteTokensHTML(List<Token> tokens, String rutaSalida) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"es\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>Reporte de Tokens - PromptZal</title>\n");
        html.append("<style>\n");
        html.append("  :root { --primary: #1e40af; --bg: #f8fafc; --card-bg: #ffffff; --text: #0f172a; --border: #e2e8f0; }\n");
        html.append("  body { font-family: 'Inter', system-ui, -apple-system, sans-serif; background-color: var(--bg); color: var(--text); margin: 0; padding: 40px 20px; }\n");
        html.append("  .container { max-width: 1100px; margin: 0 auto; background: var(--card-bg); padding: 32px; border-radius: 16px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.05); border: 1px solid var(--border); }\n");
        html.append("  .header { border-bottom: 2px solid var(--border); padding-bottom: 16px; margin-bottom: 24px; display: flex; justify-content: space-between; align-items: center; }\n");
        html.append("  h1 { color: var(--primary); font-size: 1.75rem; margin: 0; font-weight: 700; display: flex; align-items: center; gap: 10px; }\n");
        html.append("  .badge-total { background: #eff6ff; color: #1e40af; font-size: 0.875rem; font-weight: 600; padding: 6px 12px; border-radius: 9999px; border: 1px solid #bfdbfe; }\n");
        html.append("  table { width: 100%; border-collapse: separate; border-spacing: 0; border-radius: 12px; overflow: hidden; border: 1px solid var(--border); }\n");
        html.append("  th, td { padding: 14px 18px; text-align: left; text-size-adjust: 100%; }\n");
        html.append("  th { background-color: #f1f5f9; color: #475569; font-weight: 600; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid var(--border); }\n");
        html.append("  tr:not(:last-child) td { border-bottom: 1px solid var(--border); }\n");
        html.append("  tr:hover { background-color: #f8fafc; }\n");
        html.append("  .code { font-family: 'Fira Code', 'Consolas', monospace; font-size: 0.9rem; background: #f1f5f9; padding: 3px 8px; border-radius: 6px; color: #0f172a; border: 1px solid #e2e8f0; display: inline-block; }\n");
        html.append("  .token-badge { font-weight: 600; font-size: 0.75rem; padding: 4px 10px; border-radius: 6px; text-transform: uppercase; letter-spacing: 0.03em; display: inline-block; }\n");
        html.append("  /* Badges por tipo */\n");
        html.append(" .badge-directiva  { background: #fae8ff; color: #86198f; } /* Purpura */ \n");
        html.append(" .badge-keyword    { background: #e0e7ff; color: #3730a3; } /* Azul añil */ \n");
        html.append(" .badge-comando    { background: #fce7f3; color: #9d174d; } /* Rosa / Fucsia */ \n");
        html.append(" .badge-conector   { background: #e0f2fe; color: #0369a1; } /* Celeste */ \n");
        html.append(" .badge-id         { background: #dcfce7; color: #166534; } /* Verde */ \n");
        html.append(" .badge-string     { background: #ffedd5; color: #9a3412; } /* Naranja */ \n");
        html.append(" .badge-number     { background: #fef3c7; color: #92400e; } /* Ámbar / Amarillo */ \n");
        html.append(" .badge-operator   { background: #f3e8ff; color: #6b21a8; } /* Violeta */ \n");
        html.append(" .badge-delimitador{ background: #f1f5f9; color: #334155; } /* Gris oscuro */ \n");
        html.append(" .badge-default    { background: #f1f5f9; color: #64748b; } /* Gris neutro */ \n");
        html.append("</style>\n</head>\n<body>\n");

        html.append("<div class=\"container\">\n");
        html.append("  <div class=\"header\">\n");
        html.append("    <h1>⚡ Reporte de Tokens Reconocidos</h1>\n");
        html.append("    <span class=\"badge-total\">Total: ").append(tokens.size()).append(" tokens</span>\n");
        html.append("  </div>\n");

        html.append("  <table>\n");
        html.append("    <thead>\n<tr><th>#</th><th>Lexema</th><th>Tipo de Token</th><th>Fila</th><th>Columna</th></tr>\n</thead>\n");
        html.append("    <tbody>\n");

        for (Token t : tokens) {
            String tokenTipo = String.valueOf(t.getTipo());
            String claseBadge = obtenerClaseCssPorTipo(t.getTipo());

            html.append("<tr>")
                    .append("<td>").append(t.getId()).append("</td>")
                    .append("<td><span class=\"code\">").append(escapeHtml(t.getLexema())).append("</span></td>")
                    .append("<td><span class=\"token-badge ").append(claseBadge).append("\">").append(tokenTipo).append("</span></td>")
                    .append("<td>").append(t.getFila()).append("</td>")
                    .append("<td>").append(t.getColumna()).append("</td>")
                    .append("</tr>\n");
        }

        html.append("    </tbody>\n  </table>\n");
        html.append("</div>\n</body>\n</html>");
        escribirArchivo(rutaSalida, html.toString());
    }

    private String obtenerClaseCssPorTipo(TipoToken tipo) {
        if (tipo == null) return "badge-default";

        switch (tipo) {
            case DIRECTIVA:
                return "badge-directiva";

            case AGENTE:
            case CONTEXTO:
            case VARIABLE:
            case EJECUTAR:
            case EXPORTAR:
                return "badge-keyword";

            case COMANDO_IA:
                return "badge-comando";

            case CONECTOR:
            case FLECHA:
                return "badge-conector";

            case ID:
            case CARGAR:
                return "badge-id";

            case CADENA:
                return "badge-string";
            case NUMERO:
                return "badge-number";

            case IGUAL:
            case MAS:
                return "badge-operator";

            case LLAVE_A:
            case LLAVE_C:
            case PAR_A:
            case PAR_C:
            case COMA:
                return "badge-delimitador";

            case EOF:
            default:
                return "badge-default";
        }
    }

    private String escapeHtml(String text) {
        if (text == null){
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void escribirArchivo(String ruta, String contenido) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta))) {
            bw.write(contenido);
            System.out.println("Reporte generado exitosamente en: " + ruta);
        } catch (IOException e) {
            System.err.println("Error al guardar el reporte: " + e.getMessage());
        }
    }

    public void generarReporteEstadisticasHTML(List<Token> tokens, List<ErrorCompilacion> errores, int totalLineas, String rutaSalida) {
        // Arreglo de contadores del mismo tamaño que el Enum
        TipoToken[] tipos = TipoToken.values();
        int[] conteo = new int[tipos.length];

        // Incrementar el contador según el ordinal de cada tipo de token
        for (Token token : tokens) {
            if (token.getTipo() != null) {
                conteo[token.getTipo().ordinal()]++;
            }
        }

        int totalTokens = tokens.size();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"es\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>Reporte de Estadísticas - PromptZal</title>\n");
        html.append("<style>\n");
        html.append("  :root { --primary: #1e40af; --bg: #f8fafc; --card-bg: #ffffff; --text: #0f172a; --border: #e2e8f0; }\n");
        html.append("  body { font-family: 'Inter', system-ui, -apple-system, sans-serif; background-color: var(--bg); color: var(--text); margin: 0; padding: 40px 20px; }\n");
        html.append("  .container { max-width: 1000px; margin: 0 auto; background: var(--card-bg); padding: 32px; border-radius: 16px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.05); border: 1px solid var(--border); }\n");
        html.append("  .header { border-bottom: 2px solid var(--border); padding-bottom: 16px; margin-bottom: 28px; }\n");
        html.append("  h1 { color: var(--primary); font-size: 1.75rem; margin: 0; font-weight: 700; }\n");
        html.append("  h2 { font-size: 1.2rem; color: #334155; margin: 32px 0 16px 0; font-weight: 600; }\n");
        html.append("  .metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 20px; margin-bottom: 32px; }\n");
        html.append("  .metric-card { background: #f8fafc; border: 1px solid var(--border); padding: 20px; border-radius: 12px; display: flex; flex-direction: column; gap: 8px; }\n");
        html.append("  .metric-card.error { background: #fef2f2; border-color: #fecaca; }\n");
        html.append("  .metric-label { font-size: 0.85rem; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; }\n");
        html.append("  .metric-card.error .metric-label { color: #991b1b; }\n");
        html.append("  .metric-value { font-size: 2rem; font-weight: 800; color: #0f172a; }\n");
        html.append("  .metric-card.error .metric-value { color: #dc2626; }\n");
        html.append("  table { width: 100%; border-collapse: separate; border-spacing: 0; border-radius: 12px; overflow: hidden; border: 1px solid var(--border); }\n");
        html.append("  th, td { padding: 14px 18px; text-align: left; }\n");
        html.append("  th { background-color: #f1f5f9; color: #475569; font-weight: 600; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid var(--border); }\n");
        html.append("  tr:not(:last-child) td { border-bottom: 1px solid var(--border); }\n");
        html.append("  tr:hover { background-color: #f8fafc; }\n");
        html.append("  .progress-bar-bg { background: #e2e8f0; height: 8px; border-radius: 9999px; width: 100%; overflow: hidden; margin-top: 6px; }\n");
        html.append("  .progress-bar-fill { background: var(--primary); height: 100%; border-radius: 9999px; }\n");
        html.append("  .token-badge { font-weight: 600; font-size: 0.75rem; padding: 4px 10px; border-radius: 6px; text-transform: uppercase; letter-spacing: 0.03em; display: inline-block; }\n");
        html.append("  .badge-directiva { background: #fae8ff; color: #86198f; }\n");
        html.append("  .badge-keyword   { background: #e0e7ff; color: #3730a3; }\n");
        html.append("  .badge-comando   { background: #fce7f3; color: #9d174d; }\n");
        html.append("  .badge-conector  { background: #e0f2fe; color: #0369a1; }\n");
        html.append("  .badge-id        { background: #dcfce7; color: #166534; }\n");
        html.append("  .badge-string    { background: #ffedd5; color: #9a3412; }\n");
        html.append("  .badge-number    { background: #fef3c7; color: #92400e; }\n");
        html.append("  .badge-operator  { background: #f3e8ff; color: #6b21a8; }\n");
        html.append("  .badge-delimitador{ background: #f1f5f9; color: #334155; }\n");
        html.append("  .badge-default   { background: #f1f5f9; color: #64748b; }\n");
        html.append("</style>\n</head>\n<body>\n");
        html.append("<div class=\"container\">\n");
        html.append("  <div class=\"header\">\n");
        html.append("    <h1>📊 Resumen Estadístico del Análisis</h1>\n");
        html.append("  </div>\n");
        html.append("  <div class=\"metrics-grid\">\n");
        html.append("    <div class=\"metric-card\">\n");
        html.append("      <span class=\"metric-label\">Líneas Analizadas</span>\n");
        html.append("      <span class=\"metric-value\">").append(totalLineas).append("</span>\n");
        html.append("    </div>\n");

        html.append("    <div class=\"metric-card\">\n");
        html.append("      <span class=\"metric-label\">Tokens Reconocidos</span>\n");
        html.append("      <span class=\"metric-value\">").append(totalTokens).append("</span>\n");
        html.append("    </div>\n");

        html.append("    <div class=\"metric-card ").append(errores.isEmpty() ? "" : "error").append("\">\n");
        html.append("      <span class=\"metric-label\">Errores Totales</span>\n");
        html.append("      <span class=\"metric-value\">").append(errores.size()).append("</span>\n");
        html.append("    </div>\n");
        html.append("  </div>\n");
        html.append("  <h2>Frecuencia por Tipo de Token</h2>\n");
        html.append("  <table>\n");
        html.append("    <thead>\n<tr><th>Tipo de Token</th><th>Cantidad Detectada</th><th>Distribución</th></tr>\n</thead>\n");
        html.append("    <tbody>\n");

        for (int i = 0; i < tipos.length; i++) {
            if (conteo[i] > 0) {
                TipoToken tipo = tipos[i];
                int cantidad = conteo[i];
                double porcentaje = totalTokens > 0 ? ((double) cantidad / totalTokens) * 100 : 0;
                String claseBadge = obtenerClaseCssPorTipo(tipo);

                html.append("<tr>")
                        .append("<td><span class=\"token-badge ").append(claseBadge).append("\">").append(tipo).append("</span></td>")
                        .append("<td><strong>").append(cantidad).append("</strong></td>")
                        .append("<td style=\"width: 40%;\">")
                        .append("  <div style=\"font-size: 0.8rem; color: #64748b;\">").append(String.format("%.1f", porcentaje)).append("%</div>")
                        .append("  <div class=\"progress-bar-bg\"><div class=\"progress-bar-fill\" style=\"width: ").append(String.format("%.1f", porcentaje)).append("%;\"></div></div>")
                        .append("</td>")
                        .append("</tr>\n");
            }
        }

        html.append("    </tbody>\n  </table>\n");
        html.append("</div>\n</body>\n</html>");

        escribirArchivo(rutaSalida, html.toString());
    }

    public void generarReporteErroresHTML(List<ErrorCompilacion> errores, String rutaSalida) {
        StringBuilder html = inicioPagina("Reporte de Errores - PromptZal", "Reporte de Errores de Compilación", "#dc2626", "#fef2f2", "#991b1b");

        if (errores.isEmpty()) {
            html.append("  <div class=\"ok\">✔ La compilación terminó sin errores (léxicos, sintácticos ni semánticos).</div>\n");
        } else {
            int lex = 0;
            int sin = 0;
            int sem = 0;
            for (ErrorCompilacion error : errores) {
                switch (error.getTipo()) {
                    case LEXICO:
                        lex++;
                        break;
                    case SINTACTICO:
                        sin++;
                        break;
                    case SEMANTICO:
                        sem++;
                        break;
                }
            }
            html.append("  <p class=\"resumen\">Total: <b>").append(errores.size()).append("</b> &nbsp;|&nbsp; Léxicos: <b>")
                    .append(lex).append("</b> &nbsp;|&nbsp; Sintácticos: <b>").append(sin)
                    .append("</b> &nbsp;|&nbsp; Semánticos: <b>").append(sem).append("</b></p>\n");
            html.append("  <table>\n<thead><tr><th>#</th><th>Tipo</th><th>Token / Lexema</th><th>Descripción</th><th>Fila</th><th>Columna</th></tr></thead>\n<tbody>\n");
            int i = 1;
            for (ErrorCompilacion errorCompilacion : errores) {
                html.append("<tr>")
                        .append("<td>").append(i++).append("</td>")
                        .append("<td><span class=\"tipo t-").append(errorCompilacion.getTipo()).append("\">").append(errorCompilacion.getTipo()).append("</span></td>")
                        .append("<td><span class=\"code\">").append(escapeHtml(errorCompilacion.getLexema())).append("</span></td>")
                        .append("<td>").append(escapeHtml(errorCompilacion.getDescripcion())).append("</td>")
                        .append("<td>").append(errorCompilacion.getFila()).append("</td>")
                        .append("<td>").append(errorCompilacion.getColumna()).append("</td>")
                        .append("</tr>\n");
            }
            html.append("</tbody>\n  </table>\n");
        }
        finPagina(html, rutaSalida);
    }



    private StringBuilder inicioPagina(String titulo, String h1, String colorTitulo, String fondoTh, String colorTh) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"es\">\n<head>\n<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>").append(escapeHtml(titulo)).append("</title>\n<style>\n");
        html.append("  body { font-family: 'Inter', system-ui, -apple-system, sans-serif; background: #f8fafc; color: #0f172a; margin: 0; padding: 40px 20px; }\n");
        html.append("  .container { max-width: 1100px; margin: 0 auto; background: #fff; padding: 32px; border-radius: 16px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.05); border: 1px solid #e2e8f0; }\n");
        html.append("  .header { border-bottom: 2px solid #e2e8f0; padding-bottom: 16px; margin-bottom: 24px; }\n");
        html.append("  h1 { color: ").append(colorTitulo).append("; font-size: 1.75rem; margin: 0; font-weight: 700; }\n");
        html.append("  .resumen { color: #475569; margin: 0 0 16px 0; }\n");
        html.append("  table { width: 100%; border-collapse: separate; border-spacing: 0; border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; }\n");
        html.append("  th, td { padding: 12px 16px; text-align: left; }\n");
        html.append("  th { background: ").append(fondoTh).append("; color: ").append(colorTh).append("; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid #e2e8f0; }\n");
        html.append("  tr:not(:last-child) td { border-bottom: 1px solid #e2e8f0; }\n");
        html.append("  .code { font-family: 'Fira Code', 'Consolas', monospace; font-size: 0.9rem; background: #f1f5f9; padding: 3px 8px; border-radius: 6px; border: 1px solid #e2e8f0; display: inline-block; }\n");
        html.append("  .tipo { font-weight: 700; font-size: 0.75rem; padding: 4px 10px; border-radius: 6px; display: inline-block; }\n");
        html.append("  .t-LEXICO { background: #fef3c7; color: #92400e; }\n");
        html.append("  .t-SINTACTICO { background: #fee2e2; color: #991b1b; }\n");
        html.append("  .t-SEMANTICO { background: #e0e7ff; color: #3730a3; }\n");
        html.append("  .ok { text-align: center; padding: 36px 20px; background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 12px; color: #166534; font-size: 1.1rem; font-weight: 600; }\n");
        html.append("  .arbol { background: #0f172a; color: #e2e8f0; padding: 20px; border-radius: 12px; overflow-x: auto; font-family: 'Fira Code', 'Consolas', monospace; font-size: 0.9rem; line-height: 1.5; }\n");
        html.append("</style>\n</head>\n<body>\n<div class=\"container\">\n");
        html.append("  <div class=\"header\"><h1>").append(h1).append("</h1></div>\n");
        return html;
    }

    private void finPagina(StringBuilder html, String rutaSalida) {
        html.append("</div>\n</body>\n</html>");
        escribirArchivo(rutaSalida, html.toString());
    }
}