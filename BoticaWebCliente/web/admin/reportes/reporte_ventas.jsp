<%-- 
    Document   : reporte_ventas
    Created on : 9 dic. 2025
    Author     : Sistema Botica
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "Reporte de Ventas - Econosalud Farmacia");
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>
<!-- Chart.js CDN -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<style>
    .filtros-card {
        background: #f8f9fa;
        border: 1px solid #e9ecef;
    }
    
    .stat-card-ventas {
        border-radius: 10px;
        padding: 20px;
        color: white;
        position: relative;
        overflow: hidden;
    }
    
    .stat-card-ventas::after {
        content: '';
        position: absolute;
        top: -50%;
        right: -50%;
        width: 100%;
        height: 200%;
        background: rgba(255,255,255,0.1);
        transform: rotate(30deg);
    }
    
    .stat-card-ventas .icon {
        position: absolute;
        right: 20px;
        top: 50%;
        transform: translateY(-50%);
        font-size: 3rem;
        opacity: 0.3;
    }
    
    .stat-card-ventas h6 {
        font-size: 0.85rem;
        opacity: 0.9;
        margin-bottom: 5px;
    }
    
    .stat-card-ventas h3 {
        font-size: 1.8rem;
        font-weight: 700;
        margin-bottom: 0;
    }
    
    .bg-gradient-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
    .bg-gradient-success { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
    .bg-gradient-info { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
    .bg-gradient-warning { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
    
    .chart-container-ventas {
        height: 300px;
        position: relative;
    }
    
    .table-ventas th {
        background-color: #035b77;
        color: white;
        font-weight: 500;
        position: sticky;
        top: 0;
    }
    
    .resumen-metodos-mini {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
    }
    
    .metodo-mini {
        padding: 10px 15px;
        border-radius: 8px;
        text-align: center;
        min-width: 120px;
        flex: 1;
    }
    
    .metodo-mini strong {
        display: block;
        font-size: 1.1rem;
    }
    
    .metodo-mini small {
        opacity: 0.8;
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
                    <i class="bi bi-graph-up me-2"></i>Reporte de Ventas
                </h1>
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
                                <option value="hoy">Hoy</option>
                                <option value="ayer">Ayer</option>
                                <option value="semana">Esta semana</option>
                                <option value="mes" selected>Este mes</option>
                                <option value="mesAnterior">Mes anterior</option>
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

            <!-- Cards de Resumen -->
            <div class="row mb-4">
                <div class="col-md-3 mb-3">
                    <div class="stat-card-ventas bg-gradient-primary">
                        <h6>TOTAL VENTAS</h6>
                        <h3 id="totalGeneral">S/. 0.00</h3>
                        <i class="bi bi-cash-stack icon"></i>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="stat-card-ventas bg-gradient-success">
                        <h6>N° TRANSACCIONES</h6>
                        <h3 id="cantidadVentas">0</h3>
                        <i class="bi bi-receipt icon"></i>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="stat-card-ventas bg-gradient-info">
                        <h6>PROMEDIO/VENTA</h6>
                        <h3 id="promedioVenta">S/. 0.00</h3>
                        <i class="bi bi-calculator icon"></i>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="stat-card-ventas bg-gradient-warning">
                        <h6>MEJOR DÍA</h6>
                        <h3 id="mejorDia">-</h3>
                        <i class="bi bi-trophy icon"></i>
                    </div>
                </div>
            </div>

            <!-- Gráfico y Resumen por Método -->
            <div class="row mb-4">
                <div class="col-lg-8 mb-3">
                    <div class="card h-100">
                        <div class="card-header bg-white">
                            <h5 class="mb-0"><i class="bi bi-bar-chart me-2"></i>Ventas por Día</h5>
                        </div>
                        <div class="card-body">
                            <div class="chart-container-ventas">
                                <canvas id="chartVentasDia"></canvas>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-4 mb-3">
                    <div class="card h-100">
                        <div class="card-header bg-white">
                            <h5 class="mb-0"><i class="bi bi-wallet2 me-2"></i>Por Método de Pago</h5>
                        </div>
                        <div class="card-body">
                            <div class="chart-container-ventas">
                                <canvas id="chartMetodos"></canvas>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Resumen por Métodos (Mini) -->
            <div class="card mb-4">
                <div class="card-body">
                    <div class="resumen-metodos-mini" id="resumenMetodos">
                        <div class="metodo-mini bg-success bg-opacity-10">
                            <strong id="metodoEfectivo">S/. 0.00</strong>
                            <small class="text-success">Efectivo</small>
                        </div>
                        <div class="metodo-mini" style="background-color: rgba(111,66,193,0.1);">
                            <strong id="metodoYapePlin">S/. 0.00</strong>
                            <small style="color: #6f42c1;">Yape/Plin</small>
                        </div>
                        <div class="metodo-mini bg-info bg-opacity-10">
                            <strong id="metodoTarjeta">S/. 0.00</strong>
                            <small class="text-info">Tarjeta</small>
                        </div>
                        <div class="metodo-mini bg-warning bg-opacity-10">
                            <strong id="metodoMixto">S/. 0.00</strong>
                            <small class="text-warning">Mixto</small>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Tabla de Ventas -->
            <div class="card">
                <div class="card-header bg-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0"><i class="bi bi-table me-2"></i>Detalle de Ventas</h5>
                    <span class="badge bg-primary" id="totalRegistros">0 registros</span>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
                        <table class="table table-ventas table-hover mb-0">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>N° Venta</th>
                                    <th>Fecha</th>
                                    <th>Hora</th>
                                    <th>Cajero</th>
                                    <th>Método Pago</th>
                                    <th class="text-end">Total</th>
                                </tr>
                            </thead>
                            <tbody id="tablaVentas">
                                <tr>
                                    <td colspan="7" class="text-center py-5">
                                        <div class="spinner-border text-primary" role="status"></div>
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
    var ventasData = [];
    var chartVentasDia = null;
    var chartMetodos = null;
    
    document.addEventListener('DOMContentLoaded', function() {
        aplicarFiltroRapido();
        buscarReporte();
    });
    
    function formatearFechaInput(fecha) {
        var year = fecha.getFullYear();
        var month = String(fecha.getMonth() + 1).padStart(2, '0');
        var day = String(fecha.getDate()).padStart(2, '0');
        return year + '-' + month + '-' + day;
    }
    
    function aplicarFiltroRapido() {
        var filtro = document.getElementById('filtroRapido').value;
        var hoy = new Date();
        var desde = new Date();
        var hasta = new Date();
        
        switch(filtro) {
            case 'hoy':
                desde = hoy;
                hasta = hoy;
                break;
            case 'ayer':
                desde.setDate(hoy.getDate() - 1);
                hasta.setDate(hoy.getDate() - 1);
                break;
            case 'semana':
                var diaSemana = hoy.getDay();
                desde.setDate(hoy.getDate() - diaSemana);
                hasta = hoy;
                break;
            case 'mes':
                desde = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
                hasta = hoy;
                break;
            case 'mesAnterior':
                desde = new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1);
                hasta = new Date(hoy.getFullYear(), hoy.getMonth(), 0);
                break;
            default:
                return;
        }
        
        document.getElementById('fechaDesde').value = formatearFechaInput(desde);
        document.getElementById('fechaHasta').value = formatearFechaInput(hasta);
    }
    
    function buscarReporte() {
        var fechaDesde = document.getElementById('fechaDesde').value;
        var fechaHasta = document.getElementById('fechaHasta').value;
        
        if (!fechaDesde || !fechaHasta) {
            alert('Seleccione el rango de fechas');
            return;
        }
        
        var url = '<%= request.getContextPath() %>/ReporteController?action=listarVentas';
        url += '&fechaDesde=' + fechaDesde;
        url += '&fechaHasta=' + fechaHasta;
        
        document.getElementById('tablaVentas').innerHTML = 
            '<tr><td colspan="7" class="text-center py-5">' +
            '<div class="spinner-border text-primary" role="status"></div>' +
            '<p class="mt-2 text-muted">Cargando datos...</p></td></tr>';
        
        fetch(url)
            .then(function(response) { return response.json(); })
            .then(function(data) {
                ventasData = data.ventas || [];
                actualizarResumen(data.resumen);
                renderizarTabla(ventasData);
                cargarGraficos(fechaDesde, fechaHasta, data.resumen);
            })
            .catch(function(error) {
                console.error('Error:', error);
                document.getElementById('tablaVentas').innerHTML = 
                    '<tr><td colspan="7" class="text-center py-5 text-danger">' +
                    '<i class="bi bi-exclamation-circle fs-1"></i>' +
                    '<p class="mt-2">Error al cargar los datos</p></td></tr>';
            });
    }
    
    function actualizarResumen(resumen) {
        document.getElementById('totalGeneral').textContent = 'S/. ' + parseFloat(resumen.totalGeneral || 0).toFixed(2);
        document.getElementById('cantidadVentas').textContent = resumen.cantidadVentas || 0;
        document.getElementById('promedioVenta').textContent = 'S/. ' + parseFloat(resumen.promedioVenta || 0).toFixed(2);
        
        // Métodos de pago
        document.getElementById('metodoEfectivo').textContent = 'S/. ' + parseFloat(resumen.totalEfectivo || 0).toFixed(2);
        document.getElementById('metodoYapePlin').textContent = 'S/. ' + parseFloat(resumen.totalYapePlin || 0).toFixed(2);
        document.getElementById('metodoTarjeta').textContent = 'S/. ' + parseFloat(resumen.totalTarjeta || 0).toFixed(2);
        document.getElementById('metodoMixto').textContent = 'S/. ' + parseFloat(resumen.totalMixto || 0).toFixed(2);
    }
    
    function cargarGraficos(fechaDesde, fechaHasta, resumen) {
        // Cargar ventas por día
        var url = '<%= request.getContextPath() %>/ReporteController?action=ventasPorDia';
        url += '&fechaDesde=' + fechaDesde;
        url += '&fechaHasta=' + fechaHasta;
        
        fetch(url)
            .then(function(response) { return response.json(); })
            .then(function(ventasPorDia) {
                renderizarGraficoVentas(ventasPorDia);
                
                // Encontrar mejor día
                if (ventasPorDia.length > 0) {
                    var mejorDia = ventasPorDia.reduce(function(max, dia) {
                        return parseFloat(dia.total) > parseFloat(max.total) ? dia : max;
                    });
                    var fecha = new Date(mejorDia.fecha);
                    document.getElementById('mejorDia').textContent = 'S/. ' + parseFloat(mejorDia.total).toFixed(0);
                }
            });
        
        // Gráfico de métodos
        renderizarGraficoMetodos(resumen);
    }
    
    function renderizarGraficoVentas(ventasPorDia) {
        var ctx = document.getElementById('chartVentasDia').getContext('2d');
        
        if (chartVentasDia) {
            chartVentasDia.destroy();
        }
        
        var labels = ventasPorDia.map(function(d) {
            var fecha = new Date(d.fecha);
            return fecha.toLocaleDateString('es-PE', {day: '2-digit', month: 'short'});
        });
        
        var datos = ventasPorDia.map(function(d) { return parseFloat(d.total); });
        
        chartVentasDia = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Ventas S/.',
                    data: datos,
                    backgroundColor: 'rgba(102, 126, 234, 0.7)',
                    borderColor: 'rgba(102, 126, 234, 1)',
                    borderWidth: 1,
                    borderRadius: 5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) { return 'S/. ' + value; }
                        }
                    }
                }
            }
        });
    }
    
    function renderizarGraficoMetodos(resumen) {
        var ctx = document.getElementById('chartMetodos').getContext('2d');
        
        if (chartMetodos) {
            chartMetodos.destroy();
        }
        
        var datos = [
            parseFloat(resumen.totalEfectivo || 0),
            parseFloat(resumen.totalYapePlin || 0),
            parseFloat(resumen.totalTarjeta || 0),
            parseFloat(resumen.totalMixto || 0)
        ];
        
        // Si no hay datos, mostrar mensaje
        if (datos.every(function(d) { return d === 0; })) {
            ctx.font = '14px Arial';
            ctx.fillStyle = '#6c757d';
            ctx.textAlign = 'center';
            ctx.fillText('Sin datos', ctx.canvas.width / 2, ctx.canvas.height / 2);
            return;
        }
        
        chartMetodos = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Efectivo', 'Yape/Plin', 'Tarjeta', 'Mixto'],
                datasets: [{
                    data: datos,
                    backgroundColor: ['#28a745', '#6f42c1', '#17a2b8', '#ffc107'],
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { boxWidth: 12, padding: 10 }
                    }
                }
            }
        });
    }
    
    function renderizarTabla(ventas) {
        var tbody = document.getElementById('tablaVentas');
        document.getElementById('totalRegistros').textContent = ventas.length + ' registros';
        
        if (ventas.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-5">' +
                '<i class="bi bi-inbox fs-1 text-muted"></i>' +
                '<p class="mt-2 text-muted">No se encontraron ventas en el período seleccionado</p></td></tr>';
            return;
        }
        
        var html = '';
        for (var i = 0; i < ventas.length; i++) {
            var v = ventas[i];
            var fecha = new Date(v.fecha);
            var fechaFormateada = fecha.toLocaleDateString('es-PE');
            var horaFormateada = fecha.toLocaleTimeString('es-PE', {hour: '2-digit', minute: '2-digit'});
            
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
                '<td>' + fechaFormateada + '</td>' +
                '<td>' + horaFormateada + '</td>' +
                '<td>' + v.cajero + '</td>' +
                '<td>' + metodoBadge + '</td>' +
                '<td class="text-end"><strong>S/. ' + parseFloat(v.total).toFixed(2) + '</strong></td>' +
                '</tr>';
        }
        tbody.innerHTML = html;
    }
    
    function exportarCSV() {
        if (ventasData.length === 0) {
            alert('No hay datos para exportar');
            return;
        }
        
        var csv = 'N°,Número Venta,Fecha,Hora,Cajero,Método Pago,Total\n';
        
        for (var i = 0; i < ventasData.length; i++) {
            var v = ventasData[i];
            var fecha = new Date(v.fecha);
            
            csv += (i + 1) + ',' +
                v.numero + ',' +
                fecha.toLocaleDateString('es-PE') + ',' +
                fecha.toLocaleTimeString('es-PE', {hour: '2-digit', minute: '2-digit'}) + ',' +
                '"' + v.cajero + '",' +
                v.metodoPago + ',' +
                v.total + '\n';
        }
        
        var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        var link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = 'reporte_ventas_' + new Date().toISOString().slice(0,10) + '.csv';
        link.click();
    }
    
    function exportarPDF() {
        alert('Funcionalidad de exportar PDF próximamente disponible.\nPuede usar Ctrl+P para imprimir esta página.');
        window.print();
    }
</script>
