package controller;

import DAO.DashboardDAO;
import DTO.UsuarioDTO;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
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
                    out.print(gson.toJson(obtenerResumen()));
                    break;
                case "topProductos":
                    int limite = getIntParam(request, "limite", 5);
                    out.print(gson.toJson(dashboardDAO.obtenerTopProductosVendidos(limite)));
                    break;
                case "topProductosMes":
                    int limiteMes = getIntParam(request, "limite", 5);
                    out.print(gson.toJson(dashboardDAO.obtenerTopProductosMes(limiteMes)));
                    break;
                case "ventasSemana":
                    out.print(gson.toJson(dashboardDAO.obtenerVentasUltimos7Dias()));
                    break;
                case "ultimasVentas":
                    int limiteVentas = getIntParam(request, "limite", 5);
                    out.print(gson.toJson(dashboardDAO.obtenerUltimasVentas(limiteVentas)));
                    break;
                case "productosStockBajo":
                    int limiteStock = getIntParam(request, "limite", 5);
                    out.print(gson.toJson(dashboardDAO.obtenerProductosStockBajo(limiteStock)));
                    break;
                case "productosPorVencer":
                    int limiteVencer = getIntParam(request, "limite", 5);
                    out.print(gson.toJson(dashboardDAO.obtenerProductosPorVencer(limiteVencer)));
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
                    out.print(gson.toJson(dashboardDAO.obtenerCajasDisponibles()));
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
     * Obtener resumen general del dashboard
     */
    private Map<String, Object> obtenerResumen() {
        Map<String, Object> resumen = new HashMap<>();
        
        // Ventas
        resumen.put("ventasHoy", dashboardDAO.obtenerVentasDelDia());
        resumen.put("cantidadVentasHoy", dashboardDAO.obtenerCantidadVentasDelDia());
        resumen.put("ventasMes", dashboardDAO.obtenerVentasDelMes());
        
        // Compras
        resumen.put("comprasHoy", dashboardDAO.obtenerComprasDelDia());
        
        // Inventario
        resumen.put("totalProductos", dashboardDAO.contarTotalProductos());
        resumen.put("stockBajo", dashboardDAO.contarProductosStockBajo());
        resumen.put("agotados", dashboardDAO.contarProductosAgotados());
        resumen.put("porVencer", dashboardDAO.contarProductosPorVencer());
        resumen.put("vencidos", dashboardDAO.contarProductosVencidos());
        
        // Top productos
        resumen.put("topProductos", dashboardDAO.obtenerTopProductosVendidos(5));
        resumen.put("topProductosMes", dashboardDAO.obtenerTopProductosMes(5));
        
        // Ventas semana
        resumen.put("ventasSemana", dashboardDAO.obtenerVentasUltimos7Dias());
        
        // Últimas ventas
        resumen.put("ultimasVentas", dashboardDAO.obtenerUltimasVentas(5));
        
        return resumen;
    }
    
    /**
     * Obtener solo alertas
     */
    private Map<String, Object> obtenerAlertas() {
        Map<String, Object> alertas = new HashMap<>();
        alertas.put("stockBajo", dashboardDAO.contarProductosStockBajo());
        alertas.put("agotados", dashboardDAO.contarProductosAgotados());
        alertas.put("porVencer", dashboardDAO.contarProductosPorVencer());
        alertas.put("vencidos", dashboardDAO.contarProductosVencidos());
        alertas.put("productosStockBajo", dashboardDAO.obtenerProductosStockBajo(5));
        alertas.put("productosPorVencer", dashboardDAO.obtenerProductosPorVencer(5));
        return alertas;
    }
    
    // =====================================================
    //     MÉTODOS PARA FARMACÉUTICO
    // =====================================================
    
    /**
     * Obtener resumen completo del turno del farmacéutico
     */
    private Map<String, Object> obtenerMiTurno(HttpServletRequest request) {
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
        resultado.put("productosPorVencer", dashboardDAO.obtenerProductosPorVencer(5));
        
        // Cajas disponibles (para apertura)
        resultado.put("cajasDisponibles", dashboardDAO.obtenerCajasDisponibles());
        
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
