package api.compras;

import api.common.ApiResponse;
import api.config.RMIClientFactory;
import api.websocket.NotificacionBroadcaster;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import rmi.compras.CompraServiceRMI;
import rmi.dto.CatalogoProductoDTO;
import rmi.dto.DetalleTransaccionDTO;
import rmi.dto.ProductoDTO;
import rmi.dto.ProveedorDTO;
import rmi.dto.TransaccionDTO;

@WebServlet(name = "CompraApiServlet", urlPatterns = {"/api/compras", "/api/compras/*", "/api/compras/proveedores"})
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
                String termino = request.getParameter("q");
                List<ProveedorDTO> proveedores = termino == null || termino.trim().isEmpty()
                        ? compraService.listarProveedoresActivos()
                        : compraService.buscarProveedoresAutocomplete(termino);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Proveedores encontrados", proveedores)));
                return;
            }

            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.startsWith("/productos")) {
                atenderProductos(request, response, compraService, pathInfo);
                return;
            }

            Long id = obtenerId(request);
            if (id != null) {
                TransaccionDTO compra = compraService.buscarPorId(id);
                if (compra == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(gson.toJson(ApiResponse.error("Compra no encontrada")));
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Compra encontrada", compra)));
                return;
            }

            int pagina = parseInt(request.getParameter("pagina"), 1);
            int porPagina = parseInt(request.getParameter("porPagina"), parseLimite(request.getParameter("limite")));
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("compras", compraService.listarCompras(pagina, porPagina));
            data.put("total", compraService.contarCompras());
            data.put("pagina", pagina);
            data.put("porPagina", porPagina);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Compras encontradas", data)));
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
            if ("/api/compras/proveedores".equals(request.getServletPath())) {
                ProveedorDTO proveedor = gson.fromJson(request.getReader(), ProveedorDTO.class);
                CompraServiceRMI compraService = RMIClientFactory.getCompraService();
                Long id = compraService.insertarProveedor(proveedor);
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Proveedor registrado", compraService.buscarProveedorPorId(id))));
                return;
            }

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
            compra.setSubtotal(compraRequest.subtotal);
            compra.setIgv(compraRequest.igv);
            compra.setTotal(compraRequest.total);
            compra.setEstado(compraRequest.estado);

            CompraServiceRMI compraService = RMIClientFactory.getCompraService();
            Long compraId = compraService.registrarCompra(compra, compraRequest.detalles);
            TransaccionDTO registrada = compraService.buscarPorId(compraId);
            NotificacionBroadcaster.enviar(
                    "COMPRA",
                    "Compra registrada",
                    "Compra " + texto(registrada == null ? null : registrada.getNumeroTransaccion())
                    + " por S/ " + monto(registrada == null ? null : registrada.getTotal())
            );

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

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            Long id = obtenerId(request);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Id requerido")));
                return;
            }
            boolean ok = RMIClientFactory.getCompraService().anular(id);
            if (ok) {
                NotificacionBroadcaster.enviar("COMPRA", "Compra anulada", "Se anulo la compra ID " + id);
            }
            response.setStatus(ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(ok ? ApiResponse.ok("Compra anulada", null) : ApiResponse.error("Compra no encontrada")));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    private void atenderProductos(HttpServletRequest request, HttpServletResponse response, CompraServiceRMI compraService, String pathInfo) throws Exception {
        if ("/productos/codigo".equals(pathInfo)) {
            CatalogoProductoDTO producto = compraService.buscarProductoCatalogoPorCodigo(request.getParameter("codigo"));
            if (producto == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(ApiResponse.error("Producto no encontrado")));
                return;
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Producto encontrado", producto)));
            return;
        }

        Long productoId = obtenerIdDesdePath(pathInfo, "/productos/");
        if (productoId != null && pathInfo.endsWith("/stock")) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("stockTotal", compraService.obtenerStockTotalPorCatalogo(productoId));
            data.put("precioCompraPromedio", compraService.obtenerPrecioCompraPorCatalogo(productoId));
            data.put("lotes", compraService.listarLotesPorCatalogo(productoId));
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Stock encontrado", data)));
            return;
        }

        if (productoId != null) {
            CatalogoProductoDTO producto = compraService.buscarProductoCatalogoPorId(productoId);
            if (producto == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(ApiResponse.error("Producto no encontrado")));
                return;
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Producto encontrado", producto)));
            return;
        }

        String termino = request.getParameter("q");
        int limite = parseLimite(request.getParameter("limite"));
        List<CatalogoProductoDTO> productos = compraService.buscarProductosCatalogo(termino, limite);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(ApiResponse.ok("Productos encontrados", productos)));
    }

    private int parseLimite(String value) {
        return parseInt(value, 10);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private Long obtenerId(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1 || pathInfo.startsWith("/productos")) {
            return null;
        }
        try {
            return Long.valueOf(pathInfo.substring(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long obtenerIdDesdePath(String pathInfo, String prefix) {
        if (pathInfo == null || !pathInfo.startsWith(prefix)) {
            return null;
        }
        String value = pathInfo.substring(prefix.length());
        int slash = value.indexOf('/');
        if (slash >= 0) {
            value = value.substring(0, slash);
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String texto(String value) {
        return value == null || value.trim().isEmpty() ? "registrada" : value;
    }

    private String monto(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static class CompraRequest {
        private Long usuarioId;
        private Long proveedorId;
        private String metodoPago;
        private String tipoComprobante;
        private String observaciones;
        private BigDecimal montoEfectivo;
        private BigDecimal montoVirtual;
        private BigDecimal subtotal;
        private BigDecimal igv;
        private BigDecimal total;
        private String estado;
        private List<DetalleTransaccionDTO> detalles;
    }
}
