<%-- 
    Document   : caja
    Created on : 2 dic. 2025
    Author     : Sistema Botica
    Description: Panel de control del farmacéutico - Mi Caja
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="DTO.UsuarioDTO, DTO.SesionCajaDTO, DAO.SesionCajaDAO" %>
<%
    UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
    if (usuario == null) {
        usuario = (UsuarioDTO) session.getAttribute("usuario");
    }
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    
    SesionCajaDAO sesionDAO = new SesionCajaDAO();
    SesionCajaDTO sesionActiva = sesionDAO.buscarSesionAbierta(usuario.getId());
    boolean tieneCajaAbierta = (sesionActiva != null);
    
    request.setAttribute("pageTitle", "Mi Caja - Econosalud Farmacia");
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>

<style>
    .caja-status { padding: 25px; border-radius: 15px; margin-bottom: 25px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
    .caja-status.abierta { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); color: white; }
    .caja-status.cerrada { background: linear-gradient(135deg, #6c757d 0%, #495057 100%); color: white; }
    .caja-status .icon-caja { font-size: 3.5rem; opacity: 0.3; }
    .caja-status h4 { margin-bottom: 5px; font-weight: 600; }
    .caja-status .info-turno { font-size: 0.9rem; opacity: 0.9; }
    
    .stat-card-farm { border-radius: 12px; padding: 20px; color: white; position: relative; overflow: hidden; height: 100%; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
    .stat-card-farm .icon { position: absolute; right: 15px; top: 50%; transform: translateY(-50%); font-size: 2.5rem; opacity: 0.25; }
    .stat-card-farm h6 { font-size: 0.8rem; opacity: 0.9; text-transform: uppercase; margin-bottom: 5px; }
    .stat-card-farm h3 { font-size: 1.6rem; font-weight: 700; margin-bottom: 0; }
    .stat-card-farm small { opacity: 0.8; font-size: 0.75rem; }
    
    .bg-ventas { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
    .bg-efectivo { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
    .bg-digital { background: linear-gradient(135deg, #6f42c1 0%, #9d4edd 100%); }
    .bg-cantidad { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
    
    .alert-card { border-radius: 10px; border-left: 4px solid; padding: 15px; margin-bottom: 10px; background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
    .alert-card.warning { border-color: #ffc107; background: #fff9e6; }
    .alert-card.danger { border-color: #dc3545; background: #fff5f5; }
    .alert-card .count { font-size: 1.5rem; font-weight: bold; }
    
    .tabla-alertas { font-size: 0.85rem; }
    .tabla-alertas th { background-color: #f8f9fa; font-weight: 600; }
    
    .venta-item { padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
    .venta-item:last-child { border-bottom: none; }
    .venta-numero { font-weight: 600; color: #333; }
    .venta-total { font-size: 1.1rem; font-weight: bold; color: #28a745; }
    .metodo-badge { font-size: 0.7rem; padding: 3px 8px; border-radius: 4px; }
    
    .quick-action { padding: 25px 20px; border-radius: 12px; text-align: center; transition: all 0.3s ease; text-decoration: none; display: block; height: 100%; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
    .quick-action:hover { transform: translateY(-5px); box-shadow: 0 8px 25px rgba(0,0,0,0.2); }
    .quick-action i { font-size: 2.5rem; margin-bottom: 10px; }
    .quick-action.pos { background: linear-gradient(135deg, #28a745 0%, #20c997 100%); color: white; }
    .quick-action.pos.disabled { background: linear-gradient(135deg, #adb5bd 0%, #6c757d 100%); pointer-events: none; opacity: 0.7; }
    .quick-action.historial { background: linear-gradient(135deg, #035b77 0%, #0984a3 100%); color: white; }
</style>

<%@ include file="/WEB-INF/includes/navbar.jsp" %>

<div class="container-fluid">
    <div class="row">
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
        
        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h1 class="h2"><i class="bi bi-safe me-2"></i>Mi Caja</h1>
                <div class="btn-toolbar mb-2 mb-md-0">
                    <span class="text-muted me-3" id="fechaHora"></span>
                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="cargarDatos()">
                        <i class="bi bi-arrow-clockwise me-1"></i>Actualizar
                    </button>
                </div>
            </div>

            <!-- Estado de Caja -->
            <div class="caja-status <%= tieneCajaAbierta ? "abierta" : "cerrada" %>" id="cajaStatus">
                <div class="row align-items-center">
                    <div class="col-auto">
                        <i class="bi bi-safe icon-caja"></i>
                    </div>
                    <div class="col">
                        <% if (tieneCajaAbierta) { %>
                            <h4 id="statusTitulo"><%= sesionActiva.getCajaNombre() != null ? sesionActiva.getCajaNombre() : "Caja" %> - Abierta</h4>
                            <p class="info-turno mb-0" id="statusInfo">
                                Turno iniciado | Monto inicial: S/. <%= String.format("%.2f", sesionActiva.getMontoInicial()) %>
                            </p>
                        <% } else { %>
                            <h4 id="statusTitulo">Caja Cerrada</h4>
                            <p class="info-turno mb-0" id="statusInfo">Debes abrir caja para realizar ventas</p>
                        <% } %>
                    </div>
                    <div class="col-auto">
                        <% if (tieneCajaAbierta) { %>
                            <button class="btn btn-light btn-lg" id="btnAccionCaja" onclick="CajaManager.mostrarCerrar()">
                                <i class="bi bi-lock me-2"></i>Cerrar Caja
                            </button>
                        <% } else { %>
                            <button class="btn btn-light btn-lg" id="btnAccionCaja" onclick="CajaManager.mostrarAbrir()">
                                <i class="bi bi-unlock me-2"></i>Abrir Caja
                            </button>
                        <% } %>
                    </div>
                </div>
            </div>

            <!-- Tarjetas de Ventas del Turno -->
            <div class="row mb-4" id="seccionVentasTurno" style="<%= tieneCajaAbierta ? "" : "display:none;" %>">
                <div class="col-xl-3 col-md-6 mb-3">
                    <div class="stat-card-farm bg-ventas">
                        <h6>Total Ventas Turno</h6>
                        <h3 id="totalVentasTurno">S/. 0.00</h3>
                        <small id="cantidadVentasTurno">0 ventas</small>
                        <i class="bi bi-cash-stack icon"></i>
                    </div>
                </div>
                <div class="col-xl-3 col-md-6 mb-3">
                    <div class="stat-card-farm bg-efectivo">
                        <h6>Efectivo Neto</h6>
                        <h3 id="totalEfectivo">S/. 0.00</h3>
                        <small>Recibido - Vueltos</small>
                        <i class="bi bi-cash icon"></i>
                    </div>
                </div>
                <div class="col-xl-3 col-md-6 mb-3">
                    <div class="stat-card-farm bg-digital">
                        <h6>Pagos Digitales</h6>
                        <h3 id="totalDigital">S/. 0.00</h3>
                        <small>Yape/Plin/Tarjeta</small>
                        <i class="bi bi-phone icon"></i>
                    </div>
                </div>
                <div class="col-xl-3 col-md-6 mb-3">
                    <div class="stat-card-farm bg-cantidad">
                        <h6>Efectivo Esperado</h6>
                        <h3 id="efectivoEsperado">S/. 0.00</h3>
                        <small>Inicial + Neto</small>
                        <i class="bi bi-calculator icon"></i>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-lg-8">
                    <!-- Acceso Rápido -->
                    <div class="row mb-4">
                        <div class="col-md-6 mb-3">
                            <% if (tieneCajaAbierta) { %>
                                <a href="<%= request.getContextPath() %>/farmaceutico/ventas/nueva.jsp" class="quick-action pos" id="btnPOS">
                                    <i class="bi bi-cart-plus d-block"></i>
                                    <strong>Punto de Venta</strong>
                                    <small class="d-block mt-1">Realizar nueva venta</small>
                                </a>
                            <% } else { %>
                                <div class="quick-action pos disabled" id="btnPOS">
                                    <i class="bi bi-cart-plus d-block"></i>
                                    <strong>Punto de Venta</strong>
                                    <small class="d-block mt-1">Abre caja primero</small>
                                </div>
                            <% } %>
                        </div>
                        <div class="col-md-6 mb-3">
                            <a href="<%= request.getContextPath() %>/farmaceutico/ventas/listar.jsp" class="quick-action historial">
                                <i class="bi bi-clock-history d-block"></i>
                                <strong>Historial de Ventas</strong>
                                <small class="d-block mt-1">Ver mis ventas</small>
                            </a>
                        </div>
                    </div>

                    <!-- Últimas Ventas del Turno -->
                    <div class="card mb-4">
                        <div class="card-header bg-white d-flex justify-content-between align-items-center">
                            <h5 class="mb-0"><i class="bi bi-receipt me-2"></i>Últimas Ventas del Turno</h5>
                            <a href="<%= request.getContextPath() %>/farmaceutico/ventas/listar.jsp" class="btn btn-sm btn-outline-primary">Ver todas</a>
                        </div>
                        <div class="card-body" id="ultimasVentasContainer">
                            <div class="text-center py-4">
                                <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
                                <p class="mt-2 text-muted mb-0">Cargando...</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-4">
                    <!-- Alertas de Stock -->
                    <div class="card mb-4">
                        <div class="card-header bg-white">
                            <h5 class="mb-0"><i class="bi bi-exclamation-triangle text-warning me-2"></i>Alertas de Stock</h5>
                        </div>
                        <div class="card-body">
                            <div class="alert-card danger" id="alertaAgotados" style="display: none;">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div><i class="bi bi-x-circle text-danger me-2"></i><strong>Agotados</strong></div>
                                    <span class="count text-danger" id="countAgotados">0</span>
                                </div>
                            </div>
                            <div class="alert-card warning" id="alertaStockBajo" style="display: none;">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div><i class="bi bi-exclamation-triangle text-warning me-2"></i><strong>Stock Bajo</strong></div>
                                    <span class="count text-warning" id="countStockBajo">0</span>
                                </div>
                            </div>
                            <div id="listaStockBajo" class="mt-3" style="display: none;">
                                <div class="table-responsive" style="max-height: 150px; overflow-y: auto;">
                                    <table class="table tabla-alertas table-sm mb-0">
                                        <thead><tr><th>Producto</th><th class="text-center">Stock</th></tr></thead>
                                        <tbody id="tablaStockBajo"></tbody>
                                    </table>
                                </div>
                            </div>
                            <div id="sinAlertasStock" class="text-center py-3">
                                <i class="bi bi-check-circle text-success fs-3"></i>
                                <p class="text-muted mb-0 mt-2 small">Stock OK</p>
                            </div>
                        </div>
                    </div>

                    <!-- Alertas de Vencimiento -->
                    <div class="card">
                        <div class="card-header bg-white">
                            <h5 class="mb-0"><i class="bi bi-calendar-x text-danger me-2"></i>Vencimientos</h5>
                        </div>
                        <div class="card-body">
                            <div class="alert-card danger" id="alertaVencidos" style="display: none;">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div><i class="bi bi-calendar-x-fill text-danger me-2"></i><strong>Vencidos</strong></div>
                                    <span class="count text-danger" id="countVencidos">0</span>
                                </div>
                            </div>
                            <div class="alert-card warning" id="alertaPorVencer" style="display: none;">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div><i class="bi bi-calendar-event text-warning me-2"></i><strong>Por Vencer</strong></div>
                                    <span class="count text-warning" id="countPorVencer">0</span>
                                </div>
                            </div>
                            <div id="listaPorVencer" class="mt-3" style="display: none;">
                                <div class="table-responsive" style="max-height: 150px; overflow-y: auto;">
                                    <table class="table tabla-alertas table-sm mb-0">
                                        <thead><tr><th>Producto</th><th class="text-center">Días</th></tr></thead>
                                        <tbody id="tablaPorVencer"></tbody>
                                    </table>
                                </div>
                            </div>
                            <div id="sinAlertasVencer" class="text-center py-3">
                                <i class="bi bi-check-circle text-success fs-3"></i>
                                <p class="text-muted mb-0 mt-2 small">Sin vencimientos próximos</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<!-- Modales de Caja (unificados) -->
<%@ include file="/WEB-INF/includes/caja/modal-abrir.jsp" %>
<%@ include file="/WEB-INF/includes/caja/modal-cerrar.jsp" %>

<%@ include file="/WEB-INF/includes/footer.jsp" %>

<script src="<%= request.getContextPath() %>/assets/js/caja-manager.js"></script>
<script>
    // Variables globales requeridas por CajaManager
    var contextPath = '<%= request.getContextPath() %>';
    var sesionCajaId = <%= tieneCajaAbierta ? sesionActiva.getId() : "null" %>;
    var tieneCajaAbierta = <%= tieneCajaAbierta %>;
    var montoInicialCaja = <%= tieneCajaAbierta ? sesionActiva.getMontoInicial() : "0" %>;
    var urlRetorno = null; // Se queda en la misma página

    document.addEventListener('DOMContentLoaded', function() {
        CajaManager.init();
        actualizarFechaHora();
        setInterval(actualizarFechaHora, 1000);
        cargarDatos();
    });

    function actualizarFechaHora() {
        var now = new Date();
        var opciones = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
        var fecha = now.toLocaleDateString('es-PE', opciones);
        var hora = now.toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        document.getElementById('fechaHora').textContent = fecha + ' - ' + hora;
    }

    function cargarDatos() {
        fetch(contextPath + '/DashboardController?action=miTurno')
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.tieneCajaAbierta && data.ventasTurno) {
                    actualizarVentasTurno(data.ventasTurno, data.sesionActiva);
                }
                actualizarUltimasVentas(data.ultimasVentas);
                actualizarAlertas(data);
            })
            .catch(function(error) { console.error('Error:', error); });
    }

    function actualizarVentasTurno(vt, sesion) {
        var totalVentas = parseFloat(vt.totalVentas || 0);
        var cantidadVentas = parseInt(vt.cantidadVentas || 0);
        var efectivoNeto = parseFloat(vt.totalEfectivo || 0) - parseFloat(vt.totalVueltos || 0);
        var totalDigital = parseFloat(vt.totalYapePlin || 0) + parseFloat(vt.totalTarjeta || 0);
        var montoInicial = parseFloat(sesion ? sesion.montoInicial : montoInicialCaja) || 0;
        var efectivoEsperadoVal = montoInicial + efectivoNeto;

        document.getElementById('totalVentasTurno').textContent = formatearMoneda(totalVentas);
        document.getElementById('cantidadVentasTurno').textContent = cantidadVentas + ' ventas';
        document.getElementById('totalEfectivo').textContent = formatearMoneda(efectivoNeto);
        document.getElementById('totalDigital').textContent = formatearMoneda(totalDigital);
        document.getElementById('efectivoEsperado').textContent = formatearMoneda(efectivoEsperadoVal);
    }

    function actualizarUltimasVentas(ventas) {
        var container = document.getElementById('ultimasVentasContainer');

        if (!ventas || ventas.length === 0) {
            container.innerHTML = '<div class="text-center py-4 text-muted">' +
                '<i class="bi bi-inbox fs-1 d-block mb-2"></i>' +
                (tieneCajaAbierta ? 'Aún no hay ventas en este turno' : 'Abre caja para empezar a vender') + '</div>';
            return;
        }

        var html = '';
        for (var i = 0; i < ventas.length; i++) {
            var v = ventas[i];
            var fecha = new Date(v.fecha);
            var horaFormateada = fecha.toLocaleTimeString('es-PE', {hour: '2-digit', minute: '2-digit'});

            html += '<div class="venta-item d-flex justify-content-between align-items-center">' +
                '<div><span class="venta-numero">' + v.numero + '</span><br>' +
                '<small class="text-muted">' + horaFormateada + '</small> ' + obtenerBadgeMetodo(v.metodoPago) + '</div>' +
                '<span class="venta-total">' + formatearMoneda(v.total) + '</span></div>';
        }
        container.innerHTML = html;
    }

    function actualizarAlertas(data) {
        var hayAlertasStock = false;
        var hayAlertasVencer = false;

        if (data.agotados > 0) {
            document.getElementById('alertaAgotados').style.display = 'block';
            document.getElementById('countAgotados').textContent = data.agotados;
            hayAlertasStock = true;
        } else {
            document.getElementById('alertaAgotados').style.display = 'none';
        }

        if (data.stockBajo > 0) {
            document.getElementById('alertaStockBajo').style.display = 'block';
            document.getElementById('countStockBajo').textContent = data.stockBajo;
            hayAlertasStock = true;

            if (data.productosStockBajo && data.productosStockBajo.length > 0) {
                document.getElementById('listaStockBajo').style.display = 'block';
                var html = '';
                for (var i = 0; i < Math.min(data.productosStockBajo.length, 5); i++) {
                    var p = data.productosStockBajo[i];
                    html += '<tr><td class="small">' + truncarTexto(p.nombre, 25) + '</td>' +
                        '<td class="text-center"><span class="badge bg-danger">' + p.stockActual + '</span></td></tr>';
                }
                document.getElementById('tablaStockBajo').innerHTML = html;
            }
        } else {
            document.getElementById('alertaStockBajo').style.display = 'none';
            document.getElementById('listaStockBajo').style.display = 'none';
        }

        document.getElementById('sinAlertasStock').style.display = hayAlertasStock ? 'none' : 'block';

        if (data.vencidos > 0) {
            document.getElementById('alertaVencidos').style.display = 'block';
            document.getElementById('countVencidos').textContent = data.vencidos;
            hayAlertasVencer = true;
        } else {
            document.getElementById('alertaVencidos').style.display = 'none';
        }

        if (data.porVencer > 0) {
            document.getElementById('alertaPorVencer').style.display = 'block';
            document.getElementById('countPorVencer').textContent = data.porVencer;
            hayAlertasVencer = true;

            if (data.productosPorVencer && data.productosPorVencer.length > 0) {
                document.getElementById('listaPorVencer').style.display = 'block';
                var html = '';
                for (var i = 0; i < Math.min(data.productosPorVencer.length, 5); i++) {
                    var p = data.productosPorVencer[i];
                    var badgeClass = p.diasRestantes <= 7 ? 'bg-danger' : p.diasRestantes <= 15 ? 'bg-warning' : 'bg-info';
                    html += '<tr><td class="small">' + truncarTexto(p.nombre, 25) + '</td>' +
                        '<td class="text-center"><span class="badge ' + badgeClass + '">' + p.diasRestantes + 'd</span></td></tr>';
                }
                document.getElementById('tablaPorVencer').innerHTML = html;
            }
        } else {
            document.getElementById('alertaPorVencer').style.display = 'none';
            document.getElementById('listaPorVencer').style.display = 'none';
        }

        document.getElementById('sinAlertasVencer').style.display = hayAlertasVencer ? 'none' : 'block';
    }

    function formatearMoneda(valor) {
        return 'S/. ' + parseFloat(valor || 0).toFixed(2);
    }

    function truncarTexto(texto, max) {
        if (!texto) return '';
        return texto.length > max ? texto.substring(0, max - 3) + '...' : texto;
    }

    function obtenerBadgeMetodo(metodo) {
        switch(metodo) {
            case 'EFECTIVO': return '<span class="metodo-badge bg-success text-white">Efectivo</span>';
            case 'YAPE_PLIN': return '<span class="metodo-badge text-white" style="background:#6f42c1;">Yape/Plin</span>';
            case 'TARJETA': return '<span class="metodo-badge bg-info text-white">Tarjeta</span>';
            case 'MIXTO': return '<span class="metodo-badge bg-warning text-dark">Mixto</span>';
            default: return '<span class="metodo-badge bg-secondary text-white">' + (metodo || '-') + '</span>';
        }
    }
</script>
