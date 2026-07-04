<%-- 
    Document   : productos
    Created on : 09 dic. 2025
    Author     : Sistema Botica
    Description: Listado y gestión de productos en inventario (mi farmacia)
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="DAO.ProductoDAO, DTO.ProductoDTO, java.util.List, java.text.SimpleDateFormat, java.text.DecimalFormat" %>
<%
    request.setAttribute("pageTitle", "Mis Productos - Sistema Botica");
    
    // Parámetros de búsqueda y filtros
    String busqueda = request.getParameter("busqueda");
    String filtroStock = request.getParameter("filtroStock");
    String filtroVencimiento = request.getParameter("filtroVencimiento");
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
    
    // Obtener datos
    ProductoDAO productoDAO = new ProductoDAO();
    List<ProductoDTO> productos = productoDAO.buscarConFiltros(
        busqueda, null, filtroStock, filtroVencimiento, pagina, porPagina);
    int totalRegistros = productoDAO.contarConFiltros(busqueda, null, filtroStock, filtroVencimiento);
    
    int totalPaginas = (int) Math.ceil((double) totalRegistros / porPagina);
    if (totalPaginas < 1) totalPaginas = 1;
    
    // Estadísticas
    int totalProductos = productoDAO.contarTotal();
    int stockBajo = productoDAO.contarStockBajo();
    int agotados = productoDAO.contarAgotados();
    int porVencer = productoDAO.contarPorVencer(30);
    
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    DecimalFormat df = new DecimalFormat("#,##0.00");
    
    // Limpiar valores null
    if (busqueda == null) busqueda = "";
    if (filtroStock == null) filtroStock = "";
    if (filtroVencimiento == null) filtroVencimiento = "";
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>

