package rmi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rmi.config.DatabaseConfig;
import rmi.dto.MovimientoInventarioDTO;

public class MovimientoInventarioDAO {

    public boolean realizarAjuste(Long productoId, int nuevoStock, String motivo, Long usuarioId) {
        try (Connection con = DatabaseConfig.getConnection()) {
            con.setAutoCommit(false);
            try {
                int stockAnterior = obtenerStockActual(con, productoId);
                int diferencia = nuevoStock - stockAnterior;
                String tipoMovimiento = diferencia >= 0 ? "ENTRADA" : "SALIDA";

                try (PreparedStatement update = con.prepareStatement("UPDATE productos SET stock_actual = ? WHERE id = ?")) {
                    update.setInt(1, nuevoStock);
                    update.setLong(2, productoId);
                    update.executeUpdate();
                }

                String sqlInsert = "INSERT INTO movimientos_inventario (producto_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo, motivo, referencia_tipo, usuario_id) VALUES (?, ?, ?, ?, ?, ?, 'AJUSTE', ?)";
                try (PreparedStatement insert = con.prepareStatement(sqlInsert)) {
                    insert.setLong(1, productoId);
                    insert.setString(2, tipoMovimiento);
                    insert.setInt(3, Math.abs(diferencia));
                    insert.setInt(4, stockAnterior);
                    insert.setInt(5, nuevoStock);
                    insert.setString(6, motivo);
                    insert.setLong(7, usuarioId);
                    insert.executeUpdate();
                }

                con.commit();
                return true;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al realizar ajuste", ex);
        }
    }

    public List<MovimientoInventarioDTO> listar(String termino, int pagina, int porPagina) {
        StringBuilder sql = new StringBuilder(baseSelect());
        List<Object> params = new ArrayList<>();
        if (termino != null && !termino.trim().isEmpty()) {
            sql.append("WHERE c.nombre_comercial LIKE ? OR c.concentracion LIKE ? OR p.lote LIKE ? OR u.nombre_completo LIKE ? OR m.motivo LIKE ? OR m.tipo_movimiento LIKE ? ");
            String busqueda = "%" + termino.trim() + "%";
            for (int i = 0; i < 6; i++) {
                params.add(busqueda);
            }
        }
        sql.append("ORDER BY m.fecha_movimiento DESC LIMIT ? OFFSET ?");
        params.add(porPagina);
        params.add((pagina - 1) * porPagina);

        List<MovimientoInventarioDTO> movimientos = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapear(rs));
                }
            }
            return movimientos;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al listar movimientos", ex);
        }
    }

    public int contar(String termino) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM movimientos_inventario m INNER JOIN usuarios u ON m.usuario_id = u.id INNER JOIN productos p ON m.producto_id = p.id INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id ");
        List<Object> params = new ArrayList<>();
        if (termino != null && !termino.trim().isEmpty()) {
            sql.append("WHERE c.nombre_comercial LIKE ? OR c.concentracion LIKE ? OR p.lote LIKE ? OR u.nombre_completo LIKE ? OR m.motivo LIKE ? OR m.tipo_movimiento LIKE ?");
            String busqueda = "%" + termino.trim() + "%";
            for (int i = 0; i < 6; i++) {
                params.add(busqueda);
            }
        }
        try (Connection con = DatabaseConfig.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al contar movimientos", ex);
        }
    }

    private int obtenerStockActual(Connection con, Long productoId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT stock_actual FROM productos WHERE id = ?")) {
            ps.setLong(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Producto no encontrado");
                }
                return rs.getInt("stock_actual");
            }
        }
    }

    private String baseSelect() {
        return "SELECT m.*, u.nombre_completo as usuario_nombre, c.nombre_comercial, c.concentracion, p.lote "
                + "FROM movimientos_inventario m "
                + "INNER JOIN usuarios u ON m.usuario_id = u.id "
                + "INNER JOIN productos p ON m.producto_id = p.id "
                + "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id ";
    }

    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private MovimientoInventarioDTO mapear(ResultSet rs) throws SQLException {
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
        dto.setUsuarioNombre(rs.getString("usuario_nombre"));
        String concentracion = rs.getString("concentracion");
        dto.setProductoNombre(rs.getString("nombre_comercial") + (concentracion != null ? " " + concentracion : ""));
        dto.setLote(rs.getString("lote"));
        return dto;
    }
}
