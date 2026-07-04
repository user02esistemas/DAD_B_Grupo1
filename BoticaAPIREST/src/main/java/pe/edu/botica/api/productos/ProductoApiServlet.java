package pe.edu.botica.api.productos;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import pe.edu.botica.api.common.ApiResponse;
import pe.edu.botica.api.config.RMIClientFactory;
import pe.edu.botica.rmi.dto.ProductoResumenDTO;
import pe.edu.botica.rmi.productos.ProductoServiceRMI;

@WebServlet(name = "ProductoApiServlet", urlPatterns = {"/api/productos"})
public class ProductoApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            String termino = request.getParameter("termino");
            int limite = parseLimite(request.getParameter("limite"));

            ProductoServiceRMI productoService = RMIClientFactory.getProductoService();
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
}
