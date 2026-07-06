package api.caja;

import api.common.ApiResponse;
import api.config.RMIClientFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import rmi.dto.SesionCajaDTO;
import rmi.ventas.VentaServiceRMI;

@WebServlet(name = "CajaApiServlet", urlPatterns = {"/api/caja/estado", "/api/caja/sesiones", "/api/caja/abrir", "/api/caja/cerrar"})
public class CajaApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            VentaServiceRMI ventaService = RMIClientFactory.getVentaService();
            if (request.getServletPath().endsWith("/sesiones")) {
                int limite = parseInt(request.getParameter("limite"), 2);
                response.getWriter().write(gson.toJson(ApiResponse.ok(
                        "Sesiones de caja", ventaService.listarUltimasSesionesCaja(limite))));
                return;
            }
            Long usuarioId = parseLong(request.getParameter("usuarioId"));
            if (usuarioId == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Usuario requerido")));
                return;
            }
            SesionCajaDTO sesion = ventaService.buscarSesionAbierta(usuarioId);
            response.getWriter().write(gson.toJson(ApiResponse.ok(
                    sesion == null ? "Caja cerrada" : "Caja abierta", sesion)));
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
            CajaRequest cajaRequest = gson.fromJson(request.getReader(), CajaRequest.class);
            if (cajaRequest == null || cajaRequest.usuarioId == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Usuario requerido")));
                return;
            }

            VentaServiceRMI ventaService = RMIClientFactory.getVentaService();
            if (request.getServletPath().endsWith("/abrir")) {
                SesionCajaDTO sesion = new SesionCajaDTO();
                sesion.setUsuarioId(cajaRequest.usuarioId);
                sesion.setCajaId(cajaRequest.cajaId == null ? 1L : cajaRequest.cajaId);
                sesion.setMontoInicial(cajaRequest.montoInicial == null ? BigDecimal.ZERO : cajaRequest.montoInicial);
                Long sesionId = ventaService.abrirSesionCaja(sesion);
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Caja abierta", ventaService.buscarSesionAbierta(cajaRequest.usuarioId))));
                return;
            }

            SesionCajaDTO cierre = new SesionCajaDTO();
            cierre.setUsuarioId(cajaRequest.usuarioId);
            cierre.setMontoFinal(cajaRequest.montoFinal == null ? BigDecimal.ZERO : cajaRequest.montoFinal);
            cierre.setObservaciones(cajaRequest.observaciones);
            ventaService.cerrarSesionCaja(cierre);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Caja cerrada", true)));
        } catch (JsonSyntaxException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error("JSON invalido")));
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static class CajaRequest {
        private Long usuarioId;
        private Long cajaId;
        private BigDecimal montoInicial;
        private BigDecimal montoFinal;
        private String observaciones;
    }
}
