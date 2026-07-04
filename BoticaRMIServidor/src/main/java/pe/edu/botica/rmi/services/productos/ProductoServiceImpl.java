package pe.edu.botica.rmi.services.productos;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import pe.edu.botica.rmi.dao.ProductoDAO;
import pe.edu.botica.rmi.dto.ProductoResumenDTO;
import pe.edu.botica.rmi.productos.ProductoServiceRMI;

public class ProductoServiceImpl extends UnicastRemoteObject implements ProductoServiceRMI {

    private static final long serialVersionUID = 1L;
    private final ProductoDAO productoDAO;

    public ProductoServiceImpl() throws RemoteException {
        this.productoDAO = new ProductoDAO();
    }

    @Override
    public List<ProductoResumenDTO> buscarParaVenta(String termino, int limite) throws RemoteException {
        String terminoNormalizado = termino == null ? "" : termino.trim();
        int limiteSeguro = limite <= 0 || limite > 50 ? 15 : limite;
        return productoDAO.buscarParaVenta(terminoNormalizado, limiteSeguro);
    }
}
