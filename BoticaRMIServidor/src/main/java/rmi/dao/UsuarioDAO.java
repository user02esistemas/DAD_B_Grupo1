package rmi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import rmi.config.DatabaseConfig;
import rmi.dto.RolDTO;
import rmi.dto.UsuarioDTO;

public class UsuarioDAO {

    public List<UsuarioDTO> listarTodos() {
        String sql = "SELECT id, username, password, email, nombre_completo, dni, "
                + "telefono, activo, created_at, updated_at "
                + "FROM usuarios ORDER BY created_at DESC";
        List<UsuarioDTO> usuarios = new ArrayList<>();

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs, false));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al listar usuarios", ex);
        }
        return usuarios;
    }

    public UsuarioDTO buscarPorId(Long id) {
        String sql = "SELECT id, username, password, email, nombre_completo, dni, "
                + "telefono, activo, created_at, updated_at "
                + "FROM usuarios WHERE id = ?";

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearUsuario(rs, false) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar usuario", ex);
        }
    }

    public UsuarioDTO buscarPorIdConPassword(Long id) {
        String sql = "SELECT id, username, password, email, nombre_completo, dni, "
                + "telefono, activo, created_at, updated_at "
                + "FROM usuarios WHERE id = ?";

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearUsuario(rs, true) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar usuario", ex);
        }
    }

    public UsuarioDTO buscarPorUsername(String username) {
        String sql = "SELECT id, username, password, email, nombre_completo, dni, "
                + "telefono, activo, created_at, updated_at "
                + "FROM usuarios WHERE username = ?";

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return mapearUsuario(rs, true);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al buscar usuario", ex);
        }
    }

    public boolean existeUsername(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        return existe(sql, username);
    }

    public boolean existeEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        return existe(sql, email);
    }

    public boolean existeDni(String dni) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE dni = ?";
        return existe(sql, dni);
    }

    public List<RolDTO> listarRoles() {
        String sql = "SELECT id, nombre, descripcion FROM roles ORDER BY nombre";
        List<RolDTO> roles = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RolDTO rol = new RolDTO();
                rol.setId(rs.getLong("id"));
                rol.setNombre(rs.getString("nombre"));
                rol.setDescripcion(rs.getString("descripcion"));
                roles.add(rol);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al listar roles", ex);
        }
        return roles;
    }

    public Long insertar(UsuarioDTO usuario) {
        String sql = "INSERT INTO usuarios (username, password, email, nombre_completo, dni, telefono, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPassword());
            ps.setString(3, usuario.getEmail());
            ps.setString(4, usuario.getNombreCompleto());
            ps.setString(5, usuario.getDni());
            ps.setString(6, usuario.getTelefono());
            ps.setBoolean(7, usuario.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new IllegalStateException("No se obtuvo id de usuario creado");
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al registrar usuario", ex);
        }
    }

    public boolean actualizar(UsuarioDTO usuario) {
        String sql = "UPDATE usuarios SET username = ?, email = ?, nombre_completo = ?, dni = ?, telefono = ?, activo = ? WHERE id = ?";

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getNombreCompleto());
            ps.setString(4, usuario.getDni());
            ps.setString(5, usuario.getTelefono());
            ps.setBoolean(6, usuario.isActivo());
            ps.setLong(7, usuario.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al actualizar usuario", ex);
        }
    }

    public boolean desactivar(Long id) {
        String sql = "UPDATE usuarios SET activo = 0 WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al desactivar usuario", ex);
        }
    }

    public boolean actualizarPassword(Long usuarioId, String nuevaPasswordHash) {
        String sql = "UPDATE usuarios SET password = ? WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevaPasswordHash);
            ps.setLong(2, usuarioId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al actualizar password", ex);
        }
    }

    public boolean asignarRoles(Long usuarioId, List<Long> rolesIds) {
        String deleteSql = "DELETE FROM usuario_roles WHERE usuario_id = ?";
        String insertSql = "INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (?, ?)";

        try (Connection con = DatabaseConfig.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement delete = con.prepareStatement(deleteSql)) {
                delete.setLong(1, usuarioId);
                delete.executeUpdate();
            }
            if (rolesIds != null && !rolesIds.isEmpty()) {
                try (PreparedStatement insert = con.prepareStatement(insertSql)) {
                    for (Long rolId : rolesIds) {
                        if (rolId == null) {
                            continue;
                        }
                        insert.setLong(1, usuarioId);
                        insert.setLong(2, rolId);
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
            }
            con.commit();
            return true;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al asignar roles", ex);
        }
    }

    private boolean existe(String sql, String value) {
        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al validar usuario", ex);
        }
    }

    private List<RolDTO> obtenerRolesPorUsuarioId(Long usuarioId) throws SQLException {
        String sql = "SELECT r.id, r.nombre, r.descripcion "
                + "FROM roles r "
                + "INNER JOIN usuario_roles ur ON r.id = ur.rol_id "
                + "WHERE ur.usuario_id = ?";
        List<RolDTO> roles = new ArrayList<>();

        try (Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RolDTO rol = new RolDTO();
                    rol.setId(rs.getLong("id"));
                    rol.setNombre(rs.getString("nombre"));
                    rol.setDescripcion(rs.getString("descripcion"));
                    roles.add(rol);
                }
            }
        }
        return roles;
    }

    private UsuarioDTO mapearUsuario(ResultSet rs, boolean incluirPassword) throws SQLException {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(rs.getLong("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPassword(incluirPassword ? rs.getString("password") : null);
        usuario.setEmail(rs.getString("email"));
        usuario.setNombreCompleto(rs.getString("nombre_completo"));
        usuario.setDni(rs.getString("dni"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setActivo(rs.getBoolean("activo"));
        usuario.setCreatedAt(rs.getTimestamp("created_at"));
        usuario.setUpdatedAt(rs.getTimestamp("updated_at"));
        usuario.setRoles(obtenerRolesPorUsuarioId(usuario.getId()));
        return usuario;
    }
}
