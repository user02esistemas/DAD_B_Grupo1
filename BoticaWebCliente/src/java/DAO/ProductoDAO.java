package DAO;

import DTO.CatalogoProductoDTO;
import DTO.ProductoDTO;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para Productos en inventario
 * Ampliado para el módulo de Inventario
 */
public class ProductoDAO {

    /**
     * Insertar nuevo producto en inventario
     */
    public Long insertar(ProductoDTO producto) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "INSERT INTO productos (catalogo_producto_id, lote, fecha_vencimiento, " +
                        "stock_actual, stock_minimo, precio_compra, precio_venta, ubicacion, " +
                        "requiere_receta, estado, activo) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstm = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstm.setLong(1, producto.getCatalogoProductoId());
            pstm.setString(2, producto.getLote());
            pstm.setDate(3, producto.getFechaVencimiento());
            pstm.setInt(4, producto.getStockActual() != null ? producto.getStockActual() : 0);
            pstm.setInt(5, producto.getStockMinimo() != null ? producto.getStockMinimo() : 10);
            pstm.setBigDecimal(6, producto.getPrecioCompra());
            pstm.setBigDecimal(7, producto.getPrecioVenta());
            pstm.setString(8, producto.getUbicacion());
            pstm.setBoolean(9, producto.isRequiereReceta());
            pstm.setString(10, producto.getEstado() != null ? producto.getEstado() : "DISPONIBLE");
            pstm.setBoolean(11, producto.isActivo());

