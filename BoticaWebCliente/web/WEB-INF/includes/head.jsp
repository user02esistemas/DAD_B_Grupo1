<%-- 
    Document   : head
    Created on : 2 dic. 2025, 15:21:12
    Author     : yerri
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="DTO.RolDTO" %>
<%@ page import="java.util.List" %>
<%
    // Verificar sesión
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    
    String nombreCompleto = (String) session.getAttribute("nombreCompleto");
    String username = (String) session.getAttribute("username");
    
    // Obtener el primer rol para mostrar en navbar (NO declarar roles aquí)
    String rolActual = "INVITADO";
    List rolesUsuario = (List) session.getAttribute("roles");
    if (rolesUsuario != null && !rolesUsuario.isEmpty()) {
        RolDTO primerRol = (RolDTO) rolesUsuario.get(0);
        rolActual = primerRol.getNombre().replace("ROLE_", "");
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "Econosalud Farmacia" %></title>
    
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    
    <!-- CSS personalizado -->
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>