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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RolDAO {

    /**
     * Listar todos los roles
     */
    public List<RolDTO> listarTodos() {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<RolDTO> lista = new ArrayList<>();

        try {
            con = Conexion.getConnection();
            String sql = "SELECT id, nombre, descripcion FROM roles ORDER BY id";
            pstm = con.prepareStatement(sql);
            rs = pstm.executeQuery();

            while (rs.next()) {
                RolDTO rol = new RolDTO();
                rol.setId(rs.getLong("id"));
                rol.setNombre(rs.getString("nombre"));
                rol.setDescripcion(rs.getString("descripcion"));
                lista.add(rol);
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al listar roles: " + ex.getMessage(), ex);
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
     * Buscar rol por ID
     */
    public RolDTO buscarPorId(Long id) {
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            con = Conexion.getConnection();
            String sql = "SELECT id, nombre, descripcion FROM roles WHERE id = ?";
            pstm = con.prepareStatement(sql);
            pstm.setLong(1, id);
            rs = pstm.executeQuery();

            if (rs.next()) {
                RolDTO rol = new RolDTO();
                rol.setId(rs.getLong("id"));
                rol.setNombre(rs.getString("nombre"));
                rol.setDescripcion(rs.getString("descripcion"));
                return rol;
            }
            return null;
        } catch (SQLException ex) {
            throw new RuntimeException("Error al buscar rol: " + ex.getMessage(), ex);
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
}
