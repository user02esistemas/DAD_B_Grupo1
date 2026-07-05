package api.reportes;

import api.common.ApiResponse;
import api.config.RMIClientFactory;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import rmi.reportes.ReporteServiceRMI;

@WebServlet(name = "ReporteApiServlet", urlPatterns = {
    "/api/reportes/ventas",
    "/api/reportes/productos-mas-vendidos"
})
public class ReporteApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            ReporteServiceRMI reporteService = RMIClientFactory.getReporteService();
            String path = request.getServletPath();

            if ("/api/reportes/productos-mas-vendidos".equals(path)) {
                int limite = parseLimite(request.getParameter("limite"));
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Productos mas vendidos", reporteService.obtenerProductosMasVendidos(limite))));
                return;
            }

            String desde = validarFecha(request.getParameter("desde"), "desde");
            String hasta = validarFecha(request.getParameter("hasta"), "hasta");
            Map<String, Object> reporte = new LinkedHashMap<>();
            reporte.put("desde", desde);
            reporte.put("hasta", hasta);
            reporte.put("resumen", reporteService.obtenerResumenVentas(desde, hasta));
            reporte.put("ventasPorDia", reporteService.obtenerVentasPorDia(desde, hasta));
            reporte.put("ventas", reporteService.reporteVentasPorFecha(desde, hasta));

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Reporte de ventas", reporte)));
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    private String validarFecha(String value, String nombre) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Parametro requerido: " + nombre);
        }
        try {
            return LocalDate.parse(value).toString();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Formato de fecha invalido para " + nombre + ": use yyyy-MM-dd");
        }
    }

    private int parseLimite(String value) {
        try {
            return value == null ? 10 : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 10;
        }
    }
}
