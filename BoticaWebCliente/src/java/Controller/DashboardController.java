package controller;

import DAO.DashboardDAO;
import DTO.UsuarioDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import integration.api.CajaApiClient;
import integration.api.DashboardApiClient;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para el Dashboard
 * Proporciona datos estadísticos del sistema
 */
@WebServlet(name = "DashboardController", urlPatterns = {"/DashboardController"})
public class DashboardController extends HttpServlet {

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final DashboardApiClient dashboardApiClient = new DashboardApiClient();
    private final CajaApiClient cajaApiClient = new CajaApiClient();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "resumen";
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            switch (action) {
                case "resumen":
                    out.print(gson.toJson(dashboardApiClient.obtenerResumen()));
                    break;
                case "topProductos":
                    out.print(gson.toJson(dashboardApiClient.obtenerResumen().getAsJsonArray("topProductos")));
                    break;
                case "topProductosMes":
                    out.print(gson.toJson(dashboardApiClient.obtenerResumen().getAsJsonArray("topProductosMes")));
                    break;
                case "ventasSemana":
                    out.print(gson.toJson(dashboardApiClient.obtenerResumen().getAsJsonArray("ventasSemana")));
                    break;
                case "ultimasVentas":
                    out.print(gson.toJson(dashboardApiClient.obtenerResumen().getAsJsonArray("ultimasVentas")));
                    break;
                case "productosStockBajo":
                    int limiteStock = getIntParam(request, "limite", 5);
                    out.print(gson.toJson(dashboardApiClient.obtenerProductosStockBajo(limiteStock)));
                    break;
                case "productosPorVencer":
                    int limiteVencer = getIntParam(request, "limite", 5);
                    out.print(gson.toJson(dashboardApiClient.obtenerProductosPorVencer(limiteVencer)));
                    break;
                case "alertas":
                    out.print(gson.toJson(obtenerAlertas()));
                    break;
                    
                // =====================================================
                //     ENDPOINTS PARA FARMACÉUTICO (Mi Caja)
                // =====================================================
                case "miTurno":
                    out.print(gson.toJson(obtenerMiTurno(request)));
                    break;
                case "sesionActiva":
                    out.print(gson.toJson(obtenerSesionActiva(request)));
                    break;
                case "ventasTurno":
                    Long sesionId = getLongParam(request, "sesionId", null);
                    if (sesionId != null) {
                        out.print(gson.toJson(dashboardDAO.obtenerVentasTurno(sesionId)));
                    } else {
                        out.print(gson.toJson(new HashMap<>()));
                    }
                    break;
                case "misVentasHoy":
                    out.print(gson.toJson(obtenerMisVentasHoy(request)));
                    break;
                case "misUltimasVentas":
                    out.print(gson.toJson(obtenerMisUltimasVentas(request)));
                    break;
                case "cajasDisponibles":
                    out.print(gson.toJson(cajaApiClient.listarCajasDisponibles()));
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
        }
    }

    /**
     * Obtener solo alertas
     */
    private Map<String, Object> obtenerAlertas() throws IOException {
        Map<String, Object> alertas = new HashMap<>();
        JsonObject resumen = dashboardApiClient.obtenerResumen();
        alertas.put("stockBajo", getInt(resumen, "stockBajo"));
        alertas.put("agotados", getInt(resumen, "agotados"));
        alertas.put("porVencer", getInt(resumen, "porVencer"));
        alertas.put("vencidos", getInt(resumen, "vencidos"));
        alertas.put("productosStockBajo", dashboardApiClient.obtenerProductosStockBajo(5));
        alertas.put("productosPorVencer", dashboardApiClient.obtenerProductosPorVencer(5));
        return alertas;
    }

    private int getInt(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsInt() : 0;
    }
    
    // =====================================================
    //     MÉTODOS PARA FARMACÉUTICO
    // =====================================================
    
    /**
     * Obtener resumen completo del turno del farmacéutico
     */
    private Map<String, Object> obtenerMiTurno(HttpServletRequest request) throws IOException {
        Map<String, Object> resultado = new HashMap<>();
        
        Long usuarioId = obtenerUsuarioIdSesion(request);
        if (usuarioId == null) {
            resultado.put("error", "Usuario no autenticado");
            return resultado;
        }
        
        // Sesión de caja activa
        Map<String, Object> sesionActiva = dashboardDAO.obtenerSesionCajaActiva(usuarioId);
        resultado.put("sesionActiva", sesionActiva);
        resultado.put("tieneCajaAbierta", sesionActiva != null);
        
        // Si tiene caja abierta, obtener ventas del turno
        if (sesionActiva != null) {
            Long sesionId = (Long) sesionActiva.get("id");
            resultado.put("ventasTurno", dashboardDAO.obtenerVentasTurno(sesionId));
        }
        
        // Ventas del día del usuario
        resultado.put("ventasHoy", dashboardDAO.obtenerVentasDelDiaUsuario(usuarioId));
        
        // Últimas ventas del usuario
        resultado.put("ultimasVentas", dashboardDAO.obtenerUltimasVentasUsuario(usuarioId, 5));
        
        // Alertas generales (productos con stock bajo y por vencer)
        resultado.put("stockBajo", dashboardDAO.contarProductosStockBajo());
        resultado.put("agotados", dashboardDAO.contarProductosAgotados());
        resultado.put("porVencer", dashboardDAO.contarProductosPorVencer());
        resultado.put("vencidos", dashboardDAO.contarProductosVencidos());
        resultado.put("productosStockBajo", dashboardDAO.obtenerProductosStockBajo(5));
        resultado.put("productosPorVencer", dashboardApiClient.obtenerProductosPorVencer(5));
        
        // Cajas disponibles (para apertura)
        resultado.put("cajasDisponibles", cajaApiClient.listarCajasDisponibles());
        
        return resultado;
    }
    
    /**
     * Obtener sesión de caja activa del usuario
     */
    private Map<String, Object> obtenerSesionActiva(HttpServletRequest request) {
        Long usuarioId = obtenerUsuarioIdSesion(request);
        if (usuarioId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Usuario no autenticado");
            return error;
        }
        
        Map<String, Object> sesion = dashboardDAO.obtenerSesionCajaActiva(usuarioId);
        if (sesion == null) {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("activa", false);
            return resultado;
        }
        
        sesion.put("activa", true);
        // Agregar ventas del turno
        Long sesionId = (Long) sesion.get("id");
        sesion.put("ventasTurno", dashboardDAO.obtenerVentasTurno(sesionId));
        
        return sesion;
    }
    
    /**
     * Obtener ventas del día del usuario actual
     */
    private Map<String, Object> obtenerMisVentasHoy(HttpServletRequest request) {
        Long usuarioId = obtenerUsuarioIdSesion(request);
        if (usuarioId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Usuario no autenticado");
            return error;
        }
        return dashboardDAO.obtenerVentasDelDiaUsuario(usuarioId);
    }
    
    /**
     * Obtener últimas ventas del usuario actual
     */
    private List<Map<String, Object>> obtenerMisUltimasVentas(HttpServletRequest request) {
        Long usuarioId = obtenerUsuarioIdSesion(request);
        if (usuarioId == null) {
            return java.util.Collections.emptyList();
        }
        int limite = getIntParam(request, "limite", 5);
        return dashboardDAO.obtenerUltimasVentasUsuario(usuarioId, limite);
    }
    
    /**
     * Obtener ID del usuario de la sesión
     */
    private Long obtenerUsuarioIdSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Intentar con ambas claves
            UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
            if (usuario == null) {
                usuario = (UsuarioDTO) session.getAttribute("usuario");
            }
            if (usuario != null) {
                return usuario.getId();
            }
        }
        return null;
    }
    
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
