<%-- 
    Document   : listar
    Created on : 07 dic. 2025
    Author     : Sistema Botica
    Description: Listado de movimientos de inventario (entradas y salidas)
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="DAO.MovimientoInventarioDAO, DTO.MovimientoInventarioDTO, java.util.List, java.text.SimpleDateFormat" %>
<%
    request.setAttribute("pageTitle", "Inventario - Sistema Botica");
    
    // Obtener parámetros de búsqueda y paginación
    String busqueda = request.getParameter("busqueda");
    String paginaParam = request.getParameter("pagina");
    
    int pagina = 1;
    int porPagina = 15;
    
    if (paginaParam != null && !paginaParam.isEmpty()) {
        try {
            pagina = Integer.parseInt(paginaParam);
            if (pagina < 1) pagina = 1;
        } catch (NumberFormatException e) {
            pagina = 1;
        }
    }
    
    // Obtener datos usando DAO directamente
    MovimientoInventarioDAO movimientoDAO = new MovimientoInventarioDAO();
    List<MovimientoInventarioDTO> movimientos;
    int totalRegistros;
    
    if (busqueda != null && !busqueda.trim().isEmpty()) {
        busqueda = busqueda.trim();
        movimientos = movimientoDAO.buscar(busqueda, pagina, porPagina);
        totalRegistros = movimientoDAO.contarBusqueda(busqueda);
    } else {
        movimientos = movimientoDAO.listarTodos(pagina, porPagina);
        totalRegistros = movimientoDAO.contarTodos();
        busqueda = "";
    }
    
    int totalPaginas = (int) Math.ceil((double) totalRegistros / porPagina);
    if (totalPaginas < 1) totalPaginas = 1;
    
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>

