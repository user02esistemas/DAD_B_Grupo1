package api.compras;

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
import java.util.List;
import rmi.compras.CompraServiceRMI;
import rmi.dto.DetalleTransaccionDTO;
import rmi.dto.ProveedorDTO;
import rmi.dto.TransaccionDTO;

@WebServlet(name = "CompraApiServlet", urlPatterns = {"/api/compras", "/api/compras/proveedores"})
public class CompraApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            CompraServiceRMI compraService = RMIClientFactory.getCompraService();
            if ("/api/compras/proveedores".equals(request.getServletPath())) {
                List<ProveedorDTO> proveedores = compraService.listarProveedoresActivos();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Proveedores encontrados", proveedores)));
                return;
            }

            int limite = parseLimite(request.getParameter("limite"));
            List<TransaccionDTO> compras = compraService.listarCompras(1, limite);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Compras encontradas", compras)));
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
            CompraRequest compraRequest = gson.fromJson(request.getReader(), CompraRequest.class);
            if (compraRequest == null || compraRequest.usuarioId == null || compraRequest.proveedorId == null
                    || compraRequest.detalles == null || compraRequest.detalles.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Datos de compra incompletos")));
                return;
            }

            TransaccionDTO compra = new TransaccionDTO();
            compra.setUsuarioId(compraRequest.usuarioId);
            compra.setProveedorId(compraRequest.proveedorId);
            compra.setMetodoPago(compraRequest.metodoPago);
            compra.setTipoComprobante(compraRequest.tipoComprobante);
            compra.setObservaciones(compraRequest.observaciones);
            compra.setMontoEfectivo(compraRequest.montoEfectivo);
            compra.setMontoVirtual(compraRequest.montoVirtual);

            CompraServiceRMI compraService = RMIClientFactory.getCompraService();
            Long compraId = compraService.registrarCompra(compra, compraRequest.detalles);
            TransaccionDTO registrada = compraService.buscarPorId(compraId);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Compra registrada", registrada)));
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

    private int parseLimite(String value) {
        try {
            return value == null ? 10 : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 10;
        }
    }

    private static class CompraRequest {
        private Long usuarioId;
        private Long proveedorId;
        private String metodoPago;
        private String tipoComprobante;
        private String observaciones;
        private BigDecimal montoEfectivo;
        private BigDecimal montoVirtual;
        private List<DetalleTransaccionDTO> detalles;
    }
}
