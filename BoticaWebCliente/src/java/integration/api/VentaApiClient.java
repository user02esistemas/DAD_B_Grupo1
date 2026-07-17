package integration.api;

import DTO.DetalleTransaccionDTO;
import DTO.TransaccionDTO;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VentaApiClient {

    private final WebApiClient apiClient = new WebApiClient();

    public JsonObject registrarVenta(Long usuarioId, String cliente, String metodoPago,
            String tipoComprobante, BigDecimal montoEfectivo, BigDecimal montoVirtual,
            String medioPagoVirtual, BigDecimal vuelto, List<DetalleTransaccionDTO> detalles)
            throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("usuarioId", usuarioId);
        body.put("clienteNombre", cliente);
        body.put("metodoPago", metodoPago);
        body.put("tipoComprobante", tipoComprobante);
        body.put("montoEfectivo", montoEfectivo);
        body.put("montoVirtual", montoVirtual);
        body.put("medioPagoVirtual", medioPagoVirtual);
        body.put("vuelto", vuelto);
        body.put("detalles", toDetalles(detalles));
        ApiResult result = apiClient.post("/api/ventas", body);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return result.getJson().getAsJsonObject("data");
    }

    public JsonObject buscarPorId(Long id) throws IOException {
        ApiResult result = apiClient.get("/api/ventas/" + id);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return result.getJson().getAsJsonObject("data");
    }

    public TransaccionDTO buscarTransaccionPorId(Long id) throws IOException {
        return toTransaccion(buscarPorId(id));
    }

    public VentasResult listarVentas(String termino, int pagina, int porPagina) throws IOException {
        String path = "/api/ventas?pagina=" + pagina + "&porPagina=" + porPagina;
        if (termino != null && !termino.trim().isEmpty()) {
            path += "&termino=" + URLEncoder.encode(termino.trim(), StandardCharsets.UTF_8);
        }
        ApiResult result = apiClient.get(path);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonObject data = result.getJson().getAsJsonObject("data");
        List<TransaccionDTO> ventas = new ArrayList<>();
        JsonArray ventasJson = data.getAsJsonArray("ventas");
        if (ventasJson != null) {
            for (JsonElement element : ventasJson) {
                if (element.isJsonObject()) {
                    ventas.add(toTransaccion(element.getAsJsonObject()));
                }
            }
        }
        return new VentasResult(ventas, getInt(data, "total"));
    }

    private TransaccionDTO toTransaccion(JsonObject json) {
        TransaccionDTO venta = new TransaccionDTO();
        venta.setId(getLong(json, "id"));
        venta.setTipoTransaccionId(getLong(json, "tipoTransaccionId"));
        venta.setNumeroTransaccion(getString(json, "numeroTransaccion"));
        venta.setUsuarioId(getLong(json, "usuarioId"));
        venta.setNombrePersona(getString(json, "nombrePersona"));
        venta.setCliente(getString(json, "cliente") != null ? getString(json, "cliente") : getString(json, "nombrePersona"));
        venta.setFecha(getTimestamp(json, "fecha"));
        venta.setSubtotal(getBigDecimal(json, "subtotal"));
        venta.setIgv(getBigDecimal(json, "igv"));
        venta.setTotal(getBigDecimal(json, "total"));
        venta.setMetodoPago(getString(json, "metodoPago"));
        venta.setMontoEfectivo(getBigDecimal(json, "montoEfectivo"));
        venta.setMontoVirtual(getBigDecimal(json, "montoVirtual"));
        venta.setMedioPagoVirtual(getString(json, "medioPagoVirtual"));
        venta.setVuelto(getBigDecimal(json, "vuelto"));
        venta.setTipoComprobante(getString(json, "tipoComprobante"));
        venta.setEstado(getString(json, "estado"));
        venta.setObservaciones(getString(json, "observaciones"));
        venta.setTipoTransaccionNombre(getString(json, "tipoTransaccionNombre"));
        venta.setUsuarioNombre(getString(json, "usuarioNombre"));
        JsonArray detalles = json.getAsJsonArray("detalles");
        if (detalles != null) {
            List<DetalleTransaccionDTO> detalleDtos = new ArrayList<>();
            for (JsonElement element : detalles) {
                if (element.isJsonObject()) {
                    detalleDtos.add(toDetalle(element.getAsJsonObject()));
                }
            }
            venta.setDetalles(detalleDtos);
        }
        return venta;
    }

    private DetalleTransaccionDTO toDetalle(JsonObject json) {
        DetalleTransaccionDTO detalle = new DetalleTransaccionDTO();
        detalle.setId(getLong(json, "id"));
        detalle.setTransaccionId(getLong(json, "transaccionId"));
        detalle.setProductoId(getLong(json, "productoId"));
        detalle.setCantidad(getInt(json, "cantidad"));
        detalle.setPrecioUnitario(getBigDecimal(json, "precioUnitario"));
        detalle.setSubtotal(getBigDecimal(json, "subtotal"));
        detalle.setNombreComercial(getString(json, "nombreComercial"));
        detalle.setConcentracion(getString(json, "concentracion"));
        detalle.setLote(getString(json, "lote"));
        detalle.setFechaVencimiento(getString(json, "fechaVencimiento"));
        return detalle;
    }

    private JsonArray toDetalles(List<DetalleTransaccionDTO> detalles) {
        JsonArray array = new JsonArray();
        for (DetalleTransaccionDTO detalle : detalles) {
            JsonObject item = new JsonObject();
            item.addProperty("productoId", detalle.getProductoId());
            item.addProperty("cantidad", detalle.getCantidad());
            item.addProperty("precioUnitario", detalle.getPrecioUnitario());
            item.addProperty("subtotal", detalle.getSubtotal());
            array.add(item);
        }
        return array;
    }

    private String getString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private Long getLong(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsLong() : null;
    }

    private int getInt(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : 0;
    }

    private BigDecimal getBigDecimal(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsBigDecimal() : null;
    }

    private Timestamp getTimestamp(JsonObject json, String key) {
        return ApiDateParser.toTimestamp(getString(json, key));
    }

    public static class VentasResult {
        private final List<TransaccionDTO> ventas;
        private final int total;

        VentasResult(List<TransaccionDTO> ventas, int total) {
            this.ventas = ventas;
            this.total = total;
        }

        public List<TransaccionDTO> getVentas() {
            return ventas;
        }

        public int getTotal() {
            return total;
        }
    }
}
