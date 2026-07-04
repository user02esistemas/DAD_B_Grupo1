package pe.edu.botica.rmi.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class DashboardResumenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal ventasHoy;
    private int cantidadVentasHoy;
    private BigDecimal ventasMes;
    private BigDecimal comprasHoy;
    private int totalProductos;
    private int stockBajo;
    private int agotados;
    private int porVencer;
    private int vencidos;
    private List<ProductoVendidoDTO> topProductos;
    private List<ProductoVendidoDTO> topProductosMes;
    private List<VentaDiaDTO> ventasSemana;
    private List<UltimaVentaDTO> ultimasVentas;

    public DashboardResumenDTO() {
        this.ventasHoy = BigDecimal.ZERO;
        this.ventasMes = BigDecimal.ZERO;
        this.comprasHoy = BigDecimal.ZERO;
    }

    public BigDecimal getVentasHoy() { return ventasHoy; }
    public void setVentasHoy(BigDecimal ventasHoy) { this.ventasHoy = ventasHoy; }

    public int getCantidadVentasHoy() { return cantidadVentasHoy; }
    public void setCantidadVentasHoy(int cantidadVentasHoy) { this.cantidadVentasHoy = cantidadVentasHoy; }

    public BigDecimal getVentasMes() { return ventasMes; }
    public void setVentasMes(BigDecimal ventasMes) { this.ventasMes = ventasMes; }

    public BigDecimal getComprasHoy() { return comprasHoy; }
    public void setComprasHoy(BigDecimal comprasHoy) { this.comprasHoy = comprasHoy; }

    public int getTotalProductos() { return totalProductos; }
    public void setTotalProductos(int totalProductos) { this.totalProductos = totalProductos; }

    public int getStockBajo() { return stockBajo; }
    public void setStockBajo(int stockBajo) { this.stockBajo = stockBajo; }

    public int getAgotados() { return agotados; }
    public void setAgotados(int agotados) { this.agotados = agotados; }

    public int getPorVencer() { return porVencer; }
    public void setPorVencer(int porVencer) { this.porVencer = porVencer; }

    public int getVencidos() { return vencidos; }
    public void setVencidos(int vencidos) { this.vencidos = vencidos; }

    public List<ProductoVendidoDTO> getTopProductos() { return topProductos; }
    public void setTopProductos(List<ProductoVendidoDTO> topProductos) { this.topProductos = topProductos; }

    public List<ProductoVendidoDTO> getTopProductosMes() { return topProductosMes; }
    public void setTopProductosMes(List<ProductoVendidoDTO> topProductosMes) { this.topProductosMes = topProductosMes; }

    public List<VentaDiaDTO> getVentasSemana() { return ventasSemana; }
    public void setVentasSemana(List<VentaDiaDTO> ventasSemana) { this.ventasSemana = ventasSemana; }

    public List<UltimaVentaDTO> getUltimasVentas() { return ultimasVentas; }
    public void setUltimasVentas(List<UltimaVentaDTO> ultimasVentas) { this.ultimasVentas = ultimasVentas; }

    public int getTotalAlertas() { return stockBajo + agotados + porVencer + vencidos; }
    public boolean hayAlertasCriticas() { return agotados > 0 || vencidos > 0; }
}
