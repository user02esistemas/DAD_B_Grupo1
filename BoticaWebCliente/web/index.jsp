<%-- 
    Document   : index
    Created on : 2 dic. 2025, 15:38:08
    Author     : yerri
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="DTO.RolDTO" %>
<%@ page import="java.util.List" %>
<%
    // Verificar si hay sesión activa
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    // Redirigir según el rol del usuario
    List<RolDTO> rolesUsuario = (List<RolDTO>) session.getAttribute("roles");

    if (rolesUsuario != null && !rolesUsuario.isEmpty()) {
        RolDTO primerRol = rolesUsuario.get(0);

        if ("ROLE_ADMIN".equals(primerRol.getNombre())) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
            return;
        } else if ("ROLE_FARMACEUTICO".equals(primerRol.getNombre())) {
            response.sendRedirect(request.getContextPath() + "/farmaceutico/ventas/nueva.jsp");
            return;
        }
    }

    // Si no tiene rol, cerrar sesión
    response.sendRedirect(request.getContextPath() + "/AuthController?action=logout");
%>
