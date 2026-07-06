package integration.api;

import DTO.DetalleTransaccionDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;
import java.math.BigDecimal;
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
}
