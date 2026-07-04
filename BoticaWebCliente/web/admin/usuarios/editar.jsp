<%-- 
    Document   : editar
    Created on : 2 dic. 2025, 16:15:00
    Author     : yerri
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="DTO.UsuarioDTO, DTO.RolDTO, java.util.List" %>
<%
    request.setAttribute("pageTitle", "Editar Usuario - Seycalf Farmacia");
    
    UsuarioDTO usuario = (UsuarioDTO) request.getAttribute("usuario");
    List<RolDTO> todosLosRoles = (List<RolDTO>) request.getAttribute("roles");
    
    // Obtener mensajes de error o éxito
    String error = request.getParameter("error");
    String success = request.getParameter("success");
    
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/admin/usuarios/listar.jsp");
        return;
    }
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>
<%@ include file="/WEB-INF/includes/navbar.jsp" %>

<div class="container-fluid">
    <div class="row">
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h1 class="h2"><i class="bi bi-pencil-square me-2"></i>Editar Usuario</h1>
                <a href="<%= request.getContextPath()%>/admin/usuarios/listar.jsp" class="btn btn-secondary">
                    <i class="bi bi-arrow-left me-1"></i>Volver
                </a>
            </div>

            <!-- Alertas -->
            <% if (error != null) {%>
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <%= error%>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <% } %>

            <% if (success != null) {%>
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <%= success%>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <% } %>

            <div class="row">
                <div class="col-md-8">
                    <!-- Formulario de edición de datos básicos -->
                    <div class="card mb-3">
                        <div class="card-header">
                            <h5 class="mb-0">Información del Usuario</h5>
                        </div>
                        <div class="card-body">
                            <form action="<%= request.getContextPath()%>/UsuarioController" method="POST">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="id" value="<%= usuario.getId()%>">

                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="username" class="form-label">Usuario *</label>
                                        <input type="text" class="form-control" id="username" name="username" 
                                               value="<%= usuario.getUsername()%>" required>
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label for="email" class="form-label">Email *</label>
                                        <input type="email" class="form-control" id="email" name="email" 
                                               value="<%= usuario.getEmail()%>" required>
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <label for="nombreCompleto" class="form-label">Nombre Completo *</label>
                                    <input type="text" class="form-control" id="nombreCompleto" name="nombreCompleto" 
                                           value="<%= usuario.getNombreCompleto()%>" required>
                                </div>

                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="dni" class="form-label">DNI *</label>
                                        <input type="text" class="form-control" id="dni" name="dni" 
                                               value="<%= usuario.getDni()%>" required maxlength="20">
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label for="telefono" class="form-label">Teléfono</label>
                                        <input type="text" class="form-control" id="telefono" name="telefono" 
                                               value="<%= usuario.getTelefono() != null ? usuario.getTelefono() : ""%>" maxlength="20">
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Roles *</label>
                                    <%
                                        List<RolDTO> rolesDelUsuario = usuario.getRoles();
                                        if (todosLosRoles != null) {
                                            for (RolDTO rol : todosLosRoles) {
                                                boolean tieneRol = false;
                                                if (rolesDelUsuario != null) {
                                                    for (RolDTO r : rolesDelUsuario) {
                                                        if (r.getId().equals(rol.getId())) {
                                                            tieneRol = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                    %>
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" name="roles" 
                                               value="<%= rol.getId()%>" id="rol_<%= rol.getId()%>" 
                                               <%= tieneRol ? "checked" : ""%>>
                                        <label class="form-check-label" for="rol_<%= rol.getId()%>">
                                            <%= rol.getNombre().replace("ROLE_", "")%> - <%= rol.getDescripcion()%>
                                        </label>
                                    </div>
                                    <%      }
                                        }
                                    %>
                                </div>

                                <div class="mb-3 form-check">
                                    <input type="checkbox" class="form-check-input" id="activo" name="activo" 
                                           <%= usuario.isActivo() ? "checked" : ""%>>
                                    <label class="form-check-label" for="activo">Usuario Activo</label>
                                </div>

                                <div class="d-grid gap-2">
                                    <button type="submit" class="btn btn-primary">
                                        <i class="bi bi-save me-1"></i>Actualizar Usuario
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Formulario de cambio de contraseña -->
                    <div class="card">
                        <div class="card-header bg-warning text-dark">
                            <h5 class="mb-0"><i class="bi bi-key me-2"></i>Cambiar Contraseña</h5>
                        </div>
                        <div class="card-body">
                            <form action="<%= request.getContextPath()%>/UsuarioController" method="POST" 
                                  onsubmit="return validarPasswords()">
                                <input type="hidden" name="action" value="cambiarPassword">
                                <input type="hidden" name="id" value="<%= usuario.getId()%>">

                                <div class="mb-3">
                                    <label for="nuevaPassword" class="form-label">Nueva Contraseña *</label>
                                    <input type="password" class="form-control" id="nuevaPassword" 
                                           name="nuevaPassword" required minlength="6">
                                    <div class="form-text">Mínimo 6 caracteres</div>
                                </div>

                                <div class="mb-3">
                                    <label for="confirmarPassword" class="form-label">Confirmar Contraseña *</label>
                                    <input type="password" class="form-control" id="confirmarPassword" 
                                           name="confirmarPassword" required minlength="6">
                                </div>

                                <div class="alert alert-warning" role="alert">
                                    <i class="bi bi-exclamation-triangle me-2"></i>
                                    Al cambiar la contraseña, el usuario deberá usar la nueva contraseña en su próximo inicio de sesión.
                                </div>

                                <div class="d-grid gap-2">
                                    <button type="submit" class="btn btn-warning">
                                        <i class="bi bi-key me-1"></i>Cambiar Contraseña
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>

                <!-- Panel lateral con información adicional -->
                <div class="col-md-4">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">Información del Sistema</h5>
                        </div>
                        <div class="card-body">
                            <p><strong>ID:</strong> <%= usuario.getId()%></p>
                            <p><strong>Creado:</strong> <%= usuario.getCreatedAt()%></p>
                            <p><strong>Actualizado:</strong> <%= usuario.getUpdatedAt()%></p>
                            <p><strong>Estado:</strong> 
                                <% if (usuario.isActivo()) {%>
                                <span class="badge bg-success">Activo</span>
                                <% } else {%>
                                <span class="badge bg-danger">Inactivo</span>
                                <% }%>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<script>
function validarPasswords() {
    const nueva = document.getElementById('nuevaPassword').value;
    const confirmar = document.getElementById('confirmarPassword').value;
    
    if (nueva !== confirmar) {
        alert('Las contraseñas no coinciden');
        return false;
    }
    
    if (nueva.length < 6) {
        alert('La contraseña debe tener al menos 6 caracteres');
        return false;
    }
    
    return confirm('¿Está seguro de cambiar la contraseña de este usuario?');
}
</script>

<%@ include file="/WEB-INF/includes/footer.jsp" %>