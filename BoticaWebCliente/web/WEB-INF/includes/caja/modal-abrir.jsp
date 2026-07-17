<%-- 
    Modal Unificado: Abrir Caja
    Diseño tomado de farmaceutico/caja.jsp
    Usar en: nueva.jsp y caja.jsp
--%>
<head>
    <meta charset="UTF-8">
</head>
<!-- Modal Abrir Caja -->
<div class="modal fade" id="modalAbrirCaja" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title"><i class="bi bi-unlock me-2"></i>Abrir Caja</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div id="mensajeAbrirCaja" class="d-none"></div>
                <div class="mb-4">
                    <label class="form-label fw-semibold">Selecciona una caja disponible:</label>
                    <div id="cajasDisponiblesContainer">
                        <div class="text-center py-3">
                            <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
                            <p class="mb-0 mt-2 small text-muted">Cargando cajas...</p>
                        </div>
                    </div>
                </div>
                <div class="mb-3">
                    <label class="form-label fw-semibold">Monto inicial en caja (S/.):</label>
                    <div class="input-group input-group-lg">
                        <span class="input-group-text">S/.</span>
                        <input type="number" class="form-control text-center" id="montoInicialAbrir" 
                               step="0.01" min="0" placeholder="0.00">
                    </div>
                    <small class="text-muted">Cuenta el efectivo con el que inicias el turno (deja vacio si es 0)</small>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn btn-primary btn-lg" onclick="CajaManager.confirmarAbrir()" id="btnConfirmarAbrir">
                    <i class="bi bi-check-lg me-1"></i>Abrir Caja
                </button>
            </div>
            <div class="text-center pb-3">
                <small class="text-muted"><i class="bi bi-keyboard"></i> Presiona Enter para confirmar</small>
            </div>
        </div>
    </div>
</div>

<style>
/* Estilos para selección de caja */
.caja-option {
    padding: 15px;
    border: 2px solid #e9ecef;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;
    margin-bottom: 10px;
    display: block;
}

.caja-option:hover {
    border-color: #035b77;
    background: #f8f9fa;
}

.caja-option.selected {
    border-color: #035b77;
    background: #eef8fb;
}

.caja-option input[type="radio"] {
    display: none;
}
</style>
