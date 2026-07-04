<%-- 
    Document   : catalogo
    Created on : 09 dic. 2025
    Author     : Sistema Botica
    Description: CRUD del Catálogo de Productos DIGEMID
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("pageTitle", "Catálogo de Productos - Sistema Botica");
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>

<style>
    .catalogo-header {
        background: linear-gradient(135deg, #1a5a4c 0%, #2d8a7a 100%);
        color: white;
        padding: 20px;
        border-radius: 10px;
        margin-bottom: 20px;
    }
    
    .search-box {
        position: relative;
        max-width: 500px;
    }
    
    .search-box input {
        padding: 12px 20px 12px 45px;
        border-radius: 25px;
        border: none;
        font-size: 16px;
        width: 100%;
    }
    
    .search-box i {
        position: absolute;
        left: 15px;
        top: 50%;
        transform: translateY(-50%);
        color: #666;
    }
    
    .autocomplete-dropdown {
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        background: #fff;
        border-radius: 10px;
        box-shadow: 0 5px 20px rgba(0,0,0,0.2);
        max-height: 400px;
        overflow-y: auto;
        z-index: 1000;
        display: none;
        margin-top: 5px;
    }
    
    .autocomplete-dropdown.show {
        display: block;
    }
    
    .autocomplete-item {
        padding: 12px 15px;
        cursor: pointer;
        border-bottom: 1px solid #eee;
        transition: background 0.2s;
    }
    
    .autocomplete-item:hover {
        background: #f0f9f7;
    }
    
    .producto-nombre {
        font-weight: 600;
        color: #333;
    }
    
    .producto-detalle {
        font-size: 12px;
        color: #666;
    }
    
    .producto-laboratorio {
        font-size: 11px;
        color: #999;
    }
    
    .stats-card {
        background: #fff;
        border-radius: 10px;
        padding: 15px 20px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        text-align: center;
    }
    
    .stats-card .number {
        font-size: 28px;
        font-weight: 700;
        color: #1a5a4c;
    }
    
    .stats-card .label {
        font-size: 12px;
        color: #666;
    }
    
    .producto-card {
        background: #fff;
        border-radius: 10px;
        padding: 20px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        margin-bottom: 15px;
        transition: transform 0.2s, box-shadow 0.2s;
    }
    
    .producto-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 15px rgba(0,0,0,0.15);
    }
    
    .producto-card .nombre {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin-bottom: 5px;
    }
    
    .producto-card .info {
        font-size: 13px;
        color: #666;
    }
    
    .producto-card .laboratorio {
        font-size: 12px;
        color: #999;
    }
    
    .producto-card .acciones {
        display: flex;
        gap: 8px;
        margin-top: 10px;
    }
    
    .btn-accion {
        padding: 5px 12px;
        border-radius: 5px;
        font-size: 12px;
        cursor: pointer;
        border: none;
        transition: all 0.2s;
    }
    
    .btn-editar {
        background: #17a2b8;
        color: white;
    }
    
    .btn-editar:hover {
        background: #138496;
    }
    
    .btn-eliminar {
        background: #dc3545;
        color: white;
    }
    
    .btn-eliminar:hover {
        background: #c82333;
    }
    
    .empty-state {
        text-align: center;
        padding: 60px 20px;
        color: #999;
    }
    
    .empty-state i {
        font-size: 80px;
        margin-bottom: 20px;
        opacity: 0.5;
    }
    
    .badge-codigo {
        background: #e9ecef;
        color: #495057;
        padding: 3px 8px;
        border-radius: 4px;
        font-size: 11px;
        font-family: monospace;
    }
    
    .form-floating label {
        color: #666;
    }
    
    .resultado-info {
        background: #f8f9fa;
        padding: 10px 15px;
        border-radius: 8px;
        margin-bottom: 15px;
        font-size: 14px;
    }
</style>

<%@ include file="/WEB-INF/includes/navbar.jsp" %>

