package rmi.services.auth;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import org.mindrot.jbcrypt.BCrypt;
import rmi.auth.AuthServiceRMI;
import rmi.dao.UsuarioDAO;
import rmi.dto.UsuarioDTO;

public class AuthServiceImpl extends UnicastRemoteObject implements AuthServiceRMI {

    private final UsuarioDAO usuarioDAO;

    public AuthServiceImpl() throws RemoteException {
        this.usuarioDAO = new UsuarioDAO();
    }

    @Override
    public UsuarioDTO login(String username, String password) throws RemoteException {
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            return null;
        }

        UsuarioDTO usuario = usuarioDAO.buscarPorUsername(username.trim());
        if (usuario == null || !usuario.isActivo()) {
            return null;
        }

        if (!BCrypt.checkpw(password, usuario.getPassword())) {
            return null;
        }

        usuario.setPassword(null);
        return usuario;
    }

    @Override
    public boolean logout(String username) throws RemoteException {
        return username != null && !username.trim().isEmpty();
    }

    @Override
    public boolean cambiarPassword(Long usuarioId, String passwordActual, String nuevaPassword) throws RemoteException {
        if (usuarioId == null || passwordActual == null || passwordActual.trim().isEmpty()
                || nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
            return false;
        }

        UsuarioDTO usuario = usuarioDAO.buscarPorIdConPassword(usuarioId);
        if (usuario == null || !usuario.isActivo() || usuario.getPassword() == null) {
            return false;
        }
        if (!BCrypt.checkpw(passwordActual, usuario.getPassword())) {
            return false;
        }
        String hash = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt());
        return usuarioDAO.actualizarPassword(usuarioId, hash);
    }

    @Override
    public boolean existeUsername(String username) throws RemoteException {
        return username != null && usuarioDAO.existeUsername(username.trim());
    }

    @Override
    public boolean existeEmail(String email) throws RemoteException {
        return email != null && usuarioDAO.existeEmail(email.trim());
    }
}
