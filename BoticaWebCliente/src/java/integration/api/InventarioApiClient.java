package integration.api;

import DTO.MovimientoInventarioDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventarioApiClient {

    private final WebApiClient apiClient = new WebApiClient();

    public MovimientosResult listarMovimientos(String termino, int pagina, int porPagina) throws IOException {
        StringBuilder path = new StringBuilder("/api/inventario/movimientos?pagina=").append(pagina).append("&porPagina=").append(porPagina);
        if (termino != null && !termino.trim().isEmpty()) {
            path.append("&termino=").append(URLEncoder.encode(termino.trim(), StandardCharsets.UTF_8));
        }
        ApiResult result = apiClient.get(path.toString());
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonObject data = result.getJson().getAsJsonObject("data");
        List<MovimientoInventarioDTO> movimientos = new ArrayList<>();
        JsonArray items = data.getAsJsonArray("movimientos");
        if (items != null) {
            for (JsonElement element : items) {
                if (element.isJsonObject()) {
                    movimientos.add(toMovimiento(element.getAsJsonObject()));
                }
            }
        }
        return new MovimientosResult(movimientos, getInt(data, "total"));
    }

    public void ajustarStock(Long productoId, int nuevoStock, String motivo, Long usuarioId) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("productoId", productoId);
        body.put("nuevoStock", nuevoStock);
        body.put("motivo", motivo);
        body.put("usuarioId", usuarioId);
        ApiResult result = apiClient.post("/api/inventario/ajustar", body);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
    }

    private MovimientoInventarioDTO toMovimiento(JsonObject data) {
        MovimientoInventarioDTO movimiento = new MovimientoInventarioDTO();
        movimiento.setId(getLong(data, "id"));
        movimiento.setProductoId(getLong(data, "productoId"));
        movimiento.setTipoMovimiento(getString(data, "tipoMovimiento"));
        movimiento.setCantidad(getInt(data, "cantidad"));
        movimiento.setStockAnterior(getInt(data, "stockAnterior"));
        movimiento.setStockNuevo(getInt(data, "stockNuevo"));
        movimiento.setMotivo(getString(data, "motivo"));
        movimiento.setReferenciaId(getLong(data, "referenciaId"));
        movimiento.setReferenciaTipo(getString(data, "referenciaTipo"));
        movimiento.setUsuarioId(getLong(data, "usuarioId"));
        movimiento.setFechaMovimiento(getTimestamp(data, "fechaMovimiento"));
        movimiento.setUsuarioNombre(getString(data, "usuarioNombre"));
        movimiento.setProductoNombre(getString(data, "productoNombre"));
        movimiento.setLote(getString(data, "lote"));
        return movimiento;
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

    public static class MovimientosResult {
        private final List<MovimientoInventarioDTO> movimientos;
        private final int total;

        MovimientosResult(List<MovimientoInventarioDTO> movimientos, int total) {
            this.movimientos = movimientos;
            this.total = total;
        }

        public List<MovimientoInventarioDTO> getMovimientos() {
            return movimientos;
        }

        public int getTotal() {
            return total;
        }
    }
}