            int filas = pstm.executeUpdate();
            if (filas > 0) {
                rs = pstm.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al insertar producto: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Actualizar producto existente
     */
    public int actualizar(ProductoDTO producto) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE productos SET lote = ?, fecha_vencimiento = ?, " +
                        "stock_minimo = ?, precio_compra = ?, precio_venta = ?, " +
                        "ubicacion = ?, requiere_receta = ?, estado = ?, activo = ? " +
                        "WHERE id = ?";

            pstm = con.prepareStatement(sql);
            pstm.setString(1, producto.getLote());
            pstm.setDate(2, producto.getFechaVencimiento());
            pstm.setInt(3, producto.getStockMinimo());
            pstm.setBigDecimal(4, producto.getPrecioCompra());
            pstm.setBigDecimal(5, producto.getPrecioVenta());
            pstm.setString(6, producto.getUbicacion());
            pstm.setBoolean(7, producto.isRequiereReceta());
            pstm.setString(8, producto.getEstado());
            pstm.setBoolean(9, producto.isActivo());
            pstm.setLong(10, producto.getId());

            return pstm.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar producto: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(null, pstm);
        }
    }

    /**
     * Actualizar solo precios de un producto
     */
    public int actualizarPrecios(Long productoId, BigDecimal precioCompra, BigDecimal precioVenta) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE productos SET precio_compra = ?, precio_venta = ? WHERE id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setBigDecimal(1, precioCompra);
            pstm.setBigDecimal(2, precioVenta);
            pstm.setLong(3, productoId);
            return pstm.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar precios: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(null, pstm);
        }
    }

    /**
     * Actualizar stock mínimo
     */
    public int actualizarStockMinimo(Long productoId, int stockMinimo) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE productos SET stock_minimo = ? WHERE id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setInt(1, stockMinimo);
            pstm.setLong(2, productoId);
            return pstm.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar stock mínimo: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(null, pstm);
        }
    }

    /**
     * Buscar por ID con datos del catálogo
     */
    public ProductoDTO buscarPorId(Long id) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT p.*, " +
                        "c.codigo_producto, c.nombre_comercial, c.principio_activo, c.concentracion, " +
                        "c.forma_farmaceutica, c.presentacion, c.laboratorio, c.cantidad as cantidad_presentacion, " +
                        "c.registro_sanitario " +
                        "FROM productos p " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "WHERE p.id = ?";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, id);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return mapearProductoConCatalogo(rs);
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar producto: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Buscar por catálogo y lote (combinación única)
     */
    public ProductoDTO buscarPorCatalogoYLote(Long catalogoId, String lote) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT p.*, " +
                        "c.codigo_producto, c.nombre_comercial, c.principio_activo, c.concentracion, " +
                        "c.forma_farmaceutica, c.presentacion, c.laboratorio, c.cantidad as cantidad_presentacion " +
                        "FROM productos p " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "WHERE p.catalogo_producto_id = ? AND p.lote = ?";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, catalogoId);
            pstm.setString(2, lote);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return mapearProductoConCatalogo(rs);
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar producto por catálogo y lote: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Verificar si existe un lote para un producto del catálogo
     */
    public boolean existeLote(Long catalogoId, String lote) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM productos " +
                        "WHERE catalogo_producto_id = ? AND lote = ?";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, catalogoId);
            pstm.setString(2, lote);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
            return false;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al verificar lote: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar todos los productos activos con datos del catálogo (paginado)
     */
    public List<ProductoDTO> listarTodos(int pagina, int porPagina) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<ProductoDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT p.*, " +
                        "c.codigo_producto, c.nombre_comercial, c.principio_activo, c.concentracion, " +
                        "c.forma_farmaceutica, c.presentacion, c.laboratorio, c.cantidad as cantidad_presentacion " +
                        "FROM productos p " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "WHERE p.activo = 1 " +
                        "ORDER BY c.nombre_comercial, p.fecha_vencimiento " +
                        "LIMIT ? OFFSET ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, porPagina);
            pstm.setInt(2, (pagina - 1) * porPagina);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearProductoConCatalogo(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar productos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar todos sin paginación
     */
    public List<ProductoDTO> listarTodos() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<ProductoDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT p.*, " +
                        "c.codigo_producto, c.nombre_comercial, c.principio_activo, c.concentracion, " +
                        "c.forma_farmaceutica, c.presentacion, c.laboratorio, c.cantidad as cantidad_presentacion " +
                        "FROM productos p " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "WHERE p.activo = 1 " +
                        "ORDER BY c.nombre_comercial, p.fecha_vencimiento";

            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearProductoConCatalogo(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar productos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Buscar productos con filtros
     */
    public List<ProductoDTO> buscarConFiltros(String termino, String estado, String filtroStock, 
            String filtroVencimiento, int pagina, int porPagina) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<ProductoDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT p.*, ");
            sql.append("c.codigo_producto, c.nombre_comercial, c.principio_activo, c.concentracion, ");
            sql.append("c.forma_farmaceutica, c.presentacion, c.laboratorio, c.cantidad as cantidad_presentacion ");
            sql.append("FROM productos p ");
            sql.append("INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id ");
            sql.append("WHERE p.activo = 1 ");

            List<Object> params = new ArrayList<>();

            // Filtro por término de búsqueda
            if (termino != null && !termino.trim().isEmpty()) {
                sql.append("AND (c.nombre_comercial LIKE ? OR c.principio_activo LIKE ? OR p.lote LIKE ? OR c.codigo_producto LIKE ?) ");
                String busqueda = "%" + termino.trim() + "%";
                params.add(busqueda);
                params.add(busqueda);
                params.add(busqueda);
                params.add(busqueda);
            }

            // Filtro por estado
            if (estado != null && !estado.isEmpty()) {
                sql.append("AND p.estado = ? ");
                params.add(estado);
            }

            // Filtro por stock
            if (filtroStock != null && !filtroStock.isEmpty()) {
                switch (filtroStock) {
                    case "bajo":
                        sql.append("AND p.stock_actual <= p.stock_minimo AND p.stock_actual > 0 ");
                        break;
                    case "agotado":
                        sql.append("AND p.stock_actual = 0 ");
                        break;
                    case "disponible":
                        sql.append("AND p.stock_actual > p.stock_minimo ");
                        break;
                }
            }

            // Filtro por vencimiento
            if (filtroVencimiento != null && !filtroVencimiento.isEmpty()) {
                switch (filtroVencimiento) {
                    case "vencido":
                        sql.append("AND p.fecha_vencimiento < CURDATE() ");
                        break;
                    case "por_vencer_30":
                        sql.append("AND p.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) ");
                        break;
                    case "por_vencer_60":
                        sql.append("AND p.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 60 DAY) ");
                        break;
                    case "por_vencer_90":
                        sql.append("AND p.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 90 DAY) ");
                        break;
                }
            }

            sql.append("ORDER BY c.nombre_comercial, p.fecha_vencimiento ");
            sql.append("LIMIT ? OFFSET ?");
            params.add(porPagina);
            params.add((pagina - 1) * porPagina);

            pstm = con.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstm.setObject(i + 1, params.get(i));
            }

            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearProductoConCatalogo(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar productos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar productos con filtros
     */
    public int contarConFiltros(String termino, String estado, String filtroStock, String filtroVencimiento) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) as total ");
            sql.append("FROM productos p ");
            sql.append("INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id ");
            sql.append("WHERE p.activo = 1 ");

            List<Object> params = new ArrayList<>();

            if (termino != null && !termino.trim().isEmpty()) {
                sql.append("AND (c.nombre_comercial LIKE ? OR c.principio_activo LIKE ? OR p.lote LIKE ? OR c.codigo_producto LIKE ?) ");
                String busqueda = "%" + termino.trim() + "%";
                params.add(busqueda);
                params.add(busqueda);
                params.add(busqueda);
                params.add(busqueda);
            }

            if (estado != null && !estado.isEmpty()) {
                sql.append("AND p.estado = ? ");
                params.add(estado);
            }

            if (filtroStock != null && !filtroStock.isEmpty()) {
                switch (filtroStock) {
                    case "bajo":
                        sql.append("AND p.stock_actual <= p.stock_minimo AND p.stock_actual > 0 ");
                        break;
                    case "agotado":
                        sql.append("AND p.stock_actual = 0 ");
                        break;
                    case "disponible":
                        sql.append("AND p.stock_actual > p.stock_minimo ");
                        break;
                }
            }

            if (filtroVencimiento != null && !filtroVencimiento.isEmpty()) {
                switch (filtroVencimiento) {
                    case "vencido":
                        sql.append("AND p.fecha_vencimiento < CURDATE() ");
                        break;
                    case "por_vencer_30":
                        sql.append("AND p.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) ");
                        break;
                    case "por_vencer_60":
                        sql.append("AND p.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 60 DAY) ");
                        break;
                    case "por_vencer_90":
                        sql.append("AND p.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 90 DAY) ");
                        break;
                }
            }

            pstm = con.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstm.setObject(i + 1, params.get(i));
            }

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
     * Listar productos con stock bajo
     */
    public List<ProductoDTO> listarStockBajo(int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<ProductoDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT p.*, " +
                        "c.codigo_producto, c.nombre_comercial, c.principio_activo, c.concentracion, " +
                        "c.forma_farmaceutica, c.presentacion, c.laboratorio, c.cantidad as cantidad_presentacion " +
                        "FROM productos p " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "WHERE p.activo = 1 AND p.stock_actual <= p.stock_minimo " +
                        "ORDER BY p.stock_actual ASC, c.nombre_comercial " +
                        "LIMIT ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, limite);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearProductoConCatalogo(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar stock bajo: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar productos próximos a vencer
     */
    public List<ProductoDTO> listarPorVencer(int diasLimite, int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<ProductoDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT p.*, " +
                        "c.codigo_producto, c.nombre_comercial, c.principio_activo, c.concentracion, " +
                        "c.forma_farmaceutica, c.presentacion, c.laboratorio, c.cantidad as cantidad_presentacion, " +
                        "DATEDIFF(p.fecha_vencimiento, CURDATE()) as dias_restantes " +
                        "FROM productos p " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "WHERE p.activo = 1 AND p.stock_actual > 0 " +
                        "AND p.fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                        "ORDER BY p.fecha_vencimiento ASC " +
                        "LIMIT ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, diasLimite);
            pstm.setInt(2, limite);
            rs = pstm.executeQuery();

            while (rs.next()) {
                ProductoDTO dto = mapearProductoConCatalogo(rs);
                dto.setDiasParaVencer(rs.getInt("dias_restantes"));
                lista.add(dto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar por vencer: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar productos vencidos
     */
    public List<ProductoDTO> listarVencidos(int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<ProductoDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT p.*, " +
                        "c.codigo_producto, c.nombre_comercial, c.principio_activo, c.concentracion, " +
                        "c.forma_farmaceutica, c.presentacion, c.laboratorio, c.cantidad as cantidad_presentacion, " +
                        "DATEDIFF(CURDATE(), p.fecha_vencimiento) as dias_vencido " +
                        "FROM productos p " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "WHERE p.activo = 1 AND p.stock_actual > 0 " +
                        "AND p.fecha_vencimiento < CURDATE() " +
                        "ORDER BY p.fecha_vencimiento ASC " +
                        "LIMIT ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, limite);
            rs = pstm.executeQuery();

            while (rs.next()) {
                ProductoDTO dto = mapearProductoConCatalogo(rs);
                dto.setDiasParaVencer(-rs.getInt("dias_vencido"));
                lista.add(dto);
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar vencidos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar productos por catálogo (todos los lotes de un medicamento)
     */
    public List<ProductoDTO> listarPorCatalogo(Long catalogoId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<ProductoDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT p.*, " +
                        "c.codigo_producto, c.nombre_comercial, c.principio_activo, c.concentracion, " +
                        "c.forma_farmaceutica, c.presentacion, c.laboratorio, c.cantidad as cantidad_presentacion " +
                        "FROM productos p " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "WHERE p.catalogo_producto_id = ? AND p.activo = 1 " +
                        "ORDER BY p.fecha_vencimiento";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, catalogoId);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearProductoConCatalogo(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar productos por catálogo: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener stock total de un producto del catálogo (suma de todos los lotes)
     */
    public int obtenerStockTotalPorCatalogo(Long catalogoId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COALESCE(SUM(stock_actual), 0) as stock_total " +
                        "FROM productos WHERE catalogo_producto_id = ? AND activo = 1";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, catalogoId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("stock_total");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener stock total: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener precio de compra promedio de un producto del catálogo
     */
    public BigDecimal obtenerPrecioCompraPorCatalogo(Long catalogoId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COALESCE(AVG(precio_compra), 0) as precio_promedio " +
                        "FROM productos WHERE catalogo_producto_id = ? AND activo = 1";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, catalogoId);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("precio_promedio");
            }
            return BigDecimal.ZERO;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener precio promedio: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar total de productos
     */
    public int contarTotal() {
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
     * Contar productos con stock bajo
     */
    public int contarStockBajo() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM productos " +
                        "WHERE activo = 1 AND stock_actual <= stock_minimo AND stock_actual > 0";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar stock bajo: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar productos agotados
     */
    public int contarAgotados() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM productos " +
                        "WHERE activo = 1 AND stock_actual = 0";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar agotados: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Contar productos por vencer (30 días)
     */
    public int contarPorVencer(int dias) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM productos " +
                        "WHERE activo = 1 AND stock_actual > 0 " +
                        "AND fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)";
            pstm = con.prepareStatement(sql);
            pstm.setInt(1, dias);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
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
    public int contarVencidos() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM productos " +
                        "WHERE activo = 1 AND stock_actual > 0 AND fecha_vencimiento < CURDATE()";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar vencidos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener valor total del inventario
     */
    public BigDecimal obtenerValorInventario() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COALESCE(SUM(stock_actual * precio_compra), 0) as valor_total " +
                        "FROM productos WHERE activo = 1";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("valor_total");
            }
            return BigDecimal.ZERO;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener valor inventario: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Buscar productos para POS (con stock disponible)
     * Retorna productos activos con stock > 0
     */
    public List<ProductoDTO> buscarParaVenta(String termino, int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<ProductoDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT p.*, " +
                        "c.codigo_producto, c.nombre_comercial, c.principio_activo, c.concentracion, " +
                        "c.forma_farmaceutica, c.presentacion, c.laboratorio, c.cantidad as cantidad_presentacion " +
                        "FROM productos p " +
                        "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id " +
                        "WHERE p.activo = 1 AND p.stock_actual > 0 " +
                        "AND p.fecha_vencimiento > CURDATE() " +
                        "AND (c.nombre_comercial LIKE ? OR c.principio_activo LIKE ? " +
                        "OR c.concentracion LIKE ? OR p.lote LIKE ?) " +
                        "ORDER BY c.nombre_comercial, p.fecha_vencimiento " +
                        "LIMIT ?";

            String busqueda = "%" + termino + "%";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, busqueda);
            pstm.setString(2, busqueda);
            pstm.setString(3, busqueda);
            pstm.setString(4, busqueda);
            pstm.setInt(5, limite);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearProductoConCatalogo(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar productos para venta: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Eliminar producto (soft delete)
     */
    public int eliminar(Long id) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE productos SET activo = 0 WHERE id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, id);
            return pstm.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Error al eliminar producto: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(null, pstm);
        }
    }

    // =====================================================
    //              MÉTODOS AUXILIARES
    // =====================================================

    private ProductoDTO mapearProductoConCatalogo(ResultSet rs) throws SQLException {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(rs.getLong("id"));
        dto.setCatalogoProductoId(rs.getLong("catalogo_producto_id"));
        dto.setLote(rs.getString("lote"));
        dto.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
        dto.setStockActual(rs.getInt("stock_actual"));
        dto.setStockMinimo(rs.getInt("stock_minimo"));
        dto.setPrecioCompra(rs.getBigDecimal("precio_compra"));
        dto.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        dto.setUbicacion(rs.getString("ubicacion"));
        dto.setRequiereReceta(rs.getBoolean("requiere_receta"));
        dto.setEstado(rs.getString("estado"));
        dto.setActivo(rs.getBoolean("activo"));
        dto.setCreatedAt(rs.getTimestamp("created_at"));
        dto.setUpdatedAt(rs.getTimestamp("updated_at"));

        // Datos del catálogo
        CatalogoProductoDTO catalogo = new CatalogoProductoDTO();
        catalogo.setId(rs.getLong("catalogo_producto_id"));
        catalogo.setCodigoProducto(rs.getString("codigo_producto"));
        catalogo.setNombreComercial(rs.getString("nombre_comercial"));
        catalogo.setPrincipioActivo(rs.getString("principio_activo"));
        catalogo.setConcentracion(rs.getString("concentracion"));
        catalogo.setFormaFarmaceutica(rs.getString("forma_farmaceutica"));
        catalogo.setPresentacion(rs.getString("presentacion"));
        catalogo.setLaboratorio(rs.getString("laboratorio"));
        catalogo.setCantidad(rs.getInt("cantidad_presentacion"));
        
        try {
            catalogo.setRegistroSanitario(rs.getString("registro_sanitario"));
        } catch (SQLException e) {
            // Campo no incluido en la consulta
        }
        
        dto.setCatalogoProducto(catalogo);

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
