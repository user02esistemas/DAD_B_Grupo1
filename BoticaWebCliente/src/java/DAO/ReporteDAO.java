package DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO para Reportes del Sistema
 * Consultas especializadas para generación de reportes
 */
public class ReporteDAO {

    // =====================================================
    //     REPORTE DE PRODUCTOS POR VENCER
    // =====================================================

    /**
     * Obtener productos próximos a vencer
     * @param diasDesde Días mínimos para vencer (0 = hoy)
     * @param diasHasta Días máximos para vencer (30 = un mes)
     */
    public List<Map<String, Object>> obtenerProductosPorVencer(int diasDesde, int diasHasta) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT p.id, c.nombre_comercial, c.concentracion, c.laboratorio, " +
                    "p.lote, p.fecha_vencimiento, p.stock_actual, p.precio_venta, " +
                    "DATEDIFF(p.fecha_vencimiento, CURDATE()) as dias_restantes " +
                    "FROM productos p " +
                    "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                    "WHERE p.activo = 1 " +
                    "AND DATEDIFF(p.fecha_vencimiento, CURDATE()) >= ? " +
                    "AND DATEDIFF(p.fecha_vencimiento, CURDATE()) <= ? " +
                    "ORDER BY p.fecha_vencimiento ASC";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, diasDesde);
            pstm.setInt(2, diasHasta);
            rs = pstm.executeQuery();

            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                producto.put("id", rs.getLong("id"));
                
                String nombre = rs.getString("nombre_comercial");
                String concentracion = rs.getString("concentracion");
                if (concentracion != null && !concentracion.isEmpty()) {
                    nombre += " " + concentracion;
                }
                producto.put("nombre", nombre);
                producto.put("laboratorio", rs.getString("laboratorio"));
                producto.put("lote", rs.getString("lote"));
                producto.put("fechaVencimiento", rs.getDate("fecha_vencimiento"));
                producto.put("stockActual", rs.getInt("stock_actual"));
                producto.put("precioVenta", rs.getBigDecimal("precio_venta"));
                producto.put("diasRestantes", rs.getInt("dias_restantes"));
                lista.add(producto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener productos por vencer: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener productos por vencer con filtro de fechas específicas
     */
    public List<Map<String, Object>> obtenerProductosPorVencerRango(java.util.Date fechaDesde, java.util.Date fechaHasta) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT p.id, c.nombre_comercial, c.concentracion, c.laboratorio, " +
                    "p.lote, p.fecha_vencimiento, p.stock_actual, p.precio_venta, " +
                    "DATEDIFF(p.fecha_vencimiento, CURDATE()) as dias_restantes " +
                    "FROM productos p " +
                    "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                    "WHERE p.activo = 1 " +
                    "AND p.fecha_vencimiento >= ? " +
                    "AND p.fecha_vencimiento <= ? " +
                    "ORDER BY p.fecha_vencimiento ASC";

            pstm = con.prepareStatement(sql);
            pstm.setDate(1, new java.sql.Date(fechaDesde.getTime()));
            pstm.setDate(2, new java.sql.Date(fechaHasta.getTime()));
            rs = pstm.executeQuery();

            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                producto.put("id", rs.getLong("id"));
                
                String nombre = rs.getString("nombre_comercial");
                String concentracion = rs.getString("concentracion");
                if (concentracion != null && !concentracion.isEmpty()) {
                    nombre += " " + concentracion;
                }
                producto.put("nombre", nombre);
                producto.put("laboratorio", rs.getString("laboratorio"));
                producto.put("lote", rs.getString("lote"));
                producto.put("fechaVencimiento", rs.getDate("fecha_vencimiento"));
                producto.put("stockActual", rs.getInt("stock_actual"));
                producto.put("precioVenta", rs.getBigDecimal("precio_venta"));
                producto.put("diasRestantes", rs.getInt("dias_restantes"));
                lista.add(producto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener productos por vencer: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar productos por criticidad de vencimiento
     */
    public Map<String, Integer> contarPorCriticidad() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        Map<String, Integer> conteo = new HashMap<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT " +
                    "SUM(CASE WHEN DATEDIFF(fecha_vencimiento, CURDATE()) < 0 THEN 1 ELSE 0 END) as vencidos, " +
                    "SUM(CASE WHEN DATEDIFF(fecha_vencimiento, CURDATE()) BETWEEN 0 AND 9 THEN 1 ELSE 0 END) as critico, " +
                    "SUM(CASE WHEN DATEDIFF(fecha_vencimiento, CURDATE()) BETWEEN 10 AND 20 THEN 1 ELSE 0 END) as moderado, " +
                    "SUM(CASE WHEN DATEDIFF(fecha_vencimiento, CURDATE()) BETWEEN 21 AND 30 THEN 1 ELSE 0 END) as normal " +
                    "FROM productos WHERE activo = 1";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                conteo.put("vencidos", rs.getInt("vencidos"));
                conteo.put("critico", rs.getInt("critico"));
                conteo.put("moderado", rs.getInt("moderado"));
                conteo.put("normal", rs.getInt("normal"));
            }
            return conteo;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar criticidad: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    // =====================================================
    //     REPORTE DE SESIONES DE CAJA
    // =====================================================

    /**
     * Listar sesiones de caja con filtros
     */
    public List<Map<String, Object>> listarSesionesCaja(java.util.Date fechaDesde, java.util.Date fechaHasta, Long usuarioId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT s.*, c.nombre as caja_nombre, u.nombre_completo as usuario_nombre ");
            sql.append("FROM sesiones_caja s ");
            sql.append("INNER JOIN cajas c ON s.caja_id = c.id ");
            sql.append("INNER JOIN usuarios u ON s.usuario_id = u.id ");
            sql.append("WHERE 1=1 ");

            List<Object> params = new ArrayList<>();

            if (fechaDesde != null) {
                sql.append("AND DATE(s.fecha_apertura) >= ? ");
                params.add(new java.sql.Date(fechaDesde.getTime()));
            }
            if (fechaHasta != null) {
                sql.append("AND DATE(s.fecha_apertura) <= ? ");
                params.add(new java.sql.Date(fechaHasta.getTime()));
            }
            if (usuarioId != null) {
                sql.append("AND s.usuario_id = ? ");
                params.add(usuarioId);
            }

            sql.append("ORDER BY s.fecha_apertura DESC");

            pstm = con.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof java.sql.Date) {
                    pstm.setDate(i + 1, (java.sql.Date) param);
                } else if (param instanceof Long) {
                    pstm.setLong(i + 1, (Long) param);
                }
            }

            rs = pstm.executeQuery();

            while (rs.next()) {
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
                lista.add(sesion);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar sesiones: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener detalle completo de una sesión de caja
     */
    public Map<String, Object> obtenerDetalleSesionCaja(Long sesionId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        Map<String, Object> resultado = new HashMap<>();

        try {
            con = Conexion.getConnection();

            // 1. Datos de la sesión
            String sqlSesion = "SELECT s.*, c.nombre as caja_nombre, u.nombre_completo as usuario_nombre " +
                    "FROM sesiones_caja s " +
                    "INNER JOIN cajas c ON s.caja_id = c.id " +
                    "INNER JOIN usuarios u ON s.usuario_id = u.id " +
                    "WHERE s.id = ?";

            pstm = con.prepareStatement(sqlSesion);
            pstm.setLong(1, sesionId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                resultado.put("id", rs.getLong("id"));
                resultado.put("cajaNombre", rs.getString("caja_nombre"));
                resultado.put("usuarioNombre", rs.getString("usuario_nombre"));
                resultado.put("fechaApertura", rs.getTimestamp("fecha_apertura"));
                resultado.put("fechaCierre", rs.getTimestamp("fecha_cierre"));
                resultado.put("montoInicial", rs.getBigDecimal("monto_inicial"));
                resultado.put("montoFinal", rs.getBigDecimal("monto_final"));
                resultado.put("totalTransacciones", rs.getBigDecimal("total_transacciones"));
                resultado.put("totalVentasEfectivo", rs.getBigDecimal("total_ventas_efectivo"));
                resultado.put("totalVentasVirtual", rs.getBigDecimal("total_ventas_virtual"));
                resultado.put("efectivoEsperado", rs.getBigDecimal("efectivo_esperado"));
                resultado.put("estado", rs.getString("estado"));
                resultado.put("observaciones", rs.getString("observaciones"));
            }
            rs.close();
            pstm.close();

            // 2. Calcular totales por método de pago
            String sqlTotales = "SELECT " +
                    "COUNT(*) as cantidad_ventas, " +
                    "COALESCE(SUM(total), 0) as total_general, " +
                    "COALESCE(SUM(CASE WHEN metodo_pago = 'EFECTIVO' THEN total ELSE 0 END), 0) as total_efectivo, " +
                    "COALESCE(SUM(CASE WHEN metodo_pago = 'YAPE_PLIN' THEN total ELSE 0 END), 0) as total_yape_plin, " +
                    "COALESCE(SUM(CASE WHEN metodo_pago = 'TARJETA' THEN total ELSE 0 END), 0) as total_tarjeta, " +
                    "COALESCE(SUM(CASE WHEN metodo_pago = 'MIXTO' THEN total ELSE 0 END), 0) as total_mixto, " +
                    "COALESCE(SUM(monto_efectivo), 0) as efectivo_recibido, " +
                    "COALESCE(SUM(vuelto), 0) as total_vueltos " +
                    "FROM transacciones " +
                    "WHERE sesion_caja_id = ? AND tipo_transaccion_id = 2 AND estado = 'COMPLETADA'";

            pstm = con.prepareStatement(sqlTotales);
            pstm.setLong(1, sesionId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                resultado.put("cantidadVentas", rs.getInt("cantidad_ventas"));
                resultado.put("totalGeneral", rs.getBigDecimal("total_general"));
                resultado.put("totalEfectivo", rs.getBigDecimal("total_efectivo"));
                resultado.put("totalYapePlin", rs.getBigDecimal("total_yape_plin"));
                resultado.put("totalTarjeta", rs.getBigDecimal("total_tarjeta"));
                resultado.put("totalMixto", rs.getBigDecimal("total_mixto"));
                resultado.put("efectivoRecibido", rs.getBigDecimal("efectivo_recibido"));
                resultado.put("totalVueltos", rs.getBigDecimal("total_vueltos"));

                // Calcular diferencia de caja
                BigDecimal montoInicial = (BigDecimal) resultado.get("montoInicial");
                BigDecimal efectivoRecibido = rs.getBigDecimal("efectivo_recibido");
                BigDecimal totalVueltos = rs.getBigDecimal("total_vueltos");
                BigDecimal montoFinal = (BigDecimal) resultado.get("montoFinal");

                if (montoInicial != null && efectivoRecibido != null) {
                    BigDecimal efectivoEsperado = montoInicial.add(efectivoRecibido).subtract(totalVueltos);
                    resultado.put("efectivoEsperadoCalculado", efectivoEsperado);

                    if (montoFinal != null) {
                        resultado.put("diferenciaCaja", montoFinal.subtract(efectivoEsperado));
                    }
                }
            }

            return resultado;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener detalle sesión: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener ventas de una sesión de caja
     */
    public List<Map<String, Object>> obtenerVentasSesion(Long sesionId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT t.id, t.numero_transaccion, t.fecha, t.total, " +
                    "t.metodo_pago, t.monto_efectivo, t.monto_virtual, t.vuelto " +
                    "FROM transacciones t " +
                    "WHERE t.sesion_caja_id = ? " +
                    "AND t.tipo_transaccion_id = 2 " +
                    "AND t.estado = 'COMPLETADA' " +
                    "ORDER BY t.fecha ASC";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, sesionId);
            rs = pstm.executeQuery();

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
                lista.add(venta);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener ventas sesión: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    // =====================================================
    //     REPORTE DE VENTAS POR FECHA
    // =====================================================

    /**
     * Obtener resumen de ventas por rango de fechas
     */
    public Map<String, Object> obtenerResumenVentas(java.util.Date fechaDesde, java.util.Date fechaHasta) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        Map<String, Object> resumen = new HashMap<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT " +
                    "COUNT(*) as cantidad_ventas, " +
                    "COALESCE(SUM(total), 0) as total_general, " +
                    "COALESCE(SUM(CASE WHEN metodo_pago = 'EFECTIVO' THEN total ELSE 0 END), 0) as total_efectivo, " +
                    "COALESCE(SUM(CASE WHEN metodo_pago = 'YAPE_PLIN' THEN total ELSE 0 END), 0) as total_yape_plin, " +
                    "COALESCE(SUM(CASE WHEN metodo_pago = 'TARJETA' THEN total ELSE 0 END), 0) as total_tarjeta, " +
                    "COALESCE(SUM(CASE WHEN metodo_pago = 'MIXTO' THEN total ELSE 0 END), 0) as total_mixto, " +
                    "COALESCE(AVG(total), 0) as promedio_venta " +
                    "FROM transacciones " +
                    "WHERE tipo_transaccion_id = 2 " +
                    "AND estado = 'COMPLETADA' " +
                    "AND DATE(fecha) >= ? " +
                    "AND DATE(fecha) <= ?";

            pstm = con.prepareStatement(sql);
            pstm.setDate(1, new java.sql.Date(fechaDesde.getTime()));
            pstm.setDate(2, new java.sql.Date(fechaHasta.getTime()));
            rs = pstm.executeQuery();

            if (rs.next()) {
                resumen.put("cantidadVentas", rs.getInt("cantidad_ventas"));
                resumen.put("totalGeneral", rs.getBigDecimal("total_general"));
                resumen.put("totalEfectivo", rs.getBigDecimal("total_efectivo"));
                resumen.put("totalYapePlin", rs.getBigDecimal("total_yape_plin"));
                resumen.put("totalTarjeta", rs.getBigDecimal("total_tarjeta"));
                resumen.put("totalMixto", rs.getBigDecimal("total_mixto"));
                resumen.put("promedioVenta", rs.getBigDecimal("promedio_venta"));
            }
            return resumen;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener resumen ventas: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar ventas por rango de fechas
     */
    public List<Map<String, Object>> listarVentas(java.util.Date fechaDesde, java.util.Date fechaHasta) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT t.id, t.numero_transaccion, t.fecha, t.total, " +
                    "t.metodo_pago, t.monto_efectivo, t.monto_virtual, t.vuelto, " +
                    "u.nombre_completo as cajero " +
                    "FROM transacciones t " +
                    "INNER JOIN usuarios u ON t.usuario_id = u.id " +
                    "WHERE t.tipo_transaccion_id = 2 " +
                    "AND t.estado = 'COMPLETADA' " +
                    "AND DATE(t.fecha) >= ? " +
                    "AND DATE(t.fecha) <= ? " +
                    "ORDER BY t.fecha DESC";

            pstm = con.prepareStatement(sql);
            pstm.setDate(1, new java.sql.Date(fechaDesde.getTime()));
            pstm.setDate(2, new java.sql.Date(fechaHasta.getTime()));
            rs = pstm.executeQuery();

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
                venta.put("cajero", rs.getString("cajero"));
                lista.add(venta);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar ventas: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener ventas agrupadas por día
     */
    public List<Map<String, Object>> obtenerVentasPorDia(java.util.Date fechaDesde, java.util.Date fechaHasta) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT DATE(fecha) as dia, " +
                    "COUNT(*) as cantidad, " +
                    "SUM(total) as total " +
                    "FROM transacciones " +
                    "WHERE tipo_transaccion_id = 2 " +
                    "AND estado = 'COMPLETADA' " +
                    "AND DATE(fecha) >= ? " +
                    "AND DATE(fecha) <= ? " +
                    "GROUP BY DATE(fecha) " +
                    "ORDER BY dia ASC";

            pstm = con.prepareStatement(sql);
            pstm.setDate(1, new java.sql.Date(fechaDesde.getTime()));
            pstm.setDate(2, new java.sql.Date(fechaHasta.getTime()));
            rs = pstm.executeQuery();

            while (rs.next()) {
                Map<String, Object> dia = new HashMap<>();
                dia.put("fecha", rs.getDate("dia"));
                dia.put("cantidad", rs.getInt("cantidad"));
                dia.put("total", rs.getBigDecimal("total"));
                lista.add(dia);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener ventas por día: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar usuarios para filtros
     */
    public List<Map<String, Object>> listarUsuariosActivos() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT id, nombre_completo FROM usuarios WHERE activo = 1 ORDER BY nombre_completo";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                Map<String, Object> usuario = new HashMap<>();
                usuario.put("id", rs.getLong("id"));
                usuario.put("nombre", rs.getString("nombre_completo"));
                lista.add(usuario);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar usuarios: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    private void cerrarRecursos(ResultSet rs, PreparedStatement pstm) {
        try {
            if (rs != null) rs.close();
            if (pstm != null) pstm.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
