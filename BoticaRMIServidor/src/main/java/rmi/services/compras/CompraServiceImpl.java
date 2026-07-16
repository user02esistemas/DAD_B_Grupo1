package rmi.services.compras;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.math.BigDecimal;
import java.util.List;
import rmi.compras.CompraServiceRMI;
import rmi.dao.CompraDAO;
import rmi.dto.CatalogoProductoDTO;
import rmi.dto.DetalleTransaccionDTO;
import rmi.dto.ProductoDTO;
import rmi.dto.ProveedorDTO;
import rmi.dto.TransaccionDTO;

public class CompraServiceImpl extends UnicastRemoteObject implements CompraServiceRMI {

    private final CompraDAO compraDAO;

    public CompraServiceImpl() throws RemoteException {
        this.compraDAO = new CompraDAO();
    }

    @Override
    public Long registrarCompra(TransaccionDTO compra, List<DetalleTransaccionDTO> detalles) throws RemoteException {
        return compraDAO.registrarCompra(compra, detalles);
    }

    @Override
    public TransaccionDTO buscarPorId(Long id) throws RemoteException {
        return compraDAO.buscarPorId(id);
    }

    @Override
    public List<TransaccionDTO> listarCompras(int pagina, int porPagina) throws RemoteException {
        return compraDAO.listarCompras(pagina, porPagina);
    }

    @Override
    public int contarCompras() throws RemoteException {
        return compraDAO.contarCompras();
    }

    @Override
    public boolean anular(Long id) throws RemoteException {
        return compraDAO.anular(id);
    }

    @Override
    public String generarNumeroCompra() throws RemoteException {
        return compraDAO.generarNumeroCompra();
    }

    @Override
    public List<CatalogoProductoDTO> buscarProductosCatalogo(String termino, int limite) throws RemoteException {
        return compraDAO.buscarProductosCatalogo(termino, limite);
    }

    @Override
    public CatalogoProductoDTO buscarProductoCatalogoPorCodigo(String codigo) throws RemoteException {
        return compraDAO.buscarProductoCatalogoPorCodigo(codigo);
    }

    @Override
    public CatalogoProductoDTO buscarProductoCatalogoPorId(Long id) throws RemoteException {
        return compraDAO.buscarProductoCatalogoPorId(id);
    }

    @Override
    public int obtenerStockTotalPorCatalogo(Long catalogoId) throws RemoteException {
        return compraDAO.obtenerStockTotalPorCatalogo(catalogoId);
    }

    @Override
    public BigDecimal obtenerPrecioCompraPorCatalogo(Long catalogoId) throws RemoteException {
        return compraDAO.obtenerPrecioCompraPorCatalogo(catalogoId);
    }

    @Override
    public List<ProductoDTO> listarLotesPorCatalogo(Long catalogoId) throws RemoteException {
        return compraDAO.listarLotesPorCatalogo(catalogoId);
    }

    @Override
    public List<ProveedorDTO> listarProveedores() throws RemoteException {
        return compraDAO.listarProveedores();
    }

    @Override
    public List<ProveedorDTO> listarProveedoresActivos() throws RemoteException {
        return compraDAO.listarProveedoresActivos();
    }

    @Override
    public ProveedorDTO buscarProveedorPorId(Long id) throws RemoteException {
        return compraDAO.buscarProveedorPorId(id);
    }

    @Override
    public Long insertarProveedor(ProveedorDTO proveedor) throws RemoteException {
        return compraDAO.insertarProveedor(proveedor);
    }

    @Override
    public boolean actualizarProveedor(ProveedorDTO proveedor) throws RemoteException {
        return compraDAO.actualizarProveedor(proveedor);
    }

    @Override
    public boolean eliminarProveedor(Long id) throws RemoteException {
        return compraDAO.eliminarProveedor(id);
    }

    @Override
    public boolean existeRuc(String ruc) throws RemoteException {
        return compraDAO.existeRuc(ruc);
    }

    @Override
    public List<ProveedorDTO> buscarProveedoresAutocomplete(String termino) throws RemoteException {
        return compraDAO.buscarProveedoresAutocomplete(termino);
    }
}
