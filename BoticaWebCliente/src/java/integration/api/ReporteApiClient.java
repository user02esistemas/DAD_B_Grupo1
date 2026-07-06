package integration.api;

import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

    public JsonObject obtenerSesionesCaja(String desde, String hasta, Long usuarioId) throws IOException {
        StringBuilder path = new StringBuilder("/api/reportes/caja/sesiones?");
        append(path, "desde", desde);
        append(path, "hasta", hasta);
        if (usuarioId != null) {
            append(path, "usuarioId", String.valueOf(usuarioId));
        }
        ApiResult result = apiClient.get(path.toString());
        JsonObject data = new JsonObject();
        data.add("sesiones", obtenerArrayData(result, "Sesiones de caja sin datos"));
        return data;
    }

    public JsonObject obtenerDetalleSesionCaja(Long sesionId) throws IOException {
        ApiResult result = apiClient.get("/api/reportes/caja/detalle?sesionId=" + sesionId);
        return obtenerData(result, "Detalle de sesion sin datos");
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

    private com.google.gson.JsonArray obtenerArrayData(ApiResult result, String mensajeSinDatos) throws IOException {
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        com.google.gson.JsonArray data = result.getJson().getAsJsonArray("data");
        if (data == null || data.isJsonNull()) {
            throw new IOException(mensajeSinDatos);
        }
        return data;
    }

    private void append(StringBuilder path, String key, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (path.charAt(path.length() - 1) != '?') {
            path.append('&');
        }
        path.append(key).append('=').append(URLEncoder.encode(value.trim(), StandardCharsets.UTF_8));
    }
}
