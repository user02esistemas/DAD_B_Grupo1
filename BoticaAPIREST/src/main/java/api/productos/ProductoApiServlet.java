package api.productos;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import api.common.ApiResponse;
import api.config.RMIClientFactory;
import rmi.dto.ProductoResumenDTO;
import rmi.productos.ProductoServiceRMI;

@WebServlet(name = "ProductoApiServlet", urlPatterns = {"/api/productos", "/api/productos/*"})
public class ProductoApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            ProductoServiceRMI productoService = RMIClientFactory.getProductoService();
            if (esRutaInventarioEstadisticas(request)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Estadisticas de inventario", productoService.obtenerEstadisticasInventario())));
                return;
            }
            if (esRutaInventario(request)) {
                int pagina = parseInt(request.getParameter("pagina"), 1);
                int porPagina = parseInt(request.getParameter("porPagina"), 15);
                String termino = request.getParameter("termino");
                String filtroStock = request.getParameter("filtroStock");
                String filtroVencimiento = request.getParameter("filtroVencimiento");

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("productos", productoService.buscarInventario(termino, filtroStock, filtroVencimiento, pagina, porPagina));
                data.put("total", productoService.contarInventario(termino, filtroStock, filtroVencimiento));
                data.put("pagina", pagina);
                data.put("porPagina", porPagina);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Inventario encontrado", data)));
                return;
            }

            Long id = obtenerId(request);
            if (id != null) {
                ProductoResumenDTO producto = productoService.buscarPorId(id);
                if (producto == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(gson.toJson(ApiResponse.error("Producto no encontrado")));
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Producto encontrado", producto)));
                return;
            }

            String termino = request.getParameter("termino");
            int limite = parseLimite(request.getParameter("limite"));

            List<ProductoResumenDTO> productos = productoService.buscarParaVenta(termino, limite);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Productos encontrados", productos)));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    private int parseLimite(String value) {
        return parseInt(value, 15);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private boolean esRutaInventario(HttpServletRequest request) {
        return "/inventario".equals(request.getPathInfo());
    }

    private boolean esRutaInventarioEstadisticas(HttpServletRequest request) {
        return "/inventario/estadisticas".equals(request.getPathInfo());
    }

    private Long obtenerId(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            return null;
        }
        try {
            return Long.valueOf(pathInfo.substring(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
