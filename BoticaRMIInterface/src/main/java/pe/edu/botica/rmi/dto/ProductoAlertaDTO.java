package pe.edu.botica.rmi.dto;

import java.io.Serializable;

public class ProductoAlertaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productoId;
    private String nombreProducto;
    private String lote;
    private int stockActual;
    private int stockMinimo;
    private String fechaVencimiento;
    private int diasParaVencer;
    private String tipoAlerta; // STOCK_BAJO, AGOTADO, POR_VENCER, VENCIDO

    public ProductoAlertaDTO() {}

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public int getDiasParaVencer() { return diasParaVencer; }
    public void setDiasParaVencer(int diasParaVencer) { this.diasParaVencer = diasParaVencer; }

    public String getTipoAlerta() { return tipoAlerta; }
    public void setTipoAlerta(String tipoAlerta) { this.tipoAlerta = tipoAlerta; }
}
