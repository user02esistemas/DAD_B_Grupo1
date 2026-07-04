<%-- 
    Document   : sidebar
    Created on : 2 dic. 2025, 15:21:29
    Author     : yerri
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="DTO.RolDTO" %>
<%@ page import="java.util.List" %>
<%
    // Determinar el rol del usuario
    boolean esAdmin = false;
    boolean esFarmaceutico = false;
    
    // Usar un nombre diferente para evitar conflicto
    List<RolDTO> listaRoles = (List<RolDTO>) session.getAttribute("roles");
    if (listaRoles != null) {
        for (RolDTO rolDto : listaRoles) {
            if ("ROLE_ADMIN".equals(rolDto.getNombre())) {
                esAdmin = true;
            } else if ("ROLE_FARMACEUTICO".equals(rolDto.getNombre())) {
                esFarmaceutico = true;
            }
        }
    }
    
    String currentPage = request.getRequestURI();
%>

<!-- Sidebar -->
<nav id="sidebarMenu" class="col-md-3 col-lg-2 d-md-block bg-light sidebar collapse">
    <div class="position-sticky pt-3">
        <ul class="nav flex-column">
            
            <% if (esAdmin) { %>
                <!-- MENÚ ADMINISTRADOR -->
                <li class="nav-item">
                    <a class="nav-link <%= currentPage.contains("dashboard") ? "active" : "" %>" 
                       href="<%= request.getContextPath() %>/admin/dashboard.jsp">
                        <i class="bi bi-speedometer2 me-2"></i>Dashboard
                    </a>
                </li>
                
                
                
                <!-- Reportes con submenu -->
                <li class="nav-item">
                    <a class="nav-link <%= currentPage.contains("reportes") ? "active" : "" %>" 
                       data-bs-toggle="collapse" href="#reportesMenu">
                        <i class="bi bi-graph-up me-2"></i>Reportes
                        <i class="bi bi-chevron-down float-end"></i>
                    </a>
                    <div class="collapse <%= currentPage.contains("reportes") ? "show" : "" %>" id="reportesMenu">
                        <ul class="nav flex-column ms-3">
                            <li class="nav-item">
                                <a class="nav-link" href="<%= request.getContextPath() %>/admin/reportes/reporte_ventas.jsp">
                                    <i class="bi bi-cash-coin me-2"></i>Ventas
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<%= request.getContextPath() %>/admin/reportes/reporte_caja.jsp">
                                    <i class="bi bi-safe me-2"></i>Caja
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<%= request.getContextPath() %>/admin/reportes/reportes_vencimiento.jsp">
                                    <i class="bi bi-calendar-x me-2"></i>Vencimientos
                                </a>
                            </li>
                        </ul>
                    </div>
                </li>
                
                <!-- Compras con submenu -->
                <li class="nav-item">
                    <a class="nav-link <%= currentPage.contains("compras") ? "active" : "" %>" 
                       data-bs-toggle="collapse" href="#comprasMenu">
                        <i class="bi bi-cart-plus me-2"></i>Compras
                        <i class="bi bi-chevron-down float-end"></i>
                    </a>
                    <div class="collapse <%= currentPage.contains("compras") ? "show" : "" %>" id="comprasMenu">
                        <ul class="nav flex-column ms-3">
                            <li class="nav-item">
                                <a class="nav-link" href="<%= request.getContextPath() %>/admin/compras/nueva.jsp">
                                    <i class="bi bi-plus-circle me-2"></i>Nueva Compra
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<%= request.getContextPath() %>/admin/compras/listar.jsp">
                                    <i class="bi bi-list-ul me-2"></i>Listar Compras
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<%= request.getContextPath() %>/admin/compras/catalogo.jsp">
                                    <i class="bi bi-journal-medical me-2"></i>Catálogo DIGEMID
                                </a>
                            </li>
                        </ul>
                    </div>
                </li>
                
                <!-- Ventas con submenu -->
                <li class="nav-item">
                    <a class="nav-link <%= currentPage.contains("ventas") && !currentPage.contains("ventas/nueva") ? "active" : "" %>" 
                       data-bs-toggle="collapse" href="#ventasMenu">
                        <i class="bi bi-bag-check me-2"></i>Ventas
                        <i class="bi bi-chevron-down float-end"></i>
                    </a>
                    <div class="collapse <%= currentPage.contains("ventas") && !currentPage.contains("ventas/nueva") ? "show" : "" %>" id="ventasMenu">
                        <ul class="nav flex-column ms-3">
                            <!-- POS - Punto de Venta -->
                <li class="nav-item">
                    <a class="nav-link <%= currentPage.contains("ventas/nueva") ? "active" : "" %>" 
                       href="<%= request.getContextPath() %>/farmaceutico/ventas/nueva.jsp">
                        <i class="bi bi-cash-stack me-2"></i>Punto de Venta
                    </a>
                </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<%= request.getContextPath() %>/farmaceutico/ventas/listar.jsp">
                                    <i class="bi bi-list-ul me-2"></i>Historial
                                </a>
                            </li>
                        </ul>
                    </div>
                </li>
                
                <!-- Inventario con submenu -->
                <li class="nav-item">
                    <a class="nav-link <%= currentPage.contains("inventario") ? "active" : "" %>" 
                       data-bs-toggle="collapse" href="#inventarioMenu">
                        <i class="bi bi-boxes me-2"></i>Inventario
                        <i class="bi bi-chevron-down float-end"></i>
                    </a>
                    <div class="collapse <%= currentPage.contains("inventario") ? "show" : "" %>" id="inventarioMenu">
                        <ul class="nav flex-column ms-3">
                            <li class="nav-item">
                                <a class="nav-link" href="<%= request.getContextPath() %>/admin/inventario/productos.jsp">
                                    <i class="bi bi-box-seam me-2"></i>Mis Productos
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<%= request.getContextPath() %>/admin/inventario/listar.jsp">
                                    <i class="bi bi-arrow-left-right me-2"></i>Movimientos
                                </a>
                            </li>
                        </ul>
                    </div>
                </li>
                
                <li class="nav-item">
                    <a class="nav-link <%= currentPage.contains("usuario") ? "active" : "" %>" 
                       href="<%= request.getContextPath() %>/admin/usuarios/listar.jsp">
                        <i class="bi bi-people me-2"></i>Usuarios
                    </a>
                </li>
                
            <% } %>
            
            <% if (esFarmaceutico) { %>
                <!-- MENÚ FARMACÉUTICO -->
                
                <!-- POS - Punto de Venta -->
                <li class="nav-item">
                    <a class="nav-link <%= currentPage.contains("ventas/nueva") ? "active" : "" %>" 
                       href="<%= request.getContextPath() %>/farmaceutico/ventas/nueva.jsp">
                        <i class="bi bi-cash-stack me-2"></i>Punto de Venta
                    </a>
                </li>
                
                <li class="nav-item">
                    <a class="nav-link <%= currentPage.contains("caja") ? "active" : "" %>" 
                       href="<%= request.getContextPath() %>/farmaceutico/caja.jsp">
                        <i class="bi bi-inbox me-2"></i>Mi Caja
                    </a>
                </li>
                
                <!-- Ventas con submenu -->
                <li class="nav-item">
                    <a class="nav-link <%= currentPage.contains("ventas/listar") ? "active" : "" %>" 
                       href="<%= request.getContextPath() %>/farmaceutico/ventas/listar.jsp">
                        <i class="bi bi-clock-history me-2"></i>Historial Ventas
                    </a>
                </li>
            <% } %>
            
            <!-- Opciones comunes -->
            <li class="nav-item mt-3">
                <hr>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="#">
                    <i class="bi bi-question-circle me-2"></i>Ayuda
                </a>
            </li>
        </ul>
    </div>
</nav>
