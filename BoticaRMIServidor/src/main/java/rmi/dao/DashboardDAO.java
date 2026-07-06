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
import rmi.dto.ProductoVendidoDTO;
import rmi.dto.UltimaVentaDTO;
import rmi.dto.VentaDiaDTO;

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
        resumen.setTopProductos(obtenerTopProductosVendidos(5, false));
        resumen.setTopProductosMes(obtenerTopProductosVendidos(5, true));
        resumen.setVentasSemana(obtenerVentasUltimos7Dias());
        resumen.setUltimasVentas(obtenerUltimasVentas(5));
        return resumen;
    }

    private List<ProductoVendidoDTO> obtenerTopProductosVendidos(int limite, boolean soloMesActual) {
        String sql = "SELECT c.nombre_comercial, c.concentracion, SUM(dt.cantidad) AS cantidad_vendida, "
                + "SUM(dt.subtotal) AS total_vendido "
                + "FROM detalle_transacciones dt "
                + "INNER JOIN transacciones t ON dt.transaccion_id = t.id "
                + "INNER JOIN productos p ON dt.producto_id = p.id "
                + "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id "
                + "WHERE t.tipo_transaccion_id = 2 AND t.estado = 'COMPLETADA' "
                + (soloMesActual ? "AND MONTH(t.fecha) = MONTH(CURDATE()) AND YEAR(t.fecha) = YEAR(CURDATE()) " : "")
                + "GROUP BY p.catalogo_producto_id, c.nombre_comercial, c.concentracion "
                + "ORDER BY cantidad_vendida DESC LIMIT ?";
        List<ProductoVendidoDTO> productos = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite <= 0 ? 5 : limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductoVendidoDTO producto = new ProductoVendidoDTO();
                    producto.setNombreProducto(nombreProducto(rs.getString("nombre_comercial"), rs.getString("concentracion"), soloMesActual ? 25 : 30));
                    producto.setCantidadVendida(rs.getInt("cantidad_vendida"));
                    producto.setTotalVendido(rs.getBigDecimal("total_vendido"));
                    productos.add(producto);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener top productos", ex);
        }
        return productos;
    }

    private List<VentaDiaDTO> obtenerVentasUltimos7Dias() {
        String sql = "SELECT DATE(fecha) AS dia, COALESCE(SUM(total), 0) AS total_ventas, COUNT(*) AS cantidad "
                + "FROM transacciones "
                + "WHERE tipo_transaccion_id = 2 AND estado = 'COMPLETADA' "
                + "AND fecha >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) "
                + "GROUP BY DATE(fecha) ORDER BY dia ASC";
        List<VentaDiaDTO> ventas = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ventas.add(new VentaDiaDTO(String.valueOf(rs.getDate("dia")), rs.getBigDecimal("total_ventas"), rs.getInt("cantidad")));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener ventas semanales", ex);
        }
        return ventas;
    }

    private List<UltimaVentaDTO> obtenerUltimasVentas(int limite) {
        String sql = "SELECT t.numero_transaccion, t.fecha, t.total, t.metodo_pago, u.nombre_completo AS usuario "
                + "FROM transacciones t "
                + "INNER JOIN usuarios u ON t.usuario_id = u.id "
                + "WHERE t.tipo_transaccion_id = 2 AND t.estado = 'COMPLETADA' "
                + "ORDER BY t.fecha DESC LIMIT ?";
        List<UltimaVentaDTO> ventas = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite <= 0 ? 5 : limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UltimaVentaDTO venta = new UltimaVentaDTO();
                    venta.setNumeroTransaccion(rs.getString("numero_transaccion"));
                    venta.setFecha(String.valueOf(rs.getTimestamp("fecha")));
                    venta.setTotal(rs.getBigDecimal("total"));
                    venta.setMetodoPago(rs.getString("metodo_pago"));
                    venta.setVendedor(rs.getString("usuario"));
                    ventas.add(venta);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener ultimas ventas", ex);
        }
        return ventas;
    }

    private String nombreProducto(String nombre, String concentracion, int maxLength) {
        String resultado = nombre == null ? "" : nombre;
        if (concentracion != null && !concentracion.isEmpty()) {
            resultado += " " + concentracion;
        }
        return resultado.length() > maxLength ? resultado.substring(0, maxLength - 3) + "..." : resultado;
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
