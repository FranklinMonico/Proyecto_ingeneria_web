package com.proyecto.Studentservices.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CertificatePdfService {

    public byte[] generateCertificate(String studentName, String courseName, LocalDateTime issuedAt) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);

            // Horizontal landscape
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            Document document = new Document(pdf);
            document.setMargins(0, 0, 0, 0);

            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            DeviceRgb darkBlue = new DeviceRgb(31, 78, 121);
            DeviceRgb lightBlue = new DeviceRgb(46, 117, 182);
            DeviceRgb gold = new DeviceRgb(212, 175, 55);
            DeviceRgb lightGray = new DeviceRgb(245, 247, 250);

            // Fondo azul oscuro arriba
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .useAllAvailableWidth();
            Cell headerCell = new Cell()
                    .setBackgroundColor(darkBlue)
                    .setPadding(30)
                    .setBorder(null);
            headerCell.add(new Paragraph("🎓 CERTIFICADO DE FINALIZACIÓN")
                    .setFont(boldFont)
                    .setFontSize(28)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER));
            headerCell.add(new Paragraph("Student Services — Plataforma de Cursos en Línea")
                    .setFont(regularFont)
                    .setFontSize(13)
                    .setFontColor(new DeviceRgb(180, 200, 220))
                    .setTextAlignment(TextAlignment.CENTER));
            headerTable.addCell(headerCell);
            document.add(headerTable);

            // Línea dorada
            Table goldLine = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .useAllAvailableWidth();
            Cell goldCell = new Cell()
                    .setBackgroundColor(gold)
                    .setHeight(6)
                    .setBorder(null);
            goldLine.addCell(goldCell);
            document.add(goldLine);

            // Cuerpo principal
            Table bodyTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .useAllAvailableWidth();
            Cell bodyCell = new Cell()
                    .setBackgroundColor(lightGray)
                    .setPaddingTop(40)
                    .setPaddingBottom(40)
                    .setPaddingLeft(60)
                    .setPaddingRight(60)
                    .setBorder(null);

            bodyCell.add(new Paragraph("Se certifica que:")
                    .setFont(regularFont)
                    .setFontSize(16)
                    .setFontColor(new DeviceRgb(100, 100, 100))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

            bodyCell.add(new Paragraph(studentName)
                    .setFont(boldFont)
                    .setFontSize(36)
                    .setFontColor(darkBlue)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

            bodyCell.add(new Paragraph("ha completado exitosamente el curso:")
                    .setFont(italicFont)
                    .setFontSize(16)
                    .setFontColor(new DeviceRgb(100, 100, 100))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

            bodyCell.add(new Paragraph(courseName)
                    .setFont(boldFont)
                    .setFontSize(26)
                    .setFontColor(lightBlue)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30));

            // Fecha
            String fecha = issuedAt.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy",
                    new java.util.Locale("es", "ES")));
            bodyCell.add(new Paragraph("Fecha de emisión: " + fecha)
                    .setFont(regularFont)
                    .setFontSize(13)
                    .setFontColor(new DeviceRgb(120, 120, 120))
                    .setTextAlignment(TextAlignment.CENTER));

            bodyTable.addCell(bodyCell);
            document.add(bodyTable);

            // Línea dorada inferior
            Table goldLine2 = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .useAllAvailableWidth();
            Cell goldCell2 = new Cell()
                    .setBackgroundColor(gold)
                    .setHeight(6)
                    .setBorder(null);
            goldLine2.addCell(goldCell2);
            document.add(goldLine2);

            // Footer
            Table footerTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .useAllAvailableWidth();
            Cell footerCell = new Cell()
                    .setBackgroundColor(darkBlue)
                    .setPadding(20)
                    .setBorder(null);
            footerCell.add(new Paragraph("Universidad Evangélica de El Salvador | Ingeniería de la Web 2026")
                    .setFont(regularFont)
                    .setFontSize(11)
                    .setFontColor(new DeviceRgb(180, 200, 220))
                    .setTextAlignment(TextAlignment.CENTER));
            footerTable.addCell(footerCell);
            document.add(footerTable);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            System.out.println("Error generando PDF: " + e.getMessage());
            return null;
        }
    }
}