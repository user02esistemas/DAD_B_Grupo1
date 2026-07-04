package pe.edu.botica.rmi.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class ProductoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long catalogoProductoId;
    private String lote;
    private Date fechaVencimiento;
    private Integer stockActual;
    private Integer stockMinimo;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private String ubicacion;
    private boolean requiereReceta;
    private String estado;
    private boolean activo;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private CatalogoProductoDTO catalogoProducto;
    private Integer diasParaVencer;

    public ProductoDTO() {
        this.stockMinimo = 10;
        this.activo = true;
        this.estado = "DISPONIBLE";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCatalogoProductoId() { return catalogoProductoId; }
    public void setCatalogoProductoId(Long catalogoProductoId) { this.catalogoProductoId = catalogoProductoId; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public Date getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal precioCompra) { this.precioCompra = precioCompra; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public boolean isRequiereReceta() { return requiereReceta; }
    public void setRequiereReceta(boolean requiereReceta) { this.requiereReceta = requiereReceta; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public CatalogoProductoDTO getCatalogoProducto() { return catalogoProducto; }
    public void setCatalogoProducto(CatalogoProductoDTO catalogoProducto) { this.catalogoProducto = catalogoProducto; }

    public Integer getDiasParaVencer() { return diasParaVencer; }
    public void setDiasParaVencer(Integer diasParaVencer) { this.diasParaVencer = diasParaVencer; }

    public boolean estaAgotado() { return stockActual == null || stockActual == 0; }
    public boolean tieneStockBajo() { return stockActual != null && stockMinimo != null && stockActual <= stockMinimo && stockActual > 0; }
    public boolean estaVencido() { return fechaVencimiento != null && fechaVencimiento.getTime() < System.currentTimeMillis(); }
    public boolean estaPorVencer() {
        if (fechaVencimiento == null) return false;
        long dias = (fechaVencimiento.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
        return dias <= 30 && dias > 0;
    }

    public String getNombreCompleto() {
        if (catalogoProducto == null) return "Producto #" + id;
        StringBuilder sb = new StringBuilder(catalogoProducto.getNombreComercial() != null ? catalogoProducto.getNombreComercial() : "");
        if (catalogoProducto.getConcentracion() != null && !catalogoProducto.getConcentracion().isEmpty())
            sb.append(" ").append(catalogoProducto.getConcentracion());
        return sb.toString();
    }
}
