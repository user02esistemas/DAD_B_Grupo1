package DAO;

import DTO.SesionCajaDTO;
import java.math.BigDecimal;
import java.sql.*;

/**
 * DAO para Sesiones de Caja
 */
public class SesionCajaDAO {

    /**
     * Abrir nueva sesión de caja
     */
    public Long abrirSesion(SesionCajaDTO sesion) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "INSERT INTO sesiones_caja (caja_id, usuario_id, monto_inicial, estado) " +
                        "VALUES (?, ?, ?, 'ABIERTA')";

            pstm = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstm.setLong(1, sesion.getCajaId() != null ? sesion.getCajaId() : 1L);
            pstm.setLong(2, sesion.getUsuarioId());
            pstm.setBigDecimal(3, sesion.getMontoInicial());

            int filas = pstm.executeUpdate();
            if (filas > 0) {
                rs = pstm.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al abrir sesión de caja: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Cerrar sesión de caja
     */
    public boolean cerrarSesion(SesionCajaDTO sesion) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE sesiones_caja SET " +
                        "fecha_cierre = NOW(), " +
                        "monto_final = ?, " +
                        "total_transacciones = ?, " +
                        "total_ventas_efectivo = ?, " +
                        "total_ventas_virtual = ?, " +
                        "efectivo_esperado = ?, " +
                        "estado = 'CERRADA', " +
                        "observaciones = ? " +
                        "WHERE id = ?";

            pstm = con.prepareStatement(sql);
            pstm.setBigDecimal(1, sesion.getMontoFinal());
            pstm.setBigDecimal(2, sesion.getTotalTransacciones());
            pstm.setBigDecimal(3, sesion.getTotalVentasEfectivo());
            pstm.setBigDecimal(4, sesion.getTotalVentasVirtual());
            pstm.setBigDecimal(5, sesion.getEfectivoEsperado());
            pstm.setString(6, sesion.getObservaciones());
            pstm.setLong(7, sesion.getId());

            return pstm.executeUpdate() > 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al cerrar sesión: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(null, pstm);
        }
    }

    /**
     * Buscar sesión abierta del usuario
     */
    public SesionCajaDTO buscarSesionAbierta(Long usuarioId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT s.*, c.nombre as caja_nombre, u.nombre_completo as usuario_nombre " +
                        "FROM sesiones_caja s " +
                        "INNER JOIN cajas c ON s.caja_id = c.id " +
                        "INNER JOIN usuarios u ON s.usuario_id = u.id " +
                        "WHERE s.usuario_id = ? AND s.estado = 'ABIERTA' " +
                        "ORDER BY s.fecha_apertura DESC LIMIT 1";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, usuarioId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return mapearResultSet(rs);
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar sesión abierta: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Buscar sesión por ID
     */
    public SesionCajaDTO buscarPorId(Long id) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT s.*, c.nombre as caja_nombre, u.nombre_completo as usuario_nombre " +
                        "FROM sesiones_caja s " +
                        "INNER JOIN cajas c ON s.caja_id = c.id " +
                        "INNER JOIN usuarios u ON s.usuario_id = u.id " +
                        "WHERE s.id = ?";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, id);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return mapearResultSet(rs);
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar sesión: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Verificar si usuario tiene sesión abierta
     */
    public boolean tieneSesionAbierta(Long usuarioId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM sesiones_caja " +
                        "WHERE usuario_id = ? AND estado = 'ABIERTA'";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, usuarioId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
            return false;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al verificar sesión: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Actualizar totales de la sesión
     */
    public boolean actualizarTotales(SesionCajaDTO sesion) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE sesiones_caja SET " +
                        "total_transacciones = ?, " +
                        "total_ventas_efectivo = ?, " +
                        "total_ventas_virtual = ? " +
                        "WHERE id = ?";

            pstm = con.prepareStatement(sql);
            pstm.setBigDecimal(1, sesion.getTotalTransacciones());
            pstm.setBigDecimal(2, sesion.getTotalVentasEfectivo());
            pstm.setBigDecimal(3, sesion.getTotalVentasVirtual());
            pstm.setLong(4, sesion.getId());

            return pstm.executeUpdate() > 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar totales: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(null, pstm);
        }
    }

    /**
     * Obtener resumen de ventas de la sesión
     * 
     * Lógica del efectivo esperado:
     * Efectivo Esperado = Monto Inicial + (Monto Efectivo Recibido - Vueltos Entregados)
     * 
     * Es decir: lo que entró a caja menos lo que salió como vuelto
     */
    public SesionCajaDTO obtenerResumenSesion(Long sesionId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            
            // Primero obtener la sesión
            SesionCajaDTO sesion = buscarPorId(sesionId);
            if (sesion == null) return null;

            // Calcular totales de ventas de esta sesión
            // monto_efectivo = lo que pagó el cliente en efectivo
            // vuelto = lo que se devolvió al cliente
            // Efectivo NETO que quedó en caja = monto_efectivo - vuelto
            String sql = "SELECT " +
                        "COALESCE(SUM(total), 0) as total_ventas, " +
                        "COALESCE(SUM(monto_efectivo), 0) as total_efectivo_recibido, " +
                        "COALESCE(SUM(vuelto), 0) as total_vueltos, " +
                        "COALESCE(SUM(monto_virtual), 0) as total_virtual, " +
                        "COUNT(*) as num_transacciones " +
                        "FROM transacciones " +
                        "WHERE sesion_caja_id = ? AND tipo_transaccion_id = 2 AND estado = 'COMPLETADA'";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, sesionId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                BigDecimal totalVentas = rs.getBigDecimal("total_ventas");
                BigDecimal totalEfectivoRecibido = rs.getBigDecimal("total_efectivo_recibido");
                BigDecimal totalVueltos = rs.getBigDecimal("total_vueltos");
                BigDecimal totalVirtual = rs.getBigDecimal("total_virtual");
                
                // Efectivo neto = Efectivo recibido - Vueltos entregados
                BigDecimal efectivoNeto = totalEfectivoRecibido.subtract(totalVueltos);
                
                // Efectivo esperado = Monto inicial + Efectivo neto
                BigDecimal efectivoEsperado = sesion.getMontoInicial().add(efectivoNeto);
                
                sesion.setTotalTransacciones(totalVentas);
                sesion.setTotalVentasEfectivo(totalEfectivoRecibido);  // Guardamos el bruto recibido
                sesion.setTotalVueltos(totalVueltos);  // Guardamos los vueltos por separado
                sesion.setTotalVentasVirtual(totalVirtual);
                sesion.setEfectivoEsperado(efectivoEsperado);
            }

            return sesion;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener resumen: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    // =====================================================
    //              MÉTODOS AUXILIARES
    // =====================================================

    private SesionCajaDTO mapearResultSet(ResultSet rs) throws SQLException {
        SesionCajaDTO dto = new SesionCajaDTO();
        dto.setId(rs.getLong("id"));
        dto.setCajaId(rs.getLong("caja_id"));
        dto.setUsuarioId(rs.getLong("usuario_id"));
        dto.setFechaApertura(rs.getTimestamp("fecha_apertura"));
        dto.setFechaCierre(rs.getTimestamp("fecha_cierre"));
        dto.setMontoInicial(rs.getBigDecimal("monto_inicial"));
        dto.setMontoFinal(rs.getBigDecimal("monto_final"));
        dto.setTotalTransacciones(rs.getBigDecimal("total_transacciones"));
        dto.setEstado(rs.getString("estado"));
        dto.setObservaciones(rs.getString("observaciones"));

        try {
            dto.setTotalVentasEfectivo(rs.getBigDecimal("total_ventas_efectivo"));
            dto.setTotalVentasVirtual(rs.getBigDecimal("total_ventas_virtual"));
            dto.setEfectivoEsperado(rs.getBigDecimal("efectivo_esperado"));
        } catch (SQLException e) {
            // Campos opcionales
        }

        try {
            dto.setCajaNombre(rs.getString("caja_nombre"));
            dto.setUsuarioNombre(rs.getString("usuario_nombre"));
        } catch (SQLException e) {
            // Campos de JOIN
        }

        return dto;
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
