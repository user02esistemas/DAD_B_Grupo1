<%-- 
    Document   : nueva
    Created on : 06 dic. 2025
    Author     : Sistema Botica
    Description: Formulario de nueva compra con búsqueda asincrónica
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("pageTitle", "Nueva Compra - Sistema Botica");
%>
<%@ include file="/WEB-INF/includes/head.jsp" %>

<!-- CSS específico para compras -->
<style>
    /* Contenedor principal */
    .compra-container {
        display: flex;
        gap: 20px;
        height: calc(100vh - 140px);
    }

    /* Panel izquierdo - Lista de productos */
    .panel-productos {
        flex: 1;
        display: flex;
        flex-direction: column;
        background: #fff;
        border-radius: 10px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        overflow: hidden;
    }

    /* Panel derecho - Resumen */
    .panel-resumen {
        width: 350px;
        background: #fff;
        border-radius: 10px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        display: flex;
        flex-direction: column;
    }

    /* Barra de búsqueda */
    .search-bar {
        padding: 15px;
        background: linear-gradient(135deg, #035b77 0%, #0b7896 100%);
        border-radius: 10px 10px 0 0;
    }

    .search-input-container {
        position: relative;
    }

    .search-input {
        width: 100%;
        padding: 12px 20px 12px 45px;
        border: none;
        border-radius: 25px;
        font-size: 16px;
        outline: none;
        transition: box-shadow 0.3s;
    }

    .search-input:focus {
        box-shadow: 0 0 0 3px rgba(255,255,255,0.3);
    }

    .search-icon {
        position: absolute;
        left: 15px;
        top: 50%;
        transform: translateY(-50%);
        color: #666;
        font-size: 18px;
    }

    /* Autocomplete dropdown */
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

    .autocomplete-item:hover, .autocomplete-item.active {
        background: #eef8fb;
    }

    .autocomplete-item:last-child {
        border-bottom: none;
    }

    .producto-nombre {
        font-weight: 600;
        color: #333;
        margin-bottom: 3px;
    }

    .producto-detalle {
        font-size: 12px;
        color: #666;
    }

    .producto-laboratorio {
        font-size: 11px;
        color: #999;
    }

    /* Opciones de compra */
    .opciones-compra {
        display: flex;
        gap: 15px;
        padding: 10px 15px;
        background: #f8f9fa;
        border-bottom: 1px solid #eee;
        flex-wrap: wrap;
        align-items: center;
    }

    .opcion-toggle {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 13px;
        color: #555;
    }

    .form-check-input:checked {
        background-color: #035b77;
        border-color: #035b77;
    }

    /* Tabla de productos */
    .tabla-productos-container {
        flex: 1;
        overflow-y: auto;
        padding: 0;
    }

    .tabla-productos {
        width: 100%;
        border-collapse: collapse;
    }

    .tabla-productos th {
        background: #f8f9fa;
        padding: 12px 15px;
        text-align: left;
        font-weight: 600;
        color: #555;
        font-size: 13px;
        position: sticky;
        top: 0;
        z-index: 10;
        border-bottom: 2px solid #eee;
    }

    .tabla-productos td {
        padding: 12px 15px;
        border-bottom: 1px solid #eee;
        vertical-align: middle;
    }

    .tabla-productos tr:hover {
        background: #f8f9fa;
    }

    .btn-eliminar-item {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        border: none;
        background: #fee;
        color: #d33;
        cursor: pointer;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .btn-eliminar-item:hover {
        background: #d33;
        color: #fff;
    }

    /* Estado vacío */
    .estado-vacio {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #999;
        padding: 40px;
    }

    .estado-vacio img {
        width: 150px;
        opacity: 0.7;
        margin-bottom: 20px;
    }

    .estado-vacio h4 {
        color: #666;
        margin-bottom: 10px;
    }

    /* Panel resumen */
    .resumen-header {
        padding: 20px;
        background: linear-gradient(135deg, #035b77 0%, #0b7896 100%);
        color: #fff;
        border-radius: 10px 10px 0 0;
    }

    .total-compra {
        font-size: 32px;
        font-weight: 700;
    }

    .resumen-body {
        flex: 1;
        padding: 20px;
        overflow-y: auto;
    }

    .resumen-item {
        margin-bottom: 15px;
    }

    .resumen-item label {
        display: block;
        font-size: 12px;
        color: #666;
        margin-bottom: 5px;
    }

    .resumen-item input, .resumen-item select {
        width: 100%;
        padding: 10px 12px;
        border: 1px solid #ddd;
        border-radius: 8px;
        font-size: 14px;
    }

    .resumen-item input:focus, .resumen-item select:focus {
        border-color: #035b77;
        outline: none;
        box-shadow: 0 0 0 3px rgba(3, 91, 119, 0.12);
    }

    .detalle-totales {
        background: #f8f9fa;
        border-radius: 8px;
        padding: 15px;
        margin-bottom: 15px;
    }

    .linea-total {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        font-size: 14px;
    }

    .linea-total.total-final {
        font-weight: 700;
        font-size: 18px;
        color: #035b77;
        border-top: 1px solid #ddd;
        padding-top: 10px;
        margin-top: 10px;
    }

    .resumen-footer {
        padding: 20px;
        border-top: 1px solid #eee;
    }

    .btn-confirmar {
        width: 100%;
        padding: 15px;
        background: linear-gradient(135deg, #035b77 0%, #0b7896 100%);
        color: #fff;
        border: none;
        border-radius: 10px;
        font-size: 16px;
        font-weight: 600;
        cursor: pointer;
        transition: transform 0.2s, box-shadow 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
    }

    .btn-confirmar:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 5px 20px rgba(3, 91, 119, 0.25);
    }

    .btn-confirmar:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    /* Acciones rápidas */
    .acciones-rapidas {
        display: flex;
        gap: 10px;
        padding: 10px 15px;
        background: #f8f9fa;
        border-top: 1px solid #eee;
    }

    .btn-accion {
        padding: 8px 15px;
        border: 1px solid #ddd;
        border-radius: 6px;
        background: #fff;
        font-size: 13px;
        cursor: pointer;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        gap: 5px;
    }

    .btn-accion:hover {
        border-color: #035b77;
        color: #035b77;
    }

    /* Modal de producto */
    .modal-producto .modal-content {
        border-radius: 15px;
        border: none;
    }

    .modal-producto .modal-header {
        background: linear-gradient(135deg, #035b77 0%, #0b7896 100%);
        color: #fff;
        border-radius: 15px 15px 0 0;
        padding: 20px;
    }

    .modal-producto .modal-header .btn-close {
        filter: brightness(0) invert(1);
    }

    .info-producto-actual {
        background: #f8f9fa;
        border-radius: 10px;
        padding: 15px;
        margin-bottom: 20px;
        text-align: center;
    }

    .stock-actual {
        font-size: 14px;
        color: #666;
    }

    .stock-actual strong {
        color: #035b77;
        font-size: 18px;
    }

    .cantidad-presentacion {
        font-size: 13px;
        color: #888;
        margin-top: 5px;
    }

    .form-compra-producto {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 15px;
    }

    .form-compra-producto .campo-full {
        grid-column: span 2;
    }

    .seccion-precios {
        background: #eef8fb;
        border-radius: 10px;
        padding: 15px;
        margin-top: 10px;
        border: 1px dashed #035b77;
    }

    .seccion-precios h6 {
        color: #035b77;
        margin-bottom: 15px;
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .precio-sugerido {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 8px 0;
        border-bottom: 1px solid #ddd;
    }

    .precio-sugerido:last-child {
        border-bottom: none;
    }

    .precio-sugerido .valor {
        font-weight: 600;
        color: #035b77;
    }

    .btn-aplicar-precio {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        border: none;
        background: #035b77;
        color: #fff;
        cursor: pointer;
        font-size: 12px;
    }

    .input-ganancia {
        width: 80px;
        padding: 5px 10px;
        border: 1px solid #ddd;
        border-radius: 5px;
        text-align: center;
    }

    /* Loading spinner */
    .loading-spinner {
        display: none;
        text-align: center;
        padding: 20px;
    }

    .loading-spinner.show {
        display: block;
    }

    .spinner {
        width: 40px;
        height: 40px;
        border: 3px solid #f3f3f3;
        border-top: 3px solid #035b77;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin: 0 auto;
    }

/*    @keyframes spin {
        0% {
            transform: rotate(0deg);
        }
        100% {
            transform: rotate(360deg);
        }
    }*/

    /* Toast notification */
    .toast-container {
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
    }

    /* Responsive */
    @media (max-width: 992px) {
        .compra-container {
            flex-direction: column;
            height: auto;
        }

        .panel-resumen {
            width: 100%;
        }
    }
</style>

<%@ include file="/WEB-INF/includes/navbar.jsp" %>

<div class="container-fluid">
    <div class="row">
        <%@ include file="/WEB-INF/includes/sidebar.jsp" %>

        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4 py-3">

            <!-- Encabezado -->
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                    <h4 class="mb-0"><i class="bi bi-cart-plus me-2"></i>Nueva Compra</h4>
                    <small class="text-muted">Registra la compra de productos al inventario</small>
                </div>
                <a href="<%= request.getContextPath()%>/admin/compras/listar.jsp" class="btn btn-outline-secondary">
                    <i class="bi bi-list-ul me-1"></i>Ver Historial
                </a>
            </div>

            <!-- Contenedor principal -->
            <div class="compra-container">

                <!-- Panel Izquierdo - Productos -->
                <div class="panel-productos">

                    <!-- Barra de búsqueda -->
                    <div class="search-bar">
                        <div class="search-input-container">
                            <i class="bi bi-search search-icon"></i>
                            <input type="text" 
                                   id="buscarProducto" 
                                   class="search-input" 
                                   placeholder="Buscar medicamento por nombre, principio activo o código..."
                                   autocomplete="off">
                            <div id="autocompleteDropdown" class="autocomplete-dropdown"></div>
                        </div>
                    </div>

                    <!-- Opciones -->
                    <div class="opciones-compra">
                        <div class="opcion-toggle">
                            <div class="form-check form-switch">
                                <input class="form-check-input" type="checkbox" id="precioIncluyeIgv">
                                <label class="form-check-label" for="precioIncluyeIgv">Precio incluye IGV</label>
                            </div>
                        </div>
                        <div class="opcion-toggle">
                            <div class="form-check form-switch">
                                <input class="form-check-input" type="checkbox" id="registrarLote" checked>
                                <label class="form-check-label" for="registrarLote">Ingreso de Lotes</label>
                            </div>
                        </div>
                    </div>

                    <!-- Tabla de productos agregados -->
                    <div class="tabla-productos-container" id="tablaContainer">
                        <!-- Estado vacío inicial -->
                        <div class="estado-vacio" id="estadoVacio">
                            <svg width="150" height="150" viewBox="0 0 200 200" fill="none">
                            <circle cx="100" cy="100" r="80" fill="#eef8fb"/>
                            <rect x="70" y="60" width="60" height="80" rx="5" fill="#035b77" opacity="0.2"/>
                            <rect x="80" y="70" width="40" height="8" rx="2" fill="#035b77" opacity="0.4"/>
                            <rect x="80" y="85" width="30" height="6" rx="2" fill="#035b77" opacity="0.3"/>
                            <circle cx="130" cy="130" r="30" fill="#0b7896" opacity="0.3"/>
                            <text x="122" y="138" font-size="30" fill="#035b77">+</text>
                            </svg>
                            <h4>Busca un medicamento para empezar</h4>
                            <p>Escribe en el buscador para agregar productos a la compra</p>
                        </div>

                        <!-- Tabla (oculta inicialmente) -->
                        <table class="tabla-productos" id="tablaProductos" style="display: none;">
                            <thead>
                                <tr>
                                    <th width="40"></th>
                                    <th>Descripción</th>
                                    <th width="100">Cantidad</th>
                                    <th width="100">Costo Unit.</th>
                                    <th width="100">Total</th>
                                    <th width="100">Lote</th>
                                    <th width="110">F. Venc.</th>
                                </tr>
                            </thead>
                            <tbody id="listaProductos">
                                <!-- Los productos se agregan aquí dinámicamente -->
                            </tbody>
                        </table>
                    </div>

                    <!-- Acciones rápidas -->
                    <div class="acciones-rapidas">
                        <button class="btn-accion" onclick="limpiarLista()">
                            <i class="bi bi-trash"></i> Limpiar Lista
                        </button>
                    </div>
                </div>

                <!-- Panel Derecho - Resumen -->
                <div class="panel-resumen">
                    <div class="resumen-header">
                        <small>Total de Compra</small>
                        <div class="total-compra">S/ <span id="totalGeneral">0.00</span></div>
                    </div>

                    <div class="resumen-body">
                        <!-- Comprobante -->
                        <div class="resumen-item">
                            <label><i class="bi bi-receipt me-1"></i>Comprobante de compra</label>
                            <input type="text" id="numeroComprobante" placeholder="Ej: F001-00001234">
                        </div>

                        <!-- Proveedor -->
                        <div class="resumen-item">
                            <label><i class="bi bi-building me-1"></i>Proveedor</label>
                            <select id="proveedorSelect" class="form-select">
                                <option value="">Seleccionar proveedor...</option>
                            </select>
                            <button type="button" class="btn btn-link btn-sm p-0 mt-1" data-bs-toggle="modal" data-bs-target="#modalNuevoProveedor">
                                <i class="bi bi-plus-circle"></i> Agregar nuevo proveedor
                            </button>
                        </div>

                        <!-- Detalle de totales -->
                        <div class="detalle-totales">
                            <div class="linea-total">
                                <span>Subtotal:</span>
                                <span>S/ <span id="subtotalCompra">0.00</span></span>
                            </div>
                            <div class="linea-total">
                                <span>IGV (18%):</span>
                                <span>S/ <span id="igvCompra">0.00</span></span>
                            </div>
                            <div class="linea-total total-final">
                                <span>TOTAL:</span>
                                <span>S/ <span id="totalCompraFinal">0.00</span></span>
                            </div>
                        </div>

                        <!-- Observaciones -->
                        <div class="resumen-item">
                            <label><i class="bi bi-chat-text me-1"></i>Observaciones</label>
                            <textarea id="observaciones" class="form-control" rows="2" placeholder="Comentarios adicionales..."></textarea>
                        </div>
                    </div>

                    <div class="resumen-footer">
                        <button class="btn-confirmar" id="btnConfirmarCompra" disabled onclick="confirmarCompra()">
                            <i class="bi bi-check-circle"></i>
                            CONFIRMAR COMPRA
                        </button>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<!-- Modal Agregar Producto -->
<div class="modal fade modal-producto" id="modalProducto" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="modalProductoTitulo">Agregar Producto</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <!-- Info del producto -->
                <div class="info-producto-actual">
                    <div class="stock-actual">
                        Stock actual: <strong id="stockActualModal">0</strong> unidades
                    </div>
                    <div class="cantidad-presentacion" id="cantidadPresentacion">
                        Por cada caja vienen X unidades
                    </div>
                </div>

                <input type="hidden" id="catalogoProductoId">
                <input type="hidden" id="productoNombre">
                <input type="hidden" id="productoConcentracion">

                <div class="form-compra-producto">
                    <!-- Cantidad -->
                    <div>
                        <label class="form-label">Cantidad (unidades)</label>
                        <input type="number" class="form-control" id="cantidadCompra" min="1" value="1">
                    </div>

                    <!-- Costo Total -->
                    <div>
                        <label class="form-label">Costo Total (S/)</label>
                        <input type="number" class="form-control" id="costoTotal" step="0.01" min="0" placeholder="0.00">
                        <small class="text-muted" id="labelIgvModal">No incluye el IGV en factura</small>
                    </div>

                    <!-- Costo Unitario (calculado) -->
                    <div>
                        <label class="form-label">Costo Unitario (S/)</label>
                        <input type="number" class="form-control" id="costoUnitario" step="0.01" readonly>
                    </div>

                    <!-- Precio Venta -->
                    <div>
                        <label class="form-label">Precio Venta (S/)</label>
                        <input type="number" class="form-control" id="precioVenta" step="0.01" min="0" placeholder="0.00">
                    </div>

                    <!-- Lote -->
                    <div id="campoLote">
                        <label class="form-label">Lote *</label>
                        <input type="text" class="form-control" id="loteProducto" placeholder="Ej: A23456R">
                    </div>

                    <!-- Fecha Vencimiento -->
                    <div id="campoVencimiento">
                        <label class="form-label">Fecha Vencimiento *</label>
                        <input type="date" class="form-control" id="fechaVencimiento">
                    </div>

                    <!-- Sección de precios sugeridos -->
                    <div class="campo-full">
                        <div class="seccion-precios">
                            <h6><i class="bi bi-calculator"></i> Calcular Precio de Venta</h6>
                            <div class="d-flex align-items-center gap-3 mb-3">
                                <span>Ganancia:</span>
                                <input type="number" class="input-ganancia" id="porcentajeGanancia" value="30" min="0" max="500">
                                <span>%</span>
                                <button type="button" class="btn btn-sm btn-outline-success" onclick="calcularPrecioSugerido()">
                                    <i class="bi bi-arrow-repeat"></i> Calcular
                                </button>
                            </div>
                            <div class="precio-sugerido">
                                <span>P. Venta Sugerido:</span>
                                <span class="valor" id="precioSugerido">S/ 0.00</span>
                                <button type="button" class="btn-aplicar-precio" onclick="aplicarPrecioSugerido()" title="Aplicar">
                                    <i class="bi bi-check"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn btn-primary" onclick="agregarProductoALista()">
                    <i class="bi bi-plus-circle me-1"></i>Confirmar
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Modal Nuevo Proveedor -->
<div class="modal fade" id="modalNuevoProveedor" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="bi bi-building me-2"></i>Nuevo Proveedor</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="mb-3">
                    <label class="form-label">RUC *</label>
                    <input type="text" class="form-control" id="proveedorRuc" maxlength="11" placeholder="20123456789">
                </div>
                <div class="mb-3">
                    <label class="form-label">Razón Social *</label>
                    <input type="text" class="form-control" id="proveedorRazonSocial" placeholder="Nombre de la empresa">
                </div>
                <div class="mb-3">
                    <label class="form-label">Contacto</label>
                    <input type="text" class="form-control" id="proveedorContacto" placeholder="Nombre del contacto">
                </div>
                <div class="mb-3">
                    <label class="form-label">Teléfono</label>
                    <input type="text" class="form-control" id="proveedorTelefono" placeholder="999999999">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn btn-primary" onclick="guardarProveedor()">
                    <i class="bi bi-save me-1"></i>Guardar
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Toast de notificaciones -->
<div class="toast-container">
    <div id="toastNotificacion" class="toast" role="alert">
        <div class="toast-header">
            <i class="bi bi-check-circle text-primary me-2" id="toastIcon"></i>
            <strong class="me-auto" id="toastTitulo">Notificación</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
        </div>
        <div class="toast-body" id="toastMensaje"></div>
    </div>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>

<script>
// =====================================================
//          VARIABLES GLOBALES
// =====================================================
    const contextPath = '<%= request.getContextPath()%>';
    let productosCompra = [];
    let productoSeleccionado = null;
    let timeoutBusqueda = null;
    let indiceAutocompletado = -1;

// =====================================================
//          INICIALIZACIÓN
// =====================================================
    document.addEventListener('DOMContentLoaded', function () {
        cargarProveedores();
        configurarBusqueda();
        configurarEventosModal();

        // Configurar fecha mínima de vencimiento (hoy)
        const hoy = new Date().toISOString().split('T')[0];
        document.getElementById('fechaVencimiento').min = hoy;
    });

// =====================================================
//          BÚSQUEDA DE PRODUCTOS (AUTOCOMPLETE)
// =====================================================
    function configurarBusqueda() {
        const inputBusqueda = document.getElementById('buscarProducto');
        const dropdown = document.getElementById('autocompleteDropdown');

        // Evento de escritura con debounce
        inputBusqueda.addEventListener('input', function () {
            clearTimeout(timeoutBusqueda);
            const termino = this.value.trim();

            if (termino.length < 2) {
                dropdown.classList.remove('show');
                return;
            }

            // Debounce de 300ms para no sobrecargar el servidor
            timeoutBusqueda = setTimeout(() => {
                buscarProductos(termino);
            }, 300);
        });

        // Navegación con teclado
        inputBusqueda.addEventListener('keydown', function (e) {
            const items = dropdown.querySelectorAll('.autocomplete-item');

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                indiceAutocompletado = Math.min(indiceAutocompletado + 1, items.length - 1);
                actualizarSeleccionAutocomplete(items);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                indiceAutocompletado = Math.max(indiceAutocompletado - 1, 0);
                actualizarSeleccionAutocomplete(items);
            } else if (e.key === 'Enter') {
                e.preventDefault();
                if (indiceAutocompletado >= 0 && items[indiceAutocompletado]) {
                    items[indiceAutocompletado].click();
                }
            } else if (e.key === 'Escape') {
                dropdown.classList.remove('show');
            }
        });

        // Cerrar dropdown al hacer clic fuera
        document.addEventListener('click', function (e) {
            if (!inputBusqueda.contains(e.target) && !dropdown.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });
    }

    function buscarProductos(termino) {
        const dropdown = document.getElementById('autocompleteDropdown');

        fetch(contextPath + '/CompraController?action=buscarProducto&q=' + encodeURIComponent(termino))
                .then(response => response.json())
                .then(productos => {
                    if (productos.length === 0) {
                        dropdown.innerHTML = '<div class="autocomplete-item text-muted">No se encontraron resultados</div>';
                    } else {
                        dropdown.innerHTML = productos.map(function (p, index) {
                            var jsonStr = JSON.stringify(p).replace(/'/g, "&#39;");
                            return '<div class="autocomplete-item" data-id="' + p.id + '" data-producto=\'' + jsonStr + '\'>' +
                                    '<div class="producto-nombre">' + (p.nombre || 'Sin nombre') + '</div>' +
                                    '<div class="producto-detalle">' +
                                    (p.principioActivo || '') + ' ' + (p.concentracion || '') + ' - ' + (p.formaFarmaceutica || '') +
                                    '</div>' +
                                    '<div class="producto-laboratorio">' + (p.laboratorio || '') + '</div>' +
                                    '</div>';
                        }).join('');

                        // Agregar eventos de clic
                        dropdown.querySelectorAll('.autocomplete-item[data-id]').forEach(item => {
                            item.addEventListener('click', function () {
                                const producto = JSON.parse(this.dataset.producto);
                                seleccionarProducto(producto);
                            });
                        });
                    }

                    dropdown.classList.add('show');
                    indiceAutocompletado = -1;
                })
                .catch(error => {
                    console.error('Error en búsqueda:', error);
                    dropdown.innerHTML = '<div class="autocomplete-item text-danger">Error al buscar</div>';
                    dropdown.classList.add('show');
                });
    }

    function actualizarSeleccionAutocomplete(items) {
        items.forEach((item, i) => {
            item.classList.toggle('active', i === indiceAutocompletado);
        });

        if (items[indiceAutocompletado]) {
            items[indiceAutocompletado].scrollIntoView({block: 'nearest'});
        }
    }

// =====================================================
//          SELECCIÓN DE PRODUCTO
// =====================================================
    function seleccionarProducto(producto) {
        productoSeleccionado = producto;

        // Cerrar dropdown y limpiar búsqueda
        document.getElementById('autocompleteDropdown').classList.remove('show');
        document.getElementById('buscarProducto').value = '';

        // Cargar información adicional del producto
        fetch(contextPath + '/CompraController?action=obtenerInfoStock&catalogoId=' + producto.id)
                .then(response => response.json())
                .then(info => {
                    // Actualizar modal con datos
                    document.getElementById('modalProductoTitulo').textContent =
                            producto.nombre + ' (' + (producto.concentracion || '') + ')';
                    document.getElementById('catalogoProductoId').value = producto.id;
                    document.getElementById('productoNombre').value = producto.nombre;
                    document.getElementById('productoConcentracion').value = producto.concentracion || '';
                    document.getElementById('stockActualModal').textContent = info.stockTotal || 0;

                    const cantidadPres = producto.cantidad || 1;
                    document.getElementById('cantidadPresentacion').textContent =
                    'Por cada caja/presentaci\u00f3n vienen ' + cantidadPres + ' unidades';

                    // Resetear campos del formulario
                    document.getElementById('cantidadCompra').value = 1;
                    document.getElementById('costoTotal').value = '';
                    document.getElementById('costoUnitario').value = '';
                    document.getElementById('precioVenta').value = '';
                    document.getElementById('loteProducto').value = '';
                    document.getElementById('fechaVencimiento').value = '';
                    document.getElementById('precioSugerido').textContent = 'S/ 0.00';

                    // Si hay precio de compra anterior, sugerirlo
                    if (info.precioCompraPromedio && info.precioCompraPromedio > 0) {
                        document.getElementById('costoUnitario').value = parseFloat(info.precioCompraPromedio).toFixed(2);
                    }

                    // Mostrar/ocultar campos de lote según opción
                    const mostrarLote = document.getElementById('registrarLote').checked;
                    document.getElementById('campoLote').style.display = mostrarLote ? 'block' : 'none';
                    document.getElementById('campoVencimiento').style.display = mostrarLote ? 'block' : 'none';

                    // Actualizar label de IGV
                    const incluyeIgv = document.getElementById('precioIncluyeIgv').checked;
                    document.getElementById('labelIgvModal').textContent =
                            incluyeIgv ? 'El precio ya incluye IGV' : 'No incluye el IGV en factura';

                    // Abrir modal
                    const modal = new bootstrap.Modal(document.getElementById('modalProducto'));
                    modal.show();
                })
                .catch(error => {
                    console.error('Error al obtener info:', error);
                    mostrarToast('Error al cargar información del producto', 'error');
                });
    }

// =====================================================
//          EVENTOS DEL MODAL
// =====================================================
    function configurarEventosModal() {
        // Calcular costo unitario cuando cambia cantidad o costo total
        document.getElementById('cantidadCompra').addEventListener('input', calcularCostoUnitario);
        document.getElementById('costoTotal').addEventListener('input', calcularCostoUnitario);
    }

    function calcularCostoUnitario() {
        const cantidad = parseFloat(document.getElementById('cantidadCompra').value) || 0;
        const costoTotal = parseFloat(document.getElementById('costoTotal').value) || 0;

        if (cantidad > 0 && costoTotal > 0) {
            const costoUnitario = costoTotal / cantidad;
            document.getElementById('costoUnitario').value = costoUnitario.toFixed(2);
        } else {
            document.getElementById('costoUnitario').value = '';
        }
    }

    function calcularPrecioSugerido() {
        const costoUnitario = parseFloat(document.getElementById('costoUnitario').value) || 0;
        const porcentaje = parseFloat(document.getElementById('porcentajeGanancia').value) || 0;

        if (costoUnitario > 0) {
            const precioSugerido = costoUnitario * (1 + porcentaje / 100);
            document.getElementById('precioSugerido').textContent = 'S/ ' + precioSugerido.toFixed(2);
        }
    }

    function aplicarPrecioSugerido() {
        const texto = document.getElementById('precioSugerido').textContent;
        const precio = parseFloat(texto.replace('S/ ', '')) || 0;
        document.getElementById('precioVenta').value = precio.toFixed(2);
    }

// =====================================================
//          AGREGAR PRODUCTO A LA LISTA
// =====================================================
    function agregarProductoALista() {
        const registrarLote = document.getElementById('registrarLote').checked;

        // Validaciones
        const cantidad = parseInt(document.getElementById('cantidadCompra').value) || 0;
        const costoUnitario = parseFloat(document.getElementById('costoUnitario').value) || 0;
        const precioVenta = parseFloat(document.getElementById('precioVenta').value) || 0;
        const lote = document.getElementById('loteProducto').value.trim();
        const fechaVencimiento = document.getElementById('fechaVencimiento').value;

        if (cantidad <= 0) {
            mostrarToast('La cantidad debe ser mayor a 0', 'error');
            return;
        }

        if (costoUnitario <= 0) {
            mostrarToast('Ingrese el costo total para calcular el costo unitario', 'error');
            return;
        }

        if (precioVenta <= 0) {
            mostrarToast('Ingrese el precio de venta', 'error');
            return;
        }

        if (registrarLote) {
            if (!lote) {
                mostrarToast('Ingrese el número de lote', 'error');
                return;
            }
            if (!fechaVencimiento) {
                mostrarToast('Ingrese la fecha de vencimiento', 'error');
                return;
            }
        }

        // Crear objeto del producto
        const itemCompra = {
            id: Date.now(), // ID temporal único
            catalogoProductoId: parseInt(document.getElementById('catalogoProductoId').value),
            nombreComercial: document.getElementById('productoNombre').value,
            concentracion: document.getElementById('productoConcentracion').value,
            cantidad: cantidad,
            precioCompra: costoUnitario,
            precioVenta: precioVenta,
            lote: registrarLote ? lote : 'SIN-LOTE',
            fechaVencimiento: registrarLote ? fechaVencimiento : getFechaDefault(),
            subtotal: cantidad * costoUnitario
        };

        // Verificar si ya existe el mismo producto con mismo lote
        const existente = productosCompra.find(p =>
            p.catalogoProductoId === itemCompra.catalogoProductoId &&
                    p.lote === itemCompra.lote
        );

        if (existente) {
            // Actualizar cantidad
            existente.cantidad += itemCompra.cantidad;
            existente.subtotal = existente.cantidad * existente.precioCompra;
            mostrarToast('Cantidad actualizada para el producto existente', 'info');
        } else {
            productosCompra.push(itemCompra);
        }

        // Cerrar modal y actualizar vista
        bootstrap.Modal.getInstance(document.getElementById('modalProducto')).hide();
        actualizarTablaProductos();
        actualizarTotales();
    }

    function getFechaDefault() {
        // Fecha por defecto: 2 años desde hoy
        const fecha = new Date();
        fecha.setFullYear(fecha.getFullYear() + 2);
        return fecha.toISOString().split('T')[0];
    }

// =====================================================
//          ACTUALIZAR TABLA DE PRODUCTOS
// =====================================================
    function actualizarTablaProductos() {
        const tbody = document.getElementById('listaProductos');
        const tabla = document.getElementById('tablaProductos');
        const estadoVacio = document.getElementById('estadoVacio');

        if (productosCompra.length === 0) {
            tabla.style.display = 'none';
            estadoVacio.style.display = 'flex';
            document.getElementById('btnConfirmarCompra').disabled = true;
            return;
        }

        tabla.style.display = 'table';
        estadoVacio.style.display = 'none';
        document.getElementById('btnConfirmarCompra').disabled = false;

        tbody.innerHTML = productosCompra.map(function (p, index) {
            var concentracionHtml = p.concentracion ? '<small class="text-muted d-block">' + p.concentracion + '</small>' : '';
            return '<tr>' +
                    '<td>' +
                    '<button class="btn-eliminar-item" onclick="eliminarProducto(' + p.id + ')" title="Eliminar">' +
                    '<i class="bi bi-trash"></i>' +
                    '</button>' +
                    '</td>' +
                    '<td>' +
                    '<strong>' + p.nombreComercial + '</strong>' +
                    concentracionHtml +
                    '</td>' +
                    '<td class="text-center">' + p.cantidad + '</td>' +
                    '<td class="text-end">S/ ' + p.precioCompra.toFixed(2) + '</td>' +
                    '<td class="text-end"><strong>S/ ' + p.subtotal.toFixed(2) + '</strong></td>' +
                    '<td>' + p.lote + '</td>' +
                    '<td>' + formatearFecha(p.fechaVencimiento) + '</td>' +
                    '</tr>';
        }).join('');
    }

    function eliminarProducto(id) {
        productosCompra = productosCompra.filter(p => p.id !== id);
        actualizarTablaProductos();
        actualizarTotales();
    }

    function limpiarLista() {
        if (productosCompra.length === 0)
            return;

        if (confirm('¿Está seguro de limpiar toda la lista de productos?')) {
            productosCompra = [];
            actualizarTablaProductos();
            actualizarTotales();
        }
    }

// =====================================================
//          ACTUALIZAR TOTALES
// =====================================================
    function actualizarTotales() {
        const incluyeIgv = document.getElementById('precioIncluyeIgv').checked;

        let subtotalSinIgv = 0;
        let total = 0;

        productosCompra.forEach(p => {
            total += p.subtotal;
        });

        if (incluyeIgv) {
            // El precio ya incluye IGV
            subtotalSinIgv = total / 1.18;
        } else {
            subtotalSinIgv = total;
            total = subtotalSinIgv * 1.18;
        }

        const igv = total - subtotalSinIgv;

        document.getElementById('subtotalCompra').textContent = subtotalSinIgv.toFixed(2);
        document.getElementById('igvCompra').textContent = igv.toFixed(2);
        document.getElementById('totalCompraFinal').textContent = total.toFixed(2);
        document.getElementById('totalGeneral').textContent = total.toFixed(2);
    }

// =====================================================
//          PROVEEDORES
// =====================================================
    function cargarProveedores() {
        fetch(contextPath + '/CompraController?action=listarProveedores')
                .then(response => response.json())
                .then(proveedores => {
                    const select = document.getElementById('proveedorSelect');
                    select.innerHTML = '<option value="">Seleccionar proveedor...</option>';

                    proveedores.forEach(p => {
                        const option = document.createElement('option');
                        option.value = p.id;
                        option.textContent = p.ruc + ' - ' + p.razonSocial;
                        select.appendChild(option);
                    });
                })
                .catch(error => console.error('Error al cargar proveedores:', error));
    }

    function guardarProveedor() {
        const ruc = document.getElementById('proveedorRuc').value.trim();
        const razonSocial = document.getElementById('proveedorRazonSocial').value.trim();
        const contacto = document.getElementById('proveedorContacto').value.trim();
        const telefono = document.getElementById('proveedorTelefono').value.trim();

        if (!ruc || !razonSocial) {
            mostrarToast('RUC y Razón Social son obligatorios', 'error');
            return;
        }

        const formData = new FormData();
        formData.append('action', 'registrarProveedor');
        formData.append('ruc', ruc);
        formData.append('razonSocial', razonSocial);
        formData.append('contacto', contacto);
        formData.append('telefono', telefono);

        fetch(contextPath + '/CompraController', {
            method: 'POST',
            body: new URLSearchParams(formData)
        })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        mostrarToast('Proveedor registrado correctamente', 'success');
                        bootstrap.Modal.getInstance(document.getElementById('modalNuevoProveedor')).hide();

                        // Agregar al select y seleccionar
                        const select = document.getElementById('proveedorSelect');
                        const option = document.createElement('option');
                        option.value = data.id;
                        option.textContent = data.ruc + ' - ' + data.razonSocial;
                        select.appendChild(option);
                        select.value = data.id;

                        // Limpiar formulario
                        document.getElementById('proveedorRuc').value = '';
                        document.getElementById('proveedorRazonSocial').value = '';
                        document.getElementById('proveedorContacto').value = '';
                        document.getElementById('proveedorTelefono').value = '';
                    } else {
                        mostrarToast(data.error || 'Error al registrar proveedor', 'error');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    mostrarToast('Error al registrar proveedor', 'error');
                });
    }

// =====================================================
//          CONFIRMAR COMPRA
// =====================================================
    function confirmarCompra() {
        if (productosCompra.length === 0) {
            mostrarToast('Agregue al menos un producto', 'error');
            return;
        }

        const proveedorId = document.getElementById('proveedorSelect').value;
        const observaciones = document.getElementById('observaciones').value.trim();
        const incluyeIgv = document.getElementById('precioIncluyeIgv').checked;

        // Preparar datos
        const datosCompra = {
            proveedorId: proveedorId ? parseInt(proveedorId) : null,
            observaciones: observaciones,
            precioIncluyeIgv: incluyeIgv,
            detalles: productosCompra.map(p => ({
                    catalogoProductoId: p.catalogoProductoId,
                    nombreComercial: p.nombreComercial,
                    cantidad: p.cantidad,
                    precioCompra: p.precioCompra,
                    precioVenta: p.precioVenta,
                    lote: p.lote,
                    fechaVencimiento: p.fechaVencimiento
                }))
        };

        // Deshabilitar botón
        const btn = document.getElementById('btnConfirmarCompra');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Procesando...';

        fetch(contextPath + '/CompraController?action=registrarCompra', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(datosCompra)
        })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        mostrarToast('Compra ' + data.numeroTransaccion + ' registrada exitosamente', 'success');

                        // Limpiar formulario
                        productosCompra = [];
                        actualizarTablaProductos();
                        actualizarTotales();
                        document.getElementById('proveedorSelect').value = '';
                        document.getElementById('observaciones').value = '';
                        document.getElementById('numeroComprobante').value = '';

                    } else {
                        mostrarToast(data.error || 'Error al registrar la compra', 'error');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    mostrarToast('Error al procesar la compra', 'error');
                })
                .finally(() => {
                    btn.disabled = false;
                    btn.innerHTML = '<i class="bi bi-check-circle"></i> CONFIRMAR COMPRA';
                });
    }

// =====================================================
//          UTILIDADES
// =====================================================
    function formatearFecha(fecha) {
        if (!fecha || fecha === '' || fecha === 'null' || fecha === 'undefined')
            return '-';
        
        // Si es una fecha ISO o timestamp, convertir
        try {
            var fechaObj;
            
            // Si viene como string con formato YYYY-MM-DD
            if (typeof fecha === 'string' && fecha.includes('-')) {
                var partes = fecha.split('-');
                if (partes.length === 3 && partes[0].length === 4) {
                    return partes[2] + '/' + partes[1] + '/' + partes[0];
                }
            }
            
            // Intentar parsear como fecha
            fechaObj = new Date(fecha);
            if (!isNaN(fechaObj.getTime())) {
                var dia = ('0' + fechaObj.getDate()).slice(-2);
                var mes = ('0' + (fechaObj.getMonth() + 1)).slice(-2);
                var anio = fechaObj.getFullYear();
                return dia + '/' + mes + '/' + anio;
            }
            
            return fecha;
        } catch (e) {
            return fecha || '-';
        }
            }

            function mostrarToast(mensaje, tipo = 'success') {
                const toast = document.getElementById('toastNotificacion');
                const icon = document.getElementById('toastIcon');
                const titulo = document.getElementById('toastTitulo');
                const body = document.getElementById('toastMensaje');

                body.textContent = mensaje;

                if (tipo === 'success') {
                    icon.className = 'bi bi-check-circle text-primary me-2';
                    titulo.textContent = 'Éxito';
                } else if (tipo === 'error') {
                    icon.className = 'bi bi-exclamation-circle text-danger me-2';
                    titulo.textContent = 'Error';
                } else {
                    icon.className = 'bi bi-info-circle text-info me-2';
                    titulo.textContent = 'Información';
                }

                const bsToast = new bootstrap.Toast(toast);
                bsToast.show();
            }

// Evento para recalcular totales cuando cambia la opción de IGV
            document.getElementById('precioIncluyeIgv').addEventListener('change', actualizarTotales);
</script>
