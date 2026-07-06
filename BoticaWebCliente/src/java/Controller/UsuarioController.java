package controller;

import DTO.RolDTO;
import DTO.UsuarioDTO;
import integration.api.UsuarioApiClient;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "UsuarioController", urlPatterns = {"/UsuarioController"})
public class UsuarioController extends HttpServlet {

    private final UsuarioApiClient usuarioApiClient = new UsuarioApiClient();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            if ("insert".equals(action)) {
                insertarUsuario(request, response);
            } else if ("update".equals(action)) {
                actualizarUsuario(request, response);
            } else if ("delete".equals(action)) {
                eliminarUsuario(request, response);
            } else if ("cambiarPassword".equals(action)) {
                cambiarPassword(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                    + java.net.URLEncoder.encode("Error: " + e.getMessage(), "UTF-8"));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if ("edit".equals(action)) {
            try {
                String idParam = request.getParameter("id");

                if (idParam == null || idParam.trim().isEmpty()) {
                    response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                            + java.net.URLEncoder.encode("ID no proporcionado", "UTF-8"));
                    return;
                }

                Long id = Long.parseLong(idParam);

                UsuarioDTO usuario = usuarioApiClient.buscarPorId(id);

                if (usuario != null) {
                    List<RolDTO> todosLosRoles = usuarioApiClient.listarRoles();

                    request.setAttribute("usuario", usuario);
                    request.setAttribute("roles", todosLosRoles);

                    request.getRequestDispatcher("/admin/usuarios/editar.jsp").forward(request, response);
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                            + java.net.URLEncoder.encode("Usuario no encontrado", "UTF-8"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                        + java.net.URLEncoder.encode("Error al cargar usuario: " + e.getMessage(), "UTF-8"));
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp");
        }
    }

    // -----------------------------------------------------
    // MÉTODOS PRIVADOS OPERANDO CON DTO
    // -----------------------------------------------------
    private void insertarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String email = request.getParameter("email");
            String nombreCompleto = request.getParameter("nombreCompleto");
            String dni = request.getParameter("dni");
            String telefono = request.getParameter("telefono");
            String[] rolesSeleccionados = request.getParameterValues("roles");
            boolean activo = "on".equals(request.getParameter("activo"));

            // Validaciones
            if (username == null || username.trim().isEmpty()
                    || password == null || password.trim().isEmpty()
                    || email == null || email.trim().isEmpty()
                    || nombreCompleto == null || nombreCompleto.trim().isEmpty()
                    || dni == null || dni.trim().isEmpty()) {

                response.sendRedirect(request.getContextPath() + "/admin/usuarios/crear.jsp?error="
                        + java.net.URLEncoder.encode("Todos los campos obligatorios deben ser completados", "UTF-8"));
                return;
            }

            if (password.length() < 6) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/crear.jsp?error="
                        + java.net.URLEncoder.encode("La contraseña debe tener al menos 6 caracteres", "UTF-8"));
                return;
            }

            if (rolesSeleccionados == null || rolesSeleccionados.length == 0) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/crear.jsp?error="
                        + java.net.URLEncoder.encode("Debe seleccionar al menos un rol", "UTF-8"));
                return;
            }

            UsuarioDTO usuario = new UsuarioDTO();
            usuario.setUsername(username.trim());
            usuario.setPassword(password);
            usuario.setEmail(email.trim());
            usuario.setNombreCompleto(nombreCompleto.trim());
            usuario.setDni(dni.trim());
            usuario.setTelefono(telefono != null ? telefono.trim() : null);
            usuario.setActivo(activo);

            List<Long> rolesIds = Arrays.stream(rolesSeleccionados)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            usuarioApiClient.crear(usuario, rolesIds);

            response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?success="
                    + java.net.URLEncoder.encode("Usuario creado exitosamente", "UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios/crear.jsp?error="
                    + java.net.URLEncoder.encode("Error al crear usuario: " + e.getMessage(), "UTF-8"));
        }
    }

    private void actualizarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            Long id = Long.parseLong(request.getParameter("id"));
            String username = request.getParameter("username");
            String email = request.getParameter("email");
            String nombreCompleto = request.getParameter("nombreCompleto");
            String dni = request.getParameter("dni");
            String telefono = request.getParameter("telefono");
            String[] rolesSeleccionados = request.getParameterValues("roles");
            boolean activo = "on".equals(request.getParameter("activo"));

            if (username == null || username.trim().isEmpty()
                    || email == null || email.trim().isEmpty()
                    || nombreCompleto == null || nombreCompleto.trim().isEmpty()
                    || dni == null || dni.trim().isEmpty()) {

                UsuarioDTO usuarioActual = usuarioApiClient.buscarPorId(id);
                List<RolDTO> todosLosRoles = usuarioApiClient.listarRoles();

                request.setAttribute("usuario", usuarioActual);
                request.setAttribute("roles", todosLosRoles);
                request.setAttribute("error", "Todos los campos obligatorios deben ser completados");
                request.getRequestDispatcher("/admin/usuarios/editar.jsp").forward(request, response);
                return;
            }

            if (rolesSeleccionados == null || rolesSeleccionados.length == 0) {

                UsuarioDTO usuarioActual = usuarioApiClient.buscarPorId(id);
                List<RolDTO> todosLosRoles = usuarioApiClient.listarRoles();

                request.setAttribute("usuario", usuarioActual);
                request.setAttribute("roles", todosLosRoles);
                request.setAttribute("error", "Debe seleccionar al menos un rol");
                request.getRequestDispatcher("/admin/usuarios/editar.jsp").forward(request, response);
                return;
            }

            UsuarioDTO usuario = new UsuarioDTO();
            usuario.setId(id);
            usuario.setUsername(username.trim());
            usuario.setEmail(email.trim());
            usuario.setNombreCompleto(nombreCompleto.trim());
            usuario.setDni(dni.trim());
            usuario.setTelefono(telefono != null ? telefono.trim() : null);
            usuario.setActivo(activo);

            List<Long> rolesIds = Arrays.stream(rolesSeleccionados)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            usuarioApiClient.actualizar(usuario, rolesIds);

            response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?success="
                    + java.net.URLEncoder.encode("Usuario actualizado exitosamente", "UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                    + java.net.URLEncoder.encode("Error al actualizar usuario: " + e.getMessage(), "UTF-8"));
        }
    }

    private void eliminarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            Long id = Long.parseLong(request.getParameter("id"));

            UsuarioDTO usuario = usuarioApiClient.buscarPorId(id);

            if (usuario == null) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                        + java.net.URLEncoder.encode("Usuario no encontrado", "UTF-8"));
                return;
            }

            usuarioApiClient.desactivar(usuario.getId());
            response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?success="
                    + java.net.URLEncoder.encode("Usuario desactivado exitosamente", "UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                    + java.net.URLEncoder.encode("Error al eliminar usuario: " + e.getMessage(), "UTF-8"));
        }
    }

    private void cambiarPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            Long id = Long.parseLong(request.getParameter("id"));
            String nuevaPassword = request.getParameter("nuevaPassword");

            if (nuevaPassword == null || nuevaPassword.trim().isEmpty()) {

                UsuarioDTO usuarioActual = usuarioApiClient.buscarPorId(id);
                List<RolDTO> todosLosRoles = usuarioApiClient.listarRoles();

                request.setAttribute("usuario", usuarioActual);
                request.setAttribute("roles", todosLosRoles);
                request.setAttribute("error", "La contraseña no puede estar vacía");
                request.getRequestDispatcher("/admin/usuarios/editar.jsp").forward(request, response);
                return;
            }

            if (nuevaPassword.length() < 6) {
                UsuarioDTO usuarioActual = usuarioApiClient.buscarPorId(id);
                List<RolDTO> todosLosRoles = usuarioApiClient.listarRoles();

                request.setAttribute("usuario", usuarioActual);
                request.setAttribute("roles", todosLosRoles);
                request.setAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                request.getRequestDispatcher("/admin/usuarios/editar.jsp").forward(request, response);
                return;
            }

            UsuarioDTO usuario = usuarioApiClient.buscarPorId(id);

            if (usuario == null) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                        + java.net.URLEncoder.encode("Usuario no encontrado", "UTF-8"));
                return;
            }

            usuarioApiClient.cambiarPassword(usuario.getId(), nuevaPassword);
            response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?success="
                    + java.net.URLEncoder.encode("Contraseña actualizada exitosamente", "UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                    + java.net.URLEncoder.encode("Error al cambiar contraseña: " + e.getMessage(), "UTF-8"));
        }
    }
}
