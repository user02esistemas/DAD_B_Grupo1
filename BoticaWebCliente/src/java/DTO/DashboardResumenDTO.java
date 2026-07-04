package DTO;

import DAO.DashboardDAO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    //              MÉTODOS DE NEGOCIO (DTO)
    // =====================================================

    /**
     * Cargar resumen completo del dashboard
     */
    public static DashboardResumenDTO cargarResumenCompleto() {
        DashboardDAO dao = new DashboardDAO();
        DashboardResumenDTO resumen = new DashboardResumenDTO();

        // Ventas
        resumen.setVentasHoy(dao.obtenerVentasDelDia());
        resumen.setCantidadVentasHoy(dao.obtenerCantidadVentasDelDia());
        resumen.setVentasMes(dao.obtenerVentasDelMes());
        resumen.setComprasHoy(dao.obtenerComprasDelDia());

        // Inventario
        resumen.setTotalProductos(dao.contarTotalProductos());
        resumen.setStockBajo(dao.contarProductosStockBajo());
        resumen.setAgotados(dao.contarProductosAgotados());
        resumen.setPorVencer(dao.contarProductosPorVencer());
        resumen.setVencidos(dao.contarProductosVencidos());

        // Top productos (convertir de Map a DTO)
        resumen.setTopProductos(ProductoVendidoDTO.fromMapList(dao.obtenerTopProductosVendidos(5)));
        resumen.setTopProductosMes(ProductoVendidoDTO.fromMapList(dao.obtenerTopProductosMes(5)));

        // Ventas semana
        resumen.setVentasSemana(VentaDiaDTO.fromMapList(dao.obtenerVentasUltimos7Dias()));

        // Últimas ventas
        resumen.setUltimasVentas(UltimaVentaDTO.fromMapList(dao.obtenerUltimasVentas(5)));

        return resumen;
    }

    /**
     * Obtener solo las ventas del día
     */
    public static BigDecimal obtenerVentasHoy() {
        return new DashboardDAO().obtenerVentasDelDia();
    }

    /**
     * Obtener cantidad de ventas del día
     */
    public static int obtenerCantidadVentasHoy() {
        return new DashboardDAO().obtenerCantidadVentasDelDia();
    }

    /**
     * Obtener ventas del mes
     */
    public static BigDecimal obtenerVentasMes() {
        return new DashboardDAO().obtenerVentasDelMes();
    }

    /**
     * Obtener compras del día
     */
    public static BigDecimal obtenerComprasHoy() {
        return new DashboardDAO().obtenerComprasDelDia();
    }

    /**
     * Obtener conteo de productos con stock bajo
     */
    public static int obtenerStockBajo() {
        return new DashboardDAO().contarProductosStockBajo();
    }

    /**
     * Obtener conteo de productos agotados
     */
    public static int obtenerAgotados() {
        return new DashboardDAO().contarProductosAgotados();
    }

    /**
     * Obtener conteo de productos por vencer
     */
    public static int obtenerPorVencer() {
        return new DashboardDAO().contarProductosPorVencer();
    }

    /**
     * Obtener conteo de productos vencidos
     */
    public static int obtenerVencidos() {
        return new DashboardDAO().contarProductosVencidos();
    }

    /**
     * Obtener total de productos
     */
    public static int obtenerTotalProductos() {
        return new DashboardDAO().contarTotalProductos();
    }

    /**
     * Obtener top productos vendidos
     */
    public static List<ProductoVendidoDTO> obtenerTopProductos(int limite) {
        return ProductoVendidoDTO.fromMapList(new DashboardDAO().obtenerTopProductosVendidos(limite));
    }

    /**
     * Obtener top productos del mes
     */
    public static List<ProductoVendidoDTO> obtenerTopProductosMes(int limite) {
        return ProductoVendidoDTO.fromMapList(new DashboardDAO().obtenerTopProductosMes(limite));
    }

    /**
     * Obtener ventas de los últimos 7 días
     */
    public static List<VentaDiaDTO> obtenerVentasSemana() {
        return VentaDiaDTO.fromMapList(new DashboardDAO().obtenerVentasUltimos7Dias());
    }

    /**
     * Obtener últimas ventas
     */
    public static List<UltimaVentaDTO> obtenerUltimasVentas(int limite) {
        return UltimaVentaDTO.fromMapList(new DashboardDAO().obtenerUltimasVentas(limite));
    }

    /**
     * Obtener productos con stock bajo (detalle)
     */
    public static List<ProductoAlertaDTO> obtenerProductosStockBajo(int limite) {
        return ProductoAlertaDTO.fromStockBajoMapList(new DashboardDAO().obtenerProductosStockBajo(limite));
    }

    /**
     * Obtener productos por vencer (detalle)
     */
    public static List<ProductoAlertaDTO> obtenerProductosPorVencer(int limite) {
        return ProductoAlertaDTO.fromPorVencerMapList(new DashboardDAO().obtenerProductosPorVencer(limite));
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
