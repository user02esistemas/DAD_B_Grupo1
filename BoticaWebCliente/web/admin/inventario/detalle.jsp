<%-- 
    Document   : detalle
    Created on : 07 dic. 2025
    Author     : Sistema Botica
    Description: Detalle de una transacción (compra o venta)
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="integration.api.VentaApiClient, DTO.TransaccionDTO, DTO.DetalleTransaccionDTO, java.util.List, java.text.SimpleDateFormat" %>
<%
    request.setAttribute("pageTitle", "Detalle de Transacción - Sistema Botica");
    
    String idParam = request.getParameter("id");
    TransaccionDTO transaccion = null;
    String error = null;
    
    if (idParam != null && !idParam.isEmpty()) {
        try {
            Long id = Long.parseLong(idParam);
            transaccion = new VentaApiClient().buscarTransaccionPorId(id);
            
            if (transaccion == null) {
                error = "Transacción no encontrada";
            }
        } catch (NumberFormatException e) {
            error = "ID de transacción inválido";
        } catch (Exception e) {
            error = "Error al cargar la transacción: " + e.getMessage();
        }
    } else {
        error = "No se especificó el ID de la transacción";
    }
    
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>

<style>
    .detalle-header {
        background: linear-gradient(135deg, #1a5a4c 0%, #2d8a7a 100%);
        color: white;
        padding: 25px;
        border-radius: 10px;
        margin-bottom: 25px;
    }
    
    .badge-compra {
        background: #28a745;
        color: white;
        padding: 6px 15px;
        border-radius: 20px;
        font-size: 14px;
        display: inline-block;
    }
    
    .badge-venta {
        background: #007bff;
        color: white;
        padding: 6px 15px;
        border-radius: 20px;
        font-size: 14px;
        display: inline-block;
    }
    
    .badge-completada {
        background: #d4edda;
        color: #155724;
        padding: 5px 12px;
        border-radius: 15px;
        display: inline-block;
    }
    
    .badge-anulada {
        background: #f8d7da;
        color: #721c24;
        padding: 5px 12px;
        border-radius: 15px;
        display: inline-block;
    }
    
    .info-label {
        font-weight: 600;
        color: #555;
        font-size: 13px;
    }
    
    .totales-container {
        background: #f8f9fa;
        border-radius: 8px;
        padding: 15px;
    }
    
    .total-row {
        display: flex;
        justify-content: space-between;
        padding: 8px 0;
        border-bottom: 1px solid #e9ecef;
    }
    
    .total-row:last-child {
        border-bottom: none;
        font-weight: bold;
        font-size: 18px;
        color: #1a5a4c;
    }
    
    .btn-volver {
        background: white;
        color: #1a5a4c;
        border: none;
    }
    
    .btn-volver:hover {
        background: #f8f9fa;
        color: #1a5a4c;
    }
</style>

<%@ include file="/WEB-INF/includes/navbar.jsp" %>

<div class="container-fluid">
    <div class="row">
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4 py-4">
            
            <% if (error != null) { %>
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i><%= error %>
                    <a href="<%= request.getContextPath() %>/admin/inventario/listar.jsp" class="alert-link ms-2">Volver al listado</a>
                </div>
            <% } else if (transaccion != null) { %>
            
            <!-- Encabezado -->
            <div class="detalle-header">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h4 class="mb-2">
                            <i class="bi bi-receipt me-2"></i>
                            Transacción <%= transaccion.getNumeroTransaccion() %>
                        </h4>
                        <div>
                            <% if (transaccion.getTipoTransaccionId() != null && transaccion.getTipoTransaccionId() == 1L) { %>
                                <span class="badge-compra">
                                    <i class="bi bi-cart-check me-1"></i>COMPRA
                                </span>
                            <% } else { %>
                                <span class="badge-venta">
                                    <i class="bi bi-cash me-1"></i>VENTA
                                </span>
                            <% } %>
                            
                            <% if ("COMPLETADA".equals(transaccion.getEstado())) { %>
                                <span class="badge-completada ms-2">
                                    <i class="bi bi-check-circle me-1"></i>Completada
                                </span>
                            <% } else { %>
                                <span class="badge-anulada ms-2">
                                    <i class="bi bi-x-circle me-1"></i>Anulada
                                </span>
                            <% } %>
                        </div>
                    </div>
                    <a href="<%= request.getContextPath() %>/admin/inventario/listar.jsp" class="btn btn-volver">
                        <i class="bi bi-arrow-left me-1"></i>Volver al listado
                    </a>
                </div>
            </div>

            <div class="row">
                <!-- Información General -->
                <div class="col-md-6">
                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-info-circle me-2"></i>Información General
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <div class="info-label">N° Transacción</div>
                                <div><strong><%= transaccion.getNumeroTransaccion() %></strong></div>
                            </div>
                            
                            <div class="mb-3">
                                <div class="info-label">Fecha y Hora</div>
                                <div><%= transaccion.getFecha() != null ? sdf.format(transaccion.getFecha()) : "-" %></div>
                            </div>
                            
                            <div class="mb-3">
                                <div class="info-label">Tipo</div>
                                <div><%= transaccion.getTipoTransaccionNombre() != null ? transaccion.getTipoTransaccionNombre() : "-" %></div>
                            </div>
                            
                            <div class="mb-3">
                                <div class="info-label">Método de Pago</div>
                                <div><%= transaccion.getMetodoPago() != null ? transaccion.getMetodoPago() : "No especificado" %></div>
                            </div>
                            
                            <% if (transaccion.getObservaciones() != null && !transaccion.getObservaciones().isEmpty()) { %>
                                <div class="mb-0">
                                    <div class="info-label">Observaciones</div>
                                    <div><%= transaccion.getObservaciones() %></div>
                                </div>
                            <% } %>
                        </div>
                    </div>
                </div>
                
                <!-- Información de Personas -->
                <div class="col-md-6">
                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-people me-2"></i>Personas Involucradas
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <div class="info-label">Usuario que registró</div>
                                <div>
                                    <i class="bi bi-person-badge me-1"></i>
                                    <%= transaccion.getUsuarioNombre() != null ? transaccion.getUsuarioNombre() : "No disponible" %>
                                </div>
                            </div>
                            
                            <% if (transaccion.getTipoTransaccionId() != null && transaccion.getTipoTransaccionId() == 1L) { %>
                                <div class="mb-0">
                                    <div class="info-label">Proveedor</div>
                                    <div>
                                        <i class="bi bi-building me-1"></i>
                                        <%= transaccion.getProveedor() != null ? transaccion.getProveedor().getRazonSocial() : "No especificado" %>
                                    </div>
                                </div>
                            <% } %>
                            
                            <% if (transaccion.getTipoTransaccionId() != null && transaccion.getTipoTransaccionId() == 2L && transaccion.getNombrePersona() != null && !transaccion.getNombrePersona().isEmpty()) { %>
                                <div class="mb-0">
                                    <div class="info-label">Cliente</div>
                                    <div>
                                        <i class="bi bi-person me-1"></i>
                                        <%= transaccion.getNombrePersona() %>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Detalle de Productos -->
            <div class="card mb-4">
                <div class="card-header">
                    <i class="bi bi-box-seam me-2"></i>Productos
                </div>
                <div class="card-body">
                    <% 
                        List<DetalleTransaccionDTO> detalles = transaccion.getDetalles();
                        if (detalles == null || detalles.isEmpty()) { 
                    %>
                        <div class="text-center text-muted py-4">
                            <i class="bi bi-inbox" style="font-size: 40px;"></i>
                            <p class="mt-2">No hay productos en esta transacción</p>
                        </div>
                    <% } else { %>
                        <div class="table-responsive">
                            <table class="table">
                                <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>Producto</th>
                                        <th>Lote</th>
                                        <th>F. Vencimiento</th>
                                        <th class="text-center">Cantidad</th>
                                        <th class="text-end">P. Unitario</th>
                                        <th class="text-end">Subtotal</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% 
                                        int contador = 0;
                                        for (DetalleTransaccionDTO d : detalles) { 
                                            contador++;
                                    %>
                                        <tr>
                                            <td><%= contador %></td>
                                            <td>
                                                <strong><%= d.getNombreComercial() != null ? d.getNombreComercial() : "-" %></strong>
                                                <% if (d.getConcentracion() != null && !d.getConcentracion().isEmpty()) { %>
                                                    <small class="text-muted d-block"><%= d.getConcentracion() %></small>
                                                <% } %>
                                            </td>
                                            <td><span class="badge bg-secondary"><%= d.getLote() != null ? d.getLote() : "-" %></span></td>
                                            <td><%= d.getFechaVencimiento() != null ? d.getFechaVencimiento() : "-" %></td>
                                            <td class="text-center"><%= d.getCantidad() %></td>
                                            <td class="text-end">S/ <%= d.getPrecioUnitario() != null ? String.format("%.2f", d.getPrecioUnitario()) : "0.00" %></td>
                                            <td class="text-end"><strong>S/ <%= d.getSubtotal() != null ? String.format("%.2f", d.getSubtotal()) : "0.00" %></strong></td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                        
                        <!-- Totales -->
                        <div class="row justify-content-end">
                            <div class="col-md-4">
                                <div class="totales-container">
                                    <div class="total-row">
                                        <span>Subtotal:</span>
                                        <span>S/ <%= transaccion.getSubtotal() != null ? String.format("%.2f", transaccion.getSubtotal()) : "0.00" %></span>
                                    </div>
                                    <div class="total-row">
                                        <span>IGV (18%):</span>
                                        <span>S/ <%= transaccion.getIgv() != null ? String.format("%.2f", transaccion.getIgv()) : "0.00" %></span>
                                    </div>
                                    <div class="total-row">
                                        <span>TOTAL:</span>
                                        <span>S/ <%= transaccion.getTotal() != null ? String.format("%.2f", transaccion.getTotal()) : "0.00" %></span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    <% } %>
                </div>
            </div>

            <!-- Botones de Acción -->
            <div class="d-flex justify-content-between">
                <a href="<%= request.getContextPath() %>/admin/inventario/listar.jsp" class="btn btn-secondary">
                    <i class="bi bi-arrow-left me-1"></i>Volver al listado
                </a>
                
                <button class="btn btn-outline-primary" onclick="window.print()">
                    <i class="bi bi-printer me-1"></i>Imprimir
                </button>
            </div>
            
            <% } %>
        </main>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
