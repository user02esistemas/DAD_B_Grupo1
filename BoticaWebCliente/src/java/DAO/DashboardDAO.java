package DAO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO para consultas del Dashboard
 * Proporciona estadísticas y métricas del sistema
 */
public class DashboardDAO {

    /**
     * Obtener total de ventas del día
     */
    public BigDecimal obtenerVentasDelDia() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COALESCE(SUM(total), 0) as total_ventas " +
                    "FROM transacciones " +
                    "WHERE tipo_transaccion_id = 2 " +
                    "AND DATE(fecha) = CURDATE() " +
                    "AND estado = 'COMPLETADA'";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total_ventas");
            }
            return BigDecimal.ZERO;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener ventas del día: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener cantidad de ventas del día
     */
    public int obtenerCantidadVentasDelDia() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as cantidad " +
                    "FROM transacciones " +
                    "WHERE tipo_transaccion_id = 2 " +
                    "AND DATE(fecha) = CURDATE() " +
                    "AND estado = 'COMPLETADA'";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("cantidad");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener cantidad de ventas: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener total de compras del día
     */
    public BigDecimal obtenerComprasDelDia() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COALESCE(SUM(total), 0) as total_compras " +
                    "FROM transacciones " +
                    "WHERE tipo_transaccion_id = 1 " +
                    "AND DATE(fecha) = CURDATE() " +
                    "AND estado = 'COMPLETADA'";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total_compras");
            }
            return BigDecimal.ZERO;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener compras del día: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar productos con stock bajo (stock_actual <= stock_minimo)
     */
    public int contarProductosStockBajo() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as cantidad " +
                    "FROM productos " +
                    "WHERE stock_actual <= stock_minimo " +
                    "AND stock_actual > 0 " +
                    "AND activo = 1";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("cantidad");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar stock bajo: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar productos agotados (stock_actual = 0)
     */
    public int contarProductosAgotados() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as cantidad " +
                    "FROM productos " +
                    "WHERE stock_actual = 0 " +
                    "AND activo = 1";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("cantidad");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar agotados: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar productos por vencer (en los próximos 30 días)
     */
    public int contarProductosPorVencer() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as cantidad " +
                    "FROM productos " +
                    "WHERE fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) " +
                    "AND activo = 1";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("cantidad");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar por vencer: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar productos vencidos
     */
    public int contarProductosVencidos() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as cantidad " +
                    "FROM productos " +
                    "WHERE fecha_vencimiento < CURDATE() " +
                    "AND activo = 1";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("cantidad");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar vencidos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener TOP 5 productos más vendidos (general)
     * Retorna lista de mapas con: nombre, cantidad, total
     */
    public List<Map<String, Object>> obtenerTopProductosVendidos(int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT c.nombre_comercial, c.concentracion, " +
                    "SUM(dt.cantidad) as cantidad_vendida, " +
                    "SUM(dt.subtotal) as total_vendido " +
                    "FROM detalle_transacciones dt " +
                    "INNER JOIN transacciones t ON dt.transaccion_id = t.id " +
                    "INNER JOIN productos p ON dt.producto_id = p.id " +
                    "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                    "WHERE t.tipo_transaccion_id = 2 " +
                    "AND t.estado = 'COMPLETADA' " +
                    "GROUP BY p.catalogo_producto_id, c.nombre_comercial, c.concentracion " +
                    "ORDER BY cantidad_vendida DESC " +
                    "LIMIT ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, limite);
            rs = pstm.executeQuery();

            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                String nombre = rs.getString("nombre_comercial");
                String concentracion = rs.getString("concentracion");
                if (concentracion != null && !concentracion.isEmpty()) {
                    nombre += " " + concentracion;
                }
                // Truncar nombre si es muy largo
                if (nombre.length() > 30) {
                    nombre = nombre.substring(0, 27) + "...";
                }
                producto.put("nombre", nombre);
                producto.put("cantidad", rs.getInt("cantidad_vendida"));
                producto.put("total", rs.getBigDecimal("total_vendido"));
                lista.add(producto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener top productos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener TOP 5 productos más vendidos del mes actual
     */
    public List<Map<String, Object>> obtenerTopProductosMes(int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT c.nombre_comercial, c.concentracion, " +
                    "SUM(dt.cantidad) as cantidad_vendida, " +
                    "SUM(dt.subtotal) as total_vendido " +
                    "FROM detalle_transacciones dt " +
                    "INNER JOIN transacciones t ON dt.transaccion_id = t.id " +
                    "INNER JOIN productos p ON dt.producto_id = p.id " +
                    "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                    "WHERE t.tipo_transaccion_id = 2 " +
                    "AND t.estado = 'COMPLETADA' " +
                    "AND MONTH(t.fecha) = MONTH(CURDATE()) " +
                    "AND YEAR(t.fecha) = YEAR(CURDATE()) " +
                    "GROUP BY p.catalogo_producto_id, c.nombre_comercial, c.concentracion " +
                    "ORDER BY cantidad_vendida DESC " +
                    "LIMIT ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, limite);
            rs = pstm.executeQuery();

            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                String nombre = rs.getString("nombre_comercial");
                String concentracion = rs.getString("concentracion");
                if (concentracion != null && !concentracion.isEmpty()) {
                    nombre += " " + concentracion;
                }
                // Truncar nombre si es muy largo
                if (nombre.length() > 25) {
                    nombre = nombre.substring(0, 22) + "...";
                }
                producto.put("nombre", nombre);
                producto.put("cantidad", rs.getInt("cantidad_vendida"));
                producto.put("total", rs.getBigDecimal("total_vendido"));
                lista.add(producto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener top del mes: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener ventas de los últimos 7 días
     * Retorna lista de mapas con: fecha, total
     */
    public List<Map<String, Object>> obtenerVentasUltimos7Dias() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT DATE(fecha) as dia, COALESCE(SUM(total), 0) as total_ventas " +
                    "FROM transacciones " +
                    "WHERE tipo_transaccion_id = 2 " +
                    "AND estado = 'COMPLETADA' " +
                    "AND fecha >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                    "GROUP BY DATE(fecha) " +
                    "ORDER BY dia ASC";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                Map<String, Object> dia = new HashMap<>();
                dia.put("fecha", rs.getDate("dia"));
                dia.put("total", rs.getBigDecimal("total_ventas"));
                lista.add(dia);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener ventas 7 días: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener últimas transacciones (ventas)
     */
    public List<Map<String, Object>> obtenerUltimasVentas(int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT t.numero_transaccion, t.fecha, t.total, t.metodo_pago, " +
                    "u.nombre_completo as usuario " +
                    "FROM transacciones t " +
                    "INNER JOIN usuarios u ON t.usuario_id = u.id " +
                    "WHERE t.tipo_transaccion_id = 2 " +
                    "AND t.estado = 'COMPLETADA' " +
                    "ORDER BY t.fecha DESC " +
                    "LIMIT ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, limite);
            rs = pstm.executeQuery();

            while (rs.next()) {
                Map<String, Object> venta = new HashMap<>();
                venta.put("numero", rs.getString("numero_transaccion"));
                venta.put("fecha", rs.getTimestamp("fecha"));
                venta.put("total", rs.getBigDecimal("total"));
                venta.put("metodoPago", rs.getString("metodo_pago"));
                venta.put("usuario", rs.getString("usuario"));
                lista.add(venta);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener últimas ventas: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener productos con stock bajo (lista detallada)
     */
    public List<Map<String, Object>> obtenerProductosStockBajo(int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT c.nombre_comercial, c.concentracion, p.lote, " +
                    "p.stock_actual, p.stock_minimo " +
                    "FROM productos p " +
                    "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                    "WHERE p.stock_actual <= p.stock_minimo " +
                    "AND p.stock_actual > 0 " +
                    "AND p.activo = 1 " +
                    "ORDER BY p.stock_actual ASC " +
                    "LIMIT ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, limite);
            rs = pstm.executeQuery();

            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                String nombre = rs.getString("nombre_comercial");
                String concentracion = rs.getString("concentracion");
                if (concentracion != null && !concentracion.isEmpty()) {
                    nombre += " " + concentracion;
                }
                producto.put("nombre", nombre);
                producto.put("lote", rs.getString("lote"));
                producto.put("stockActual", rs.getInt("stock_actual"));
                producto.put("stockMinimo", rs.getInt("stock_minimo"));
                lista.add(producto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener stock bajo: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener productos por vencer (lista detallada)
     */
    public List<Map<String, Object>> obtenerProductosPorVencer(int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT c.nombre_comercial, c.concentracion, p.lote, " +
                    "p.fecha_vencimiento, p.stock_actual, " +
                    "DATEDIFF(p.fecha_vencimiento, CURDATE()) as dias_restantes " +
                    "FROM productos p " +
                    "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                    "WHERE p.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) " +
                    "AND p.activo = 1 " +
                    "ORDER BY p.fecha_vencimiento ASC " +
                    "LIMIT ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, limite);
            rs = pstm.executeQuery();

            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                String nombre = rs.getString("nombre_comercial");
                String concentracion = rs.getString("concentracion");
                if (concentracion != null && !concentracion.isEmpty()) {
                    nombre += " " + concentracion;
                }
                producto.put("nombre", nombre);
                producto.put("lote", rs.getString("lote"));
                producto.put("fechaVencimiento", rs.getDate("fecha_vencimiento"));
                producto.put("stockActual", rs.getInt("stock_actual"));
                producto.put("diasRestantes", rs.getInt("dias_restantes"));
                lista.add(producto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener por vencer: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener total de productos en inventario
     */
    public int contarTotalProductos() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM productos WHERE activo = 1";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar productos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener ventas del mes actual
     */
    public BigDecimal obtenerVentasDelMes() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COALESCE(SUM(total), 0) as total_ventas " +
                    "FROM transacciones " +
                    "WHERE tipo_transaccion_id = 2 " +
                    "AND MONTH(fecha) = MONTH(CURDATE()) " +
                    "AND YEAR(fecha) = YEAR(CURDATE()) " +
                    "AND estado = 'COMPLETADA'";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total_ventas");
            }
            return BigDecimal.ZERO;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener ventas del mes: " + ex.getMessage(), ex);
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

    // =====================================================
    //     MÉTODOS PARA FARMACÉUTICO (Mi Turno)
    // =====================================================

    /**
     * Obtener sesión de caja activa del usuario
     */
    public Map<String, Object> obtenerSesionCajaActiva(Long usuarioId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT s.*, c.nombre as caja_nombre " +
                    "FROM sesiones_caja s " +
                    "INNER JOIN cajas c ON s.caja_id = c.id " +
                    "WHERE s.usuario_id = ? " +
                    "AND s.estado = 'ABIERTA' " +
                    "ORDER BY s.fecha_apertura DESC LIMIT 1";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, usuarioId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                Map<String, Object> sesion = new HashMap<>();
                sesion.put("id", rs.getLong("id"));
                sesion.put("cajaId", rs.getLong("caja_id"));
                sesion.put("cajaNombre", rs.getString("caja_nombre"));
                sesion.put("fechaApertura", rs.getTimestamp("fecha_apertura"));
                sesion.put("montoInicial", rs.getBigDecimal("monto_inicial"));
                sesion.put("estado", rs.getString("estado"));
                return sesion;
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener sesión activa: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener ventas del turno actual del usuario
     */
    public Map<String, Object> obtenerVentasTurno(Long sesionCajaId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        Map<String, Object> resultado = new HashMap<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT " +
                    "COUNT(*) as cantidad_ventas, " +
                    "COALESCE(SUM(total), 0) as total_ventas, " +
                    "COALESCE(SUM(CASE WHEN metodo_pago = 'EFECTIVO' THEN total ELSE 0 END), 0) as total_efectivo, " +
                    "COALESCE(SUM(CASE WHEN metodo_pago = 'YAPE_PLIN' THEN total ELSE 0 END), 0) as total_yape_plin, " +
                    "COALESCE(SUM(CASE WHEN metodo_pago = 'TARJETA' THEN total ELSE 0 END), 0) as total_tarjeta, " +
                    "COALESCE(SUM(CASE WHEN metodo_pago = 'MIXTO' THEN total ELSE 0 END), 0) as total_mixto, " +
                    "COALESCE(SUM(monto_efectivo), 0) as efectivo_recibido, " +
                    "COALESCE(SUM(vuelto), 0) as total_vueltos " +
                    "FROM transacciones " +
                    "WHERE sesion_caja_id = ? " +
                    "AND tipo_transaccion_id = 2 " +
                    "AND estado = 'COMPLETADA'";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, sesionCajaId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                resultado.put("cantidadVentas", rs.getInt("cantidad_ventas"));
                resultado.put("totalVentas", rs.getBigDecimal("total_ventas"));
                resultado.put("totalEfectivo", rs.getBigDecimal("total_efectivo"));
                resultado.put("totalYapePlin", rs.getBigDecimal("total_yape_plin"));
                resultado.put("totalTarjeta", rs.getBigDecimal("total_tarjeta"));
                resultado.put("totalMixto", rs.getBigDecimal("total_mixto"));
                resultado.put("efectivoRecibido", rs.getBigDecimal("efectivo_recibido"));
                resultado.put("totalVueltos", rs.getBigDecimal("total_vueltos"));
            }
            return resultado;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener ventas turno: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener ventas del día del usuario específico
     */
    public Map<String, Object> obtenerVentasDelDiaUsuario(Long usuarioId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        Map<String, Object> resultado = new HashMap<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT " +
                    "COUNT(*) as cantidad_ventas, " +
                    "COALESCE(SUM(total), 0) as total_ventas " +
                    "FROM transacciones " +
                    "WHERE usuario_id = ? " +
                    "AND tipo_transaccion_id = 2 " +
                    "AND DATE(fecha) = CURDATE() " +
                    "AND estado = 'COMPLETADA'";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, usuarioId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                resultado.put("cantidadVentas", rs.getInt("cantidad_ventas"));
                resultado.put("totalVentas", rs.getBigDecimal("total_ventas"));
            }
            return resultado;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener ventas del día usuario: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener últimas ventas del usuario
     */
    public List<Map<String, Object>> obtenerUltimasVentasUsuario(Long usuarioId, int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT t.numero_transaccion, t.fecha, t.total, t.metodo_pago " +
                    "FROM transacciones t " +
                    "WHERE t.usuario_id = ? " +
                    "AND t.tipo_transaccion_id = 2 " +
                    "AND t.estado = 'COMPLETADA' " +
                    "ORDER BY t.fecha DESC " +
                    "LIMIT ?";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, usuarioId);
            pstm.setInt(2, limite);
            rs = pstm.executeQuery();

            while (rs.next()) {
                Map<String, Object> venta = new HashMap<>();
                venta.put("numero", rs.getString("numero_transaccion"));
                venta.put("fecha", rs.getTimestamp("fecha"));
                venta.put("total", rs.getBigDecimal("total"));
                venta.put("metodoPago", rs.getString("metodo_pago"));
                lista.add(venta);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener últimas ventas usuario: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener cajas disponibles para apertura
     */
    public List<Map<String, Object>> obtenerCajasDisponibles() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT c.id, c.nombre, c.descripcion " +
                    "FROM cajas c " +
                    "WHERE c.activo = 1 " +
                    "AND c.id NOT IN (" +
                    "   SELECT caja_id FROM sesiones_caja WHERE estado = 'ABIERTA'" +
                    ") " +
                    "ORDER BY c.nombre";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                Map<String, Object> caja = new HashMap<>();
                caja.put("id", rs.getLong("id"));
                caja.put("nombre", rs.getString("nombre"));
                caja.put("ubicacion", rs.getString("descripcion"));
                lista.add(caja);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener cajas disponibles: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }
}
