<%-- 
    Document   : crear
    Created on : 2 dic. 2025, 16:10:38
    Author     : yerri
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="DTO.RolDTO, integration.api.UsuarioApiClient, java.util.List" %>
<%
    request.setAttribute("pageTitle", "Crear Usuario - Seycalf Farmacia");

    UsuarioApiClient usuarioApiClient = new UsuarioApiClient();
    List<RolDTO> rolesDisponibles = usuarioApiClient.listarRoles();
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>    
<%@ include file="/WEB-INF/includes/navbar.jsp" %>

<div class="container-fluid">
    <div class="row">
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h1 class="h2"><i class="bi bi-person-plus me-2"></i>Crear Nuevo Usuario</h1>
                <a href="<%= request.getContextPath()%>/admin/usuarios/listar.jsp" class="btn btn-secondary">
                    <i class="bi bi-arrow-left me-1"></i>Volver
                </a>
            </div>

            <div class="row">
                <div class="col-md-8">
                    <div class="card">
                        <div class="card-body">
                            <form action="<%= request.getContextPath()%>/UsuarioController" method="POST">
                                <input type="hidden" name="action" value="insert">

                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="username" class="form-label">Usuario *</label>
                                        <input type="text" class="form-control" id="username" name="username" required>
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label for="password" class="form-label">Contraseña *</label>
                                        <input type="password" class="form-control" id="password" name="password" required minlength="6">
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <label for="nombreCompleto" class="form-label">Nombre Completo *</label>
                                    <input type="text" class="form-control" id="nombreCompleto" name="nombreCompleto" required>
                                </div>

                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="email" class="form-label">Email *</label>
                                        <input type="email" class="form-control" id="email" name="email" required>
                                    </div>

                                    <div class="col-md-6 mb-3">
                                        <label for="dni" class="form-label">DNI *</label>
                                        <input type="text" class="form-control" id="dni" name="dni" required maxlength="20">
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <label for="telefono" class="form-label">Teléfono</label>
                                    <input type="text" class="form-control" id="telefono" name="telefono" maxlength="20">
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Roles *</label>
                                    <% for (RolDTO rolDisp : rolesDisponibles) {%>
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" name="roles" 
                                               value="<%= rolDisp.getId()%>" id="rol_<%= rolDisp.getId()%>">
                                        <label class="form-check-label" for="rol_<%= rolDisp.getId()%>">
                                            <%= rolDisp.getNombre().replace("ROLE_", "")%> - <%= rolDisp.getDescripcion()%>
                                        </label>
                                    </div>
                                    <% }
                                       %>
                                </div>

                                <div class="mb-3 form-check">
                                    <input type="checkbox" class="form-check-input" id="activo" name="activo" checked>
                                    <label class="form-check-label" for="activo">Usuario Activo</label>
                                </div>

                                <div class="d-grid gap-2">
                                    <button type="submit" class="btn btn-primary">
                                        <i class="bi bi-save me-1"></i>Guardar Usuario
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
