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
import rmi.dto.CatalogoProductoDTO;
import rmi.dto.DetalleTransaccionDTO;
import rmi.dto.ProductoDTO;
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

    public List<TransaccionDTO> listarCompras(int pagina, int porPagina) {
        String sql = "SELECT t.*, tt.nombre AS tipo_nombre, p.razon_social AS proveedor_nombre, u.nombre_completo AS usuario_nombre "
                + "FROM transacciones t "
                + "INNER JOIN tipos_transaccion tt ON t.tipo_transaccion_id = tt.id "
                + "LEFT JOIN proveedores p ON t.proveedor_id = p.id "
                + "LEFT JOIN usuarios u ON t.usuario_id = u.id "
                + "WHERE t.tipo_transaccion_id = 1 "
                + "ORDER BY t.fecha DESC LIMIT ? OFFSET ?";

        List<TransaccionDTO> compras = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            int paginaSegura = pagina <= 0 ? 1 : pagina;
            int porPaginaSeguro = porPagina <= 0 ? 10 : porPagina;
            ps.setInt(1, porPaginaSeguro);
            ps.setInt(2, (paginaSegura - 1) * porPaginaSeguro);
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

    public int contarCompras() {
        String sql = "SELECT COUNT(*) FROM transacciones WHERE tipo_transaccion_id = 1";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al contar compras", ex);
        }
    }

    public boolean anular(Long id) {
        String sql = "UPDATE transacciones SET estado = 'ANULADA' WHERE id = ? AND tipo_transaccion_id = 1";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al anular compra", ex);
        }
    }

    public List<CatalogoProductoDTO> buscarProductosCatalogo(String termino, int limite) {
        String sql = "SELECT * FROM catalogo_productos_digemid "
                + "WHERE activo = 1 AND (codigo_producto LIKE ? OR nombre_comercial LIKE ? OR principio_activo LIKE ? OR laboratorio LIKE ?) "
                + "ORDER BY nombre_comercial LIMIT ?";
        List<CatalogoProductoDTO> productos = new ArrayList<>();
        String filtro = "%" + (termino == null ? "" : termino.trim()) + "%";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setString(3, filtro);
            ps.setString(4, filtro);
            ps.setInt(5, limite <= 0 ? 15 : limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapearCatalogo(rs));
                }
            }
            return productos;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar catalogo", ex);
        }
    }

    public CatalogoProductoDTO buscarProductoCatalogoPorCodigo(String codigo) {
        String sql = "SELECT * FROM catalogo_productos_digemid WHERE activo = 1 AND codigo_producto = ?";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearCatalogo(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar producto por codigo", ex);
        }
    }

    public CatalogoProductoDTO buscarProductoCatalogoPorId(Long id) {
        String sql = "SELECT * FROM catalogo_productos_digemid WHERE id = ? AND activo = 1";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearCatalogo(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar producto de catalogo", ex);
        }
    }

    public int obtenerStockTotalPorCatalogo(Long catalogoId) {
        String sql = "SELECT COALESCE(SUM(stock_actual), 0) FROM productos WHERE catalogo_producto_id = ? AND activo = 1";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, catalogoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener stock", ex);
        }
    }

    public BigDecimal obtenerPrecioCompraPorCatalogo(Long catalogoId) {
        String sql = "SELECT precio_compra FROM productos WHERE catalogo_producto_id = ? AND activo = 1 ORDER BY updated_at DESC, id DESC LIMIT 1";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, catalogoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al obtener precio de compra", ex);
        }
    }

    public List<ProductoDTO> listarLotesPorCatalogo(Long catalogoId) {
        String sql = "SELECT * FROM productos WHERE catalogo_producto_id = ? AND activo = 1 ORDER BY fecha_vencimiento, lote";
        List<ProductoDTO> lotes = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, catalogoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductoDTO producto = new ProductoDTO();
                    producto.setId(rs.getLong("id"));
                    producto.setCatalogoProductoId(rs.getLong("catalogo_producto_id"));
                    producto.setLote(rs.getString("lote"));
                    producto.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
                    producto.setStockActual(rs.getInt("stock_actual"));
                    producto.setStockMinimo(rs.getInt("stock_minimo"));
                    producto.setPrecioCompra(rs.getBigDecimal("precio_compra"));
                    producto.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                    producto.setActivo(rs.getBoolean("activo"));
                    lotes.add(producto);
                }
            }
            return lotes;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al listar lotes", ex);
        }
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

    public Long insertarProveedor(ProveedorDTO proveedor) {
        String sql = "INSERT INTO proveedores (ruc, razon_social, contacto, telefono, email, direccion, activo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, proveedor.getRuc());
            ps.setString(2, proveedor.getRazonSocial());
            ps.setString(3, proveedor.getContacto());
            ps.setString(4, proveedor.getTelefono());
            ps.setString(5, proveedor.getEmail());
            ps.setString(6, proveedor.getDireccion());
            ps.setBoolean(7, proveedor.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            throw new SQLException("No se pudo obtener el ID del proveedor");
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al registrar proveedor", ex);
        }
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

        if (compra.getSubtotal() == null || compra.getIgv() == null || compra.getTotal() == null) {
            BigDecimal subtotal = BigDecimal.ZERO;
            for (DetalleTransaccionDTO detalle : detalles) {
                detalle.calcularSubtotal();
                subtotal = subtotal.add(detalle.getSubtotal());
            }
            compra.setSubtotal(subtotal);
            compra.setIgv(subtotal.multiply(new BigDecimal("0.18")).setScale(2, BigDecimal.ROUND_HALF_UP));
            compra.setTotal(compra.getSubtotal().add(compra.getIgv()));
        }
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
                Long productoId = detalle.getProductoId();
                if (productoId == null) {
                    productoId = asegurarProductoExiste(con, detalle);
                }
                ps.setLong(1, compraId);
                ps.setLong(2, productoId);
                ps.setInt(3, detalle.getCantidad());
                ps.setBigDecimal(4, detalle.getPrecioUnitario());
                ps.setBigDecimal(5, detalle.getSubtotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void validarDetalles(Connection con, List<DetalleTransaccionDTO> detalles) throws SQLException {
        for (DetalleTransaccionDTO detalle : detalles) {
            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0 || detalle.getPrecioUnitario() == null) {
                throw new IllegalArgumentException("Detalle de compra invalido");
            }
            if (detalle.getProductoId() == null) {
                if (detalle.getCatalogoProductoId() == null || detalle.getLote() == null || detalle.getLote().trim().isEmpty()
                        || detalle.getFechaVencimiento() == null || detalle.getFechaVencimiento().trim().isEmpty()
                        || detalle.getPrecioVenta() == null) {
                    throw new IllegalArgumentException("Detalle de compra invalido");
                }
                if (!existeCatalogo(con, detalle.getCatalogoProductoId())) {
                    throw new IllegalArgumentException("Producto de catalogo no encontrado: " + detalle.getCatalogoProductoId());
                }
            } else if (!existeProducto(con, detalle.getProductoId())) {
                throw new IllegalArgumentException("Producto no encontrado: " + detalle.getProductoId());
            }
        }
    }

    private Long asegurarProductoExiste(Connection con, DetalleTransaccionDTO detalle) throws SQLException {
        String sqlBuscar = "SELECT id FROM productos WHERE catalogo_producto_id = ? AND lote = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlBuscar)) {
            ps.setLong(1, detalle.getCatalogoProductoId());
            ps.setString(2, detalle.getLote());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long productoId = rs.getLong("id");
                    actualizarPreciosProducto(con, productoId, detalle);
                    return productoId;
                }
            }
        }

        String sqlInsert = "INSERT INTO productos (catalogo_producto_id, lote, fecha_vencimiento, stock_actual, precio_compra, precio_venta, activo) "
                + "VALUES (?, ?, ?, 0, ?, ?, 1)";
        try (PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, detalle.getCatalogoProductoId());
            ps.setString(2, detalle.getLote());
            ps.setDate(3, java.sql.Date.valueOf(detalle.getFechaVencimiento()));
            ps.setBigDecimal(4, detalle.getPrecioUnitario());
            ps.setBigDecimal(5, detalle.getPrecioVenta());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo crear producto para la compra");
    }

    private void actualizarPreciosProducto(Connection con, Long productoId, DetalleTransaccionDTO detalle) throws SQLException {
        String sql = "UPDATE productos SET precio_compra = ?, precio_venta = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, detalle.getPrecioUnitario());
            ps.setBigDecimal(2, detalle.getPrecioVenta());
            ps.setLong(3, productoId);
            ps.executeUpdate();
        }
    }

    private boolean existeCatalogo(Connection con, Long catalogoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM catalogo_productos_digemid WHERE id = ? AND activo = 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, catalogoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean existeProducto(Connection con, Long productoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM productos WHERE id = ? AND activo = 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private List<DetalleTransaccionDTO> obtenerDetalles(Connection con, Long compraId) throws SQLException {
        String sql = "SELECT dt.*, p.catalogo_producto_id, c.nombre_comercial, c.concentracion, p.lote, p.fecha_vencimiento "
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
                    detalle.setCatalogoProductoId(rs.getLong("catalogo_producto_id"));
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

    private CatalogoProductoDTO mapearCatalogo(ResultSet rs) throws SQLException {
        CatalogoProductoDTO producto = new CatalogoProductoDTO();
        producto.setId(rs.getLong("id"));
        producto.setCodigoProducto(rs.getString("codigo_producto"));
        producto.setNombreComercial(rs.getString("nombre_comercial"));
        producto.setPrincipioActivo(rs.getString("principio_activo"));
        producto.setConcentracion(rs.getString("concentracion"));
        producto.setFormaFarmaceutica(rs.getString("forma_farmaceutica"));
        producto.setPresentacion(rs.getString("presentacion"));
        producto.setLaboratorio(rs.getString("laboratorio"));
        producto.setRegistroSanitario(rs.getString("registro_sanitario"));
        producto.setCantidad(rs.getInt("cantidad"));
        producto.setNombreTitular(rs.getString("nombre_titular"));
        producto.setNombreFabricante(rs.getString("nombre_fabricante"));
        producto.setActivo(rs.getBoolean("activo"));
        producto.setCreatedAt(rs.getTimestamp("created_at"));
        return producto;
    }
}
