package pe.edu.botica.rmi.productos;

import pe.edu.botica.rmi.dto.ProductoDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ProductoServiceRMI extends Remote {

    String SERVICE_NAME = "ProductoService";

    List<ProductoDTO> listarTodos() throws RemoteException;

    List<ProductoDTO> listarTodos(int pagina, int porPagina) throws RemoteException;

    ProductoDTO buscarPorId(Long id) throws RemoteException;

    List<ProductoDTO> buscarConFiltros(String termino, String estado,
            String filtroStock, String filtroVencimiento,
            int pagina, int porPagina) throws RemoteException;

    int contarConFiltros(String termino, String estado,
            String filtroStock, String filtroVencimiento) throws RemoteException;

    Long insertar(ProductoDTO producto) throws RemoteException;

    boolean actualizar(ProductoDTO producto) throws RemoteException;

    boolean eliminar(Long id) throws RemoteException;

    List<ProductoDTO> listarStockBajo(int limite) throws RemoteException;

    List<ProductoDTO> listarPorVencer(int diasLimite, int limite) throws RemoteException;

    List<ProductoDTO> listarVencidos(int limite) throws RemoteException;

    int contarTotal() throws RemoteException;

    int contarStockBajo() throws RemoteException;

    int contarAgotados() throws RemoteException;

    int contarPorVencer(int dias) throws RemoteException;

    int contarVencidos() throws RemoteException;

    List<ProductoDTO> buscarParaVenta(String termino) throws RemoteException;
}
