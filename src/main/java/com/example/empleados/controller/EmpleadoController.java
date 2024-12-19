/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.empleados.controller;

import com.example.empleados.model.Empleado;
import com.example.empleados.service.EmpleadoService;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    private final EmpleadoService service;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.service = empleadoService;
    }

    @GetMapping
    public String listarEmpleados(Model model) {
        model.addAttribute("empleados", this.service.listarTodos());
        return "empleados"; // Vista de lista de empleados
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("empleado", new Empleado());
        return "formulario"; // Vista del formulario para crear empleado
    }

    @PostMapping
    public String guardarEmpleado(@ModelAttribute Empleado empleado) {
        // Aquí guardamos el empleado con número celular y contraseña
        this.service.guardar(empleado);
        return "redirect:/empleados";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("empleado", this.service.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("ID inválido " + id)));
        return "formulario"; // Vista del formulario para editar empleado
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable Long id) {
        this.service.eliminar(id);
        return "redirect:/empleados";
    }

    // Generar reporte PDF de empleados
    @GetMapping("/reporte/pdf")
    public void generarPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=empleados_reporte.pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

        document.add(new Paragraph("Reporte de Empleados").setBold().setFontSize(18));

        Table table = new Table(5); // Cinco columnas: ID, Nombre, Apellido, Número de Celular, Contraseña
        table.addCell("ID");
        table.addCell("Nombre");
        table.addCell("Apellido");
        table.addCell("Número de Celular");
        table.addCell("Contraseña");

        List<Empleado> empleados = this.service.listarTodos();
        for (Empleado empleado : empleados) {
            table.addCell(empleado.getId().toString());
            table.addCell(empleado.getNombre());
            table.addCell(empleado.getApellido());
            table.addCell(empleado.getNumeroCelular()); // Número de celular
            table.addCell(empleado.getContrasena()); // Contraseña
        }

        document.add(table);
        document.close();
    }

    // Generar reporte Excel de empleados
    @GetMapping("/reporte/excel")
    public void generarExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=empleados_reporte.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Empleados");

        // Crear fila de encabezados
        Row headerRow = sheet.createRow(0);
        String[] columnHeaders = { "ID", "Nombre", "Apellido", "Número de Celular", "Contraseña" };

        // Creando celdas de encabezado
        for (int i = 0; i < columnHeaders.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeaders[i]);
            CellStyle style = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        // Llenar filas con los datos de empleados
        List<Empleado> empleados = this.service.listarTodos();
        int rowIndex = 1;
        for (Empleado empleado : empleados) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(empleado.getId());
            row.createCell(1).setCellValue(empleado.getNombre());
            row.createCell(2).setCellValue(empleado.getApellido());
            row.createCell(3).setCellValue(empleado.getNumeroCelular()); // Número de celular
            row.createCell(4).setCellValue(empleado.getContrasena()); // Contraseña
        }

        // Autoajustar el tamaño de las columnas
        for (int i = 0; i < columnHeaders.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Escribir el archivo Excel en el flujo de salida
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
