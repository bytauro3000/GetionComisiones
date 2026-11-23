package com.vendelote.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.vendelote.entity.Cliente;
import com.vendelote.entity.Proforma;
import com.vendelote.entity.ProformaDetalle;
import com.vendelote.entity.Usuario;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.ClienteRepository;
import com.vendelote.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Genera el PDF de una proforma (OpenPDF). Layout limpio y profesional:
 * encabezado del vendedor, datos del cliente, lotes agrupados por empresa/
 * programa con subtotales, total, observaciones y notas legales.
 */
@Service
@RequiredArgsConstructor
public class ProformaPdfService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY;

    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
        s.setGroupingSeparator(',');
        s.setDecimalSeparator('.');
        MONEY = new DecimalFormat("#,##0.00", s);
    }

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    public byte[] generar(Proforma p) {
        Integer idV = p.getIdVendedor();
        Usuario vendedor = usuarioRepository.findById(idV).orElse(null);
        Cliente cliente = p.getIdCliente() != null
                ? clienteRepository.findByIdAndIdVendedor(p.getIdCliente(), idV).orElse(null)
                : null;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font small = FontFactory.getFont(FontFactory.HELVETICA, 8);

            // ── Encabezado: vendedor (izq) + título (der) ────────────────────
            PdfPTable head = new PdfPTable(new float[] { 1.4f, 1f });
            head.setWidthPercentage(100);

            PdfPCell izq = new PdfPCell();
            izq.setBorder(0);
            izq.addElement(new Paragraph("ASESOR / VENDEDOR", small));
            izq.addElement(new Paragraph(nombreVendedor(vendedor), h2));
            izq.addElement(new Paragraph(datosVendedor(vendedor), small));
            head.addCell(izq);

            PdfPCell der = new PdfPCell();
            der.setBorder(0);
            der.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph titulo = new Paragraph("PROFORMA", h1);
            titulo.setAlignment(Element.ALIGN_RIGHT);
            der.addElement(titulo);
            Paragraph numero = new Paragraph(p.getNumero() != null ? p.getNumero() : "", bold);
            numero.setAlignment(Element.ALIGN_RIGHT);
            der.addElement(numero);
            Paragraph fecha = new Paragraph(
                    "Fecha: " + (p.getFecha() != null ? p.getFecha().format(FECHA) : "—"), small);
            fecha.setAlignment(Element.ALIGN_RIGHT);
            der.addElement(fecha);
            Paragraph estado = new Paragraph("Estado: " + p.getEstado(), small);
            estado.setAlignment(Element.ALIGN_RIGHT);
            der.addElement(estado);
            head.addCell(der);

            doc.add(head);
            doc.add(new Paragraph(" "));

            // ── Datos del cliente ────────────────────────────────────────────
            doc.add(new Paragraph("DATOS DEL CLIENTE", bold));
            doc.add(new Paragraph(
                    "Cliente: " + nombreCliente(cliente), normal));
            doc.add(new Paragraph(
                    "Documento: " + (cliente != null && cliente.getNumDocumento() != null
                            ? cliente.getNumDocumento() : "—")
                            + "     Teléfono: " + (cliente != null && cliente.getTelefono() != null
                            ? cliente.getTelefono() : "—"), normal));
            if (cliente != null && cliente.getCorreo() != null) {
                doc.add(new Paragraph("Correo: " + cliente.getCorreo(), normal));
            }
            doc.add(new Paragraph(" "));

            // ── Tabla de lotes agrupada por empresa/programa ────────────────
            Map<String, List<ProformaDetalle>> grupos = agrupar(p.getDetalles());
            PdfPTable tabla = new PdfPTable(new float[] { 1f, 0.3f, 0.4f, 0.4f });
            tabla.setWidthPercentage(100);

            cabecera(tabla, "Lote", Element.ALIGN_LEFT, bold);
            cabecera(tabla, "Área (m²)", Element.ALIGN_RIGHT, bold);
            cabecera(tabla, "Inicial USD", Element.ALIGN_RIGHT, bold);
            cabecera(tabla, "Precio USD", Element.ALIGN_RIGHT, bold);

            BigDecimal total = BigDecimal.ZERO;
            BigDecimal inicialTotal = BigDecimal.ZERO;
            int cuotasMax = 0;

            for (Map.Entry<String, List<ProformaDetalle>> g : grupos.entrySet()) {
                PdfPCell grupo = new PdfPCell(new Phrase(g.getKey(), bold));
                grupo.setColspan(4);
                grupo.setPadding(4);
                tabla.addCell(grupo);

                BigDecimal subtotal = BigDecimal.ZERO;
                for (ProformaDetalle d : g.getValue()) {
                    BigDecimal inicial = d.getInicial() != null ? d.getInicial() : BigDecimal.ZERO;
                    tabla.addCell(celda(d.getDescripcion() != null ? d.getDescripcion() : "—",
                            Element.ALIGN_LEFT, normal));
                    tabla.addCell(celda(d.getAreaM2() != null ? MONEY.format(d.getAreaM2()) : "—",
                            Element.ALIGN_RIGHT, normal));
                    tabla.addCell(celda(money(inicial), Element.ALIGN_RIGHT, normal));
                    tabla.addCell(celda(money(d.getPrecioUnitario()), Element.ALIGN_RIGHT, normal));
                    subtotal = subtotal.add(d.getPrecioUnitario() != null
                            ? d.getPrecioUnitario() : BigDecimal.ZERO);
                    inicialTotal = inicialTotal.add(inicial);
                    if (d.getNumCuotas() != null) {
                        cuotasMax = Math.max(cuotasMax, d.getNumCuotas());
                    }
                }
                PdfPCell sub = new PdfPCell(new Phrase("Subtotal " + nombreCorto(g.getKey()) + ": "
                        + money(subtotal), small));
                sub.setColspan(4);
                sub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                sub.setPadding(4);
                tabla.addCell(sub);
                total = total.add(subtotal);
            }

            PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL USD: " + money(total), h2));
            totalCell.setColspan(4);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCell.setPadding(6);
            tabla.addCell(totalCell);

            doc.add(tabla);
            doc.add(new Paragraph(" "));

            BigDecimal saldo = total.subtract(inicialTotal);
            if (saldo.signum() < 0) {
                saldo = BigDecimal.ZERO;
            }
            BigDecimal cuota = cuotasMax > 0
                    ? saldo.divide(BigDecimal.valueOf(cuotasMax), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            doc.add(new Paragraph("Inicial total USD: " + money(inicialTotal), normal));
            doc.add(new Paragraph("Saldo a financiar USD: " + money(saldo), normal));
            if (cuotasMax > 0) {
                doc.add(new Paragraph("N° de cuotas: " + cuotasMax
                        + "   ·   Cuota aprox. USD: " + money(cuota), normal));
            }
            doc.add(new Paragraph(" "));

            if (p.getObservaciones() != null && !p.getObservaciones().isBlank()) {
                doc.add(new Paragraph("OBSERVACIONES", bold));
                doc.add(new Paragraph(p.getObservaciones(), normal));
                doc.add(new Paragraph(" "));
            }

            // ── Firmas ───────────────────────────────────────────────────────
            PdfPTable firmas = new PdfPTable(2);
            firmas.setWidthPercentage(100);
            firmas.setSpacingBefore(30);
            firmas.addCell(firma("Cliente", small));
            firmas.addCell(firma("Asesor comercial", small));
            doc.add(firmas);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(
                    "Proforma emitida por el asesor comercial como oferta de venta. No constituye "
                            + "comprobante de pago ni reserva el lote hasta su formalización.", small));

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new NegocioException("No se pudo generar el PDF de la proforma");
        }
    }

    // ── Helpers de layout ─────────────────────────────────────────────────────

    private PdfPCell celda(String texto, int align, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(texto, font));
        c.setHorizontalAlignment(align);
        c.setPadding(4);
        return c;
    }

    private void cabecera(PdfPTable t, String texto, int align, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(texto, font));
        c.setHorizontalAlignment(align);
        c.setPadding(5);
        t.addCell(c);
    }

    private PdfPCell firma(String etiqueta, Font font) {
        PdfPCell c = new PdfPCell();
        c.setBorder(0);
        c.setPaddingTop(24);
        Paragraph linea = new Paragraph("________________________", font);
        linea.setAlignment(Element.ALIGN_CENTER);
        c.addElement(linea);
        Paragraph lbl = new Paragraph(etiqueta, font);
        lbl.setAlignment(Element.ALIGN_CENTER);
        c.addElement(lbl);
        return c;
    }

    private Map<String, List<ProformaDetalle>> agrupar(List<ProformaDetalle> detalles) {
        Map<String, List<ProformaDetalle>> mapa = new LinkedHashMap<>();
        if (detalles == null) {
            return mapa;
        }
        for (ProformaDetalle d : detalles) {
            String emp = d.getEmpresaNombre() != null ? d.getEmpresaNombre() : "Empresa";
            String proy = d.getProyecto() != null && !d.getProyecto().isBlank()
                    ? d.getProyecto() : "Programa";
            String clave = emp + " · " + proy;
            mapa.computeIfAbsent(clave, k -> new java.util.ArrayList<>()).add(d);
        }
        return mapa;
    }

    private String nombreCorto(String grupo) {
        int idx = grupo.indexOf(" · ");
        return idx > 0 ? grupo.substring(0, idx) : grupo;
    }

    private String money(BigDecimal valor) {
        return valor == null ? "0.00" : MONEY.format(valor);
    }

    private String nombreVendedor(Usuario u) {
        if (u == null) {
            return "Vendedor";
        }
        return (u.getNombres() + (u.getApellidos() != null ? " " + u.getApellidos() : "")).trim();
    }

    private String datosVendedor(Usuario u) {
        if (u == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (u.getDni() != null && !u.getDni().isBlank()) {
            sb.append("DNI ").append(u.getDni());
        }
        if (u.getTelefono() != null && !u.getTelefono().isBlank()) {
            if (sb.length() > 0) sb.append("  ·  ");
            sb.append(u.getTelefono());
        }
        if (u.getCorreo() != null && !u.getCorreo().isBlank()) {
            if (sb.length() > 0) sb.append("  ·  ");
            sb.append(u.getCorreo());
        }
        return sb.toString();
    }

    private String nombreCliente(Cliente c) {
        if (c == null) {
            return "Cliente";
        }
        return (c.getNombres() + (c.getApellidos() != null ? " " + c.getApellidos() : "")).trim();
    }
}
