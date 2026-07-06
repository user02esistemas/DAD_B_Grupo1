package rmi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import rmi.config.DatabaseConfig;
import rmi.dto.CatalogoProductoDTO;

public class CatalogoProductoDAO {

    public CatalogoProductoDTO buscarPorId(Long id) {
        String sql = columnas() + " FROM catalogo_productos_digemid WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearCompleto(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar producto de catalogo", ex);
        }
    }

    public CatalogoProductoDTO buscarPorCodigo(String codigo) {
        String sql = columnas() + " FROM catalogo_productos_digemid WHERE codigo_producto = ? AND activo = 1";
        try (Connection con = DatabaseConfig.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearCompleto(rs) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar producto de catalogo por codigo", ex);
        }
    }

    public List<CatalogoProductoDTO> buscarParaAutocomplete(String termino, int limite) {
        String sql = columnasSinCreated() + " FROM catalogo_productos_digemid "
                + "WHERE activo = 1 AND (nombre_comercial LIKE ? OR principio_activo LIKE ? OR codigo_producto LIKE ? OR laboratorio LIKE ?) "
                + "ORDER BY CASE WHEN nombre_comercial LIKE ? THEN 1 WHEN principio_activo LIKE ? THEN 2 ELSE 3 END, nombre_comercial LIMIT ?";
        List<CatalogoProductoDTO> productos = new ArrayList<>();
        String busqueda = "%" + (termino == null ? "" : termino.trim()) + "%";
        String inicio = (termino == null ? "" : termino.trim()) + "%";
        try (Connection con = DatabaseConfig.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, busqueda);
            ps.setString(2, busqueda);
            ps.setString(3, busqueda);
            ps.setString(4, busqueda);
            ps.setString(5, inicio);
            ps.setString(6, inicio);
            ps.setInt(7, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
            }
            return productos;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar catalogo", ex);
        }
    }

    public List<CatalogoProductoDTO> listarPaginado(int pagina, int porPagina) {
        String sql = columnasSinCreated() + " FROM catalogo_productos_digemid WHERE activo = 1 ORDER BY nombre_comercial LIMIT ? OFFSET ?";
        List<CatalogoProductoDTO> productos = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, porPagina);
            ps.setInt(2, (pagina - 1) * porPagina);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
            }
            return productos;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al listar catalogo", ex);
        }
    }

    public int contarTotal() {
        return contar("SELECT COUNT(*) FROM catalogo_productos_digemid WHERE activo = 1");
    }

    public int contarLaboratorios() {
        return contar("SELECT COUNT(DISTINCT laboratorio) FROM catalogo_productos_digemid WHERE activo = 1 AND laboratorio IS NOT NULL AND laboratorio != ''");
    }

    public String obtenerUltimoAgregado() {
        String sql = "SELECT DATE_FORMAT(created_at, '%d/%m/%Y') as fecha FROM catalogo_productos_digemid ORDER BY created_at DESC LIMIT 1";
        try (Connection con = DatabaseConfig.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString("fecha") : null;
        } catch (SQLException ex) {
            return null;
        }
    }

    public Long insertar(CatalogoProductoDTO producto) {
        String sql = "INSERT INTO catalogo_productos_digemid (codigo_producto, nombre_comercial, principio_activo, concentracion, forma_farmaceutica, presentacion, laboratorio, registro_sanitario, cantidad, nombre_titular, nombre_fabricante, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
        try (Connection con = DatabaseConfig.getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setProducto(ps, producto);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al insertar producto de catalogo", ex);
        }
    }

    public boolean actualizar(CatalogoProductoDTO producto) {
        String sql = "UPDATE catalogo_productos_digemid SET codigo_producto = ?, nombre_comercial = ?, principio_activo = ?, concentracion = ?, forma_farmaceutica = ?, presentacion = ?, laboratorio = ?, registro_sanitario = ?, cantidad = ?, nombre_titular = ?, nombre_fabricante = ? WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            setProducto(ps, producto);
            ps.setLong(12, producto.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al actualizar producto de catalogo", ex);
        }
    }

    public boolean desactivar(Long id) {
        String sql = "UPDATE catalogo_productos_digemid SET activo = 0 WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al desactivar producto de catalogo", ex);
        }
    }

    private void setProducto(PreparedStatement ps, CatalogoProductoDTO producto) throws SQLException {
        ps.setString(1, producto.getCodigoProducto());
        ps.setString(2, producto.getNombreComercial());
        ps.setString(3, producto.getPrincipioActivo());
        ps.setString(4, producto.getConcentracion());
        ps.setString(5, producto.getFormaFarmaceutica());
        ps.setString(6, producto.getPresentacion());
        ps.setString(7, producto.getLaboratorio());
        ps.setString(8, producto.getRegistroSanitario());
        ps.setInt(9, producto.getCantidad() == null ? 1 : producto.getCantidad());
        ps.setString(10, producto.getNombreTitular());
        ps.setString(11, producto.getNombreFabricante());
    }

    private int contar(String sql) {
        try (Connection con = DatabaseConfig.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al contar catalogo", ex);
        }
    }

    private String columnas() {
        return columnasSinCreated() + ", created_at";
    }

    private String columnasSinCreated() {
        return "SELECT id, codigo_producto, nombre_comercial, principio_activo, concentracion, forma_farmaceutica, presentacion, laboratorio, registro_sanitario, cantidad, nombre_titular, nombre_fabricante, activo";
    }

    private CatalogoProductoDTO mapearCompleto(ResultSet rs) throws SQLException {
        CatalogoProductoDTO producto = mapear(rs);
        producto.setCreatedAt(rs.getTimestamp("created_at"));
        return producto;
    }

    private CatalogoProductoDTO mapear(ResultSet rs) throws SQLException {
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
        return producto;
    }
}
