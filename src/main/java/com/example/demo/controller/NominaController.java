package com.example.demo.controller;

import com.example.demo.model.Asistencia;
import com.example.demo.model.Empleado;
import com.example.demo.model.EstadoAsistencia;
import com.example.demo.repository.AsistenciaRepository;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.HoraExtraRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/nomina")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NominaController {

    private final EmpleadoRepository empleadoRepository;
    private final HoraExtraRepository horaExtraRepository;
    private final AsistenciaRepository asistenciaRepository; // ⭐ AGREGADO

    // =========================================================
    // EXPORTAR NÓMINA EN EXCEL (.xlsx) — BIEN FORMATEADO
    // =========================================================
    @GetMapping("/exportar")
    public void exportarNomina(HttpServletResponse response, HttpSession session) throws Exception {

        // Solo ADMIN y RRHH pueden exportar
        Empleado usuarioLogueado = (Empleado) session.getAttribute("empleado");
        if (usuarioLogueado == null) {
            response.sendError(401, "No autenticado");
            return;
        }
        if (!"ADMIN".equals(usuarioLogueado.getRol().toString()) &&
                !"RRHH".equals(usuarioLogueado.getRol().toString())) {
            response.sendError(403, "No tienes permiso para exportar nómina");
            return;
        }

        // Preparar respuesta como .xlsx
        String mesAnio = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM_yyyy"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=nomina_" + mesAnio + ".xlsx");

        // Crear workbook
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Nómina " + LocalDate.now().format(
                DateTimeFormatter.ofPattern("MMMM yyyy")));

        // ====== DEFINIR ESTILOS ======
        // Color azul oscuro para el header
        XSSFCellStyle styleHeader1 = crearEstilo(wb, "1F3864", "FFFFFF", true, 16, HorizontalAlignment.CENTER);
        // Color azul medio para cabeceras de columnas
        XSSFCellStyle styleHeader2 = crearEstilo(wb, "2E75B6", "FFFFFF", true, 10, HorizontalAlignment.CENTER);
        // Subtítulo
        XSSFCellStyle styleSubtitulo = crearEstilo(wb, "2E75B6", "FFFFFF", false, 11, HorizontalAlignment.CENTER);
        // Nota superior
        XSSFCellStyle styleNota = crearEstilo(wb, "F2F2F2", "595959", false, 9, HorizontalAlignment.CENTER);
        // Fila normal blanca
        XSSFCellStyle styleNormal = crearEstilo(wb, "FFFFFF", "000000", false, 10, HorizontalAlignment.LEFT);
        // Fila normal azul claro (filas alternas)
        XSSFCellStyle styleAlterno = crearEstilo(wb, "D6E4F0", "000000", false, 10, HorizontalAlignment.LEFT);
        // Fila de totales
        XSSFCellStyle styleTotales = crearEstilo(wb, "1F3864", "FFFFFF", true, 10, HorizontalAlignment.RIGHT);
        // Números (dinero) - fila normal
        XSSFCellStyle styleMoneda = crearEstilo(wb, "FFFFFF", "000000", false, 10, HorizontalAlignment.RIGHT);
        styleMoneda.setDataFormat(wb.createDataFormat().getFormat("$#,##0"));
        // Números (dinero) - fila alterna
        XSSFCellStyle styleMonedaAlt = crearEstilo(wb, "D6E4F0", "000000", false, 10, HorizontalAlignment.RIGHT);
        styleMonedaAlt.setDataFormat(wb.createDataFormat().getFormat("$#,##0"));
        // Total (verde)
        XSSFCellStyle styleTotal = crearEstilo(wb, "E2EFDA", "1F5C1F", true, 10, HorizontalAlignment.RIGHT);
        styleTotal.setDataFormat(wb.createDataFormat().getFormat("$#,##0"));
        // Total alterno (verde más claro)
        XSSFCellStyle styleTotalAlt = crearEstilo(wb, "C6EFCE", "1F5C1F", true, 10, HorizontalAlignment.RIGHT);
        styleTotalAlt.setDataFormat(wb.createDataFormat().getFormat("$#,##0"));
        // Tardanzas (amarillo)
        XSSFCellStyle styleTardanza = crearEstilo(wb, "FFF2CC", "BF8F00", true, 10, HorizontalAlignment.CENTER);
        // Sin tardanzas (centrado)
        XSSFCellStyle styleCentrado = crearEstilo(wb, "FFFFFF", "000000", false, 10, HorizontalAlignment.CENTER);
        XSSFCellStyle styleCentradoAlt = crearEstilo(wb, "D6E4F0", "000000", false, 10, HorizontalAlignment.CENTER);
        // Totales dinero
        XSSFCellStyle styleTotalesMoneda = crearEstilo(wb, "1F3864", "FFFFFF", true, 10, HorizontalAlignment.RIGHT);
        styleTotalesMoneda.setDataFormat(wb.createDataFormat().getFormat("$#,##0"));

        // ====== FILA 1: TÍTULO PRINCIPAL ======
        Row fila1 = sheet.createRow(0);
        fila1.setHeightInPoints(40);
        Cell cTitulo = fila1.createCell(0);
        cTitulo.setCellValue("SISTEMA DE GESTIÓN RRHH");
        cTitulo.setCellStyle(styleHeader1);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

        // ====== FILA 2: SUBTÍTULO MES/AÑO ======
        String mesActual = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")).toUpperCase();
        Row fila2 = sheet.createRow(1);
        fila2.setHeightInPoints(22);
        Cell cSub = fila2.createCell(0);
        cSub.setCellValue("REPORTE DE NÓMINA — " + mesActual);
        cSub.setCellStyle(styleSubtitulo);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 9));

        // ====== FILA 3: NOTA ======
        Row fila3 = sheet.createRow(2);
        fila3.setHeightInPoints(16);
        Cell cNota = fila3.createCell(0);
        cNota.setCellValue("Generado automáticamente el " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        cNota.setCellStyle(styleNota);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 9));

        // ====== FILA 4: ESPACIO ======
        sheet.createRow(3).setHeightInPoints(8);

        // ====== FILA 5: CABECERAS ======
        String[] cabeceras = {
                "N°", "Empleado", "Cargo", "F. Ingreso",
                "Días Trab.", "Tardanzas", "Salario Base",
                "H. Extras (h)", "Valor Extras", "TOTAL A PAGAR"
        };
        int[] anchos = { 6, 24, 20, 13, 11, 11, 16, 14, 16, 18 };

        Row filaCabeceras = sheet.createRow(4);
        filaCabeceras.setHeightInPoints(30);
        for (int i = 0; i < cabeceras.length; i++) {
            Cell c = filaCabeceras.createCell(i);
            c.setCellValue(cabeceras[i]);
            c.setCellStyle(styleHeader2);
            sheet.setColumnWidth(i, anchos[i] * 256);
        }

        // ====== CALCULAR FECHAS DEL MES ======
        LocalDate hoy = LocalDate.now();
        LocalDate primerDia = LocalDate.of(hoy.getYear(), hoy.getMonth(), 1);
        LocalDate ultimoDia = hoy.withDayOfMonth(hoy.lengthOfMonth());

        // ⭐ IMPORTANTE: Solo hasta HOY (no todo el mes futuro)
        LocalDate fechaHasta = hoy.isBefore(ultimoDia) ? hoy : ultimoDia;

        // ====== FILAS DE DATOS ======
        List<Empleado> empleados = empleadoRepository.findAll()
                .stream()
                .filter(Empleado::isActivo)
                .sorted((a, b) -> a.getNombre().compareTo(b.getNombre()))
                .toList();

        int filaInicio = 5;
        int filaActual = filaInicio;

        for (int idx = 0; idx < empleados.size(); idx++) {
            Empleado emp = empleados.get(idx);
            boolean alterno = idx % 2 != 0;

            double salario = emp.getCargo() != null ? emp.getCargo().getSalarioBase() : 0;
            double horasExtras = horaExtraRepository.horasExtraMesActual(emp);
            double valorExtras = horaExtraRepository.sumHorasExtras(emp.getId());

            // ⭐ CALCULAR DÍAS TRABAJADOS REALES (PRESENTE + TARDANZA)
            List<Asistencia> asistencias = asistenciaRepository
                    .findByEmpleadoAndFechaBetween(emp, primerDia, fechaHasta);

            long diasTrabajados = asistencias.stream()
                    .filter(a -> a.getEstado() == EstadoAsistencia.NORMAL ||
                            a.getEstado() == EstadoAsistencia.TARDE)
                    .count();

            // ⭐ CALCULAR TARDANZAS DEL MES
            long tardanzas = asistencias.stream()
                    .filter(a -> a.getEstado() == EstadoAsistencia.TARDE)
                    .count();

            Row fila = sheet.createRow(filaActual);
            fila.setHeightInPoints(20);

            XSSFCellStyle esNorm  = alterno ? styleAlterno   : styleNormal;
            XSSFCellStyle esMoneda = alterno ? styleMonedaAlt : styleMoneda;
            XSSFCellStyle esCentro = alterno ? styleCentradoAlt : styleCentrado;
            XSSFCellStyle esTotal  = alterno ? styleTotalAlt  : styleTotal;

            // N°
            Cell cNum = fila.createCell(0);
            cNum.setCellValue(idx + 1);
            cNum.setCellStyle(esCentro);

            // Empleado
            Cell cNombre = fila.createCell(1);
            cNombre.setCellValue(emp.getNombre() + " " + emp.getApellido());
            cNombre.setCellStyle(esNorm);

            // Cargo
            Cell cCargo = fila.createCell(2);
            cCargo.setCellValue(emp.getCargo() != null ? emp.getCargo().getNombre() : "Sin cargo");
            cCargo.setCellStyle(esNorm);

            // Fecha ingreso
            Cell cIngreso = fila.createCell(3);
            cIngreso.setCellValue(emp.getFechaIngreso() != null
                    ? emp.getFechaIngreso().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
            cIngreso.setCellStyle(esCentro);

            // ⭐ Días trabajados REALES
            Cell cDias = fila.createCell(4);
            cDias.setCellValue(diasTrabajados);
            cDias.setCellStyle(esCentro);

            // ⭐ Tardanzas REALES
            Cell cTardanzas = fila.createCell(5);
            cTardanzas.setCellValue(tardanzas);
            cTardanzas.setCellStyle(tardanzas > 0 ? styleTardanza : esCentro);

            // Salario base
            Cell cSalario = fila.createCell(6);
            cSalario.setCellValue(salario);
            cSalario.setCellStyle(esMoneda);

            // Horas extras (cantidad)
            Cell cHorasExtra = fila.createCell(7);
            cHorasExtra.setCellValue(horasExtras);
            cHorasExtra.setCellStyle(esCentro);

            // Valor extras
            Cell cValorExtras = fila.createCell(8);
            cValorExtras.setCellValue(valorExtras);
            cValorExtras.setCellStyle(esMoneda);

            // Total (fórmula Excel)
            int excelRow = filaActual + 1;
            Cell cTotalPagar = fila.createCell(9);
            cTotalPagar.setCellFormula("G" + excelRow + "+I" + excelRow);
            cTotalPagar.setCellStyle(esTotal);

            filaActual++;
        }

        // ====== FILA TOTALES ======
        Row filaTotales = sheet.createRow(filaActual);
        filaTotales.setHeightInPoints(22);

        // Merge "TOTALES"
        Cell cTotalLabel = filaTotales.createCell(0);
        cTotalLabel.setCellValue("TOTALES");
        cTotalLabel.setCellStyle(styleTotales);
        sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, 5));
        for (int i = 1; i <= 5; i++) {
            Cell c = filaTotales.createCell(i);
            c.setCellStyle(styleTotales);
        }

        int ultimaFila = filaActual; // 0-indexed
        int excelInicio = filaInicio + 1; // Excel 1-indexed
        int excelFin    = filaActual;     // Excel 1-indexed (última data row)

        // Salario total
        Cell cSalTotal = filaTotales.createCell(6);
        cSalTotal.setCellFormula("SUM(G" + excelInicio + ":G" + excelFin + ")");
        cSalTotal.setCellStyle(styleTotalesMoneda);

        // Horas extras total
        Cell cHExtTotal = filaTotales.createCell(7);
        cHExtTotal.setCellFormula("SUM(H" + excelInicio + ":H" + excelFin + ")");
        cHExtTotal.setCellStyle(styleTotales);

        // Valor extras total
        Cell cVExtTotal = filaTotales.createCell(8);
        cVExtTotal.setCellFormula("SUM(I" + excelInicio + ":I" + excelFin + ")");
        cVExtTotal.setCellStyle(styleTotalesMoneda);

        // Total a pagar
        Cell cTotalPagarTotal = filaTotales.createCell(9);
        cTotalPagarTotal.setCellFormula("SUM(J" + excelInicio + ":J" + excelFin + ")");
        cTotalPagarTotal.setCellStyle(styleTotalesMoneda);

        // ====== NOTA LEGAL ======
        Row filaNota = sheet.createRow(filaActual + 2);
        Cell cNotaLegal = filaNota.createCell(0);
        cNotaLegal.setCellValue(
                "⚖️  Legislación Colombiana: " +
                        "Horas extras diurnas +25%  |  Nocturnas +75%  |  " +
                        "Festivas +75%  |  Salario mínimo 2026: $1.423.500");
        XSSFCellStyle styleNotaLegal = crearEstilo(wb, "FFFFFF", "595959", false, 8, HorizontalAlignment.LEFT);

        XSSFFont fontNota = wb.createFont();
        fontNota.setItalic(true);
        styleNotaLegal.setFont(fontNota);

        cNotaLegal.setCellStyle(styleNotaLegal);
        sheet.addMergedRegion(new CellRangeAddress(filaActual + 2, filaActual + 2, 0, 9));

        // ====== CONGELAR PRIMERA FILA DE DATOS ======
        sheet.createFreezePane(0, 5);

        // ====== ESCRIBIR RESPUESTA ======
        wb.write(response.getOutputStream());
        wb.close();
    }

    // ======================================================
    // HELPER: Crear estilo de celda
    // ======================================================
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

        // Borde sutil
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


    // =========================================================
