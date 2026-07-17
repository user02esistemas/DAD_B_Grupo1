<%-- 
    Document   : nueva (POS - Punto de Venta)
    Created on : 07 dic. 2025
    Author     : Sistema Botica
    Description: Punto de Venta (POS) - Modelo Farmacia Perú (IGV incluido)
--%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="DTO.UsuarioDTO, DTO.SesionCajaDTO, DTO.RolDTO, integration.api.CajaApiClient, java.util.List" %>
<%
    UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    
    // Determinar rol para redirección al cerrar
    boolean posEsAdmin = false;
    List<RolDTO> roles = (List<RolDTO>) session.getAttribute("roles");
    if (roles != null) {
        for (RolDTO rol : roles) {
            if ("ROLE_ADMIN".equals(rol.getNombre())) {
                posEsAdmin = true;
                break;
            }
        }
    }
    String urlRetorno = posEsAdmin ? "/admin/dashboard.jsp" : "/farmaceutico/caja.jsp";
    String username = usuario.getUsername();
    String nombreCompleto = usuario.getNombreCompleto();
    String rolActual = posEsAdmin ? "ADMIN" : "FARMACEUTICO";
    
    SesionCajaDTO sesionCaja = new CajaApiClient().buscarSesionAbierta(usuario.getId());
    boolean cajaCerrada = (sesionCaja == null);
    
    // Datos para el ticket
    String nombreUsuario = usuario.getNombreCompleto();
    String nombreUsuarioOculto = nombreUsuario.length() > 4 ? 
        nombreUsuario.substring(0, 2).toUpperCase() + "****" + 
        nombreUsuario.substring(nombreUsuario.length() - 2).toUpperCase() : nombreUsuario;
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Punto de Venta - Econosalud</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { 
            background: #f5f5f5; 
            font-family: 'Segoe UI', sans-serif;
            overflow: hidden;
            height: 100vh;
            margin: 0;
        }
        .pos-main {
            min-width: 0;
            min-height: 0;
            height: calc(100vh - var(--navbar-height));
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }
        .pos-header {
            background: #035b77;
            color: white;
            padding: 10px 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .pos-header .logo { font-size: 24px; font-weight: bold; }
        .pos-header .user-info { display: flex; align-items: center; gap: 15px; }
        .search-container {
            background: white;
            padding: 10px 16px;
            border-bottom: 1px solid #ddd;
        }
        .search-input {
            font-size: 16px;
            padding: 10px 18px;
            border: 2px solid #035b77;
            border-radius: 30px;
            width: 100%;
        }
        .search-input:focus {
            outline: none;
            box-shadow: 0 0 0 3px rgba(3, 91, 119, 0.18);
        }
        .quick-menu {
            background: #fff;
            padding: 7px 16px;
            border-bottom: 1px solid #ddd;
            display: flex;
            gap: 20px;
            font-size: 13px;
        }
        .quick-menu a { color: #666; text-decoration: none; }
        .quick-menu a:hover { color: #035b77; }
        .pos-container {
            display: flex;
            height: calc(100vh - 140px);
        }
        .products-panel {
            flex: 1;
            display: flex;
            flex-direction: column;
            border-right: 1px solid #ddd;
            background: white;
        }
        .products-header {
            display: grid;
            grid-template-columns: 50px 1fr 70px 80px 80px 90px 70px 60px;
            gap: 10px;
            padding: 10px 15px;
            background: #f8f9fa;
            font-weight: 600;
            font-size: 12px;
            color: #666;
            border-bottom: 1px solid #ddd;
        }
        .products-list {
            flex: 1;
            overflow-y: auto;
            padding: 0;
        }
        .product-row {
            display: grid;
            grid-template-columns: 50px 1fr 70px 80px 80px 90px 70px 60px;
            gap: 10px;
            padding: 12px 15px;
            border-bottom: 1px solid #eee;
            align-items: center;
            font-size: 13px;
        }
        .product-row:hover { background: #f8f9fa; }
        .product-actions { display: flex; gap: 5px; }
        .product-actions button {
            width: 30px;
            height: 30px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
        }
        .btn-delete { background: #dc3545; color: white; }
        .btn-delete:hover { background: #c82333; }
        .product-name { font-weight: 500; }
        .quantity-cell {
            cursor: pointer;
            color: #035b77;
            font-weight: bold;
            text-decoration: underline;
        }
        .summary-panel {
            width: 320px;
            background: white;
            display: flex;
            flex-direction: column;
            padding: 20px;
        }
        .total-display { text-align: right; margin-bottom: 20px; }
        .total-label { font-size: 14px; color: #666; }
        .total-amount { font-size: 42px; font-weight: bold; color: #035b77; }
        .client-info {
            background: #f8f9fa;
            padding: 10px 15px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        .client-info label { font-size: 12px; color: #666; }
        .client-info .client-name { font-weight: bold; }
        .action-buttons { display: flex; flex-direction: column; gap: 10px; }
        .action-btn {
            padding: 15px;
            border: 2px dashed #ddd;
            border-radius: 10px;
            background: white;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 14px;
            font-weight: 500;
            transition: all 0.2s;
        }
        .action-btn:hover { border-color: #035b77; background: #f8f9fa; }
        .action-btn i { font-size: 20px; }
        .action-btn.primary { background: #035b77; color: white; border: none; }
        .action-btn.primary:hover { background: #0b7896; }
        .summary-totals {
            margin-top: auto;
            padding-top: 20px;
            border-top: 1px solid #eee;
        }
        .summary-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 8px;
            font-size: 14px;
        }
        .autocomplete-results {
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: white;
            border: 1px solid #ddd;
            border-top: none;
            border-radius: 0 0 10px 10px;
            max-height: 400px;
            overflow-y: auto;
            z-index: 1000;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            display: none;
        }
        .autocomplete-item {
            padding: 12px 20px;
            cursor: pointer;
            border-bottom: 1px solid #eee;
        }
        .autocomplete-item:hover { background: #f8f9fa; }
        .autocomplete-item .name { font-weight: 500; color: #333; }
        .autocomplete-item .details { font-size: 12px; color: #666; margin-top: 4px; }
        .autocomplete-item .stock { float: right; color: #0b7896; font-weight: 500; }
        .autocomplete-item .price { float: right; color: #035b77; font-weight: bold; margin-left: 15px; }
        .empty-state {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100%;
            color: #999;
        }
        .empty-state h5 { color: #666; }
        .btn-cerrar-turno {
            background: #dc3545;
            color: white;
            border: none;
            padding: 8px 16px;
            border-radius: 5px;
            cursor: pointer;
        }
        .btn-cerrar-turno:hover { background: #c82333; }
        .btn-volver {
            background: rgba(255,255,255,0.2);
            color: white;
            border: 1px solid rgba(255,255,255,0.3);
            padding: 8px 16px;
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
        }
        .btn-volver:hover { background: rgba(255,255,255,0.3); color: white; }
        
        /* Métodos de pago */
        .metodo-pago-btn {
            flex: 1;
            padding: 15px 10px;
            border: 2px solid #ddd;
            border-radius: 10px;
            background: white;
            cursor: pointer;
            text-align: center;
            transition: all 0.2s;
        }
        .metodo-pago-btn:hover { border-color: #035b77; }
        .metodo-pago-btn.active { border-color: #035b77; background: #eef8fb; }
        .metodo-pago-btn i { font-size: 24px; display: block; margin-bottom: 5px; }
        .metodo-pago-btn span { font-size: 12px; }
        
        /* Ticket */
        .ticket-container {
            font-family: 'Courier New', monospace;
            width: 280px;
            margin: 0 auto;
            padding: 15px;
            background: white;
            font-size: 12px;
        }
        .ticket-header { text-align: center; margin-bottom: 10px; }
        .ticket-logo { font-size: 18px; font-weight: bold; margin-bottom: 5px; }
        .ticket-divider { border-top: 1px dashed #333; margin: 8px 0; }
        .ticket-row { display: flex; justify-content: space-between; margin: 3px 0; }
        .ticket-item { margin: 5px 0; font-size: 11px; }
        .ticket-item-header { display: grid; grid-template-columns: 30px 1fr 50px 50px; font-size: 10px; margin-bottom: 5px; }
        .ticket-item-row { display: grid; grid-template-columns: 30px 1fr 50px 50px; font-size: 11px; }
        .ticket-total { font-weight: bold; font-size: 14px; }
        .ticket-footer { text-align: center; margin-top: 15px; font-size: 10px; }
        .ticket-qr { text-align: center; margin: 10px 0; }
        .logo-img { width: 55px; height: auto; display: block; margin: 0 auto; }
        .pos-container.modern-pos {
            display: grid;
            grid-template-columns: minmax(0, 1fr) 420px;
            gap: 10px;
            height: calc(100vh - var(--navbar-height) - 205px);
            min-height: 0;
            padding: 10px 14px;
            background: #f4f7f9;
        }
        .workspace-panel {
            min-width: 0;
            display: grid;
            grid-template-rows: 42% minmax(0, 1fr);
            gap: 10px;
        }
        .pos-card {
            background: #fff;
            border: 1px solid #d7e8ee;
            border-radius: 14px;
            box-shadow: 0 1px 8px rgba(3, 91, 119, 0.07);
            overflow: hidden;
        }
        .pos-card-header {
            height: 42px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 16px;
            border-bottom: 1px solid #d7e8ee;
            background: #f7fbfd;
            font-weight: 800;
            color: #20202a;
        }
        .quick-products-grid {
            padding: 12px;
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
            gap: 10px;
            overflow-y: auto;
            height: calc(100% - 42px);
        }
        .quick-product-card {
            min-height: 104px;
            border: 1px solid #e6ddd4;
            border-radius: 12px;
            padding: 12px;
            cursor: pointer;
            color: #fff;
            background: linear-gradient(180deg, rgba(3, 91, 119, 0.04), rgba(0, 0, 0, 0.70)), linear-gradient(135deg, #0b7896, #035b77 52%, #03394d);
            display: flex;
            flex-direction: column;
            justify-content: flex-end;
            transition: transform 0.15s ease, box-shadow 0.15s ease;
        }
        .quick-product-card:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(3, 91, 119, 0.18); }
        .quick-product-name { font-weight: 800; font-size: 14px; line-height: 1.2; }
        .quick-product-meta { font-size: 11px; opacity: 0.85; margin-top: 3px; }
        .quick-product-price { font-size: 16px; font-weight: 900; margin-top: 4px; }
        .cart-panel {
            display: flex;
            flex-direction: column;
            min-height: 0;
        }
        .cart-table-header, .cart-row {
            display: grid;
            grid-template-columns: 44px minmax(0, 1fr) 80px 90px 90px 52px;
            gap: 10px;
            align-items: center;
        }
        .cart-table-header {
            padding: 10px 14px;
            color: #7b7280;
            font-size: 12px;
            font-weight: 800;
            border-bottom: 1px solid #eee5dc;
        }
        .cart-row {
            padding: 12px 14px;
            border-bottom: 1px solid #f0e8df;
            font-size: 13px;
        }
        .cart-row:hover { background: #fffcf8; }
        .cart-name { font-weight: 800; color: #25212a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
        .cart-sub { color: #8a8190; font-size: 11px; margin-top: 2px; }
        .cart-qty {
            border: 1px solid #d7c8b8;
            border-radius: 999px;
            padding: 5px 10px;
            text-align: center;
            color: #145646;
            font-weight: 900;
            cursor: pointer;
        }
        .cart-remove {
            border: none;
            background: #fff1f1;
            color: #c92a2a;
            width: 34px;
            height: 34px;
            border-radius: 10px;
        }
        .summary-panel.modern-summary {
            width: auto;
            padding: 0;
            height: 100%;
            min-height: 0;
            border: 1px solid #d7e8ee;
            border-radius: 14px;
            box-shadow: 0 1px 8px rgba(3, 91, 119, 0.08);
            overflow: hidden;
        }
        .summary-section { padding: 14px; border-bottom: 1px solid #d7e8ee; }
        .summary-section:last-child { border-bottom: none; }
        .section-label { font-size: 11px; color: #7b7280; font-weight: 800; letter-spacing: .06em; text-transform: uppercase; margin-bottom: 7px; }
        .side-input, .side-select {
            width: 100%;
            height: 36px;
            border: 1px solid #cfe3eb;
            border-radius: 8px;
            padding: 0 12px;
            background: #fbfdfe;
        }
        .side-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
        .total-display.modern-total { text-align: right; margin: 0; }
        .total-display.modern-total .total-amount { font-size: 36px; color: #035b77; }
        .calc-card {
            border: 1px solid #d7e8ee;
            border-radius: 12px;
            padding: 11px;
            background: #f7fbfd;
        }
        .pay-button {
            width: 100%;
            border: none;
            border-radius: 14px;
            background: #035b77;
            color: #fff;
            font-weight: 900;
            padding: 16px;
            letter-spacing: .04em;
        }
        .pay-button:disabled, .pay-button.disabled {
            background: #d7e8ee;
            color: #6b8792;
        }
        @media (max-width: 1100px) {
            .pos-container.modern-pos { grid-template-columns: 1fr; overflow-y: auto; }
            .summary-panel.modern-summary { min-height: 520px; }
            body { overflow: auto; }
        }
    </style>
</head>
<body>
    <%@ include file="/WEB-INF/includes/navbar.jsp" %>

    <div class="container-fluid">
        <div class="row">
            <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

            <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4 pos-main">
                <div class="d-flex justify-content-between flex-wrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                    <div>
                        <h1 class="h3 mb-1"><i class="bi bi-cash-stack me-2"></i>Punto de Venta</h1>
                        <span class="badge bg-light text-dark border" id="estadoCaja">
                            <% if (!cajaCerrada) { %>
                                <i class="bi bi-unlock-fill text-primary"></i> Caja Abierta
                            <% } else { %>
                                <i class="bi bi-lock-fill text-danger"></i> Caja Cerrada
                            <% } %>
                        </span>
                    </div>
                    <div class="d-flex gap-2">
                        <a href="<%= request.getContextPath() + urlRetorno %>" class="btn btn-outline-secondary">
                            <i class="bi bi-arrow-left"></i> Volver
                        </a>
                        <button class="btn btn-danger" onclick="CajaManager.mostrarCerrar()">
                            <i class="bi bi-box-arrow-right"></i> Cerrar Turno
                        </button>
                    </div>
                </div>

    <!-- Buscador -->
    <div class="search-container">
        <div class="position-relative">
            <input type="text" class="search-input" id="searchInput" 
                   placeholder="Buscar medicamento por nombre, principio activo o lote..." autocomplete="off">
            <div class="autocomplete-results" id="autocompleteResults"></div>
        </div>
    </div>

    <!-- Menú rápido -->
    <div class="quick-menu">
        <a href="<%= request.getContextPath() %>/farmaceutico/ventas/listar.jsp"><i class="bi bi-clock-history"></i> Historial de Ventas</a>
        <% if (posEsAdmin) { %>
        <a href="<%= request.getContextPath() %>/admin/inventario/listar.jsp"><i class="bi bi-boxes"></i> Inventario</a>
        <% } %>
    </div>

    <!-- Contenedor principal -->
    <div class="pos-container modern-pos">
        <div class="workspace-panel">
            <section class="pos-card">
                <div class="pos-card-header">
                    <span><i class="bi bi-lightning-charge text-warning"></i> Productos Rápidos</span>
                    <small class="text-muted">Selecciona para agregar al carrito</small>
                </div>
                <div class="quick-products-grid" id="quickProducts">
                    <div class="text-muted small p-2">Cargando productos frecuentes...</div>
                </div>
            </section>

            <section class="pos-card cart-panel">
                <div class="pos-card-header">
                    <span><i class="bi bi-cart3"></i> Carrito de Venta</span>
                    <button type="button" class="btn btn-sm btn-outline-danger" onclick="vaciarCarrito()">
                        <i class="bi bi-trash3"></i> Vaciar
                    </button>
                </div>
                <div class="cart-table-header">
                    <span>Acc.</span>
                    <span>Medicamento</span>
                    <span>Cant.</span>
                    <span>P.Unit.</span>
                    <span>Importe</span>
                    <span>Stock</span>
                </div>
                <div class="products-list" id="productsList">
                    <div class="empty-state" id="emptyState">
                        <i class="bi bi-cart3" style="font-size: 70px; color: #ddd;"></i>
                        <h5>El carrito está vacío</h5>
                        <p class="text-muted">Busca o selecciona productos rápidos</p>
                    </div>
                </div>
            </section>
        </div>

        <aside class="summary-panel modern-summary">
            <div class="summary-section">
                <div class="d-flex justify-content-between align-items-start">
                    <div>
                        <div class="section-label">Cliente</div>
                        <div class="input-group">
                            <span class="input-group-text bg-white"><i class="bi bi-person"></i></span>
                            <input type="text" class="side-input" id="clientNameInput" value="CLIENTES VARIOS" oninput="sincronizarCliente()">
                        </div>
                    </div>
                    <div class="total-display modern-total">
                        <div class="total-label">Total a Pagar</div>
                        <div class="total-amount">S/ <span id="totalAmount">0.00</span></div>
                    </div>
                </div>
                <div class="client-info d-none">
                    <div class="client-name" id="clientName">CLIENTES VARIOS</div>
                </div>
            </div>

            <div class="summary-section">
                <div class="side-grid">
                    <div>
                        <div class="section-label">Método de pago</div>
                        <select class="side-select" id="sideMetodoPago" onchange="seleccionarMetodoPago(this.value)">
                            <option value="EFECTIVO">Efectivo</option>
                            <option value="YAPE">Yape</option>
                            <option value="PLIN">Plin</option>
                            <option value="TRANSFERENCIA">Transferencia</option>
                        </select>
                    </div>
                    <div>
                        <div class="section-label">Comprobante</div>
                        <select class="side-select" id="sideComprobante">
                            <option value="NOTA_VENTA">Nota de venta</option>
                            <option value="BOLETA">Boleta</option>
                            <option value="FACTURA">Factura</option>
                        </select>
                    </div>
                </div>
            </div>

            <div class="summary-section">
                <div class="calc-card">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <strong><i class="bi bi-calculator"></i> Calculadora de vuelto</strong>
                        <strong>S/ <span id="calcTotal">0.00</span></strong>
                    </div>
                    <label class="small text-muted">Monto recibido</label>
                    <input type="number" class="side-input mb-2" id="sideMontoRecibido" step="0.01" min="0" oninput="calcularCambioRapido()">
                    <div class="d-flex justify-content-between bg-white rounded p-2">
                        <span class="text-muted">Vuelto:</span>
                        <strong>S/ <span id="sideVuelto">0.00</span></strong>
                    </div>
                </div>
            </div>

            <div class="summary-section summary-totals mt-auto">
                <div class="summary-row"><span>Op. Gravadas:</span><span>S/ <span id="subtotalAmount">0.00</span></span></div>
                <div class="summary-row"><span>IGV (18%):</span><span>S/ <span id="igvAmount">0.00</span></span></div>
                <div class="summary-row"><span>Descuento:</span><span>S/ <span id="descuentoAmount">0.00</span></span></div>
                <div class="summary-row small text-muted"><span><i class="bi bi-info-circle"></i> Precios incluyen IGV</span><span id="cartCount">0 items</span></div>
                <button class="pay-button mt-3" id="sidePayButton" onclick="prepararCobroDesdePanel()" disabled>
                    <i class="bi bi-check2"></i> CONFIRMAR VENTA
                </button>
            </div>
        </aside>
    </div>
        </main>
    </div>
    </div>

    <!-- Modales de Caja (unificados) -->
    <%@ include file="/WEB-INF/includes/caja/modal-abrir.jsp" %>
    <%@ include file="/WEB-INF/includes/caja/modal-cerrar.jsp" %>

    <!-- Modal Cobro -->
    <div class="modal fade" id="modalCobro" tabindex="-1">
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title"><i class="bi bi-cash-coin"></i> Procesar Cobro</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="text-center mb-4 p-3 bg-light rounded">
                                <small class="text-muted">TOTAL A COBRAR</small>
                                <h2 class="text-primary mb-0">S/. <span id="cobroTotal">0.00</span></h2>
                            </div>
                            
                            <label class="form-label fw-bold mb-2">Método de Pago:</label>
                            <div class="d-flex gap-2 mb-4">
                                <div class="metodo-pago-btn active" data-metodo="EFECTIVO" onclick="seleccionarMetodoPago('EFECTIVO')">
                                    <i class="bi bi-cash-coin text-primary"></i>
                                    <span>Efectivo</span>
                                </div>
                                <div class="metodo-pago-btn" data-metodo="YAPE" onclick="seleccionarMetodoPago('YAPE')">
                                    <i class="bi bi-phone" style="color: #6f2c91;"></i>
                                    <span>Yape</span>
                                </div>
                                <div class="metodo-pago-btn" data-metodo="PLIN" onclick="seleccionarMetodoPago('PLIN')">
                                    <i class="bi bi-phone text-info"></i>
                                    <span>Plin</span>
                                </div>
                                <div class="metodo-pago-btn" data-metodo="TRANSFERENCIA" onclick="seleccionarMetodoPago('TRANSFERENCIA')">
                                    <i class="bi bi-bank text-primary"></i>
                                    <span>Transf.</span>
                                </div>
                            </div>
                            
                            <div id="seccionEfectivo">
                                <div class="mb-3">
                                    <label class="form-label"><i class="bi bi-cash"></i> Efectivo Recibido:</label>
                                    <input type="number" class="form-control form-control-lg text-center" 
                                           id="montoEfectivo" placeholder="0.00" step="0.01" min="0" oninput="calcularCambio()">
                                </div>
                                <div class="d-flex gap-2 mb-3">
                                    <button type="button" class="btn btn-outline-secondary flex-fill" onclick="setEfectivo(10)">S/10</button>
                                    <button type="button" class="btn btn-outline-secondary flex-fill" onclick="setEfectivo(20)">S/20</button>
                                    <button type="button" class="btn btn-outline-secondary flex-fill" onclick="setEfectivo(50)">S/50</button>
                                    <button type="button" class="btn btn-outline-secondary flex-fill" onclick="setEfectivo(100)">S/100</button>
                                </div>
                                <button type="button" class="btn btn-outline-primary w-100 mb-3" onclick="setMontoExacto()">
                                    <i class="bi bi-check-circle"></i> Monto Exacto
                                </button>
                                <div class="card bg-light">
                                    <div class="card-body text-center py-3">
                                        <small class="text-muted">CAMBIO A DEVOLVER</small>
                                        <h3 class="mb-0" id="cambioAmount">S/. 0.00</h3>
                                        <small class="text-danger" id="restanteText"></small>
                                    </div>
                                </div>
                            </div>
                            
                            <div id="seccionVirtual" style="display: none;">
                                <div class="alert alert-info">
                                    <i class="bi bi-info-circle"></i> 
                                    Pago: <strong id="metodoVirtualNombre">YAPE</strong><br>
                                    <small>Monto: <strong>S/. <span id="montoVirtualDisplay">0.00</span></strong></small>
                                </div>
                                <div class="card text-white" style="background: #035b77;">
                                    <div class="card-body text-center py-3">
                                        <i class="bi bi-check-circle" style="font-size: 40px;"></i>
                                        <p class="mb-0 mt-2">Sin vuelto - Pago exacto</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label class="form-label fw-bold">Tipo de Comprobante:</label>
                                <div class="btn-group w-100" role="group">
                                    <input type="radio" class="btn-check" name="tipoComp" id="compNota" value="NOTA_VENTA" checked>
                                    <label class="btn btn-outline-primary" for="compNota">Nota de Venta</label>
                                    <input type="radio" class="btn-check" name="tipoComp" id="compBoleta" value="BOLETA">
                                    <label class="btn btn-outline-primary" for="compBoleta">Boleta</label>
                                    <input type="radio" class="btn-check" name="tipoComp" id="compFactura" value="FACTURA">
                                    <label class="btn btn-outline-primary" for="compFactura">Factura</label>
                                </div>
                            </div>
                            
                            <div class="card mb-3">
                                <div class="card-header bg-light">
                                    <small class="fw-bold">Resumen de Venta</small>
                                </div>
                                <div class="card-body py-2">
                                    <div class="d-flex justify-content-between small">
                                        <span>Items:</span>
                                        <span id="resumenItems">0</span>
                                    </div>
                                    <div class="d-flex justify-content-between small">
                                        <span>Op. Gravadas:</span>
                                        <span>S/ <span id="resumenSubtotal">0.00</span></span>
                                    </div>
                                    <div class="d-flex justify-content-between small">
                                        <span>IGV (18%):</span>
                                        <span>S/ <span id="resumenIGV">0.00</span></span>
                                    </div>
                                    <div class="d-flex justify-content-between small">
                                        <span>Descuento:</span>
                                        <span>S/ <span id="resumenDescuento">0.00</span></span>
                                    </div>
                                    <hr class="my-2">
                                    <div class="d-flex justify-content-between fw-bold">
                                        <span>TOTAL:</span>
                                        <span>S/ <span id="resumenTotal">0.00</span></span>
                                    </div>
                                </div>
                            </div>
                            
                            <hr>
                            <button class="btn btn-primary btn-lg w-100" id="btnConfirmarVenta" onclick="procesarVenta()">
                                <i class="bi bi-check-circle"></i> Confirmar Venta
                            </button>
                            <small class="text-muted d-block text-center mt-2">
                                <i class="bi bi-keyboard"></i> Presiona ENTER para confirmar
                            </small>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal Editar Cantidad -->
    <div class="modal fade" id="modalCantidad" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered modal-sm">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Editar Cantidad</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body text-center">
                    <h6 id="modalProductoNombre" class="mb-3 text-primary"></h6>
                    <input type="number" class="form-control form-control-lg text-center mb-3" 
                           id="modalCantidadInput" min="1">
                    <small class="text-muted" id="modalStockInfo"></small>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" class="btn btn-primary" onclick="confirmarCantidad()">Aceptar</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal Venta Exitosa con Ticket -->
    <div class="modal fade" id="modalExito" data-bs-backdrop="static" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content">
                <div class="modal-body p-0">
                    <div class="row g-0">
                        <div class="col-md-5 text-white p-4 d-flex flex-column justify-content-center align-items-center" style="background: #035b77;">
                            <i class="bi bi-check-circle-fill" style="font-size: 80px;"></i>
                            <h3 class="mt-3">¡VENTA EXITOSA!</h3>
                            <hr class="w-75">
                            <div class="text-center">
                                <p class="mb-1">Total cobrado:</p>
                                <h2 id="exitoTotalGrande">S/ 0.00</h2>
                            </div>
                            <div class="text-center" id="exitoVueltoSection">
                                <p class="mb-1">Vuelto:</p>
                                <h4 id="exitoVueltoGrande">S/ 0.00</h4>
                            </div>
                            <button class="btn btn-light btn-lg mt-4" onclick="nuevaVenta()">
                                <i class="bi bi-plus-circle"></i> Nueva Venta
                            </button>
                        </div>
                        <div class="col-md-7 p-3 bg-light" style="max-height: 80vh; overflow-y: auto;">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h6 class="mb-0"><i class="bi bi-receipt"></i> Ticket de Venta</h6>
                                <button class="btn btn-sm btn-outline-primary" onclick="imprimirTicket()">
                                    <i class="bi bi-printer"></i> Imprimir
                                </button>
                            </div>
                            <div id="ticketContent" class="ticket-container border"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script src="<%= request.getContextPath() %>/assets/js/caja-manager.js"></script>
    <script>
        // Variables globales requeridas por CajaManager
        var contextPath = '<%= request.getContextPath() %>';
        var urlRetorno = '<%= request.getContextPath() + urlRetorno %>';
        var sesionCajaId = <% if (!cajaCerrada) { %><%= sesionCaja.getId() %><% } else { %>null<% } %>;
        var nombreCajero = '<%= nombreUsuarioOculto %>';
        
        // Variables de venta
        var productosVenta = [];
        var totalVenta = 0;
        var subtotalVenta = 0;
        var igvVenta = 0;
        var descuentoTotal = 0;
        var productoEditandoIndex = -1;
        var modalCantidadInstance = null;
        var metodoPagoSeleccionado = 'EFECTIVO';
        
        // Inicialización
        document.addEventListener('DOMContentLoaded', function() {
            // Inicializar CajaManager
            CajaManager.init({
                mostrarAbrirAlInicio: <%= cajaCerrada %>
            });
            
            // Buscador
            var searchInput = document.getElementById('searchInput');
            var searchTimeout;
            
            searchInput.addEventListener('input', function() {
                clearTimeout(searchTimeout);
                var termino = this.value;
                searchTimeout = setTimeout(function() { buscarProductos(termino); }, 300);
            });
            
            document.addEventListener('click', function(e) {
                if (!e.target.closest('.search-container')) {
                    document.getElementById('autocompleteResults').style.display = 'none';
                }
            });
            
            // Atajos de teclado
            document.addEventListener('keydown', function(e) {
                if (e.code === 'Space' && e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA') {
                    e.preventDefault();
                    if (productosVenta.length > 0) mostrarModalCobro();
                }
                if (e.code === 'Enter' && document.getElementById('modalCobro').classList.contains('show')) {
                    e.preventDefault();
                    procesarVenta();
                }
            });
            
            renderizarProductos();
            cargarProductosRapidos();
            if (!<%= cajaCerrada %>) document.getElementById('searchInput').focus();
        });
        
        // =====================================================
        //              BÚSQUEDA DE PRODUCTOS
        // =====================================================
        function buscarProductos(termino) {
            var resultsDiv = document.getElementById('autocompleteResults');
            if (termino.length < 2) {
                resultsDiv.style.display = 'none';
                return;
            }
            
            fetch(contextPath + '/VentaController?action=buscarProductos&termino=' + encodeURIComponent(termino))
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data.length === 0) {
                        resultsDiv.innerHTML = '<div class="p-3 text-muted text-center">No se encontraron productos</div>';
                    } else {
                        var html = '';
                        for (var i = 0; i < data.length; i++) {
                            var p = data[i];
                            html += '<div class="autocomplete-item" data-index="' + i + '">' +
                                '<span class="price">S/ ' + parseFloat(p.precioVenta).toFixed(2) + '</span>' +
                                '<span class="stock">Stock: ' + p.stock + '</span>' +
                                '<div class="name">' + p.nombre + ' ' + (p.concentracion || '') + '</div>' +
                                '<div class="details">' + (p.laboratorio || '-') + ' | Lote: ' + (p.lote || '-') + '</div>' +
                            '</div>';
                        }
                        resultsDiv.innerHTML = html;
                        
                        var items = resultsDiv.querySelectorAll('.autocomplete-item');
                        for (var j = 0; j < items.length; j++) {
                            (function(index) {
                                items[index].addEventListener('click', function() {
                                    agregarProductoDesdeData(data[index]);
                                });
                            })(j);
                        }
                    }
                    resultsDiv.style.display = 'block';
                })
                .catch(function(err) { console.error('Error:', err); });
        }
        
        function agregarProductoDesdeData(producto) {
            document.getElementById('autocompleteResults').style.display = 'none';
            document.getElementById('searchInput').value = '';
            
            var existente = -1;
            for (var i = 0; i < productosVenta.length; i++) {
                if (productosVenta[i].id === producto.id) {
                    existente = i;
                    break;
                }
            }
            
            if (existente >= 0) {
                if (productosVenta[existente].cantidad < productosVenta[existente].stockMax) {
                    productosVenta[existente].cantidad++;
                    productosVenta[existente].importe = productosVenta[existente].cantidad * productosVenta[existente].precioUnitario;
                } else {
                    alert('Stock insuficiente. Disponible: ' + productosVenta[existente].stockMax);
                    return;
                }
            } else {
                productosVenta.push({
                    id: producto.id,
                    nombre: producto.nombre + ' ' + (producto.concentracion || ''),
                    cantidad: 1,
                    precioUnitario: parseFloat(producto.precioVenta),
                    descuento: 0,
                    importe: parseFloat(producto.precioVenta),
                    stockMax: parseInt(producto.stock)
                });
            }
            
            renderizarProductos();
            calcularTotales();
            document.getElementById('searchInput').focus();
        }

        function cargarProductosRapidos() {
            fetch(contextPath + '/VentaController?action=buscarProductos&termino=pa')
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    var contenedor = document.getElementById('quickProducts');
                    if (!data || data.length === 0) {
                        contenedor.innerHTML = '<div class="text-muted small p-2">Busca productos para agregarlos al carrito.</div>';
                        return;
                    }
                    var html = '';
                    for (var i = 0; i < Math.min(data.length, 8); i++) {
                        var p = data[i];
                        html += '<div class="quick-product-card" data-index="' + i + '">' +
                            '<div class="quick-product-name">' + escapeHtml(p.nombre || 'Producto') + '</div>' +
                            '<div class="quick-product-meta">' + escapeHtml(p.concentracion || p.laboratorio || 'Disponible') + ' · Stock ' + (p.stock || 0) + '</div>' +
                            '<div class="quick-product-price">S/ ' + parseFloat(p.precioVenta || 0).toFixed(2) + '</div>' +
                        '</div>';
                    }
                    contenedor.innerHTML = html;
                    contenedor.querySelectorAll('.quick-product-card').forEach(function(card) {
                        card.addEventListener('click', function() {
                            agregarProductoDesdeData(data[parseInt(this.getAttribute('data-index'))]);
                        });
                    });
                })
                .catch(function() {
                    document.getElementById('quickProducts').innerHTML = '<div class="text-muted small p-2">No se pudieron cargar productos rápidos.</div>';
                });
        }
        
        function renderizarProductos() {
            var lista = document.getElementById('productsList');
            
            if (productosVenta.length === 0) {
                lista.innerHTML = '<div class="empty-state">' +
                    '<i class="bi bi-cart3" style="font-size: 70px; color: #ddd;"></i>' +
                    '<h5>El carrito está vacío</h5>' +
                    '<p class="text-muted">Escanea, busca o selecciona productos rápidos</p>' +
                '</div>';
                return;
            }
            
            var html = '';
            for (var i = 0; i < productosVenta.length; i++) {
                var p = productosVenta[i];
                html += '<div class="cart-row">' +
                    '<button type="button" class="cart-remove" data-action="eliminar" data-index="' + i + '"><i class="bi bi-trash"></i></button>' +
                    '<div><div class="cart-name">' + escapeHtml(p.nombre) + '</div><div class="cart-sub">Precio incluido IGV</div></div>' +
                    '<div class="cart-qty" data-action="editar" data-index="' + i + '">' + p.cantidad + '</div>' +
                    '<div>S/ ' + p.precioUnitario.toFixed(2) + '</div>' +
                    '<div><strong>S/ ' + p.importe.toFixed(2) + '</strong></div>' +
                    '<div>' + p.stockMax + '</div>' +
                '</div>';
            }
            lista.innerHTML = html;
            
            lista.onclick = function(e) {
                var target = e.target.closest('[data-action]');
                if (!target) return;
                var action = target.getAttribute('data-action');
                var index = parseInt(target.getAttribute('data-index'));
                if (action === 'eliminar') eliminarProducto(index);
                else if (action === 'editar') editarCantidad(index);
            };
        }
        
        function calcularTotales() {
            totalVenta = 0;
            descuentoTotal = 0;
            
            for (var i = 0; i < productosVenta.length; i++) {
                totalVenta += productosVenta[i].importe;
                descuentoTotal += productosVenta[i].descuento * productosVenta[i].cantidad;
            }
            
            subtotalVenta = totalVenta / 1.18;
            igvVenta = totalVenta - subtotalVenta;
            
            document.getElementById('subtotalAmount').textContent = subtotalVenta.toFixed(2);
            document.getElementById('igvAmount').textContent = igvVenta.toFixed(2);
            document.getElementById('totalAmount').textContent = totalVenta.toFixed(2);
            document.getElementById('descuentoAmount').textContent = descuentoTotal.toFixed(2);
            document.getElementById('calcTotal').textContent = totalVenta.toFixed(2);
            document.getElementById('cartCount').textContent = productosVenta.length + (productosVenta.length === 1 ? ' item' : ' items');
            document.getElementById('sidePayButton').disabled = productosVenta.length === 0;
            calcularCambioRapido();
        }

        function vaciarCarrito() {
            if (productosVenta.length === 0) return;
            if (!confirm('¿Vaciar el carrito de venta?')) return;
            productosVenta = [];
            renderizarProductos();
            calcularTotales();
        }

        function sincronizarCliente() {
            var valor = document.getElementById('clientNameInput').value.trim();
            document.getElementById('clientName').textContent = valor || 'CLIENTES VARIOS';
        }

        function calcularCambioRapido() {
            var recibido = parseFloat(document.getElementById('sideMontoRecibido').value) || 0;
            var vuelto = recibido - totalVenta;
            document.getElementById('sideVuelto').textContent = (vuelto > 0 ? vuelto : 0).toFixed(2);
        }

        function prepararCobroDesdePanel() {
            sincronizarCliente();
            seleccionarMetodoPago(document.getElementById('sideMetodoPago').value);
            var comprobante = document.getElementById('sideComprobante').value;
            var radio = document.querySelector('input[name="tipoComp"][value="' + comprobante + '"]');
            if (radio) radio.checked = true;
            mostrarModalCobro();
            if (metodoPagoSeleccionado === 'EFECTIVO') {
                var recibido = document.getElementById('sideMontoRecibido').value;
                if (recibido) {
                    document.getElementById('montoEfectivo').value = recibido;
                    calcularCambio();
                }
            }
        }

        function escapeHtml(value) {
            return String(value == null ? '' : value).replace(/[&<>'"]/g, function(char) {
                return {'&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'}[char];
            });
        }
        
        function eliminarProducto(index) {
            productosVenta.splice(index, 1);
            renderizarProductos();
            calcularTotales();
        }
        
        function editarCantidad(index) {
            productoEditandoIndex = index;
            var p = productosVenta[index];
            
            document.getElementById('modalProductoNombre').textContent = p.nombre;
            document.getElementById('modalCantidadInput').value = p.cantidad;
            document.getElementById('modalCantidadInput').setAttribute('max', p.stockMax);
            document.getElementById('modalStockInfo').textContent = 'Stock disponible: ' + p.stockMax;
            
            modalCantidadInstance = new bootstrap.Modal(document.getElementById('modalCantidad'));
            modalCantidadInstance.show();
        }
        
        function confirmarCantidad() {
            var nuevaCantidad = parseInt(document.getElementById('modalCantidadInput').value);
            var p = productosVenta[productoEditandoIndex];
            
            if (isNaN(nuevaCantidad) || nuevaCantidad < 1) {
                alert('La cantidad debe ser al menos 1');
                return;
            }
            if (nuevaCantidad > p.stockMax) {
                alert('Stock insuficiente. Disponible: ' + p.stockMax);
                return;
            }
            
            p.cantidad = nuevaCantidad;
            p.importe = p.cantidad * p.precioUnitario;
            
            if (modalCantidadInstance) modalCantidadInstance.hide();
            renderizarProductos();
            calcularTotales();
        }
        
        // =====================================================
        //              MODAL DE COBRO
        // =====================================================
        function mostrarModalCobro() {
            if (productosVenta.length === 0) {
                alert('Agregue al menos un producto');
                return;
            }
            
            document.getElementById('cobroTotal').textContent = totalVenta.toFixed(2);
            document.getElementById('resumenItems').textContent = productosVenta.length;
            document.getElementById('resumenSubtotal').textContent = subtotalVenta.toFixed(2);
            document.getElementById('resumenIGV').textContent = igvVenta.toFixed(2);
            document.getElementById('resumenDescuento').textContent = descuentoTotal.toFixed(2);
            document.getElementById('resumenTotal').textContent = totalVenta.toFixed(2);
            
            seleccionarMetodoPago('EFECTIVO');
            document.getElementById('montoEfectivo').value = '';
            document.getElementById('cambioAmount').textContent = 'S/. 0.00';
            document.getElementById('restanteText').textContent = '';
            
            new bootstrap.Modal(document.getElementById('modalCobro')).show();
            setTimeout(function() { document.getElementById('montoEfectivo').focus(); }, 500);
        }
        
        function seleccionarMetodoPago(metodo) {
            metodoPagoSeleccionado = metodo;
            
            document.querySelectorAll('.metodo-pago-btn').forEach(function(btn) {
                btn.classList.remove('active');
                if (btn.getAttribute('data-metodo') === metodo) btn.classList.add('active');
            });
            
            if (metodo === 'EFECTIVO') {
                document.getElementById('seccionEfectivo').style.display = 'block';
                document.getElementById('seccionVirtual').style.display = 'none';
            } else {
                document.getElementById('seccionEfectivo').style.display = 'none';
                document.getElementById('seccionVirtual').style.display = 'block';
                document.getElementById('metodoVirtualNombre').textContent = metodo;
                document.getElementById('montoVirtualDisplay').textContent = totalVenta.toFixed(2);
            }
        }
        
        function setEfectivo(monto) {
            var actual = parseFloat(document.getElementById('montoEfectivo').value) || 0;
            document.getElementById('montoEfectivo').value = (actual + monto).toFixed(2);
            calcularCambio();
        }
        
        function setMontoExacto() {
            document.getElementById('montoEfectivo').value = totalVenta.toFixed(2);
            calcularCambio();
        }
        
        function calcularCambio() {
            var efectivo = parseFloat(document.getElementById('montoEfectivo').value) || 0;
            var cambio = efectivo - totalVenta;
            
            if (cambio >= 0) {
                document.getElementById('cambioAmount').textContent = 'S/. ' + cambio.toFixed(2);
                document.getElementById('cambioAmount').className = 'text-primary mb-0';
                document.getElementById('restanteText').textContent = '';
            } else {
                document.getElementById('cambioAmount').textContent = 'S/. 0.00';
                document.getElementById('cambioAmount').className = 'mb-0';
                document.getElementById('restanteText').textContent = 'Falta: S/. ' + Math.abs(cambio).toFixed(2);
            }
        }
        
        function procesarVenta() {
            var montoEfectivo = 0;
            var montoVirtual = 0;
            var medioPagoVirtual = '';
            
            if (metodoPagoSeleccionado === 'EFECTIVO') {
                montoEfectivo = parseFloat(document.getElementById('montoEfectivo').value) || 0;
                if (montoEfectivo < totalVenta) {
                    alert('Monto insuficiente');
                    document.getElementById('montoEfectivo').focus();
                    return;
                }
            } else {
                montoVirtual = totalVenta;
                medioPagoVirtual = metodoPagoSeleccionado;
            }
            
            var tipoComp = document.querySelector('input[name="tipoComp"]:checked').value;
            
            var productos = productosVenta.map(function(p) {
                return {
                    productoId: p.id,
                    cantidad: p.cantidad,
                    precioUnitario: p.precioUnitario.toFixed(2),
                    descuento: p.descuento.toFixed(2)
                };
            });
            
            var btn = document.getElementById('btnConfirmarVenta');
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Procesando...';
            
            var params = new URLSearchParams();
            params.append('action', 'procesarVenta');
            params.append('productos', JSON.stringify(productos));
            params.append('montoEfectivo', montoEfectivo);
            params.append('montoVirtual', montoVirtual);
            params.append('medioPagoVirtual', medioPagoVirtual);
            params.append('tipoComprobante', tipoComp);
            params.append('cliente', document.getElementById('clientName').textContent);
            params.append('total', totalVenta.toFixed(2));
            
            fetch(contextPath + '/VentaController', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: params.toString()
            })
            .then(function(r) { return r.json(); })
            .then(function(data) {
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-check-circle"></i> Confirmar Venta';
                
                if (data.success) {
                    bootstrap.Modal.getInstance(document.getElementById('modalCobro')).hide();
                    mostrarTicketExitoso(data, montoEfectivo, tipoComp);
                } else {
                    alert(data.message || 'Error al procesar la venta');
                }
            })
            .catch(function(err) { 
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-check-circle"></i> Confirmar Venta';
                alert('Error: ' + err); 
            });
        }
        
        function mostrarTicketExitoso(data, montoEfectivo, tipoComprobante) {
            var vuelto = parseFloat(data.vuelto) || 0;
            var total = parseFloat(data.total);
            
            document.getElementById('exitoTotalGrande').textContent = 'S/ ' + total.toFixed(2);
            
            if (metodoPagoSeleccionado === 'EFECTIVO' && vuelto > 0) {
                document.getElementById('exitoVueltoSection').style.display = 'block';
                document.getElementById('exitoVueltoGrande').textContent = 'S/ ' + vuelto.toFixed(2);
            } else {
                document.getElementById('exitoVueltoSection').style.display = 'none';
            }
            
            generarTicketHTML(data, montoEfectivo, tipoComprobante, vuelto);
            new bootstrap.Modal(document.getElementById('modalExito')).show();
        }
        
        function generarTicketHTML(data, montoEfectivo, tipoComprobante, vuelto) {
            var fecha = new Date();
            var fechaStr = fecha.toLocaleDateString('es-PE') + ' ' + fecha.toLocaleTimeString('es-PE');
            var tipoCompTexto = tipoComprobante === 'BOLETA' ? 'BOLETA DE VENTA' : 
                               tipoComprobante === 'FACTURA' ? 'FACTURA' : 'NOTA DE VENTA';
            
            var html = '<div class="ticket-header">' +
                '<div class="ticket-logo">BOTICA ECONOSALUD</div>' +
                '<div style="font-size: 10px;">RUC: 20123456789</div>' +
                '<div style="font-weight: bold; margin-top: 5px;">' + tipoCompTexto + '</div>' +
                '<div>' + data.numeroTransaccion + '</div>' +
            '</div>' +
            '<div class="ticket-divider"></div>' +
            '<div style="font-size: 11px;">' +
                '<div>Cajero: ' + nombreCajero + '</div>' +
                '<div>Fecha: ' + fechaStr + '</div>' +
                '<div>Cliente: ' + document.getElementById('clientName').textContent + '</div>' +
            '</div>' +
            '<div class="ticket-divider"></div>' +
            '<div class="ticket-item-header"><span>Cant</span><span>Producto</span><span>P.Unit</span><span>Imp.</span></div>';
            
            for (var i = 0; i < productosVenta.length; i++) {
                var p = productosVenta[i];
                html += '<div class="ticket-item-row">' +
                    '<span>' + p.cantidad + '</span>' +
                    '<span style="font-size: 10px;">' + p.nombre.substring(0, 18) + '</span>' +
                    '<span>' + p.precioUnitario.toFixed(2) + '</span>' +
                    '<span>' + p.importe.toFixed(2) + '</span>' +
                '</div>';
            }
            
            html += '<div class="ticket-divider"></div>' +
                '<div class="ticket-row ticket-total"><span>TOTAL:</span><span>S/.' + totalVenta.toFixed(2) + '</span></div>';
            
            if (metodoPagoSeleccionado === 'EFECTIVO') {
                html += '<div class="ticket-row"><span>Efectivo:</span><span>S/.' + montoEfectivo.toFixed(2) + '</span></div>' +
                    '<div class="ticket-row"><span>Cambio:</span><span>S/.' + vuelto.toFixed(2) + '</span></div>';
            } else {
                html += '<div class="ticket-row"><span>' + metodoPagoSeleccionado + ':</span><span>S/.' + totalVenta.toFixed(2) + '</span></div>';
            }
            
            html += '<div class="ticket-divider"></div>' +
                '<div class="ticket-footer"><p style="font-weight: bold;">GRACIAS POR SU COMPRA</p></div>';
            
            document.getElementById('ticketContent').innerHTML = html;
        }
        
        function imprimirTicket() {
            var contenido = document.getElementById('ticketContent').innerHTML;
            var ventana = window.open('', '_blank', 'width=320,height=600');
            ventana.document.write('<html><head><title>Ticket</title><style>' +
                'body { font-family: "Courier New", monospace; font-size: 12px; margin: 0; padding: 10px; }' +
                '.ticket-header { text-align: center; }' +
                '.ticket-logo { font-size: 18px; font-weight: bold; }' +
                '.ticket-divider { border-top: 1px dashed #333; margin: 8px 0; }' +
                '.ticket-row { display: flex; justify-content: space-between; }' +
                '.ticket-total { font-weight: bold; }' +
                '.ticket-footer { text-align: center; font-size: 10px; }' +
                '.ticket-item-header, .ticket-item-row { display: grid; grid-template-columns: 30px 1fr 50px 50px; font-size: 11px; }' +
            '</style></head><body>' + contenido + '</body></html>');
            ventana.document.close();
            ventana.print();
        }
        
        function nuevaVenta() {
            bootstrap.Modal.getInstance(document.getElementById('modalExito')).hide();
            productosVenta = [];
            renderizarProductos();
            calcularTotales();
            document.getElementById('searchInput').focus();
        }
    </script>
</body>
</html>
