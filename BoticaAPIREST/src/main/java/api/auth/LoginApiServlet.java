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
import rmi.auth.AuthServiceRMI;
import rmi.dto.UsuarioDTO;

@WebServlet(name = "LoginApiServlet", urlPatterns = {"/api/auth/login", "/api/auth/logout"})
public class LoginApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            LoginRequest loginRequest = leerRequest(request);
            if (loginRequest == null) {
                loginRequest = new LoginRequest();
            }

            if ("/api/auth/logout".equals(request.getServletPath())) {
                procesarLogout(loginRequest, response);
                return;
            }

            if (loginRequest.username == null || loginRequest.username.trim().isEmpty()
                    || loginRequest.password == null || loginRequest.password.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Usuario y password son obligatorios")));
                return;
            }

            AuthServiceRMI authService = RMIClientFactory.getAuthService();
            UsuarioDTO usuario = authService.login(loginRequest.username, loginRequest.password);

            if (usuario == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(ApiResponse.error("Usuario o password incorrectos")));
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Login correcto", usuario)));
        } catch (JsonSyntaxException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error("JSON invalido")));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    private void procesarLogout(LoginRequest loginRequest, HttpServletResponse response) throws Exception {
        if (loginRequest.username == null || loginRequest.username.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error("Usuario obligatorio")));
            return;
        }

        AuthServiceRMI authService = RMIClientFactory.getAuthService();
        authService.logout(loginRequest.username);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(ApiResponse.ok("Logout correcto", null)));
    }

    private LoginRequest leerRequest(HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            return gson.fromJson(request.getReader(), LoginRequest.class);
        }

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.username = request.getParameter("username");
        loginRequest.password = request.getParameter("password");
        return loginRequest;
    }

    private static class LoginRequest {
        private String username;
        private String password;
    }
}
