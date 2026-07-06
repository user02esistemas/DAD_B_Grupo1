package integration.api;

import DTO.CatalogoProductoDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CatalogoApiClient {

    private final WebApiClient apiClient = new WebApiClient();

    public List<CatalogoProductoDTO> buscar(String termino, int limite) throws IOException {
        ApiResult result = apiClient.get("/api/catalogo?q=" + URLEncoder.encode(termino, StandardCharsets.UTF_8) + "&porPagina=" + limite);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return toProductos(result.getJson().getAsJsonArray("data"));
    }

    public CatalogoProductoDTO buscarPorId(Long id) throws IOException {
        ApiResult result = apiClient.get("/api/catalogo/" + id);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return toProducto(result.getJson().getAsJsonObject("data"));
    }

    public List<CatalogoProductoDTO> listarPaginado(int pagina, int porPagina) throws IOException {
        ApiResult result = apiClient.get("/api/catalogo?pagina=" + pagina + "&porPagina=" + porPagina);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonObject data = result.getJson().getAsJsonObject("data");
        return toProductos(data.getAsJsonArray("productos"));
    }

    public int contarTotal() throws IOException {
        return (Integer) obtenerEstadisticas().get("total");
    }

    public Map<String, Object> obtenerEstadisticas() throws IOException {
        ApiResult result = apiClient.get("/api/catalogo/estadisticas");
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonObject data = result.getJson().getAsJsonObject("data");
        Map<String, Object> estadisticas = new LinkedHashMap<>();
        estadisticas.put("total", getInt(data, "total"));
        estadisticas.put("activos", getInt(data, "activos"));
        estadisticas.put("laboratorios", getInt(data, "laboratorios"));
        estadisticas.put("ultimoAgregado", getString(data, "ultimoAgregado"));
        return estadisticas;
    }

    public CatalogoProductoDTO guardar(CatalogoProductoDTO producto) throws IOException {
        ApiResult result = producto.getId() == null
                ? apiClient.post("/api/catalogo", producto)
                : apiClient.put("/api/catalogo/" + producto.getId(), producto);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return toProducto(result.getJson().getAsJsonObject("data"));
    }

    public void desactivar(Long id) throws IOException {
        ApiResult result = apiClient.delete("/api/catalogo/" + id);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
    }

    private List<CatalogoProductoDTO> toProductos(JsonArray data) {
        List<CatalogoProductoDTO> productos = new ArrayList<>();
        if (data == null) {
            return productos;
        }
        for (JsonElement element : data) {
            if (element.isJsonObject()) {
                productos.add(toProducto(element.getAsJsonObject()));
            }
        }
        return productos;
    }

    private CatalogoProductoDTO toProducto(JsonObject data) {
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
        producto.setNombreTitular(getString(data, "nombreTitular"));
        producto.setNombreFabricante(getString(data, "nombreFabricante"));
        producto.setActivo(getBoolean(data, "activo"));
        producto.setCreatedAt(getTimestamp(data, "createdAt"));
        return producto;
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

    private Timestamp getTimestamp(JsonObject object, String key) {
        String value = getString(object, key);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Timestamp.valueOf(value.replace('T', ' ').substring(0, 19));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
