package DAO;

import DTO.CatalogoProductoDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para Catálogo de Productos DIGEMID
 * Optimizado para búsquedas rápidas en 14k+ registros
 */
public class CatalogoProductoDAO {

    /**
     * Búsqueda optimizada para autocomplete
     * Usa LIKE con índice y LIMIT para rendimiento
     * Busca en: nombre_comercial, principio_activo, concentracion, laboratorio
     */
    public List<CatalogoProductoDTO> buscarParaAutocomplete(String termino, int limite) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<CatalogoProductoDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            
            // Búsqueda optimizada: primero por nombre_comercial (más común),
            // luego por principio_activo si no hay suficientes resultados
            String sql = "SELECT id, codigo_producto, nombre_comercial, principio_activo, " +
                        "concentracion, forma_farmaceutica, presentacion, laboratorio, " +
                        "registro_sanitario, cantidad, nombre_titular, nombre_fabricante, activo " +
                        "FROM catalogo_productos_digemid " +
                        "WHERE activo = 1 AND (" +
                        "nombre_comercial LIKE ? OR " +
                        "principio_activo LIKE ? OR " +
                        "codigo_producto LIKE ? OR " +
                        "laboratorio LIKE ?) " +
                        "ORDER BY " +
                        "CASE " +
                        "  WHEN nombre_comercial LIKE ? THEN 1 " +
                        "  WHEN principio_activo LIKE ? THEN 2 " +
                        "  ELSE 3 " +
                        "END, nombre_comercial " +
                        "LIMIT ?";

            pstm = con.prepareStatement(sql);
            
            String terminoBusqueda = "%" + termino.trim() + "%";
            String terminoInicio = termino.trim() + "%";
            
            pstm.setString(1, terminoBusqueda);
            pstm.setString(2, terminoBusqueda);
            pstm.setString(3, terminoBusqueda);
            pstm.setString(4, terminoBusqueda);
            pstm.setString(5, terminoInicio);
            pstm.setString(6, terminoInicio);
            pstm.setInt(7, limite);

            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error en búsqueda de catálogo: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Buscar por código de producto (código de barras)
     */
    public CatalogoProductoDTO buscarPorCodigo(String codigo) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT id, codigo_producto, nombre_comercial, principio_activo, " +
                        "concentracion, forma_farmaceutica, presentacion, laboratorio, " +
                        "registro_sanitario, cantidad, nombre_titular, nombre_fabricante, activo, created_at " +
                        "FROM catalogo_productos_digemid WHERE codigo_producto = ? AND activo = 1";

