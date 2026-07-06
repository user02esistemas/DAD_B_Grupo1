package DTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para el Resumen del Dashboard
 * Contiene todas las estadísticas y métricas del sistema
 */
public class DashboardResumenDTO {

    // Ventas
    private BigDecimal ventasHoy;
    private int cantidadVentasHoy;
    private BigDecimal ventasMes;
    private BigDecimal comprasHoy;

    // Inventario
    private int totalProductos;
    private int stockBajo;
    private int agotados;
    private int porVencer;
    private int vencidos;

    // Top productos
    private List<ProductoVendidoDTO> topProductos;
    private List<ProductoVendidoDTO> topProductosMes;

    // Ventas por día
    private List<VentaDiaDTO> ventasSemana;

    // Últimas ventas
    private List<UltimaVentaDTO> ultimasVentas;

    // Constructor
    public DashboardResumenDTO() {
        this.ventasHoy = BigDecimal.ZERO;
        this.ventasMes = BigDecimal.ZERO;
        this.comprasHoy = BigDecimal.ZERO;
    }

    // =====================================================
    //              MÉTODOS DE CÁLCULO
    // =====================================================

    /**
     * Calcular total de alertas
     */
    public int getTotalAlertas() {
        return stockBajo + agotados + porVencer + vencidos;
    }

    /**
     * Verificar si hay alertas críticas
     */
    public boolean hayAlertasCriticas() {
        return agotados > 0 || vencidos > 0;
    }

    /**
     * Obtener porcentaje de productos con stock bajo
     */
    public double getPorcentajeStockBajo() {
        if (totalProductos == 0) return 0;
        return (double) stockBajo / totalProductos * 100;
    }

    /**
     * Obtener porcentaje de productos por vencer
     */
    public double getPorcentajePorVencer() {
        if (totalProductos == 0) return 0;
        return (double) porVencer / totalProductos * 100;
    }

    // =====================================================
    //              GETTERS Y SETTERS
    // =====================================================

    public BigDecimal getVentasHoy() {
        return ventasHoy;
    }

    public void setVentasHoy(BigDecimal ventasHoy) {
        this.ventasHoy = ventasHoy;
    }

    public int getCantidadVentasHoy() {
        return cantidadVentasHoy;
    }

    public void setCantidadVentasHoy(int cantidadVentasHoy) {
        this.cantidadVentasHoy = cantidadVentasHoy;
    }

    public BigDecimal getVentasMes() {
        return ventasMes;
    }

    public void setVentasMes(BigDecimal ventasMes) {
        this.ventasMes = ventasMes;
    }

    public BigDecimal getComprasHoy() {
        return comprasHoy;
    }

    public void setComprasHoy(BigDecimal comprasHoy) {
        this.comprasHoy = comprasHoy;
    }

    public int getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(int totalProductos) {
        this.totalProductos = totalProductos;
    }

    public int getStockBajo() {
        return stockBajo;
    }

    public void setStockBajo(int stockBajo) {
        this.stockBajo = stockBajo;
    }

    public int getAgotados() {
        return agotados;
    }

    public void setAgotados(int agotados) {
        this.agotados = agotados;
    }

    public int getPorVencer() {
        return porVencer;
    }

    public void setPorVencer(int porVencer) {
        this.porVencer = porVencer;
    }

    public int getVencidos() {
        return vencidos;
    }

    public void setVencidos(int vencidos) {
        this.vencidos = vencidos;
    }

    public List<ProductoVendidoDTO> getTopProductos() {
        return topProductos;
    }

    public void setTopProductos(List<ProductoVendidoDTO> topProductos) {
        this.topProductos = topProductos;
    }

    public List<ProductoVendidoDTO> getTopProductosMes() {
        return topProductosMes;
    }

    public void setTopProductosMes(List<ProductoVendidoDTO> topProductosMes) {
        this.topProductosMes = topProductosMes;
    }

    public List<VentaDiaDTO> getVentasSemana() {
        return ventasSemana;
    }

    public void setVentasSemana(List<VentaDiaDTO> ventasSemana) {
        this.ventasSemana = ventasSemana;
    }

    public List<UltimaVentaDTO> getUltimasVentas() {
        return ultimasVentas;
    }

    public void setUltimasVentas(List<UltimaVentaDTO> ultimasVentas) {
        this.ultimasVentas = ultimasVentas;
    }
}
