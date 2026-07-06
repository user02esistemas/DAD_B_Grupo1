package integration.api;

import DTO.ProveedorDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompraApiClient {

    private final WebApiClient apiClient = new WebApiClient();

    public List<ProveedorDTO> listarProveedoresActivos() throws IOException {
        ApiResult result = apiClient.get("/api/compras/proveedores");
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }

        List<ProveedorDTO> proveedores = new ArrayList<>();
        JsonArray data = result.getJson().getAsJsonArray("data");
        if (data == null) {
            return proveedores;
        }
        for (JsonElement element : data) {
            if (element.isJsonObject()) {
                proveedores.add(toProveedor(element.getAsJsonObject()));
            }
        }
        return proveedores;
    }

    private ProveedorDTO toProveedor(JsonObject data) {
        ProveedorDTO proveedor = new ProveedorDTO();
        proveedor.setId(getLong(data, "id"));
        proveedor.setRuc(getString(data, "ruc"));
        proveedor.setRazonSocial(getString(data, "razonSocial"));
        proveedor.setContacto(getString(data, "contacto"));
        proveedor.setTelefono(getString(data, "telefono"));
        proveedor.setEmail(getString(data, "email"));
        proveedor.setDireccion(getString(data, "direccion"));
        proveedor.setActivo(getBoolean(data, "activo"));
        return proveedor;
    }

    private String getString(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull()
                ? object.get(key).getAsString()
                : null;
    }

    private Long getLong(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull()
                ? object.get(key).getAsLong()
                : null;
    }

    private boolean getBoolean(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() && object.get(key).getAsBoolean();
    }
}