<style>
    .inventario-header {
        background: linear-gradient(135deg, #1a5a4c 0%, #2d8a7a 100%);
        color: white;
        padding: 20px;
        border-radius: 10px;
        margin-bottom: 20px;
    }
    
    .badge-entrada {
        background: #28a745;
        color: white;
        padding: 5px 12px;
        border-radius: 20px;
        font-size: 12px;
        display: inline-block;
    }
    
    .badge-salida {
        background: #dc3545;
        color: white;
        padding: 5px 12px;
        border-radius: 20px;
        font-size: 12px;
        display: inline-block;
    }
    
    .badge-ajuste {
        background: #ffc107;
        color: #212529;
        padding: 5px 12px;
        border-radius: 20px;
        font-size: 12px;
        display: inline-block;
    }
    
    .stock-anterior {
        color: #6c757d;
        font-size: 13px;
    }
    
    .stock-nuevo {
        font-weight: bold;
        font-size: 15px;
    }
    
    .cantidad-entrada {
        color: #28a745;
        font-weight: bold;
    }
    
    .cantidad-salida {
        color: #dc3545;
        font-weight: bold;
    }
    
    .table thead th {
        background: #f8f9fa;
        font-weight: 600;
        font-size: 13px;
    }
    
    .table tbody td {
        vertical-align: middle;
    }
    
    .empty-state {
        text-align: center;
        padding: 60px 20px;
        color: #6c757d;
    }
    
    .empty-state i {
        font-size: 64px;
        margin-bottom: 15px;
        opacity: 0.5;
    }
    
    .arrow-icon {
        font-size: 18px;
        margin: 0 5px;
    }
    
    .motivo-text {
        font-size: 12px;
        color: #6c757d;
        max-width: 200px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
</style>

<%@ include file="/WEB-INF/includes/navbar.jsp" %>

<div class="container-fluid">
    <div class="row">
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4 py-4">
            
            <!-- Encabezado -->
            <div class="inventario-header">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h4 class="mb-0"><i class="bi bi-box-seam me-2"></i>Inventario - Movimientos</h4>
                        <small>Historial de entradas y salidas de productos</small>
                    </div>
                    <a href="<%= request.getContextPath() %>/admin/compras/nueva.jsp" class="btn btn-light">
                        <i class="bi bi-cart-plus me-1"></i>Nueva Compra
                    </a>
                </div>
            </div>

            <!-- Alertas -->
            <% if (success != null) { %>
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <%= success %>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <% } %>

            <% if (error != null) { %>
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <%= error %>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <% } %>

            <!-- Buscador -->
            <div class="card mb-4">
                <div class="card-body">
                    <form action="<%= request.getContextPath() %>/admin/inventario/listar.jsp" method="get" class="row g-3 align-items-end">
                        <div class="col-md-8">
                            <label class="form-label">Buscar movimiento</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-search"></i></span>
                                <input type="text" class="form-control" name="busqueda" 
                                       value="<%= busqueda %>" 
                                       placeholder="Buscar por producto, lote, usuario, motivo o tipo (entrada/salida)...">
                            </div>
                        </div>
                        <div class="col-md-2">
                            <button type="submit" class="btn btn-primary w-100">
                                <i class="bi bi-search me-1"></i>Buscar
                            </button>
                        </div>
                        <div class="col-md-2">
                            <a href="<%= request.getContextPath() %>/admin/inventario/listar.jsp" class="btn btn-outline-secondary w-100">
                                <i class="bi bi-x-circle me-1"></i>Limpiar
                            </a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Tabla de movimientos -->
            <div class="card">
                <div class="card-body">
                    <% if (movimientos == null || movimientos.isEmpty()) { %>
                        <div class="empty-state">
                            <i class="bi bi-inbox"></i>
                            <h5>No hay movimientos</h5>
                            <p>No se encontraron movimientos de inventario.</p>
                        </div>
                    <% } else { %>
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead>
                                    <tr>
                                        <th>Fecha</th>
                                        <th>Tipo</th>
                                        <th>Producto</th>
                                        <th>Lote</th>
                                        <th class="text-center">Cantidad</th>
                                        <th class="text-center">Stock Anterior</th>
                                        <th class="text-center">Stock Actual</th>
                                        <th>Usuario</th>
                                        <th>Motivo</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (MovimientoInventarioDTO m : movimientos) { %>
                                        <tr>
                                            <td>
                                                <small><%= m.getFechaMovimiento() != null ? sdf.format(m.getFechaMovimiento()) : "-" %></small>
                                            </td>
                                            <td>
                                                <% if ("ENTRADA".equals(m.getTipoMovimiento())) { %>
                                                    <span class="badge-entrada">
                                                        <i class="bi bi-arrow-down-circle me-1"></i>Entrada
                                                    </span>
                                                <% } else if ("SALIDA".equals(m.getTipoMovimiento())) { %>
                                                    <span class="badge-salida">
                                                        <i class="bi bi-arrow-up-circle me-1"></i>Salida
                                                    </span>
                                                <% } else { %>
                                                    <span class="badge-ajuste">
                                                        <i class="bi bi-gear me-1"></i>Ajuste
                                                    </span>
                                                <% } %>
                                            </td>
                                            <td>
                                                <strong><%= m.getProductoNombre() != null ? m.getProductoNombre() : "-" %></strong>
                                            </td>
                                            <td>
                                                <span class="badge bg-secondary"><%= m.getLote() != null ? m.getLote() : "-" %></span>
                                            </td>
                                            <td class="text-center">
                                                <% if ("ENTRADA".equals(m.getTipoMovimiento())) { %>
                                                    <span class="cantidad-entrada">+<%= m.getCantidad() %></span>
                                                <% } else if ("SALIDA".equals(m.getTipoMovimiento())) { %>
                                                    <span class="cantidad-salida">-<%= m.getCantidad() %></span>
                                                <% } else { %>
                                                    <span><%= m.getCantidad() %></span>
                                                <% } %>
                                            </td>
                                            <td class="text-center">
                                                <span class="stock-anterior"><%= m.getStockAnterior() %></span>
                                            </td>
                                            <td class="text-center">
                                                <span class="stock-nuevo"><%= m.getStockNuevo() %></span>
                                            </td>
                                            <td>
                                                <small>
                                                    <i class="bi bi-person-circle me-1"></i>
                                                    <%= m.getUsuarioNombre() != null ? m.getUsuarioNombre() : "-" %>
                                                </small>
                                            </td>
                                            <td>
                                                <span class="motivo-text" title="<%= m.getMotivo() != null ? m.getMotivo() : "" %>">
                                                    <%= m.getMotivo() != null ? m.getMotivo() : "-" %>
                                                </span>
                                            </td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>

                        <!-- Paginación -->
                        <div class="d-flex justify-content-between align-items-center mt-3">
                            <div class="text-muted">
                                Mostrando página <%= pagina %> de <%= totalPaginas %> 
                                (<%= totalRegistros %> registros)
                            </div>
                            <nav>
                                <ul class="pagination pagination-sm mb-0">
                                    <li class="page-item <%= pagina == 1 ? "disabled" : "" %>">
                                        <a class="page-link" href="<%= request.getContextPath() %>/admin/inventario/listar.jsp?pagina=<%= pagina - 1 %>&busqueda=<%= busqueda %>">
                                            Anterior
                                        </a>
                                    </li>
                                    
                                    <% 
                                        int inicio = Math.max(1, pagina - 2);
                                        int fin = Math.min(totalPaginas, pagina + 2);
                                        for (int i = inicio; i <= fin; i++) { 
                                    %>
                                        <li class="page-item <%= i == pagina ? "active" : "" %>">
                                            <a class="page-link" href="<%= request.getContextPath() %>/admin/inventario/listar.jsp?pagina=<%= i %>&busqueda=<%= busqueda %>">
                                                <%= i %>
                                            </a>
                                        </li>
                                    <% } %>
                                    
                                    <li class="page-item <%= pagina >= totalPaginas ? "disabled" : "" %>">
                                        <a class="page-link" href="<%= request.getContextPath() %>/admin/inventario/listar.jsp?pagina=<%= pagina + 1 %>&busqueda=<%= busqueda %>">
                                            Siguiente
                                        </a>
                                    </li>
                                </ul>
                            </nav>
                        </div>
                    <% } %>
                </div>
            </div>
        </main>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
