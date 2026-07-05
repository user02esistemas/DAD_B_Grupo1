package api.productos;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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
        try {
            return value == null ? 15 : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 15;
        }
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
