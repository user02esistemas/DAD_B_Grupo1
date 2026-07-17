/**
 * CajaManager - Gestión unificada de apertura y cierre de caja
 * Usado por: nueva.jsp y caja.jsp
 * 
 * Requiere:
 * - Bootstrap 5
 * - Los modales modal-abrir.jsp y modal-cerrar.jsp incluidos en la página
 * 
 * Variables que deben estar definidas en la página:
 * - contextPath: ruta base de la aplicación
 * - sesionCajaId: ID de la sesión actual (null si no hay)
 * - urlRetorno: URL a donde redirigir después de cerrar caja
 */

var CajaManager = (function() {
    
    // Variables privadas
    var cajaSeleccionada = null;
    var efectivoEsperado = 0;
    var modalAbrir = null;
    var modalCerrar = null;

    function mostrarMensaje(elementId, mensaje, tipo) {
        var target = document.getElementById(elementId);
        if (!target) return;
        var icon = tipo === 'danger' ? 'exclamation-triangle' : (tipo === 'warning' ? 'exclamation-circle' : 'check-circle');
        target.className = 'alert alert-' + (tipo || 'info') + ' d-flex align-items-start gap-2';
        target.innerHTML = '<i class="bi bi-' + icon + ' mt-1"></i><div>' + mensaje + '</div>';
    }

    function limpiarMensaje(elementId) {
        var target = document.getElementById(elementId);
        if (!target) return;
        target.className = 'd-none';
        target.innerHTML = '';
    }
    
    // =====================================================
    //     INICIALIZACIÓN
    // =====================================================
    
    function init(options) {
        options = options || {};
        
        // Configurar eventos de teclado para los modales
        var modalAbrirEl = document.getElementById('modalAbrirCaja');
        var modalCerrarEl = document.getElementById('modalCerrarCaja');
        
        if (modalAbrirEl) {
            modalAbrirEl.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    var btn = document.getElementById('btnConfirmarAbrir');
                    if (btn && !btn.disabled) {
                        e.preventDefault();
                        confirmarAbrir();
                    }
                }
            });
            
            modalAbrirEl.addEventListener('shown.bs.modal', function() {
                var input = document.getElementById('montoInicialAbrir');
                if (input) input.focus();
            });
        }
        
        if (modalCerrarEl) {
            modalCerrarEl.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    var btn = document.getElementById('btnConfirmarCerrar');
                    if (btn && !btn.disabled) {
                        e.preventDefault();
                        confirmarCerrar();
                    }
                }
            });
            
            modalCerrarEl.addEventListener('shown.bs.modal', function() {
                var input = document.getElementById('montoFinalCierre');
                if (input) input.focus();
            });
        }
        
        // Mostrar modal de abrir si se solicita
        if (options.mostrarAbrirAlInicio) {
            mostrarAbrir();
        }
    }
    
    // =====================================================
    //     ABRIR CAJA
    // =====================================================
    
    function mostrarAbrir() {
        cajaSeleccionada = null;
        limpiarMensaje('mensajeAbrirCaja');
        
        var inputMonto = document.getElementById('montoInicialAbrir');
        if (inputMonto) inputMonto.value = '';
        
        // Cargar cajas disponibles
        var container = document.getElementById('cajasDisponiblesContainer');
        if (container) {
            container.innerHTML = '<div class="text-center py-3">' +
                '<div class="spinner-border spinner-border-sm text-primary" role="status"></div>' +
                '<p class="mb-0 mt-2 small text-muted">Cargando cajas...</p></div>';
        }
        
        fetch(contextPath + '/SesionCajaController?action=cajasDisponibles')
            .then(function(response) { return response.json(); })
            .then(function(cajas) {
                renderizarCajasDisponibles(cajas);
            })
            .catch(function(error) {
                console.error('Error cargando cajas:', error);
                if (container) {
                    container.innerHTML = '<div class="alert alert-danger mb-0">' +
                        '<i class="bi bi-exclamation-triangle me-2"></i>Error al cargar cajas</div>';
                }
            });
        
        modalAbrir = new bootstrap.Modal(document.getElementById('modalAbrirCaja'));
        modalAbrir.show();
    }
    
    function renderizarCajasDisponibles(cajas) {
        var container = document.getElementById('cajasDisponiblesContainer');
        var btnConfirmar = document.getElementById('btnConfirmarAbrir');
        
        if (!container) return;
        
        if (!cajas || cajas.length === 0) {
            container.innerHTML = '<div class="alert alert-warning mb-0">' +
                '<i class="bi bi-exclamation-triangle me-2"></i>' +
                'No hay cajas disponibles. Todas están en uso o no existen.</div>';
            if (btnConfirmar) btnConfirmar.disabled = true;
            return;
        }
        
        var html = '';
        for (var i = 0; i < cajas.length; i++) {
            var c = cajas[i];
            var isFirst = (i === 0);
            var selectedClass = isFirst ? 'selected' : '';
            
            if (isFirst) cajaSeleccionada = c.id;
            
            html += '<label class="caja-option ' + selectedClass + '" onclick="CajaManager.seleccionarCaja(' + c.id + ', this)">' +
                '<input type="radio" name="cajaSeleccion" value="' + c.id + '"' + (isFirst ? ' checked' : '') + '>' +
                '<div class="d-flex align-items-center">' +
                '<i class="bi bi-safe fs-3 me-3 text-primary"></i>' +
                '<div>' +
                '<strong>' + c.nombre + '</strong>' +
                '<small class="d-block text-muted">' + (c.ubicacion || 'Ubicación principal') + '</small>' +
                '</div></div></label>';
        }
        
        container.innerHTML = html;
        if (btnConfirmar) btnConfirmar.disabled = false;
    }
    
    function seleccionarCaja(id, elemento) {
        cajaSeleccionada = id;
        
        document.querySelectorAll('.caja-option').forEach(function(el) {
            el.classList.remove('selected');
        });
        
        if (elemento) {
            elemento.classList.add('selected');
        }
    }
    
    function confirmarAbrir() {
        if (!cajaSeleccionada) {
            mostrarMensaje('mensajeAbrirCaja', 'Por favor selecciona una caja disponible para iniciar el turno.', 'warning');
            return;
        }
        
        var btn = document.getElementById('btnConfirmarAbrir');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Abriendo...';
        }
        
        var montoInicial = parseFloat(document.getElementById('montoInicialAbrir').value) || 0;
        
        fetch(contextPath + '/SesionCajaController', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=abrir&cajaId=' + cajaSeleccionada + '&montoInicial=' + montoInicial
        })
        .then(function(response) { return response.json(); })
        .then(function(data) {
            if (data.success) {
                // Actualizar variable global y recargar
                if (typeof sesionCajaId !== 'undefined') {
                    sesionCajaId = data.sesionId;
                }
                
                // Cerrar modal y recargar página
                if (modalAbrir) modalAbrir.hide();
                window.location.reload();
            } else {
                mostrarMensaje('mensajeAbrirCaja', data.error || 'No se pudo abrir la caja.', 'danger');
                resetBotonAbrir(btn);
            }
        })
        .catch(function(error) {
            console.error('Error:', error);
            mostrarMensaje('mensajeAbrirCaja', 'Error al abrir caja. Verifica la conexión e inténtalo nuevamente.', 'danger');
            resetBotonAbrir(btn);
        });
    }
    
    function resetBotonAbrir(btn) {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Abrir Caja';
        }
    }
    
    // =====================================================
    //     CERRAR CAJA
    // =====================================================
    
    function mostrarCerrar() {
        if (typeof sesionCajaId === 'undefined' || !sesionCajaId) {
            mostrarMensaje('mensajeCerrarCaja', 'No hay una sesión de caja activa para cerrar.', 'warning');
            return;
        }
        
        fetch(contextPath + '/SesionCajaController?action=resumen&sesionId=' + sesionCajaId)
            .then(function(response) { return response.json(); })
            .then(function(sesion) {
                llenarDatosCierre(sesion);
                limpiarMensaje('mensajeCerrarCaja');
                cancelarConfirmacionCerrar();
                modalCerrar = new bootstrap.Modal(document.getElementById('modalCerrarCaja'));
                modalCerrar.show();
            })
            .catch(function(error) {
                console.error('Error:', error);
                mostrarMensaje('mensajeCerrarCaja', 'Error al obtener el resumen de caja.', 'danger');
            });
    }
    
    function llenarDatosCierre(sesion) {
        var montoInicial = parseFloat(sesion.montoInicial) || 0;
        var totalVentas = parseFloat(sesion.totalTransacciones) || 0;
        var totalEfectivo = parseFloat(sesion.totalVentasEfectivo) || 0;
        var totalVueltos = parseFloat(sesion.totalVueltos) || 0;
        var totalVirtual = parseFloat(sesion.totalVentasVirtual) || 0;
        
        // Efectivo neto = Efectivo recibido - Vueltos
        var efectivoNeto = totalEfectivo - totalVueltos;
        
        // Efectivo esperado = Monto inicial + Efectivo neto
        efectivoEsperado = parseFloat(sesion.efectivoEsperado) || (montoInicial + efectivoNeto);
        
        // Horarios
        var fechaApertura = sesion.fechaApertura ? new Date(sesion.fechaApertura.replace(' ', 'T')) : null;
        var ahora = new Date();
        
        if (fechaApertura) {
            document.getElementById('cierreHoraApertura').textContent = 
                fechaApertura.toLocaleDateString('es-PE') + ' ' + 
                fechaApertura.toLocaleTimeString('es-PE', {hour: '2-digit', minute: '2-digit'});
            
            // Calcular duración
            var diffMs = ahora - fechaApertura;
            var diffHrs = Math.floor(diffMs / 3600000);
            var diffMins = Math.floor((diffMs % 3600000) / 60000);
            document.getElementById('cierreDuracion').textContent = diffHrs + 'h ' + diffMins + 'min';
        }
        
        document.getElementById('cierreHoraCierre').textContent = 
            ahora.toLocaleDateString('es-PE') + ' ' + 
            ahora.toLocaleTimeString('es-PE', {hour: '2-digit', minute: '2-digit'});
        
        // Cards superiores
        document.getElementById('cierreFondoCard').textContent = 'S/ ' + montoInicial.toFixed(2);
        document.getElementById('cierreEfectivoCard').textContent = 'S/ ' + efectivoNeto.toFixed(2);
        document.getElementById('cierreVirtualCard').textContent = 'S/ ' + totalVirtual.toFixed(2);
        document.getElementById('cierreEsperadoCard').textContent = 'S/ ' + efectivoEsperado.toFixed(2);
        
        // Tabla detalle
        document.getElementById('cierreFondo').textContent = montoInicial.toFixed(2);
        document.getElementById('cierreVentasEfectivo').textContent = efectivoNeto.toFixed(2);
        document.getElementById('cierreVentasVirtual').textContent = totalVirtual.toFixed(2);
        document.getElementById('cierreTotalVentas').textContent = totalVentas.toFixed(2);
        document.getElementById('cierreEsperado').textContent = efectivoEsperado.toFixed(2);
        
        // Limpiar formulario
        document.getElementById('montoFinalCierre').value = '';
        document.getElementById('diferenciaCierre').textContent = 'S/ 0.00';
        document.getElementById('cardDiferencia').className = 'card mb-3';
        document.getElementById('observacionesCierre').value = '';
    }
    
    function calcularDiferencia() {
        var montoContado = parseFloat(document.getElementById('montoFinalCierre').value) || 0;
        var diferencia = montoContado - efectivoEsperado;
        
        var cardDif = document.getElementById('cardDiferencia');
        var difElement = document.getElementById('diferenciaCierre');
        
        if (Math.abs(diferencia) < 0.01) {
            difElement.textContent = 'S/ 0.00 (Cuadrado)';
            cardDif.className = 'card mb-3 bg-primary text-white';
        } else if (diferencia > 0) {
            difElement.textContent = '+S/ ' + diferencia.toFixed(2) + ' (Sobrante)';
            cardDif.className = 'card mb-3 bg-warning';
        } else {
            difElement.textContent = '-S/ ' + Math.abs(diferencia).toFixed(2) + ' (Faltante)';
            cardDif.className = 'card mb-3 bg-danger text-white';
        }
    }
    
    function confirmarCerrar() {
        var montoFinal = document.getElementById('montoFinalCierre').value;
        if (!montoFinal || montoFinal === '') {
            mostrarMensaje('mensajeCerrarCaja', 'Ingrese el monto final contado en caja antes de cerrar el turno.', 'warning');
            document.getElementById('montoFinalCierre').focus();
            return;
        }

        limpiarMensaje('mensajeCerrarCaja');
        var panel = document.getElementById('confirmacionCerrarCaja');
        if (panel) panel.classList.remove('d-none');
    }

    function cancelarConfirmacionCerrar() {
        var panel = document.getElementById('confirmacionCerrarCaja');
        if (panel) panel.classList.add('d-none');
    }

    function ejecutarCerrar() {
        var montoFinal = document.getElementById('montoFinalCierre').value;
        if (!montoFinal || montoFinal === '') {
            mostrarMensaje('mensajeCerrarCaja', 'Ingrese el monto final contado en caja antes de cerrar el turno.', 'warning');
            document.getElementById('montoFinalCierre').focus();
            cancelarConfirmacionCerrar();
            return;
        }

        var btn = document.getElementById('btnConfirmarCerrar');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Cerrando...';
        }
        cancelarConfirmacionCerrar();
        
        var observaciones = document.getElementById('observacionesCierre').value;
        
        fetch(contextPath + '/SesionCajaController', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=cerrar&sesionId=' + sesionCajaId + '&montoFinal=' + montoFinal + 
                '&observaciones=' + encodeURIComponent(observaciones)
        })
        .then(function(response) { return response.json(); })
        .then(function(data) {
            if (data.success) {
                var diferencia = parseFloat(data.diferencia) || 0;
                var msg = '<strong>Caja cerrada correctamente.</strong><br>';
                
                if (diferencia > 0) {
                    msg += 'Sobrante: S/ ' + diferencia.toFixed(2);
                } else if (diferencia < 0) {
                    msg += 'Faltante: S/ ' + Math.abs(diferencia).toFixed(2);
                } else {
                    msg += 'Caja cuadrada, sin diferencias.';
                }
                
                mostrarMensaje('mensajeCerrarCaja', msg, 'success');
                
                // Redirigir según contexto
                setTimeout(function() {
                    if (typeof urlRetorno !== 'undefined' && urlRetorno) {
                        window.location.href = urlRetorno;
                    } else {
                        window.location.reload();
                    }
                }, 1100);
            } else {
                mostrarMensaje('mensajeCerrarCaja', data.error || 'No se pudo cerrar la caja.', 'danger');
                resetBotonCerrar(btn);
            }
        })
        .catch(function(error) {
            console.error('Error:', error);
            mostrarMensaje('mensajeCerrarCaja', 'Error al cerrar caja. Verifica la conexión e inténtalo nuevamente.', 'danger');
            resetBotonCerrar(btn);
        });
    }
    
    function resetBotonCerrar(btn) {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-lock me-1"></i>Cerrar Caja';
        }
    }
    
   
    return {
        init: init,
        mostrarAbrir: mostrarAbrir,
        mostrarCerrar: mostrarCerrar,
        seleccionarCaja: seleccionarCaja,
        confirmarAbrir: confirmarAbrir,
        confirmarCerrar: confirmarCerrar,
        ejecutarCerrar: ejecutarCerrar,
        cancelarConfirmacionCerrar: cancelarConfirmacionCerrar,
        calcularDiferencia: calcularDiferencia
    };
    
})();