<div class="container-fluid">
    <div class="row">
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4 py-3">

            <!-- Header -->
            <div class="catalogo-header">
                <div class="row align-items-center">
                    <div class="col-md-6">
                        <h4 class="mb-1"><i class="bi bi-journal-medical me-2"></i>Catálogo de Productos DIGEMID</h4>
                        <small class="opacity-75">Gestiona el catálogo de medicamentos disponibles</small>
                    </div>
                    <div class="col-md-6 text-md-end mt-3 mt-md-0">
                        <button class="btn btn-light" onclick="mostrarModalNuevo()">
                            <i class="bi bi-plus-circle me-1"></i>Agregar Producto
                        </button>
                    </div>
                </div>
            </div>

            <!-- Estadísticas -->
            <div class="row mb-4">
                <div class="col-md-3">
                    <div class="stats-card">
                        <div class="number" id="totalProductos">-</div>
                        <div class="label">Total Productos</div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stats-card">
                        <div class="number" id="totalActivos">-</div>
                        <div class="label">Activos</div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stats-card">
                        <div class="number" id="totalLaboratorios">-</div>
                        <div class="label">Laboratorios</div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stats-card">
                        <div class="number" id="ultimoAgregado">-</div>
                        <div class="label">Último Agregado</div>
                    </div>
                </div>
            </div>

            <!-- Buscador -->
            <div class="card mb-4">
                <div class="card-body">
                    <div class="row align-items-center">
                        <div class="col-md-8">
                            <div class="search-box">
                                <i class="bi bi-search"></i>
                                <input type="text" id="buscarProducto" 
                                       placeholder="Buscar por nombre, principio activo, laboratorio..." 
                                       autocomplete="off">
                                <div id="autocompleteDropdown" class="autocomplete-dropdown"></div>
                            </div>
                        </div>
                        <div class="col-md-4 text-md-end mt-3 mt-md-0">
                            <button class="btn btn-outline-secondary btn-sm" onclick="limpiarBusqueda()">
                                <i class="bi bi-x-circle me-1"></i>Limpiar
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Resultados -->
            <div id="resultadoInfo" class="resultado-info" style="display: none;">
                <i class="bi bi-info-circle me-1"></i>
                <span id="infoTexto"></span>
            </div>

            <div id="listaProductos">
                <div class="empty-state">
                    <i class="bi bi-search"></i>
                    <h5>Busca un producto para comenzar</h5>
                    <p class="text-muted">Escribe al menos 2 caracteres para buscar en el catálogo</p>
                </div>
            </div>

            <!-- Paginación -->
            <div id="paginacion" class="d-flex justify-content-center mt-4" style="display: none !important;">
            </div>

        </main>
    </div>
</div>

