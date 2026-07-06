package integration.api;

import DTO.SesionCajaDTO;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class CajaApiClient {

    private final WebApiClient apiClient = new WebApiClient();

    public SesionCajaDTO buscarSesionAbierta(Long usuarioId) throws IOException {
        ApiResult result = apiClient.get("/api/caja/estado?usuarioId=" + usuarioId);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonElement data = result.getJson().get("data");
        return data != null && data.isJsonObject() ? toSesion(data.getAsJsonObject()) : null;
    }

    public SesionCajaDTO abrir(Long usuarioId, Long cajaId, BigDecimal montoInicial) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("usuarioId", usuarioId);
        body.put("cajaId", cajaId);
        body.put("montoInicial", montoInicial == null ? BigDecimal.ZERO : montoInicial);
        ApiResult result = apiClient.post("/api/caja/abrir", body);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonElement data = result.getJson().get("data");
        return data != null && data.isJsonObject() ? toSesion(data.getAsJsonObject()) : null;
    }

    public void cerrar(Long usuarioId, BigDecimal montoFinal, String observaciones) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("usuarioId", usuarioId);
        body.put("montoFinal", montoFinal == null ? BigDecimal.ZERO : montoFinal);
        body.put("observaciones", observaciones);
        ApiResult result = apiClient.post("/api/caja/cerrar", body);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
    }

    private SesionCajaDTO toSesion(JsonObject json) {
        SesionCajaDTO sesion = new SesionCajaDTO();
        sesion.setId(getLong(json, "id"));
        sesion.setCajaId(getLong(json, "cajaId"));
        sesion.setUsuarioId(getLong(json, "usuarioId"));
        sesion.setFechaApertura(getTimestamp(json, "fechaApertura"));
        sesion.setFechaCierre(getTimestamp(json, "fechaCierre"));
        sesion.setMontoInicial(getBigDecimal(json, "montoInicial"));
        sesion.setMontoFinal(getBigDecimal(json, "montoFinal"));
        sesion.setTotalTransacciones(getBigDecimal(json, "totalTransacciones"));
        sesion.setTotalVentasEfectivo(getBigDecimal(json, "totalVentasEfectivo"));
        sesion.setTotalVentasVirtual(getBigDecimal(json, "totalVentasVirtual"));
        sesion.setTotalVueltos(getBigDecimal(json, "totalVueltos"));
        sesion.setEfectivoEsperado(getBigDecimal(json, "efectivoEsperado"));
        sesion.setEstado(getString(json, "estado"));
        sesion.setObservaciones(getString(json, "observaciones"));
        sesion.setCajaNombre(getString(json, "cajaNombre"));
        sesion.setUsuarioNombre(getString(json, "usuarioNombre"));
        return sesion;
    }

    private String getString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private Long getLong(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsLong() : null;
    }

    private BigDecimal getBigDecimal(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull()
                ? json.get(key).getAsBigDecimal()
                : BigDecimal.ZERO;
    }

    private Timestamp getTimestamp(JsonObject json, String key) {
        String value = getString(json, key);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Timestamp.valueOf(value.replace('T', ' ').substring(0, 19));
        } catch (Exception ex) {
            return null;
        }
    }
}