<style>
    .productos-header {
        background: linear-gradient(135deg, #2d5a7c 0%, #1a3a5c 100%);
        color: white;
        padding: 20px;
        border-radius: 10px;
        margin-bottom: 20px;
    }
    
    .stat-card {
        background: #fff;
        border-radius: 10px;
        padding: 15px 20px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        text-align: center;
        transition: transform 0.2s;
    }
    
    .stat-card:hover {
        transform: translateY(-2px);
    }
    
    .stat-card .number {
        font-size: 28px;
        font-weight: 700;
    }
    
    .stat-card .label {
        font-size: 12px;
        color: #666;
    }
    
    .stat-total .number { color: #1a5a4c; }
    .stat-bajo .number { color: #ffc107; }
    .stat-agotado .number { color: #dc3545; }
    .stat-vencer .number { color: #17a2b8; }
    
    .badge-stock-ok {
        background: #28a745;
        color: white;
    }
    
    .badge-stock-bajo {
        background: #ffc107;
        color: #212529;
    }
    
    .badge-stock-agotado {
        background: #dc3545;
        color: white;
    }
    
    .badge-vigente {
        background: #28a745;
        color: white;
    }
    
    .badge-por-vencer {
        background: #ffc107;
        color: #212529;
    }
    
    .badge-vencido {
        background: #dc3545;
        color: white;
    }
    
    .btn-ajustar {
        background: #17a2b8;
        color: white;
        border: none;
        padding: 5px 12px;
        border-radius: 5px;
        font-size: 12px;
        cursor: pointer;
    }
    
    .btn-ajustar:hover {
        background: #138496;
        color: white;
    }
    
    .precio-compra {
        color: #6c757d;
        font-size: 12px;
    }
    
    .precio-venta {
        color: #28a745;
        font-weight: bold;
    }
    
    .lote-badge {
        background: #e9ecef;
        color: #495057;
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 11px;
        font-family: monospace;
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
    
    .table thead th {
        background: #f8f9fa;
        font-weight: 600;
        font-size: 13px;
        white-space: nowrap;
    }
    
    .table tbody td {
        vertical-align: middle;
    }
    
    .producto-nombre {
        font-weight: 600;
        color: #333;
    }
    
    .producto-detalle {
        font-size: 12px;
        color: #666;
    }
</style>

<%@ include file="/WEB-INF/includes/navbar.jsp" %>

<div class="container-fluid">
    <div class="row">
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4 py-4">
            
            <!-- Header -->
            <div class="productos-header">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h4 class="mb-0"><i class="bi bi-box-seam me-2"></i>Mis Productos</h4>
                        <small>Inventario actual de la farmacia</small>
                    </div>
                    <a href="<%= request.getContextPath() %>/admin/compras/nueva.jsp" class="btn btn-light">
                        <i class="bi bi-cart-plus me-1"></i>Nueva Compra
                    </a>
                </div>
            </div>

            

            <!-- Filtros -->
            <div class="card mb-4">
                <div class="card-body">
                    <form action="<%= request.getContextPath() %>/admin/inventario/productos.jsp" method="get" class="row g-3 align-items-end">
                        <div class="col-md-4">
                            <label class="form-label">Buscar producto</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-search"></i></span>
                                <input type="text" class="form-control" name="busqueda" 
                                       value="<%= busqueda %>" 
                                       placeholder="Nombre, lote, código...">
                            </div>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Stock</label>
                            <select class="form-select" name="filtroStock">
                                <option value="">Todos</option>
                                <option value="disponible" <%= "disponible".equals(filtroStock) ? "selected" : "" %>>Disponible</option>
                                <option value="bajo" <%= "bajo".equals(filtroStock) ? "selected" : "" %>>Stock Bajo</option>
                                <option value="agotado" <%= "agotado".equals(filtroStock) ? "selected" : "" %>>Agotado</option>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Vencimiento</label>
                            <select class="form-select" name="filtroVencimiento">
                                <option value="">Todos</option>
                                <option value="por_vencer_30" <%= "por_vencer_30".equals(filtroVencimiento) ? "selected" : "" %>>Por vencer (30d)</option>
                                <option value="por_vencer_60" <%= "por_vencer_60".equals(filtroVencimiento) ? "selected" : "" %>>Por vencer (60d)</option>
                                <option value="vencido" <%= "vencido".equals(filtroVencimiento) ? "selected" : "" %>>Vencidos</option>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <button type="submit" class="btn btn-primary w-100">
                                <i class="bi bi-funnel me-1"></i>Filtrar
                            </button>
                        </div>
                        <div class="col-md-2">
                            <a href="<%= request.getContextPath() %>/admin/inventario/productos.jsp" class="btn btn-outline-secondary w-100">
                                <i class="bi bi-x-circle me-1"></i>Limpiar
                            </a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Tabla de productos -->
            <div class="card">
                <div class="card-body">
                    <% if (productos == null || productos.isEmpty()) { %>
                        <div class="empty-state">
                            <i class="bi bi-inbox"></i>
                            <h5>No hay productos</h5>
                            <p>No se encontraron productos con los filtros seleccionados.</p>
                            <a href="<%= request.getContextPath() %>/admin/compras/nueva.jsp" class="btn btn-primary">
                                <i class="bi bi-cart-plus me-1"></i>Registrar Compra
                            </a>
                        </div>
                    <% } else { %>
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead>
                                    <tr>
                                        <th>Producto</th>
                                        <th>Lote</th>
                                        <th class="text-center">Stock</th>
                                        <th class="text-center">Stock Mín.</th>
                                        <th class="text-end">P. Compra</th>
                                        <th class="text-end">P. Venta</th>
                                        <th class="text-center">Vencimiento</th>
                                        <!--<th class="text-center">Estado</th>-->
                                        <th class="text-center">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (ProductoDTO p : productos) { 
                                        String estadoStock = p.getEstadoStock();
                                        String estadoVenc = p.getEstadoVencimiento();
                                        String badgeStock = "badge-stock-ok";
                                        String badgeVenc = "badge-vigente";
                                        
                                        if ("BAJO".equals(estadoStock)) badgeStock = "badge-stock-bajo";
                                        else if ("AGOTADO".equals(estadoStock)) badgeStock = "badge-stock-agotado";
                                        
                                        if ("POR_VENCER".equals(estadoVenc)) badgeVenc = "badge-por-vencer";
                                        else if ("VENCIDO".equals(estadoVenc)) badgeVenc = "badge-vencido";
                                    %>
                                        <tr>
                                            <td>
                                                <div class="producto-nombre"><%= p.getNombreCompleto() %></div>
                                                <div class="producto-detalle">
                                                    <%= p.getCatalogoProducto() != null ? p.getCatalogoProducto().getFormaFarmaceutica() : "" %>
                                                    <% if (p.getCatalogoProducto() != null && p.getCatalogoProducto().getLaboratorio() != null) { %>
                                                        - <%= p.getCatalogoProducto().getLaboratorio() %>
                                                    <% } %>
                                                </div>
                                            </td>
                                            <td>
                                                <span class="lote-badge"><%= p.getLote() != null ? p.getLote() : "-" %></span>
                                            </td>
                                            <td class="text-center">
                                                <strong><%= p.getStockActual() %></strong>
                                            </td>
                                            <td class="text-center">
                                                <%= p.getStockMinimo() %>
                                            </td>
                                            <td class="text-end">
                                                <span class="precio-compra">S/ <%= p.getPrecioCompra() != null ? df.format(p.getPrecioCompra()) : "0.00" %></span>
                                            </td>
                                            <td class="text-end">
                                                <span class="precio-venta">S/ <%= p.getPrecioVenta() != null ? df.format(p.getPrecioVenta()) : "0.00" %></span>
                                            </td>
                                            <td class="text-center">
                                                <small><%= p.getFechaVencimiento() != null ? sdf.format(p.getFechaVencimiento()) : "-" %></small>
                                            </td>
<!--                                            <td class="text-center">
                                                <span class="badge <%= badgeStock %> me-1"><%= estadoStock %></span>
                                                <span class="badge <%= badgeVenc %>"><%= estadoVenc.replace("_", " ") %></span>
                                            </td>-->
                                            <td class="text-center">
                                                <button class="btn-ajustar" onclick="abrirModalAjuste(<%= p.getId() %>, '<%= p.getNombreCompleto().replace("'", "\\'") %>', <%= p.getStockActual() %>)">
                                                    <i class="bi bi-pencil-square me-1"></i>Ajustar
                                                </button>
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
                                (<%= totalRegistros %> productos)
                            </div>
                            <nav>
                                <ul class="pagination pagination-sm mb-0">
                                    <li class="page-item <%= pagina == 1 ? "disabled" : "" %>">
                                        <a class="page-link" href="<%= request.getContextPath() %>/admin/inventario/productos.jsp?pagina=<%= pagina - 1 %>&busqueda=<%= busqueda %>&filtroStock=<%= filtroStock %>&filtroVencimiento=<%= filtroVencimiento %>">
                                            Anterior
                                        </a>
                                    </li>
                                    
                                    <% 
                                        int inicio = Math.max(1, pagina - 2);
                                        int fin = Math.min(totalPaginas, pagina + 2);
                                        for (int i = inicio; i <= fin; i++) { 
                                    %>
                                        <li class="page-item <%= i == pagina ? "active" : "" %>">
                                            <a class="page-link" href="<%= request.getContextPath() %>/admin/inventario/productos.jsp?pagina=<%= i %>&busqueda=<%= busqueda %>&filtroStock=<%= filtroStock %>&filtroVencimiento=<%= filtroVencimiento %>">
                                                <%= i %>
                                            </a>
                                        </li>
                                    <% } %>
                                    
                                    <li class="page-item <%= pagina >= totalPaginas ? "disabled" : "" %>">
                                        <a class="page-link" href="<%= request.getContextPath() %>/admin/inventario/productos.jsp?pagina=<%= pagina + 1 %>&busqueda=<%= busqueda %>&filtroStock=<%= filtroStock %>&filtroVencimiento=<%= filtroVencimiento %>">
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

<!-- Modal Ajuste de Stock -->
<div class="modal fade" id="modalAjuste" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header bg-info text-white">
                <h5 class="modal-title"><i class="bi bi-pencil-square me-2"></i>Ajustar Stock</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="ajusteProductoId">
                
                <div class="mb-3">
                    <label class="form-label fw-bold">Producto:</label>
                    <p id="ajusteProductoNombre" class="mb-0"></p>
                </div>
                
                <div class="row mb-3">
                    <div class="col-6">
                        <label class="form-label">Stock Actual:</label>
                        <input type="number" class="form-control" id="ajusteStockActual" readonly>
                    </div>
                    <div class="col-6">
                        <label class="form-label">Nuevo Stock: *</label>
                        <input type="number" class="form-control" id="ajusteNuevoStock" min="0" required>
                    </div>
                </div>
                
                <div class="mb-3">
                    <label class="form-label">Motivo del ajuste: *</label>
                    <select class="form-select" id="ajusteMotivoSelect">
                        <option value="">Seleccionar motivo...</option>
                        <option value="Conteo físico">Conteo físico</option>
                        <option value="Corrección de error">Corrección de error</option>
                        <option value="Producto dañado">Producto dañado</option>
                        <option value="Producto vencido">Producto vencido</option>
                        <option value="Merma">Merma</option>
                        <option value="Otro">Otro (especificar)</option>
                    </select>
                </div>
                
                <div class="mb-3" id="ajusteMotivoOtroDiv" style="display: none;">
                    <label class="form-label">Especifique el motivo:</label>
                    <textarea class="form-control" id="ajusteMotivoOtro" rows="2"></textarea>
                </div>
                
                <div id="ajusteDiferencia" class="alert alert-info" style="display: none;">
                    <i class="bi bi-info-circle me-1"></i>
                    <span id="ajusteDiferenciaTexto"></span>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn btn-info text-white" id="btnGuardarAjuste" onclick="guardarAjuste()">
                    <i class="bi bi-check-circle me-1"></i>Guardar Ajuste
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Toast -->
<div class="toast-container position-fixed top-0 end-0 p-3">
    <div id="toastNotificacion" class="toast" role="alert">
        <div class="toast-header">
            <i class="bi bi-check-circle text-success me-2" id="toastIcon"></i>
            <strong class="me-auto" id="toastTitulo">Notificación</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
        </div>
        <div class="toast-body" id="toastMensaje"></div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>

<script>
    var contextPath = '<%= request.getContextPath() %>';
    
    // Mostrar/ocultar campo otro motivo
    document.getElementById('ajusteMotivoSelect').addEventListener('change', function() {
        var otroDiv = document.getElementById('ajusteMotivoOtroDiv');
        if (this.value === 'Otro') {
            otroDiv.style.display = 'block';
        } else {
            otroDiv.style.display = 'none';
        }
    });
    
    // Calcular diferencia
    document.getElementById('ajusteNuevoStock').addEventListener('input', function() {
        var stockActual = parseInt(document.getElementById('ajusteStockActual').value) || 0;
        var nuevoStock = parseInt(this.value) || 0;
        var diferencia = nuevoStock - stockActual;
        
        var divDif = document.getElementById('ajusteDiferencia');
        var textoDif = document.getElementById('ajusteDiferenciaTexto');
        
        if (diferencia !== 0) {
            divDif.style.display = 'block';
            if (diferencia > 0) {
                divDif.className = 'alert alert-success';
                textoDif.textContent = 'Se agregarán +' + diferencia + ' unidades al inventario';
            } else {
                divDif.className = 'alert alert-warning';
                textoDif.textContent = 'Se restarán ' + Math.abs(diferencia) + ' unidades del inventario';
            }
        } else {
            divDif.style.display = 'none';
        }
    });
    
    function abrirModalAjuste(id, nombre, stockActual) {
        document.getElementById('ajusteProductoId').value = id;
        document.getElementById('ajusteProductoNombre').textContent = nombre;
        document.getElementById('ajusteStockActual').value = stockActual;
        document.getElementById('ajusteNuevoStock').value = stockActual;
        document.getElementById('ajusteMotivoSelect').value = '';
        document.getElementById('ajusteMotivoOtro').value = '';
        document.getElementById('ajusteMotivoOtroDiv').style.display = 'none';
        document.getElementById('ajusteDiferencia').style.display = 'none';
        
        new bootstrap.Modal(document.getElementById('modalAjuste')).show();
    }
    
    function guardarAjuste() {
        var productoId = document.getElementById('ajusteProductoId').value;
        var nuevoStock = document.getElementById('ajusteNuevoStock').value;
        var motivoSelect = document.getElementById('ajusteMotivoSelect').value;
        var motivoOtro = document.getElementById('ajusteMotivoOtro').value;
        
        if (!nuevoStock || nuevoStock < 0) {
            mostrarToast('Ingrese un stock válido', 'error');
            return;
        }
        
        if (!motivoSelect) {
            mostrarToast('Seleccione un motivo', 'error');
            return;
        }
        
        var motivo = motivoSelect === 'Otro' ? motivoOtro : motivoSelect;
        if (!motivo.trim()) {
            mostrarToast('Ingrese el motivo del ajuste', 'error');
            return;
        }
        
        var btn = document.getElementById('btnGuardarAjuste');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Guardando...';
        
        fetch(contextPath + '/InventarioController?action=ajustarStock', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: 'productoId=' + productoId + '&nuevoStock=' + nuevoStock + '&motivo=' + encodeURIComponent(motivo)
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-check-circle me-1"></i>Guardar Ajuste';
            
            if (data.success) {
                bootstrap.Modal.getInstance(document.getElementById('modalAjuste')).hide();
                mostrarToast('Stock ajustado correctamente', 'success');
                setTimeout(function() {
                    location.reload();
                }, 1000);
            } else {
                mostrarToast(data.error || 'Error al ajustar stock', 'error');
            }
        })
        .catch(function(err) {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-check-circle me-1"></i>Guardar Ajuste';
            mostrarToast('Error de conexión', 'error');
        });
    }
    
    function mostrarToast(mensaje, tipo) {
        var toast = document.getElementById('toastNotificacion');
        var icon = document.getElementById('toastIcon');
        var titulo = document.getElementById('toastTitulo');
        var body = document.getElementById('toastMensaje');
        
        body.textContent = mensaje;
        
        if (tipo === 'success') {
            icon.className = 'bi bi-check-circle text-success me-2';
            titulo.textContent = 'Éxito';
        } else if (tipo === 'error') {
            icon.className = 'bi bi-exclamation-circle text-danger me-2';
            titulo.textContent = 'Error';
        }
        
        new bootstrap.Toast(toast).show();
    }
</script>
