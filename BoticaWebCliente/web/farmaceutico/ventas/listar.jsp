<%-- 
    Document   : listar
    Created on : 07 dic. 2025
    Author     : Sistema Botica
    Description: Historial de Ventas
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="DTO.TransaccionDTO, DTO.RolDTO, integration.api.VentaApiClient, java.util.List, java.text.SimpleDateFormat" %>
<%
    request.setAttribute("pageTitle", "Historial de Ventas - Seycalf Farmacia");
    
    // Verificar permisos (variable con nombre único para evitar conflicto con sidebar)
    boolean esAdminVentas = false;
    List<RolDTO> rolesVentas = (List<RolDTO>) session.getAttribute("roles");
    if (rolesVentas != null) {
        for (RolDTO rol : rolesVentas) {
            if ("ROLE_ADMIN".equals(rol.getNombre())) {
                esAdminVentas = true;
                break;
            }
        }
    }
    
    // Parámetros de paginación y búsqueda
    int pagina = 1;
    int porPagina = 15;
    String busqueda = request.getParameter("busqueda");
    if (busqueda == null) busqueda = "";
    
    try {
        pagina = Integer.parseInt(request.getParameter("pagina"));
        if (pagina < 1) pagina = 1;
    } catch (Exception e) { pagina = 1; }
    
    VentaApiClient.VentasResult ventasResult = new VentaApiClient().listarVentas(busqueda, pagina, porPagina);
    int totalRegistros = ventasResult.getTotal();
    int totalPaginas = (int) Math.ceil((double) totalRegistros / porPagina);
    int offset = (pagina - 1) * porPagina;
    List<TransaccionDTO> ventas = ventasResult.getVentas();
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>
<%@ include file="/WEB-INF/includes/navbar.jsp" %>

<div class="container-fluid">
    <div class="row">
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
        
        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h1 class="h2"><i class="bi bi-clock-history"></i> Historial de Ventas</h1>
                <a href="<%= request.getContextPath() %>/farmaceutico/ventas/nueva.jsp" class="btn btn-primary">
                    <i class="bi bi-plus-circle"></i> Nueva Venta
                </a>
            </div>
            
            <!-- Buscador -->
            <div class="card mb-4">
                <div class="card-body">
                    <form method="get" class="row g-3">
                        <div class="col-md-8">
                            <input type="text" class="form-control" name="busqueda" 
                                   placeholder="Buscar por número de transacción o cliente..." 
                                   value="<%= busqueda %>">
                        </div>
                        <div class="col-md-4">
                            <button type="submit" class="btn btn-primary me-2">
                                <i class="bi bi-search"></i> Buscar
                            </button>
                            <a href="<%= request.getContextPath() %>/farmaceutico/ventas/listar.jsp" class="btn btn-secondary">
                                <i class="bi bi-x-circle"></i> Limpiar
                            </a>
                        </div>
                    </form>
                </div>
            </div>
            
            <!-- Tabla de ventas -->
            <div class="card">
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead class="table-dark">
                                <tr>
                                    <th>Número</th>
                                    <th>Fecha</th>
                                    <th>Cliente</th>
                                    <th>Comprobante</th>
                                    <th>Método Pago</th>
                                    <th class="text-end">Total</th>
                                    <th>Estado</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% if (ventas.isEmpty()) { %>
                                    <tr>
                                        <td colspan="8" class="text-center text-muted py-4">
                                            <i class="bi bi-inbox" style="font-size: 48px;"></i>
                                            <p class="mt-2">No se encontraron ventas</p>
                                        </td>
                                    </tr>
                                <% } else { 
                                    for (TransaccionDTO v : ventas) { %>
                                    <tr>
                                        <td><strong><%= v.getNumeroTransaccion() %></strong></td>
                                        <td><%= sdf.format(v.getFecha()) %></td>
                                        <td><%= v.getCliente() != null ? v.getCliente() : "CLIENTES VARIOS" %></td>
                                        <td>
                                            <% String tipoComp = v.getTipoComprobante();
                                               String badgeClass = "bg-secondary";
                                               if ("BOLETA".equals(tipoComp)) badgeClass = "bg-primary";
                                               else if ("FACTURA".equals(tipoComp)) badgeClass = "bg-success";
                                            %>
                                            <span class="badge <%= badgeClass %>"><%= tipoComp != null ? tipoComp.replace("_", " ") : "NOTA" %></span>
                                        </td>
                                        <td>
                                            <% 
                                            java.math.BigDecimal efectivo = v.getMontoEfectivo();
                                            java.math.BigDecimal virtual = v.getMontoVirtual();
                                            boolean tieneEfectivo = efectivo != null && efectivo.compareTo(java.math.BigDecimal.ZERO) > 0;
                                            boolean tieneVirtual = virtual != null && virtual.compareTo(java.math.BigDecimal.ZERO) > 0;
                                            if (tieneEfectivo && tieneVirtual) { %>
                                                <span class="badge bg-info">MIXTO</span>
                                            <% } else if (tieneVirtual) { %>
                                                <span class="badge bg-warning text-dark"><%= v.getMedioPagoVirtual() %></span>
                                            <% } else { %>
                                                <span class="badge bg-success">EFECTIVO</span>
                                            <% } %>
                                        </td>
                                        <td class="text-end"><strong>S/ <%= String.format("%.2f", v.getTotal()) %></strong></td>
                                        <td>
                                            <span class="badge bg-<%= "COMPLETADA".equals(v.getEstado()) ? "success" : "warning" %>">
                                                <%= v.getEstado() %>
                                            </span>
                                        </td>
                                        <td>
                                            <button class="btn btn-sm btn-outline-primary" onclick="verDetalle(<%= v.getId() %>)" title="Ver detalle">
                                                <i class="bi bi-eye"></i>
                                            </button>
                                        </td>
                                    </tr>
                                <% } } %>
                            </tbody>
                        </table>
                    </div>
                    
                    <!-- Paginación -->
                    <% if (totalPaginas > 1) { %>
                    <nav>
                        <ul class="pagination justify-content-center">
                            <li class="page-item <%= pagina <= 1 ? "disabled" : "" %>">
                                <a class="page-link" href="?pagina=<%= pagina - 1 %>&busqueda=<%= busqueda %>">Anterior</a>
                            </li>
                            <% for (int i = 1; i <= totalPaginas; i++) { %>
                                <li class="page-item <%= i == pagina ? "active" : "" %>">
                                    <a class="page-link" href="?pagina=<%= i %>&busqueda=<%= busqueda %>"><%= i %></a>
                                </li>
                            <% } %>
                            <li class="page-item <%= pagina >= totalPaginas ? "disabled" : "" %>">
                                <a class="page-link" href="?pagina=<%= pagina + 1 %>&busqueda=<%= busqueda %>">Siguiente</a>
                            </li>
                        </ul>
                    </nav>
                    <% } %>
                    
                    <div class="text-muted text-center">
                        Mostrando <%= Math.min(offset + 1, totalRegistros) %> - <%= Math.min(offset + porPagina, totalRegistros) %> de <%= totalRegistros %> registros
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<!-- Modal Detalle -->
<div class="modal fade" id="modalDetalle" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="bi bi-receipt"></i> Detalle de Venta</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body" id="detalleContenido">
                <div class="text-center py-5">
                    <div class="spinner-border text-primary"></div>
                    <p>Cargando...</p>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
