package pe.edu.botica.rmi.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class DetalleTransaccionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long transaccionId;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private Long catalogoProductoId;
    private String lote;
    private String fechaVencimiento;
    private BigDecimal precioVenta;
    private Long usuarioId;
    private String descripcionProducto;
    private String nombreComercial;
    private String concentracion;

    public DetalleTransaccionDTO() {
        this.cantidad = 0;
        this.precioUnitario = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTransaccionId() { return transaccionId; }
    public void setTransaccionId(Long transaccionId) { this.transaccionId = transaccionId; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public Long getCatalogoProductoId() { return catalogoProductoId; }
    public void setCatalogoProductoId(Long catalogoProductoId) { this.catalogoProductoId = catalogoProductoId; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getDescripcionProducto() { return descripcionProducto; }
    public void setDescripcionProducto(String descripcionProducto) { this.descripcionProducto = descripcionProducto; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getConcentracion() { return concentracion; }
    public void setConcentracion(String concentracion) { this.concentracion = concentracion; }

    public void calcularSubtotal() {
        if (cantidad != null && precioUnitario != null)
            this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
