package DTO;

import DAO.UsuarioDAO;
import DAO.RolDAO;
import java.sql.Timestamp;
import java.util.List;

public class UsuarioDTO {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String nombreCompleto;
    private String dni;
    private String telefono;
    private boolean activo;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<RolDTO> roles;

    public UsuarioDTO() {
    }

    // ----------------------------
    //        GETTERS / SETTERS
    // ----------------------------
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<RolDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RolDTO> roles) {
        this.roles = roles;
    }

    // ---------------------------------------------------
    //              MÉTODOS DE NEGOCIO 
    // ---------------------------------------------------
    /**
     * Insertar usuario
     */
    public boolean insertar() {
        UsuarioDAO dao = new UsuarioDAO();
        Long idGenerado = dao.insertar(this);
        if (idGenerado != null) {
            this.id = idGenerado;
            return true;
        }
        return false;
    }

    /**
     * Actualizar usuario
     */
    public boolean actualizar() {
        UsuarioDAO dao = new UsuarioDAO();
        return dao.actualizar(this) > 0;
    }

    /**
     * Actualizar contraseña
     */
    public boolean actualizarPassword(String nuevaPasswordHash) {
        UsuarioDAO dao = new UsuarioDAO();
        return dao.actualizarPassword(this.id, nuevaPasswordHash) > 0;
    }

    /**
     * Eliminar usuario
     */
    public boolean eliminar() {
        UsuarioDAO dao = new UsuarioDAO();
        return dao.eliminar(this.id) > 0;
    }

    /**
     * Desactivar usuario
     */
    public boolean desactivar() {
        UsuarioDAO dao = new UsuarioDAO();
        return dao.desactivar(this.id) > 0;
    }

    /**
     * Cargar usuario por ID
     */
    public static UsuarioDTO buscarPorId(Long id) {
        UsuarioDAO dao = new UsuarioDAO();
        return dao.buscarPorId(id);
    }

    /**
     * Cargar usuario por username
     */
    public static UsuarioDTO buscarPorUsername(String username) {
        UsuarioDAO dao = new UsuarioDAO();
        return dao.buscarPorUsername(username);
    }

    /**
     * Listar todos los usuarios
     */
    public static List<UsuarioDTO> listarTodos() {
        UsuarioDAO dao = new UsuarioDAO();
        return dao.listarTodos();
    }

    /**
     * Asignar roles
     */
    public boolean asignarRoles(List<Long> rolesIds) {
        UsuarioDAO dao = new UsuarioDAO();
        dao.eliminarRoles(this.id);
        for (Long rolId : rolesIds) {
            dao.asignarRol(this.id, rolId);
        }
        return true;
    }

    /**
     * Validaciones de duplicados
     */
    public static boolean existeUsername(String username) {
        return new UsuarioDAO().existeUsername(username);
    }

    public static boolean existeEmail(String email) {
        return new UsuarioDAO().existeEmail(email);
    }

    public static boolean existeDni(String dni) {
        return new UsuarioDAO().existeDni(dni);
    }
}
