package DAO;

import DTO.MovimientoInventarioDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para Movimientos de Inventario
 */
public class MovimientoInventarioDAO {

    /**
     * Registrar movimiento de inventario (ajuste manual)
     */
    public Long registrar(MovimientoInventarioDTO movimiento) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "INSERT INTO movimientos_inventario (producto_id, tipo_movimiento, cantidad, " +
                        "stock_anterior, stock_nuevo, motivo, referencia_id, referencia_tipo, usuario_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstm = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstm.setLong(1, movimiento.getProductoId());
            pstm.setString(2, movimiento.getTipoMovimiento());
            pstm.setInt(3, movimiento.getCantidad());
            pstm.setInt(4, movimiento.getStockAnterior());
            pstm.setInt(5, movimiento.getStockNuevo());
            pstm.setString(6, movimiento.getMotivo());
            
            if (movimiento.getReferenciaId() != null) {
                pstm.setLong(7, movimiento.getReferenciaId());
            } else {
                pstm.setNull(7, Types.BIGINT);
            }
            
            pstm.setString(8, movimiento.getReferenciaTipo());
            pstm.setLong(9, movimiento.getUsuarioId());

            int filas = pstm.executeUpdate();
            if (filas > 0) {
                rs = pstm.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al registrar movimiento: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Realizar ajuste de inventario (actualiza stock y registra movimiento)
     */
    public boolean realizarAjuste(Long productoId, int nuevoStock, String motivo, Long usuarioId) {
        Connection con = null;
        PreparedStatement pstmSelect = null;
        PreparedStatement pstmUpdate = null;
        PreparedStatement pstmInsert = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            con.setAutoCommit(false);

            // Obtener stock actual
            String sqlSelect = "SELECT stock_actual FROM productos WHERE id = ?";
            pstmSelect = con.prepareStatement(sqlSelect);
            pstmSelect.setLong(1, productoId);
            rs = pstmSelect.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Producto no encontrado");
            }

            int stockAnterior = rs.getInt("stock_actual");
            int diferencia = nuevoStock - stockAnterior;
            String tipoMovimiento = diferencia >= 0 ? "ENTRADA" : "SALIDA";

            // Actualizar stock
            String sqlUpdate = "UPDATE productos SET stock_actual = ? WHERE id = ?";
            pstmUpdate = con.prepareStatement(sqlUpdate);
            pstmUpdate.setInt(1, nuevoStock);
            pstmUpdate.setLong(2, productoId);
            pstmUpdate.executeUpdate();

            // Registrar movimiento
            String sqlInsert = "INSERT INTO movimientos_inventario (producto_id, tipo_movimiento, cantidad, " +
                    "stock_anterior, stock_nuevo, motivo, referencia_tipo, usuario_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 'AJUSTE', ?)";
            pstmInsert = con.prepareStatement(sqlInsert);
            pstmInsert.setLong(1, productoId);
            pstmInsert.setString(2, tipoMovimiento);
            pstmInsert.setInt(3, Math.abs(diferencia));
            pstmInsert.setInt(4, stockAnterior);
            pstmInsert.setInt(5, nuevoStock);
            pstmInsert.setString(6, motivo);
            pstmInsert.setLong(7, usuarioId);
            pstmInsert.executeUpdate();

            con.commit();
            return true;

        } catch (SQLException ex) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            }
            throw new RuntimeException("Error al realizar ajuste: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmSelect != null) pstmSelect.close();
                if (pstmUpdate != null) pstmUpdate.close();
                if (pstmInsert != null) pstmInsert.close();
                if (con != null) con.setAutoCommit(true);
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    /**
     * Listar movimientos por producto
     */
    public List<MovimientoInventarioDTO> listarPorProducto(Long productoId, int pagina, int porPagina) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<MovimientoInventarioDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT m.*, u.nombre_completo as usuario_nombre " +
                        "FROM movimientos_inventario m " +
                        "INNER JOIN usuarios u ON m.usuario_id = u.id " +
                        "WHERE m.producto_id = ? " +
                        "ORDER BY m.fecha_movimiento DESC " +
                        "LIMIT ? OFFSET ?";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, productoId);
            pstm.setInt(2, porPagina);
            pstm.setInt(3, (pagina - 1) * porPagina);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar movimientos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar todos los movimientos (paginado)
     */
    public List<MovimientoInventarioDTO> listarTodos(int pagina, int porPagina) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<MovimientoInventarioDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT m.*, u.nombre_completo as usuario_nombre, " +
                        "c.nombre_comercial, c.concentracion, p.lote " +
                        "FROM movimientos_inventario m " +
                        "INNER JOIN usuarios u ON m.usuario_id = u.id " +
                        "INNER JOIN productos p ON m.producto_id = p.id " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "ORDER BY m.fecha_movimiento DESC " +
                        "LIMIT ? OFFSET ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, porPagina);
            pstm.setInt(2, (pagina - 1) * porPagina);
            rs = pstm.executeQuery();

            while (rs.next()) {
                MovimientoInventarioDTO dto = mapearResultSet(rs);
                String nombreProducto = rs.getString("nombre_comercial");
                String concentracion = rs.getString("concentracion");
                String lote = rs.getString("lote");
                dto.setProductoNombre(nombreProducto + 
                        (concentracion != null ? " " + concentracion : ""));
                dto.setLote(lote);
                lista.add(dto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar movimientos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Buscar movimientos por término (producto, usuario, motivo)
     */
    public List<MovimientoInventarioDTO> buscar(String termino, int pagina, int porPagina) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<MovimientoInventarioDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT m.*, u.nombre_completo as usuario_nombre, " +
                        "c.nombre_comercial, c.concentracion, p.lote " +
                        "FROM movimientos_inventario m " +
                        "INNER JOIN usuarios u ON m.usuario_id = u.id " +
                        "INNER JOIN productos p ON m.producto_id = p.id " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "WHERE c.nombre_comercial LIKE ? " +
                        "OR c.concentracion LIKE ? " +
                        "OR p.lote LIKE ? " +
                        "OR u.nombre_completo LIKE ? " +
                        "OR m.motivo LIKE ? " +
                        "OR m.tipo_movimiento LIKE ? " +
                        "ORDER BY m.fecha_movimiento DESC " +
                        "LIMIT ? OFFSET ?";

            String busqueda = "%" + termino + "%";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, busqueda);
            pstm.setString(2, busqueda);
            pstm.setString(3, busqueda);
            pstm.setString(4, busqueda);
            pstm.setString(5, busqueda);
            pstm.setString(6, busqueda);
            pstm.setInt(7, porPagina);
            pstm.setInt(8, (pagina - 1) * porPagina);
            rs = pstm.executeQuery();

            while (rs.next()) {
                MovimientoInventarioDTO dto = mapearResultSet(rs);
                String nombreProducto = rs.getString("nombre_comercial");
                String concentracion = rs.getString("concentracion");
                String lote = rs.getString("lote");
                dto.setProductoNombre(nombreProducto + 
                        (concentracion != null ? " " + concentracion : ""));
                dto.setLote(lote);
                lista.add(dto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar movimientos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar total de movimientos
     */
    public int contarTodos() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM movimientos_inventario";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar movimientos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar movimientos por búsqueda
     */
    public int contarBusqueda(String termino) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total " +
                        "FROM movimientos_inventario m " +
                        "INNER JOIN usuarios u ON m.usuario_id = u.id " +
                        "INNER JOIN productos p ON m.producto_id = p.id " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "WHERE c.nombre_comercial LIKE ? " +
                        "OR c.concentracion LIKE ? " +
                        "OR p.lote LIKE ? " +
                        "OR u.nombre_completo LIKE ? " +
                        "OR m.motivo LIKE ? " +
                        "OR m.tipo_movimiento LIKE ?";

            String busqueda = "%" + termino + "%";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, busqueda);
            pstm.setString(2, busqueda);
            pstm.setString(3, busqueda);
            pstm.setString(4, busqueda);
            pstm.setString(5, busqueda);
            pstm.setString(6, busqueda);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar búsqueda: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar movimientos recientes
     */
    public List<MovimientoInventarioDTO> listarRecientes(int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<MovimientoInventarioDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT m.*, u.nombre_completo as usuario_nombre, " +
                        "c.nombre_comercial, c.concentracion, p.lote " +
                        "FROM movimientos_inventario m " +
                        "INNER JOIN usuarios u ON m.usuario_id = u.id " +
                        "INNER JOIN productos p ON m.producto_id = p.id " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "ORDER BY m.fecha_movimiento DESC " +
                        "LIMIT ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, limite);
            rs = pstm.executeQuery();

            while (rs.next()) {
                MovimientoInventarioDTO dto = mapearResultSet(rs);
                String nombreProducto = rs.getString("nombre_comercial");
                String concentracion = rs.getString("concentracion");
                String lote = rs.getString("lote");
                dto.setProductoNombre(nombreProducto + 
                        (concentracion != null ? " " + concentracion : ""));
                dto.setLote(lote);
                lista.add(dto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar movimientos recientes: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar movimientos por producto
     */
    public int contarPorProducto(Long productoId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM movimientos_inventario WHERE producto_id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, productoId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar movimientos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    // =====================================================
    //              MÉTODOS AUXILIARES
    // =====================================================

    private MovimientoInventarioDTO mapearResultSet(ResultSet rs) throws SQLException {
        MovimientoInventarioDTO dto = new MovimientoInventarioDTO();
        dto.setId(rs.getLong("id"));
        dto.setProductoId(rs.getLong("producto_id"));
        dto.setTipoMovimiento(rs.getString("tipo_movimiento"));
        dto.setCantidad(rs.getInt("cantidad"));
        dto.setStockAnterior(rs.getInt("stock_anterior"));
        dto.setStockNuevo(rs.getInt("stock_nuevo"));
        dto.setMotivo(rs.getString("motivo"));
        
        Long referenciaId = rs.getLong("referencia_id");
        dto.setReferenciaId(rs.wasNull() ? null : referenciaId);
        
        dto.setReferenciaTipo(rs.getString("referencia_tipo"));
        dto.setUsuarioId(rs.getLong("usuario_id"));
        dto.setFechaMovimiento(rs.getTimestamp("fecha_movimiento"));
        
        try {
            dto.setUsuarioNombre(rs.getString("usuario_nombre"));
        } catch (SQLException e) {
            // Columna no incluida
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
