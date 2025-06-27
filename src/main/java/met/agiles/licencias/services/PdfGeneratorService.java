package met.agiles.licencias.services;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.Chunk;
import java.awt.Color;
import com.lowagie.text.pdf.PdfWriter;
import met.agiles.licencias.persistance.models.PaymentReceipt;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.enums.LicenseClass;

@Service
public class PdfGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("$#,##0.00");

    public byte[] generateReceiptPdf(PaymentReceipt paymentReceipt) throws IOException, DocumentException {
        // Tamaño de ticket - más alto que ancho para formato vertical
        // 210mm x 297mm (A4) pero ajustado para ticket: 150mm x 200mm
        Rectangle pageSize = new Rectangle(260f, 330f); // 150mm x 200mm en puntos
        Document document = new Document(pageSize, 0, 0, 0, 0);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, os);

        document.open();

        try {
            // ==========================================================
            // COLORES Y FUENTES
            // ==========================================================
            Color borderColor = new Color(108, 117, 125); // #6c757d
            Color labelColor = new Color(73, 80, 87); // #495057
            Color footerTextColor = new Color(108, 117, 125); // #6c757d

            Font fontTitle = new Font(Font.COURIER, 14, Font.BOLD, Color.BLACK);
            Font fontSubtitle = new Font(Font.COURIER, 10, Font.NORMAL, Color.BLACK);
            Font fontLabel = new Font(Font.COURIER, 9, Font.BOLD, labelColor);
            Font fontValue = new Font(Font.COURIER, 9, Font.NORMAL, Color.BLACK);
            Font fontTotal = new Font(Font.COURIER, 12, Font.BOLD, Color.BLACK);
            Font fontFooter = new Font(Font.COURIER, 8, Font.NORMAL, footerTextColor);

            // ==========================================================
            // ESTRUCTURA PRINCIPAL DEL COMPROBANTE
            // ==========================================================

            // Tabla principal con borde punteado
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPCell mainCell = new PdfPCell();
            mainCell.setBorder(Rectangle.NO_BORDER);
            // Simular borde punteado con múltiples líneas finas
            mainCell.setPadding(15f);

            // Contenido interno
            PdfPTable contentTable = new PdfPTable(1);
            contentTable.setWidthPercentage(100);

            // ==========================================================
            // ENCABEZADO
            // ==========================================================
            addReceiptHeader(contentTable, paymentReceipt, fontTitle, fontSubtitle, borderColor);

            // ==========================================================
            // CUERPO - DATOS DEL COMPROBANTE
            // ==========================================================
            addReceiptBody(contentTable, paymentReceipt, fontLabel, fontValue, fontTotal);

            // ==========================================================
            // PIE DE PÁGINA
            // ==========================================================
            addReceiptFooter(contentTable, fontFooter, borderColor);

            mainCell.addElement(contentTable);
            mainTable.addCell(mainCell);
            document.add(mainTable);

        } finally {
            document.close();
        }

        return os.toByteArray();
    }

    // ==========================================================
    // MÉTODOS HELPER
    // ==========================================================

    private void addReceiptHeader(PdfPTable table, PaymentReceipt receipt,
                                  Font titleFont, Font subtitleFont, Color borderColor) {

        // Título principal
        PdfPCell titleCell = new PdfPCell(new Phrase("COMPROBANTE DE PAGO", titleFont));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setPaddingBottom(5f);
        table.addCell(titleCell);

        // ID Transacción
        PdfPCell idCell = new PdfPCell(new Phrase("ID de Transacción: " + receipt.getId(), subtitleFont));
        idCell.setBorder(Rectangle.NO_BORDER);
        idCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        idCell.setPaddingBottom(10f);
        table.addCell(idCell);

        // Línea separadora
        PdfPCell separatorCell = new PdfPCell();
        separatorCell.setBorder(Rectangle.BOTTOM);
        separatorCell.setBorderColor(borderColor);
        separatorCell.setBorderWidth(1f);
        separatorCell.setFixedHeight(10f);
        table.addCell(separatorCell);

        // Espacio
        addSpacerCell(table, 10f);
    }

    private void addReceiptBody(PdfPTable table, PaymentReceipt receipt,
                                Font labelFont, Font valueFont, Font totalFont) {

        // Fecha de Pago
        addReceiptItem(table, "Fecha de Pago:",
                receipt.getPaymentDate().format(DATE_FORMATTER),
                labelFont, valueFont);

        // Titular Licencia
        addReceiptItem(table, "Titular Licencia:",
                receipt.getLicense().getLast_name() + ", " + receipt.getLicense().getFirst_name(),
                labelFont, valueFont);

        // N° Licencia
        addReceiptItem(table, "N° Licencia:",
                String.valueOf(receipt.getLicense().getDni()),
                labelFont, valueFont);

        // Línea separadora
        addSeparatorLine(table);

        // Concepto
        addReceiptItem(table, "Concepto:",
                "Emisión de Licencia de Conducir",
                labelFont, valueFont);

        // Método de Pago
        String paymentMethod = receipt.getPaymentMethod().name().replace("_", " ");
        addReceiptItem(table, "Método de Pago:",
                paymentMethod,
                labelFont, valueFont);

        // Importe Total (destacado)
        addReceiptItem(table, "IMPORTE TOTAL:",
                MONEY_FORMAT.format(receipt.getLicense().getCost()),
                totalFont, totalFont);

        // Línea separadora
        addSeparatorLine(table);

        // Atendido por
        addReceiptItem(table, "Atendido por:",
                receipt.getAdministrativo().getUsername(),
                labelFont, valueFont);
    }

    private void addReceiptFooter(PdfPTable table, Font footerFont, Color borderColor) {
        // Espacio antes del footer
        addSpacerCell(table, 15f);

        // Línea separadora superior
        PdfPCell separatorCell = new PdfPCell();
        separatorCell.setBorder(Rectangle.TOP);
        separatorCell.setBorderColor(borderColor);
        separatorCell.setBorderWidth(1f);
        separatorCell.setFixedHeight(10f);
        table.addCell(separatorCell);

        // Mensaje del footer
        PdfPCell footerCell = new PdfPCell(new Phrase("Gracias por su pago. Conserve este comprobante.", footerFont));
        footerCell.setBorder(Rectangle.NO_BORDER);
        footerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        footerCell.setPaddingTop(10f);
        table.addCell(footerCell);
    }

    private void addReceiptItem(PdfPTable table, String label, String value,
                                Font labelFont, Font valueFont) {

        // Crear tabla de 2 columnas para cada item
        PdfPTable itemTable = new PdfPTable(new float[]{0.4f, 0.6f});
        itemTable.setWidthPercentage(100);

        // Celda del label (izquierda)
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setPaddingBottom(8f);
        itemTable.addCell(labelCell);

        // Celda del valor (derecha)
        PdfPCell valueCell = new PdfPCell(new Phrase(value.toUpperCase(), valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingBottom(8f);
        itemTable.addCell(valueCell);

        // Agregar la tabla item a la tabla principal
        PdfPCell itemWrapperCell = new PdfPCell(itemTable);
        itemWrapperCell.setBorder(Rectangle.NO_BORDER);
        itemWrapperCell.setPadding(0);
        table.addCell(itemWrapperCell);
    }

    private void addSeparatorLine(PdfPTable table) {
        PdfPCell hrCell = new PdfPCell();
        hrCell.setBorder(Rectangle.BOTTOM);
        hrCell.setBorderColor(Color.LIGHT_GRAY);
        hrCell.setBorderWidth(0.5f);
        hrCell.setFixedHeight(5f);
        hrCell.setPaddingTop(5f);
        hrCell.setPaddingBottom(5f);
        table.addCell(hrCell);
    }

    private void addSpacerCell(PdfPTable table, float height) {
        PdfPCell spacerCell = new PdfPCell();
        spacerCell.setBorder(Rectangle.NO_BORDER);
        spacerCell.setFixedHeight(height);
        table.addCell(spacerCell);
    }

    public byte[] generateLicensePdf(License licencia) throws IOException, DocumentException {
        // Dimensiones estándar de tarjeta de crédito/ID (ID-1) en puntos (1 pulgada = 72 puntos)
        // 85.6mm × 53.98mm  => 242.66pt x 153.01pt
        Rectangle pageSize = new Rectangle(242.66f, 153.01f);
        Document document = new Document(pageSize, 0, 0, 0, 0);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, os);

        document.open();

        try {
            // ==========================================================
            // COLORES Y FUENTES (Tus personalizaciones se mantienen)
            // ==========================================================
            Color primaryBlue = new Color(7, 150, 211); // #0796D3
            Color lightGrayBorder = new Color(222, 226, 230); // #dee2e6
            Color dataLabelGray = new Color(73, 80, 87); // #495057
            Color photoBgGray = new Color(233, 236, 239); // #e9ecef

            Font fontHeader = new Font(Font.COURIER, 9, Font.BOLD, primaryBlue);
            Font fontSubHeader = new Font(Font.COURIER, 8, Font.NORMAL, primaryBlue);
            Font fontLabel = new Font(Font.COURIER, 7, Font.BOLD, dataLabelGray);
            Font fontValue = new Font(Font.COURIER, 7, Font.NORMAL, Color.BLACK);
            Font fontValueBold = new Font(Font.COURIER, 7, Font.BOLD, Color.BLACK);
            Font fontClass = new Font(Font.COURIER, 12, Font.BOLD, Color.BLACK);
            Font fontPhotoPlaceholder = new Font(Font.COURIER, 10, Font.NORMAL, dataLabelGray);
            Font fontFooter = new Font(Font.COURIER, 7, Font.BOLD, Color.WHITE);

            // ==========================================================
            // CREACIÓN DE COMPONENTES (Tu código se mantiene)
            // ==========================================================

            // ENCABEZADO
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            headerTable.setSpacingAfter(5f);
            addCellToTable(headerTable, "LICENCIA NACIONAL DE CONDUCIR", fontHeader, Element.ALIGN_CENTER, Rectangle.NO_BORDER);
            addCellToTable(headerTable, "Santa Fe", fontSubHeader, Element.ALIGN_CENTER, Rectangle.NO_BORDER);
            PdfPCell lineCell = new PdfPCell();
            lineCell.setBorder(Rectangle.BOTTOM);
            lineCell.setBorderWidth(0.5f);
            lineCell.setBorderColor(lightGrayBorder);
            headerTable.addCell(lineCell);

            // CUERPO PRINCIPAL
            PdfPTable bodyTable = new PdfPTable(new float[]{0.3f, 0.7f});
            bodyTable.setWidthPercentage(100);
            PdfPCell photoCell = new PdfPCell(new Phrase("Foto", fontPhotoPlaceholder));
            photoCell.setBorder(Rectangle.BOX);
            photoCell.setBorderColor(Color.GRAY);
            photoCell.setBackgroundColor(photoBgGray);
            photoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            photoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            photoCell.setFixedHeight(80f);
            bodyTable.addCell(photoCell);

            PdfPCell dataCell = new PdfPCell();
            dataCell.setBorder(Rectangle.NO_BORDER);
            dataCell.setPaddingLeft(5f);
            PdfPTable topDataRow = new PdfPTable(new float[]{0.6f, 0.4f});
            topDataRow.setWidthPercentage(100);
            topDataRow.addCell(createDataPhrase("N° Licencia: ", String.valueOf(licencia.getDni()), fontLabel, fontValue));
            String classesString = licencia.getLicenseClasses() != null ?
                    licencia.getLicenseClasses().stream().map(LicenseClass::name).collect(Collectors.joining(" ")): "";
            Phrase classPhrase = new Phrase();
            classPhrase.add(new Chunk("Clase(s): ", fontLabel));
            classPhrase.add(new Chunk("[" + classesString + "]", fontClass));
            PdfPCell classCell = new PdfPCell(classPhrase);
            classCell.setBorder(Rectangle.NO_BORDER);
            classCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            topDataRow.addCell(classCell);
            dataCell.addElement(topDataRow);
            dataCell.addElement(createDataParagraph("Apellido: ", licencia.getLast_name(), fontLabel, fontValue));
            dataCell.addElement(createDataParagraph("Nombre: ", licencia.getFirst_name(), fontLabel, fontValue));
            dataCell.addElement(createDataParagraph("Domicilio: ", licencia.getAddress(), fontLabel, fontValue));
            dataCell.addElement(createDataParagraph("Fecha de Nacimiento: ", licencia.getBirthDate().format(DATE_FORMATTER), fontLabel, fontValue));
            PdfPTable bottomDataRow = new PdfPTable(2);
            bottomDataRow.setWidthPercentage(100);
            bottomDataRow.addCell(createDataPhrase("Otorgamiento: ", licencia.getIssuanceDate().format(DATE_FORMATTER), fontLabel, fontValue));
            bottomDataRow.addCell(createDataPhrase("Vencimiento: ", licencia.getExpirationDate().format(DATE_FORMATTER), fontLabel, fontValueBold));
            dataCell.addElement(bottomDataRow);
            bodyTable.addCell(dataCell);

            // PIE DE PÁGINA
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            PdfPCell securityCell = new PdfPCell(new Phrase("SEGURIDAD VIAL", fontFooter));
            securityCell.setBackgroundColor(primaryBlue);
            securityCell.setBorder(Rectangle.NO_BORDER);
            securityCell.setPadding(4f);
            securityCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            securityCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            Paragraph ministryLines = new Paragraph();
            ministryLines.add(new Phrase("Ministerio de Transporte\n", fontFooter));
            ministryLines.add(new Phrase("República Argentina", fontFooter));
            PdfPCell ministryCell = new PdfPCell(ministryLines);
            ministryCell.setBackgroundColor(primaryBlue);
            ministryCell.setBorder(Rectangle.NO_BORDER);
            ministryCell.setPadding(4f);
            ministryCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            ministryCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            footerTable.addCell(securityCell);
            footerTable.addCell(ministryCell);

            // ==========================================================
            // ENSAMBLAJE FINAL (SECCIÓN CORREGIDA)
            // ==========================================================
            // En lugar de añadir todo a una celda, creamos una tabla principal
            // que contendrá la tarjeta completa, con un borde exterior.
            PdfPTable cardBorderTable = new PdfPTable(1);
            cardBorderTable.setWidthPercentage(100);

            PdfPCell cardCell = new PdfPCell();
            cardCell.setBorder(Rectangle.BOX); // Borde exterior de la tarjeta
            cardCell.setBorderColor(primaryBlue);
            cardCell.setBorderWidth(1.5f);
            cardCell.setPadding(0); // El padding se maneja adentro

            // Tabla interna para organizar header, body y footer y evitar que el footer se caiga
            PdfPTable innerLayout = new PdfPTable(1);
            innerLayout.setWidthPercentage(100);

            // Celda para header y body con padding
            PdfPCell contentCell = new PdfPCell();
            contentCell.setBorder(Rectangle.NO_BORDER);
            contentCell.setPadding(8f);
            contentCell.addElement(headerTable);
            contentCell.addElement(bodyTable);
            innerLayout.addCell(contentCell);

            // Celda para el footer, sin padding para que el color azul llene el espacio
            PdfPCell footerWrapperCell = new PdfPCell(footerTable);
            footerWrapperCell.setBorder(Rectangle.NO_BORDER);
            footerWrapperCell.setPadding(0);
            innerLayout.addCell(footerWrapperCell);

            cardCell.addElement(innerLayout);
            cardBorderTable.addCell(cardCell);

            document.add(cardBorderTable);

        } finally {
            document.close();
        }
        return os.toByteArray();
    }

    // ==========================================================
    // MÉTODOS HELPER (Tus métodos se mantienen)
    // ==========================================================

    private Paragraph createDataParagraph(String label, String value, Font labelFont, Font valueFont) {
        Paragraph p = new Paragraph();
        p.setLeading(8f);
        p.add(new Chunk(label, labelFont));
        p.add(new Chunk(value.toUpperCase(), valueFont));
        return p;
    }

    private PdfPCell createDataPhrase(String label, String value, Font labelFont, Font valueFont) {
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label, labelFont));
        phrase.add(new Chunk(value.toUpperCase(), valueFont));
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private void addCellToTable(PdfPTable table, String text, Font font, int alignment, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(border);
        table.addCell(cell);
    }
}
