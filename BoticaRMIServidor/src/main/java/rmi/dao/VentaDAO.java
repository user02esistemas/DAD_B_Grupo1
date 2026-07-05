package rmi.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import rmi.config.DatabaseConfig;
import rmi.dto.DetalleTransaccionDTO;
import rmi.dto.TransaccionDTO;

public class VentaDAO {

    public Long registrarVenta(TransaccionDTO venta, List<DetalleTransaccionDTO> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener detalle");
        }

        try (Connection con = DatabaseConfig.getConnection()) {
            con.setAutoCommit(false);
            try {
                validarStock(con, detalles);
                prepararVenta(con, venta, detalles);
                Long ventaId = insertarCabecera(con, venta);
                insertarDetalles(con, ventaId, detalles);
                con.commit();
                return ventaId;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al registrar venta", ex);
        }
    }

    public TransaccionDTO buscarPorId(Long id) {
        String sql = "SELECT t.*, tt.nombre AS tipo_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM transacciones t "
                + "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id "
                + "LEFT JOIN usuarios u ON t.usuario_id = u.id "
                + "WHERE t.id = ? AND t.tipo_transaccion_id = 2";

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                TransaccionDTO venta = mapearVenta(rs);
                venta.setDetalles(obtenerDetalles(con, venta.getId()));
                return venta;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar venta", ex);
        }
    }

    public List<TransaccionDTO> listarUltimas(int limite) {
        String sql = "SELECT t.*, tt.nombre AS tipo_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM transacciones t "
                + "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id "
                + "LEFT JOIN usuarios u ON t.usuario_id = u.id "
                + "WHERE t.tipo_transaccion_id = 2 "
                + "ORDER BY t.fecha DESC LIMIT ?";

        List<TransaccionDTO> ventas = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite <= 0 ? 10 : limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapearVenta(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al listar ventas", ex);
        }
        return ventas;
    }

    public String generarNumeroVenta() {
        try (Connection con = DatabaseConfig.getConnection()) {
            return generarNumeroVenta(con);
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al generar numero de venta", ex);
        }
    }

    private void prepararVenta(Connection con, TransaccionDTO venta, List<DetalleTransaccionDTO> detalles) throws SQLException {
        venta.setTipoTransaccionId(TransaccionDTO.TIPO_VENTA);
        if (venta.getNumeroTransaccion() == null || venta.getNumeroTransaccion().isEmpty()) {
            venta.setNumeroTransaccion(generarNumeroVenta(con));
        }
        if (venta.getNombrePersona() == null || venta.getNombrePersona().trim().isEmpty()) {
            venta.setNombrePersona("CLIENTES VARIOS");
        }
        if (venta.getMetodoPago() == null || venta.getMetodoPago().trim().isEmpty()) {
            venta.setMetodoPago("EFECTIVO");
        }
        if (venta.getTipoComprobante() == null || venta.getTipoComprobante().trim().isEmpty()) {
            venta.setTipoComprobante("NOTA_VENTA");
        }
        if (venta.getEstado() == null || venta.getEstado().trim().isEmpty()) {
            venta.setEstado("COMPLETADA");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleTransaccionDTO detalle : detalles) {
            detalle.calcularSubtotal();
            subtotal = subtotal.add(detalle.getSubtotal());
        }
        venta.setSubtotal(subtotal);
        venta.setIgv(subtotal.multiply(new BigDecimal("0.18")).setScale(2, BigDecimal.ROUND_HALF_UP));
        venta.setTotal(venta.getSubtotal().add(venta.getIgv()));
        if (venta.getMontoEfectivo() == null) {
            venta.setMontoEfectivo(venta.getTotal());
        }
        if (venta.getMontoVirtual() == null) {
            venta.setMontoVirtual(BigDecimal.ZERO);
        }
        if (venta.getVuelto() == null) {
            venta.setVuelto(BigDecimal.ZERO);
        }
    }

    private Long insertarCabecera(Connection con, TransaccionDTO venta) throws SQLException {
        String sql = "INSERT INTO transacciones (tipo_transaccion_id, numero_transaccion, usuario_id, sesion_caja_id, "
                + "nombre_persona, subtotal, igv, total, metodo_pago, monto_efectivo, monto_virtual, "
                + "medio_pago_virtual, vuelto, tipo_comprobante, estado, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, TransaccionDTO.TIPO_VENTA);
            ps.setString(2, venta.getNumeroTransaccion());
            ps.setLong(3, venta.getUsuarioId());
            if (venta.getSesionCajaId() == null) {
                ps.setNull(4, Types.BIGINT);
            } else {
                ps.setLong(4, venta.getSesionCajaId());
            }
            ps.setString(5, venta.getNombrePersona());
            ps.setBigDecimal(6, venta.getSubtotal());
            ps.setBigDecimal(7, venta.getIgv());
            ps.setBigDecimal(8, venta.getTotal());
            ps.setString(9, venta.getMetodoPago());
            ps.setBigDecimal(10, venta.getMontoEfectivo());
            ps.setBigDecimal(11, venta.getMontoVirtual());
            ps.setString(12, venta.getMedioPagoVirtual());
            ps.setBigDecimal(13, venta.getVuelto());
            ps.setString(14, venta.getTipoComprobante());
            ps.setString(15, venta.getEstado());
            ps.setString(16, venta.getObservaciones());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el ID de la venta");
    }

    private void insertarDetalles(Connection con, Long ventaId, List<DetalleTransaccionDTO> detalles) throws SQLException {
        String sql = "INSERT INTO detalle_transacciones (transaccion_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (DetalleTransaccionDTO detalle : detalles) {
                ps.setLong(1, ventaId);
                ps.setLong(2, detalle.getProductoId());
                ps.setInt(3, detalle.getCantidad());
                ps.setBigDecimal(4, detalle.getPrecioUnitario());
                ps.setBigDecimal(5, detalle.getSubtotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void validarStock(Connection con, List<DetalleTransaccionDTO> detalles) throws SQLException {
        String sql = "SELECT stock_actual FROM productos WHERE id = ? AND activo = 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (DetalleTransaccionDTO detalle : detalles) {
                if (detalle.getProductoId() == null || detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                    throw new IllegalArgumentException("Detalle de venta invalido");
                }
                ps.setLong(1, detalle.getProductoId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Producto no encontrado: " + detalle.getProductoId());
                    }
                    int stock = rs.getInt("stock_actual");
                    if (stock < detalle.getCantidad()) {
                        throw new IllegalArgumentException("Stock insuficiente para producto " + detalle.getProductoId());
                    }
                }
            }
        }
    }

    private List<DetalleTransaccionDTO> obtenerDetalles(Connection con, Long ventaId) throws SQLException {
        String sql = "SELECT dt.*, c.nombre_comercial, c.concentracion, p.lote, p.fecha_vencimiento "
                + "FROM detalle_transacciones dt "
                + "INNER JOIN productos p ON dt.producto_id = p.id "
                + "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id "
                + "WHERE dt.transaccion_id = ?";
        List<DetalleTransaccionDTO> detalles = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleTransaccionDTO detalle = new DetalleTransaccionDTO();
                    detalle.setId(rs.getLong("id"));
                    detalle.setTransaccionId(rs.getLong("transaccion_id"));
                    detalle.setProductoId(rs.getLong("producto_id"));
                    detalle.setCantidad(rs.getInt("cantidad"));
                    detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    detalle.setSubtotal(rs.getBigDecimal("subtotal"));
                    detalle.setNombreComercial(rs.getString("nombre_comercial"));
                    detalle.setConcentracion(rs.getString("concentracion"));
                    detalle.setLote(rs.getString("lote"));
                    detalle.setFechaVencimiento(String.valueOf(rs.getDate("fecha_vencimiento")));
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    private String generarNumeroVenta(Connection con) throws SQLException {
        String fecha = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String sql = "SELECT COUNT(*) + 1 FROM transacciones WHERE tipo_transaccion_id = 2 AND DATE(fecha) = CURDATE()";
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            int siguiente = rs.next() ? rs.getInt(1) : 1;
            return String.format("V-%s-%04d", fecha, siguiente);
        }
    }

    private TransaccionDTO mapearVenta(ResultSet rs) throws SQLException {
        TransaccionDTO venta = new TransaccionDTO();
        venta.setId(rs.getLong("id"));
        venta.setTipoTransaccionId(rs.getLong("tipo_transaccion_id"));
        venta.setNumeroTransaccion(rs.getString("numero_transaccion"));
        venta.setUsuarioId(rs.getLong("usuario_id"));
        venta.setNombrePersona(rs.getString("nombre_persona"));
        venta.setFecha(rs.getTimestamp("fecha"));
        venta.setSubtotal(rs.getBigDecimal("subtotal"));
        venta.setIgv(rs.getBigDecimal("igv"));
        venta.setTotal(rs.getBigDecimal("total"));
        venta.setMetodoPago(rs.getString("metodo_pago"));
        venta.setMontoEfectivo(rs.getBigDecimal("monto_efectivo"));
        venta.setMontoVirtual(rs.getBigDecimal("monto_virtual"));
        venta.setMedioPagoVirtual(rs.getString("medio_pago_virtual"));
        venta.setVuelto(rs.getBigDecimal("vuelto"));
        venta.setTipoComprobante(rs.getString("tipo_comprobante"));
        venta.setEstado(rs.getString("estado"));
        venta.setObservaciones(rs.getString("observaciones"));
        venta.setTipoTransaccionNombre(rs.getString("tipo_nombre"));
        venta.setUsuarioNombre(rs.getString("usuario_nombre"));
        return venta;
    }
}
