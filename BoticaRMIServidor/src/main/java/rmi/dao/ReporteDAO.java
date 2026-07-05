package rmi.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rmi.config.DatabaseConfig;
import rmi.dto.ProductoVendidoDTO;
import rmi.dto.ResumenVentasDTO;
import rmi.dto.TransaccionDTO;
import rmi.dto.VentaDiaDTO;

public class ReporteDAO {

    public ResumenVentasDTO obtenerResumenVentas(String fechaInicio, String fechaFin) {
        String sql = "SELECT COUNT(*) as cantidad_ventas, "
                + "COALESCE(SUM(total), 0) as total_general, "
                + "COALESCE(SUM(CASE WHEN metodo_pago = 'EFECTIVO' THEN total ELSE 0 END), 0) as total_efectivo, "
                + "COALESCE(SUM(CASE WHEN metodo_pago = 'YAPE_PLIN' THEN total ELSE 0 END), 0) as total_yape_plin, "
                + "COALESCE(SUM(CASE WHEN metodo_pago = 'TARJETA' THEN total ELSE 0 END), 0) as total_tarjeta, "
                + "COALESCE(SUM(CASE WHEN metodo_pago = 'MIXTO' THEN total ELSE 0 END), 0) as total_mixto, "
                + "COALESCE(AVG(total), 0) as promedio_venta "
                + "FROM transacciones "
                + "WHERE tipo_transaccion_id = 2 AND estado = 'COMPLETADA' "
                + "AND DATE(fecha) >= ? AND DATE(fecha) <= ?";

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            setFechas(ps, fechaInicio, fechaFin);
            try (ResultSet rs = ps.executeQuery()) {
                ResumenVentasDTO resumen = new ResumenVentasDTO();
                if (rs.next()) {
                    resumen.setCantidadVentas(rs.getInt("cantidad_ventas"));
                    resumen.setTotalGeneral(rs.getBigDecimal("total_general"));
                    resumen.setTotalEfectivo(rs.getBigDecimal("total_efectivo"));
                    resumen.setTotalYapePlin(rs.getBigDecimal("total_yape_plin"));
                    resumen.setTotalTarjeta(rs.getBigDecimal("total_tarjeta"));
                    resumen.setTotalMixto(rs.getBigDecimal("total_mixto"));
                    resumen.setPromedioVenta(rs.getBigDecimal("promedio_venta"));
                }
                return resumen;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener resumen de ventas", ex);
        }
    }

    public List<TransaccionDTO> reporteVentasPorFecha(String fechaInicio, String fechaFin) {
        return listarTransaccionesPorFecha(TransaccionDTO.TIPO_VENTA, fechaInicio, fechaFin);
    }

    public List<TransaccionDTO> reporteComprasPorFecha(String fechaInicio, String fechaFin) {
        return listarTransaccionesPorFecha(TransaccionDTO.TIPO_COMPRA, fechaInicio, fechaFin);
    }

    public List<VentaDiaDTO> obtenerVentasPorDia(String fechaInicio, String fechaFin) {
        String sql = "SELECT DATE(fecha) as dia, COUNT(*) as cantidad, COALESCE(SUM(total), 0) as total "
                + "FROM transacciones "
                + "WHERE tipo_transaccion_id = 2 AND estado = 'COMPLETADA' "
                + "AND DATE(fecha) >= ? AND DATE(fecha) <= ? "
                + "GROUP BY DATE(fecha) ORDER BY dia ASC";

        List<VentaDiaDTO> ventas = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            setFechas(ps, fechaInicio, fechaFin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventas.add(new VentaDiaDTO(String.valueOf(rs.getDate("dia")), rs.getBigDecimal("total"), rs.getInt("cantidad")));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener ventas por dia", ex);
        }
        return ventas;
    }

