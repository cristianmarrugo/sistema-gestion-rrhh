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
@RequestMapping("/api/asistencias")
@RequiredArgsConstructor
public class ExportadorAsistenciaController {

    private final AsistenciaService asistenciaService;

    /**
     * Exportar asistencias a Excel (.xlsx) — Bien formateado
     * GET /api/asistencias/exportar?fecha=2026-02-18
     * GET /api/asistencias/exportar?mes=2&anio=2026
     */
    @GetMapping("/exportar")
    public void exportarAsistencias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio,
            HttpServletResponse response,
            HttpSession session) throws Exception {

        // Solo ADMIN y RRHH pueden exportar
        Empleado usuarioLogueado = (Empleado) session.getAttribute("empleado");
        if (usuarioLogueado == null) {
            response.sendError(401, "No autenticado");
            return;
        }
        if (!"ADMIN".equals(usuarioLogueado.getRol().toString()) &&
                !"RRHH".equals(usuarioLogueado.getRol().toString())) {
            response.sendError(403, "No tienes permiso para exportar asistencias");
            return;
        }

        // Determinar si es reporte diario o mensual
        List<Asistencia> asistencias;
        String tituloReporte;
        String nombreArchivo;

        if (mes != null && anio != null) {
            // Reporte mensual
            asistencias = asistenciaService.listarPorMes(mes, anio);
            tituloReporte = obtenerNombreMes(mes).toUpperCase() + " " + anio;
            nombreArchivo = String.format("asistencias_%04d-%02d", anio, mes);
        } else {
            // Reporte diario (por defecto: hoy)
            if (fecha == null) {
                fecha = LocalDate.now();
            }
            asistencias = asistenciaService.listarPorFecha(fecha);
            tituloReporte = fecha.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy")).toUpperCase();
            nombreArchivo = "asistencias_" + fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        // Preparar respuesta
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + nombreArchivo + ".xlsx");

        // Crear workbook
        XSSFWorkbook wb = new XSSFWorkbook();

        // Nombre de la hoja según el tipo de reporte
        String nombreHoja;
        if (mes != null && anio != null) {
            nombreHoja = "Asistencias " + obtenerNombreMes(mes) + " " + anio;
        } else {
            nombreHoja = "Asistencias " + (fecha != null ?
                    fecha.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) :
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        }

        XSSFSheet sheet = wb.createSheet(nombreHoja);

        // ====== ESTILOS ======
        XSSFCellStyle styleHeader1 = crearEstilo(wb, "1F3864", "FFFFFF", true, 16, HorizontalAlignment.CENTER);
        XSSFCellStyle styleHeader2 = crearEstilo(wb, "2E75B6", "FFFFFF", true, 10, HorizontalAlignment.CENTER);
        XSSFCellStyle styleSubtitulo = crearEstilo(wb, "2E75B6", "FFFFFF", false, 11, HorizontalAlignment.CENTER);
        XSSFCellStyle styleNota = crearEstilo(wb, "F2F2F2", "595959", false, 9, HorizontalAlignment.CENTER);
        XSSFCellStyle styleNormal = crearEstilo(wb, "FFFFFF", "000000", false, 10, HorizontalAlignment.LEFT);
        XSSFCellStyle styleAlterno = crearEstilo(wb, "D6E4F0", "000000", false, 10, HorizontalAlignment.LEFT);
        XSSFCellStyle styleCentrado = crearEstilo(wb, "FFFFFF", "000000", false, 10, HorizontalAlignment.CENTER);
        XSSFCellStyle styleCentradoAlt = crearEstilo(wb, "D6E4F0", "000000", false, 10, HorizontalAlignment.CENTER);

        // Estados
        XSSFCellStyle styleNormalBadge = crearEstilo(wb, "E2EFDA", "1F5C1F", true, 10, HorizontalAlignment.CENTER);
        XSSFCellStyle styleTardeBadge = crearEstilo(wb, "FFF2CC", "BF8F00", true, 10, HorizontalAlignment.CENTER);
        XSSFCellStyle styleAusenteBadge = crearEstilo(wb, "FCE4D6", "C45911", true, 10, HorizontalAlignment.CENTER);
        XSSFCellStyle stylePermisoBadge = crearEstilo(wb, "D6E4F0", "2E75B6", true, 10, HorizontalAlignment.CENTER);
        XSSFCellStyle styleVacacionBadge = crearEstilo(wb, "E2E8F0", "5A67D8", true, 10, HorizontalAlignment.CENTER);

        // ====== TÍTULO ======
        Row fila1 = sheet.createRow(0);
        fila1.setHeightInPoints(40);
        Cell cTitulo = fila1.createCell(0);
        cTitulo.setCellValue("📊  REPORTE DE ASISTENCIAS");
        cTitulo.setCellStyle(styleHeader1);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        // ====== SUBTÍTULO ======
        Row fila2 = sheet.createRow(1);
        fila2.setHeightInPoints(22);
        Cell cSub = fila2.createCell(0);
        cSub.setCellValue(tituloReporte);
        cSub.setCellStyle(styleSubtitulo);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

        // ====== NOTA ======
        Row fila3 = sheet.createRow(2);
        fila3.setHeightInPoints(16);
        Cell cNota = fila3.createCell(0);
        cNota.setCellValue("Generado el " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        cNota.setCellStyle(styleNota);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 7));

        // Espacio
        sheet.createRow(3).setHeightInPoints(8);

        // ====== RESUMEN ======
        Row filaResumen = sheet.createRow(4);
        filaResumen.setHeightInPoints(18);

        long totalNormal = asistencias.stream()
                .filter(a -> a.getEstado().toString().equals("NORMAL")).count();
        long totalTarde = asistencias.stream()
                .filter(a -> a.getEstado().toString().equals("TARDE")).count();
        long totalAusente = asistencias.stream()
                .filter(a -> a.getEstado().toString().equals("AUSENTE")).count();
        long totalPermiso = asistencias.stream()
                .filter(a -> a.getEstado().toString().equals("PERMISO")).count();

        Cell cResumen = filaResumen.createCell(0);
        cResumen.setCellValue(String.format(
                "📈 Resumen: %d registros  |  ✅ %d Normal  |  ⏰ %d Tarde  |  ❌ %d Ausente  |  📄 %d Permiso",
                asistencias.size(), totalNormal, totalTarde, totalAusente, totalPermiso));
        XSSFCellStyle styleResumen = crearEstilo(wb, "E2EFDA", "1F5C1F", false, 9, HorizontalAlignment.LEFT);
        cResumen.setCellStyle(styleResumen);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 7));

        // Espacio
        sheet.createRow(5).setHeightInPoints(8);

        // ====== CABECERAS ======
        String[] cabeceras = {
                "N°", "Empleado", "Documento", "Cargo",
                "Entrada", "Salida", "Horas Trabajadas", "Estado"
        };
        int[] anchos = { 6, 24, 16, 20, 14, 14, 18, 14 };

        Row filaCabeceras = sheet.createRow(6);
        filaCabeceras.setHeightInPoints(30);
        for (int i = 0; i < cabeceras.length; i++) {
            Cell c = filaCabeceras.createCell(i);
            c.setCellValue(cabeceras[i]);
            c.setCellStyle(styleHeader2);
            sheet.setColumnWidth(i, anchos[i] * 256);
        }

        // ====== DATOS ======
        int filaActual = 7;

        if (asistencias.isEmpty()) {
            Row filaVacia = sheet.createRow(filaActual);
            filaVacia.setHeightInPoints(30);
            Cell cVacia = filaVacia.createCell(0);
            cVacia.setCellValue("📄 No hay registros de asistencia para esta fecha");
            XSSFCellStyle styleVacio = crearEstilo(wb, "FFF2CC", "BF8F00", false, 10, HorizontalAlignment.CENTER);
            cVacia.setCellStyle(styleVacio);
            sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, 7));
        } else {
            for (int idx = 0; idx < asistencias.size(); idx++) {
                Asistencia a = asistencias.get(idx);
                boolean alterno = idx % 2 != 0;

                Row fila = sheet.createRow(filaActual);
                fila.setHeightInPoints(22);

                XSSFCellStyle esNorm = alterno ? styleAlterno : styleNormal;
                XSSFCellStyle esCentro = alterno ? styleCentradoAlt : styleCentrado;

                // N°
                Cell cNum = fila.createCell(0);
                cNum.setCellValue(idx + 1);
                cNum.setCellStyle(esCentro);

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

                XSSFCellStyle styleEstado = switch (a.getEstado().toString()) {
                    case "NORMAL" -> styleNormalBadge;
                    case "TARDE" -> styleTardeBadge;
                    case "AUSENTE" -> styleAusenteBadge;
                    case "PERMISO" -> stylePermisoBadge;
                    case "VACACIONES" -> styleVacacionBadge;
                    default -> esCentro;
                };
                cEstado.setCellStyle(styleEstado);

                filaActual++;
            }
        }

        // ====== NOTA LEGAL ======
        Row filaNota = sheet.createRow(filaActual + 2);
        Cell cNotaLegal = filaNota.createCell(0);
        cNotaLegal.setCellValue(
                "ℹ️  Los registros automáticos de ausencia se marcan a las 23:00. " +
                        "Las salidas olvidadas se registran automáticamente a las 23:30 según el horario del empleado.");

        XSSFCellStyle styleNotaLegal = wb.createCellStyle();
        styleNotaLegal.setFillForegroundColor(new XSSFColor(hexToBytes("FFFFFF"), null));
        styleNotaLegal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleNotaLegal.setAlignment(HorizontalAlignment.LEFT);
        styleNotaLegal.setVerticalAlignment(VerticalAlignment.CENTER);
        styleNotaLegal.setWrapText(true);

        XSSFFont fontNota = wb.createFont();
        fontNota.setFontName("Arial");
        fontNota.setItalic(true);
        fontNota.setFontHeightInPoints((short) 8);
        fontNota.setColor(new XSSFColor(hexToBytes("595959"), null));
        styleNotaLegal.setFont(fontNota);

        cNotaLegal.setCellStyle(styleNotaLegal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual + 2, filaActual + 2, 0, 7));

        // ====== CONGELAR PANELES ======
        sheet.createFreezePane(0, 7);

        // ====== ESCRIBIR ======
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
        return new byte[]{ (byte) r, (byte) g, (byte) b };
    }

    private String obtenerNombreMes(int mes) {
        String[] meses = {
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        return mes >= 1 && mes <= 12 ? meses[mes - 1] : "Mes inválido";
    }
}
