package rmi.inventario;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import rmi.dto.MovimientoInventarioDTO;

public interface InventarioServiceRMI extends Remote {

    String SERVICE_NAME = "InventarioService";

    boolean ajustarStock(Long productoId, int nuevoStock, String motivo, Long usuarioId) throws RemoteException;

    List<MovimientoInventarioDTO> listarMovimientos(String termino, int pagina, int porPagina) throws RemoteException;

    int contarMovimientos(String termino) throws RemoteException;
}
