package api.auth;

import api.common.ApiResponse;
import api.config.RMIClientFactory;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import rmi.auth.UsuarioServiceRMI;
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
}
