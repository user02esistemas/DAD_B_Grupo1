package controller;

import DTO.RolDTO;
import DTO.UsuarioDTO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "UsuarioController", urlPatterns = {"/UsuarioController"})
public class UsuarioController extends HttpServlet {

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

                UsuarioDTO usuario = UsuarioDTO.buscarPorId(id);  // <---- DTO

                if (usuario != null) {
                    List<RolDTO> todosLosRoles = RolDTO.listarTodos(); // <---- DTO

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

            // Validaciones de duplicados con DTO
            if (UsuarioDTO.existeUsername(username)) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/crear.jsp?error="
                        + java.net.URLEncoder.encode("El nombre de usuario ya existe", "UTF-8"));
                return;
            }

            if (UsuarioDTO.existeEmail(email)) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/crear.jsp?error="
                        + java.net.URLEncoder.encode("El email ya está registrado", "UTF-8"));
                return;
            }

            if (UsuarioDTO.existeDni(dni)) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/crear.jsp?error="
                        + java.net.URLEncoder.encode("El DNI ya está registrado", "UTF-8"));
                return;
            }

            // Crear DTO
            UsuarioDTO usuario = new UsuarioDTO();
            usuario.setUsername(username.trim());
            usuario.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(10)));
            usuario.setEmail(email.trim());
            usuario.setNombreCompleto(nombreCompleto.trim());
            usuario.setDni(dni.trim());
            usuario.setTelefono(telefono != null ? telefono.trim() : null);
            usuario.setActivo(activo);

            // Insertar desde el DTO
            boolean creado = usuario.insertar(); // <---- DTO

            if (!creado || usuario.getId() == null) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/crear.jsp?error="
                        + java.net.URLEncoder.encode("Error al crear el usuario", "UTF-8"));
                return;
            }

            // Asignar roles desde el DTO
            List<Long> rolesIds = Arrays.stream(rolesSeleccionados)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            usuario.asignarRoles(rolesIds); // <---- DTO

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

                UsuarioDTO usuarioActual = UsuarioDTO.buscarPorId(id);
                List<RolDTO> todosLosRoles = RolDTO.listarTodos();

                request.setAttribute("usuario", usuarioActual);
                request.setAttribute("roles", todosLosRoles);
                request.setAttribute("error", "Todos los campos obligatorios deben ser completados");
                request.getRequestDispatcher("/admin/usuarios/editar.jsp").forward(request, response);
                return;
            }

            if (rolesSeleccionados == null || rolesSeleccionados.length == 0) {

                UsuarioDTO usuarioActual = UsuarioDTO.buscarPorId(id);
                List<RolDTO> todosLosRoles = RolDTO.listarTodos();

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

            boolean actualizado = usuario.actualizar(); // <---- DTO

            if (!actualizado) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                        + java.net.URLEncoder.encode("No se pudo actualizar el usuario", "UTF-8"));
                return;
            }

            // Actualizar roles vía DTO
            List<Long> rolesIds = Arrays.stream(rolesSeleccionados)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            usuario.asignarRoles(rolesIds); // <---- DTO

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

            UsuarioDTO usuario = UsuarioDTO.buscarPorId(id); // <---- DTO

            if (usuario == null) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                        + java.net.URLEncoder.encode("Usuario no encontrado", "UTF-8"));
                return;
            }

            boolean eliminado = usuario.eliminar(); // <---- DTO (hard delete)

            if (eliminado) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?success="
                        + java.net.URLEncoder.encode("Usuario eliminado exitosamente", "UTF-8"));
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                        + java.net.URLEncoder.encode("No se pudo eliminar el usuario", "UTF-8"));
            }

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

                UsuarioDTO usuarioActual = UsuarioDTO.buscarPorId(id);
                List<RolDTO> todosLosRoles = RolDTO.listarTodos();

                request.setAttribute("usuario", usuarioActual);
                request.setAttribute("roles", todosLosRoles);
                request.setAttribute("error", "La contraseña no puede estar vacía");
                request.getRequestDispatcher("/admin/usuarios/editar.jsp").forward(request, response);
                return;
            }

            if (nuevaPassword.length() < 6) {
                UsuarioDTO usuarioActual = UsuarioDTO.buscarPorId(id);
                List<RolDTO> todosLosRoles = RolDTO.listarTodos();

                request.setAttribute("usuario", usuarioActual);
                request.setAttribute("roles", todosLosRoles);
                request.setAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                request.getRequestDispatcher("/admin/usuarios/editar.jsp").forward(request, response);
                return;
            }

            UsuarioDTO usuario = UsuarioDTO.buscarPorId(id); // <---- DTO

            if (usuario == null) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                        + java.net.URLEncoder.encode("Usuario no encontrado", "UTF-8"));
                return;
            }

            String passwordHasheada = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt(10));

            boolean actualizado = usuario.actualizarPassword(passwordHasheada); // <---- DTO

            if (actualizado) {
                response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?success="
                        + java.net.URLEncoder.encode("Contraseña actualizada exitosamente", "UTF-8"));
            } else {

                UsuarioDTO usuarioActual = UsuarioDTO.buscarPorId(id);
                List<RolDTO> todosLosRoles = RolDTO.listarTodos();

                request.setAttribute("usuario", usuarioActual);
                request.setAttribute("roles", todosLosRoles);
                request.setAttribute("error", "No se pudo actualizar la contraseña");
                request.getRequestDispatcher("/admin/usuarios/editar.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp?error="
                    + java.net.URLEncoder.encode("Error al cambiar contraseña: " + e.getMessage(), "UTF-8"));
        }
    }
}
