<%-- 
    Document   : listar
    Created on : 06 dic. 2025
    Author     : Sistema Botica
    Description: Listado de compras realizadas
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="DTO.TransaccionDTO, java.util.List, java.text.SimpleDateFormat" %>
<%
    request.setAttribute("pageTitle", "Historial de Compras - Sistema Botica");
    
    // Obtener compras paginadas
    int pagina = 1;
    int porPagina = 15;
    
    try {
        if (request.getParameter("pagina") != null) {
            pagina = Integer.parseInt(request.getParameter("pagina"));
        }
    } catch (NumberFormatException e) {
        pagina = 1;
    }
    
    List<TransaccionDTO> compras = TransaccionDTO.listarCompras(pagina, porPagina);
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>

<style>
    .card-compras {
        border: none;
        border-radius: 10px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
    
    .tabla-compras th {
        background: #f8f9fa;
        font-weight: 600;
        color: #555;
        border-bottom: 2px solid #eee;
    }
    
    .tabla-compras td {
        vertical-align: middle;
    }
    
    .badge-completada {
        background: #d4edda;
        color: #155724;
    }
    
    .badge-anulada {
        background: #f8d7da;
        color: #721c24;
    }
    
    .btn-accion {
        width: 32px;
        height: 32px;
        padding: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border-radius: 6px;
    }
    
    .filtros-compras {
        background: #f8f9fa;
        border-radius: 10px;
        padding: 15px;
        margin-bottom: 20px;
    }
    
    .resumen-stats {
        display: flex;
        gap: 20px;
        margin-bottom: 20px;
    }
    
    .stat-card {
        flex: 1;
        background: #fff;
        border-radius: 10px;
        padding: 20px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
    
    .stat-card .valor {
        font-size: 28px;
        font-weight: 700;
        color: #1a5a4c;
    }
    
    .stat-card .label {
        color: #666;
        font-size: 13px;
    }
</style>

<%@ include file="/WEB-INF/includes/navbar.jsp" %>

<div class="container-fluid">
    <div class="row">
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4 py-3">
            
            <!-- Encabezado -->
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h4 class="mb-0"><i class="bi bi-list-ul me-2"></i>Historial de Compras</h4>
                    <small class="text-muted">Registro de todas las compras realizadas</small>
                </div>
                <a href="<%= request.getContextPath() %>/admin/compras/nueva.jsp" class="btn btn-primary">
                    <i class="bi bi-plus-circle me-1"></i>Nueva Compra
                </a>
            </div>

            <!-- Filtros -->
            <div class="filtros-compras">
                <div class="row g-3 align-items-end">
                    <div class="col-md-3">
                        <label class="form-label">Fecha Desde</label>
                        <input type="date" class="form-control" id="fechaDesde">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Fecha Hasta</label>
                        <input type="date" class="form-control" id="fechaHasta">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Proveedor</label>
                        <select class="form-select" id="filtroProveedor">
                            <option value="">Todos</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <button class="btn btn-outline-primary me-2" onclick="aplicarFiltros()">
                            <i class="bi bi-search me-1"></i>Filtrar
                        </button>
                        <button class="btn btn-outline-secondary" onclick="limpiarFiltros()">
                            <i class="bi bi-x-circle me-1"></i>Limpiar
                        </button>
                    </div>
                </div>
            </div>

            <!-- Tabla de compras -->
            <div class="card card-compras">
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table tabla-compras mb-0">
                            <thead>
                                <tr>
                                    <th>N° Compra</th>
                                    <th>Fecha</th>
                                    <th>Proveedor</th>
                                    <th class="text-end">Subtotal</th>
                                    <th class="text-end">IGV</th>
                                    <th class="text-end">Total</th>
                                    <th class="text-center">Estado</th>
                                    <th class="text-center">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% if (compras != null && !compras.isEmpty()) { 
                                    for (TransaccionDTO compra : compras) { %>
                                <tr>
                                    <td><strong><%= compra.getNumeroTransaccion() %></strong></td>
                                    <td><%= sdf.format(compra.getFecha()) %></td>
                                    <td>
                                        <% if (compra.getProveedor() != null) { %>
                                            <%= compra.getProveedor().getRazonSocial() %>
                                        <% } else { %>
                                            <span class="text-muted">Sin proveedor</span>
                                        <% } %>
                                    </td>
                                    <td class="text-end">S/ <%= String.format("%.2f", compra.getSubtotal()) %></td>
                                    <td class="text-end">S/ <%= String.format("%.2f", compra.getIgv()) %></td>
                                    <td class="text-end"><strong>S/ <%= String.format("%.2f", compra.getTotal()) %></strong></td>
                                    <td class="text-center">
                                        <% if ("COMPLETADA".equals(compra.getEstado())) { %>
                                            <span class="badge badge-completada">Completada</span>
                                        <% } else { %>
                                            <span class="badge badge-anulada">Anulada</span>
                                        <% } %>
                                    </td>
                                    <td class="text-center">
                                        <button class="btn btn-sm btn-outline-primary btn-accion" 
                                                onclick="verDetalle(<%= compra.getId() %>)" title="Ver detalle">
                                            <i class="bi bi-eye"></i>
                                        </button>
                                        <% if ("COMPLETADA".equals(compra.getEstado())) { %>
                                        <button class="btn btn-sm btn-outline-danger btn-accion" 
                                                onclick="anularCompra(<%= compra.getId() %>)" title="Anular">
                                            <i class="bi bi-x-circle"></i>
                                        </button>
                                        <% } %>
                                    </td>
                                </tr>
                                <% } 
                                } else { %>
                                <tr>
                                    <td colspan="8" class="text-center py-5">
                                        <i class="bi bi-inbox text-muted" style="font-size: 48px;"></i>
                                        <p class="text-muted mt-2">No hay compras registradas</p>
                                        <a href="<%= request.getContextPath() %>/admin/compras/nueva.jsp" class="btn btn-primary btn-sm">
                                            <i class="bi bi-plus-circle me-1"></i>Registrar primera compra
                                        </a>
                                    </td>
                                </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
                
                <!-- Paginación -->
                <div class="card-footer bg-white">
                    <nav>
                        <ul class="pagination pagination-sm justify-content-center mb-0">
                            <li class="page-item <%= pagina == 1 ? "disabled" : "" %>">
                                <a class="page-link" href="?pagina=<%= pagina - 1 %>">Anterior</a>
                            </li>
                            <li class="page-item active">
                                <span class="page-link"><%= pagina %></span>
                            </li>
                            <li class="page-item <%= (compras != null && compras.size() < porPagina) ? "disabled" : "" %>">
                                <a class="page-link" href="?pagina=<%= pagina + 1 %>">Siguiente</a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </div>
        </main>
    </div>
</div>

<!-- Modal Ver Detalle -->
<div class="modal fade" id="modalDetalle" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="bi bi-receipt me-2"></i>Detalle de Compra</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body" id="contenidoDetalle">
                <div class="text-center py-4">
                    <div class="spinner-border text-primary" role="status"></div>
                    <p class="mt-2">Cargando...</p>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                <button type="button" class="btn btn-primary" onclick="imprimirDetalle()">
                    <i class="bi bi-printer me-1"></i>Imprimir
                </button>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>

<script>
const contextPath = '<%= request.getContextPath() %>';

function verDetalle(id) {
    const modal = new bootstrap.Modal(document.getElementById('modalDetalle'));
    modal.show();
    
    fetch(contextPath + '/CompraController?action=verCompra&id=' + id)
        .then(response => response.json())
        .then(compra => {
            if (compra.error) {
                document.getElementById('contenidoDetalle').innerHTML = 
                    `<div class="alert alert-danger">${compra.error}</div>`;
                return;
            }
            
            let detallesHtml = '';
            if (compra.detalles && compra.detalles.length > 0) {
                detallesHtml = compra.detalles.map(function(d) {
                    return '<tr>' +
                        '<td>' + d.nombreComercial + ' ' + (d.concentracion || '') + '</td>' +
                        '<td class="text-center">' + d.cantidad + '</td>' +
                        '<td class="text-end">S/ ' + parseFloat(d.precioUnitario).toFixed(2) + '</td>' +
                        '<td class="text-end">S/ ' + parseFloat(d.subtotal).toFixed(2) + '</td>' +
                        '<td>' + (d.lote || '-') + '</td>' +
                        '<td>' + (d.fechaVencimiento || '-') + '</td>' +
                    '</tr>';
                }).join('');
            }
            
            var estadoBadge = compra.estado === 'COMPLETADA' ? 'bg-success' : 'bg-danger';
            var proveedorNombre = compra.proveedor ? compra.proveedor.razonSocial : 'Sin proveedor';
            var fechaFormateada = new Date(compra.fecha).toLocaleString();
            
            document.getElementById('contenidoDetalle').innerHTML = 
                '<div class="row mb-4">' +
                    '<div class="col-md-6">' +
                        '<p><strong>N° Compra:</strong> ' + compra.numeroTransaccion + '</p>' +
                        '<p><strong>Fecha:</strong> ' + fechaFormateada + '</p>' +
                        '<p><strong>Estado:</strong> ' +
                            '<span class="badge ' + estadoBadge + '">' +
                                compra.estado +
                            '</span>' +
                        '</p>' +
                    '</div>' +
                    '<div class="col-md-6">' +
                        '<p><strong>Proveedor:</strong> ' + proveedorNombre + '</p>' +
                        '<p><strong>Observaciones:</strong> ' + (compra.observaciones || 'Ninguna') + '</p>' +
                    '</div>' +
                '</div>' +
                '<h6 class="mb-3">Productos</h6>' +
                '<div class="table-responsive">' +
                    '<table class="table table-sm table-bordered">' +
                        '<thead class="table-light">' +
                            '<tr>' +
                                '<th>Producto</th>' +
                                '<th class="text-center">Cant.</th>' +
                                '<th class="text-end">P. Unit.</th>' +
                                '<th class="text-end">Subtotal</th>' +
                                '<th>Lote</th>' +
                                '<th>F. Venc.</th>' +
                            '</tr>' +
                        '</thead>' +
                        '<tbody>' +
                            detallesHtml +
                        '</tbody>' +
                        '<tfoot>' +
                            '<tr>' +
                                '<td colspan="3" class="text-end"><strong>Subtotal:</strong></td>' +
                                '<td class="text-end">S/ ' + parseFloat(compra.subtotal).toFixed(2) + '</td>' +
                                '<td colspan="2"></td>' +
                            '</tr>' +
                            '<tr>' +
                                '<td colspan="3" class="text-end"><strong>IGV (18%):</strong></td>' +
                                '<td class="text-end">S/ ' + parseFloat(compra.igv).toFixed(2) + '</td>' +
                                '<td colspan="2"></td>' +
                            '</tr>' +
                            '<tr class="table-primary">' +
                                '<td colspan="3" class="text-end"><strong>TOTAL:</strong></td>' +
                                '<td class="text-end"><strong>S/ ' + parseFloat(compra.total).toFixed(2) + '</strong></td>' +
                                '<td colspan="2"></td>' +
                            '</tr>' +
                        '</tfoot>' +
                    '</table>' +
                '</div>';
        })
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('contenidoDetalle').innerHTML = 
                '<div class="alert alert-danger">Error al cargar el detalle</div>';
        });
}

function anularCompra(id) {
    if (!confirm('¿Está seguro de anular esta compra?\n\nEsta acción no se puede deshacer y el stock se revertirá.')) {
        return;
    }
    
    fetch(contextPath + '/CompraController?action=anularCompra&id=' + id, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('Compra anulada correctamente');
            location.reload();
        } else {
            alert(data.error || 'Error al anular la compra');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error al procesar la solicitud');
    });
}

function aplicarFiltros() {
    // TODO: Implementar filtros
    alert('Filtros en desarrollo');
}

function limpiarFiltros() {
    document.getElementById('fechaDesde').value = '';
    document.getElementById('fechaHasta').value = '';
    document.getElementById('filtroProveedor').value = '';
}

function imprimirDetalle() {
    window.print();
}
</script>
