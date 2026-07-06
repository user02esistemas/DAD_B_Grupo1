package integration.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardApiClient {

    private final WebApiClient apiClient = new WebApiClient();

    public JsonObject obtenerResumen() throws IOException {
        ApiResult result = apiClient.get("/api/dashboard/resumen");
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonObject data = result.getJson().getAsJsonObject("data");
        if (data == null || data.isJsonNull()) {
            return new JsonObject();
        }
        return data;
    }

    public JsonArray obtenerProductosStockBajo(int limite) throws IOException {
        ApiResult result = apiClient.get("/api/dashboard/productos-alerta?limite=" + limite);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonElement data = result.getJson().get("data");
        return data != null && data.isJsonArray() ? data.getAsJsonArray() : new JsonArray();
    }

    public List<Map<String, Object>> obtenerProductosPorVencer(int limite) throws IOException {
        ApiResult result = apiClient.get("/api/dashboard/productos-vencer?limite=" + limite);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        List<Map<String, Object>> productos = new ArrayList<>();
        JsonElement data = result.getJson().get("data");
        if (data == null || !data.isJsonArray()) {
            return productos;
        }
        for (JsonElement element : data.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            Map<String, Object> producto = new HashMap<>();
            producto.put("nombre", getString(item, "nombreProducto"));
            producto.put("lote", getString(item, "lote"));
            producto.put("fechaVencimiento", getString(item, "fechaVencimiento"));
            producto.put("stockActual", getInt(item, "stockActual"));
            producto.put("diasRestantes", getInt(item, "diasParaVencer"));
            productos.add(producto);
        }
        return productos;
    }

    private String getString(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : null;
    }

    private int getInt(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsInt() : 0;
    }
}
