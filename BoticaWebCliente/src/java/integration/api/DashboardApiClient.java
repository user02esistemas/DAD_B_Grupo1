package integration.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;

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
}
