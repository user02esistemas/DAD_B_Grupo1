<%-- 
    Document   : listar
    Created on : 2 dic. 2025, 16:09:17
    Author     : yerri
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="DTO.UsuarioDTO, DTO.RolDTO, integration.api.UsuarioApiClient, java.util.List" %>
<%
    request.setAttribute("pageTitle", "Gestión de Usuarios - Seycalf Farmacia");

    UsuarioApiClient usuarioApiClient = new UsuarioApiClient();
    List<UsuarioDTO> usuarios = usuarioApiClient.listarTodos();

    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>
<%@ include file="/WEB-INF/includes/navbar.jsp" %>

<div class="container-fluid">
    <div class="row">
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h1 class="h2"><i class="bi bi-people me-2"></i>Gestión de Usuarios</h1>
                <a href="<%= request.getContextPath()%>/admin/usuarios/crear.jsp" class="btn btn-primary">
                    <i class="bi bi-plus-circle me-1"></i>Nuevo Usuario
                </a>
            </div>

            <!-- Alertas -->
            <% if (success != null) {%>
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <%= success%>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <% } %>

            <% if (error != null) {%>
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <%= error%>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <% } %>

            <!-- Tabla de usuarios -->
            <div class="card">
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <th>Usuario</th>
                                    <th>Nombre Completo</th>
                                    <th>Email</th>
                                    <th>DNI</th>
                                    <th>Roles</th>
                                    <th>Estado</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (UsuarioDTO usuario : usuarios) {%>
                                <tr>
                                    <td><strong><%= usuario.getUsername()%></strong></td>
                                    <td><%= usuario.getNombreCompleto()%></td>
                                    <td><%= usuario.getEmail()%></td>
                                    <td><%= usuario.getDni()%></td>
                                    <td>
                                        <%
                                            List<RolDTO> rolesUsr = usuario.getRoles();
                                            if (rolesUsr != null) {
                                                for (RolDTO r : rolesUsr) {
                                        %>
                                        <span class="badge bg-primary me-1"><%= r.getNombre().replace("ROLE_", "")%></span>
                                        <%
                                                }
                                            }
                                        %>
                                    </td>
                                    <td>
                                        <% if (usuario.isActivo()) { %>
                                        <span class="badge bg-primary">Activo</span>
                                        <% } else { %>
                                        <span class="badge bg-danger">Inactivo</span>
                                        <% }%>
                                    </td>
                                    <td>
                                        <a href="<%= request.getContextPath()%>/UsuarioController?action=edit&id=<%= usuario.getId()%>" 
                                           class="btn btn-sm btn-warning" title="Editar">
                                            <i class="bi bi-pencil"></i>
                                        </a>

                                        <form action="<%= request.getContextPath()%>/UsuarioController" method="POST" style="display:inline;">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="<%= usuario.getId()%>">
                                            <button type="submit" class="btn btn-sm btn-danger" 
                                                    onclick="return confirm('⚠️ ADVERTENCIA: ¿Está seguro de eliminar permanentemente este usuario?\n\nEsta acción NO se puede deshacer y se eliminarán:\n- Todos los datos del usuario\n- Sus roles asignados\n\n¿Desea continuar?')" 
                                                    title="Eliminar permanentemente">
                                                <i class="bi bi-trash"></i>
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                                <% }%>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
