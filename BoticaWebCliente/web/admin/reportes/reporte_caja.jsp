<%-- 
    Document   : reporte_caja
    Created on : 9 dic. 2025
    Author     : Sistema Botica
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "Reporte de Caja - Econosalud Farmacia");
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>

<style>
    .filtros-card {
        background: #f8f9fa;
        border: 1px solid #e9ecef;
    }
    
    .sesion-card {
        border-left: 4px solid #035b77;
        transition: all 0.3s ease;
        cursor: pointer;
    }
    
    .sesion-card:hover {
        transform: translateX(5px);
        box-shadow: 0 4px 15px rgba(0,0,0,0.1);
    }
    
    .sesion-card.selected {
        border-left-color: #28a745;
        background-color: #f8fff8;
    }
    
    .sesion-card .estado-badge {
        font-size: 0.75rem;
    }
    
    .detalle-section {
        background: linear-gradient(135deg, #035b77 0%, #024c64 100%);
        color: white;
        border-radius: 10px;
        padding: 20px;
    }
    
    .detalle-section h5 {
        border-bottom: 1px solid rgba(255,255,255,0.2);
        padding-bottom: 10px;
    }
    
    .detalle-item {
        display: flex;
        justify-content: space-between;
        padding: 8px 0;
        border-bottom: 1px solid rgba(255,255,255,0.1);
    }
    
    .detalle-item:last-child {
        border-bottom: none;
    }
    
    .detalle-item .label {
        opacity: 0.8;
    }
    
    .detalle-item .value {
        font-weight: 600;
    }
    
    .diferencia-positiva { color: #28a745 !important; }
    .diferencia-negativa { color: #dc3545 !important; }
    
    .resumen-metodos {
        background: white;
        border-radius: 10px;
        padding: 15px;
    }
    
    .metodo-item {
        display: flex;
        align-items: center;
        padding: 10px;
        border-radius: 5px;
        margin-bottom: 10px;
    }
    
    .metodo-item:last-child {
        margin-bottom: 0;
    }
    
    .metodo-item i {
        font-size: 1.5rem;
        margin-right: 15px;
        width: 40px;
        text-align: center;
    }
    
    .metodo-efectivo { background-color: #d4edda; }
    .metodo-efectivo i { color: #28a745; }
    
    .metodo-yape { background-color: #e2d5f1; }
    .metodo-yape i { color: #6f42c1; }
    
    .metodo-tarjeta { background-color: #cce5ff; }
    .metodo-tarjeta i { color: #0d6efd; }
    
    .metodo-mixto { background-color: #fff3cd; }
    .metodo-mixto i { color: #ffc107; }
    
    .table-ventas th {
        background-color: #035b77;
        color: white;
        font-weight: 500;
    }
</style>

<%@ include file="/WEB-INF/includes/navbar.jsp" %>

<div class="container-fluid">
    <div class="row">
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
        
        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
            <!-- Header -->
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h1 class="h2">
                    <i class="bi bi-cash-coin me-2"></i>Reporte de Caja
                </h1>
<!--                <div class="btn-toolbar mb-2 mb-md-0">
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="window.print()">
                        <i class="bi bi-printer me-1"></i>Imprimir
                    </button>
                </div>-->
            </div>

            <!-- Filtros -->
            <div class="card filtros-card mb-4">
                <div class="card-body">
                    <form id="formFiltros" class="row g-3 align-items-end">
                        <div class="col-md-3">
                            <label class="form-label fw-semibold">Fecha Desde</label>
                            <input type="date" class="form-control" id="fechaDesde">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label fw-semibold">Fecha Hasta</label>
                            <input type="date" class="form-control" id="fechaHasta">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label fw-semibold">Cajero</label>
                            <select class="form-select" id="usuarioId">
                                <option value="">Todos los cajeros</option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <button type="button" class="btn btn-primary w-100" onclick="buscarSesiones()">
                                <i class="bi bi-search me-1"></i>Buscar
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <div class="row">
                <!-- Lista de Sesiones -->
                <div class="col-lg-5 mb-4">
                    <div class="card h-100">
                        <div class="card-header bg-white">
                            <h5 class="mb-0"><i class="bi bi-list-ul me-2"></i>Sesiones de Caja</h5>
                        </div>
                        <div class="card-body p-2" style="max-height: 600px; overflow-y: auto;" id="listaSesiones">
                            <div class="text-center py-5">
                                <div class="spinner-border text-primary" role="status"></div>
                                <p class="mt-2 text-muted">Cargando sesiones...</p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Detalle de Sesión -->
                <div class="col-lg-7 mb-4">
                    <div class="card h-100">
                        <div class="card-header bg-white">
                            <h5 class="mb-0"><i class="bi bi-receipt me-2"></i>Detalle de Sesión</h5>
                        </div>
                        <div class="card-body" id="detalleSesion">
                            <div class="text-center py-5 text-muted">
                                <i class="bi bi-hand-index fs-1"></i>
                                <p class="mt-3">Seleccione una sesión de caja para ver el detalle</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Tabla de Ventas del Turno -->
            <div class="card" id="cardVentas" style="display: none;">
                <div class="card-header bg-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0"><i class="bi bi-cart-check me-2"></i>Ventas del Turno</h5>
                    <span class="badge bg-primary" id="totalVentas">0 ventas</span>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-ventas table-hover mb-0">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>N° Venta</th>
                                    <th>Hora</th>
                                    <th>Método Pago</th>
                                    <th class="text-end">Efectivo</th>
                                    <th class="text-end">Virtual</th>
                                    <th class="text-end">Vuelto</th>
                                    <th class="text-end">Total</th>
                                </tr>
                            </thead>
                            <tbody id="tablaVentas">
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

        </main>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>

<script>
    var sesionesData = [];
    var sesionSeleccionada = null;
    
    document.addEventListener('DOMContentLoaded', function() {
        // Establecer fechas por defecto (últimos 7 días)
        var hoy = new Date();
        var hace7dias = new Date();
        hace7dias.setDate(hace7dias.getDate() - 7);
        
        document.getElementById('fechaDesde').value = formatearFechaInput(hace7dias);
        document.getElementById('fechaHasta').value = formatearFechaInput(hoy);
        
        cargarUsuarios();
        buscarSesiones();
    });
    
    function formatearFechaInput(fecha) {
        var year = fecha.getFullYear();
        var month = String(fecha.getMonth() + 1).padStart(2, '0');
        var day = String(fecha.getDate()).padStart(2, '0');
        return year + '-' + month + '-' + day;
    }
    
    function cargarUsuarios() {
        fetch('<%= request.getContextPath() %>/ReporteController?action=usuarios')
            .then(function(response) { return response.json(); })
            .then(function(usuarios) {
                var select = document.getElementById('usuarioId');
                for (var i = 0; i < usuarios.length; i++) {
                    var option = document.createElement('option');
                    option.value = usuarios[i].id;
                    option.textContent = usuarios[i].nombre;
                    select.appendChild(option);
                }
            });
    }
    
    function buscarSesiones() {
        var fechaDesde = document.getElementById('fechaDesde').value;
        var fechaHasta = document.getElementById('fechaHasta').value;
        var usuarioId = document.getElementById('usuarioId').value;
        
        var url = '<%= request.getContextPath() %>/ReporteController?action=sesionesCaja';
        url += '&fechaDesde=' + fechaDesde;
        url += '&fechaHasta=' + fechaHasta;
        if (usuarioId) url += '&usuarioId=' + usuarioId;
        
        document.getElementById('listaSesiones').innerHTML = 
            '<div class="text-center py-5">' +
            '<div class="spinner-border text-primary" role="status"></div>' +
            '<p class="mt-2 text-muted">Cargando sesiones...</p></div>';
        
        fetch(url)
            .then(function(response) { return response.json(); })
            .then(function(data) {
                sesionesData = data;
                renderizarSesiones(data);
            })
            .catch(function(error) {
                console.error('Error:', error);
                document.getElementById('listaSesiones').innerHTML = 
                    '<div class="text-center py-5 text-danger">' +
                    '<i class="bi bi-exclamation-circle fs-1"></i>' +
                    '<p class="mt-2">Error al cargar las sesiones</p></div>';
            });
    }
    
    function renderizarSesiones(sesiones) {
        var container = document.getElementById('listaSesiones');
        
        if (sesiones.length === 0) {
            container.innerHTML = '<div class="text-center py-5 text-muted">' +
                '<i class="bi bi-inbox fs-1"></i>' +
                '<p class="mt-2">No se encontraron sesiones de caja</p></div>';
            return;
        }
        
        var html = '';
        for (var i = 0; i < sesiones.length; i++) {
            var s = sesiones[i];
            var fechaApertura = new Date(s.fechaApertura);
            var estadoBadge = s.estado === 'ABIERTA' 
                ? '<span class="badge bg-success estado-badge">Abierta</span>'
                : '<span class="badge bg-secondary estado-badge">Cerrada</span>';
            
            html += '<div class="card sesion-card mb-2" onclick="seleccionarSesion(' + s.id + ', this)">' +
                '<div class="card-body py-2 px-3">' +
                '<div class="d-flex justify-content-between align-items-center">' +
                '<div>' +
                '<strong>' + s.usuarioNombre + '</strong> ' + estadoBadge +
                '<br><small class="text-muted">' +
                '<i class="bi bi-calendar3 me-1"></i>' + fechaApertura.toLocaleDateString('es-PE') + ' ' +
                '<i class="bi bi-clock ms-2 me-1"></i>' + fechaApertura.toLocaleTimeString('es-PE', {hour: '2-digit', minute: '2-digit'}) +
                '</small>' +
                '</div>' +
                '<div class="text-end">' +
                '<strong class="text-primary">S/. ' + parseFloat(s.totalTransacciones || 0).toFixed(2) + '</strong>' +
                '</div></div></div></div>';
        }
        container.innerHTML = html;
    }
    
    function seleccionarSesion(sesionId, elemento) {
        // Quitar selección anterior
        var cards = document.querySelectorAll('.sesion-card');
        cards.forEach(function(card) { card.classList.remove('selected'); });
        
        // Marcar seleccionado
        elemento.classList.add('selected');
        sesionSeleccionada = sesionId;
        
        // Cargar detalle
        cargarDetalleSesion(sesionId);
    }
    
    function cargarDetalleSesion(sesionId) {
        document.getElementById('detalleSesion').innerHTML = 
            '<div class="text-center py-5">' +
            '<div class="spinner-border text-primary" role="status"></div></div>';
        
        fetch('<%= request.getContextPath() %>/ReporteController?action=detalleSesion&sesionId=' + sesionId)
            .then(function(response) { return response.json(); })
            .then(function(data) {
                renderizarDetalle(data);
                renderizarVentas(data.ventas || []);
            })
            .catch(function(error) {
                console.error('Error:', error);
                document.getElementById('detalleSesion').innerHTML = 
                    '<div class="text-center py-5 text-danger">' +
                    '<i class="bi bi-exclamation-circle fs-1"></i>' +
                    '<p class="mt-2">Error al cargar el detalle</p></div>';
            });
    }
    
    function renderizarDetalle(data) {
        var fechaApertura = data.fechaApertura ? new Date(data.fechaApertura) : null;
        var fechaCierre = data.fechaCierre ? new Date(data.fechaCierre) : null;
        
        var diferencia = data.diferenciaCaja || 0;
        var diferenciaClass = diferencia >= 0 ? 'diferencia-positiva' : 'diferencia-negativa';
        var diferenciaTexto = diferencia >= 0 ? '+S/. ' + diferencia.toFixed(2) : '-S/. ' + Math.abs(diferencia).toFixed(2);
        
        var duracion = '';
        if (fechaApertura && fechaCierre) {
            var diff = fechaCierre - fechaApertura;
            var horas = Math.floor(diff / (1000 * 60 * 60));
            var minutos = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
            duracion = horas + 'h ' + minutos + 'm';
        }
        
        var html = '<div class="detalle-section mb-4">' +
            '<h5><i class="bi bi-info-circle me-2"></i>Información del Turno</h5>' +
            '<div class="row">' +
            '<div class="col-md-6">' +
            '<div class="detalle-item"><span class="label">Cajero:</span><span class="value">' + data.usuarioNombre + '</span></div>' +
            '<div class="detalle-item"><span class="label">Caja:</span><span class="value">' + data.cajaNombre + '</span></div>' +
            '<div class="detalle-item"><span class="label">Apertura:</span><span class="value">' + 
                (fechaApertura ? fechaApertura.toLocaleString('es-PE') : '-') + '</span></div>' +
            '<div class="detalle-item"><span class="label">Cierre:</span><span class="value">' + 
                (fechaCierre ? fechaCierre.toLocaleString('es-PE') : 'En curso') + '</span></div>' +
            (duracion ? '<div class="detalle-item"><span class="label">Duración:</span><span class="value">' + duracion + '</span></div>' : '') +
            '</div>' +
            '<div class="col-md-6">' +
            '<div class="detalle-item"><span class="label">Monto Inicial:</span><span class="value">S/. ' + parseFloat(data.montoInicial || 0).toFixed(2) + '</span></div>' +
            '<div class="detalle-item"><span class="label">Efectivo Esperado:</span><span class="value">S/. ' + parseFloat(data.efectivoEsperadoCalculado || data.efectivoEsperado || 0).toFixed(2) + '</span></div>' +
            '<div class="detalle-item"><span class="label">Efectivo Real:</span><span class="value">S/. ' + parseFloat(data.montoFinal || 0).toFixed(2) + '</span></div>' +
            '<div class="detalle-item"><span class="label">Diferencia:</span><span class="value ' + diferenciaClass + '">' + diferenciaTexto + '</span></div>' +
            '</div></div></div>';
        
        // Resumen por métodos de pago
        html += '<div class="resumen-metodos">' +
            '<h6 class="mb-3"><i class="bi bi-wallet2 me-2"></i>Resumen por Método de Pago</h6>' +
            '<div class="row">' +
            '<div class="col-md-6">' +
            '<div class="metodo-item metodo-efectivo">' +
            '<i class="bi bi-cash-stack"></i>' +
            '<div><strong>Efectivo</strong><br>S/. ' + parseFloat(data.totalEfectivo || 0).toFixed(2) + '</div></div>' +
            '<div class="metodo-item metodo-yape">' +
            '<i class="bi bi-phone"></i>' +
            '<div><strong>Yape/Plin</strong><br>S/. ' + parseFloat(data.totalYapePlin || 0).toFixed(2) + '</div></div>' +
            '</div>' +
            '<div class="col-md-6">' +
            '<div class="metodo-item metodo-tarjeta">' +
            '<i class="bi bi-credit-card"></i>' +
            '<div><strong>Tarjeta</strong><br>S/. ' + parseFloat(data.totalTarjeta || 0).toFixed(2) + '</div></div>' +
            '<div class="metodo-item metodo-mixto">' +
            '<i class="bi bi-shuffle"></i>' +
            '<div><strong>Mixto</strong><br>S/. ' + parseFloat(data.totalMixto || 0).toFixed(2) + '</div></div>' +
            '</div></div>' +
            '<hr>' +
            '<div class="d-flex justify-content-between align-items-center">' +
            '<strong>TOTAL GENERAL:</strong>' +
            '<strong class="fs-4 text-primary">S/. ' + parseFloat(data.totalGeneral || 0).toFixed(2) + '</strong>' +
            '</div></div>';
        
        document.getElementById('detalleSesion').innerHTML = html;
    }
    
    function renderizarVentas(ventas) {
        var cardVentas = document.getElementById('cardVentas');
        var tbody = document.getElementById('tablaVentas');
        
        document.getElementById('totalVentas').textContent = ventas.length + ' ventas';
        
        if (ventas.length === 0) {
            cardVentas.style.display = 'none';
            return;
        }
        
        cardVentas.style.display = 'block';
        
        var html = '';
        for (var i = 0; i < ventas.length; i++) {
            var v = ventas[i];
            var fecha = new Date(v.fecha);
            
            var metodoBadge = '';
            switch(v.metodoPago) {
                case 'EFECTIVO':
                    metodoBadge = '<span class="badge bg-success">Efectivo</span>';
                    break;
                case 'YAPE_PLIN':
                    metodoBadge = '<span class="badge" style="background:#6f42c1;">Yape/Plin</span>';
                    break;
                case 'TARJETA':
                    metodoBadge = '<span class="badge bg-info">Tarjeta</span>';
                    break;
                case 'MIXTO':
                    metodoBadge = '<span class="badge bg-warning text-dark">Mixto</span>';
                    break;
                default:
                    metodoBadge = '<span class="badge bg-secondary">' + v.metodoPago + '</span>';
            }
            
            html += '<tr>' +
                '<td>' + (i + 1) + '</td>' +
                '<td><strong>' + v.numero + '</strong></td>' +
                '<td>' + fecha.toLocaleTimeString('es-PE', {hour: '2-digit', minute: '2-digit'}) + '</td>' +
                '<td>' + metodoBadge + '</td>' +
                '<td class="text-end">S/. ' + parseFloat(v.montoEfectivo || 0).toFixed(2) + '</td>' +
                '<td class="text-end">S/. ' + parseFloat(v.montoVirtual || 0).toFixed(2) + '</td>' +
                '<td class="text-end">S/. ' + parseFloat(v.vuelto || 0).toFixed(2) + '</td>' +
                '<td class="text-end"><strong>S/. ' + parseFloat(v.total).toFixed(2) + '</strong></td>' +
                '</tr>';
        }
        tbody.innerHTML = html;
    }
</script>
