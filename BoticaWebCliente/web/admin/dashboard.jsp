<%-- 
    Document   : dashboard
    Created on : 2 dic. 2025, 15:24:55
    Author     : yerri
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.setAttribute("pageTitle", "Dashboard - Econosalud Farmacia");
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>
<!-- Chart.js CDN -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<style>
    /* Estilos específicos del Dashboard */
    .stat-card {
        border-radius: 10px;
        transition: all 0.3s ease;
    }

    .stat-card:hover {
        transform: translateY(-5px);
    }

    .stat-card .icon {
        font-size: 2.5rem;
        opacity: 0.2;
    }

    .stat-card.blue {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
    }

    .stat-card.green {
        background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
        color: white;
    }

    .stat-card.orange {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: white;
    }

    .stat-card.purple {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        color: white;
    }

    .stat-card h6 {
        opacity: 0.9;
        font-size: 0.85rem;
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }

    .stat-card h3 {
        font-weight: 700;
        margin-bottom: 0;
    }

    .alert-badge {
        display: inline-flex;
        align-items: center;
        padding: 0.35rem 0.75rem;
        border-radius: 50px;
        font-size: 0.8rem;
        font-weight: 600;
    }

    .alert-badge.warning {
        background-color: #fff3cd;
        color: #856404;
    }

    .alert-badge.danger {
        background-color: #f8d7da;
        color: #721c24;
    }

    .alert-badge i {
        margin-right: 5px;
    }

    .chart-container {
        position: relative;
        height: 300px;
    }

    .table-dashboard {
        font-size: 0.875rem;
    }

    .table-dashboard th {
        font-weight: 600;
        background-color: #f8f9fa;
        border-bottom: 2px solid #dee2e6;
    }

    .table-dashboard td {
        vertical-align: middle;
    }

    .producto-rank {
        width: 30px;
        height: 30px;
        border-radius: 50%;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-weight: bold;
        font-size: 0.8rem;
    }

    .rank-1 {
        background-color: #ffd700;
        color: #000;
    }
    .rank-2 {
        background-color: #c0c0c0;
        color: #000;
    }
    .rank-3 {
        background-color: #cd7f32;
        color: #fff;
    }
    .rank-default {
        background-color: #e9ecef;
        color: #495057;
    }

    .loading-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(255,255,255,0.8);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10;
    }

    .card-header-custom {
        background-color: #fff;
        border-bottom: 2px solid #f0f0f0;
        padding: 1rem 1.25rem;
    }

    .card-header-custom h5 {
        margin: 0;
        font-weight: 600;
        color: #333;
    }

    .ultimas-ventas-item {
        padding: 0.75rem 0;
        border-bottom: 1px solid #f0f0f0;
    }

    .ultimas-ventas-item:last-child {
        border-bottom: none;
    }

    .metodo-pago-badge {
        font-size: 0.7rem;
        padding: 0.2rem 0.5rem;
        border-radius: 3px;
    }

    .fecha-hora {
        font-size: 0.75rem;
        color: #6c757d;
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
                    <i class="bi bi-speedometer2 me-2"></i>Dashboard
                </h1>
                <div class="btn-toolbar mb-2 mb-md-0">
                    <span class="text-muted me-3" id="fechaActual"></span>
                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="cargarDatos()">
                        <i class="bi bi-arrow-clockwise me-1"></i>Actualizar
                    </button>
                </div>
            </div>

            <!-- Alertas -->
            <div class="row mb-3" id="alertasContainer">
                <!-- Se llena dinámicamente -->
            </div>

            <!-- Tarjetas de estadísticas -->
            <div class="row mb-4">
                <!-- Ventas del día -->
                <div class="col-xl-3 col-md-6 mb-3">
                    <div class="card stat-card blue h-100">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h6 class="mb-2">Ventas Hoy</h6>
                                    <h3 id="ventasHoy">S/. 0.00</h3>
                                    <small id="cantidadVentasHoy">0 ventas</small>
                                </div>
                                <i class="bi bi-cash-stack icon"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Ventas del mes -->
                <div class="col-xl-3 col-md-6 mb-3">
                    <div class="card stat-card green h-100">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h6 class="mb-2">Ventas del Mes</h6>
                                    <h3 id="ventasMes">S/. 0.00</h3>
                                    <small>Acumulado mensual</small>
                                </div>
                                <i class="bi bi-graph-up-arrow icon"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Stock bajo -->
                <div class="col-xl-3 col-md-6 mb-3">
                    <div class="card stat-card orange h-100">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h6 class="mb-2">Stock Bajo</h6>
                                    <h3 id="stockBajo">0</h3>
                                    <small id="agotados">0 agotados</small>
                                </div>
                                <i class="bi bi-exclamation-triangle icon"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Por vencer -->
                <div class="col-xl-3 col-md-6 mb-3">
                    <div class="card stat-card purple h-100">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h6 class="mb-2">Por Vencer</h6>
                                    <h3 id="porVencer">0</h3>
                                    <small>Próximos 30 días</small>
                                </div>
                                <i class="bi bi-calendar-x icon"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Gráficos -->
            <div class="row mb-4">
                <!-- Top 5 productos más vendidos (Barras) -->
                <div class="col-lg-8 mb-3">
                    <div class="card h-100">
                        <div class="card-header card-header-custom d-flex justify-content-between align-items-center">
                            <h5><i class="bi bi-bar-chart me-2"></i>Top 5 Productos Más Vendidos</h5>
                            <span class="badge bg-primary">General</span>
                        </div>
                        <div class="card-body">
                            <div class="chart-container">
                                <canvas id="chartTopProductos"></canvas>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Productos más vendidos del mes (Donut) -->
                <div class="col-lg-4 mb-3">
                    <div class="card h-100">
                        <div class="card-header card-header-custom d-flex justify-content-between align-items-center">
                            <h5><i class="bi bi-pie-chart me-2"></i>Ventas del Mes</h5>
                            <span class="badge bg-success">Este mes</span>
                        </div>
                        <div class="card-body">
                            <div class="chart-container">
                                <canvas id="chartProductosMes"></canvas>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Tablas y listas -->
            <div class="row">
                <!-- Tabla Top productos -->
                <div class="col-lg-6 mb-3">
                    <div class="card h-100">
                        <div class="card-header card-header-custom">
                            <h5><i class="bi bi-trophy me-2"></i>Ranking de Productos</h5>
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-dashboard table-hover mb-0">
                                    <thead>
                                        <tr>
                                            <th style="width: 50px;">#</th>
                                            <th>Producto</th>
                                            <th class="text-center">Cantidad</th>
                                            <th class="text-end">Total</th>
                                        </tr>
                                    </thead>
                                    <tbody id="tablaTopProductos">
                                        <tr>
                                            <td colspan="4" class="text-center py-4">
                                                <div class="spinner-border spinner-border-sm text-primary" role="status">
                                                    <span class="visually-hidden">Cargando...</span>
                                                </div>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Últimas ventas -->
                <div class="col-lg-6 mb-3">
                    <div class="card h-100">
                        <div class="card-header card-header-custom d-flex justify-content-between align-items-center">
                            <h5><i class="bi bi-clock-history me-2"></i>Últimas Ventas</h5>
                            <a href="<%= request.getContextPath()%>/farmaceutico/ventas/listar.jsp" class="btn btn-sm btn-outline-primary">
                                Ver todas
                            </a>
                        </div>
                        <div class="card-body" id="ultimasVentasContainer">
                            <div class="text-center py-4">
                                <div class="spinner-border spinner-border-sm text-primary" role="status">
                                    <span class="visually-hidden">Cargando...</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>

<script>
    // Variables globales para los gráficos
    var chartTopProductos = null;
    var chartProductosMes = null;

    // Colores para los gráficos
    var coloresBarras = [
        'rgba(102, 126, 234, 0.8)',
        'rgba(118, 75, 162, 0.8)',
        'rgba(17, 153, 142, 0.8)',
        'rgba(56, 239, 125, 0.8)',
        'rgba(79, 172, 254, 0.8)'
    ];

    var coloresDonut = [
        '#667eea',
        '#764ba2',
        '#11998e',
        '#38ef7d',
        '#4facfe'
    ];

    // Inicialización
    document.addEventListener('DOMContentLoaded', function () {
        mostrarFechaActual();
        cargarDatos();
    });

    function mostrarFechaActual() {
        var opciones = {weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'};
        var fecha = new Date().toLocaleDateString('es-PE', opciones);
        document.getElementById('fechaActual').textContent = fecha.charAt(0).toUpperCase() + fecha.slice(1);
    }

    function cargarDatos() {
        fetch('<%= request.getContextPath()%>/DashboardController?action=resumen')
                .then(function (response) {
                    return response.json();
                })
                .then(function (data) {
                    actualizarTarjetas(data);
                    actualizarAlertas(data);
                    actualizarGraficos(data);
                    actualizarTablas(data);
                    actualizarUltimasVentas(data.ultimasVentas);
                })
                .catch(function (error) {
                    console.error('Error al cargar datos:', error);
                    mostrarError('Error al cargar los datos del dashboard');
                });
    }

    function actualizarTarjetas(data) {
        // Ventas del día
        document.getElementById('ventasHoy').textContent = formatearMoneda(data.ventasHoy || 0);
        document.getElementById('cantidadVentasHoy').textContent = (data.cantidadVentasHoy || 0) + ' ventas';

        // Ventas del mes
        document.getElementById('ventasMes').textContent = formatearMoneda(data.ventasMes || 0);

        // Stock bajo
        document.getElementById('stockBajo').textContent = data.stockBajo || 0;
        document.getElementById('agotados').textContent = (data.agotados || 0) + ' agotados';

        // Por vencer
        document.getElementById('porVencer').textContent = data.porVencer || 0;
    }

    function actualizarAlertas(data) {
        var container = document.getElementById('alertasContainer');
        var html = '';

        if (data.agotados > 0) {
            html += '<div class="col-auto">' +
                    '<span class="alert-badge danger">' +
                    '<i class="bi bi-x-circle"></i>' +
                    data.agotados + ' producto(s) agotado(s)' +
                    '</span></div>';
        }

        if (data.stockBajo > 0) {
            html += '<div class="col-auto">' +
                    '<span class="alert-badge warning">' +
                    '<i class="bi bi-exclamation-triangle"></i>' +
                    data.stockBajo + ' producto(s) con stock bajo' +
                    '</span></div>';
        }

        if (data.porVencer > 0) {
            html += '<div class="col-auto">' +
                    '<span class="alert-badge warning">' +
                    '<i class="bi bi-calendar-x"></i>' +
                    data.porVencer + ' producto(s) por vencer' +
                    '</span></div>';
        }

        if (data.vencidos > 0) {
            html += '<div class="col-auto">' +
                    '<span class="alert-badge danger">' +
                    '<i class="bi bi-calendar-x-fill"></i>' +
                    data.vencidos + ' producto(s) vencido(s)' +
                    '</span></div>';
        }

        container.innerHTML = html;
    }

    function actualizarGraficos(data) {
        // Gráfico de barras - Top productos
        var topProductos = data.topProductos || [];
        var labels = topProductos.map(function (p) {
            return p.nombre;
        });
        var cantidades = topProductos.map(function (p) {
            return p.cantidad;
        });

        if (chartTopProductos) {
            chartTopProductos.destroy();
        }

        var ctxBarras = document.getElementById('chartTopProductos').getContext('2d');

        if (topProductos.length === 0) {
            ctxBarras.font = '14px Arial';
            ctxBarras.fillStyle = '#6c757d';
            ctxBarras.textAlign = 'center';
            ctxBarras.fillText('Sin datos de ventas', ctxBarras.canvas.width / 2, ctxBarras.canvas.height / 2);
        } else {
            chartTopProductos = new Chart(ctxBarras, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                            label: 'Unidades vendidas',
                            data: cantidades,
                            backgroundColor: coloresBarras,
                            borderColor: coloresBarras.map(function (c) {
                                return c.replace('0.8', '1');
                            }),
                            borderWidth: 1,
                            borderRadius: 5
                        }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                stepSize: 1
                            }
                        },
                        x: {
                            ticks: {
                                maxRotation: 45,
                                minRotation: 45
                            }
                        }
                    }
                }
            });
        }

        // Gráfico donut - Productos del mes
        var topMes = data.topProductosMes || [];
        var labelsMes = topMes.map(function (p) {
            return p.nombre;
        });
        var cantidadesMes = topMes.map(function (p) {
            return p.cantidad;
        });

        if (chartProductosMes) {
            chartProductosMes.destroy();
        }

        var ctxDonut = document.getElementById('chartProductosMes').getContext('2d');

        if (topMes.length === 0) {
            ctxDonut.font = '14px Arial';
            ctxDonut.fillStyle = '#6c757d';
            ctxDonut.textAlign = 'center';
            ctxDonut.fillText('Sin ventas este mes', ctxDonut.canvas.width / 2, ctxDonut.canvas.height / 2);
        } else {
            chartProductosMes = new Chart(ctxDonut, {
                type: 'doughnut',
                data: {
                    labels: labelsMes,
                    datasets: [{
                            data: cantidadesMes,
                            backgroundColor: coloresDonut,
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
                            labels: {
                                boxWidth: 12,
                                padding: 10,
                                font: {
                                    size: 11
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    function actualizarTablas(data) {
        // Tabla top productos
        var topProductos = data.topProductos || [];
        var tbodyTop = document.getElementById('tablaTopProductos');

        if (topProductos.length === 0) {
            tbodyTop.innerHTML = '<tr>' +
                    '<td colspan="4" class="text-center py-4 text-muted">' +
                    '<i class="bi bi-inbox fs-3 d-block mb-2"></i>' +
                    'No hay ventas registradas' +
                    '</td></tr>';
        } else {
            var htmlTop = '';
            for (var i = 0; i < topProductos.length; i++) {
                var p = topProductos[i];
                var rankClass = i < 3 ? 'rank-' + (i + 1) : 'rank-default';
                htmlTop += '<tr>' +
                        '<td><span class="producto-rank ' + rankClass + '">' + (i + 1) + '</span></td>' +
                        '<td>' + p.nombre + '</td>' +
                        '<td class="text-center"><strong>' + p.cantidad + '</strong></td>' +
                        '<td class="text-end">' + formatearMoneda(p.total) + '</td>' +
                        '</tr>';
            }
            tbodyTop.innerHTML = htmlTop;
        }

        // Tabla stock bajo
        actualizarTablaStockBajo();

        // Tabla por vencer
        actualizarTablaPorVencer();
    }

    function actualizarTablaStockBajo() {
        fetch('<%= request.getContextPath()%>/DashboardController?action=productosStockBajo&limite=5')
                .then(function (response) {
                    return response.json();
                })
                .then(function (productos) {
                    var tbody = document.getElementById('tablaStockBajo');

                    if (productos.length === 0) {
                        tbody.innerHTML = '<tr>' +
                                '<td colspan="4" class="text-center py-4 text-success">' +
                                '<i class="bi bi-check-circle fs-3 d-block mb-2"></i>' +
                                'Todos los productos tienen stock suficiente' +
                                '</td></tr>';
                    } else {
                        var html = '';
                        for (var i = 0; i < productos.length; i++) {
                            var p = productos[i];
                            html += '<tr>' +
                                    '<td>' + p.nombre + '</td>' +
                                    '<td><span class="badge bg-secondary">' + p.lote + '</span></td>' +
                                    '<td class="text-center"><span class="badge bg-danger">' + p.stockActual + '</span></td>' +
                                    '<td class="text-center">' + p.stockMinimo + '</td>' +
                                    '</tr>';
                        }
                        tbody.innerHTML = html;
                    }
                });
    }

    function actualizarTablaPorVencer() {
        fetch('<%= request.getContextPath()%>/DashboardController?action=productosPorVencer&limite=5')
                .then(function (response) {
                    return response.json();
                })
                .then(function (productos) {
                    var tbody = document.getElementById('tablaPorVencer');

                    if (productos.length === 0) {
                        tbody.innerHTML = '<tr>' +
                                '<td colspan="4" class="text-center py-4 text-success">' +
                                '<i class="bi bi-check-circle fs-3 d-block mb-2"></i>' +
                                'Sin productos próximos a vencer' +
                                '</td></tr>';
                    } else {
                        var html = '';
                        for (var i = 0; i < productos.length; i++) {
                            var p = productos[i];
                            var fecha = new Date(p.fechaVencimiento);
                            var fechaFormateada = fecha.toLocaleDateString('es-PE');
                            var badgeClass = p.diasRestantes <= 7 ? 'bg-danger' :
                                    p.diasRestantes <= 15 ? 'bg-warning' : 'bg-info';

                            html += '<tr>' +
                                    '<td>' + p.nombre + '</td>' +
                                    '<td><span class="badge bg-secondary">' + p.lote + '</span></td>' +
                                    '<td class="text-center">' + fechaFormateada + '</td>' +
                                    '<td class="text-center"><span class="badge ' + badgeClass + '">' + p.diasRestantes + 'd</span></td>' +
                                    '</tr>';
                        }
                        tbody.innerHTML = html;
                    }
                });
    }

    function actualizarUltimasVentas(ventas) {
        var container = document.getElementById('ultimasVentasContainer');

        if (!ventas || ventas.length === 0) {
            container.innerHTML = '<div class="text-center py-4 text-muted">' +
                    '<i class="bi bi-inbox fs-3 d-block mb-2"></i>' +
                    'No hay ventas registradas' +
                    '</div>';
            return;
        }

        var html = '';
        for (var i = 0; i < ventas.length; i++) {
            var v = ventas[i];
            var fecha = new Date(v.fecha);
            var fechaFormateada = fecha.toLocaleDateString('es-PE');
            var horaFormateada = fecha.toLocaleTimeString('es-PE', {hour: '2-digit', minute: '2-digit'});

            var metodoBadge = '';
            switch (v.metodoPago) {
                case 'EFECTIVO':
                    metodoBadge = '<span class="metodo-pago-badge bg-success text-white">Efectivo</span>';
                    break;
                case 'YAPE_PLIN':
                    metodoBadge = '<span class="metodo-pago-badge text-white" style="background:#6f42c1;">Yape/Plin</span>';
                    break;
                case 'TARJETA':
                    metodoBadge = '<span class="metodo-pago-badge bg-info text-white">Tarjeta</span>';
                    break;
                case 'MIXTO':
                    metodoBadge = '<span class="metodo-pago-badge bg-warning text-dark">Mixto</span>';
                    break;
                default:
                    metodoBadge = '<span class="metodo-pago-badge bg-secondary text-white">' + v.metodoPago + '</span>';
            }

            html += '<div class="ultimas-ventas-item d-flex justify-content-between align-items-center">' +
                    '<div>' +
                    '<div class="fw-semibold">' + v.numero + '</div>' +
                    '<div class="fecha-hora">' +
                    '<i class="bi bi-calendar3 me-1"></i>' + fechaFormateada + ' ' +
                    '<i class="bi bi-clock ms-2 me-1"></i>' + horaFormateada +
                    '</div>' +
                    '<div class="mt-1">' + metodoBadge + '</div>' +
                    '</div>' +
                    '<div class="text-end">' +
                    '<div class="fw-bold text-success fs-5">' + formatearMoneda(v.total) + '</div>' +
                    '<small class="text-muted">' + v.usuario + '</small>' +
                    '</div></div>';
        }
        container.innerHTML = html;
    }

    function formatearMoneda(valor) {
        return 'S/. ' + parseFloat(valor || 0).toFixed(2);
    }

    function mostrarError(mensaje) {
        console.error(mensaje);
    }
</script>
