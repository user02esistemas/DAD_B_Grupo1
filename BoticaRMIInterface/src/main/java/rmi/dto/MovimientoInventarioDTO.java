package rmi.dto;

import java.io.Serializable;
import java.sql.Timestamp;

public class MovimientoInventarioDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productoId;
    private String tipoMovimiento;
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private String motivo;
    private Long referenciaId;
    private String referenciaTipo;
    private Long usuarioId;
    private Timestamp fechaMovimiento;
    private String usuarioNombre;
    private String productoNombre;
    private String lote;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Integer getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(Integer stockAnterior) { this.stockAnterior = stockAnterior; }
    public Integer getStockNuevo() { return stockNuevo; }
    public void setStockNuevo(Integer stockNuevo) { this.stockNuevo = stockNuevo; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }
    public String getReferenciaTipo() { return referenciaTipo; }
    public void setReferenciaTipo(String referenciaTipo) { this.referenciaTipo = referenciaTipo; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Timestamp getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(Timestamp fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }
}