<!-- Modal Agregar/Editar Producto -->
<div class="modal fade" id="modalProducto" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title" id="modalTitulo">
                    <i class="bi bi-plus-circle me-2"></i>Nuevo Producto
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="productoId">
                
                <div class="row g-3">
                    <!-- Código -->
                    <div class="col-md-4">
                        <div class="form-floating">
                            <input type="text" class="form-control" id="codigoProducto" placeholder="Código">
                            <label>Código Producto</label>
                        </div>
                    </div>
                    
                    <!-- Registro Sanitario -->
                    <div class="col-md-4">
                        <div class="form-floating">
                            <input type="text" class="form-control" id="registroSanitario" placeholder="Registro">
                            <label>Registro Sanitario</label>
                        </div>
                    </div>
                    
                    <!-- Cantidad -->
                    <div class="col-md-4">
                        <div class="form-floating">
                            <input type="number" class="form-control" id="cantidad" placeholder="Cantidad" min="1" value="1">
                            <label>Cantidad por Presentación</label>
                        </div>
                    </div>
                    
                    <!-- Nombre Comercial -->
                    <div class="col-md-6">
                        <div class="form-floating">
                            <input type="text" class="form-control" id="nombreComercial" placeholder="Nombre" required>
                            <label>Nombre Comercial *</label>
                        </div>
                    </div>
                    
                    <!-- Principio Activo -->
                    <div class="col-md-6">
                        <div class="form-floating">
                            <input type="text" class="form-control" id="principioActivo" placeholder="Principio">
                            <label>Principio Activo</label>
                        </div>
                    </div>
                    
                    <!-- Concentración -->
                    <div class="col-md-4">
                        <div class="form-floating">
                            <input type="text" class="form-control" id="concentracion" placeholder="Concentración">
                            <label>Concentración</label>
                        </div>
                    </div>
                    
                    <!-- Forma Farmacéutica -->
                    <div class="col-md-4">
                        <div class="form-floating">
                            <input type="text" class="form-control" id="formaFarmaceutica" placeholder="Forma">
                            <label>Forma Farmacéutica</label>
                        </div>
                    </div>
                    
                    <!-- Presentación -->
                    <div class="col-md-4">
                        <div class="form-floating">
                            <input type="text" class="form-control" id="presentacion" placeholder="Presentación">
                            <label>Presentación</label>
                        </div>
                    </div>
                    
                    <!-- Laboratorio -->
                    <div class="col-md-6">
                        <div class="form-floating">
                            <input type="text" class="form-control" id="laboratorio" placeholder="Laboratorio">
                            <label>Laboratorio</label>
                        </div>
                    </div>
                    
                    <!-- Fabricante -->
                    <div class="col-md-6">
                        <div class="form-floating">
                            <input type="text" class="form-control" id="nombreFabricante" placeholder="Fabricante">
                            <label>Nombre Fabricante</label>
                        </div>
                    </div>
                    
                    <!-- Titular -->
                    <div class="col-12">
                        <div class="form-floating">
                            <input type="text" class="form-control" id="nombreTitular" placeholder="Titular">
                            <label>Nombre Titular</label>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn btn-primary" id="btnGuardar" onclick="guardarProducto()">
                    <i class="bi bi-save me-1"></i>Guardar
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Modal Confirmar Eliminación -->
<div class="modal fade" id="modalEliminar" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header bg-danger text-white">
                <h5 class="modal-title"><i class="bi bi-exclamation-triangle me-2"></i>Confirmar Eliminación</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p>¿Está seguro que desea eliminar este producto del catálogo?</p>
                <p class="fw-bold" id="productoEliminarNombre"></p>
                <p class="text-muted small">Esta acción desactivará el producto. Los registros de inventario asociados no se eliminarán.</p>
                <input type="hidden" id="productoEliminarId">
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn btn-danger" onclick="confirmarEliminar()">
                    <i class="bi bi-trash me-1"></i>Eliminar
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
    var timeoutBusqueda = null;
    var productosResultado = [];
    
    document.addEventListener('DOMContentLoaded', function() {
        cargarEstadisticas();
        configurarBuscador();
    });
    
    // =====================================================
    //          ESTADÍSTICAS
    // =====================================================
    function cargarEstadisticas() {
        fetch(contextPath + '/CatalogoController?action=estadisticas')
            .then(function(r) { return r.json(); })
            .then(function(data) {
                document.getElementById('totalProductos').textContent = data.total || 0;
                document.getElementById('totalActivos').textContent = data.activos || 0;
                document.getElementById('totalLaboratorios').textContent = data.laboratorios || 0;
                document.getElementById('ultimoAgregado').textContent = data.ultimoAgregado || '-';
            })
            .catch(function(err) {
                console.error('Error al cargar estadísticas:', err);
            });
    }
    
    // =====================================================
    //          BUSCADOR (similar al de compras)
    // =====================================================
    function configurarBuscador() {
        var inputBusqueda = document.getElementById('buscarProducto');
        var dropdown = document.getElementById('autocompleteDropdown');
        
        inputBusqueda.addEventListener('input', function() {
            clearTimeout(timeoutBusqueda);
            var termino = this.value.trim();
            
            if (termino.length < 2) {
                dropdown.classList.remove('show');
                mostrarEstadoVacio();
                return;
            }
            
            timeoutBusqueda = setTimeout(function() {
                buscarProductos(termino);
            }, 300);
        });
        
        inputBusqueda.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                dropdown.classList.remove('show');
            }
        });
        
        document.addEventListener('click', function(e) {
            if (!inputBusqueda.contains(e.target) && !dropdown.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });
    }
    
    function buscarProductos(termino) {
        var dropdown = document.getElementById('autocompleteDropdown');
        
        fetch(contextPath + '/CatalogoController?action=buscar&q=' + encodeURIComponent(termino))
            .then(function(r) { return r.json(); })
            .then(function(productos) {
                productosResultado = productos;
                
                if (productos.length === 0) {
                    dropdown.innerHTML = '<div class="autocomplete-item text-muted text-center">No se encontraron resultados</div>';
                    mostrarEstadoVacio('No se encontraron productos para "' + termino + '"');
                } else {
                    var html = '';
                    for (var i = 0; i < productos.length; i++) {
                        var p = productos[i];
                        html += '<div class="autocomplete-item" data-index="' + i + '">' +
                                '<div class="producto-nombre">' + (p.nombreComercial || 'Sin nombre') + '</div>' +
                                '<div class="producto-detalle">' +
                                (p.principioActivo || '') + ' ' + (p.concentracion || '') + ' - ' + (p.formaFarmaceutica || '') +
                                '</div>' +
                                '<div class="producto-laboratorio">' + (p.laboratorio || '') + '</div>' +
                            '</div>';
                    }
                    dropdown.innerHTML = html;
                    
                    var items = dropdown.querySelectorAll('.autocomplete-item');
                    for (var j = 0; j < items.length; j++) {
                        items[j].addEventListener('click', function() {
                            var index = parseInt(this.getAttribute('data-index'));
                            seleccionarProducto(productosResultado[index]);
                        });
                    }
                    
                    // Mostrar lista en cards
                    mostrarResultados(productos, termino);
                }
                
                dropdown.classList.add('show');
            })
            .catch(function(err) {
                console.error('Error en búsqueda:', err);
                dropdown.innerHTML = '<div class="autocomplete-item text-danger">Error al buscar</div>';
                dropdown.classList.add('show');
            });
    }
    
    function mostrarResultados(productos, termino) {
        var container = document.getElementById('listaProductos');
        var infoDiv = document.getElementById('resultadoInfo');
        var infoTexto = document.getElementById('infoTexto');
        
        infoDiv.style.display = 'block';
        infoTexto.textContent = 'Se encontraron ' + productos.length + ' productos para "' + termino + '"';
        
        var html = '<div class="row">';
        for (var i = 0; i < productos.length; i++) {
            var p = productos[i];
            html += '<div class="col-md-6 col-lg-4">' +
                '<div class="producto-card">' +
                    '<div class="d-flex justify-content-between align-items-start">' +
                        '<div class="nombre">' + (p.nombreComercial || 'Sin nombre') + '</div>' +
                        '<span class="badge-codigo">' + (p.codigoProducto || 'S/C') + '</span>' +
                    '</div>' +
                    '<div class="info">' +
                        (p.principioActivo || '') + ' ' + (p.concentracion || '') +
                    '</div>' +
                    '<div class="info">' + (p.formaFarmaceutica || '') + ' - ' + (p.presentacion || '') + '</div>' +
                    '<div class="laboratorio"><i class="bi bi-building me-1"></i>' + (p.laboratorio || 'Sin laboratorio') + '</div>' +
                    '<div class="acciones">' +
                        '<button class="btn-accion btn-editar" onclick="editarProducto(' + p.id + ')">' +
                            '<i class="bi bi-pencil me-1"></i>Editar' +
                        '</button>' +
                        '<button class="btn-accion btn-eliminar" onclick="eliminarProducto(' + p.id + ', \'' + (p.nombreComercial || '').replace(/'/g, "\\'") + '\')">' +
                            '<i class="bi bi-trash me-1"></i>Eliminar' +
                        '</button>' +
                    '</div>' +
                '</div>' +
            '</div>';
        }
        html += '</div>';
        
        container.innerHTML = html;
    }
    
    function mostrarEstadoVacio(mensaje) {
        var container = document.getElementById('listaProductos');
        var infoDiv = document.getElementById('resultadoInfo');
        infoDiv.style.display = 'none';
        
        container.innerHTML = '<div class="empty-state">' +
            '<i class="bi bi-search"></i>' +
            '<h5>' + (mensaje || 'Busca un producto para comenzar') + '</h5>' +
            '<p class="text-muted">Escribe al menos 2 caracteres para buscar en el catálogo</p>' +
        '</div>';
    }
    
    function seleccionarProducto(producto) {
        document.getElementById('autocompleteDropdown').classList.remove('show');
        editarProducto(producto.id);
    }
    
    function limpiarBusqueda() {
        document.getElementById('buscarProducto').value = '';
        document.getElementById('autocompleteDropdown').classList.remove('show');
        mostrarEstadoVacio();
    }
    
    // =====================================================
    //          MODAL NUEVO/EDITAR
    // =====================================================
    function mostrarModalNuevo() {
        document.getElementById('modalTitulo').innerHTML = '<i class="bi bi-plus-circle me-2"></i>Nuevo Producto';
        document.getElementById('productoId').value = '';
        document.getElementById('codigoProducto').value = '';
        document.getElementById('registroSanitario').value = '';
        document.getElementById('cantidad').value = '1';
        document.getElementById('nombreComercial').value = '';
        document.getElementById('principioActivo').value = '';
        document.getElementById('concentracion').value = '';
        document.getElementById('formaFarmaceutica').value = '';
        document.getElementById('presentacion').value = '';
        document.getElementById('laboratorio').value = '';
        document.getElementById('nombreFabricante').value = '';
        document.getElementById('nombreTitular').value = '';
        
        new bootstrap.Modal(document.getElementById('modalProducto')).show();
    }
    
    function editarProducto(id) {
        fetch(contextPath + '/CatalogoController?action=obtener&id=' + id)
            .then(function(r) { return r.json(); })
            .then(function(data) {
                if (data.success) {
                    var p = data.producto;
                    document.getElementById('modalTitulo').innerHTML = '<i class="bi bi-pencil me-2"></i>Editar Producto';
                    document.getElementById('productoId').value = p.id;
                    document.getElementById('codigoProducto').value = p.codigoProducto || '';
                    document.getElementById('registroSanitario').value = p.registroSanitario || '';
                    document.getElementById('cantidad').value = p.cantidad || 1;
                    document.getElementById('nombreComercial').value = p.nombreComercial || '';
                    document.getElementById('principioActivo').value = p.principioActivo || '';
                    document.getElementById('concentracion').value = p.concentracion || '';
                    document.getElementById('formaFarmaceutica').value = p.formaFarmaceutica || '';
                    document.getElementById('presentacion').value = p.presentacion || '';
                    document.getElementById('laboratorio').value = p.laboratorio || '';
                    document.getElementById('nombreFabricante').value = p.nombreFabricante || '';
                    document.getElementById('nombreTitular').value = p.nombreTitular || '';
                    
                    new bootstrap.Modal(document.getElementById('modalProducto')).show();
                } else {
                    mostrarToast(data.error || 'Error al cargar producto', 'error');
                }
            })
            .catch(function(err) {
                mostrarToast('Error de conexión', 'error');
            });
    }
    
    function guardarProducto() {
        var nombreComercial = document.getElementById('nombreComercial').value.trim();
        
        if (!nombreComercial) {
            mostrarToast('El nombre comercial es obligatorio', 'error');
            document.getElementById('nombreComercial').focus();
            return;
        }
        
        var datos = {
            id: document.getElementById('productoId').value || null,
            codigoProducto: document.getElementById('codigoProducto').value.trim(),
            registroSanitario: document.getElementById('registroSanitario').value.trim(),
            cantidad: parseInt(document.getElementById('cantidad').value) || 1,
            nombreComercial: nombreComercial,
            principioActivo: document.getElementById('principioActivo').value.trim(),
            concentracion: document.getElementById('concentracion').value.trim(),
            formaFarmaceutica: document.getElementById('formaFarmaceutica').value.trim(),
            presentacion: document.getElementById('presentacion').value.trim(),
            laboratorio: document.getElementById('laboratorio').value.trim(),
            nombreFabricante: document.getElementById('nombreFabricante').value.trim(),
            nombreTitular: document.getElementById('nombreTitular').value.trim()
        };
        
        var btn = document.getElementById('btnGuardar');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Guardando...';
        
        fetch(contextPath + '/CatalogoController?action=guardar', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(datos)
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-save me-1"></i>Guardar';
            
            if (data.success) {
                bootstrap.Modal.getInstance(document.getElementById('modalProducto')).hide();
                mostrarToast(data.message || 'Producto guardado correctamente', 'success');
                cargarEstadisticas();
                
                // Refrescar búsqueda si hay término
                var termino = document.getElementById('buscarProducto').value.trim();
                if (termino.length >= 2) {
                    buscarProductos(termino);
                }
            } else {
                mostrarToast(data.error || 'Error al guardar', 'error');
            }
        })
        .catch(function(err) {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-save me-1"></i>Guardar';
            mostrarToast('Error de conexión', 'error');
        });
    }
    
    // =====================================================
    //          ELIMINAR
    // =====================================================
    function eliminarProducto(id, nombre) {
        document.getElementById('productoEliminarId').value = id;
        document.getElementById('productoEliminarNombre').textContent = nombre;
        new bootstrap.Modal(document.getElementById('modalEliminar')).show();
    }
    
    function confirmarEliminar() {
        var id = document.getElementById('productoEliminarId').value;
        
        fetch(contextPath + '/CatalogoController?action=eliminar&id=' + id, {
            method: 'POST'
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            bootstrap.Modal.getInstance(document.getElementById('modalEliminar')).hide();
            
            if (data.success) {
                mostrarToast('Producto eliminado correctamente', 'success');
                cargarEstadisticas();
                
                var termino = document.getElementById('buscarProducto').value.trim();
                if (termino.length >= 2) {
                    buscarProductos(termino);
                } else {
                    mostrarEstadoVacio();
                }
            } else {
                mostrarToast(data.error || 'Error al eliminar', 'error');
            }
        })
        .catch(function(err) {
            mostrarToast('Error de conexión', 'error');
        });
    }
    
    // =====================================================
    //          UTILIDADES
    // =====================================================
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
        } else {
            icon.className = 'bi bi-info-circle text-info me-2';
            titulo.textContent = 'Información';
        }
        
        new bootstrap.Toast(toast).show();
    }
</script>
