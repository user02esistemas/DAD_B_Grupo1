package rmi.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rmi.config.DatabaseConfig;
import rmi.dto.DashboardResumenDTO;
import rmi.dto.ProductoAlertaDTO;

public class DashboardDAO {

    public DashboardResumenDTO obtenerResumenCompleto() {
        DashboardResumenDTO resumen = new DashboardResumenDTO();
        resumen.setVentasHoy(obtenerDecimal("SELECT COALESCE(SUM(total), 0) FROM transacciones WHERE tipo_transaccion_id = 2 AND DATE(fecha) = CURDATE() AND estado = 'COMPLETADA'"));
        resumen.setCantidadVentasHoy(obtenerEntero("SELECT COUNT(*) FROM transacciones WHERE tipo_transaccion_id = 2 AND DATE(fecha) = CURDATE() AND estado = 'COMPLETADA'"));
        resumen.setVentasMes(obtenerDecimal("SELECT COALESCE(SUM(total), 0) FROM transacciones WHERE tipo_transaccion_id = 2 AND MONTH(fecha) = MONTH(CURDATE()) AND YEAR(fecha) = YEAR(CURDATE()) AND estado = 'COMPLETADA'"));
        resumen.setComprasHoy(obtenerDecimal("SELECT COALESCE(SUM(total), 0) FROM transacciones WHERE tipo_transaccion_id = 1 AND DATE(fecha) = CURDATE() AND estado = 'COMPLETADA'"));
        resumen.setTotalProductos(obtenerEntero("SELECT COUNT(*) FROM productos WHERE activo = 1"));
        resumen.setStockBajo(obtenerEntero("SELECT COUNT(*) FROM productos WHERE stock_actual <= stock_minimo AND stock_actual > 0 AND activo = 1"));
        resumen.setAgotados(obtenerEntero("SELECT COUNT(*) FROM productos WHERE stock_actual = 0 AND activo = 1"));
        resumen.setPorVencer(obtenerEntero("SELECT COUNT(*) FROM productos WHERE fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) AND activo = 1"));
        resumen.setVencidos(obtenerEntero("SELECT COUNT(*) FROM productos WHERE fecha_vencimiento < CURDATE() AND activo = 1"));
        return resumen;
    }

    public List<ProductoAlertaDTO> obtenerProductosStockBajo(int limite) {
        String sql = "SELECT p.id, c.nombre_comercial, p.lote, p.stock_actual, p.stock_minimo, p.fecha_vencimiento "
                + "FROM productos p "
                + "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id "
                + "WHERE p.stock_actual <= p.stock_minimo AND p.activo = 1 "
                + "ORDER BY p.stock_actual ASC LIMIT ?";
        return obtenerAlertas(sql, limite, "STOCK_BAJO");
    }

    public List<ProductoAlertaDTO> obtenerProductosPorVencer(int limite) {
        String sql = "SELECT p.id, c.nombre_comercial, p.lote, p.stock_actual, p.stock_minimo, p.fecha_vencimiento "
                + "FROM productos p "
                + "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id "
                + "WHERE p.fecha_vencimiento <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) AND p.activo = 1 "
                + "ORDER BY p.fecha_vencimiento ASC LIMIT ?";
        return obtenerAlertas(sql, limite, "POR_VENCER");
    }

    private BigDecimal obtenerDecimal(String sql) {
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener indicador", ex);
        }
    }

    private int obtenerEntero(String sql) {
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener indicador", ex);
        }
    }

    private List<ProductoAlertaDTO> obtenerAlertas(String sql, int limite, String tipoAlerta) {
        List<ProductoAlertaDTO> alertas = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite <= 0 ? 10 : limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductoAlertaDTO alerta = new ProductoAlertaDTO();
                    alerta.setProductoId(rs.getLong("id"));
                    alerta.setNombreProducto(rs.getString("nombre_comercial"));
                    alerta.setLote(rs.getString("lote"));
                    alerta.setStockActual(rs.getInt("stock_actual"));
                    alerta.setStockMinimo(rs.getInt("stock_minimo"));
                    alerta.setFechaVencimiento(String.valueOf(rs.getDate("fecha_vencimiento")));
                    alerta.setDiasParaVencer(calcularDiasParaVencer(rs.getDate("fecha_vencimiento")));
                    alerta.setTipoAlerta(alerta.getStockActual() == 0 ? "AGOTADO" : tipoAlerta);
                    alertas.add(alerta);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener productos con alerta", ex);
        }
        return alertas;
    }

    private int calcularDiasParaVencer(java.sql.Date fecha) {
        if (fecha == null) {
            return 0;
        }
        long diff = fecha.toLocalDate().toEpochDay() - java.time.LocalDate.now().toEpochDay();
        return (int) diff;
    }
}
