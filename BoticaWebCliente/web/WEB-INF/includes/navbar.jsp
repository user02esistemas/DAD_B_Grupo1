<%-- 
    Document   : navbar
    Created on : 2 dic. 2025, 15:21:19
    Author     : yerri
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<!-- Navbar superior -->
<nav class="navbar navbar-expand-lg navbar-dark sticky-top">
    <div class="container-fluid">
        <!-- Toggle para móvil -->
        <button class="navbar-toggler me-2" type="button" data-bs-toggle="collapse" data-bs-target="#sidebarMenu">
            <span class="navbar-toggler-icon"></span>
        </button>

        <!-- Logo y nombre -->
        <a class="navbar-brand d-flex align-items-center" href="<%= request.getContextPath()%>/index.jsp">
            <img src="<%= request.getContextPath()%>/assets/img/logo.png" 
                 alt="logo" class="logo-img" />

            <span class="fw-bold">EconoSalud Farmacia</span>
        </a>

        <!-- Búsqueda rápida (opcional) -->


        <!-- Usuario y notificaciones -->


        <!-- Usuario -->
        <div class="dropdown">
            <button class="btn btn-link text-white text-decoration-none d-flex align-items-center" 
                    type="button" data-bs-toggle="dropdown">
                <div class="bg-white rounded-circle p-2 me-2">
                    <i class="bi bi-person-fill text-primary"></i>
                </div>
                <div class="text-start d-none d-md-block">
                    <small class="d-block" style="font-size: 0.75rem; opacity: 0.9;"><%= username%></small>
                    <span class="fw-bold"><%= nombreCompleto%></span>
                </div>
                <i class="bi bi-chevron-down ms-2"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end">
                <li><h6 class="dropdown-header">Rol: <%= rolActual%></h6></li>
                <li><hr class="dropdown-divider"></li>
                <li>
                    <a class="dropdown-item" href="#">
                        <i class="bi bi-person-circle me-2"></i>Mi Perfil
                    </a>
                </li>
                <li>
                    <a class="dropdown-item" href="#">
                        <i class="bi bi-gear me-2"></i>Configuración
                    </a>
                </li>
                <li><hr class="dropdown-divider"></li>
                <li>
                    <a class="dropdown-item text-danger" href="<%= request.getContextPath()%>/AuthController?action=logout">
                        <i class="bi bi-box-arrow-right me-2"></i>Cerrar Sesión
                    </a>
                </li>
            </ul>
        </div>
    </div>
</div>
</nav>
