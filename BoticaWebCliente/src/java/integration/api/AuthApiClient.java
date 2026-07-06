package integration.api;

import DTO.RolDTO;
import DTO.UsuarioDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import integration.api.WebApiClient.ApiResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthApiClient {

    private final WebApiClient apiClient = new WebApiClient();

    public UsuarioDTO login(String username, String password) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        ApiResult result = apiClient.post("/api/auth/login", body);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        JsonObject data = result.getJson().getAsJsonObject("data");
        if (data == null || data.isJsonNull()) {
            throw new IOException("Login sin datos de usuario");
        }
        return toUsuario(data);
    }

    public void logout(String username) throws IOException {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        apiClient.post("/api/auth/logout", body);
    }

    private UsuarioDTO toUsuario(JsonObject data) {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(getLong(data, "id"));
        usuario.setUsername(getString(data, "username"));
        usuario.setEmail(getString(data, "email"));
        usuario.setNombreCompleto(getString(data, "nombreCompleto"));
        usuario.setDni(getString(data, "dni"));
        usuario.setTelefono(getString(data, "telefono"));
        usuario.setActivo(getBoolean(data, "activo"));
        usuario.setRoles(toRoles(data.getAsJsonArray("roles")));
        return usuario;
    }

    private List<RolDTO> toRoles(JsonArray rolesJson) {
        List<RolDTO> roles = new ArrayList<>();
        if (rolesJson == null) {
            return roles;
        }
        for (JsonElement element : rolesJson) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            RolDTO rol = new RolDTO();
            rol.setId(getLong(item, "id"));
            rol.setNombre(getString(item, "nombre"));
            rol.setDescripcion(getString(item, "descripcion"));
            roles.add(rol);
        }
        return roles;
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
