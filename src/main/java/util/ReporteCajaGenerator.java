package util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReporteCajaGenerator {

    public static void generarTxtCorte(String vendedor, double fondo, double ventas, double totalEsperado, double totalReal, double diferencia, String estadoCorte) {
        try {
            File directorio = new File("Cortes_Caja");
            if (!directorio.exists()) {
                directorio.mkdir();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fechaFmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            File archivo = new File(directorio, "Corte_" + vendedor + "_" + timestamp + ".txt");

            try (PrintWriter out = new PrintWriter(new FileWriter(archivo))) {
                out.println("========================================");
                out.println("             CORTE DE CAJA              ");
                out.println("========================================");
                out.println("Fecha y Hora : " + fechaFmt);
                out.println("Cajero/Admin : " + vendedor);
                out.println("----------------------------------------");
                out.println("Fondo Inicial        : $" + String.format("%.2f", fondo));
                out.println("Ventas del Turno     : $" + String.format("%.2f", ventas));
                out.println("Total Esperado       : $" + String.format("%.2f", totalEsperado));
                out.println("----------------------------------------");
                out.println("Total Físico (Real)  : $" + String.format("%.2f", totalReal));
                out.println("Diferencia           : $" + String.format("%.2f", diferencia));
                out.println("Estado               : " + estadoCorte);
                out.println("========================================");
            }
            
            System.out.println("Archivo de corte guardado en: " + archivo.getAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("Error al generar el TXT del corte: " + e.getMessage());
        }
    }
}