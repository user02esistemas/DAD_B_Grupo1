package integration.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
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
        normalizarResumen(data);
        return data;
    }

    private void normalizarResumen(JsonObject data) {
        normalizarProductos(data, "topProductos");
        normalizarProductos(data, "topProductosMes");
        normalizarUltimasVentas(data);
    }

    private void normalizarProductos(JsonObject data, String key) {
        if (!data.has(key) || !data.get(key).isJsonArray()) {
            return;
        }
        JsonArray normalizados = new JsonArray();
        for (JsonElement element : data.getAsJsonArray(key)) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            JsonObject producto = new JsonObject();
            producto.addProperty("nombre", getString(item, "nombreProducto"));
            producto.addProperty("cantidad", getInt(item, "cantidadVendida"));
            producto.add("total", item.has("totalVendido") ? item.get("totalVendido") : null);
            normalizados.add(producto);
        }
        data.add(key, normalizados);
    }

    private void normalizarUltimasVentas(JsonObject data) {
        if (!data.has("ultimasVentas") || !data.get("ultimasVentas").isJsonArray()) {
            return;
        }
        JsonArray normalizadas = new JsonArray();
        for (JsonElement element : data.getAsJsonArray("ultimasVentas")) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            JsonObject venta = new JsonObject();
            venta.addProperty("numero", getString(item, "numeroTransaccion"));
            venta.add("fecha", item.has("fecha") ? item.get("fecha") : null);
            venta.add("total", item.has("total") ? item.get("total") : null);
            venta.addProperty("metodoPago", getString(item, "metodoPago"));
            venta.addProperty("usuario", getString(item, "vendedor"));
            normalizadas.add(venta);
        }
        data.add("ultimasVentas", normalizadas);
    }

    public List<Map<String, Object>> obtenerProductosStockBajo(int limite) throws IOException {
        ApiResult result = apiClient.get("/api/dashboard/productos-alerta?limite=" + limite);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return toProductosAlerta(result.getJson().get("data"), true);
    }

    public List<Map<String, Object>> obtenerProductosPorVencer(int limite) throws IOException {
        ApiResult result = apiClient.get("/api/dashboard/productos-vencer?limite=" + limite);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return toProductosAlerta(result.getJson().get("data"), false);
    }

    private List<Map<String, Object>> toProductosAlerta(JsonElement data, boolean incluirStockMinimo) {
        List<Map<String, Object>> productos = new ArrayList<>();
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
            producto.put("stockActual", getInt(item, "stockActual"));
            if (incluirStockMinimo) {
                producto.put("stockMinimo", getInt(item, "stockMinimo"));
            } else {
                producto.put("fechaVencimiento", getString(item, "fechaVencimiento"));
                producto.put("diasRestantes", getInt(item, "diasParaVencer"));
            }
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
