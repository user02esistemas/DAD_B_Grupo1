package DAO;

import DTO.ProveedorDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para Proveedores
 */
public class ProveedorDAO {

    /**
     * Insertar nuevo proveedor
     */
    public Long insertar(ProveedorDTO proveedor) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "INSERT INTO proveedores (ruc, razon_social, contacto, telefono, email, direccion, activo) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

            pstm = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, proveedor.getRuc());
            pstm.setString(2, proveedor.getRazonSocial());
            pstm.setString(3, proveedor.getContacto());
            pstm.setString(4, proveedor.getTelefono());
            pstm.setString(5, proveedor.getEmail());
            pstm.setString(6, proveedor.getDireccion());
            pstm.setBoolean(7, proveedor.isActivo());

            int filas = pstm.executeUpdate();
            if (filas > 0) {
                rs = pstm.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al insertar proveedor: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Actualizar proveedor
     */
    public int actualizar(ProveedorDTO proveedor) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE proveedores SET ruc = ?, razon_social = ?, contacto = ?, " +
                        "telefono = ?, email = ?, direccion = ?, activo = ? WHERE id = ?";

            pstm = con.prepareStatement(sql);
            pstm.setString(1, proveedor.getRuc());
            pstm.setString(2, proveedor.getRazonSocial());
            pstm.setString(3, proveedor.getContacto());
            pstm.setString(4, proveedor.getTelefono());
            pstm.setString(5, proveedor.getEmail());
            pstm.setString(6, proveedor.getDireccion());
            pstm.setBoolean(7, proveedor.isActivo());
            pstm.setLong(8, proveedor.getId());

            return pstm.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar proveedor: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(null, pstm);
        }
    }

    /**
     * Eliminar proveedor (soft delete)
     */
    public int eliminar(Long id) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE proveedores SET activo = 0 WHERE id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, id);
            return pstm.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Error al eliminar proveedor: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(null, pstm);
        }
    }

    /**
     * Buscar por ID
     */
    public ProveedorDTO buscarPorId(Long id) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT * FROM proveedores WHERE id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, id);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return mapearResultSet(rs);
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar proveedor: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Buscar por RUC
     */
    public ProveedorDTO buscarPorRuc(String ruc) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT * FROM proveedores WHERE ruc = ?";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, ruc);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return mapearResultSet(rs);
            }
            return null;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar proveedor por RUC: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar todos los proveedores
     */
    public List<ProveedorDTO> listarTodos() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<ProveedorDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT * FROM proveedores ORDER BY razon_social";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar proveedores: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Listar solo proveedores activos
     */
    public List<ProveedorDTO> listarActivos() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<ProveedorDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT * FROM proveedores WHERE activo = 1 ORDER BY razon_social";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar proveedores activos: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Verificar si existe RUC
     */
    public boolean existeRuc(String ruc) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM proveedores WHERE ruc = ?";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, ruc);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
            return false;

        } catch (SQLException ex) {
            throw new RuntimeException("Error al verificar RUC: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    /**
     * Búsqueda para autocomplete
     */
    public List<ProveedorDTO> buscarParaAutocomplete(String termino) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<ProveedorDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT * FROM proveedores " +
                        "WHERE activo = 1 AND (razon_social LIKE ? OR ruc LIKE ?) " +
                        "ORDER BY razon_social LIMIT 10";
            
            pstm = con.prepareStatement(sql);
            String busqueda = "%" + termino + "%";
            pstm.setString(1, busqueda);
            pstm.setString(2, busqueda);
            rs = pstm.executeQuery();

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
            return lista;

        } catch (SQLException ex) {
            throw new RuntimeException("Error en búsqueda de proveedores: " + ex.getMessage(), ex);
        } finally {
            cerrarRecursos(rs, pstm);
        }
    }

    // =====================================================
    //              MÉTODOS AUXILIARES
    // =====================================================

    private ProveedorDTO mapearResultSet(ResultSet rs) throws SQLException {
        ProveedorDTO dto = new ProveedorDTO();
        dto.setId(rs.getLong("id"));
        dto.setRuc(rs.getString("ruc"));
        dto.setRazonSocial(rs.getString("razon_social"));
        dto.setContacto(rs.getString("contacto"));
        dto.setTelefono(rs.getString("telefono"));
        dto.setEmail(rs.getString("email"));
        dto.setDireccion(rs.getString("direccion"));
        dto.setActivo(rs.getBoolean("activo"));
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
