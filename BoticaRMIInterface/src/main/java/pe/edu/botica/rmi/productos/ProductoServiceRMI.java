package pe.edu.botica.rmi.productos;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import pe.edu.botica.rmi.dto.ProductoResumenDTO;

public interface ProductoServiceRMI extends Remote {

    List<ProductoResumenDTO> buscarParaVenta(String termino, int limite) throws RemoteException;
}
