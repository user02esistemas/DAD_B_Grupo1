package rmi.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<Map<String, Object>> obtenerProductosPorVencer(int diasDesde, int diasHasta) {
        String sql = "SELECT p.id, c.nombre_comercial, c.concentracion, c.laboratorio, p.lote, "
                + "p.fecha_vencimiento, p.stock_actual, p.precio_venta, "
                + "DATEDIFF(p.fecha_vencimiento, CURDATE()) AS dias_restantes "
                + "FROM productos p "
                + "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id "
                + "WHERE p.activo = 1 "
                + "AND DATEDIFF(p.fecha_vencimiento, CURDATE()) >= ? "
                + "AND DATEDIFF(p.fecha_vencimiento, CURDATE()) <= ? "
                + "ORDER BY p.fecha_vencimiento ASC";
        List<Map<String, Object>> productos = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, diasDesde);
            ps.setInt(2, diasHasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapearProductoVencimiento(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener productos por vencer", ex);
        }
        return productos;
    }

    public List<Map<String, Object>> obtenerProductosPorVencerRango(String fechaDesde, String fechaHasta) {
        String sql = "SELECT p.id, c.nombre_comercial, c.concentracion, c.laboratorio, p.lote, "
                + "p.fecha_vencimiento, p.stock_actual, p.precio_venta, "
                + "DATEDIFF(p.fecha_vencimiento, CURDATE()) AS dias_restantes "
                + "FROM productos p "
                + "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id "
                + "WHERE p.activo = 1 AND p.fecha_vencimiento >= ? AND p.fecha_vencimiento <= ? "
                + "ORDER BY p.fecha_vencimiento ASC";
        List<Map<String, Object>> productos = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fechaDesde));
            ps.setDate(2, Date.valueOf(fechaHasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapearProductoVencimiento(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener productos por vencer por rango", ex);
        }
        return productos;
    }

    public Map<String, Integer> contarVencimientosPorCriticidad() {
        String sql = "SELECT "
                + "SUM(CASE WHEN DATEDIFF(fecha_vencimiento, CURDATE()) < 0 THEN 1 ELSE 0 END) AS vencidos, "
                + "SUM(CASE WHEN DATEDIFF(fecha_vencimiento, CURDATE()) BETWEEN 0 AND 9 THEN 1 ELSE 0 END) AS critico, "
                + "SUM(CASE WHEN DATEDIFF(fecha_vencimiento, CURDATE()) BETWEEN 10 AND 20 THEN 1 ELSE 0 END) AS moderado, "
                + "SUM(CASE WHEN DATEDIFF(fecha_vencimiento, CURDATE()) BETWEEN 21 AND 30 THEN 1 ELSE 0 END) AS normal "
                + "FROM productos WHERE activo = 1";
        Map<String, Integer> conteo = new HashMap<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                conteo.put("vencidos", rs.getInt("vencidos"));
                conteo.put("critico", rs.getInt("critico"));
                conteo.put("moderado", rs.getInt("moderado"));
                conteo.put("normal", rs.getInt("normal"));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al contar criticidad de vencimientos", ex);
        }
        return conteo;
    }

    public List<Map<String, Object>> listarSesionesCaja(String fechaDesde, String fechaHasta, Long usuarioId) {
        StringBuilder sql = new StringBuilder("SELECT s.*, c.nombre AS caja_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM sesiones_caja s "
                + "INNER JOIN cajas c ON s.caja_id = c.id "
                + "INNER JOIN usuarios u ON s.usuario_id = u.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (fechaDesde != null && !fechaDesde.trim().isEmpty()) {
            sql.append("AND DATE(s.fecha_apertura) >= ? ");
            params.add(Date.valueOf(fechaDesde));
        }
        if (fechaHasta != null && !fechaHasta.trim().isEmpty()) {
            sql.append("AND DATE(s.fecha_apertura) <= ? ");
            params.add(Date.valueOf(fechaHasta));
        }
        if (usuarioId != null) {
            sql.append("AND s.usuario_id = ? ");
            params.add(usuarioId);
        }
        sql.append("ORDER BY s.fecha_apertura DESC");

        List<Map<String, Object>> sesiones = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sesiones.add(mapearSesionCaja(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al listar sesiones de caja", ex);
        }
        return sesiones;
    }

    public Map<String, Object> obtenerDetalleSesionCaja(Long sesionId) {
        Map<String, Object> detalle = new HashMap<>();
        String sqlSesion = "SELECT s.*, c.nombre AS caja_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM sesiones_caja s "
                + "INNER JOIN cajas c ON s.caja_id = c.id "
                + "INNER JOIN usuarios u ON s.usuario_id = u.id WHERE s.id = ?";
        String sqlTotales = "SELECT COUNT(*) AS cantidad_ventas, COALESCE(SUM(total), 0) AS total_general, "
                + "COALESCE(SUM(CASE WHEN metodo_pago = 'EFECTIVO' THEN total ELSE 0 END), 0) AS total_efectivo, "
                + "COALESCE(SUM(CASE WHEN metodo_pago = 'YAPE_PLIN' THEN total ELSE 0 END), 0) AS total_yape_plin, "
                + "COALESCE(SUM(CASE WHEN metodo_pago = 'TARJETA' THEN total ELSE 0 END), 0) AS total_tarjeta, "
                + "COALESCE(SUM(CASE WHEN metodo_pago = 'MIXTO' THEN total ELSE 0 END), 0) AS total_mixto, "
                + "COALESCE(SUM(monto_efectivo), 0) AS efectivo_recibido, COALESCE(SUM(vuelto), 0) AS total_vueltos "
                + "FROM transacciones WHERE sesion_caja_id = ? AND tipo_transaccion_id = 2 AND estado = 'COMPLETADA'";
        try (Connection con = DatabaseConfig.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sqlSesion)) {
                ps.setLong(1, sesionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        detalle.putAll(mapearSesionCaja(rs));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sqlTotales)) {
                ps.setLong(1, sesionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        detalle.put("cantidadVentas", rs.getInt("cantidad_ventas"));
                        detalle.put("totalGeneral", rs.getBigDecimal("total_general"));
                        detalle.put("totalEfectivo", rs.getBigDecimal("total_efectivo"));
                        detalle.put("totalYapePlin", rs.getBigDecimal("total_yape_plin"));
                        detalle.put("totalTarjeta", rs.getBigDecimal("total_tarjeta"));
                        detalle.put("totalMixto", rs.getBigDecimal("total_mixto"));
                        detalle.put("efectivoRecibido", rs.getBigDecimal("efectivo_recibido"));
                        detalle.put("totalVueltos", rs.getBigDecimal("total_vueltos"));
                        java.math.BigDecimal montoInicial = (java.math.BigDecimal) detalle.get("montoInicial");
                        java.math.BigDecimal montoFinal = (java.math.BigDecimal) detalle.get("montoFinal");
                        java.math.BigDecimal efectivoEsperado = montoInicial == null ? null : montoInicial.add(rs.getBigDecimal("efectivo_recibido")).subtract(rs.getBigDecimal("total_vueltos"));
                        detalle.put("efectivoEsperadoCalculado", efectivoEsperado);
                        if (montoFinal != null && efectivoEsperado != null) {
                            detalle.put("diferenciaCaja", montoFinal.subtract(efectivoEsperado));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener detalle de sesion de caja", ex);
        }
        return detalle;
    }

    public List<Map<String, Object>> obtenerVentasSesionCaja(Long sesionId) {
        String sql = "SELECT t.id, t.numero_transaccion, t.fecha, t.total, t.metodo_pago, "
                + "t.monto_efectivo, t.monto_virtual, t.vuelto "
                + "FROM transacciones t WHERE t.sesion_caja_id = ? AND t.tipo_transaccion_id = 2 "
                + "AND t.estado = 'COMPLETADA' ORDER BY t.fecha ASC";
        List<Map<String, Object>> ventas = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, sesionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> venta = new HashMap<>();
                    venta.put("id", rs.getLong("id"));
                    venta.put("numero", rs.getString("numero_transaccion"));
                    venta.put("fecha", rs.getTimestamp("fecha"));
                    venta.put("total", rs.getBigDecimal("total"));
                    venta.put("metodoPago", rs.getString("metodo_pago"));
                    venta.put("montoEfectivo", rs.getBigDecimal("monto_efectivo"));
                    venta.put("montoVirtual", rs.getBigDecimal("monto_virtual"));
                    venta.put("vuelto", rs.getBigDecimal("vuelto"));
                    ventas.add(venta);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener ventas de sesion de caja", ex);
        }
        return ventas;
    }

    private Map<String, Object> mapearSesionCaja(ResultSet rs) throws SQLException {
        Map<String, Object> sesion = new HashMap<>();
        sesion.put("id", rs.getLong("id"));
        sesion.put("cajaNombre", rs.getString("caja_nombre"));
        sesion.put("usuarioNombre", rs.getString("usuario_nombre"));
        sesion.put("fechaApertura", rs.getTimestamp("fecha_apertura"));
        sesion.put("fechaCierre", rs.getTimestamp("fecha_cierre"));
        sesion.put("montoInicial", rs.getBigDecimal("monto_inicial"));
        sesion.put("montoFinal", rs.getBigDecimal("monto_final"));
        sesion.put("totalTransacciones", rs.getBigDecimal("total_transacciones"));
        sesion.put("totalVentasEfectivo", rs.getBigDecimal("total_ventas_efectivo"));
        sesion.put("totalVentasVirtual", rs.getBigDecimal("total_ventas_virtual"));
        sesion.put("efectivoEsperado", rs.getBigDecimal("efectivo_esperado"));
        sesion.put("estado", rs.getString("estado"));
        sesion.put("observaciones", rs.getString("observaciones"));
        return sesion;
    }

    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof Date) {
                ps.setDate(i + 1, (Date) param);
            } else if (param instanceof Long) {
                ps.setLong(i + 1, (Long) param);
            }
        }
    }

    private Map<String, Object> mapearProductoVencimiento(ResultSet rs) throws SQLException {
        Map<String, Object> producto = new HashMap<>();
        producto.put("id", rs.getLong("id"));
        producto.put("nombre", construirNombre(rs.getString("nombre_comercial"), rs.getString("concentracion")));
        producto.put("laboratorio", rs.getString("laboratorio"));
        producto.put("lote", rs.getString("lote"));
        producto.put("fechaVencimiento", rs.getDate("fecha_vencimiento"));
        producto.put("stockActual", rs.getInt("stock_actual"));
        producto.put("precioVenta", rs.getBigDecimal("precio_venta"));
        producto.put("diasRestantes", rs.getInt("dias_restantes"));
        return producto;
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
