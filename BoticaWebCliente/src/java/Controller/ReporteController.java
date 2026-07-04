package controller;

import DAO.ReporteDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para Reportes Maneja las solicitudes de reportes del sistema
 */
@WebServlet(name = "ReporteController", urlPatterns = {"/ReporteController"})
public class ReporteController extends HttpServlet {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            action = "vencimiento";
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            switch (action) {
                // ========== REPORTE VENCIMIENTO ==========
                case "vencimiento":
                    out.print(gson.toJson(reporteVencimiento(request)));
                    break;
                case "vencimientoRango":
                    out.print(gson.toJson(reporteVencimientoRango(request)));
                    break;
                case "criticidad":
                    out.print(gson.toJson(reporteDAO.contarPorCriticidad()));
                    break;

                // ========== REPORTE CAJA ==========
                case "sesionesCaja":
                    out.print(gson.toJson(reporteSesionesCaja(request)));
                    break;
                case "detalleSesion":
                    Long sesionId = getLongParam(request, "sesionId", null);
                    if (sesionId != null) {
                        Map<String, Object> detalle = reporteDAO.obtenerDetalleSesionCaja(sesionId);
                        detalle.put("ventas", reporteDAO.obtenerVentasSesion(sesionId));
                        out.print(gson.toJson(detalle));
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\": \"Se requiere sesionId\"}");
                    }
                    break;

                // ========== REPORTE VENTAS ==========
                case "resumenVentas":
                    out.print(gson.toJson(reporteResumenVentas(request)));
                    break;
                case "listarVentas":
                    out.print(gson.toJson(reporteListarVentas(request)));
                    break;
                case "ventasPorDia":
                    out.print(gson.toJson(reporteVentasPorDia(request)));
                    break;

                // ========== DATOS AUXILIARES ==========
                case "usuarios":
                    out.print(gson.toJson(reporteDAO.listarUsuariosActivos()));
                    break;

                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Acción no válida\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            out.print(gson.toJson(error));
            e.printStackTrace();
        }
    }

    // =====================================================
    //     MÉTODOS DE REPORTE
    // =====================================================
    private Map<String, Object> reporteVencimiento(HttpServletRequest request) {
        int diasDesde = getIntParam(request, "diasDesde", 0);
        int diasHasta = getIntParam(request, "diasHasta", 30);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("productos", reporteDAO.obtenerProductosPorVencer(diasDesde, diasHasta));
        resultado.put("criticidad", reporteDAO.contarPorCriticidad());
        return resultado;
    }

    private Map<String, Object> reporteVencimientoRango(HttpServletRequest request) throws ParseException {
        String fechaDesdeStr = request.getParameter("fechaDesde");
        String fechaHastaStr = request.getParameter("fechaHasta");

        Date fechaDesde = fechaDesdeStr != null ? sdf.parse(fechaDesdeStr) : new Date();
        Date fechaHasta = fechaHastaStr != null ? sdf.parse(fechaHastaStr) : sumarDias(new Date(), 30);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("productos", reporteDAO.obtenerProductosPorVencerRango(fechaDesde, fechaHasta));
        resultado.put("criticidad", reporteDAO.contarPorCriticidad());
        return resultado;
    }

    private List<Map<String, Object>> reporteSesionesCaja(HttpServletRequest request) throws ParseException {
        String fechaDesdeStr = request.getParameter("fechaDesde");
        String fechaHastaStr = request.getParameter("fechaHasta");
        Long usuarioId = getLongParam(request, "usuarioId", null);

        Date fechaDesde = fechaDesdeStr != null && !fechaDesdeStr.isEmpty() ? sdf.parse(fechaDesdeStr) : null;
        Date fechaHasta = fechaHastaStr != null && !fechaHastaStr.isEmpty() ? sdf.parse(fechaHastaStr) : null;

        return reporteDAO.listarSesionesCaja(fechaDesde, fechaHasta, usuarioId);
    }

    private Map<String, Object> reporteResumenVentas(HttpServletRequest request) throws ParseException {
        String fechaDesdeStr = request.getParameter("fechaDesde");
        String fechaHastaStr = request.getParameter("fechaHasta");

        Date fechaDesde = fechaDesdeStr != null && !fechaDesdeStr.isEmpty()
                ? sdf.parse(fechaDesdeStr) : obtenerPrimerDiaMes();
        Date fechaHasta = fechaHastaStr != null && !fechaHastaStr.isEmpty()
                ? sdf.parse(fechaHastaStr) : new Date();

        Map<String, Object> resultado = reporteDAO.obtenerResumenVentas(fechaDesde, fechaHasta);
        resultado.put("ventasPorDia", reporteDAO.obtenerVentasPorDia(fechaDesde, fechaHasta));
        return resultado;
    }

    private Map<String, Object> reporteListarVentas(HttpServletRequest request) throws ParseException {
        String fechaDesdeStr = request.getParameter("fechaDesde");
        String fechaHastaStr = request.getParameter("fechaHasta");

        Date fechaDesde = fechaDesdeStr != null && !fechaDesdeStr.isEmpty()
                ? sdf.parse(fechaDesdeStr) : obtenerPrimerDiaMes();
        Date fechaHasta = fechaHastaStr != null && !fechaHastaStr.isEmpty()
                ? sdf.parse(fechaHastaStr) : new Date();

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("resumen", reporteDAO.obtenerResumenVentas(fechaDesde, fechaHasta));
        resultado.put("ventas", reporteDAO.listarVentas(fechaDesde, fechaHasta));
        return resultado;
    }

    private List<Map<String, Object>> reporteVentasPorDia(HttpServletRequest request) throws ParseException {
        String fechaDesdeStr = request.getParameter("fechaDesde");
        String fechaHastaStr = request.getParameter("fechaHasta");

        Date fechaDesde = fechaDesdeStr != null && !fechaDesdeStr.isEmpty()
                ? sdf.parse(fechaDesdeStr) : sumarDias(new Date(), -7);
        Date fechaHasta = fechaHastaStr != null && !fechaHastaStr.isEmpty()
                ? sdf.parse(fechaHastaStr) : new Date();

        return reporteDAO.obtenerVentasPorDia(fechaDesde, fechaHasta);
    }

    // =====================================================
    //     MÉTODOS AUXILIARES
    // =====================================================
    private int getIntParam(HttpServletRequest request, String param, int defaultValue) {
        String value = request.getParameter(param);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private Long getLongParam(HttpServletRequest request, String param, Long defaultValue) {
        String value = request.getParameter(param);
        if (value != null && !value.isEmpty()) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private Date sumarDias(Date fecha, int dias) {
        long tiempo = fecha.getTime();
        tiempo += dias * 24L * 60L * 60L * 1000L;
        return new Date(tiempo);
    }

    private Date obtenerPrimerDiaMes() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        return cal.getTime();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
