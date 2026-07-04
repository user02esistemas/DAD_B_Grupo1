package DTO;

import java.math.BigDecimal;

/**
 * DTO para Detalle de Transacciones
 * Representa cada línea de una compra o venta
 */
public class DetalleTransaccionDTO {

    private Long id;
    private Long transaccionId;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Datos adicionales para la vista (no se guardan, solo para mostrar)
    private Long catalogoProductoId;  // ID del catálogo (para crear producto si no existe)
    private String lote;
    private String fechaVencimiento;
    private BigDecimal precioVenta;    // Para compras: precio de venta sugerido
    private Long usuarioId;            // Usuario que registra (para movimientos)
    
    // Datos del producto/catálogo para mostrar en lista
    private String descripcionProducto;
    private String nombreComercial;
    private String concentracion;

    // Constructores
    public DetalleTransaccionDTO() {
        this.cantidad = 0;
        this.precioUnitario = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransaccionId() {
        return transaccionId;
    }

    public void setTransaccionId(Long transaccionId) {
        this.transaccionId = transaccionId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public Long getCatalogoProductoId() {
        return catalogoProductoId;
    }

    public void setCatalogoProductoId(Long catalogoProductoId) {
        this.catalogoProductoId = catalogoProductoId;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public String getDescripcionProducto() {
        return descripcionProducto;
    }

    public void setDescripcionProducto(String descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getConcentracion() {
        return concentracion;
    }

    public void setConcentracion(String concentracion) {
        this.concentracion = concentracion;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    // =====================================================
    //              MÉTODOS DE NEGOCIO
    // =====================================================

    /**
     * Calcular subtotal automáticamente
     */
    public void calcularSubtotal() {
        if (cantidad != null && precioUnitario != null) {
            this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad)).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * Obtener descripción corta para mostrar en lista
     */
    public String getDescripcionCorta() {
        if (descripcionProducto != null && !descripcionProducto.isEmpty()) {
            return descripcionProducto;
        }
        StringBuilder sb = new StringBuilder();
        if (nombreComercial != null) sb.append(nombreComercial);
        if (concentracion != null) sb.append(" ").append(concentracion);
        return sb.toString();
    }

    /**
     * Validar que el detalle tenga datos mínimos requeridos
     */
    public boolean esValido() {
        return catalogoProductoId != null 
            && cantidad != null && cantidad > 0
            && precioUnitario != null && precioUnitario.compareTo(BigDecimal.ZERO) > 0
            && lote != null && !lote.trim().isEmpty()
            && fechaVencimiento != null && !fechaVencimiento.trim().isEmpty();
    }
}
