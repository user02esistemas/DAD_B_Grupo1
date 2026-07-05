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
import rmi.dto.ProveedorDTO;
import rmi.dto.TransaccionDTO;

public class CompraDAO {

    public Long registrarCompra(TransaccionDTO compra, List<DetalleTransaccionDTO> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La compra debe tener detalle");
        }
        if (compra.getProveedorId() == null) {
            throw new IllegalArgumentException("Proveedor obligatorio");
        }

        try (Connection con = DatabaseConfig.getConnection()) {
            con.setAutoCommit(false);
            try {
                validarDetalles(con, detalles);
                prepararCompra(con, compra, detalles);
                Long compraId = insertarCabecera(con, compra);
                insertarDetalles(con, compraId, detalles);
                con.commit();
                return compraId;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al registrar compra", ex);
        }
    }

    public TransaccionDTO buscarPorId(Long id) {
        String sql = "SELECT t.*, tt.nombre AS tipo_nombre, p.razon_social AS proveedor_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM transacciones t "
                + "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id "
                + "LEFT JOIN proveedores p ON t.proveedor_id = p.id "
                + "LEFT JOIN usuarios u ON t.usuario_id = u.id "
                + "WHERE t.id = ? AND t.tipo_transaccion_id = 1";

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                TransaccionDTO compra = mapearCompra(rs);
                compra.setDetalles(obtenerDetalles(con, compra.getId()));
                return compra;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar compra", ex);
        }
    }

    public List<TransaccionDTO> listarCompras(int limite) {
        String sql = "SELECT t.*, tt.nombre AS tipo_nombre, p.razon_social AS proveedor_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM transacciones t "
                + "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id "
                + "LEFT JOIN proveedores p ON t.proveedor_id = p.id "
                + "LEFT JOIN usuarios u ON t.usuario_id = u.id "
                + "WHERE t.tipo_transaccion_id = 1 "
                + "ORDER BY t.fecha DESC LIMIT ?";

        List<TransaccionDTO> compras = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite <= 0 ? 10 : limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    compras.add(mapearCompra(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al listar compras", ex);
        }
        return compras;
    }

    public String generarNumeroCompra() {
        try (Connection con = DatabaseConfig.getConnection()) {
            return generarNumeroCompra(con);
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al generar numero de compra", ex);
        }
    }

    public List<ProveedorDTO> listarProveedoresActivos() {
        String sql = "SELECT * FROM proveedores WHERE activo = 1 ORDER BY razon_social";
        List<ProveedorDTO> proveedores = new ArrayList<>();

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                proveedores.add(mapearProveedor(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al listar proveedores", ex);
        }
        return proveedores;
    }

    public List<ProveedorDTO> listarProveedores() {
        String sql = "SELECT * FROM proveedores ORDER BY razon_social";
        List<ProveedorDTO> proveedores = new ArrayList<>();

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                proveedores.add(mapearProveedor(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al listar proveedores", ex);
        }
        return proveedores;
    }

    public ProveedorDTO buscarProveedorPorId(Long id) {
        String sql = "SELECT * FROM proveedores WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearProveedor(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar proveedor", ex);
        }
    }

    public boolean existeRuc(String ruc) {
        String sql = "SELECT COUNT(*) FROM proveedores WHERE ruc = ?";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ruc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al validar RUC", ex);
        }
    }

    public List<ProveedorDTO> buscarProveedoresAutocomplete(String termino) {
        String sql = "SELECT * FROM proveedores WHERE activo = 1 AND (razon_social LIKE ? OR ruc LIKE ?) ORDER BY razon_social LIMIT 10";
        List<ProveedorDTO> proveedores = new ArrayList<>();

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            String filtro = "%" + (termino == null ? "" : termino.trim()) + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    proveedores.add(mapearProveedor(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar proveedores", ex);
        }
        return proveedores;
    }

    private void prepararCompra(Connection con, TransaccionDTO compra, List<DetalleTransaccionDTO> detalles) throws SQLException {
        compra.setTipoTransaccionId(TransaccionDTO.TIPO_COMPRA);
        if (compra.getNumeroTransaccion() == null || compra.getNumeroTransaccion().isEmpty()) {
            compra.setNumeroTransaccion(generarNumeroCompra(con));
        }
        if (compra.getMetodoPago() == null || compra.getMetodoPago().trim().isEmpty()) {
            compra.setMetodoPago("EFECTIVO");
        }
        if (compra.getTipoComprobante() == null || compra.getTipoComprobante().trim().isEmpty()) {
            compra.setTipoComprobante("NOTA_VENTA");
        }
        if (compra.getEstado() == null || compra.getEstado().trim().isEmpty()) {
            compra.setEstado("COMPLETADA");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleTransaccionDTO detalle : detalles) {
            detalle.calcularSubtotal();
            subtotal = subtotal.add(detalle.getSubtotal());
        }
        compra.setSubtotal(subtotal);
        compra.setIgv(subtotal.multiply(new BigDecimal("0.18")).setScale(2, BigDecimal.ROUND_HALF_UP));
        compra.setTotal(compra.getSubtotal().add(compra.getIgv()));
        if (compra.getMontoEfectivo() == null) {
            compra.setMontoEfectivo(BigDecimal.ZERO);
        }
        if (compra.getMontoVirtual() == null) {
            compra.setMontoVirtual(BigDecimal.ZERO);
        }
        if (compra.getVuelto() == null) {
            compra.setVuelto(BigDecimal.ZERO);
        }
    }

    private Long insertarCabecera(Connection con, TransaccionDTO compra) throws SQLException {
        String sql = "INSERT INTO transacciones (tipo_transaccion_id, numero_transaccion, usuario_id, sesion_caja_id, proveedor_id, "
                + "nombre_persona, subtotal, igv, total, metodo_pago, monto_efectivo, monto_virtual, medio_pago_virtual, "
                + "vuelto, tipo_comprobante, estado, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, TransaccionDTO.TIPO_COMPRA);
            ps.setString(2, compra.getNumeroTransaccion());
            ps.setLong(3, compra.getUsuarioId());
            if (compra.getSesionCajaId() == null) ps.setNull(4, Types.BIGINT); else ps.setLong(4, compra.getSesionCajaId());
            ps.setLong(5, compra.getProveedorId());
            ps.setString(6, compra.getNombrePersona());
            ps.setBigDecimal(7, compra.getSubtotal());
            ps.setBigDecimal(8, compra.getIgv());
            ps.setBigDecimal(9, compra.getTotal());
            ps.setString(10, compra.getMetodoPago());
            ps.setBigDecimal(11, compra.getMontoEfectivo());
            ps.setBigDecimal(12, compra.getMontoVirtual());
            ps.setString(13, compra.getMedioPagoVirtual());
            ps.setBigDecimal(14, compra.getVuelto());
            ps.setString(15, compra.getTipoComprobante());
            ps.setString(16, compra.getEstado());
            ps.setString(17, compra.getObservaciones());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("No se pudo obtener el ID de la compra");
    }

    private void insertarDetalles(Connection con, Long compraId, List<DetalleTransaccionDTO> detalles) throws SQLException {
        String sql = "INSERT INTO detalle_transacciones (transaccion_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (DetalleTransaccionDTO detalle : detalles) {
                ps.setLong(1, compraId);
                ps.setLong(2, detalle.getProductoId());
                ps.setInt(3, detalle.getCantidad());
                ps.setBigDecimal(4, detalle.getPrecioUnitario());
                ps.setBigDecimal(5, detalle.getSubtotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void validarDetalles(Connection con, List<DetalleTransaccionDTO> detalles) throws SQLException {
        String sql = "SELECT COUNT(*) FROM productos WHERE id = ? AND activo = 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (DetalleTransaccionDTO detalle : detalles) {
                if (detalle.getProductoId() == null || detalle.getCantidad() == null || detalle.getCantidad() <= 0 || detalle.getPrecioUnitario() == null) {
                    throw new IllegalArgumentException("Detalle de compra invalido");
                }
                ps.setLong(1, detalle.getProductoId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        throw new IllegalArgumentException("Producto no encontrado: " + detalle.getProductoId());
                    }
                }
            }
        }
    }

    private List<DetalleTransaccionDTO> obtenerDetalles(Connection con, Long compraId) throws SQLException {
        String sql = "SELECT dt.*, c.nombre_comercial, c.concentracion, p.lote, p.fecha_vencimiento "
                + "FROM detalle_transacciones dt "
                + "INNER JOIN productos p ON dt.producto_id = p.id "
                + "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id "
                + "WHERE dt.transaccion_id = ?";
        List<DetalleTransaccionDTO> detalles = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, compraId);
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

    private String generarNumeroCompra(Connection con) throws SQLException {
        String fecha = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String sql = "SELECT COUNT(*) + 1 FROM transacciones WHERE tipo_transaccion_id = 1 AND DATE(fecha) = CURDATE()";
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            int siguiente = rs.next() ? rs.getInt(1) : 1;
            return String.format("C-%s-%04d", fecha, siguiente);
        }
    }

    private TransaccionDTO mapearCompra(ResultSet rs) throws SQLException {
        TransaccionDTO compra = new TransaccionDTO();
        compra.setId(rs.getLong("id"));
        compra.setTipoTransaccionId(rs.getLong("tipo_transaccion_id"));
        compra.setNumeroTransaccion(rs.getString("numero_transaccion"));
        compra.setUsuarioId(rs.getLong("usuario_id"));
        Long proveedorId = rs.getLong("proveedor_id");
        compra.setProveedorId(rs.wasNull() ? null : proveedorId);
        compra.setNombrePersona(rs.getString("nombre_persona"));
        compra.setFecha(rs.getTimestamp("fecha"));
        compra.setSubtotal(rs.getBigDecimal("subtotal"));
        compra.setIgv(rs.getBigDecimal("igv"));
        compra.setTotal(rs.getBigDecimal("total"));
        compra.setMetodoPago(rs.getString("metodo_pago"));
        compra.setEstado(rs.getString("estado"));
        compra.setObservaciones(rs.getString("observaciones"));
        compra.setTipoTransaccionNombre(rs.getString("tipo_nombre"));
        compra.setUsuarioNombre(rs.getString("usuario_nombre"));
        ProveedorDTO proveedor = new ProveedorDTO();
        proveedor.setId(proveedorId);
        proveedor.setRazonSocial(rs.getString("proveedor_nombre"));
        compra.setProveedor(proveedor);
        return compra;
    }

    private ProveedorDTO mapearProveedor(ResultSet rs) throws SQLException {
        ProveedorDTO proveedor = new ProveedorDTO();
        proveedor.setId(rs.getLong("id"));
        proveedor.setRuc(rs.getString("ruc"));
        proveedor.setRazonSocial(rs.getString("razon_social"));
        proveedor.setContacto(rs.getString("contacto"));
        proveedor.setTelefono(rs.getString("telefono"));
        proveedor.setEmail(rs.getString("email"));
        proveedor.setDireccion(rs.getString("direccion"));
        proveedor.setActivo(rs.getBoolean("activo"));
        proveedor.setCreatedAt(rs.getTimestamp("created_at"));
        return proveedor;
    }
}
