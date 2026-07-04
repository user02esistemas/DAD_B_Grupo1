package pe.edu.botica.rmi.auth;

import pe.edu.botica.rmi.dto.RolDTO;
import pe.edu.botica.rmi.dto.UsuarioDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface UsuarioServiceRMI extends Remote {

    String SERVICE_NAME = "UsuarioService";

    List<UsuarioDTO> listarTodos() throws RemoteException;

    UsuarioDTO buscarPorId(Long id) throws RemoteException;

    UsuarioDTO buscarPorUsername(String username) throws RemoteException;

    Long insertar(UsuarioDTO usuario) throws RemoteException;

    boolean actualizar(UsuarioDTO usuario) throws RemoteException;

    boolean eliminar(Long id) throws RemoteException;

    boolean desactivar(Long id) throws RemoteException;

    boolean actualizarPassword(Long usuarioId, String nuevaPasswordHash) throws RemoteException;

    boolean asignarRoles(Long usuarioId, List<Long> rolesIds) throws RemoteException;

    boolean existeUsername(String username) throws RemoteException;

    boolean existeEmail(String email) throws RemoteException;

    boolean existeDni(String dni) throws RemoteException;

    List<RolDTO> listarRoles() throws RemoteException;
}
