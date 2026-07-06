package controller;

import DTO.CatalogoProductoDTO;
import DTO.DetalleTransaccionDTO;
import DTO.ProductoDTO;
import DTO.ProveedorDTO;
import DTO.TransaccionDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import integration.api.CompraApiClient;
import integration.api.CompraApiClient.StockCatalogo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller para el módulo de Compras
 * Maneja búsqueda de productos, registro de compras y operaciones AJAX
 */
@WebServlet(name = "CompraController", urlPatterns = {"/CompraController"})
public class CompraController extends HttpServlet {

    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private final CompraApiClient compraApiClient = new CompraApiClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            switch (action != null ? action : "") {
                case "buscarProducto":
                    buscarProductoAutocomplete(request, response);
                    break;
                case "buscarPorCodigo":
                    buscarProductoPorCodigo(request, response);
                    break;
                case "obtenerProducto":
                    obtenerProductoDetalle(request, response);
                    break;
                case "listarProveedores":
                    listarProveedores(request, response);
                    break;
                case "buscarProveedor":
                    buscarProveedorAutocomplete(request, response);
                    break;
                case "obtenerInfoStock":
                    obtenerInfoStock(request, response);
                    break;
                case "listarCompras":
                    listarCompras(request, response);
                    break;
                case "verCompra":
                    verDetalleCompra(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/admin/compras/nueva.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            enviarErrorJson(response, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            switch (action != null ? action : "") {
                case "registrarCompra":
                    registrarCompra(request, response);
                    break;
                case "registrarProveedor":
                    registrarProveedor(request, response);
                    break;
                case "anularCompra":
                    anularCompra(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/admin/compras/nueva.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            enviarErrorJson(response, "Error: " + e.getMessage());
        }
    }

    // =====================================================
    //          BÚSQUEDA DE PRODUCTOS (AUTOCOMPLETE)
    // =====================================================

    /**
     * Búsqueda optimizada para autocomplete
     * Retorna máximo 15 resultados para rendimiento
     */
    private void buscarProductoAutocomplete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String termino = request.getParameter("q");
        if (termino == null || termino.trim().length() < 2) {
            enviarJsonResponse(response, "[]");
            return;
        }

        List<CatalogoProductoDTO> productos = compraApiClient.buscarProductosCatalogo(termino.trim(), 15);
        
        // Construir respuesta JSON optimizada
        JsonArray jsonArray = new JsonArray();
        for (CatalogoProductoDTO p : productos) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", p.getId());
            obj.addProperty("codigo", p.getCodigoProducto());
            obj.addProperty("nombre", p.getNombreComercial());
            obj.addProperty("principioActivo", p.getPrincipioActivo());
            obj.addProperty("concentracion", p.getConcentracion());
            obj.addProperty("formaFarmaceutica", p.getFormaFarmaceutica());
            obj.addProperty("presentacion", p.getPresentacion());
            obj.addProperty("laboratorio", p.getLaboratorio());
            obj.addProperty("cantidad", p.getCantidad());
            obj.addProperty("descripcionCorta", p.getDescripcionCorta());
            jsonArray.add(obj);
        }

        enviarJsonResponse(response, jsonArray.toString());
    }

    /**
     * Buscar producto por código de barras
     */
    private void buscarProductoPorCodigo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String codigo = request.getParameter("codigo");
        if (codigo == null || codigo.trim().isEmpty()) {
            enviarErrorJson(response, "Código no proporcionado");
            return;
        }

        CatalogoProductoDTO producto = compraApiClient.buscarProductoCatalogoPorCodigo(codigo.trim());
        if (producto == null) {
            enviarErrorJson(response, "Producto no encontrado");
            return;
        }

        JsonObject obj = construirJsonProducto(producto);
        enviarJsonResponse(response, obj.toString());
    }

    /**
     * Obtener detalle completo de un producto del catálogo
     * Incluye información de stock actual si existe en inventario
     */
    private void obtenerProductoDetalle(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            enviarErrorJson(response, "ID no proporcionado");
            return;
        }

