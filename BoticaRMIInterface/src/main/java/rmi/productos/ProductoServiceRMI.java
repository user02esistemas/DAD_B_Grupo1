package rmi.productos;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import rmi.dto.ProductoResumenDTO;

public interface ProductoServiceRMI extends Remote {

    String SERVICE_NAME = "ProductoService";

    List<ProductoResumenDTO> buscarParaVenta(String termino, int limite) throws RemoteException;

    ProductoResumenDTO buscarPorId(Long id) throws RemoteException;
}
