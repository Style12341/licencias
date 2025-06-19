package met.agiles.licencias.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.persistance.repository.LicenseRepository;

@Service
public class LicenseReportService {

  @Autowired
  private LicenseRepository licenseRepository;

  public void generateReport(LocalDate startDate, LocalDate endDate, HttpServletResponse response) throws IOException {
    // Obtener las licencias expiradas en el rango de fechas
    List<License> licenses = licenseRepository.findByExpirationDateBetween(startDate, endDate);

    // Crear un workbook de Excel
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Licencias Expiradas");

    // Agregar encabezados
    Row headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("ID");
    headerRow.createCell(1).setCellValue("DNI");
    headerRow.createCell(2).setCellValue("Fecha de emisión");
    headerRow.createCell(3).setCellValue("Fecha de vencimiento");
    headerRow.createCell(4).setCellValue("Nombre titular");
    headerRow.createCell(5).setCellValue("Apellido titular");

    // Agregar filas con datos
    int rowCounter = 1;
    for (License license : licenses) {
      Row row = sheet.createRow(rowCounter);
      row.createCell(0).setCellValue(license.getId());
      row.createCell(1).setCellValue(license.getDni());
      row.createCell(2).setCellValue(license.getIssuanceDate());
      row.createCell(3).setCellValue(license.getExpirationDate());
      row.createCell(4).setCellValue(license.getHolder().getName());
      row.createCell(5).setCellValue(license.getHolder().getLastName());
      rowCounter++;
    }

    // Configurar la respuesta HTTP
    response.setContentType("application/vnd.ms-excel");
    response.setHeader("Content-Disposition", "attachment; filename=licencias_expiradas.xlsx");

    // Escribir el workbook en la respuesta
    workbook.write(response.getOutputStream());
  }
}