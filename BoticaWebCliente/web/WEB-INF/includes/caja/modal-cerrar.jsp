<%-- 
    Modal Unificado: Cerrar Caja
    DiseÃ±o tomado de venta/nueva.jsp (el mÃ¡s completo)
    Usar en: nueva.jsp y caja.jsp
--%>

<!-- Modal Cerrar Caja -->
<div class="modal fade" id="modalCerrarCaja" tabindex="-1">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header bg-dark text-white">
                <h5 class="modal-title"><i class="bi bi-cash-stack me-2"></i>Cierre de Caja</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <!-- InformaciÃ³n de horarios -->
                <div class="alert alert-secondary mb-4">
                    <div class="row text-center">
                        <div class="col-md-4">
                            <i class="bi bi-unlock-fill text-success"></i>
                            <strong>Apertura:</strong><br>
                            <span id="cierreHoraApertura">--:--</span>
                        </div>
                        <div class="col-md-4">
                            <i class="bi bi-lock-fill text-danger"></i>
                            <strong>Cierre:</strong><br>
                            <span id="cierreHoraCierre">--:--</span>
                        </div>
                        <div class="col-md-4">
                            <i class="bi bi-clock-history text-primary"></i>
                            <strong>Duración:</strong><br>
                            <span id="cierreDuracion">--</span>
                        </div>
                    </div>
                </div>
                
                <!-- Cards de resumen -->
                <div class="row mb-4">
                    <div class="col-md-3">
                        <div class="card bg-secondary text-white h-100">
                            <div class="card-body text-center py-3">
                                <i class="bi bi-wallet2" style="font-size: 24px;"></i>
                                <p class="mb-1 mt-2 small">Fondo Inicial</p>
                                <h5 class="mb-0" id="cierreFondoCard">S/ 0.00</h5>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card bg-success text-white h-100">
                            <div class="card-body text-center py-3">
                                <i class="bi bi-cash" style="font-size: 24px;"></i>
                                <p class="mb-1 mt-2 small">Efectivo Neto</p>
                                <h5 class="mb-0" id="cierreEfectivoCard">S/ 0.00</h5>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card bg-info text-white h-100">
                            <div class="card-body text-center py-3">
                                <i class="bi bi-phone" style="font-size: 24px;"></i>
                                <p class="mb-1 mt-2 small">Ventas Virtual</p>
                                <h5 class="mb-0" id="cierreVirtualCard">S/ 0.00</h5>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card bg-primary text-white h-100">
                            <div class="card-body text-center py-3">
                                <i class="bi bi-calculator" style="font-size: 24px;"></i>
                                <p class="mb-1 mt-2 small">Efectivo Esperado</p>
                                <h5 class="mb-0" id="cierreEsperadoCard">S/ 0.00</h5>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Detalle y formulario -->
                <div class="row">
                    <div class="col-md-6">
                        <div class="card h-100">
                            <div class="card-header"><i class="bi bi-receipt me-2"></i>Detalle del Turno</div>
                            <div class="card-body">
                                <table class="table table-sm mb-0">
                                    <tr>
                                        <td>Fondo de Caja Inicial:</td>
                                        <td class="text-end">S/ <span id="cierreFondo">0.00</span></td>
                                    </tr>
                                    <tr>
                                        <td>(+) Ventas en Efectivo:</td>
                                        <td class="text-end text-success">S/ <span id="cierreVentasEfectivo">0.00</span></td>
                                    </tr>
                                    <tr>
                                        <td>(=) Ventas Virtuales:</td>
                                        <td class="text-end text-info">S/ <span id="cierreVentasVirtual">0.00</span></td>
                                    </tr>
                                    <tr>
                                        <td>Total Ventas del Día:</td>
                                        <td class="text-end">S/ <span id="cierreTotalVentas">0.00</span></td>
                                    </tr>
                                    <tr class="table-primary">
                                        <td><strong>Efectivo Esperado en Caja:</strong></td>
                                        <td class="text-end"><strong>S/ <span id="cierreEsperado">0.00</span></strong></td>
                                    </tr>
                                </table>
                                <small class="text-muted">
                                    <i class="bi bi-info-circle"></i> Efectivo esperado = Fondo inicial + Ventas en efectivo
                                </small>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="card h-100">
                            <div class="card-header"><i class="bi bi-lock me-2"></i>Cerrar Turno</div>
                            <div class="card-body">
                                <label class="form-label">Efectivo contado en caja:</label>
                                <input type="number" class="form-control form-control-lg text-center mb-3" 
                                       id="montoFinalCierre" placeholder="0.00" step="0.01" min="0" 
                                       oninput="CajaManager.calcularDiferencia()">
                                
                                <div class="card mb-3" id="cardDiferencia">
                                    <div class="card-body text-center py-2">
                                        <small>Diferencia:</small>
                                        <h4 class="mb-0" id="diferenciaCierre">S/ 0.00</h4>
                                    </div>
                                </div>
                                
                                <div class="mb-3">
                                    <label class="form-label">Observaciones:</label>
                                    <textarea class="form-control" id="observacionesCierre" rows="2" 
                                              placeholder="Notas sobre el cierre (opcional)..."></textarea>
                                </div>
                                
                                <button class="btn btn-danger btn-lg w-100" onclick="CajaManager.confirmarCerrar()" id="btnConfirmarCerrar">
                                    <i class="bi bi-lock me-1"></i>Cerrar Caja
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
