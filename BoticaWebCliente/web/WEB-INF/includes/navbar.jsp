<%-- 
    Document   : navbar
    Created on : 2 dic. 2025, 15:21:19
    Author     : yerri
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<!-- Navbar superior -->
<nav class="navbar navbar-expand-lg navbar-dark sticky-top">
    <div class="container-fluid">
        <!-- Toggle para móvil -->
        <button class="navbar-toggler me-2" type="button" data-bs-toggle="collapse" data-bs-target="#sidebarMenu">
            <span class="navbar-toggler-icon"></span>
        </button>

        <!-- Logo y nombre -->
        <a class="navbar-brand d-flex align-items-center" href="<%= request.getContextPath()%>/index.jsp">
            <img src="<%= request.getContextPath()%>/assets/img/logo.png" 
                 alt="logo" class="logo-img" />

            <span class="fw-bold">EconoSalud Farmacia</span>
        </a>

        <!-- Búsqueda rápida (opcional) -->


        <!-- Usuario y notificaciones -->
        <div class="ms-auto d-flex align-items-center gap-2">
        <div class="dropdown">
            <button class="btn btn-link text-white text-decoration-none position-relative notification-bell" type="button" data-bs-toggle="dropdown" aria-expanded="false" title="Notificaciones en tiempo real">
                <i class="bi bi-bell-fill fs-5"></i>
                <span id="wsNotificationBadge" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger d-none">0</span>
            </button>
            <div class="dropdown-menu dropdown-menu-end shadow notification-menu">
                <div class="notification-header">
                    <div>
                        <strong>Centro de notificaciones</strong>
                        <small class="d-block">Actividad en tiempo real</small>
                    </div>
                    <span id="wsNotificationStatus" class="notification-status">Conectando...</span>
                </div>
                <div id="wsNotificationList" class="notification-list">
                    <div class="notification-empty">
                        <i class="bi bi-inbox"></i>
                        <strong>Sin notificaciones</strong>
                        <span>Las ventas, compras e inventario aparecerán aquí.</span>
                    </div>
                </div>
                <div class="notification-footer">
                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="clearWsNotifications()">
                        <i class="bi bi-check2-all me-1"></i>Marcar como leídas
                    </button>
                </div>
            </div>
        </div>

        <!-- Usuario -->
        <div class="dropdown">
            <button class="btn btn-link text-white text-decoration-none d-flex align-items-center" 
                    type="button" data-bs-toggle="dropdown">
                <div class="bg-white rounded-circle p-2 me-2">
                    <i class="bi bi-person-fill text-primary"></i>
                </div>
                <div class="text-start d-none d-md-block">
                    <small class="d-block" style="font-size: 0.75rem; opacity: 0.9;"><%= username%></small>
                    <span class="fw-bold"><%= nombreCompleto%></span>
                </div>
                <i class="bi bi-chevron-down ms-2"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end">
                <li><h6 class="dropdown-header">Rol: <%= rolActual%></h6></li>
                <li><hr class="dropdown-divider"></li>
                <li>
                    <a class="dropdown-item" href="#">
                        <i class="bi bi-person-circle me-2"></i>Mi Perfil
                    </a>
                </li>
                <li>
                    <a class="dropdown-item" href="#">
                        <i class="bi bi-gear me-2"></i>Configuración
                    </a>
                </li>
                <li><hr class="dropdown-divider"></li>
                <li>
                    <a class="dropdown-item text-danger" href="<%= request.getContextPath()%>/AuthController?action=logout">
                        <i class="bi bi-box-arrow-right me-2"></i>Cerrar Sesión
                    </a>
                </li>
            </ul>
        </div>
        </div>
    </div>
</nav>

<div class="toast-container position-fixed bottom-0 end-0 p-3" style="z-index: 1080;">
    <div id="wsNotificationToast" class="toast notification-toast" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-header notification-toast-header">
            <span class="notification-toast-icon"><i id="wsToastIcon" class="bi bi-bell-fill"></i></span>
            <strong id="wsToastTitle" class="me-auto">Notificación</strong>
            <small id="wsToastTime" class="text-white-50">Ahora</small>
            <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Cerrar"></button>
        </div>
        <div id="wsToastBody" class="toast-body"></div>
    </div>
