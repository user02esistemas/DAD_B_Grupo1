package api.common;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import rmi.auth.AuthServiceRMI;
import rmi.auth.UsuarioServiceRMI;
import rmi.compras.CompraServiceRMI;
import rmi.productos.ProductoServiceRMI;
import rmi.reportes.DashboardServiceRMI;
import rmi.reportes.ReporteServiceRMI;
import rmi.ventas.VentaServiceRMI;

@WebServlet(name = "HealthApiServlet", urlPatterns = {"/api/health"})
public class HealthApiServlet extends HttpServlet {

    private static final String RMI_HOST = "localhost";
    private static final int RMI_PORT = 1099;
    private static final List<String> EXPECTED_SERVICES = Arrays.asList(
            AuthServiceRMI.SERVICE_NAME,
            UsuarioServiceRMI.SERVICE_NAME,
            ProductoServiceRMI.SERVICE_NAME,
            DashboardServiceRMI.SERVICE_NAME,
            ReporteServiceRMI.SERVICE_NAME,
            VentaServiceRMI.SERVICE_NAME,
            CompraServiceRMI.SERVICE_NAME
    );

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("api", "UP");
        health.put("timestamp", LocalDateTime.now().toString());

        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            List<String> published = Arrays.asList(registry.list());
            List<String> missing = new ArrayList<>();
            for (String expected : EXPECTED_SERVICES) {
                if (!published.contains(expected)) {
                    missing.add(expected);
                }
            }

            health.put("rmi", missing.isEmpty() ? "UP" : "PARTIAL");
            health.put("rmiHost", RMI_HOST);
            health.put("rmiPort", RMI_PORT);
            health.put("expectedServices", EXPECTED_SERVICES);
            health.put("publishedServices", published);
            health.put("missingServices", missing);

            int status = missing.isEmpty() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE;
            response.setStatus(status);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Estado de servicios", health)));
        } catch (Exception ex) {
            health.put("rmi", "DOWN");
            health.put("rmiHost", RMI_HOST);
            health.put("rmiPort", RMI_PORT);
            health.put("error", ex.getMessage());

            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write(gson.toJson(ApiResponse.ok("API activa, RMI no disponible", health)));
        }
    }
}