        try {
            Long id = Long.parseLong(idParam);
            CatalogoProductoDTO producto = compraApiClient.buscarProductoCatalogoPorId(id);
            
            if (producto == null) {
                enviarErrorJson(response, "Producto no encontrado");
                return;
            }

            JsonObject obj = construirJsonProducto(producto);
            
            // Agregar información de stock actual
            StockCatalogo stock = compraApiClient.obtenerStockCatalogo(id);
            
            obj.addProperty("stockActual", stock.getStockTotal());
            obj.addProperty("precioCompraActual", stock.getPrecioCompraPromedio());

            enviarJsonResponse(response, obj.toString());

        } catch (NumberFormatException e) {
            enviarErrorJson(response, "ID inválido");
        }
    }

    /**
     * Obtener información de stock de un producto del catálogo
     */
    private void obtenerInfoStock(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String catalogoIdParam = request.getParameter("catalogoId");
        if (catalogoIdParam == null) {
            enviarErrorJson(response, "ID de catálogo no proporcionado");
            return;
        }

        try {
            Long catalogoId = Long.parseLong(catalogoIdParam);
            
            StockCatalogo stock = compraApiClient.obtenerStockCatalogo(catalogoId);

            JsonObject obj = new JsonObject();
            obj.addProperty("stockTotal", stock.getStockTotal());
            obj.addProperty("precioCompraPromedio", stock.getPrecioCompraPromedio());
            
            JsonArray lotesArray = new JsonArray();
            for (ProductoDTO lote : stock.getLotes()) {
                JsonObject loteObj = new JsonObject();
                loteObj.addProperty("id", lote.getId());
                loteObj.addProperty("lote", lote.getLote());
                loteObj.addProperty("fechaVencimiento", lote.getFechaVencimiento().toString());
                loteObj.addProperty("stockActual", lote.getStockActual());
                loteObj.addProperty("precioCompra", lote.getPrecioCompra());
                loteObj.addProperty("precioVenta", lote.getPrecioVenta());
                lotesArray.add(loteObj);
            }
            obj.add("lotes", lotesArray);

            enviarJsonResponse(response, obj.toString());

        } catch (NumberFormatException e) {
            enviarErrorJson(response, "ID inválido");
        }
    }

    // =====================================================
    //                  PROVEEDORES
    // =====================================================

    private void listarProveedores(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        List<ProveedorDTO> proveedores = compraApiClient.listarProveedoresActivos();
        
        JsonArray jsonArray = new JsonArray();
        for (ProveedorDTO p : proveedores) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", p.getId());
            obj.addProperty("ruc", p.getRuc());
            obj.addProperty("razonSocial", p.getRazonSocial());
            obj.addProperty("contacto", p.getContacto());
            obj.addProperty("telefono", p.getTelefono());
            jsonArray.add(obj);
        }

        enviarJsonResponse(response, jsonArray.toString());
    }

    private void buscarProveedorAutocomplete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String termino = request.getParameter("q");
        if (termino == null || termino.trim().length() < 2) {
            enviarJsonResponse(response, "[]");
            return;
        }

        List<ProveedorDTO> proveedores = compraApiClient.buscarProveedores(termino.trim());
        
        JsonArray jsonArray = new JsonArray();
        for (ProveedorDTO p : proveedores) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", p.getId());
            obj.addProperty("ruc", p.getRuc());
            obj.addProperty("razonSocial", p.getRazonSocial());
            obj.addProperty("texto", p.getRuc() + " - " + p.getRazonSocial());
            jsonArray.add(obj);
        }

        enviarJsonResponse(response, jsonArray.toString());
    }

    private void registrarProveedor(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String ruc = request.getParameter("ruc");
        String razonSocial = request.getParameter("razonSocial");
        String contacto = request.getParameter("contacto");
        String telefono = request.getParameter("telefono");
        String email = request.getParameter("email");
        String direccion = request.getParameter("direccion");

        if (ruc == null || ruc.trim().isEmpty() || razonSocial == null || razonSocial.trim().isEmpty()) {
            enviarErrorJson(response, "RUC y Razón Social son obligatorios");
            return;
        }

        if (compraApiClient.buscarProveedores(ruc.trim()).stream().anyMatch(p -> ruc.trim().equals(p.getRuc()))) {
            enviarErrorJson(response, "El RUC ya está registrado");
            return;
        }

        ProveedorDTO proveedor = new ProveedorDTO();
        proveedor.setRuc(ruc.trim());
        proveedor.setRazonSocial(razonSocial.trim());
        proveedor.setContacto(contacto);
        proveedor.setTelefono(telefono);
        proveedor.setEmail(email);
        proveedor.setDireccion(direccion);
        proveedor.setActivo(true);

        ProveedorDTO registrado = compraApiClient.registrarProveedor(proveedor);
        if (registrado != null && registrado.getId() != null) {
            JsonObject obj = new JsonObject();
            obj.addProperty("success", true);
            obj.addProperty("id", registrado.getId());
            obj.addProperty("ruc", registrado.getRuc());
            obj.addProperty("razonSocial", registrado.getRazonSocial());
            obj.addProperty("mensaje", "Proveedor registrado correctamente");
            enviarJsonResponse(response, obj.toString());
        } else {
            enviarErrorJson(response, "Error al registrar proveedor");
        }
    }

    // =====================================================
    //              REGISTRO DE COMPRA
    // =====================================================

    /**
     * Registrar compra completa
     * Recibe JSON con cabecera y detalles
     */
    private void registrarCompra(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Leer JSON del body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String jsonData = sb.toString();
        if (jsonData.isEmpty()) {
            enviarErrorJson(response, "No se recibieron datos");
            return;
        }

        try {
            JsonObject jsonCompra = gson.fromJson(jsonData, JsonObject.class);

            // Obtener usuario de sesión
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                enviarErrorJson(response, "Sesión no válida");
                return;
            }
            Long usuarioId = (Long) session.getAttribute("userId");

            // Validar datos requeridos
            if (!jsonCompra.has("detalles") || jsonCompra.getAsJsonArray("detalles").size() == 0) {
                enviarErrorJson(response, "Debe agregar al menos un producto");
                return;
            }

            // Crear transacción
            TransaccionDTO transaccion = new TransaccionDTO();
            transaccion.setTipoTransaccionId(TransaccionDTO.TIPO_COMPRA);
            transaccion.setUsuarioId(usuarioId);
            
            if (jsonCompra.has("proveedorId") && !jsonCompra.get("proveedorId").isJsonNull()) {
                transaccion.setProveedorId(jsonCompra.get("proveedorId").getAsLong());
            }
            
            if (jsonCompra.has("observaciones") && !jsonCompra.get("observaciones").isJsonNull()) {
                transaccion.setObservaciones(jsonCompra.get("observaciones").getAsString());
            }

            // Procesar detalles
            List<DetalleTransaccionDTO> detalles = new ArrayList<>();
            JsonArray jsonDetalles = jsonCompra.getAsJsonArray("detalles");
            
            BigDecimal subtotalGeneral = BigDecimal.ZERO;
            
            for (int i = 0; i < jsonDetalles.size(); i++) {
                JsonObject jsonDetalle = jsonDetalles.get(i).getAsJsonObject();
                
                DetalleTransaccionDTO detalle = new DetalleTransaccionDTO();
                detalle.setCatalogoProductoId(jsonDetalle.get("catalogoProductoId").getAsLong());
                detalle.setCantidad(jsonDetalle.get("cantidad").getAsInt());
                detalle.setPrecioUnitario(jsonDetalle.get("precioCompra").getAsBigDecimal());
                detalle.setPrecioVenta(jsonDetalle.get("precioVenta").getAsBigDecimal());
                detalle.setLote(jsonDetalle.get("lote").getAsString());
                detalle.setFechaVencimiento(jsonDetalle.get("fechaVencimiento").getAsString());
                detalle.setNombreComercial(jsonDetalle.has("nombreComercial") ? 
                        jsonDetalle.get("nombreComercial").getAsString() : "");
                
                detalle.calcularSubtotal();
                subtotalGeneral = subtotalGeneral.add(detalle.getSubtotal());
                
                // Validar detalle
                if (!detalle.esValido()) {
                    enviarErrorJson(response, "Datos incompletos en el producto " + (i + 1));
                    return;
                }
                
                detalles.add(detalle);
            }

            // Calcular totales
            boolean precioIncluyeIgv = jsonCompra.has("precioIncluyeIgv") && 
                    jsonCompra.get("precioIncluyeIgv").getAsBoolean();
            
            if (precioIncluyeIgv) {
                // El precio ya incluye IGV, calcular base
                BigDecimal factor = new BigDecimal("1.18");
                transaccion.setTotal(subtotalGeneral);
                transaccion.setSubtotal(subtotalGeneral.divide(factor, 2, BigDecimal.ROUND_HALF_UP));
                transaccion.setIgv(transaccion.getTotal().subtract(transaccion.getSubtotal()));
            } else {
                // Precio sin IGV
                transaccion.setSubtotal(subtotalGeneral);
                transaccion.setIgv(subtotalGeneral.multiply(new BigDecimal("0.18")).setScale(2, BigDecimal.ROUND_HALF_UP));
                transaccion.setTotal(subtotalGeneral.add(transaccion.getIgv()));
            }

            transaccion.setDetalles(detalles);
            transaccion.setMetodoPago("EFECTIVO");
            transaccion.setEstado("COMPLETADA");

            // Registrar
            TransaccionDTO registrada = compraApiClient.registrarCompra(transaccion, detalles);
            if (registrada != null && registrada.getId() != null) {
                JsonObject obj = new JsonObject();
                obj.addProperty("success", true);
                obj.addProperty("transaccionId", registrada.getId());
                obj.addProperty("numeroTransaccion", registrada.getNumeroTransaccion());
                obj.addProperty("total", registrada.getTotal());
                obj.addProperty("mensaje", "Compra registrada exitosamente");
                enviarJsonResponse(response, obj.toString());
            } else {
                enviarErrorJson(response, "Error al registrar la compra");
            }

        } catch (Exception e) {
            e.printStackTrace();
            enviarErrorJson(response, "Error al procesar la compra: " + e.getMessage());
        }
    }

    // =====================================================
    //              LISTADO Y DETALLE
    // =====================================================

    private void listarCompras(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int pagina = 1;
        int porPagina = 20;

        try {
            if (request.getParameter("pagina") != null) {
                pagina = Integer.parseInt(request.getParameter("pagina"));
            }
            if (request.getParameter("porPagina") != null) {
                porPagina = Integer.parseInt(request.getParameter("porPagina"));
            }
        } catch (NumberFormatException e) {
            // Usar valores por defecto
        }

        List<TransaccionDTO> compras = compraApiClient.listarCompras(pagina, porPagina);
        
        JsonArray jsonArray = new JsonArray();
        for (TransaccionDTO c : compras) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", c.getId());
            obj.addProperty("numero", c.getNumeroTransaccion());
            obj.addProperty("fecha", c.getFecha().toString());
            obj.addProperty("proveedor", c.getProveedor() != null ? c.getProveedor().getRazonSocial() : "Sin proveedor");
            obj.addProperty("subtotal", c.getSubtotal());
            obj.addProperty("igv", c.getIgv());
            obj.addProperty("total", c.getTotal());
            obj.addProperty("estado", c.getEstado());
            jsonArray.add(obj);
        }

        enviarJsonResponse(response, jsonArray.toString());
    }

    private void verDetalleCompra(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String idParam = request.getParameter("id");
        if (idParam == null) {
            enviarErrorJson(response, "ID no proporcionado");
            return;
        }

        try {
            Long id = Long.parseLong(idParam);
            TransaccionDTO compra = compraApiClient.buscarCompraPorId(id);
            
            if (compra == null) {
                enviarErrorJson(response, "Compra no encontrada");
                return;
            }

            enviarJsonResponse(response, gson.toJson(compra));

        } catch (NumberFormatException e) {
            enviarErrorJson(response, "ID inválido");
        }
    }

    private void anularCompra(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String idParam = request.getParameter("id");
        if (idParam == null) {
            enviarErrorJson(response, "ID no proporcionado");
            return;
        }

        try {
            Long id = Long.parseLong(idParam);
            TransaccionDTO compra = compraApiClient.buscarCompraPorId(id);
            
            if (compra == null) {
                enviarErrorJson(response, "Compra no encontrada");
                return;
            }

            compraApiClient.anularCompra(id);
            JsonObject obj = new JsonObject();
            obj.addProperty("success", true);
            obj.addProperty("mensaje", "Compra anulada correctamente");
            enviarJsonResponse(response, obj.toString());

        } catch (NumberFormatException e) {
            enviarErrorJson(response, "ID inválido");
        }
    }

    // =====================================================
    //              MÉTODOS AUXILIARES
    // =====================================================

    private JsonObject construirJsonProducto(CatalogoProductoDTO producto) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", producto.getId());
        obj.addProperty("codigo", producto.getCodigoProducto());
        obj.addProperty("nombre", producto.getNombreComercial());
        obj.addProperty("principioActivo", producto.getPrincipioActivo());
        obj.addProperty("concentracion", producto.getConcentracion());
        obj.addProperty("formaFarmaceutica", producto.getFormaFarmaceutica());
        obj.addProperty("presentacion", producto.getPresentacion());
        obj.addProperty("laboratorio", producto.getLaboratorio());
        obj.addProperty("registroSanitario", producto.getRegistroSanitario());
        obj.addProperty("cantidad", producto.getCantidad());
        obj.addProperty("nombreFabricante", producto.getNombreFabricante());
        obj.addProperty("descripcionCorta", producto.getDescripcionCorta());
        obj.addProperty("descripcionCompleta", producto.getDescripcionCompleta());
        return obj;
    }

    private void enviarJsonResponse(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
            out.flush();
        }
    }

    private void enviarErrorJson(HttpServletResponse response, String mensaje) throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", false);
        obj.addProperty("error", mensaje);
        enviarJsonResponse(response, obj.toString());
    }
}