function verDetalle(id) {
    var modal = new bootstrap.Modal(document.getElementById('modalDetalle'));
    modal.show();
    
    fetch('<%= request.getContextPath() %>/VentaController?action=detalleVenta&id=' + id)
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.success) {
                var v = data.venta;
                var html = '<div class="row mb-3">' +
                    '<div class="col-md-6">' +
                        '<p><strong>Número:</strong> ' + v.numeroTransaccion + '</p>' +
                        '<p><strong>Fecha:</strong> ' + v.fecha + '</p>' +
                        '<p><strong>Cliente:</strong> ' + (v.cliente || 'CLIENTES VARIOS') + '</p>' +
                    '</div>' +
                    '<div class="col-md-6">' +
                        '<p><strong>Comprobante:</strong> ' + (v.tipoComprobante || 'NOTA').replace('_', ' ') + '</p>' +
                        
                    '</div>' +
                '</div>' +
                '<hr>' +
                '<table class="table table-sm">' +
                    '<thead><tr><th>Producto</th><th>Cant.</th><th>P.Unit</th><th>Subtotal</th></tr></thead>' +
                    '<tbody>';
                
                for (var i = 0; i < v.detalles.length; i++) {
                    var d = v.detalles[i];
                    html += '<tr>' +
                        '<td>' + d.productoNombre + '</td>' +
                        '<td>' + d.cantidad + '</td>' +
                        '<td>S/ ' + parseFloat(d.precioUnitario).toFixed(2) + '</td>' +
                        '<td>S/ ' + parseFloat(d.subtotal).toFixed(2) + '</td>' +
                    '</tr>';
                }
                
                html += '</tbody></table>' +
                    '<div class="text-end">' +
                        '<h5>Total: S/ ' + parseFloat(v.total).toFixed(2) + '</h5>' +
                        '<p>Vuelto: S/ ' + parseFloat(v.vuelto || 0).toFixed(2) + '</p>' +
                    '</div>';
                
                document.getElementById('detalleContenido').innerHTML = html;
            } else {
                document.getElementById('detalleContenido').innerHTML = '<div class="alert alert-danger">Error al cargar el detalle</div>';
            }
        })
        .catch(function(err) {
            document.getElementById('detalleContenido').innerHTML = '<div class="alert alert-danger">Error de conexión</div>';
        });
}
</script>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
