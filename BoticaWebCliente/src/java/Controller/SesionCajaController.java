package controller;

import DAO.SesionCajaDAO;
import DAO.DashboardDAO;
import DTO.SesionCajaDTO;
import DTO.UsuarioDTO;
import com.google.gson.Gson;
import integration.api.CajaApiClient;
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
 * Controller para Sesiones de Caja
 * Maneja apertura, cierre y consultas de caja
 */
@WebServlet(name = "SesionCajaController", urlPatterns = {"/SesionCajaController"})
public class SesionCajaController extends HttpServlet {

    private final SesionCajaDAO sesionCajaDAO = new SesionCajaDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final CajaApiClient cajaApiClient = new CajaApiClient();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            UsuarioDTO usuario = obtenerUsuarioSesion(request);
            if (usuario == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"No autenticado\"}");
                return;
            }

            switch (action != null ? action : "") {
                case "verificar":
                    out.print(gson.toJson(verificarSesionActiva(usuario.getId())));
                    break;
                case "estado":
                    out.print(gson.toJson(obtenerEstadoCaja(usuario.getId())));
                    break;
                case "resumen":
                    Long sesionId = getLongParam(request, "sesionId", null);
                    if (sesionId != null) {
                        SesionCajaDTO resumen = sesionCajaDAO.obtenerResumenSesion(sesionId);
                        out.print(gson.toJson(resumen));
                    } else {
                        out.print("{\"error\": \"ID de sesión requerido\"}");
                    }
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            UsuarioDTO usuario = obtenerUsuarioSesion(request);
            if (usuario == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"No autenticado\"}");
                return;
            }

            Map<String, Object> resultado = new HashMap<>();

            switch (action != null ? action : "") {
                case "abrir":
                    resultado = abrirCaja(request, usuario);
                    break;
                case "cerrar":
                    resultado = cerrarCaja(request, usuario);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resultado.put("success", false);
                    resultado.put("error", "Acción no válida");
            }

            out.print(gson.toJson(resultado));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            out.print(gson.toJson(error));
        }
    }

    /**
     * Verificar si el usuario tiene sesión de caja activa
     */
    private Map<String, Object> verificarSesionActiva(Long usuarioId) throws IOException {
        Map<String, Object> resultado = new HashMap<>();
        
        SesionCajaDTO sesion = cajaApiClient.buscarSesionAbierta(usuarioId);
        
        if (sesion != null) {
            resultado.put("activa", true);
            resultado.put("sesionId", sesion.getId());
            resultado.put("cajaId", sesion.getCajaId());
            resultado.put("cajaNombre", sesion.getCajaNombre());
            resultado.put("fechaApertura", sesion.getFechaApertura());
            resultado.put("montoInicial", sesion.getMontoInicial());
        } else {
            resultado.put("activa", false);
        }
        
        return resultado;
    }

    /**
     * Obtener estado completo de la caja del usuario
     */
    private Map<String, Object> obtenerEstadoCaja(Long usuarioId) throws IOException {
        Map<String, Object> resultado = new HashMap<>();
        
        SesionCajaDTO sesion = cajaApiClient.buscarSesionAbierta(usuarioId);
        
        if (sesion != null) {
            resultado.put("tieneCajaAbierta", true);
            resultado.put("sesion", sesion);
            
            // Obtener resumen de ventas del turno
            SesionCajaDTO resumen = sesionCajaDAO.obtenerResumenSesion(sesion.getId());
            if (resumen != null) {
                resultado.put("totalVentas", resumen.getTotalTransacciones());
                resultado.put("efectivoNeto", resumen.getTotalVentasEfectivo());
                resultado.put("totalVirtual", resumen.getTotalVentasVirtual());
                resultado.put("efectivoEsperado", resumen.getEfectivoEsperado());
            }
        } else {
            resultado.put("tieneCajaAbierta", false);
            resultado.put("cajasDisponibles", dashboardDAO.obtenerCajasDisponibles());
        }
        
        return resultado;
    }

    /**
     * Abrir nueva sesión de caja
     */
    private Map<String, Object> abrirCaja(HttpServletRequest request, UsuarioDTO usuario) throws IOException {
        Map<String, Object> resultado = new HashMap<>();
        
        // Verificar que no tenga caja abierta
        if (cajaApiClient.buscarSesionAbierta(usuario.getId()) != null) {
            resultado.put("success", false);
            resultado.put("error", "Ya tienes una caja abierta");
            return resultado;
        }
        
        // Obtener parámetros
        Long cajaId = getLongParam(request, "cajaId", 1L);
        BigDecimal montoInicial = getBigDecimalParam(request, "montoInicial", BigDecimal.ZERO);
        
        SesionCajaDTO sesionCreada = cajaApiClient.abrir(usuario.getId(), cajaId, montoInicial);
        
        if (sesionCreada != null && sesionCreada.getId() != null) {
            resultado.put("success", true);
            resultado.put("sesionId", sesionCreada.getId());
            resultado.put("mensaje", "Caja abierta correctamente");
            resultado.put("sesion", sesionCreada);
        } else {
            resultado.put("success", false);
            resultado.put("error", "No se pudo abrir la caja");
        }
        
        return resultado;
    }

    /**
     * Cerrar sesión de caja
     */
    private Map<String, Object> cerrarCaja(HttpServletRequest request, UsuarioDTO usuario) throws IOException {
        Map<String, Object> resultado = new HashMap<>();
        
        // Buscar sesión activa
        SesionCajaDTO sesionActiva = cajaApiClient.buscarSesionAbierta(usuario.getId());
        
        if (sesionActiva == null) {
            resultado.put("success", false);
            resultado.put("error", "No tienes una caja abierta");
            return resultado;
        }
        
        // Obtener resumen para cálculos
        SesionCajaDTO resumen = sesionCajaDAO.obtenerResumenSesion(sesionActiva.getId());
        
        // Obtener parámetros
        BigDecimal montoFinal = getBigDecimalParam(request, "montoFinal", BigDecimal.ZERO);
        String observaciones = request.getParameter("observaciones");
        
        // Preparar datos de cierre
        resumen.setMontoFinal(montoFinal);
        resumen.setObservaciones(observaciones);
        
        cajaApiClient.cerrar(usuario.getId(), montoFinal, observaciones);

        BigDecimal diferencia = montoFinal.subtract(resumen.getEfectivoEsperado());
        resultado.put("success", true);
        resultado.put("mensaje", "Caja cerrada correctamente");
        resultado.put("resumen", resumen);
        resultado.put("diferencia", diferencia);

        if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
            resultado.put("tipoDiferencia", "SOBRANTE");
        } else if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
            resultado.put("tipoDiferencia", "FALTANTE");
        } else {
            resultado.put("tipoDiferencia", "CUADRADO");
        }
        
        return resultado;
    }

    // =====================================================
    //              MÉTODOS AUXILIARES
    // =====================================================

    private UsuarioDTO obtenerUsuarioSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Intentar con ambas claves de sesión
            UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
            if (usuario == null) {
                usuario = (UsuarioDTO) session.getAttribute("usuario");
            }
            return usuario;
        }
        return null;
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

    private BigDecimal getBigDecimalParam(HttpServletRequest request, String param, BigDecimal defaultValue) {
        String value = request.getParameter(param);
        if (value != null && !value.isEmpty()) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
