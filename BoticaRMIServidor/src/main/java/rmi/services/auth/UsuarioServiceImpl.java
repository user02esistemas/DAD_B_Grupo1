package rmi.services.auth;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import rmi.auth.UsuarioServiceRMI;
import rmi.dao.UsuarioDAO;
import rmi.dto.RolDTO;
import rmi.dto.UsuarioDTO;

public class UsuarioServiceImpl extends UnicastRemoteObject implements UsuarioServiceRMI {

    private final UsuarioDAO usuarioDAO;

    public UsuarioServiceImpl() throws RemoteException {
        this.usuarioDAO = new UsuarioDAO();
    }

    @Override
    public List<UsuarioDTO> listarTodos() throws RemoteException {
        return usuarioDAO.listarTodos();
    }

    @Override
    public UsuarioDTO buscarPorId(Long id) throws RemoteException {
        return id == null ? null : usuarioDAO.buscarPorId(id);
    }

    @Override
    public UsuarioDTO buscarPorUsername(String username) throws RemoteException {
        UsuarioDTO usuario = usuarioDAO.buscarPorUsername(username);
        if (usuario != null) {
            usuario.setPassword(null);
        }
        return usuario;
    }

    @Override
    public Long insertar(UsuarioDTO usuario) throws RemoteException {
        validarUsuario(usuario, true);
        if (usuarioDAO.existeUsername(usuario.getUsername().trim())) {
            throw new IllegalArgumentException("El usuario ya existe");
        }
        if (usuarioDAO.existeEmail(usuario.getEmail().trim())) {
            throw new IllegalArgumentException("El email ya existe");
        }
        if (usuarioDAO.existeDni(usuario.getDni().trim())) {
            throw new IllegalArgumentException("El DNI ya existe");
        }
        usuario.setUsername(usuario.getUsername().trim());
        usuario.setEmail(usuario.getEmail().trim());
        usuario.setDni(usuario.getDni().trim());
        usuario.setNombreCompleto(usuario.getNombreCompleto().trim());
        usuario.setPassword(BCrypt.hashpw(usuario.getPassword(), BCrypt.gensalt()));
        Long id = usuarioDAO.insertar(usuario);
        return id;
    }

    @Override
    public boolean actualizar(UsuarioDTO usuario) throws RemoteException {
        validarUsuario(usuario, false);
        if (usuario.getId() == null || usuario.getId() <= 0) {
            throw new IllegalArgumentException("Id de usuario requerido");
        }
        usuario.setUsername(usuario.getUsername().trim());
        usuario.setEmail(usuario.getEmail().trim());
        usuario.setDni(usuario.getDni().trim());
        usuario.setNombreCompleto(usuario.getNombreCompleto().trim());
        return usuarioDAO.actualizar(usuario);
    }

    @Override
    public boolean eliminar(Long id) throws RemoteException {
        return desactivar(id);
    }

    @Override
    public boolean desactivar(Long id) throws RemoteException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Id de usuario requerido");
        }
        return usuarioDAO.desactivar(id);
    }

    @Override
    public boolean actualizarPassword(Long usuarioId, String nuevaPasswordHash) throws RemoteException {
        if (usuarioId == null || usuarioId <= 0 || nuevaPasswordHash == null || nuevaPasswordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Datos de password incompletos");
        }
        String hash = nuevaPasswordHash.startsWith("$2") ? nuevaPasswordHash : BCrypt.hashpw(nuevaPasswordHash, BCrypt.gensalt());
        return usuarioDAO.actualizarPassword(usuarioId, hash);
    }

    @Override
    public boolean asignarRoles(Long usuarioId, List<Long> rolesIds) throws RemoteException {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("Id de usuario requerido");
        }
        if (rolesIds == null || rolesIds.isEmpty()) {
            throw new IllegalArgumentException("Seleccione al menos un rol");
        }
        return usuarioDAO.asignarRoles(usuarioId, rolesIds);
    }

    @Override
    public boolean existeUsername(String username) throws RemoteException {
        return username != null && usuarioDAO.existeUsername(username.trim());
    }

    @Override
    public boolean existeEmail(String email) throws RemoteException {
        return email != null && usuarioDAO.existeEmail(email.trim());
    }

    @Override
    public boolean existeDni(String dni) throws RemoteException {
        return dni != null && usuarioDAO.existeDni(dni.trim());
    }

    @Override
    public List<RolDTO> listarRoles() throws RemoteException {
        return usuarioDAO.listarRoles();
    }

    private void validarUsuario(UsuarioDTO usuario, boolean requierePassword) {
        if (usuario == null) {
            throw new IllegalArgumentException("Datos de usuario requeridos");
        }
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username requerido");
        }
        if (requierePassword && (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty())) {
            throw new IllegalArgumentException("Password requerido");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email requerido");
        }
        if (usuario.getNombreCompleto() == null || usuario.getNombreCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre completo requerido");
        }
        if (usuario.getDni() == null || usuario.getDni().trim().isEmpty()) {
            throw new IllegalArgumentException("DNI requerido");
        }
    }
}