</div>

<script>
(function () {
    var notifications = [];
    var badge = document.getElementById('wsNotificationBadge');
    var list = document.getElementById('wsNotificationList');
    var status = document.getElementById('wsNotificationStatus');

    window.clearWsNotifications = function () {
        notifications = [];
        renderNotifications();
    };

    function wsUrl() {
        var protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
        return protocol + window.location.host + '/BoticaAPIREST/ws/notificaciones';
    }

    function connect() {
        if (!window.WebSocket) {
            status.textContent = 'No soportado';
            return;
        }
        var socket = new WebSocket(wsUrl());
        socket.onopen = function () { status.textContent = 'En vivo'; status.className = 'notification-status is-live'; };
        socket.onclose = function () {
            status.textContent = 'Reconectando';
            status.className = 'notification-status is-warning';
            setTimeout(connect, 4000);
        };
        socket.onerror = function () { status.textContent = 'Sin conexión'; status.className = 'notification-status is-offline'; };
        socket.onmessage = function (event) {
            try {
                addNotification(JSON.parse(event.data));
            } catch (e) {
                console.warn('Notificación inválida', event.data);
            }
        };
    }

    function addNotification(item) {
        notifications.unshift(item);
        notifications = notifications.slice(0, 12);
        renderNotifications();
        showToast(item);
    }

    function renderNotifications() {
        if (!notifications.length) {
            badge.classList.add('d-none');
            badge.textContent = '0';
            list.innerHTML = '<div class="notification-empty">'
                + '<i class="bi bi-inbox"></i>'
                + '<strong>Sin notificaciones</strong>'
                + '<span>Las ventas, compras e inventario aparecerán aquí.</span>'
                + '</div>';
            return;
        }
        badge.classList.remove('d-none');
        badge.textContent = notifications.length > 9 ? '9+' : notifications.length;
        list.innerHTML = notifications.map(function (item) {
            var meta = notificationMeta(item.tipo);
            return '<div class="notification-item">'
                + '<div class="notification-icon ' + meta.className + '"><i class="bi ' + meta.icon + '"></i></div>'
                + '<div class="notification-content">'
                + '<div class="d-flex justify-content-between gap-2 align-items-start">'
                + '<strong>' + escapeHtml(item.titulo || 'Notificación') + '</strong>'
                + '<span class="notification-type">' + escapeHtml(item.tipo || 'INFO') + '</span>'
                + '</div>'
                + '<div class="notification-message">' + escapeHtml(item.mensaje || '') + '</div>'
                + '<div class="notification-time"><i class="bi bi-clock me-1"></i>' + escapeHtml(item.fecha || '') + '</div>'
                + '</div>'
                + '</div>';
        }).join('');
    }

    function showToast(item) {
        var toastElement = document.getElementById('wsNotificationToast');
        if (!toastElement || !window.bootstrap) return;
        var meta = notificationMeta(item.tipo);
        document.getElementById('wsToastIcon').className = 'bi ' + meta.icon;
        document.getElementById('wsToastTitle').textContent = item.titulo || 'Notificación';
        document.getElementById('wsToastBody').textContent = item.mensaje || '';
        document.getElementById('wsToastTime').textContent = item.fecha || 'Ahora';
        bootstrap.Toast.getOrCreateInstance(toastElement, { delay: 4500 }).show();
    }

    function notificationMeta(tipo) {
        switch (String(tipo || '').toUpperCase()) {
            case 'VENTA': return { icon: 'bi-receipt-cutoff', className: 'is-sale' };
            case 'COMPRA': return { icon: 'bi-cart-check', className: 'is-buy' };
            case 'INVENTARIO': return { icon: 'bi-box-seam', className: 'is-stock' };
            default: return { icon: 'bi-bell-fill', className: 'is-info' };
        }
    }

    function escapeHtml(value) {
        return String(value).replace(/[&<>'"]/g, function (char) {
            return {'&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'}[char];
        });
    }

    connect();
})();
</script>