            pstm = con.prepareStatement(sql);
            pstm.setString(1, codigo);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return mapearResultSetCompleto(rs);
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar por código: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Buscar por ID
     */
    public CatalogoProductoDTO buscarPorId(Long id) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT id, codigo_producto, nombre_comercial, principio_activo, " +
                        "concentracion, forma_farmaceutica, presentacion, laboratorio, " +
                        "registro_sanitario, cantidad, nombre_titular, nombre_fabricante, activo, created_at " +
                        "FROM catalogo_productos_digemid WHERE id = ?";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, id);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return mapearResultSetCompleto(rs);
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar por ID: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar paginado
     */
    public List<CatalogoProductoDTO> listarPaginado(int pagina, int porPagina) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<CatalogoProductoDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT id, codigo_producto, nombre_comercial, principio_activo, " +
                        "concentracion, forma_farmaceutica, presentacion, laboratorio, " +
                        "registro_sanitario, cantidad, nombre_titular, nombre_fabricante, activo " +
                        "FROM catalogo_productos_digemid WHERE activo = 1 " +
                        "ORDER BY nombre_comercial " +
                        "LIMIT ? OFFSET ?";

            pstm = con.prepareStatement(sql);
            pstm.setInt(1, porPagina);
            pstm.setInt(2, (pagina - 1) * porPagina);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar catálogo: " + ex.getMessage(), ex);
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
            String sql = "SELECT COUNT(*) as total FROM catalogo_productos_digemid WHERE activo = 1";
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
     * Contar productos activos
     */
    public int contarActivos() {
        return contarTotal();
    }

    /**
     * Contar laboratorios distintos
     */
    public int contarLaboratorios() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(DISTINCT laboratorio) as total FROM catalogo_productos_digemid WHERE activo = 1 AND laboratorio IS NOT NULL AND laboratorio != ''";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al contar laboratorios: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Obtener fecha del último producto agregado
     */
    public String obtenerUltimoAgregado() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT DATE_FORMAT(created_at, '%d/%m/%Y') as fecha FROM catalogo_productos_digemid ORDER BY created_at DESC LIMIT 1";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getString("fecha");
            }
            return null;

        } catch (SQLException ex) {
            return null;
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Insertar nuevo producto
     */
    public Long insertar(CatalogoProductoDTO producto) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "INSERT INTO catalogo_productos_digemid (codigo_producto, nombre_comercial, " +
                        "principio_activo, concentracion, forma_farmaceutica, presentacion, " +
                        "laboratorio, registro_sanitario, cantidad, nombre_titular, nombre_fabricante, activo) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";

            pstm = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, producto.getCodigoProducto());
            pstm.setString(2, producto.getNombreComercial());
            pstm.setString(3, producto.getPrincipioActivo());
            pstm.setString(4, producto.getConcentracion());
            pstm.setString(5, producto.getFormaFarmaceutica());
            pstm.setString(6, producto.getPresentacion());
            pstm.setString(7, producto.getLaboratorio());
            pstm.setString(8, producto.getRegistroSanitario());
            pstm.setInt(9, producto.getCantidad() != null ? producto.getCantidad() : 1);
            pstm.setString(10, producto.getNombreTitular());
            pstm.setString(11, producto.getNombreFabricante());

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
    public boolean actualizar(CatalogoProductoDTO producto) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE catalogo_productos_digemid SET " +
                        "codigo_producto = ?, nombre_comercial = ?, principio_activo = ?, " +
                        "concentracion = ?, forma_farmaceutica = ?, presentacion = ?, " +
                        "laboratorio = ?, registro_sanitario = ?, cantidad = ?, " +
                        "nombre_titular = ?, nombre_fabricante = ? " +
                        "WHERE id = ?";

            pstm = con.prepareStatement(sql);
            pstm.setString(1, producto.getCodigoProducto());
            pstm.setString(2, producto.getNombreComercial());
            pstm.setString(3, producto.getPrincipioActivo());
            pstm.setString(4, producto.getConcentracion());
            pstm.setString(5, producto.getFormaFarmaceutica());
            pstm.setString(6, producto.getPresentacion());
            pstm.setString(7, producto.getLaboratorio());
            pstm.setString(8, producto.getRegistroSanitario());
            pstm.setInt(9, producto.getCantidad() != null ? producto.getCantidad() : 1);
            pstm.setString(10, producto.getNombreTitular());
            pstm.setString(11, producto.getNombreFabricante());
            pstm.setLong(12, producto.getId());

            return pstm.executeUpdate() > 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar producto: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(null, pstm);
        }
    }

    /**
     * Desactivar producto (eliminación lógica)
     */
    public boolean desactivar(Long id) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE catalogo_productos_digemid SET activo = 0 WHERE id = ?";

            pstm = con.prepareStatement(sql);
            pstm.setLong(1, id);

            return pstm.executeUpdate() > 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al desactivar producto: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(null, pstm);
        }
    }

    // =====================================================
    //              MÉTODOS AUXILIARES
    // =====================================================

    private CatalogoProductoDTO mapearResultSet(ResultSet rs) throws SQLException {
        CatalogoProductoDTO dto = new CatalogoProductoDTO();
        dto.setId(rs.getLong("id"));
        dto.setCodigoProducto(rs.getString("codigo_producto"));
        dto.setNombreComercial(rs.getString("nombre_comercial"));
        dto.setPrincipioActivo(rs.getString("principio_activo"));
        dto.setConcentracion(rs.getString("concentracion"));
        dto.setFormaFarmaceutica(rs.getString("forma_farmaceutica"));
        dto.setPresentacion(rs.getString("presentacion"));
        dto.setLaboratorio(rs.getString("laboratorio"));
        dto.setRegistroSanitario(rs.getString("registro_sanitario"));
        dto.setCantidad(rs.getInt("cantidad"));
        dto.setNombreTitular(rs.getString("nombre_titular"));
        dto.setNombreFabricante(rs.getString("nombre_fabricante"));
        dto.setActivo(rs.getBoolean("activo"));
        return dto;
    }

    private CatalogoProductoDTO mapearResultSetCompleto(ResultSet rs) throws SQLException {
        CatalogoProductoDTO dto = mapearResultSet(rs);
        dto.setCreatedAt(rs.getTimestamp("created_at"));
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
