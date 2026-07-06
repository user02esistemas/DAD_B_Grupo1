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
import java.util.List;
import rmi.dto.DashboardResumenDTO;
import rmi.dto.ProductoAlertaDTO;
import rmi.reportes.DashboardServiceRMI;

@WebServlet(name = "DashboardApiServlet", urlPatterns = {
    "/api/dashboard/resumen",
    "/api/dashboard/productos-alerta",
    "/api/dashboard/productos-vencer",
    "/api/dashboard/ventas-turno",
    "/api/dashboard/ventas-usuario-hoy",
    "/api/dashboard/ultimas-ventas-usuario"
})
public class DashboardApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            DashboardServiceRMI dashboardService = RMIClientFactory.getDashboardService();
            String path = request.getServletPath();

            if ("/api/dashboard/productos-alerta".equals(path)) {
                int limite = parseLimite(request.getParameter("limite"));
                List<ProductoAlertaDTO> alertas = dashboardService.obtenerProductosStockBajo(limite);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Productos con alerta", alertas)));
                return;
            }

            if ("/api/dashboard/productos-vencer".equals(path)) {
                int limite = parseLimite(request.getParameter("limite"));
                List<ProductoAlertaDTO> alertas = dashboardService.obtenerProductosPorVencer(limite);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Productos por vencer", alertas)));
                return;
            }

            if ("/api/dashboard/ventas-turno".equals(path)) {
                Long sesionId = parseLong(request.getParameter("sesionId"));
                if (sesionId == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(ApiResponse.error("Sesion requerida")));
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Ventas del turno", dashboardService.obtenerVentasTurno(sesionId))));
                return;
            }

            if ("/api/dashboard/ventas-usuario-hoy".equals(path)) {
                Long usuarioId = parseLong(request.getParameter("usuarioId"));
                if (usuarioId == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(ApiResponse.error("Usuario requerido")));
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Ventas del dia", dashboardService.obtenerVentasDelDiaUsuario(usuarioId))));
                return;
            }

            if ("/api/dashboard/ultimas-ventas-usuario".equals(path)) {
                Long usuarioId = parseLong(request.getParameter("usuarioId"));
                int limite = parseLimite(request.getParameter("limite"));
                if (usuarioId == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(ApiResponse.error("Usuario requerido")));
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Ultimas ventas del usuario", dashboardService.obtenerUltimasVentasUsuario(usuarioId, limite))));
                return;
            }

            DashboardResumenDTO resumen = dashboardService.obtenerResumenCompleto();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Resumen del dashboard", resumen)));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    private int parseLimite(String value) {
        try {
            return value == null ? 10 : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 10;
        }
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
