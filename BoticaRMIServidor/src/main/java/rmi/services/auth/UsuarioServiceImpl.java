package rmi.services.auth;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
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
        throw new UnsupportedOperationException("Registro de usuario pendiente");
    }

    @Override
    public boolean actualizar(UsuarioDTO usuario) throws RemoteException {
        throw new UnsupportedOperationException("Actualizacion de usuario pendiente");
    }

    @Override
    public boolean eliminar(Long id) throws RemoteException {
        throw new UnsupportedOperationException("Eliminacion de usuario pendiente");
    }

    @Override
    public boolean desactivar(Long id) throws RemoteException {
        throw new UnsupportedOperationException("Desactivacion de usuario pendiente");
    }

    @Override
    public boolean actualizarPassword(Long usuarioId, String nuevaPasswordHash) throws RemoteException {
        throw new UnsupportedOperationException("Cambio de password pendiente");
    }

    @Override
    public boolean asignarRoles(Long usuarioId, List<Long> rolesIds) throws RemoteException {
        throw new UnsupportedOperationException("Asignacion de roles pendiente");
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
}
