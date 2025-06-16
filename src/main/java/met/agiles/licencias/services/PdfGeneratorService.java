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

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.enums.LicenseClass; // Asumiendo que LicenseClass es un enum

@Service
public class PdfGeneratorService {


    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateLicensePdf(License licencia) throws IOException, DocumentException {
        // Dimensiones estándar de tarjeta ID-1 en puntos
        Rectangle pageSize = new Rectangle(242.36f, 153.02f);
        Document document = new Document(pageSize, 0, 0, 0, 0); // Sin márgenes en el documento

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        document.open();

        try {
            // ==========================================================
            // FUENTES Y COLORES
            // ==========================================================
            // Usar Color en lugar de BaseColor
            Color darkGreen = new Color(25, 135, 84); // #198754
            Color lightGray = new Color(222, 226, 230); // #dee2e6
            Color darkGrayText = new Color(73, 80, 87); // #495057 (para data-label)
            Color black = Color.BLACK;

            Font fontHeader = new Font(Font.HELVETICA, 8, Font.BOLD, black); // Usando darkGrayText
            Font fontTitle = new Font(Font.HELVETICA, 7, Font.BOLD, black);
            Font fontLabel = new Font(Font.HELVETICA, 6, Font.BOLD, darkGrayText); // Usando darkGrayText
            Font fontValue = new Font(Font.HELVETICA, 6, Font.NORMAL, black); // Usando black
            Font fontClass = new Font(Font.HELVETICA, 9, Font.BOLD, black); // Para la clase de licencia
            Font fontSmall = new Font(Font.HELVETICA, 5, Font.NORMAL, darkGrayText); // Para "FIRMA DEL TITULAR"

            // ==========================================================
            // CONTENEDOR PRINCIPAL - Tarjeta con Borde y Fondo
            // ==========================================================
            // Para simular el border-radius y background-color en el documento base,
            // podemos usar un PdfPTable como contenedor principal.
            PdfPTable cardContainer = new PdfPTable(1);
            cardContainer.setWidthPercentage(100); // Ocupa todo el ancho de la página
            cardContainer.setTotalWidth(pageSize.getWidth()); // Ancho total en puntos
            cardContainer.setLockedWidth(true);
            cardContainer.getDefaultCell().setBorder(Rectangle.NO_BORDER); // Por defecto sin bordes para las celdas

            // Celda para el contenido de la tarjeta
            PdfPCell cardContentCell = new PdfPCell();
            cardContentCell.setBorder(Rectangle.NO_BORDER);
            cardContentCell.setPadding(0); // El padding lo manejan las tablas internas

            // Creamos una sub-tabla para el diseño de la tarjeta (borde, fondo, padding)
            PdfPTable innerCardTable = new PdfPTable(1);
            innerCardTable.setWidthPercentage(100);
            innerCardTable.setTotalWidth(pageSize.getWidth() - 2f); // Restamos un poco para un margen
            innerCardTable.setLockedWidth(true);
            innerCardTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            innerCardTable.getDefaultCell().setPadding(0);
            innerCardTable.getDefaultCell().setBackgroundColor(new Color(248, 249, 250)); // #f8f9fa background-color
            innerCardTable.getDefaultCell().setBorderWidth(0.5f); // 0.5mm border
            innerCardTable.getDefaultCell().setBorderColor(darkGreen);
            innerCardTable.getDefaultCell().setPadding(8f); // Padding total de la tarjeta (aprox. 3mm)
            innerCardTable.setSpacingAfter(0); // Sin espacio después

            // ==========================================================
            // ENCABEZADO
            // ==========================================================
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            headerTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.setSpacingAfter(4f); // Espacio después del encabezado

            headerTable.addCell(new Phrase("REPÚBLICA ARGENTINA", fontHeader));
            headerTable.addCell(new Phrase("LICENCIA NACIONAL DE CONDUCIR", fontTitle));

            // Borde inferior debajo del encabezado
            PdfPCell borderCell = new PdfPCell();
            borderCell.setBorder(Rectangle.BOTTOM);
            borderCell.setBorderColor(lightGray); // Color gris claro
            borderCell.setBorderWidth(0.5f); // Ancho del borde en puntos
            borderCell.setPadding(0); // Sin padding extra para el borde
            borderCell.setFixedHeight(1f); // Asegura que la celda tenga un alto mínimo para el borde
            headerTable.addCell(borderCell);

            innerCardTable.addCell(headerTable);

            // ==========================================================
            // CONTENIDO PRINCIPAL (Datos de la licencia y Firma)
            // ==========================================================
            PdfPTable mainContentTable = new PdfPTable(new float[]{0.68f, 0.32f}); // Proporciones para datos y firma
            mainContentTable.setWidthPercentage(100);
            mainContentTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            mainContentTable.getDefaultCell().setPadding(0); // Padding se maneja dentro de los elementos

            // Celda para los datos de la licencia (columna izquierda)
            PdfPCell dataCell = new PdfPCell();
            dataCell.setBorder(Rectangle.NO_BORDER);
            dataCell.setPadding(0);
            dataCell.setPaddingLeft(2f); // Pequeño padding para el texto

            // Agrega los datos a la celda de datos usando Paragraphs con Chunks
            dataCell.addElement(createDataParagraph("N° Licencia: ", String.valueOf(licencia.getId()), fontLabel, fontValue));
            dataCell.addElement(createDataParagraph("Apellido: ", licencia.getLast_name(), fontLabel, fontValue));
            dataCell.addElement(createDataParagraph("Nombre: ", licencia.getFirst_name(), fontLabel, fontValue));
            dataCell.addElement(createDataParagraph("Domicilio: ", licencia.getAddress(), fontLabel, fontValue));
            dataCell.addElement(createDataParagraph("Fecha de Nacimiento: ", licencia.getBirthDate().format(DATE_FORMATTER), fontLabel, fontValue));
            dataCell.addElement(createDataParagraph("Nacionalidad: ", "ARGENTINA", fontLabel, fontValue));

            // Clase(s) de licencia con fuente más grande
            Paragraph classParagraph = new Paragraph();
            classParagraph.setLeading(fontClass.getSize() * 1.2f); // Ajustar leading si es necesario
            classParagraph.add(new Chunk("Clase(s): ", fontLabel));
            // Concatenar las clases de licencia si es una lista
            String classesString = licencia.getLicenseClasses() != null ?
                    licencia.getLicenseClasses().stream()
                            .map(LicenseClass::name) // Asumiendo que LicenseClass es un enum
                            .map(String::toUpperCase)
                            .collect(java.util.stream.Collectors.joining(", "))
                    : "";
            classParagraph.add(new Chunk(classesString, fontClass));
            dataCell.addElement(classParagraph);

            mainContentTable.addCell(dataCell);

            // Celda para las observaciones (columna derecha)
            PdfPCell observationsCell = new PdfPCell();
            observationsCell.setBorder(Rectangle.NO_BORDER);
            observationsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            observationsCell.setVerticalAlignment(Element.ALIGN_BOTTOM); // Alinea al final de la celda
            observationsCell.setPadding(0);
            observationsCell.setPaddingBottom(1f); // Pequeño padding inferior para separar del borde
            if(licencia.getObvservations() == null){
                observationsCell.addElement(new Paragraph("No hay observaciones", fontSmall));
            }
            else observationsCell.addElement(new Paragraph(licencia.getObvservations(), fontSmall));


            mainContentTable.addCell(observationsCell);
            innerCardTable.addCell(mainContentTable);

            // ==========================================================
            // FOOTER (Fechas de otorgamiento, vencimiento, organismo)
            // ==========================================================
            PdfPTable footerTable = new PdfPTable(3); // 3 columnas
            footerTable.setWidthPercentage(100);
            footerTable.setSpacingBefore(4f); // Espacio antes del footer
            footerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            footerTable.getDefaultCell().setPadding(0); // Resetear padding por defecto

            // Borde superior para el footer (similar a la línea en el HTML)
            PdfPCell topBorderCell = new PdfPCell();
            topBorderCell.setBorder(Rectangle.TOP);
            topBorderCell.setBorderColor(lightGray);
            topBorderCell.setBorderWidth(0.5f);
            topBorderCell.setPaddingTop(1f);
            topBorderCell.setFixedHeight(1f);
            topBorderCell.setColspan(3); // Que ocupe las 3 columnas
            footerTable.addCell(topBorderCell);

            // Celda de Otorgamiento
            PdfPCell cellOtorgamiento = new PdfPCell();
            cellOtorgamiento.setBorder(Rectangle.NO_BORDER);
            cellOtorgamiento.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellOtorgamiento.addElement(createDataParagraph("Otorgamiento: ", licencia.getIssuanceDate().format(DATE_FORMATTER), fontLabel, fontValue));
            footerTable.addCell(cellOtorgamiento);

            // Celda de Vencimiento
            PdfPCell cellVencimiento = new PdfPCell();
            cellVencimiento.setBorder(Rectangle.NO_BORDER);
            cellVencimiento.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellVencimiento.addElement(createDataParagraph("Vencimiento:      ", licencia.getExpirationDate().format(DATE_FORMATTER), fontLabel, fontValue));
            footerTable.addCell(cellVencimiento);

            // Celda de Organismo
            PdfPCell cellOrganismo = new PdfPCell();
            cellOrganismo.setBorder(Rectangle.NO_BORDER);
            cellOrganismo.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellOrganismo.addElement(createDataParagraph("Organismo: ", "MUNI. DE SANTA FE", fontLabel, fontValue));
            footerTable.addCell(cellOrganismo);

            innerCardTable.addCell(footerTable); // Añadir la tabla del footer a la tabla interna de la tarjeta

            cardContainer.addCell(innerCardTable); // Añadir la tabla interna al contenedor principal

            document.add(cardContainer); // Finalmente añadir el contenedor principal al documento

        } finally {
            document.close();
        }

        return os.toByteArray();
    }

    /**
     * Helper method to create a Paragraph with a label (bold) and a value (normal).
     * @param labelText The label text.
     * @param valueText The value text.
     * @param labelFont The font for the label.
     * @param valueFont The font for the value.
     * @return A Paragraph containing the styled label and value.
     */
    private Paragraph createDataParagraph(String labelText, String valueText, Font labelFont, Font valueFont) {
        Paragraph p = new Paragraph();
        p.setLeading(labelFont.getSize() * 1.1f); // Reducido de 1.2f a 1.1f para compactar
        p.add(new Chunk(labelText, labelFont));
        p.add(new Chunk(valueText.toUpperCase(), valueFont));
        p.setSpacingAfter(0.5f); // Pequeño espacio para separar líneas
        return p;
    }
}