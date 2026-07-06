package rmi.productos;

import rmi.dto.CatalogoProductoDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface CatalogoServiceRMI extends Remote {

    String SERVICE_NAME = "CatalogoService";

    CatalogoProductoDTO buscarPorId(Long id) throws RemoteException;

    CatalogoProductoDTO buscarPorCodigo(String codigo) throws RemoteException;

    List<CatalogoProductoDTO> buscarParaAutocomplete(String termino, int limite) throws RemoteException;

    List<CatalogoProductoDTO> listarPaginado(int pagina, int porPagina) throws RemoteException;

    int contarTotal() throws RemoteException;

    int contarLaboratorios() throws RemoteException;

    String obtenerUltimoAgregado() throws RemoteException;

    Long insertar(CatalogoProductoDTO producto) throws RemoteException;

    boolean actualizar(CatalogoProductoDTO producto) throws RemoteException;

    boolean desactivar(Long id) throws RemoteException;
}
