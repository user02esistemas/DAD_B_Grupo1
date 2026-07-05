package rmi.auth;

import rmi.dto.UsuarioDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthServiceRMI extends Remote {

    String SERVICE_NAME = "AuthService";

    UsuarioDTO login(String username, String password) throws RemoteException;

    boolean logout(String username) throws RemoteException;

    boolean cambiarPassword(Long usuarioId, String passwordActual, String nuevaPassword) throws RemoteException;

    boolean existeUsername(String username) throws RemoteException;

    boolean existeEmail(String email) throws RemoteException;
}
