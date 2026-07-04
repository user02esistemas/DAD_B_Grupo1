package DAO;

import DTO.DetalleTransaccionDTO;
import DTO.ProductoDTO;
import DTO.ProveedorDTO;
import DTO.TransaccionDTO;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para Transacciones (Compras y Ventas)
 * Incluye lógica para registrar transacción completa con detalles
 */
public class TransaccionDAO {

    /**
     * Registrar transacción completa (cabecera + detalles)
     * Esta operación es transaccional
     * NOTA: El TRIGGER trg_actualizar_stock_transaccion se encarga de:
     *       - Actualizar stock en productos
     *       - Registrar movimientos en movimientos_inventario
     */
    public Long registrarTransaccionCompleta(TransaccionDTO transaccion, List<DetalleTransaccionDTO> detalles) {
        Connection con = null;
        PreparedStatement pstmTransaccion = null;
        PreparedStatement pstmDetalle = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            con.setAutoCommit(false);

            // 1. Generar número de transacción
            if (transaccion.getNumeroTransaccion() == null || transaccion.getNumeroTransaccion().isEmpty()) {
                transaccion.setNumeroTransaccion(generarNumeroTransaccionInterno(con, transaccion.getTipoTransaccionId()));
            }

            // 2. Insertar cabecera
            String sqlTransaccion = "INSERT INTO transacciones (tipo_transaccion_id, numero_transaccion, " +
                    "usuario_id, sesion_caja_id, proveedor_id, nombre_persona, subtotal, igv, total, " +
                    "metodo_pago, estado, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmTransaccion = con.prepareStatement(sqlTransaccion, Statement.RETURN_GENERATED_KEYS);
            pstmTransaccion.setLong(1, transaccion.getTipoTransaccionId());
            pstmTransaccion.setString(2, transaccion.getNumeroTransaccion());
            pstmTransaccion.setLong(3, transaccion.getUsuarioId());
            
            if (transaccion.getSesionCajaId() != null) {
                pstmTransaccion.setLong(4, transaccion.getSesionCajaId());
            } else {
                pstmTransaccion.setNull(4, Types.BIGINT);
            }
            
            if (transaccion.getProveedorId() != null) {
                pstmTransaccion.setLong(5, transaccion.getProveedorId());
            } else {
                pstmTransaccion.setNull(5, Types.BIGINT);
            }
            
            pstmTransaccion.setString(6, transaccion.getNombrePersona());
            pstmTransaccion.setBigDecimal(7, transaccion.getSubtotal());
            pstmTransaccion.setBigDecimal(8, transaccion.getIgv());
            pstmTransaccion.setBigDecimal(9, transaccion.getTotal());
            pstmTransaccion.setString(10, transaccion.getMetodoPago());
            pstmTransaccion.setString(11, transaccion.getEstado());
            pstmTransaccion.setString(12, transaccion.getObservaciones());

            int filasTransaccion = pstmTransaccion.executeUpdate();
            if (filasTransaccion == 0) {
                throw new SQLException("No se pudo crear la transacción");
            }

            rs = pstmTransaccion.getGeneratedKeys();
            if (!rs.next()) {
                throw new SQLException("No se pudo obtener el ID de la transacción");
            }
            Long transaccionId = rs.getLong(1);

            // 3. Procesar detalles
            String sqlDetalle = "INSERT INTO detalle_transacciones (transaccion_id, producto_id, cantidad, " +
                    "precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
            pstmDetalle = con.prepareStatement(sqlDetalle);

            for (DetalleTransaccionDTO detalle : detalles) {
                Long productoId = detalle.getProductoId();

                // Para COMPRAS: asegurar que el producto exista (crear si es nuevo)
                if (TransaccionDTO.TIPO_COMPRA.equals(transaccion.getTipoTransaccionId())) {
                    productoId = asegurarProductoExiste(con, detalle);
                }

                if (productoId == null) {
                    throw new SQLException("No se pudo determinar el producto para el detalle");
                }

                pstmDetalle.setLong(1, transaccionId);
                pstmDetalle.setLong(2, productoId);
                pstmDetalle.setInt(3, detalle.getCantidad());
                pstmDetalle.setBigDecimal(4, detalle.getPrecioUnitario());
                pstmDetalle.setBigDecimal(5, detalle.getSubtotal());
                pstmDetalle.addBatch();
            }

            // El TRIGGER se dispara aquí y actualiza stock + registra movimiento
            pstmDetalle.executeBatch();
            con.commit();
            return transaccionId;

        } catch (SQLException ex) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            }
            throw new RuntimeException("Error al registrar transacción: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmTransaccion != null) pstmTransaccion.close();
                if (pstmDetalle != null) pstmDetalle.close();
                if (con != null) con.setAutoCommit(true);
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    /**
     * Asegurar que el producto exista para una compra.
     * Si existe, actualiza precios. Si no existe, lo crea con stock 0
     * (el TRIGGER se encargará de sumar el stock).
     */
    private Long asegurarProductoExiste(Connection con, DetalleTransaccionDTO detalle) throws SQLException {
        // Buscar si el producto ya existe
        String sqlBuscar = "SELECT id FROM productos WHERE catalogo_producto_id = ? AND lote = ?";
        try (PreparedStatement pstmBuscar = con.prepareStatement(sqlBuscar)) {
            pstmBuscar.setLong(1, detalle.getCatalogoProductoId());
            pstmBuscar.setString(2, detalle.getLote());
            
            try (ResultSet rs = pstmBuscar.executeQuery()) {
                if (rs.next()) {
                    // Producto existe - solo actualizar precios
                    Long productoId = rs.getLong("id");
                    
                    String sqlUpdate = "UPDATE productos SET precio_compra = ?, precio_venta = ? WHERE id = ?";
                    try (PreparedStatement pstmUpd = con.prepareStatement(sqlUpdate)) {
                        pstmUpd.setBigDecimal(1, detalle.getPrecioUnitario());
                        pstmUpd.setBigDecimal(2, detalle.getPrecioVenta());
                        pstmUpd.setLong(3, productoId);
                        pstmUpd.executeUpdate();
                    }
                    
                    return productoId;
                } else {
                    // Producto nuevo - insertar con stock 0 (el TRIGGER sumará la cantidad)
                    String sqlInsert = "INSERT INTO productos (catalogo_producto_id, lote, fecha_vencimiento, " +
                            "stock_actual, precio_compra, precio_venta, activo) VALUES (?, ?, ?, 0, ?, ?, 1)";
                    try (PreparedStatement pstmIns = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                        pstmIns.setLong(1, detalle.getCatalogoProductoId());
                        pstmIns.setString(2, detalle.getLote());
                        pstmIns.setDate(3, java.sql.Date.valueOf(detalle.getFechaVencimiento()));
                        pstmIns.setBigDecimal(4, detalle.getPrecioUnitario());
                        pstmIns.setBigDecimal(5, detalle.getPrecioVenta());
                        pstmIns.executeUpdate();
                        
                        try (ResultSet rsKeys = pstmIns.getGeneratedKeys()) {
                            if (rsKeys.next()) {
                                return rsKeys.getLong(1);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public TransaccionDTO buscarPorId(Long id) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT t.*, tt.nombre as tipo_nombre, " +
                    "p.razon_social as proveedor_nombre, u.nombre_completo as usuario_nombre " +
                    "FROM transacciones t " +
                    "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id " +
                    "LEFT JOIN proveedores p ON t.proveedor_id = p.id " +
                    "LEFT JOIN usuarios u ON t.usuario_id = u.id " +
                    "WHERE t.id = ?";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, id);
            rs = pstm.executeQuery();

            if (rs.next()) {
                TransaccionDTO dto = mapearResultSet(rs);
                dto.setDetalles(obtenerDetalles(id));
                return dto;
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar transacción: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    public TransaccionDTO buscarPorNumero(String numero) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT t.*, tt.nombre as tipo_nombre, u.nombre_completo as usuario_nombre " +
                    "FROM transacciones t " +
                    "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id " +
                    "LEFT JOIN usuarios u ON t.usuario_id = u.id " +
                    "WHERE t.numero_transaccion = ?";

            pstm = con.prepareStatement(sql);
            pstm.setString(1, numero);
            rs = pstm.executeQuery();

            if (rs.next()) {
                TransaccionDTO dto = mapearResultSet(rs);
                dto.setDetalles(obtenerDetalles(dto.getId()));
                return dto;
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar por número: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar TODAS las transacciones (compras y ventas) con paginación
     */
    public List<TransaccionDTO> listarTodas(int pagina, int porPagina) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<TransaccionDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT t.*, tt.nombre as tipo_nombre, " +
                    "p.razon_social as proveedor_nombre, u.nombre_completo as usuario_nombre " +
                    "FROM transacciones t " +
                    "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id " +
                    "LEFT JOIN proveedores p ON t.proveedor_id = p.id " +
                    "LEFT JOIN usuarios u ON t.usuario_id = u.id " +
                    "ORDER BY t.fecha DESC LIMIT ? OFFSET ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, porPagina);
            pstm.setInt(2, (pagina - 1) * porPagina);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar transacciones: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Buscar transacciones por término (número, proveedor, usuario)
     */
    public List<TransaccionDTO> buscar(String termino, int pagina, int porPagina) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<TransaccionDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT t.*, tt.nombre as tipo_nombre, " +
                    "p.razon_social as proveedor_nombre, u.nombre_completo as usuario_nombre " +
                    "FROM transacciones t " +
                    "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id " +
                    "LEFT JOIN proveedores p ON t.proveedor_id = p.id " +
                    "LEFT JOIN usuarios u ON t.usuario_id = u.id " +
                    "WHERE t.numero_transaccion LIKE ? " +
                    "OR p.razon_social LIKE ? " +
                    "OR u.nombre_completo LIKE ? " +
                    "OR tt.nombre LIKE ? " +
                    "ORDER BY t.fecha DESC LIMIT ? OFFSET ?";

            String busqueda = "%" + termino + "%";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, busqueda);
            pstm.setString(2, busqueda);
            pstm.setString(3, busqueda);
            pstm.setString(4, busqueda);
            pstm.setInt(5, porPagina);
            pstm.setInt(6, (pagina - 1) * porPagina);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar transacciones: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar total de transacciones
     */
    public int contarTodas() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM transacciones";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar transacciones: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar transacciones por búsqueda
     */
    public int contarBusqueda(String termino) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total " +
                    "FROM transacciones t " +
                    "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id " +
                    "LEFT JOIN proveedores p ON t.proveedor_id = p.id " +
                    "LEFT JOIN usuarios u ON t.usuario_id = u.id " +
                    "WHERE t.numero_transaccion LIKE ? " +
                    "OR p.razon_social LIKE ? " +
                    "OR u.nombre_completo LIKE ? " +
                    "OR tt.nombre LIKE ?";

            String busqueda = "%" + termino + "%";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, busqueda);
            pstm.setString(2, busqueda);
            pstm.setString(3, busqueda);
            pstm.setString(4, busqueda);
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

    public List<TransaccionDTO> listarPorTipo(Long tipoId, int pagina, int porPagina) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<TransaccionDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT t.*, tt.nombre as tipo_nombre, " +
                    "p.razon_social as proveedor_nombre, u.nombre_completo as usuario_nombre " +
                    "FROM transacciones t " +
                    "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id " +
                    "LEFT JOIN proveedores p ON t.proveedor_id = p.id " +
                    "LEFT JOIN usuarios u ON t.usuario_id = u.id " +
                    "WHERE t.tipo_transaccion_id = ? " +
                    "ORDER BY t.fecha DESC LIMIT ? OFFSET ?";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, tipoId);
            pstm.setInt(2, porPagina);
            pstm.setInt(3, (pagina - 1) * porPagina);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar transacciones: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    public List<DetalleTransaccionDTO> obtenerDetalles(Long transaccionId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<DetalleTransaccionDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT dt.*, p.lote, p.fecha_vencimiento, " +
                    "c.nombre_comercial, c.concentracion, c.principio_activo " +
                    "FROM detalle_transacciones dt " +
                    "INNER JOIN productos p ON dt.producto_id = p.id " +
                    "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                    "WHERE dt.transaccion_id = ?";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, transaccionId);
            rs = pstm.executeQuery();

            while (rs.next()) {
                DetalleTransaccionDTO dto = new DetalleTransaccionDTO();
                dto.setId(rs.getLong("id"));
                dto.setTransaccionId(rs.getLong("transaccion_id"));
                dto.setProductoId(rs.getLong("producto_id"));
                dto.setCantidad(rs.getInt("cantidad"));
                dto.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                dto.setSubtotal(rs.getBigDecimal("subtotal"));
                dto.setLote(rs.getString("lote"));
                dto.setFechaVencimiento(rs.getDate("fecha_vencimiento").toString());
                dto.setNombreComercial(rs.getString("nombre_comercial"));
                dto.setConcentracion(rs.getString("concentracion"));
                lista.add(dto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener detalles: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    public String generarNumeroTransaccion(Long tipoTransaccionId) {
        try {
            return generarNumeroTransaccionInterno(Conexion.getConnection(), tipoTransaccionId);
        } catch (SQLException ex) {
            throw new RuntimeException("Error al generar número: " + ex.getMessage(), ex);
        }
    }

    private String generarNumeroTransaccionInterno(Connection con, Long tipoTransaccionId) throws SQLException {
        String prefijo = TransaccionDTO.TIPO_COMPRA.equals(tipoTransaccionId) ? "C" : "V";
        String fecha = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        
        String sql = "SELECT COUNT(*) + 1 as siguiente FROM transacciones " +
                "WHERE tipo_transaccion_id = ? AND DATE(fecha) = CURDATE()";
        
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setLong(1, tipoTransaccionId);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    int siguiente = rs.getInt("siguiente");
                    return String.format("%s-%s-%04d", prefijo, fecha, siguiente);
                }
            }
        }
        return prefijo + "-" + fecha + "-0001";
    }

    /**
     * Insertar venta completa (para POS)
     * NOTA: El TRIGGER trg_actualizar_stock_transaccion se encarga de:
     *       - Actualizar stock en productos (restar cantidad)
     *       - Registrar movimientos en movimientos_inventario
     */
    public Long insertarVenta(TransaccionDTO transaccion) {
        Connection con = null;
        PreparedStatement pstmTransaccion = null;
        PreparedStatement pstmDetalle = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            con.setAutoCommit(false);

            // 1. Generar número de transacción
            if (transaccion.getNumeroTransaccion() == null || transaccion.getNumeroTransaccion().isEmpty()) {
                transaccion.setNumeroTransaccion(generarNumeroTransaccionInterno(con, 2L)); // VENTA
            }

            // 2. Insertar cabecera con campos de venta
            String sqlTransaccion = "INSERT INTO transacciones (tipo_transaccion_id, numero_transaccion, " +
                    "usuario_id, sesion_caja_id, nombre_persona, subtotal, igv, total, " +
                    "metodo_pago, monto_efectivo, monto_virtual, medio_pago_virtual, vuelto, " +
                    "tipo_comprobante, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmTransaccion = con.prepareStatement(sqlTransaccion, Statement.RETURN_GENERATED_KEYS);
            pstmTransaccion.setLong(1, 2L); // VENTA
            pstmTransaccion.setString(2, transaccion.getNumeroTransaccion());
            pstmTransaccion.setLong(3, transaccion.getUsuarioId());
            
            if (transaccion.getSesionCajaId() != null) {
                pstmTransaccion.setLong(4, transaccion.getSesionCajaId());
            } else {
                pstmTransaccion.setNull(4, Types.BIGINT);
            }
            
            pstmTransaccion.setString(5, transaccion.getNombrePersona());
            pstmTransaccion.setBigDecimal(6, transaccion.getSubtotal());
            pstmTransaccion.setBigDecimal(7, transaccion.getIgv());
            pstmTransaccion.setBigDecimal(8, transaccion.getTotal());
            pstmTransaccion.setString(9, transaccion.getMetodoPago());
            pstmTransaccion.setBigDecimal(10, transaccion.getMontoEfectivo());
            pstmTransaccion.setBigDecimal(11, transaccion.getMontoVirtual());
            pstmTransaccion.setString(12, transaccion.getMedioPagoVirtual());
            pstmTransaccion.setBigDecimal(13, transaccion.getVuelto());
            pstmTransaccion.setString(14, transaccion.getTipoComprobante());
            pstmTransaccion.setString(15, transaccion.getEstado());

            int filasTransaccion = pstmTransaccion.executeUpdate();
            if (filasTransaccion == 0) {
                throw new SQLException("No se pudo crear la venta");
            }

            rs = pstmTransaccion.getGeneratedKeys();
            if (!rs.next()) {
                throw new SQLException("No se pudo obtener el ID de la venta");
            }
            Long transaccionId = rs.getLong(1);

            // 3. Insertar detalles (el TRIGGER actualiza stock y registra movimiento)
            String sqlDetalle = "INSERT INTO detalle_transacciones (transaccion_id, producto_id, cantidad, " +
                    "precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
            pstmDetalle = con.prepareStatement(sqlDetalle);

            for (DetalleTransaccionDTO detalle : transaccion.getDetalles()) {
                pstmDetalle.setLong(1, transaccionId);
                pstmDetalle.setLong(2, detalle.getProductoId());
                pstmDetalle.setInt(3, detalle.getCantidad());
                pstmDetalle.setBigDecimal(4, detalle.getPrecioUnitario());
                pstmDetalle.setBigDecimal(5, detalle.getSubtotal());
                pstmDetalle.addBatch();
            }

            // El TRIGGER se dispara aquí y actualiza stock + registra movimiento
            pstmDetalle.executeBatch();
            con.commit();
            
            transaccion.setId(transaccionId);
            return transaccionId;

        } catch (SQLException ex) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            }
            throw new RuntimeException("Error al registrar venta: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmTransaccion != null) pstmTransaccion.close();
                if (pstmDetalle != null) pstmDetalle.close();
                if (con != null) con.setAutoCommit(true);
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    public int anular(Long id) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE transacciones SET estado = 'ANULADA' WHERE id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, id);
            return pstm.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Error al anular transacción: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(null, pstm);
        }
    }

    /**
     * Listar VENTAS con paginación y búsqueda
     */
    public List<TransaccionDTO> listarVentas(String busqueda, int offset, int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<TransaccionDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT t.*, tt.nombre as tipo_nombre, u.nombre_completo as usuario_nombre " +
                    "FROM transacciones t " +
                    "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id " +
                    "LEFT JOIN usuarios u ON t.usuario_id = u.id " +
                    "WHERE t.tipo_transaccion_id = 2 ";
            
            if (busqueda != null && !busqueda.trim().isEmpty()) {
                sql += "AND (t.numero_transaccion LIKE ? OR t.nombre_persona LIKE ?) ";
            }
            sql += "ORDER BY t.fecha DESC LIMIT ? OFFSET ?";

            pstm = con.prepareStatement(sql);
            int paramIndex = 1;
            if (busqueda != null && !busqueda.trim().isEmpty()) {
                String term = "%" + busqueda + "%";
                pstm.setString(paramIndex++, term);
                pstm.setString(paramIndex++, term);
            }
            pstm.setInt(paramIndex++, limite);
            pstm.setInt(paramIndex, offset);
            
            rs = pstm.executeQuery();

            while (rs.next()) {
                TransaccionDTO dto = mapearResultSetVenta(rs);
                lista.add(dto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar ventas: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar VENTAS con búsqueda
     */
    public int contarVentas(String busqueda) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM transacciones t WHERE t.tipo_transaccion_id = 2 ";
            
            if (busqueda != null && !busqueda.trim().isEmpty()) {
                sql += "AND (t.numero_transaccion LIKE ? OR t.nombre_persona LIKE ?)";
            }

            pstm = con.prepareStatement(sql);
            if (busqueda != null && !busqueda.trim().isEmpty()) {
                String term = "%" + busqueda + "%";
                pstm.setString(1, term);
                pstm.setString(2, term);
            }
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar ventas: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Mapear ResultSet para ventas incluyendo campos específicos
     */
    private TransaccionDTO mapearResultSetVenta(ResultSet rs) throws SQLException {
        TransaccionDTO dto = new TransaccionDTO();
        dto.setId(rs.getLong("id"));
        dto.setTipoTransaccionId(rs.getLong("tipo_transaccion_id"));
        dto.setNumeroTransaccion(rs.getString("numero_transaccion"));
        dto.setUsuarioId(rs.getLong("usuario_id"));
        
        Long sesionCajaId = rs.getLong("sesion_caja_id");
        dto.setSesionCajaId(rs.wasNull() ? null : sesionCajaId);
        
        dto.setNombrePersona(rs.getString("nombre_persona"));
        dto.setFecha(rs.getTimestamp("fecha"));
        dto.setSubtotal(rs.getBigDecimal("subtotal"));
        dto.setIgv(rs.getBigDecimal("igv"));
        dto.setTotal(rs.getBigDecimal("total"));
        dto.setMetodoPago(rs.getString("metodo_pago"));
        dto.setEstado(rs.getString("estado"));
        
        // Campos específicos de venta
        try {
            dto.setMontoEfectivo(rs.getBigDecimal("monto_efectivo"));
            dto.setMontoVirtual(rs.getBigDecimal("monto_virtual"));
            dto.setMedioPagoVirtual(rs.getString("medio_pago_virtual"));
            dto.setVuelto(rs.getBigDecimal("vuelto"));
            dto.setTipoComprobante(rs.getString("tipo_comprobante"));
        } catch (SQLException e) {
            // Campos pueden no existir
        }
        
        dto.setTipoTransaccionNombre(rs.getString("tipo_nombre"));
        
        try {
            dto.setUsuarioNombre(rs.getString("usuario_nombre"));
        } catch (SQLException e) {}
        
        return dto;
    }

    private TransaccionDTO mapearResultSet(ResultSet rs) throws SQLException {
        TransaccionDTO dto = new TransaccionDTO();
        dto.setId(rs.getLong("id"));
        dto.setTipoTransaccionId(rs.getLong("tipo_transaccion_id"));
        dto.setNumeroTransaccion(rs.getString("numero_transaccion"));
        dto.setUsuarioId(rs.getLong("usuario_id"));
        
        Long sesionCajaId = rs.getLong("sesion_caja_id");
        dto.setSesionCajaId(rs.wasNull() ? null : sesionCajaId);
        
        Long proveedorId = rs.getLong("proveedor_id");
        dto.setProveedorId(rs.wasNull() ? null : proveedorId);
        
        dto.setNombrePersona(rs.getString("nombre_persona"));
        dto.setFecha(rs.getTimestamp("fecha"));
        dto.setSubtotal(rs.getBigDecimal("subtotal"));
        dto.setIgv(rs.getBigDecimal("igv"));
        dto.setTotal(rs.getBigDecimal("total"));
        dto.setMetodoPago(rs.getString("metodo_pago"));
        dto.setEstado(rs.getString("estado"));
        dto.setObservaciones(rs.getString("observaciones"));
        dto.setTipoTransaccionNombre(rs.getString("tipo_nombre"));
        
        // Usuario
        try {
            dto.setUsuarioNombre(rs.getString("usuario_nombre"));
        } catch (SQLException e) {
            // Columna no existe
        }
        
        // Proveedor si existe
        try {
            String proveedorNombre = rs.getString("proveedor_nombre");
            if (proveedorNombre != null) {
                ProveedorDTO proveedor = new ProveedorDTO();
                proveedor.setId(proveedorId);
                proveedor.setRazonSocial(proveedorNombre);
                dto.setProveedor(proveedor);
            }
        } catch (SQLException e) {
            // Columna no existe en esta consulta
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
