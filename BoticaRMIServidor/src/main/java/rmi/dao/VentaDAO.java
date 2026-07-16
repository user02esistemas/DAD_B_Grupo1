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
import java.util.HashMap;
import java.util.Map;
import rmi.config.DatabaseConfig;
import rmi.dto.DetalleTransaccionDTO;
import rmi.dto.SesionCajaDTO;
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

    public TransaccionDTO buscarPorNumero(String numero) {
        String sql = "SELECT t.*, tt.nombre AS tipo_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM transacciones t "
                + "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id "
                + "LEFT JOIN usuarios u ON t.usuario_id = u.id "
                + "WHERE t.numero_transaccion = ? AND t.tipo_transaccion_id = 2";

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                TransaccionDTO venta = mapearVenta(rs);
                venta.setDetalles(obtenerDetalles(con, venta.getId()));
                return venta;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar venta por numero", ex);
        }
    }

    public boolean anular(Long id) {
        String sql = "UPDATE transacciones SET estado = 'ANULADA' WHERE id = ? AND tipo_transaccion_id = 2";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al anular venta", ex);
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

    public List<TransaccionDTO> buscar(String termino, int pagina, int porPagina) {
        StringBuilder sql = new StringBuilder("SELECT t.*, tt.nombre AS tipo_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM transacciones t "
                + "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id "
                + "LEFT JOIN usuarios u ON t.usuario_id = u.id "
                + "WHERE t.tipo_transaccion_id = 2 ");
        boolean filtrar = termino != null && !termino.trim().isEmpty();
        if (filtrar) {
            sql.append("AND (t.numero_transaccion LIKE ? OR t.nombre_persona LIKE ?) ");
        }
        sql.append("ORDER BY t.fecha DESC LIMIT ? OFFSET ?");

        List<TransaccionDTO> ventas = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int index = 1;
            if (filtrar) {
                String filtro = "%" + termino.trim() + "%";
                ps.setString(index++, filtro);
                ps.setString(index++, filtro);
            }
            int paginaSegura = pagina <= 0 ? 1 : pagina;
            int porPaginaSeguro = porPagina <= 0 ? 15 : porPagina;
            ps.setInt(index++, porPaginaSeguro);
            ps.setInt(index, (paginaSegura - 1) * porPaginaSeguro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapearVenta(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar ventas", ex);
        }
        return ventas;
    }

    public int contarVentas(String termino) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM transacciones t WHERE t.tipo_transaccion_id = 2 ");
        boolean filtrar = termino != null && !termino.trim().isEmpty();
        if (filtrar) {
            sql.append("AND (t.numero_transaccion LIKE ? OR t.nombre_persona LIKE ?) ");
        }
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (filtrar) {
                String filtro = "%" + termino.trim() + "%";
                ps.setString(1, filtro);
                ps.setString(2, filtro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al contar ventas", ex);
        }
    }

    public String generarNumeroVenta() {
        try (Connection con = DatabaseConfig.getConnection()) {
            return generarNumeroVenta(con);
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al generar numero de venta", ex);
        }
    }

    public Long abrirSesionCaja(SesionCajaDTO sesion) {
        if (sesion.getUsuarioId() == null) {
            throw new IllegalArgumentException("Usuario requerido para abrir caja");
        }
        if (tieneSesionAbierta(sesion.getUsuarioId())) {
            throw new IllegalArgumentException("Ya tienes una caja abierta");
        }
        String sql = "INSERT INTO sesiones_caja (caja_id, usuario_id, monto_inicial, estado) VALUES (?, ?, ?, 'ABIERTA')";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, sesion.getCajaId() == null ? 1L : sesion.getCajaId());
            ps.setLong(2, sesion.getUsuarioId());
            ps.setBigDecimal(3, sesion.getMontoInicial() == null ? BigDecimal.ZERO : sesion.getMontoInicial());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al abrir caja", ex);
        }
        throw new IllegalStateException("No se pudo abrir caja");
    }

    public boolean cerrarSesionCaja(SesionCajaDTO sesion) {
        if (sesion.getUsuarioId() == null) {
            throw new IllegalArgumentException("Usuario requerido para cerrar caja");
        }
        SesionCajaDTO abierta = buscarSesionAbierta(sesion.getUsuarioId());
        if (abierta == null) {
            throw new IllegalArgumentException("No tienes una caja abierta");
        }
        SesionCajaDTO resumen = obtenerResumenSesion(abierta.getId());
        BigDecimal montoFinal = sesion.getMontoFinal() == null ? BigDecimal.ZERO : sesion.getMontoFinal();
        String sql = "UPDATE sesiones_caja SET fecha_cierre = CURRENT_TIMESTAMP, monto_final = ?, "
                + "total_transacciones = ?, total_ventas_efectivo = ?, total_ventas_virtual = ?, "
                + "efectivo_esperado = ?, estado = 'CERRADA', observaciones = ? WHERE id = ? AND estado = 'ABIERTA'";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, montoFinal);
            ps.setBigDecimal(2, resumen.getTotalTransacciones());
            ps.setBigDecimal(3, resumen.getTotalVentasEfectivo());
            ps.setBigDecimal(4, resumen.getTotalVentasVirtual());
            ps.setBigDecimal(5, resumen.getEfectivoEsperado());
            ps.setString(6, sesion.getObservaciones());
            ps.setLong(7, abierta.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al cerrar caja", ex);
        }
    }

    public SesionCajaDTO buscarSesionAbierta(Long usuarioId) {
        String sql = "SELECT sc.*, c.nombre AS caja_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM sesiones_caja sc "
                + "INNER JOIN cajas c ON sc.caja_id = c.id "
                + "INNER JOIN usuarios u ON sc.usuario_id = u.id "
                + "WHERE sc.usuario_id = ? AND sc.estado = 'ABIERTA' ORDER BY sc.fecha_apertura DESC LIMIT 1";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearSesionCaja(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar caja abierta", ex);
        }
    }

    public boolean tieneSesionAbierta(Long usuarioId) {
        return buscarSesionAbierta(usuarioId) != null;
    }

    public List<SesionCajaDTO> listarUltimasSesionesCaja(int limite) {
        String sql = "SELECT sc.*, c.nombre AS caja_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM sesiones_caja sc "
                + "INNER JOIN cajas c ON sc.caja_id = c.id "
                + "INNER JOIN usuarios u ON sc.usuario_id = u.id "
                + "ORDER BY sc.fecha_apertura DESC LIMIT ?";
        List<SesionCajaDTO> sesiones = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite <= 0 ? 2 : limite);
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

    public SesionCajaDTO obtenerResumenSesion(Long sesionId) {
        String sql = "SELECT COALESCE(SUM(total), 0) AS total_transacciones, "
                + "COALESCE(SUM(monto_efectivo), 0) AS efectivo, "
                + "COALESCE(SUM(vuelto), 0) AS vueltos, "
                + "COALESCE(SUM(monto_virtual), 0) AS virtual "
                + "FROM transacciones WHERE sesion_caja_id = ? AND tipo_transaccion_id = 2 AND estado = 'COMPLETADA'";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            SesionCajaDTO sesion = buscarSesionCajaPorId(con, sesionId);
            if (sesion == null) {
                throw new IllegalArgumentException("Sesion de caja no encontrada");
            }
            ps.setLong(1, sesionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sesion.setTotalTransacciones(rs.getBigDecimal("total_transacciones"));
                    sesion.setTotalVentasEfectivo(rs.getBigDecimal("efectivo"));
                    sesion.setTotalVueltos(rs.getBigDecimal("vueltos"));
                    sesion.setTotalVentasVirtual(rs.getBigDecimal("virtual"));
                    sesion.setEfectivoEsperado(sesion.getMontoInicial()
                            .add(sesion.getTotalVentasEfectivo())
                            .subtract(sesion.getTotalVueltos()));
                }
            }
            return sesion;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener resumen de caja", ex);
        }
    }

    public List<Map<String, Object>> listarCajasDisponibles() {
        String sql = "SELECT c.id, c.nombre, c.descripcion "
                + "FROM cajas c "
                + "WHERE c.activo = 1 "
                + "AND c.id NOT IN (SELECT caja_id FROM sesiones_caja WHERE estado = 'ABIERTA') "
                + "ORDER BY c.nombre";
        List<Map<String, Object>> cajas = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> caja = new HashMap<>();
                caja.put("id", rs.getLong("id"));
                caja.put("nombre", rs.getString("nombre"));
                caja.put("ubicacion", rs.getString("descripcion"));
                cajas.add(caja);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al listar cajas disponibles", ex);
        }
        return cajas;
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

    private SesionCajaDTO buscarSesionCajaPorId(Connection con, Long id) throws SQLException {
        String sql = "SELECT sc.*, c.nombre AS caja_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM sesiones_caja sc "
                + "INNER JOIN cajas c ON sc.caja_id = c.id "
                + "INNER JOIN usuarios u ON sc.usuario_id = u.id WHERE sc.id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearSesionCaja(rs) : null;
            }
        }
    }

    private SesionCajaDTO mapearSesionCaja(ResultSet rs) throws SQLException {
        SesionCajaDTO sesion = new SesionCajaDTO();
        sesion.setId(rs.getLong("id"));
        sesion.setCajaId(rs.getLong("caja_id"));
        sesion.setUsuarioId(rs.getLong("usuario_id"));
        sesion.setFechaApertura(rs.getTimestamp("fecha_apertura"));
        sesion.setFechaCierre(rs.getTimestamp("fecha_cierre"));
        sesion.setMontoInicial(rs.getBigDecimal("monto_inicial"));
        sesion.setMontoFinal(rs.getBigDecimal("monto_final"));
        sesion.setTotalTransacciones(rs.getBigDecimal("total_transacciones"));
        sesion.setTotalVentasEfectivo(rs.getBigDecimal("total_ventas_efectivo"));
        sesion.setTotalVentasVirtual(rs.getBigDecimal("total_ventas_virtual"));
        sesion.setEfectivoEsperado(rs.getBigDecimal("efectivo_esperado"));
        sesion.setEstado(rs.getString("estado"));
        sesion.setObservaciones(rs.getString("observaciones"));
        sesion.setCajaNombre(rs.getString("caja_nombre"));
        sesion.setUsuarioNombre(rs.getString("usuario_nombre"));
        return sesion;
    }
}
