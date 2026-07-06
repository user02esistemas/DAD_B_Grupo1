package controller;

import DTO.SesionCajaDTO;
import DTO.DetalleTransaccionDTO;
import DTO.UsuarioDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import integration.api.CajaApiClient;
import integration.api.ProductoApiClient;
import integration.api.VentaApiClient;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller para el Punto de Venta (POS)
 * 
 * RESPONSABILIDAD: Solo ventas
 * - Buscar productos
 * - Procesar ventas
 * - Obtener detalle de ventas
 * 
 * La gestión de caja (abrir/cerrar) está en SesionCajaController
 * 
 * Modelo Farmacia Perú: Precios de venta YA INCLUYEN IGV
 */
@WebServlet(name = "VentaController", urlPatterns = {"/VentaController"})
public class VentaController extends HttpServlet {

    private final Gson gson = new Gson();
    private final CajaApiClient cajaApiClient = new CajaApiClient();
    private final ProductoApiClient productoApiClient = new ProductoApiClient();
    private final VentaApiClient ventaApiClient = new VentaApiClient();
    private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/farmaceutico/ventas/nueva.jsp");
            return;
        }

        switch (action) {
            case "buscarProductos":
                buscarProductos(request, response);
                break;
            case "obtenerProducto":
                obtenerProducto(request, response);
                break;
            case "detalleVenta":
                obtenerDetalleVenta(request, response);
                break;
            case "verificarSesion":
                verificarSesion(request, response);
                break;
            case "resumenCierre":
                obtenerResumenCierre(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/farmaceutico/ventas/nueva.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if (action == null) {
            enviarError(response, "Acción no especificada");
            return;
        }

        switch (action) {
            case "procesarVenta":
                procesarVenta(request, response);
                break;
            case "abrirCaja":
                abrirCaja(request, response);
                break;
            case "cerrarCaja":
                cerrarCaja(request, response);
                break;
            default:
                enviarError(response, "Acción no válida");
        }
    }

    /**
     * Buscar productos para venta (autocomplete)
     */
    private void buscarProductos(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            String termino = request.getParameter("termino");
            if (termino == null || termino.trim().length() < 2) {
                out.print("[]");
                return;
            }

            List<ProductoApiClient.ProductoVenta> productos = productoApiClient.buscarParaVenta(termino.trim(), 15);

            JsonArray jsonArray = new JsonArray();
            for (ProductoApiClient.ProductoVenta p : productos) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", p.id);
                obj.addProperty("catalogoId", p.catalogoProductoId);
                obj.addProperty("nombre", p.nombreComercial);
                obj.addProperty("concentracion", p.concentracion);
                obj.addProperty("presentacion", p.presentacion);
                obj.addProperty("laboratorio", p.laboratorio);
                obj.addProperty("lote", p.lote);
                obj.addProperty("fechaVencimiento", p.fechaVencimiento);
                obj.addProperty("stock", p.stockActual);
                obj.addProperty("precioVenta", p.precioVenta);
                obj.addProperty("precioCompra", p.precioCompra);
                jsonArray.add(obj);
            }

            out.print(jsonArray.toString());

        } catch (Exception e) {
            e.printStackTrace();
            out.print("[]");
        }
    }

    /**
     * Obtener datos de un producto específico
     */
    private void obtenerProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject json = new JsonObject();

        try {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                json.addProperty("success", false);
                json.addProperty("message", "ID no especificado");
                out.print(json.toString());
                return;
            }

            Long id = Long.parseLong(idStr);
            ProductoApiClient.ProductoVenta p = productoApiClient.buscarVentaPorId(id);

            if (p != null) {
                json.addProperty("success", true);
                json.addProperty("id", p.id);
                json.addProperty("nombre", p.nombreComercial);
                json.addProperty("concentracion", p.concentracion);
                json.addProperty("presentacion", p.presentacion);
                json.addProperty("laboratorio", p.laboratorio);
                json.addProperty("lote", p.lote);
                json.addProperty("stock", p.stockActual);
                json.addProperty("precioVenta", p.precioVenta);
            } else {
                json.addProperty("success", false);
                json.addProperty("message", "Producto no encontrado");
            }

        } catch (Exception e) {
            json.addProperty("success", false);
            json.addProperty("message", e.getMessage());
        }

        out.print(json.toString());
    }

    /**
     * Obtener detalle de una venta específica
     */
    private void obtenerDetalleVenta(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                enviarError(response, "ID no especificado");
                return;
            }

            Long id = Long.parseLong(idStr);
            JsonObject venta = ventaApiClient.buscarPorId(id);

            JsonObject json = new JsonObject();
            json.addProperty("success", true);

            JsonObject ventaObj = new JsonObject();
            ventaObj.addProperty("id", getLong(venta, "id"));
            ventaObj.addProperty("numeroTransaccion", getString(venta, "numeroTransaccion"));
            ventaObj.addProperty("fecha", getString(venta, "fecha"));
            ventaObj.addProperty("cliente", getString(venta, "cliente") != null ? getString(venta, "cliente") : getString(venta, "nombrePersona"));
            ventaObj.addProperty("tipoComprobante", getString(venta, "tipoComprobante"));
            ventaObj.addProperty("metodoPago", getString(venta, "metodoPago"));
            ventaObj.add("montoEfectivo", venta.get("montoEfectivo"));
            ventaObj.add("montoVirtual", venta.get("montoVirtual"));
            ventaObj.addProperty("medioPagoVirtual", getString(venta, "medioPagoVirtual"));
            ventaObj.add("vuelto", venta.get("vuelto"));
            ventaObj.add("subtotal", venta.get("subtotal"));
            ventaObj.add("igv", venta.get("igv"));
            ventaObj.add("total", venta.get("total"));

            JsonArray detallesArray = new JsonArray();
            JsonArray detalles = venta.getAsJsonArray("detalles");
            if (detalles != null) for (int i = 0; i < detalles.size(); i++) {
                JsonObject detalle = detalles.get(i).getAsJsonObject();
                JsonObject detalleObj = new JsonObject();
                detalleObj.addProperty("productoNombre", getString(detalle, "nombreComercial") + " " + getString(detalle, "concentracion"));
                detalleObj.add("cantidad", detalle.get("cantidad"));
                detalleObj.add("precioUnitario", detalle.get("precioUnitario"));
                detalleObj.add("subtotal", detalle.get("subtotal"));
                detallesArray.add(detalleObj);
            }
            ventaObj.add("detalles", detallesArray);

            json.add("venta", ventaObj);
            out.print(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            enviarError(response, "Error: " + e.getMessage());
        }
    }

    /**
     * Procesar venta
     */
    private void procesarVenta(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject json = new JsonObject();

        try {
            UsuarioDTO usuario = obtenerUsuario(request);
            if (usuario == null) {
                json.addProperty("success", false);
                json.addProperty("message", "Usuario no autenticado");
                out.print(json.toString());
                return;
            }

            // Verificar sesión de caja
            SesionCajaDTO sesionCaja = cajaApiClient.buscarSesionAbierta(usuario.getId());

            if (sesionCaja == null) {
                json.addProperty("success", false);
                json.addProperty("message", "No tiene una caja abierta. Abra caja primero desde Mi Caja.");
                out.print(json.toString());
                return;
            }

            // Obtener datos de la venta
            String productosJson = request.getParameter("productos");
            String montoEfectivoStr = request.getParameter("montoEfectivo");
            String montoVirtualStr = request.getParameter("montoVirtual");
            String medioPagoVirtual = request.getParameter("medioPagoVirtual");
            String tipoComprobante = request.getParameter("tipoComprobante");
            String cliente = request.getParameter("cliente");
            String totalStr = request.getParameter("total");

            BigDecimal montoEfectivo = parseBigDecimal(montoEfectivoStr);
            BigDecimal montoVirtual = parseBigDecimal(montoVirtualStr);

            // Parsear productos
            JsonArray productosArray = gson.fromJson(productosJson, JsonArray.class);
            if (productosArray == null || productosArray.size() == 0) {
                json.addProperty("success", false);
                json.addProperty("message", "No hay productos en la venta");
                out.print(json.toString());
                return;
            }

            // Calcular totales
            BigDecimal totalCalculado = BigDecimal.ZERO;
            List<DetalleTransaccionDTO> detalles = new ArrayList<>();

            for (int i = 0; i < productosArray.size(); i++) {
                JsonObject prod = productosArray.get(i).getAsJsonObject();
                Long productoId = prod.get("productoId").getAsLong();
                int cantidad = prod.get("cantidad").getAsInt();
                BigDecimal precioUnit = new BigDecimal(prod.get("precioUnitario").getAsString());

                // Validar stock
                ProductoApiClient.ProductoVenta producto = productoApiClient.buscarVentaPorId(productoId);
                if (producto == null || producto.stockActual < cantidad) {
                    json.addProperty("success", false);
                    json.addProperty("message", "Stock insuficiente para " +
                            (producto != null ? producto.nombreComercial : "producto"));
                    out.print(json.toString());
                    return;
                }

                BigDecimal subtotalItem = precioUnit.multiply(new BigDecimal(cantidad));
                totalCalculado = totalCalculado.add(subtotalItem);

                DetalleTransaccionDTO detalle = new DetalleTransaccionDTO();
                detalle.setProductoId(productoId);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precioUnit);
                detalle.setSubtotal(subtotalItem);
                detalles.add(detalle);
            }

            BigDecimal total = totalStr != null && !totalStr.isEmpty() ? 
                new BigDecimal(totalStr) : totalCalculado;
            
            // Desglose inverso del IGV
            BigDecimal subtotal = total.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP);
            BigDecimal igv = total.subtract(subtotal);

            // Calcular vuelto (solo efectivo)
            BigDecimal vuelto = BigDecimal.ZERO;
            if (montoEfectivo.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal totalPagado = montoEfectivo.add(montoVirtual);
                if (totalPagado.compareTo(total) > 0) {
                    vuelto = totalPagado.subtract(total);
                }
            }

            // Determinar método de pago
            String metodoPago;
            if (montoEfectivo.compareTo(BigDecimal.ZERO) > 0 && montoVirtual.compareTo(BigDecimal.ZERO) > 0) {
                metodoPago = "MIXTO";
            } else if (montoVirtual.compareTo(BigDecimal.ZERO) > 0) {
                metodoPago = "YAPE_PLIN";
            } else {
                metodoPago = "EFECTIVO";
            }

            JsonObject registrada = ventaApiClient.registrarVenta(
                    usuario.getId(),
                    cliente != null && !cliente.isEmpty() ? cliente : "CLIENTES VARIOS",
                    metodoPago,
                    tipoComprobante != null ? tipoComprobante : "NOTA_VENTA",
                    montoEfectivo,
                    montoVirtual,
                    medioPagoVirtual != null && !medioPagoVirtual.isEmpty() ? medioPagoVirtual : null,
                    vuelto,
                    detalles);

            json.addProperty("success", true);
            json.addProperty("transaccionId", getLong(registrada, "id"));
            json.addProperty("numeroTransaccion", getString(registrada, "numeroTransaccion"));
            json.addProperty("subtotal", subtotal.toString());
            json.addProperty("igv", igv.toString());
            json.addProperty("total", total.toString());
            json.addProperty("vuelto", vuelto.toString());
            json.addProperty("metodoPago", metodoPago);
            json.addProperty("message", "Venta procesada correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            json.addProperty("success", false);
            json.addProperty("message", "Error: " + e.getMessage());
        }

        out.print(json.toString());
    }

    // =====================================================
    //              MÉTODOS AUXILIARES
    // =====================================================

    private UsuarioDTO obtenerUsuario(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
            if (usuario == null) {
                usuario = (UsuarioDTO) session.getAttribute("usuario");
            }
            return usuario;
        }
        return null;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private Long getLong(JsonObject object, String key) {
        return object != null && object.has(key) && !object.get(key).isJsonNull()
                ? object.get(key).getAsLong()
                : null;
    }

    private String getString(JsonObject object, String key) {
        return object != null && object.has(key) && !object.get(key).isJsonNull()
                ? object.get(key).getAsString()
                : null;
    }

    private void enviarError(HttpServletResponse response, String mensaje) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject json = new JsonObject();
        json.addProperty("success", false);
        json.addProperty("message", mensaje);
        out.print(json.toString());
    }

    // =====================================================
    //     MÉTODOS DE GESTIÓN DE CAJA
    // =====================================================

    /**
     * Verificar si hay sesión de caja activa
     */
    private void verificarSesion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject json = new JsonObject();

        try {
            UsuarioDTO usuario = obtenerUsuario(request);
            if (usuario == null) {
                json.addProperty("success", false);
                json.addProperty("message", "No autenticado");
                out.print(json.toString());
                return;
            }

            SesionCajaDTO sesion = cajaApiClient.buscarSesionAbierta(usuario.getId());

            if (sesion != null) {
                json.addProperty("success", true);
                json.addProperty("activa", true);
                json.addProperty("sesionId", sesion.getId());
                json.addProperty("cajaNombre", sesion.getCajaNombre());
                json.addProperty("montoInicial", sesion.getMontoInicial());
            } else {
                json.addProperty("success", true);
                json.addProperty("activa", false);
            }
        } catch (Exception e) {
            json.addProperty("success", false);
            json.addProperty("message", e.getMessage());
        }

        out.print(json.toString());
    }

    /**
     * Abrir caja (desde el modal de apertura)
     */
    private void abrirCaja(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject json = new JsonObject();

        try {
            UsuarioDTO usuario = obtenerUsuario(request);
            if (usuario == null) {
                json.addProperty("success", false);
                json.addProperty("message", "No autenticado");
                out.print(json.toString());
                return;
            }

            if (cajaApiClient.buscarSesionAbierta(usuario.getId()) != null) {
                json.addProperty("success", false);
                json.addProperty("message", "Ya tiene una caja abierta");
                out.print(json.toString());
                return;
            }

            // Obtener monto inicial
            BigDecimal montoInicial = parseBigDecimal(request.getParameter("montoInicial"));

            SesionCajaDTO nuevaSesion = cajaApiClient.abrir(usuario.getId(), 1L, montoInicial);

            if (nuevaSesion != null && nuevaSesion.getId() != null) {
                json.addProperty("success", true);
                json.addProperty("sesionId", nuevaSesion.getId());
                json.addProperty("message", "Caja abierta correctamente");
            } else {
                json.addProperty("success", false);
                json.addProperty("message", "No se pudo abrir la caja");
            }
        } catch (Exception e) {
            json.addProperty("success", false);
            json.addProperty("message", e.getMessage());
        }

        out.print(json.toString());
    }

    /**
     * Obtener resumen para cierre de caja
     */
    private void obtenerResumenCierre(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject json = new JsonObject();

        try {
            UsuarioDTO usuario = obtenerUsuario(request);
            if (usuario == null) {
                json.addProperty("success", false);
                json.addProperty("message", "No autenticado");
                out.print(json.toString());
                return;
            }

            SesionCajaDTO sesion = cajaApiClient.buscarSesionAbierta(usuario.getId());

            if (sesion == null) {
                json.addProperty("success", false);
                json.addProperty("message", "No hay caja abierta");
                out.print(json.toString());
                return;
            }

            SesionCajaDTO resumen = sesion;

            json.addProperty("success", true);
            json.addProperty("sesionId", sesion.getId());
            json.addProperty("fechaApertura", resumen.getFechaApertura() != null ? resumen.getFechaApertura().toString() : null);
            json.addProperty("montoInicial", resumen.getMontoInicial());
            json.addProperty("totalVentas", resumen.getTotalTransacciones());
            json.addProperty("totalEfectivo", resumen.getTotalVentasEfectivo().subtract(resumen.getTotalVueltos())); // Neto
            json.addProperty("totalVirtual", resumen.getTotalVentasVirtual());
            json.addProperty("efectivoEsperado", resumen.getEfectivoEsperado());

        } catch (Exception e) {
            json.addProperty("success", false);
            json.addProperty("message", e.getMessage());
        }

        out.print(json.toString());
    }

    /**
     * Cerrar caja
     */
    private void cerrarCaja(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject json = new JsonObject();

        try {
            UsuarioDTO usuario = obtenerUsuario(request);
            if (usuario == null) {
                json.addProperty("success", false);
                json.addProperty("message", "No autenticado");
                out.print(json.toString());
                return;
            }

            SesionCajaDTO sesion = cajaApiClient.buscarSesionAbierta(usuario.getId());

            if (sesion == null) {
                json.addProperty("success", false);
                json.addProperty("message", "No hay caja abierta");
                out.print(json.toString());
                return;
            }

            SesionCajaDTO resumen = sesion;

            // Obtener monto final contado
            BigDecimal montoFinal = parseBigDecimal(request.getParameter("montoFinal"));
            String observaciones = request.getParameter("observaciones");

            cajaApiClient.cerrar(usuario.getId(), montoFinal, observaciones);
            BigDecimal diferencia = montoFinal.subtract(resumen.getEfectivoEsperado());

            json.addProperty("success", true);
            json.addProperty("message", "Caja cerrada correctamente");
            json.addProperty("diferencia", diferencia);

            if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
                json.addProperty("tipoDiferencia", "SOBRANTE");
            } else if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
                json.addProperty("tipoDiferencia", "FALTANTE");
            } else {
                json.addProperty("tipoDiferencia", "CUADRADO");
            }
        } catch (Exception e) {
            json.addProperty("success", false);
            json.addProperty("message", e.getMessage());
        }

        out.print(json.toString());
    }
}
