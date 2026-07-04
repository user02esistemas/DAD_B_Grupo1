/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

/**
 *
 * @author yerri
 */
import DTO.RolDTO;
import DTO.UsuarioDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    /**
     * Busca un usuario por username
     */
    public UsuarioDTO buscarPorUsername(String username) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            con = Conexion.getConnection();
            String sql = "SELECT id, username, password, email, nombre_completo, dni, "
                    + "telefono, activo, created_at, updated_at "
                    + "FROM usuarios WHERE username = ?";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, username);
            rs = pstm.executeQuery();

            if (rs.next()) {
                UsuarioDTO usuario = new UsuarioDTO();
                usuario.setId(rs.getLong("id"));
                usuario.setUsername(rs.getString("username"));
                usuario.setPassword(rs.getString("password"));
                usuario.setEmail(rs.getString("email"));
                usuario.setNombreCompleto(rs.getString("nombre_completo"));
                usuario.setDni(rs.getString("dni"));
                usuario.setTelefono(rs.getString("telefono"));
                usuario.setActivo(rs.getBoolean("activo"));
                usuario.setCreatedAt(rs.getTimestamp("created_at"));
                usuario.setUpdatedAt(rs.getTimestamp("updated_at"));

                // Cargar roles del usuario
                usuario.setRoles(obtenerRolesPorUsuarioId(usuario.getId()));

                return usuario;
            }
            return null;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar usuario: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Obtiene los roles de un usuario
     */
    private List<RolDTO> obtenerRolesPorUsuarioId(Long usuarioId) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<RolDTO> roles = new ArrayList<>();
        try {
            con = Conexion.getConnection();
            String sql = "SELECT r.id, r.nombre, r.descripcion "
                    + "FROM roles r "
                    + "INNER JOIN usuario_roles ur ON r.id = ur.rol_id "
                    + "WHERE ur.usuario_id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, usuarioId);
            rs = pstm.executeQuery();

            while (rs.next()) {
                RolDTO rol = new RolDTO();
                rol.setId(rs.getLong("id"));
                rol.setNombre(rs.getString("nombre"));
                rol.setDescripcion(rs.getString("descripcion"));
                roles.add(rol);
            }
            return roles;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al obtener roles: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Verifica si el usuario existe y está activo
     */
    public boolean existeUsuarioActivo(String username) {
        UsuarioDTO usuario = buscarPorUsername(username);
        return usuario != null && usuario.isActivo();
    }

    /**
     * Listar todos los usuarios
     */
    public List<UsuarioDTO> listarTodos() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<UsuarioDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT id, username, password, email, nombre_completo, dni, "
                    + "telefono, activo, created_at, updated_at "
                    + "FROM usuarios ORDER BY created_at DESC";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                UsuarioDTO usuario = new UsuarioDTO();
                usuario.setId(rs.getLong("id"));
                usuario.setUsername(rs.getString("username"));
                usuario.setPassword(rs.getString("password"));
                usuario.setEmail(rs.getString("email"));
                usuario.setNombreCompleto(rs.getString("nombre_completo"));
                usuario.setDni(rs.getString("dni"));
                usuario.setTelefono(rs.getString("telefono"));
                usuario.setActivo(rs.getBoolean("activo"));
                usuario.setCreatedAt(rs.getTimestamp("created_at"));
                usuario.setUpdatedAt(rs.getTimestamp("updated_at"));
                usuario.setRoles(obtenerRolesPorUsuarioId(usuario.getId()));
                lista.add(usuario);
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar usuarios: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Buscar usuario por ID
     */
    public UsuarioDTO buscarPorId(Long id) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT id, username, password, email, nombre_completo, dni, "
                    + "telefono, activo, created_at, updated_at "
                    + "FROM usuarios WHERE id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, id);
            rs = pstm.executeQuery();

            if (rs.next()) {
                UsuarioDTO usuario = new UsuarioDTO();
                usuario.setId(rs.getLong("id"));
                usuario.setUsername(rs.getString("username"));
                usuario.setPassword(rs.getString("password"));
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
            return null;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar usuario: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Insertar nuevo usuario (sin contraseña hasheada aún) NOTA: La contraseña
     * debe venir ya hasheada con BCrypt
     */
    public Long insertar(UsuarioDTO usuario) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "INSERT INTO usuarios (username, password, email, nombre_completo, "
                    + "dni, telefono, activo) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstm = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, usuario.getUsername());
            pstm.setString(2, usuario.getPassword()); // Ya debe venir hasheada
            pstm.setString(3, usuario.getEmail());
            pstm.setString(4, usuario.getNombreCompleto());
            pstm.setString(5, usuario.getDni());
            pstm.setString(6, usuario.getTelefono());
            pstm.setBoolean(7, usuario.isActivo());

            int filasAfectadas = pstm.executeUpdate();

            if (filasAfectadas > 0) {
                rs = pstm.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al insertar usuario: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Actualizar usuario existente
     */
    public int actualizar(UsuarioDTO usuario) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE usuarios SET username = ?, email = ?, nombre_completo = ?, "
                    + "dni = ?, telefono = ?, activo = ? WHERE id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, usuario.getUsername());
            pstm.setString(2, usuario.getEmail());
            pstm.setString(3, usuario.getNombreCompleto());
            pstm.setString(4, usuario.getDni());
            pstm.setString(5, usuario.getTelefono());
            pstm.setBoolean(6, usuario.isActivo());
            pstm.setLong(7, usuario.getId());

            return pstm.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar usuario: " + ex.getMessage(), ex);
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Actualizar solo la contraseña
     */
    public int actualizarPassword(Long usuarioId, String passwordHasheada) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE usuarios SET password = ? WHERE id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, passwordHasheada);
            pstm.setLong(2, usuarioId);

            return pstm.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al actualizar contraseña: " + ex.getMessage(), ex);
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Eliminar usuario (soft delete - solo desactiva)
     */
    public int desactivar(Long id) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "UPDATE usuarios SET activo = 0 WHERE id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, id);

            return pstm.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al desactivar usuario: " + ex.getMessage(), ex);
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Asignar rol a un usuario
     */
    public int asignarRol(Long usuarioId, Long rolId) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (?, ?)";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, usuarioId);
            pstm.setLong(2, rolId);

            return pstm.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al asignar rol: " + ex.getMessage(), ex);
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Eliminar todos los roles de un usuario
     */
    public int eliminarRoles(Long usuarioId) {
        Connection con = null;
        PreparedStatement pstm = null;

        try {
            con = Conexion.getConnection();
            String sql = "DELETE FROM usuario_roles WHERE usuario_id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, usuarioId);

            return pstm.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error al eliminar roles: " + ex.getMessage(), ex);
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Verificar si un username ya existe
     */
    public boolean existeUsername(String username) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM usuarios WHERE username = ?";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, username);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
            return false;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al verificar username: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Verificar si un email ya existe
     */
    public boolean existeEmail(String email) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM usuarios WHERE email = ?";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, email);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
            return false;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al verificar email: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Verificar si un DNI ya existe
     */
    public boolean existeDni(String dni) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT COUNT(*) as total FROM usuarios WHERE dni = ?";
            pstm = con.prepareStatement(sql);
            pstm.setString(1, dni);
            rs = pstm.executeQuery();

            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
            return false;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al verificar DNI: " + ex.getMessage(), ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    /**
     * Eliminar usuario físicamente de la base de datos (hard delete)
     * IMPORTANTE: Primero elimina los roles asociados por la relación de clave foránea
     */
    public int eliminar(Long id) {
        Connection con = null;
        PreparedStatement pstmRoles = null;
        PreparedStatement pstmUsuario = null;

        try {
            con = Conexion.getConnection();
            con.setAutoCommit(false); // Iniciar transacción
            
            // Primero eliminar los roles del usuario (tabla usuario_roles)
            String sqlRoles = "DELETE FROM usuario_roles WHERE usuario_id = ?";
            pstmRoles = con.prepareStatement(sqlRoles);
            pstmRoles.setLong(1, id);
            pstmRoles.executeUpdate();
            
            // Luego eliminar el usuario
            String sqlUsuario = "DELETE FROM usuarios WHERE id = ?";
            pstmUsuario = con.prepareStatement(sqlUsuario);
            pstmUsuario.setLong(1, id);
            int resultado = pstmUsuario.executeUpdate();
            
            con.commit(); // Confirmar transacción
            return resultado;
            
        } catch (SQLException ex) {
            // Si hay error, hacer rollback
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            throw new RuntimeException("Error al eliminar usuario: " + ex.getMessage(), ex);
        } finally {
            try {
                if (pstmRoles != null) {
                    pstmRoles.close();
                }
                if (pstmUsuario != null) {
                    pstmUsuario.close();
                }
                if (con != null) {
                    con.setAutoCommit(true); // Restaurar autocommit
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

}
