package util;

import javax.print.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

public class TicketGenerator {

    public static void generarTicket(String folio, DefaultTableModel carrito, double total, String vendedor) {
        Preferences prefs = Preferences.userRoot().node("RefaccionariaChayitoConfig");
        String nombreNegocio = prefs.get("nombreNegocio", "Refaccionaria Chayito");
        String telefono = prefs.get("telefono", "");
        String mensaje = prefs.get("mensajeTicket", "¡Gracias por su compra!");

        StringBuilder ticket = new StringBuilder();

        ticket.append("\n"); // Margen superior
        ticket.append(centrarTexto(nombreNegocio, 32)).append("\n");
        
        if (!telefono.isEmpty()) {
            ticket.append(centrarTexto("Tel: " + telefono, 32)).append("\n");
        }
        
        ticket.append("--------------------------------\n");
        ticket.append("Folio: ").append(folio.substring(0, 8).toUpperCase()).append("\n");
        ticket.append("Fecha: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())).append("\n");
        ticket.append("Vend:  ").append(vendedor).append("\n");
        ticket.append("--------------------------------\n");
        ticket.append("CANT DESCRIPCION        SUBTOTAL\n");
        ticket.append("--------------------------------\n");

        for (int i = 0; i < carrito.getRowCount(); i++) {
            String cant = carrito.getValueAt(i, 0).toString();
            String desc = carrito.getValueAt(i, 2).toString();
            String sub = "$" + carrito.getValueAt(i, 4).toString();

            if (desc.length() > 15) {
                desc = desc.substring(0, 15);
            }

            ticket.append(String.format("%-4s %-15s %10s\n", cant, desc, sub));
        }

        ticket.append("--------------------------------\n");
        ticket.append(String.format("%32s\n", "TOTAL: $" + String.format("%.2f", total)));
        ticket.append("\n");
        ticket.append(centrarTexto(mensaje, 32)).append("\n");
        
        ticket.append("\n\n\n\n\n"); 
        
        ticket.append((char)29).append("V").append((char)66).append((char)0);

        imprimirDirecto(ticket.toString());
    }

    private static String centrarTexto(String texto, int anchoMaximo) {
        if (texto.length() >= anchoMaximo) return texto;
        int espacios = (anchoMaximo - texto.length()) / 2;
        return " ".repeat(espacios) + texto;
    }

    private static void imprimirDirecto(String contenido) {
        try {
            PrintService servicioImpresion = PrintServiceLookup.lookupDefaultPrintService();
            
            if (servicioImpresion == null) {
                System.out.println("No se encontró ninguna impresora predeterminada en el sistema.");
                return;
            }

            DocPrintJob printJob = servicioImpresion.createPrintJob();
            byte[] bytes = contenido.getBytes("CP437"); 
            Doc document = new SimpleDoc(bytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);

            printJob.print(document, null);
            System.out.println("Imprimiendo silenciosamente en: " + servicioImpresion.getName());

        } catch (Exception e) {
            System.err.println("Error al intentar imprimir directamente:");
            e.printStackTrace();
        }
    }
}