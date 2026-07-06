package rmi.compras;

import rmi.dto.DetalleTransaccionDTO;
import rmi.dto.CatalogoProductoDTO;
import rmi.dto.ProductoDTO;
import rmi.dto.ProveedorDTO;
import rmi.dto.TransaccionDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface CompraServiceRMI extends Remote {

    String SERVICE_NAME = "CompraService";

    Long registrarCompra(TransaccionDTO compra, List<DetalleTransaccionDTO> detalles) throws RemoteException;

    TransaccionDTO buscarPorId(Long id) throws RemoteException;

    List<TransaccionDTO> listarCompras(int pagina, int porPagina) throws RemoteException;

    int contarCompras() throws RemoteException;

    boolean anular(Long id) throws RemoteException;

    String generarNumeroCompra() throws RemoteException;

    List<CatalogoProductoDTO> buscarProductosCatalogo(String termino, int limite) throws RemoteException;

    CatalogoProductoDTO buscarProductoCatalogoPorCodigo(String codigo) throws RemoteException;

    CatalogoProductoDTO buscarProductoCatalogoPorId(Long id) throws RemoteException;

    int obtenerStockTotalPorCatalogo(Long catalogoId) throws RemoteException;

    java.math.BigDecimal obtenerPrecioCompraPorCatalogo(Long catalogoId) throws RemoteException;

    List<ProductoDTO> listarLotesPorCatalogo(Long catalogoId) throws RemoteException;

    // Proveedores
    List<ProveedorDTO> listarProveedores() throws RemoteException;

    List<ProveedorDTO> listarProveedoresActivos() throws RemoteException;

    ProveedorDTO buscarProveedorPorId(Long id) throws RemoteException;

    Long insertarProveedor(ProveedorDTO proveedor) throws RemoteException;

    boolean actualizarProveedor(ProveedorDTO proveedor) throws RemoteException;

    boolean eliminarProveedor(Long id) throws RemoteException;

    boolean existeRuc(String ruc) throws RemoteException;

    List<ProveedorDTO> buscarProveedoresAutocomplete(String termino) throws RemoteException;
}
