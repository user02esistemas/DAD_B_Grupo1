package rmi.services.compras;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import rmi.compras.CompraServiceRMI;
import rmi.dao.CompraDAO;
import rmi.dto.DetalleTransaccionDTO;
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
        return compraDAO.listarCompras(porPagina);
    }

    @Override
    public int contarCompras() throws RemoteException {
        throw new UnsupportedOperationException("Conteo de compras pendiente");
    }

    @Override
    public boolean anular(Long id) throws RemoteException {
        throw new UnsupportedOperationException("Anulacion pendiente");
    }

    @Override
    public String generarNumeroCompra() throws RemoteException {
        return compraDAO.generarNumeroCompra();
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
        throw new UnsupportedOperationException("Registro de proveedor pendiente");
    }

    @Override
    public boolean actualizarProveedor(ProveedorDTO proveedor) throws RemoteException {
        throw new UnsupportedOperationException("Actualizacion de proveedor pendiente");
    }

    @Override
    public boolean eliminarProveedor(Long id) throws RemoteException {
        throw new UnsupportedOperationException("Eliminacion de proveedor pendiente");
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
