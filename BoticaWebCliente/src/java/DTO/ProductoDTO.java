package DTO;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * DTO para Productos en inventario
 * Representa el stock real de medicamentos con lote y vencimiento
 */
public class ProductoDTO {

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
    private String estado;  // DISPONIBLE, POR_VENCER, VENCIDO
    private boolean activo;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Datos del catálogo (para joins)
    private CatalogoProductoDTO catalogoProducto;
    
    // Campo calculado
    private Integer diasParaVencer;

    // Constructores
    public ProductoDTO() {
        this.stockMinimo = 10;
        this.activo = true;
        this.estado = "DISPONIBLE";
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public boolean isRequiereReceta() {
        return requiereReceta;
    }

    public void setRequiereReceta(boolean requiereReceta) {
        this.requiereReceta = requiereReceta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public CatalogoProductoDTO getCatalogoProducto() {
        return catalogoProducto;
    }

    public void setCatalogoProducto(CatalogoProductoDTO catalogoProducto) {
        this.catalogoProducto = catalogoProducto;
    }

    public Integer getDiasParaVencer() {
        return diasParaVencer;
    }

    public void setDiasParaVencer(Integer diasParaVencer) {
        this.diasParaVencer = diasParaVencer;
    }

    // =====================================================
    //              MÉTODOS DE CÁLCULO
    // =====================================================

    /**
     * Calcular margen de ganancia
     */
    public BigDecimal calcularMargenGanancia() {
        if (precioCompra == null || precioVenta == null || precioCompra.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return precioVenta.subtract(precioCompra)
                .divide(precioCompra, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * Calcular precio de venta sugerido dado un porcentaje de ganancia
     */
    public BigDecimal calcularPrecioVentaSugerido(BigDecimal porcentajeGanancia) {
        if (precioCompra == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal factor = BigDecimal.ONE.add(porcentajeGanancia.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP));
        return precioCompra.multiply(factor).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calcular días para vencer
     */
    public int calcularDiasParaVencer() {
        if (fechaVencimiento == null) return 0;
        long dias = (fechaVencimiento.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
        return (int) dias;
    }

    /**
     * Verificar si el producto está próximo a vencer (30 días)
     */
    public boolean estaPorVencer() {
        if (fechaVencimiento == null) return false;
        int dias = calcularDiasParaVencer();
        return dias <= 30 && dias > 0;
    }

    /**
     * Verificar si el producto está vencido
     */
    public boolean estaVencido() {
        if (fechaVencimiento == null) return false;
        return fechaVencimiento.getTime() < System.currentTimeMillis();
    }

    /**
     * Verificar si tiene stock bajo
     */
    public boolean tieneStockBajo() {
        if (stockActual == null || stockMinimo == null) return false;
        return stockActual <= stockMinimo && stockActual > 0;
    }

    /**
     * Verificar si está agotado
     */
    public boolean estaAgotado() {
        return stockActual == null || stockActual == 0;
    }

    /**
     * Obtener el valor total de este producto en inventario
     */
    public BigDecimal getValorInventario() {
        if (stockActual == null || precioCompra == null) {
            return BigDecimal.ZERO;
        }
        return precioCompra.multiply(new BigDecimal(stockActual));
    }

    /**
     * Obtener descripción del estado del stock
     */
    public String getEstadoStock() {
        if (estaAgotado()) return "AGOTADO";
        if (tieneStockBajo()) return "BAJO";
        return "OK";
    }

    /**
     * Obtener descripción del estado del vencimiento
     */
    public String getEstadoVencimiento() {
        if (estaVencido()) return "VENCIDO";
        if (estaPorVencer()) return "POR_VENCER";
        return "VIGENTE";
    }

    /**
     * Obtener nombre completo del producto (desde catálogo)
     */
    public String getNombreCompleto() {
        if (catalogoProducto == null) return "Producto #" + id;
        StringBuilder sb = new StringBuilder();
        sb.append(catalogoProducto.getNombreComercial() != null ? catalogoProducto.getNombreComercial() : "");
        if (catalogoProducto.getConcentracion() != null && !catalogoProducto.getConcentracion().isEmpty()) {
            sb.append(" ").append(catalogoProducto.getConcentracion());
        }
        return sb.toString();
    }
}
