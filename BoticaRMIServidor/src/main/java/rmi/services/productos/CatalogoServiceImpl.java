package rmi.services.productos;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import rmi.dao.CatalogoProductoDAO;
import rmi.dto.CatalogoProductoDTO;
import rmi.productos.CatalogoServiceRMI;

public class CatalogoServiceImpl extends UnicastRemoteObject implements CatalogoServiceRMI {

    private final CatalogoProductoDAO catalogoDAO;

    public CatalogoServiceImpl() throws RemoteException {
        this.catalogoDAO = new CatalogoProductoDAO();
    }

    @Override
    public CatalogoProductoDTO buscarPorId(Long id) throws RemoteException {
        return id == null ? null : catalogoDAO.buscarPorId(id);
    }

    @Override
    public CatalogoProductoDTO buscarPorCodigo(String codigo) throws RemoteException {
        return codigo == null ? null : catalogoDAO.buscarPorCodigo(codigo.trim());
    }

    @Override
    public List<CatalogoProductoDTO> buscarParaAutocomplete(String termino, int limite) throws RemoteException {
        int limiteSeguro = limite <= 0 || limite > 100 ? 20 : limite;
        return catalogoDAO.buscarParaAutocomplete(termino, limiteSeguro);
    }

    @Override
    public List<CatalogoProductoDTO> listarPaginado(int pagina, int porPagina) throws RemoteException {
        return catalogoDAO.listarPaginado(pagina <= 0 ? 1 : pagina, porPagina <= 0 || porPagina > 100 ? 20 : porPagina);
    }

    @Override
    public int contarTotal() throws RemoteException {
        return catalogoDAO.contarTotal();
    }

    @Override
    public int contarLaboratorios() throws RemoteException {
        return catalogoDAO.contarLaboratorios();
    }

    @Override
    public String obtenerUltimoAgregado() throws RemoteException {
        return catalogoDAO.obtenerUltimoAgregado();
    }

    @Override
    public Long insertar(CatalogoProductoDTO producto) throws RemoteException {
        validar(producto, false);
        return catalogoDAO.insertar(producto);
    }

    @Override
    public boolean actualizar(CatalogoProductoDTO producto) throws RemoteException {
        validar(producto, true);
        return catalogoDAO.actualizar(producto);
    }

    @Override
    public boolean desactivar(Long id) throws RemoteException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Id requerido");
        }
        return catalogoDAO.desactivar(id);
    }

    private void validar(CatalogoProductoDTO producto, boolean requiereId) {
        if (producto == null) {
            throw new IllegalArgumentException("Datos de producto requeridos");
        }
        if (requiereId && (producto.getId() == null || producto.getId() <= 0)) {
            throw new IllegalArgumentException("Id requerido");
        }
        if (producto.getNombreComercial() == null || producto.getNombreComercial().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre comercial es obligatorio");
        }
    }
}
