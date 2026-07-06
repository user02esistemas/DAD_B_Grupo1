package integration.api;

import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;

public class ReporteApiClient {

    private final WebApiClient apiClient = new WebApiClient();

    public JsonObject obtenerReporteVentas(String desde, String hasta) throws IOException {
        ApiResult result = apiClient.get("/api/reportes/ventas?desde=" + desde + "&hasta=" + hasta);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonObject data = result.getJson().getAsJsonObject("data");
        if (data == null || data.isJsonNull()) {
            throw new IOException("Reporte de ventas sin datos");
        }
        return data;
    }

    public JsonObject obtenerReporteVencimientos(int diasDesde, int diasHasta) throws IOException {
        ApiResult result = apiClient.get("/api/reportes/vencimientos?diasDesde=" + diasDesde + "&diasHasta=" + diasHasta);
        return obtenerData(result, "Reporte de vencimientos sin datos");
    }

    public JsonObject obtenerReporteVencimientosRango(String desde, String hasta) throws IOException {
        ApiResult result = apiClient.get("/api/reportes/vencimientos-rango?desde=" + desde + "&hasta=" + hasta);
        return obtenerData(result, "Reporte de vencimientos sin datos");
    }

    public JsonObject obtenerCriticidadVencimientos() throws IOException {
        ApiResult result = apiClient.get("/api/reportes/vencimientos/criticidad");
        return obtenerData(result, "Criticidad de vencimientos sin datos");
    }

    private JsonObject obtenerData(ApiResult result, String mensajeSinDatos) throws IOException {
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonObject data = result.getJson().getAsJsonObject("data");
        if (data == null || data.isJsonNull()) {
            throw new IOException(mensajeSinDatos);
        }
        return data;
    }
}
