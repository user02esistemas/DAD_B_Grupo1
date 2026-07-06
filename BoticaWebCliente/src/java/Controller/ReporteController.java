package controller;

import DTO.UsuarioDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import integration.api.ReporteApiClient;
import integration.api.UsuarioApiClient;
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
import java.util.Map;

/**
 * Controller para Reportes Maneja las solicitudes de reportes del sistema
 */
@WebServlet(name = "ReporteController", urlPatterns = {"/ReporteController"})
public class ReporteController extends HttpServlet {

    private final ReporteApiClient reporteApiClient = new ReporteApiClient();
    private final UsuarioApiClient usuarioApiClient = new UsuarioApiClient();
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
                    out.print(gson.toJson(reporteApiClient.obtenerCriticidadVencimientos()));
                    break;

                // ========== REPORTE CAJA ==========
                case "sesionesCaja":
                    out.print(gson.toJson(reporteSesionesCaja(request)));
                    break;
                case "detalleSesion":
                    Long sesionId = getLongParam(request, "sesionId", null);
                    if (sesionId != null) {
                        out.print(gson.toJson(reporteApiClient.obtenerDetalleSesionCaja(sesionId)));
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\": \"Se requiere sesionId\"}");
                    }
                    break;

                // ========== REPORTE VENTAS ==========
                case "resumenVentas":
                    out.print(gson.toJson(reporteResumenVentasApi(request)));
                    break;
                case "listarVentas":
                    out.print(gson.toJson(reporteListarVentasApi(request)));
                    break;
                case "ventasPorDia":
                    out.print(gson.toJson(reporteVentasPorDiaApi(request)));
                    break;

                // ========== DATOS AUXILIARES ==========
                case "usuarios":
                    out.print(gson.toJson(listarUsuariosActivosApi()));
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
    private JsonObject reporteVencimiento(HttpServletRequest request) throws IOException {
        int diasDesde = getIntParam(request, "diasDesde", 0);
        int diasHasta = getIntParam(request, "diasHasta", 30);
        return reporteApiClient.obtenerReporteVencimientos(diasDesde, diasHasta);
    }

    private JsonObject reporteVencimientoRango(HttpServletRequest request) throws ParseException, IOException {
        String fechaDesdeStr = request.getParameter("fechaDesde");
        String fechaHastaStr = request.getParameter("fechaHasta");

        Date fechaDesde = fechaDesdeStr != null ? sdf.parse(fechaDesdeStr) : new Date();
        Date fechaHasta = fechaHastaStr != null ? sdf.parse(fechaHastaStr) : sumarDias(new Date(), 30);

        return reporteApiClient.obtenerReporteVencimientosRango(sdf.format(fechaDesde), sdf.format(fechaHasta));
    }

    private JsonArray reporteSesionesCaja(HttpServletRequest request) throws IOException {
        String fechaDesdeStr = request.getParameter("fechaDesde");
        String fechaHastaStr = request.getParameter("fechaHasta");
        Long usuarioId = getLongParam(request, "usuarioId", null);
        return reporteApiClient.obtenerSesionesCaja(fechaDesdeStr, fechaHastaStr, usuarioId).getAsJsonArray("sesiones");
    }

    private JsonObject reporteResumenVentasApi(HttpServletRequest request) throws ParseException, IOException {
        JsonObject data = obtenerReporteVentasApi(request, obtenerPrimerDiaMes(), new Date());
        JsonObject resultado = data.getAsJsonObject("resumen").deepCopy();
        resultado.add("ventasPorDia", normalizarVentasPorDia(data.getAsJsonArray("ventasPorDia")));
        return resultado;
    }

    private JsonObject reporteListarVentasApi(HttpServletRequest request) throws ParseException, IOException {
        JsonObject data = obtenerReporteVentasApi(request, obtenerPrimerDiaMes(), new Date());
        JsonObject resultado = new JsonObject();
        resultado.add("resumen", data.getAsJsonObject("resumen"));
        resultado.add("ventas", normalizarVentas(data.getAsJsonArray("ventas")));
        return resultado;
    }

    private JsonArray reporteVentasPorDiaApi(HttpServletRequest request) throws ParseException, IOException {
        JsonObject data = obtenerReporteVentasApi(request, sumarDias(new Date(), -7), new Date());
        return normalizarVentasPorDia(data.getAsJsonArray("ventasPorDia"));
    }

    private JsonObject obtenerReporteVentasApi(HttpServletRequest request, Date fechaDesdeDefault, Date fechaHastaDefault)
            throws ParseException, IOException {
        String fechaDesdeStr = request.getParameter("fechaDesde");
        String fechaHastaStr = request.getParameter("fechaHasta");

        Date fechaDesde = fechaDesdeStr != null && !fechaDesdeStr.isEmpty()
                ? sdf.parse(fechaDesdeStr) : fechaDesdeDefault;
        Date fechaHasta = fechaHastaStr != null && !fechaHastaStr.isEmpty()
                ? sdf.parse(fechaHastaStr) : fechaHastaDefault;

        return reporteApiClient.obtenerReporteVentas(sdf.format(fechaDesde), sdf.format(fechaHasta));
    }

    private JsonArray normalizarVentas(JsonArray ventasApi) {
        JsonArray ventas = new JsonArray();
        if (ventasApi == null) {
            return ventas;
        }
        for (JsonElement element : ventasApi) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            JsonObject venta = new JsonObject();
            copiar(venta, "id", item, "id");
            copiar(venta, "numero", item, "numeroTransaccion");
            copiar(venta, "fecha", item, "fecha");
            copiar(venta, "total", item, "total");
            copiar(venta, "metodoPago", item, "metodoPago");
            copiar(venta, "montoEfectivo", item, "montoEfectivo");
            copiar(venta, "montoVirtual", item, "montoVirtual");
            copiar(venta, "vuelto", item, "vuelto");
            copiar(venta, "cajero", item, "usuarioNombre");
            ventas.add(venta);
        }
        return ventas;
    }

    private JsonArray normalizarVentasPorDia(JsonArray ventasPorDiaApi) {
        JsonArray ventasPorDia = new JsonArray();
        if (ventasPorDiaApi == null) {
            return ventasPorDia;
        }
        for (JsonElement element : ventasPorDiaApi) {
            if (element.isJsonObject()) {
                ventasPorDia.add(element.getAsJsonObject());
            }
        }
        return ventasPorDia;
    }

    private void copiar(JsonObject destino, String destinoKey, JsonObject origen, String origenKey) {
        if (origen.has(origenKey) && !origen.get(origenKey).isJsonNull()) {
            destino.add(destinoKey, origen.get(origenKey));
        }
    }

    private JsonArray listarUsuariosActivosApi() throws IOException {
        JsonArray usuarios = new JsonArray();
        for (UsuarioDTO usuario : usuarioApiClient.listarTodos()) {
            if (!usuario.isActivo()) {
                continue;
            }
            JsonObject item = new JsonObject();
            item.addProperty("id", usuario.getId());
            item.addProperty("nombre", usuario.getNombreCompleto());
            usuarios.add(item);
        }
        return usuarios;
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
