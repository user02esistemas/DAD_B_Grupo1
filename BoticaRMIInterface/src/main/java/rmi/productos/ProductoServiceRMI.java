package rmi.productos;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.List;
import rmi.dto.ProductoDTO;
import rmi.dto.ProductoResumenDTO;

public interface ProductoServiceRMI extends Remote {

    String SERVICE_NAME = "ProductoService";

    List<ProductoResumenDTO> buscarParaVenta(String termino, int limite) throws RemoteException;

    ProductoResumenDTO buscarPorId(Long id) throws RemoteException;

    List<ProductoDTO> buscarInventario(String termino, String filtroStock, String filtroVencimiento, int pagina, int porPagina) throws RemoteException;

    int contarInventario(String termino, String filtroStock, String filtroVencimiento) throws RemoteException;

    Map<String, Integer> obtenerEstadisticasInventario() throws RemoteException;
}
