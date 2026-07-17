package api.inventario;

import api.common.ApiResponse;
import api.config.RMIClientFactory;
import api.websocket.NotificacionBroadcaster;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import rmi.inventario.InventarioServiceRMI;

@WebServlet(name = "InventarioApiServlet", urlPatterns = {"/api/inventario/movimientos", "/api/inventario/ajustar"})
public class InventarioApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try {
            InventarioServiceRMI inventarioService = RMIClientFactory.getInventarioService();
            String termino = request.getParameter("termino");
            int pagina = parseInt(request.getParameter("pagina"), 1);
            int porPagina = parseInt(request.getParameter("porPagina"), 15);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("movimientos", inventarioService.listarMovimientos(termino, pagina, porPagina));
            data.put("total", inventarioService.contarMovimientos(termino));
            data.put("pagina", pagina);
            data.put("porPagina", porPagina);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Movimientos encontrados", data)));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try {
            AjusteRequest ajuste = gson.fromJson(request.getReader(), AjusteRequest.class);
            if (ajuste == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Datos de ajuste requeridos")));
                return;
            }
            RMIClientFactory.getInventarioService().ajustarStock(ajuste.productoId, ajuste.nuevoStock, ajuste.motivo, ajuste.usuarioId);
            NotificacionBroadcaster.enviar(
                    "INVENTARIO",
                    "Stock ajustado",
                    "Producto ID " + ajuste.productoId + " actualizado a " + ajuste.nuevoStock + " unidades"
            );
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Stock ajustado correctamente", null)));
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static class AjusteRequest {
        private Long productoId;
        private int nuevoStock;
        private String motivo;
        private Long usuarioId;
    }
}
