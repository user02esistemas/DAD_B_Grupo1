package integration.api;

import DTO.CatalogoProductoDTO;
import DTO.ProductoDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Date;
import java.sql.Timestamp;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductoApiClient {

    private final WebApiClient apiClient = new WebApiClient();

    public List<ProductoVenta> buscarParaVenta(String termino, int limite) throws IOException {
        ApiResult result = apiClient.get("/api/productos?termino=" + URLEncoder.encode(termino, StandardCharsets.UTF_8) + "&limite=" + limite);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        List<ProductoVenta> productos = new ArrayList<>();
        JsonArray data = result.getJson().getAsJsonArray("data");
        if (data == null) {
            return productos;
        }
        for (JsonElement element : data) {
            if (element.isJsonObject()) {
                productos.add(toProductoVenta(element.getAsJsonObject()));
            }
        }
        return productos;
    }

    public ProductoVenta buscarVentaPorId(Long id) throws IOException {
        ApiResult result = apiClient.get("/api/productos/" + id);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return toProductoVenta(result.getJson().getAsJsonObject("data"));
    }

    public InventarioResult buscarInventario(String termino, String filtroStock, String filtroVencimiento, int pagina, int porPagina) throws IOException {
        StringBuilder path = new StringBuilder("/api/productos/inventario");
        path.append("?pagina=").append(pagina);
        path.append("&porPagina=").append(porPagina);
        append(path, "termino", termino);
        append(path, "filtroStock", filtroStock);
        append(path, "filtroVencimiento", filtroVencimiento);

        ApiResult result = apiClient.get(path.toString());
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }

        JsonObject data = result.getJson().getAsJsonObject("data");
        List<ProductoDTO> productos = new ArrayList<>();
        JsonArray productosJson = data.getAsJsonArray("productos");
        if (productosJson != null) {
            for (JsonElement element : productosJson) {
                if (element.isJsonObject()) {
                    productos.add(toProducto(element.getAsJsonObject()));
                }
            }
        }
        int total = getInt(data, "total");
        return new InventarioResult(productos, total);
    }

    public Map<String, Integer> obtenerEstadisticasInventario() throws IOException {
        ApiResult result = apiClient.get("/api/productos/inventario/estadisticas");
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }

        Map<String, Integer> estadisticas = new LinkedHashMap<>();
        JsonObject data = result.getJson().getAsJsonObject("data");
        estadisticas.put("totalProductos", getInt(data, "totalProductos"));
        estadisticas.put("stockBajo", getInt(data, "stockBajo"));
        estadisticas.put("agotados", getInt(data, "agotados"));
        estadisticas.put("porVencer", getInt(data, "porVencer"));
        return estadisticas;
    }

    private ProductoDTO toProducto(JsonObject data) {
        ProductoDTO producto = new ProductoDTO();
        producto.setId(getLong(data, "id"));
        producto.setCatalogoProductoId(getLong(data, "catalogoProductoId"));
        producto.setLote(getString(data, "lote"));
        producto.setFechaVencimiento(getDate(data, "fechaVencimiento"));
        producto.setStockActual(getInt(data, "stockActual"));
        producto.setStockMinimo(getInt(data, "stockMinimo"));
        producto.setPrecioCompra(getBigDecimal(data, "precioCompra"));
        producto.setPrecioVenta(getBigDecimal(data, "precioVenta"));
        producto.setUbicacion(getString(data, "ubicacion"));
        producto.setRequiereReceta(getBoolean(data, "requiereReceta"));
        producto.setEstado(getString(data, "estado"));
        producto.setActivo(getBoolean(data, "activo"));
        producto.setCreatedAt(getTimestamp(data, "createdAt"));
        producto.setUpdatedAt(getTimestamp(data, "updatedAt"));
        if (data.has("catalogoProducto") && data.get("catalogoProducto").isJsonObject()) {
            producto.setCatalogoProducto(toCatalogo(data.getAsJsonObject("catalogoProducto")));
        }
        return producto;
    }

    private ProductoVenta toProductoVenta(JsonObject data) {
        ProductoVenta producto = new ProductoVenta();
        producto.id = getLong(data, "id");
        producto.catalogoProductoId = getLong(data, "catalogoProductoId");
        producto.nombreComercial = getString(data, "nombreComercial");
        producto.concentracion = getString(data, "concentracion");
        producto.presentacion = getString(data, "presentacion");
        producto.laboratorio = getString(data, "laboratorio");
        producto.lote = getString(data, "lote");
        producto.fechaVencimiento = getString(data, "fechaVencimiento");
        producto.stockActual = getInt(data, "stockActual");
        producto.precioCompra = getBigDecimal(data, "precioCompra");
        producto.precioVenta = getBigDecimal(data, "precioVenta");
        return producto;
    }

    private CatalogoProductoDTO toCatalogo(JsonObject data) {
        CatalogoProductoDTO catalogo = new CatalogoProductoDTO();
        catalogo.setId(getLong(data, "id"));
        catalogo.setCodigoProducto(getString(data, "codigoProducto"));
        catalogo.setNombreComercial(getString(data, "nombreComercial"));
        catalogo.setPrincipioActivo(getString(data, "principioActivo"));
        catalogo.setConcentracion(getString(data, "concentracion"));
        catalogo.setFormaFarmaceutica(getString(data, "formaFarmaceutica"));
        catalogo.setPresentacion(getString(data, "presentacion"));
        catalogo.setLaboratorio(getString(data, "laboratorio"));
        catalogo.setRegistroSanitario(getString(data, "registroSanitario"));
        catalogo.setCantidad(getInt(data, "cantidad"));
        catalogo.setNombreTitular(getString(data, "nombreTitular"));
        catalogo.setNombreFabricante(getString(data, "nombreFabricante"));
        catalogo.setActivo(getBoolean(data, "activo"));
        catalogo.setCreatedAt(getTimestamp(data, "createdAt"));
        return catalogo;
    }

    private void append(StringBuilder path, String key, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        path.append('&').append(key).append('=').append(URLEncoder.encode(value.trim(), StandardCharsets.UTF_8));
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
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsBigDecimal() : null;
    }

    private Date getDate(JsonObject object, String key) {
        return ApiDateParser.toDate(getString(object, key));
    }

    private Timestamp getTimestamp(JsonObject object, String key) {
        return ApiDateParser.toTimestamp(getString(object, key));
    }

    public static class InventarioResult {
        private final List<ProductoDTO> productos;
        private final int total;

        InventarioResult(List<ProductoDTO> productos, int total) {
            this.productos = productos;
            this.total = total;
        }

        public List<ProductoDTO> getProductos() {
            return productos;
        }

        public int getTotal() {
            return total;
        }
    }

    public static class ProductoVenta {
        public Long id;
        public Long catalogoProductoId;
        public String nombreComercial;
        public String concentracion;
        public String presentacion;
        public String laboratorio;
        public String lote;
        public String fechaVencimiento;
        public int stockActual;
        public BigDecimal precioCompra;
        public BigDecimal precioVenta;
    }
}