    public List<ProductoVendidoDTO> obtenerProductosMasVendidos(int limite) {
        String sql = "SELECT c.nombre_comercial, c.concentracion, SUM(dt.cantidad) as cantidad_vendida, "
                + "COALESCE(SUM(dt.subtotal), 0) as total_vendido "
                + "FROM detalle_transacciones dt "
                + "INNER JOIN transacciones t ON dt.transaccion_id = t.id "
                + "INNER JOIN productos p ON dt.producto_id = p.id "
                + "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id "
                + "WHERE t.tipo_transaccion_id = 2 AND t.estado = 'COMPLETADA' "
                + "GROUP BY c.id, c.nombre_comercial, c.concentracion "
                + "ORDER BY cantidad_vendida DESC, total_vendido DESC LIMIT ?";

        List<ProductoVendidoDTO> productos = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite <= 0 ? 10 : limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductoVendidoDTO producto = new ProductoVendidoDTO();
                    producto.setNombreProducto(construirNombre(rs.getString("nombre_comercial"), rs.getString("concentracion")));
                    producto.setCantidadVendida(rs.getInt("cantidad_vendida"));
                    producto.setTotalVendido(rs.getBigDecimal("total_vendido"));
                    productos.add(producto);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener productos mas vendidos", ex);
        }
        return productos;
    }

    private List<TransaccionDTO> listarTransaccionesPorFecha(Long tipoTransaccionId, String fechaInicio, String fechaFin) {
        String sql = "SELECT t.*, tt.nombre AS tipo_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM transacciones t "
                + "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id "
                + "LEFT JOIN usuarios u ON t.usuario_id = u.id "
                + "WHERE t.tipo_transaccion_id = ? AND t.estado = 'COMPLETADA' "
                + "AND DATE(t.fecha) >= ? AND DATE(t.fecha) <= ? "
                + "ORDER BY t.fecha DESC";

        List<TransaccionDTO> transacciones = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, tipoTransaccionId);
            ps.setDate(2, Date.valueOf(fechaInicio));
            ps.setDate(3, Date.valueOf(fechaFin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transacciones.add(mapearTransaccion(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al listar transacciones por fecha", ex);
        }
        return transacciones;
    }

    private void setFechas(PreparedStatement ps, String fechaInicio, String fechaFin) throws SQLException {
        ps.setDate(1, Date.valueOf(fechaInicio));
        ps.setDate(2, Date.valueOf(fechaFin));
    }

    private TransaccionDTO mapearTransaccion(ResultSet rs) throws SQLException {
        TransaccionDTO transaccion = new TransaccionDTO();
        transaccion.setId(rs.getLong("id"));
        transaccion.setTipoTransaccionId(rs.getLong("tipo_transaccion_id"));
        transaccion.setNumeroTransaccion(rs.getString("numero_transaccion"));
        transaccion.setUsuarioId(rs.getLong("usuario_id"));
        transaccion.setNombrePersona(rs.getString("nombre_persona"));
        transaccion.setFecha(rs.getTimestamp("fecha"));
        transaccion.setSubtotal(rs.getBigDecimal("subtotal"));
        transaccion.setIgv(rs.getBigDecimal("igv"));
        transaccion.setTotal(rs.getBigDecimal("total"));
        transaccion.setMetodoPago(rs.getString("metodo_pago"));
        transaccion.setMontoEfectivo(rs.getBigDecimal("monto_efectivo"));
        transaccion.setMontoVirtual(rs.getBigDecimal("monto_virtual"));
        transaccion.setMedioPagoVirtual(rs.getString("medio_pago_virtual"));
        transaccion.setVuelto(rs.getBigDecimal("vuelto"));
        transaccion.setTipoComprobante(rs.getString("tipo_comprobante"));
        transaccion.setEstado(rs.getString("estado"));
        transaccion.setObservaciones(rs.getString("observaciones"));
        transaccion.setTipoTransaccionNombre(rs.getString("tipo_nombre"));
        transaccion.setUsuarioNombre(rs.getString("usuario_nombre"));
        return transaccion;
    }

    private String construirNombre(String nombre, String concentracion) {
        if (concentracion == null || concentracion.trim().isEmpty()) {
            return nombre;
        }
        return nombre + " " + concentracion;
    }
}
