package pe.edu.botica.rmi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import pe.edu.botica.rmi.config.DatabaseConfig;
import pe.edu.botica.rmi.dto.ProductoResumenDTO;

public class ProductoDAO {

    public List<ProductoResumenDTO> buscarParaVenta(String termino, int limite) {
        String sql = "SELECT p.id, p.catalogo_producto_id, p.lote, p.fecha_vencimiento, "
                + "p.stock_actual, p.precio_venta, c.codigo_producto, c.nombre_comercial, "
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

    private ProductoResumenDTO mapearProducto(ResultSet rs) throws SQLException {
        ProductoResumenDTO producto = new ProductoResumenDTO();
        producto.setId(rs.getLong("id"));
        producto.setCatalogoProductoId(rs.getLong("catalogo_producto_id"));
        producto.setLote(rs.getString("lote"));
        producto.setFechaVencimiento(String.valueOf(rs.getDate("fecha_vencimiento")));
        producto.setStockActual(rs.getInt("stock_actual"));
        producto.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        producto.setCodigoProducto(rs.getString("codigo_producto"));
        producto.setNombreComercial(rs.getString("nombre_comercial"));
        producto.setPrincipioActivo(rs.getString("principio_activo"));
        producto.setConcentracion(rs.getString("concentracion"));
        producto.setPresentacion(rs.getString("presentacion"));
        producto.setLaboratorio(rs.getString("laboratorio"));
        return producto;
    }
}
