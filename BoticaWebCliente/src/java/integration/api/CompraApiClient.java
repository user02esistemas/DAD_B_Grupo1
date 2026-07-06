package integration.api;

import DTO.CatalogoProductoDTO;
import DTO.DetalleTransaccionDTO;
import DTO.ProductoDTO;
import DTO.ProveedorDTO;
import DTO.TransaccionDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompraApiClient {

    private final WebApiClient apiClient = new WebApiClient();

    public List<CatalogoProductoDTO> buscarProductosCatalogo(String termino, int limite) throws IOException {
        ApiResult result = apiClient.get("/api/compras/productos?q=" + encode(termino) + "&limite=" + limite);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return toCatalogoList(result.getJson().getAsJsonArray("data"));
    }

    public CatalogoProductoDTO buscarProductoCatalogoPorCodigo(String codigo) throws IOException {
        ApiResult result = apiClient.get("/api/compras/productos/codigo?codigo=" + encode(codigo));
        if (!result.isSuccess()) {
            return null;
        }
        return toCatalogo(result.getJson().getAsJsonObject("data"));
    }

    public CatalogoProductoDTO buscarProductoCatalogoPorId(Long id) throws IOException {
        ApiResult result = apiClient.get("/api/compras/productos/" + id);
        if (!result.isSuccess()) {
            return null;
        }
        return toCatalogo(result.getJson().getAsJsonObject("data"));
    }

    public StockCatalogo obtenerStockCatalogo(Long catalogoId) throws IOException {
        ApiResult result = apiClient.get("/api/compras/productos/" + catalogoId + "/stock");
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonObject data = result.getJson().getAsJsonObject("data");
        return new StockCatalogo(getInt(data, "stockTotal"), getBigDecimal(data, "precioCompraPromedio"), toProductos(data.getAsJsonArray("lotes")));
    }

    public List<ProveedorDTO> listarProveedoresActivos() throws IOException {
        return leerProveedores(apiClient.get("/api/compras/proveedores"));
    }

    public List<ProveedorDTO> buscarProveedores(String termino) throws IOException {
        return leerProveedores(apiClient.get("/api/compras/proveedores?q=" + encode(termino)));
    }

    public ProveedorDTO registrarProveedor(ProveedorDTO proveedor) throws IOException {
        ApiResult result = apiClient.post("/api/compras/proveedores", proveedor);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return toProveedor(result.getJson().getAsJsonObject("data"));
    }

    public TransaccionDTO registrarCompra(TransaccionDTO compra, List<DetalleTransaccionDTO> detalles) throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("usuarioId", compra.getUsuarioId());
        payload.put("proveedorId", compra.getProveedorId());
        payload.put("metodoPago", compra.getMetodoPago());
        payload.put("tipoComprobante", compra.getTipoComprobante());
        payload.put("observaciones", compra.getObservaciones());
        payload.put("subtotal", compra.getSubtotal());
        payload.put("igv", compra.getIgv());
        payload.put("total", compra.getTotal());
        payload.put("estado", compra.getEstado());
        payload.put("detalles", detalles);

        ApiResult result = apiClient.post("/api/compras", payload);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return toTransaccion(result.getJson().getAsJsonObject("data"));
    }

    public List<TransaccionDTO> listarCompras(int pagina, int porPagina) throws IOException {
        ApiResult result = apiClient.get("/api/compras?pagina=" + pagina + "&porPagina=" + porPagina);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonObject data = result.getJson().getAsJsonObject("data");
        return toTransacciones(data.getAsJsonArray("compras"));
    }

    public TransaccionDTO buscarCompraPorId(Long id) throws IOException {
        ApiResult result = apiClient.get("/api/compras/" + id);
        if (!result.isSuccess()) {
            return null;
        }
        return toTransaccion(result.getJson().getAsJsonObject("data"));
    }

    public void anularCompra(Long id) throws IOException {
        ApiResult result = apiClient.delete("/api/compras/" + id);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
    }

    private List<ProveedorDTO> leerProveedores(ApiResult result) throws IOException {
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        List<ProveedorDTO> proveedores = new ArrayList<>();
        JsonArray data = result.getJson().getAsJsonArray("data");
        if (data == null) {
            return proveedores;
        }
        for (JsonElement element : data) {
            if (element.isJsonObject()) {
                proveedores.add(toProveedor(element.getAsJsonObject()));
            }
        }
        return proveedores;
    }

    private List<CatalogoProductoDTO> toCatalogoList(JsonArray data) {
        List<CatalogoProductoDTO> productos = new ArrayList<>();
        if (data == null) {
            return productos;
        }
        for (JsonElement element : data) {
            if (element.isJsonObject()) {
                productos.add(toCatalogo(element.getAsJsonObject()));
            }
        }
        return productos;
    }

    private List<ProductoDTO> toProductos(JsonArray data) {
        List<ProductoDTO> productos = new ArrayList<>();
        if (data == null) {
            return productos;
        }
        for (JsonElement element : data) {
            if (element.isJsonObject()) {
                JsonObject item = element.getAsJsonObject();
                ProductoDTO producto = new ProductoDTO();
                producto.setId(getLong(item, "id"));
                producto.setCatalogoProductoId(getLong(item, "catalogoProductoId"));
                producto.setLote(getString(item, "lote"));
                producto.setFechaVencimiento(getDate(item, "fechaVencimiento"));
                producto.setStockActual(getInt(item, "stockActual"));
                producto.setStockMinimo(getInt(item, "stockMinimo"));
                producto.setPrecioCompra(getBigDecimal(item, "precioCompra"));
                producto.setPrecioVenta(getBigDecimal(item, "precioVenta"));
                producto.setActivo(getBoolean(item, "activo"));
                productos.add(producto);
            }
        }
        return productos;
    }

    private List<TransaccionDTO> toTransacciones(JsonArray data) {
        List<TransaccionDTO> compras = new ArrayList<>();
        if (data == null) {
            return compras;
        }
        for (JsonElement element : data) {
            if (element.isJsonObject()) {
                compras.add(toTransaccion(element.getAsJsonObject()));
            }
        }
        return compras;
    }

    private TransaccionDTO toTransaccion(JsonObject data) {
        TransaccionDTO compra = new TransaccionDTO();
        compra.setId(getLong(data, "id"));
        compra.setTipoTransaccionId(getLong(data, "tipoTransaccionId"));
        compra.setNumeroTransaccion(getString(data, "numeroTransaccion"));
        compra.setUsuarioId(getLong(data, "usuarioId"));
        compra.setProveedorId(getLong(data, "proveedorId"));
        compra.setFecha(getTimestamp(data, "fecha"));
        compra.setSubtotal(getBigDecimal(data, "subtotal"));
        compra.setIgv(getBigDecimal(data, "igv"));
        compra.setTotal(getBigDecimal(data, "total"));
        compra.setMetodoPago(getString(data, "metodoPago"));
        compra.setEstado(getString(data, "estado"));
        compra.setObservaciones(getString(data, "observaciones"));
        if (data.has("proveedor") && data.get("proveedor").isJsonObject()) {
            compra.setProveedor(toProveedor(data.getAsJsonObject("proveedor")));
        }
        if (data.has("detalles") && data.get("detalles").isJsonArray()) {
            compra.setDetalles(toDetalles(data.getAsJsonArray("detalles")));
        }
        return compra;
    }

    private List<DetalleTransaccionDTO> toDetalles(JsonArray data) {
        List<DetalleTransaccionDTO> detalles = new ArrayList<>();
        for (JsonElement element : data) {
            if (element.isJsonObject()) {
                JsonObject item = element.getAsJsonObject();
                DetalleTransaccionDTO detalle = new DetalleTransaccionDTO();
                detalle.setId(getLong(item, "id"));
                detalle.setTransaccionId(getLong(item, "transaccionId"));
                detalle.setProductoId(getLong(item, "productoId"));
                detalle.setCatalogoProductoId(getLong(item, "catalogoProductoId"));
                detalle.setCantidad(getInt(item, "cantidad"));
                detalle.setPrecioUnitario(getBigDecimal(item, "precioUnitario"));
                detalle.setSubtotal(getBigDecimal(item, "subtotal"));
                detalle.setLote(getString(item, "lote"));
                detalle.setFechaVencimiento(getString(item, "fechaVencimiento"));
                detalle.setPrecioVenta(getBigDecimal(item, "precioVenta"));
                detalle.setNombreComercial(getString(item, "nombreComercial"));
                detalle.setConcentracion(getString(item, "concentracion"));
                detalles.add(detalle);
            }
        }
        return detalles;
    }

    private CatalogoProductoDTO toCatalogo(JsonObject data) {
        CatalogoProductoDTO producto = new CatalogoProductoDTO();
        producto.setId(getLong(data, "id"));
        producto.setCodigoProducto(getString(data, "codigoProducto"));
        producto.setNombreComercial(getString(data, "nombreComercial"));
        producto.setPrincipioActivo(getString(data, "principioActivo"));
        producto.setConcentracion(getString(data, "concentracion"));
        producto.setFormaFarmaceutica(getString(data, "formaFarmaceutica"));
        producto.setPresentacion(getString(data, "presentacion"));
        producto.setLaboratorio(getString(data, "laboratorio"));
        producto.setRegistroSanitario(getString(data, "registroSanitario"));
        producto.setCantidad(getInt(data, "cantidad"));
        producto.setNombreFabricante(getString(data, "nombreFabricante"));
        producto.setActivo(getBoolean(data, "activo"));
        return producto;
    }

    private ProveedorDTO toProveedor(JsonObject data) {
        ProveedorDTO proveedor = new ProveedorDTO();
        proveedor.setId(getLong(data, "id"));
        proveedor.setRuc(getString(data, "ruc"));
        proveedor.setRazonSocial(getString(data, "razonSocial"));
        proveedor.setContacto(getString(data, "contacto"));
        proveedor.setTelefono(getString(data, "telefono"));
        proveedor.setEmail(getString(data, "email"));
        proveedor.setDireccion(getString(data, "direccion"));
        proveedor.setActivo(getBoolean(data, "activo"));
        return proveedor;
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String getString(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : null;
    }

    private Long getLong(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsLong() : null;
    }

    private int getInt(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsInt() : 0;
    }

    private boolean getBoolean(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() && object.get(key).getAsBoolean();
    }

    private BigDecimal getBigDecimal(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsBigDecimal() : BigDecimal.ZERO;
    }

    private Date getDate(JsonObject object, String key) {
        String value = getString(object, key);
        return value == null || value.trim().isEmpty() ? null : Date.valueOf(value.substring(0, 10));
    }

    private Timestamp getTimestamp(JsonObject object, String key) {
        String value = getString(object, key);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Timestamp.valueOf(value.replace('T', ' ').substring(0, 19));
    }

    public static class StockCatalogo {
        private final int stockTotal;
        private final BigDecimal precioCompraPromedio;
        private final List<ProductoDTO> lotes;

        StockCatalogo(int stockTotal, BigDecimal precioCompraPromedio, List<ProductoDTO> lotes) {
            this.stockTotal = stockTotal;
            this.precioCompraPromedio = precioCompraPromedio;
            this.lotes = lotes;
        }

        public int getStockTotal() {
            return stockTotal;
        }

        public BigDecimal getPrecioCompraPromedio() {
            return precioCompraPromedio;
        }

        public List<ProductoDTO> getLotes() {
            return lotes;
        }
    }
}
