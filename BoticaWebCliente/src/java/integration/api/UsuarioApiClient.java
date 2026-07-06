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

public class UsuarioApiClient {

    private final WebApiClient apiClient = new WebApiClient();

    public List<UsuarioDTO> listarTodos() throws IOException {
        ApiResult result = apiClient.get("/api/usuarios");
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        List<UsuarioDTO> usuarios = new ArrayList<>();
        JsonArray data = result.getJson().getAsJsonArray("data");
        if (data == null) {
            return usuarios;
        }
        for (JsonElement element : data) {
            if (element.isJsonObject()) {
                usuarios.add(toUsuario(element.getAsJsonObject()));
            }
        }
        return usuarios;
    }

    public UsuarioDTO buscarPorId(Long id) throws IOException {
        ApiResult result = apiClient.get("/api/usuarios/" + id);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return toUsuario(result.getJson().getAsJsonObject("data"));
    }

    public List<RolDTO> listarRoles() throws IOException {
        ApiResult result = apiClient.get("/api/usuarios/roles");
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
        return toRoles(result.getJson().getAsJsonArray("data"));
    }

    public void crear(UsuarioDTO usuario, List<Long> rolesIds) throws IOException {
        ApiResult result = apiClient.post("/api/usuarios", toBody(usuario, rolesIds, true));
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
    }

    public void actualizar(UsuarioDTO usuario, List<Long> rolesIds) throws IOException {
        ApiResult result = apiClient.put("/api/usuarios/" + usuario.getId(), toBody(usuario, rolesIds, false));
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
    }

    public void desactivar(Long id) throws IOException {
        ApiResult result = apiClient.delete("/api/usuarios/" + id);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
    }

    public void cambiarPassword(Long id, String nuevaPassword) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("password", nuevaPassword);
        ApiResult result = apiClient.put("/api/usuarios/" + id + "/password", body);
        if (!result.isSuccess()) {
            throw new IOException(result.getMessage());
        }
    }

    private Map<String, Object> toBody(UsuarioDTO usuario, List<Long> rolesIds, boolean incluyePassword) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", usuario.getUsername());
        body.put("email", usuario.getEmail());
        body.put("nombreCompleto", usuario.getNombreCompleto());
        body.put("dni", usuario.getDni());
        body.put("telefono", usuario.getTelefono());
        body.put("activo", usuario.isActivo());
        body.put("rolesIds", rolesIds);
        if (incluyePassword) {
            body.put("password", usuario.getPassword());
        }
        return body;
    }

    private UsuarioDTO toUsuario(JsonObject data) {
        if (data == null || data.isJsonNull()) {
            return null;
        }
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(getLong(data, "id"));
        usuario.setUsername(getString(data, "username"));
        usuario.setPassword(getString(data, "password"));
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
