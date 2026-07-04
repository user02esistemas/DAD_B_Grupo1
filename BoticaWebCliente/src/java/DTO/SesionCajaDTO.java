package DTO;

import DAO.SesionCajaDAO;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * DTO para Sesiones de Caja
 */
public class SesionCajaDTO {

    private Long id;
    private Long cajaId;
    private Long usuarioId;
    private Timestamp fechaApertura;
    private Timestamp fechaCierre;
    private BigDecimal montoInicial;
    private BigDecimal montoFinal;
    private BigDecimal totalTransacciones;
    private BigDecimal totalVentasEfectivo;
    private BigDecimal totalVentasVirtual;
    private BigDecimal totalVueltos;
    private BigDecimal efectivoEsperado;
    private String estado;
    private String observaciones;

    // Campos adicionales para mostrar
    private String cajaNombre;
    private String usuarioNombre;

    // Constructor
    public SesionCajaDTO() {
        this.montoInicial = BigDecimal.ZERO;
        this.montoFinal = BigDecimal.ZERO;
        this.totalTransacciones = BigDecimal.ZERO;
        this.totalVentasEfectivo = BigDecimal.ZERO;
        this.totalVentasVirtual = BigDecimal.ZERO;
        this.totalVueltos = BigDecimal.ZERO;
        this.efectivoEsperado = BigDecimal.ZERO;
        this.estado = "ABIERTA";
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCajaId() { return cajaId; }
    public void setCajaId(Long cajaId) { this.cajaId = cajaId; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Timestamp getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(Timestamp fechaApertura) { this.fechaApertura = fechaApertura; }

    public Timestamp getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(Timestamp fechaCierre) { this.fechaCierre = fechaCierre; }

    public BigDecimal getMontoInicial() { return montoInicial; }
    public void setMontoInicial(BigDecimal montoInicial) { this.montoInicial = montoInicial; }

    public BigDecimal getMontoFinal() { return montoFinal; }
    public void setMontoFinal(BigDecimal montoFinal) { this.montoFinal = montoFinal; }

    public BigDecimal getTotalTransacciones() { return totalTransacciones; }
    public void setTotalTransacciones(BigDecimal totalTransacciones) { this.totalTransacciones = totalTransacciones; }

    public BigDecimal getTotalVentasEfectivo() { return totalVentasEfectivo; }
    public void setTotalVentasEfectivo(BigDecimal totalVentasEfectivo) { this.totalVentasEfectivo = totalVentasEfectivo; }

    public BigDecimal getTotalVentasVirtual() { return totalVentasVirtual; }
    public void setTotalVentasVirtual(BigDecimal totalVentasVirtual) { this.totalVentasVirtual = totalVentasVirtual; }

    public BigDecimal getTotalVueltos() { return totalVueltos; }
    public void setTotalVueltos(BigDecimal totalVueltos) { this.totalVueltos = totalVueltos; }

    public BigDecimal getEfectivoEsperado() { return efectivoEsperado; }
    public void setEfectivoEsperado(BigDecimal efectivoEsperado) { this.efectivoEsperado = efectivoEsperado; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getCajaNombre() { return cajaNombre; }
    public void setCajaNombre(String cajaNombre) { this.cajaNombre = cajaNombre; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    // =====================================================
    //              MÉTODOS DE NEGOCIO (DTO)
    // =====================================================

    /**
     * Abrir nueva sesión de caja
     */
    public boolean abrir() {
        Long idGenerado = new SesionCajaDAO().abrirSesion(this);
        if (idGenerado != null) {
            this.id = idGenerado;
            return true;
        }
        return false;
    }

    /**
     * Cerrar sesión de caja
     */
    public boolean cerrar() {
        return new SesionCajaDAO().cerrarSesion(this);
    }

    /**
     * Buscar sesión abierta del usuario
     */
    public static SesionCajaDTO buscarSesionAbierta(Long usuarioId) {
        return new SesionCajaDAO().buscarSesionAbierta(usuarioId);
    }

    /**
     * Buscar sesión por ID
     */
    public static SesionCajaDTO buscarPorId(Long id) {
        return new SesionCajaDAO().buscarPorId(id);
    }

    /**
     * Verificar si usuario tiene sesión abierta
     */
    public static boolean tieneSesionAbierta(Long usuarioId) {
        return new SesionCajaDAO().tieneSesionAbierta(usuarioId);
    }

    /**
     * Actualizar totales de la sesión
     */
    public boolean actualizarTotales() {
        return new SesionCajaDAO().actualizarTotales(this);
    }

    /**
     * Calcular efectivo esperado
     */
    public BigDecimal calcularEfectivoEsperado() {
        return montoInicial.add(totalVentasEfectivo);
    }

    /**
     * Verificar si está abierta
     */
    public boolean estaAbierta() {
        return "ABIERTA".equals(this.estado);
    }
}
