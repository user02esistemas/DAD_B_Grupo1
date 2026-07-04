<%-- 
    Document   : reportes_vencimiento
    Created on : 9 dic. 2025
    Author     : Sistema Botica
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "Reporte de Vencimientos - Econosalud Farmacia");
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>

<style>
    .badge-critico { background-color: #dc3545; }
    .badge-moderado { background-color: #ffc107; color: #000; }
    .badge-normal { background-color: #28a745; }
    .badge-vencido { background-color: #6c757d; }
    
    .stat-mini {
        text-align: center;
        padding: 15px;
        border-radius: 10px;
        color: white;
    }
    
    .stat-mini h3 {
        margin: 0;
        font-size: 2rem;
        font-weight: 700;
    }
    
    .stat-mini p {
        margin: 5px 0 0 0;
        font-size: 0.85rem;
        opacity: 0.9;
    }
    
    .filtros-card {
        background: #f8f9fa;
        border: 1px solid #e9ecef;
    }
    
    .table-reporte th {
        background-color: #035b77;
        color: white;
        font-weight: 500;
        position: sticky;
        top: 0;
    }
    
    .table-reporte tbody tr:hover {
        background-color: rgba(3, 91, 119, 0.05);
    }
    
    .btn-export {
        border-radius: 20px;
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
                    <i class="bi bi-calendar-x me-2"></i>Reporte de Medicamentos por Vencer
                </h1>
                
            </div>

            <!-- Cards de Criticidad -->
            <div class="row mb-4">
                <div class="col-md-3 mb-3">
                    <div class="stat-mini" style="background: linear-gradient(135deg, #6c757d 0%, #495057 100%);">
                        <h3 id="contVencidos">0</h3>
                        <p><i class="bi bi-x-circle me-1"></i>Vencidos</p>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="stat-mini" style="background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);">
                        <h3 id="contCritico">0</h3>
                        <p><i class="bi bi-exclamation-triangle me-1"></i>Crítico (&lt;10 días)</p>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="stat-mini" style="background: linear-gradient(135deg, #ffc107 0%, #e0a800 100%);">
                        <h3 id="contModerado">0</h3>
                        <p><i class="bi bi-exclamation-circle me-1"></i>Moderado (10-20 días)</p>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="stat-mini" style="background: linear-gradient(135deg, #28a745 0%, #1e7e34 100%);">
                        <h3 id="contNormal">0</h3>
                        <p><i class="bi bi-check-circle me-1"></i>Normal (21-30 días)</p>
                    </div>
                </div>
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
                            <label class="form-label fw-semibold">Filtro Rápido</label>
                            <select class="form-select" id="filtroRapido" onchange="aplicarFiltroRapido()">
                                <option value="">Seleccionar...</option>
                                <option value="7">Próximos 7 días</option>
                                <option value="15">Próximos 15 días</option>
                                <option value="30" selected>Próximos 30 días</option>
                                <option value="60">Próximos 60 días</option>
                                <option value="90">Próximos 90 días</option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <button type="button" class="btn btn-primary w-100" onclick="buscarReporte()">
                                <i class="bi bi-search me-1"></i>Buscar
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Tabla de Resultados -->
            <div class="card">
                <div class="card-header bg-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0"><i class="bi bi-table me-2"></i>Listado de Productos</h5>
                    <span class="badge bg-primary" id="totalRegistros">0 registros</span>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive" style="max-height: 500px; overflow-y: auto;">
                        <table class="table table-reporte table-hover mb-0">
                            <thead>
                                <tr>
                                    <th style="width: 5%;">#</th>
                                    <th style="width: 25%;">Producto</th>
                                    <th style="width: 15%;">Laboratorio</th>
                                    <th style="width: 10%;">Lote</th>
                                    <th style="width: 12%;">Vencimiento</th>
                                    <th style="width: 10%;">Días</th>
                                    <th style="width: 8%;">Stock</th>
                                    <th style="width: 10%;">P. Venta</th>
                                    <th style="width: 5%;">Estado</th>
                                </tr>
                            </thead>
                            <tbody id="tablaProductos">
                                <tr>
                                    <td colspan="9" class="text-center py-5">
                                        <div class="spinner-border text-primary" role="status">
                                            <span class="visually-hidden">Cargando...</span>
                                        </div>
                                        <p class="mt-2 text-muted">Cargando datos...</p>
                                    </td>
                                </tr>
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
    var productosData = [];
    
    document.addEventListener('DOMContentLoaded', function() {
        // Establecer fechas por defecto (hoy + 30 días)
        var hoy = new Date();
        var en30dias = new Date();
        en30dias.setDate(en30dias.getDate() + 30);
        
        document.getElementById('fechaDesde').value = formatearFechaInput(hoy);
        document.getElementById('fechaHasta').value = formatearFechaInput(en30dias);
        
        buscarReporte();
    });
    
    function formatearFechaInput(fecha) {
        var year = fecha.getFullYear();
        var month = String(fecha.getMonth() + 1).padStart(2, '0');
        var day = String(fecha.getDate()).padStart(2, '0');
        return year + '-' + month + '-' + day;
    }
    
    function aplicarFiltroRapido() {
        var dias = document.getElementById('filtroRapido').value;
        if (dias) {
            var hoy = new Date();
            var hasta = new Date();
            hasta.setDate(hasta.getDate() + parseInt(dias));
            
            document.getElementById('fechaDesde').value = formatearFechaInput(hoy);
            document.getElementById('fechaHasta').value = formatearFechaInput(hasta);
        }
    }
    
    function buscarReporte() {
        var fechaDesde = document.getElementById('fechaDesde').value;
        var fechaHasta = document.getElementById('fechaHasta').value;
        
        var url = '<%= request.getContextPath() %>/ReporteController?action=vencimientoRango';
        url += '&fechaDesde=' + fechaDesde;
        url += '&fechaHasta=' + fechaHasta;
        
        document.getElementById('tablaProductos').innerHTML = 
            '<tr><td colspan="9" class="text-center py-5">' +
            '<div class="spinner-border text-primary" role="status"></div>' +
            '<p class="mt-2 text-muted">Cargando datos...</p></td></tr>';
        
        fetch(url)
            .then(function(response) { return response.json(); })
            .then(function(data) {
                productosData = data.productos || [];
                actualizarCriticidad(data.criticidad);
                renderizarTabla(productosData);
            })
            .catch(function(error) {
                console.error('Error:', error);
                document.getElementById('tablaProductos').innerHTML = 
                    '<tr><td colspan="9" class="text-center py-5 text-danger">' +
                    '<i class="bi bi-exclamation-circle fs-1"></i>' +
                    '<p class="mt-2">Error al cargar los datos</p></td></tr>';
            });
    }
    
    function actualizarCriticidad(criticidad) {
        document.getElementById('contVencidos').textContent = criticidad.vencidos || 0;
        document.getElementById('contCritico').textContent = criticidad.critico || 0;
        document.getElementById('contModerado').textContent = criticidad.moderado || 0;
        document.getElementById('contNormal').textContent = criticidad.normal || 0;
    }
    
    function renderizarTabla(productos) {
        var tbody = document.getElementById('tablaProductos');
        document.getElementById('totalRegistros').textContent = productos.length + ' registros';
        
        if (productos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" class="text-center py-5">' +
                '<i class="bi bi-inbox fs-1 text-muted"></i>' +
                '<p class="mt-2 text-muted">No se encontraron productos en el rango seleccionado</p></td></tr>';
            return;
        }
        
        var html = '';
        for (var i = 0; i < productos.length; i++) {
            var p = productos[i];
            var fecha = new Date(p.fechaVencimiento);
            var fechaFormateada = fecha.toLocaleDateString('es-PE');
            
            var badgeClass = '';
            var estadoTexto = '';
            if (p.diasRestantes < 0) {
                badgeClass = 'badge-vencido';
                estadoTexto = 'Vencido';
            } else if (p.diasRestantes < 10) {
                badgeClass = 'badge-critico';
                estadoTexto = 'Crítico';
            } else if (p.diasRestantes <= 20) {
                badgeClass = 'badge-moderado';
                estadoTexto = 'Moderado';
            } else {
                badgeClass = 'badge-normal';
                estadoTexto = 'Normal';
            }
            
            html += '<tr>' +
                '<td>' + (i + 1) + '</td>' +
                '<td>' + p.nombre + '</td>' +
                '<td>' + (p.laboratorio || '-') + '</td>' +
                '<td><span class="badge bg-secondary">' + p.lote + '</span></td>' +
                '<td>' + fechaFormateada + '</td>' +
                '<td class="text-center"><strong>' + p.diasRestantes + '</strong></td>' +
                '<td class="text-center">' + p.stockActual + '</td>' +
                '<td class="text-end">S/. ' + parseFloat(p.precioVenta).toFixed(2) + '</td>' +
                '<td><span class="badge ' + badgeClass + '">' + estadoTexto + '</span></td>' +
                '</tr>';
        }
        tbody.innerHTML = html;
    }
    
    function exportarCSV() {
        if (productosData.length === 0) {
            alert('No hay datos para exportar');
            return;
        }
        
        var csv = 'N°,Producto,Laboratorio,Lote,Vencimiento,Días Restantes,Stock,Precio Venta,Estado\n';
        
        for (var i = 0; i < productosData.length; i++) {
            var p = productosData[i];
            var fecha = new Date(p.fechaVencimiento);
            var estado = p.diasRestantes < 0 ? 'Vencido' : 
                        p.diasRestantes < 10 ? 'Crítico' : 
                        p.diasRestantes <= 20 ? 'Moderado' : 'Normal';
            
            csv += (i + 1) + ',' +
                '"' + p.nombre + '",' +
                '"' + (p.laboratorio || '') + '",' +
                p.lote + ',' +
                fecha.toLocaleDateString('es-PE') + ',' +
                p.diasRestantes + ',' +
                p.stockActual + ',' +
                p.precioVenta + ',' +
                estado + '\n';
        }
        
        var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        var link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = 'reporte_vencimientos_' + new Date().toISOString().slice(0,10) + '.csv';
        link.click();
    }
    
    function exportarPDF() {
        alert('Funcionalidad de exportar PDF próximamente disponible.\nPuede usar Ctrl+P para imprimir esta página.');
        window.print();
    }
</script>
