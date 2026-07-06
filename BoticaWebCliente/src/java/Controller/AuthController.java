package controller;

import DTO.UsuarioDTO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import integration.api.AuthApiClient;

@WebServlet(name = "AuthController", urlPatterns = {"/AuthController"})
public class AuthController extends HttpServlet {

    private final AuthApiClient authApiClient = new AuthApiClient();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("login".equals(action)) {
            procesarLogin(request, response);
        } else if ("logout".equals(action)) {
            procesarLogout(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("logout".equals(action)) {
            procesarLogout(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }

    private void procesarLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Usuario y contraseña son obligatorios");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        UsuarioDTO usuario;
        try {
            usuario = authApiClient.login(username.trim(), password);
        } catch (Exception ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("usuarioLogueado", usuario);
        session.setAttribute("userId", usuario.getId());
        session.setAttribute("username", usuario.getUsername());
        session.setAttribute("nombreCompleto", usuario.getNombreCompleto());
        session.setAttribute("roles", usuario.getRoles());
        session.setMaxInactiveInterval(3600);

        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    private void procesarLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object username = session.getAttribute("username");
            try {
                authApiClient.logout(username == null ? null : username.toString());
            } catch (Exception ignored) {
                // La sesión web se cierra aunque el registro remoto de logout falle.
            }
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}
