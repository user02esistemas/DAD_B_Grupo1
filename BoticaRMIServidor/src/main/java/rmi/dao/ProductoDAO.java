package rmi.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rmi.config.DatabaseConfig;
import rmi.dto.CatalogoProductoDTO;
import rmi.dto.ProductoDTO;
import rmi.dto.ProductoResumenDTO;

public class ProductoDAO {

    public List<ProductoResumenDTO> buscarParaVenta(String termino, int limite) {
        String sql = "SELECT p.id, p.catalogo_producto_id, p.lote, p.fecha_vencimiento, "
                + "p.stock_actual, p.precio_compra, p.precio_venta, c.codigo_producto, c.nombre_comercial, "
                + "c.principio_activo, c.concentracion, c.presentacion, c.laboratorio "
                + "FROM productos p "
                + "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id "
                + "WHERE p.activo = 1 AND p.stock_actual > 0 "
                + "AND p.fecha_vencimiento > CURDATE() "
                + "AND (c.nombre_comercial LIKE ? OR c.principio_activo LIKE ? "
                + "OR c.concentracion LIKE ? OR p.lote LIKE ?) "
                + "ORDER BY c.nombre_comercial, p.fecha_vencimiento "
                + "LIMIT ?";

        List<ProductoResumenDTO> productos = new ArrayList<>();
        String busqueda = "%" + termino + "%";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, busqueda);
            statement.setString(2, busqueda);
            statement.setString(3, busqueda);
            statement.setString(4, busqueda);
            statement.setInt(5, limite);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapearProducto(rs));
                }
            }
            return productos;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar productos para venta", ex);
        }
    }

    public ProductoResumenDTO buscarPorId(Long id) {
        String sql = "SELECT p.id, p.catalogo_producto_id, p.lote, p.fecha_vencimiento, "
                + "p.stock_actual, p.precio_compra, p.precio_venta, c.codigo_producto, c.nombre_comercial, "
                + "c.principio_activo, c.concentracion, c.presentacion, c.laboratorio "
                + "FROM productos p "
                + "INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id "
                + "WHERE p.id = ? AND p.activo = 1";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapearProducto(rs) : null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar producto", ex);
        }
    }

    public List<ProductoDTO> buscarInventario(String termino, String filtroStock, String filtroVencimiento, int pagina, int porPagina) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.*, ");
        sql.append("c.codigo_producto, c.nombre_comercial, c.principio_activo, c.concentracion, ");
        sql.append("c.forma_farmaceutica, c.presentacion, c.laboratorio, c.cantidad as cantidad_presentacion ");
        sql.append("FROM productos p ");
        sql.append("INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id ");
        sql.append("WHERE p.activo = 1 ");

        List<Object> params = new ArrayList<>();
        agregarFiltrosInventario(sql, params, termino, null, filtroStock, filtroVencimiento);
        sql.append("ORDER BY c.nombre_comercial, p.fecha_vencimiento LIMIT ? OFFSET ?");
        params.add(porPagina);
        params.add((pagina - 1) * porPagina);

        List<ProductoDTO> productos = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapearInventario(rs));
                }
            }
            return productos;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar inventario", ex);
        }
    }

    public int contarInventario(String termino, String filtroStock, String filtroVencimiento) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) as total FROM productos p ");
        sql.append("INNER JOIN catalogo_productos_digemid c ON p.catalogo_producto_id = c.id ");
        sql.append("WHERE p.activo = 1 ");

        List<Object> params = new ArrayList<>();
        agregarFiltrosInventario(sql, params, termino, null, filtroStock, filtroVencimiento);

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar inventario", ex);
        }
    }

    public int contarTotal() {
        return contarSimple("SELECT COUNT(*) as total FROM productos WHERE activo = 1");
    }

    public int contarStockBajo() {
        return contarSimple("SELECT COUNT(*) as total FROM productos WHERE activo = 1 AND stock_actual <= stock_minimo AND stock_actual > 0");
    }

    public int contarAgotados() {
        return contarSimple("SELECT COUNT(*) as total FROM productos WHERE activo = 1 AND stock_actual = 0");
    }

    public int contarPorVencer(int dias) {
        String sql = "SELECT COUNT(*) as total FROM productos WHERE activo = 1 AND stock_actual > 0 "
                + "AND fecha_vencimiento BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)";
        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dias);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar productos por vencer", ex);
        }
    }

    private int contarSimple(String sql) {
        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar productos", ex);
        }
    }

    private void agregarFiltrosInventario(StringBuilder sql, List<Object> params, String termino, String estado, String filtroStock, String filtroVencimiento) {
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
                default:
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
                default:
                    break;
            }
        }
    }

    private void setParams(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    private ProductoResumenDTO mapearProducto(ResultSet rs) throws SQLException {
        ProductoResumenDTO producto = new ProductoResumenDTO();
        producto.setId(rs.getLong("id"));
        producto.setCatalogoProductoId(rs.getLong("catalogo_producto_id"));
        producto.setLote(rs.getString("lote"));
        producto.setFechaVencimiento(String.valueOf(rs.getDate("fecha_vencimiento")));
        producto.setStockActual(rs.getInt("stock_actual"));
        producto.setPrecioCompra(rs.getBigDecimal("precio_compra"));
        producto.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        producto.setCodigoProducto(rs.getString("codigo_producto"));
        producto.setNombreComercial(rs.getString("nombre_comercial"));
        producto.setPrincipioActivo(rs.getString("principio_activo"));
        producto.setConcentracion(rs.getString("concentracion"));
        producto.setPresentacion(rs.getString("presentacion"));
        producto.setLaboratorio(rs.getString("laboratorio"));
        return producto;
    }

    private ProductoDTO mapearInventario(ResultSet rs) throws SQLException {
        ProductoDTO producto = new ProductoDTO();
        producto.setId(rs.getLong("id"));
        producto.setCatalogoProductoId(rs.getLong("catalogo_producto_id"));
        producto.setLote(rs.getString("lote"));
        producto.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
        producto.setStockActual(rs.getInt("stock_actual"));
        producto.setStockMinimo(rs.getInt("stock_minimo"));
        producto.setPrecioCompra(rs.getBigDecimal("precio_compra"));
        producto.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        producto.setUbicacion(rs.getString("ubicacion"));
        producto.setRequiereReceta(rs.getBoolean("requiere_receta"));
        producto.setEstado(rs.getString("estado"));
        producto.setActivo(rs.getBoolean("activo"));
        producto.setCreatedAt(rs.getTimestamp("created_at"));
        producto.setUpdatedAt(rs.getTimestamp("updated_at"));

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
        producto.setCatalogoProducto(catalogo);
        return producto;
    }
}
