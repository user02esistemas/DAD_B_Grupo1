package api.ventas;

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
import rmi.dto.DetalleTransaccionDTO;
import rmi.dto.SesionCajaDTO;
import rmi.dto.TransaccionDTO;
import rmi.ventas.VentaServiceRMI;

@WebServlet(name = "VentaApiServlet", urlPatterns = {"/api/ventas", "/api/ventas/ultimas"})
public class VentaApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            int limite = parseLimite(request.getParameter("limite"));
            VentaServiceRMI ventaService = RMIClientFactory.getVentaService();
            List<TransaccionDTO> ventas = ventaService.listarVentas(1, limite);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Ultimas ventas", ventas)));
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
            VentaRequest ventaRequest = gson.fromJson(request.getReader(), VentaRequest.class);
            if (ventaRequest == null || ventaRequest.usuarioId == null
                    || ventaRequest.detalles == null || ventaRequest.detalles.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Datos de venta incompletos")));
                return;
            }

            VentaServiceRMI ventaService = RMIClientFactory.getVentaService();
            SesionCajaDTO sesionCaja = ventaService.buscarSesionAbierta(ventaRequest.usuarioId);
            if (sesionCaja == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("No tiene una caja abierta. Abra caja antes de vender.")));
                return;
            }

            TransaccionDTO venta = new TransaccionDTO();
            venta.setUsuarioId(ventaRequest.usuarioId);
            venta.setSesionCajaId(sesionCaja.getId());
            venta.setNombrePersona(ventaRequest.clienteNombre);
            venta.setMetodoPago(ventaRequest.metodoPago);
            venta.setTipoComprobante(ventaRequest.tipoComprobante);
            venta.setMontoEfectivo(ventaRequest.montoEfectivo);
            venta.setMontoVirtual(ventaRequest.montoVirtual);
            venta.setMedioPagoVirtual(ventaRequest.medioPagoVirtual);
            venta.setVuelto(ventaRequest.vuelto);
            venta.setObservaciones(ventaRequest.observaciones);

            Long ventaId = ventaService.registrarVenta(venta, ventaRequest.detalles);
            TransaccionDTO registrada = ventaService.buscarPorId(ventaId);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Venta registrada", registrada)));
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

    private static class VentaRequest {
        private Long usuarioId;
        private Long sesionCajaId;
        private String clienteNombre;
        private String metodoPago;
        private String tipoComprobante;
        private BigDecimal montoEfectivo;
        private BigDecimal montoVirtual;
        private String medioPagoVirtual;
        private BigDecimal vuelto;
        private String observaciones;
        private List<DetalleTransaccionDTO> detalles;
    }
}
