package com.example.demo.controller;

import com.example.demo.model.Asistencia;
import com.example.demo.model.Empleado;
import com.example.demo.service.AsistenciaService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ExportadorAsistenciaController {

    private final AsistenciaService asistenciaService;

    /**
     * Exportar asistencias por rango de fechas
     * GET /api/asistencias/exportar-rango?desde=2026-02-01&hasta=2026-02-28
     */
    @GetMapping("/exportar-rango")
    public void exportarPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            HttpServletResponse response,
            HttpSession session) throws Exception {

        // Solo ADMIN y RRHH
        Empleado usuarioLogueado = (Empleado) session.getAttribute("empleado");
        if (usuarioLogueado == null) {
            response.sendError(401, "No autenticado");
            return;
        }
        if (!"ADMIN".equals(usuarioLogueado.getRol().toString()) &&
                !"RRHH".equals(usuarioLogueado.getRol().toString())) {
            response.sendError(403, "No tienes permiso");
            return;
        }

        // Obtener asistencias del rango
        List<Asistencia> asistencias = asistenciaService.listarPorRango(desde, hasta);

        String nombreArchivo = String.format("asistencias_%s_al_%s",
                desde.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                hasta.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + nombreArchivo + ".xlsx");

        // Crear workbook
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Asistencias");

        // ====== ESTILOS ======
        XSSFCellStyle styleHeader = crearEstilo(wb, "2E75B6", "FFFFFF", true, 10, HorizontalAlignment.CENTER);
        XSSFCellStyle styleNormal = crearEstilo(wb, "FFFFFF", "000000", false, 10, HorizontalAlignment.LEFT);
        XSSFCellStyle styleAlterno = crearEstilo(wb, "D6E4F0", "000000", false, 10, HorizontalAlignment.LEFT);
        XSSFCellStyle styleCentrado = crearEstilo(wb, "FFFFFF", "000000", false, 10, HorizontalAlignment.CENTER);
        XSSFCellStyle styleCentradoAlt = crearEstilo(wb, "D6E4F0", "000000", false, 10, HorizontalAlignment.CENTER);

        // ====== CABECERAS ======
        String[] cabeceras = {
                "Fecha", "Empleado", "Documento", "Cargo",
                "Entrada", "Salida", "Horas Trabajadas", "Estado"
        };
        int[] anchos = {12, 24, 16, 20, 12, 12, 16, 14};

        Row filaCabeceras = sheet.createRow(0);
        filaCabeceras.setHeightInPoints(25);
        for (int i = 0; i < cabeceras.length; i++) {
            Cell c = filaCabeceras.createCell(i);
            c.setCellValue(cabeceras[i]);
            c.setCellStyle(styleHeader);
            sheet.setColumnWidth(i, anchos[i] * 256);
        }

        // ====== DATOS ======
        int filaActual = 1;
        for (int idx = 0; idx < asistencias.size(); idx++) {
            Asistencia a = asistencias.get(idx);
            boolean alterno = idx % 2 != 0;

            Row fila = sheet.createRow(filaActual);
            fila.setHeightInPoints(20);

            XSSFCellStyle esNorm = alterno ? styleAlterno : styleNormal;
            XSSFCellStyle esCentro = alterno ? styleCentradoAlt : styleCentrado;

            // FECHA (COLUMNA 0 - LA MÁS IMPORTANTE!)
            Cell cFecha = fila.createCell(0);
            cFecha.setCellValue(a.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            cFecha.setCellStyle(esCentro);

            // Empleado
            Cell cNombre = fila.createCell(1);
            cNombre.setCellValue(a.getEmpleado().getNombre() + " " + a.getEmpleado().getApellido());
            cNombre.setCellStyle(esNorm);

            // Documento
            Cell cDoc = fila.createCell(2);
            cDoc.setCellValue(a.getEmpleado().getDocumento());
            cDoc.setCellStyle(esCentro);

            // Cargo
            Cell cCargo = fila.createCell(3);
            cCargo.setCellValue(a.getEmpleado().getCargo() != null
                    ? a.getEmpleado().getCargo().getNombre() : "Sin cargo");
            cCargo.setCellStyle(esNorm);

            // Entrada
            Cell cEntrada = fila.createCell(4);
            if (a.getHoraEntrada() != null) {
                cEntrada.setCellValue(a.getHoraEntrada().toString());
            } else {
                cEntrada.setCellValue("-");
            }
            cEntrada.setCellStyle(esCentro);

            // Salida
            Cell cSalida = fila.createCell(5);
            if (a.getHoraSalida() != null) {
                cSalida.setCellValue(a.getHoraSalida().toString());
            } else {
                cSalida.setCellValue("-");
            }
            cSalida.setCellStyle(esCentro);

            // Horas trabajadas
            Cell cHoras = fila.createCell(6);
            if (a.getHoraEntrada() != null && a.getHoraSalida() != null) {
                long minutos = java.time.Duration.between(
                        a.getHoraEntrada(), a.getHoraSalida()).toMinutes();
                long horas = minutos / 60;
                long mins = minutos % 60;
                cHoras.setCellValue(horas + "h " + mins + "m");
            } else {
                cHoras.setCellValue("-");
            }
            cHoras.setCellStyle(esCentro);

            // Estado
            Cell cEstado = fila.createCell(7);
            cEstado.setCellValue(a.getEstado().toString());
            cEstado.setCellStyle(esCentro);

            filaActual++;
        }

        // Escribir
        wb.write(response.getOutputStream());
        wb.close();
    }

    // ====== HELPERS ======
    private XSSFCellStyle crearEstilo(XSSFWorkbook wb, String bgColor, String fgColor,
                                      boolean bold, int fontSize, HorizontalAlignment align) {
        XSSFCellStyle style = wb.createCellStyle();

        // Fondo
        XSSFColor bg = new XSSFColor(hexToBytes(bgColor), null);
        style.setFillForegroundColor(bg);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Fuente
        XSSFFont font = wb.createFont();
        font.setFontName("Arial");
        font.setBold(bold);
        font.setFontHeightInPoints((short) fontSize);
        XSSFColor fg = new XSSFColor(hexToBytes(fgColor), null);
        font.setColor(fg);
        style.setFont(font);

        // Alineación
        style.setAlignment(align);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        // Bordes
        BorderStyle bs = BorderStyle.THIN;
        style.setBorderTop(bs);
        style.setBorderBottom(bs);
        style.setBorderLeft(bs);
        style.setBorderRight(bs);
        XSSFColor borderColor = new XSSFColor(hexToBytes("CCCCCC"), null);
        style.setTopBorderColor(borderColor);
        style.setBottomBorderColor(borderColor);
        style.setLeftBorderColor(borderColor);
        style.setRightBorderColor(borderColor);

        return style;
    }

    private byte[] hexToBytes(String hex) {
        int r = Integer.valueOf(hex.substring(0, 2), 16);
        int g = Integer.valueOf(hex.substring(2, 4), 16);
        int b = Integer.valueOf(hex.substring(4, 6), 16);
        return new byte[]{(byte) r, (byte) g, (byte) b};
    }
}
