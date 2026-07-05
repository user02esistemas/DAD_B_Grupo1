package api.auth;

import api.common.ApiResponse;
import api.config.RMIClientFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import rmi.auth.UsuarioServiceRMI;
import rmi.dto.RolDTO;
import rmi.dto.UsuarioDTO;

@WebServlet(name = "UsuarioApiServlet", urlPatterns = {"/api/usuarios", "/api/usuarios/*"})
public class UsuarioApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            UsuarioServiceRMI usuarioService = RMIClientFactory.getUsuarioService();
            if (esRutaRoles(request)) {
                List<RolDTO> roles = usuarioService.listarRoles();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Roles encontrados", roles)));
                return;
            }

            Long id = obtenerId(request);
            if (id != null) {
                UsuarioDTO usuario = usuarioService.buscarPorId(id);
                if (usuario == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(gson.toJson(ApiResponse.error("Usuario no encontrado")));
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Usuario encontrado", usuario)));
                return;
            }

            List<UsuarioDTO> usuarios = usuarioService.listarTodos();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Usuarios encontrados", usuarios)));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            UsuarioRequest usuarioRequest = gson.fromJson(request.getReader(), UsuarioRequest.class);
            if (usuarioRequest == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Datos de usuario requeridos")));
                return;
            }

            UsuarioDTO usuario = toDto(usuarioRequest);
            UsuarioServiceRMI usuarioService = RMIClientFactory.getUsuarioService();
            Long usuarioId = usuarioService.insertar(usuario);
            if (usuarioRequest.rolesIds != null && !usuarioRequest.rolesIds.isEmpty()) {
                usuarioService.asignarRoles(usuarioId, usuarioRequest.rolesIds);
            }
            UsuarioDTO creado = usuarioService.buscarPorId(usuarioId);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Usuario registrado", creado)));
        } catch (JsonSyntaxException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error("JSON invalido")));
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            Long id = obtenerId(request);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Id de usuario requerido")));
                return;
            }

            UsuarioRequest usuarioRequest = gson.fromJson(request.getReader(), UsuarioRequest.class);
            if (usuarioRequest == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Datos de usuario requeridos")));
                return;
            }

            UsuarioDTO usuario = toDto(usuarioRequest);
            usuario.setId(id);
            UsuarioServiceRMI usuarioService = RMIClientFactory.getUsuarioService();
            usuarioService.actualizar(usuario);
            if (usuarioRequest.rolesIds != null && !usuarioRequest.rolesIds.isEmpty()) {
                usuarioService.asignarRoles(id, usuarioRequest.rolesIds);
            }
            UsuarioDTO actualizado = usuarioService.buscarPorId(id);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Usuario actualizado", actualizado)));
        } catch (JsonSyntaxException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error("JSON invalido")));
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            Long id = obtenerId(request);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Id de usuario requerido")));
                return;
            }

            UsuarioServiceRMI usuarioService = RMIClientFactory.getUsuarioService();
            boolean ok = usuarioService.desactivar(id);
            response.setStatus(ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(ok ? ApiResponse.ok("Usuario desactivado", null) : ApiResponse.error("Usuario no encontrado")));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    private Long obtenerId(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            return null;
        }
        try {
            return Long.valueOf(pathInfo.substring(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean esRutaRoles(HttpServletRequest request) {
        return "/roles".equals(request.getPathInfo());
    }

    private UsuarioDTO toDto(UsuarioRequest request) {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setUsername(request.username);
        usuario.setPassword(request.password);
        usuario.setEmail(request.email);
        usuario.setNombreCompleto(request.nombreCompleto);
        usuario.setDni(request.dni);
        usuario.setTelefono(request.telefono);
        usuario.setActivo(request.activo == null || request.activo);
        return usuario;
    }

    private static class UsuarioRequest {
        private String username;
        private String password;
        private String email;
        private String nombreCompleto;
        private String dni;
        private String telefono;
        private Boolean activo;
        private List<Long> rolesIds;
    }
}