// EXPORTAR MI NÓMINA (PDF - Solo para el empleado logueado)
// =========================================================
    @GetMapping("/mi-nomina/exportar")
    public void exportarMiNomina(HttpServletResponse response, HttpSession session) throws Exception {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            response.sendError(401, "No autenticado");
            return;
        }

        // Preparar respuesta como .xlsx
        LocalDate hoy = LocalDate.now();
        String mesAnio = hoy.format(DateTimeFormatter.ofPattern("MMMM_yyyy"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=mi_nomina_" + mesAnio + ".xlsx");

        // Crear workbook
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Mi Nómina");

        // Estilos
        XSSFCellStyle styleHeader = crearEstilo(wb, "1F3864", "FFFFFF", true, 14, HorizontalAlignment.CENTER);
        XSSFCellStyle styleLabel = crearEstilo(wb, "2E75B6", "FFFFFF", true, 10, HorizontalAlignment.LEFT);
        XSSFCellStyle styleValue = crearEstilo(wb, "FFFFFF", "000000", false, 10, HorizontalAlignment.RIGHT);
        XSSFCellStyle styleMoneda = crearEstilo(wb, "E2EFDA", "000000", false, 11, HorizontalAlignment.RIGHT);
        styleMoneda.setDataFormat(wb.createDataFormat().getFormat("$#,##0"));
        XSSFCellStyle styleTotal = crearEstilo(wb, "1F3864", "FFFFFF", true, 12, HorizontalAlignment.RIGHT);
        styleTotal.setDataFormat(wb.createDataFormat().getFormat("$#,##0"));

        int rowNum = 0;

        // Título
        Row row0 = sheet.createRow(rowNum++);
        row0.setHeightInPoints(35);
        Cell cellTitulo = row0.createCell(0);
        cellTitulo.setCellValue("MI NÓMINA - " + hoy.format(DateTimeFormatter.ofPattern("MMMM yyyy")).toUpperCase());
        cellTitulo.setCellStyle(styleHeader);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        rowNum++; // Espacio

        // Información personal
        agregarFila(sheet, rowNum++, "Empleado:", empleado.getNombre() + " " + empleado.getApellido(), styleLabel, styleValue);
        agregarFila(sheet, rowNum++, "Cargo:", empleado.getCargo() != null ? empleado.getCargo().getNombre() : "Sin cargo", styleLabel, styleValue);
        agregarFila(sheet, rowNum++, "Documento:", empleado.getDocumento(), styleLabel, styleValue);

        rowNum++; // Espacio

        // Calcular datos
        LocalDate primerDia = LocalDate.of(hoy.getYear(), hoy.getMonth(), 1);
        List<Asistencia> asistencias = asistenciaRepository
                .findByEmpleadoAndFechaBetween(empleado, primerDia, hoy);

        long diasTrabajados = asistencias.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.NORMAL ||
                        a.getEstado() == EstadoAsistencia.TARDE)
                .count();

        long tardanzas = asistencias.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.TARDE)
                .count();

        double salarioBase = empleado.getCargo() != null ? empleado.getCargo().getSalarioBase() : 0.0;
        double horasExtras = horaExtraRepository.horasExtraMesActual(empleado);
        double valorExtras = horaExtraRepository.sumHorasExtras(empleado.getId());
        double totalPagar = salarioBase + valorExtras;

        // Asistencia
        agregarFila(sheet, rowNum++, "Días trabajados:", String.valueOf(diasTrabajados), styleLabel, styleValue);
        agregarFila(sheet, rowNum++, "Tardanzas:", String.valueOf(tardanzas), styleLabel, styleValue);

        rowNum++; // Espacio

        // Salarios
        Row rowSalBase = sheet.createRow(rowNum++);
        Cell cellSalLabel = rowSalBase.createCell(0);
        cellSalLabel.setCellValue("Salario Base:");
        cellSalLabel.setCellStyle(styleLabel);
        Cell cellSalValue = rowSalBase.createCell(1);
        cellSalValue.setCellValue(salarioBase);
        cellSalValue.setCellStyle(styleMoneda);

        agregarFila(sheet, rowNum++, "Horas extras:", String.format("%.2f h", horasExtras), styleLabel, styleValue);

        Row rowExtras = sheet.createRow(rowNum++);
        Cell cellExtLabel = rowExtras.createCell(0);
        cellExtLabel.setCellValue("Valor horas extras:");
        cellExtLabel.setCellStyle(styleLabel);
        Cell cellExtValue = rowExtras.createCell(1);
        cellExtValue.setCellValue(valorExtras);
        cellExtValue.setCellStyle(styleMoneda);

        rowNum++; // Espacio

        // Total
        Row rowTotal = sheet.createRow(rowNum++);
        rowTotal.setHeightInPoints(25);
        Cell cellTotalLabel = rowTotal.createCell(0);
        cellTotalLabel.setCellValue("TOTAL A RECIBIR:");
        cellTotalLabel.setCellStyle(styleTotal);
        Cell cellTotalValue = rowTotal.createCell(1);
        cellTotalValue.setCellValue(totalPagar);
        cellTotalValue.setCellStyle(styleTotal);

        // Ajustar anchos
        sheet.setColumnWidth(0, 25 * 256);
        sheet.setColumnWidth(1, 20 * 256);

        // Escribir
        wb.write(response.getOutputStream());
        wb.close();
    }

    // Helper para agregar filas simples
    private void agregarFila(XSSFSheet sheet, int rowNum, String label, String value,
                             XSSFCellStyle styleLabel, XSSFCellStyle styleValue) {
        Row row = sheet.createRow(rowNum);
        Cell cellLabel = row.createCell(0);
        cellLabel.setCellValue(label);
        cellLabel.setCellStyle(styleLabel);
        Cell cellValue = row.createCell(1);
        cellValue.setCellValue(value);
        cellValue.setCellStyle(styleValue);
    }
}

