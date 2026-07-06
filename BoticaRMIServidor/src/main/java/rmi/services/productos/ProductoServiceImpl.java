package rmi.services.productos;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rmi.dao.ProductoDAO;
import rmi.dto.ProductoDTO;
import rmi.dto.ProductoResumenDTO;
import rmi.productos.ProductoServiceRMI;

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

    @Override
    public ProductoResumenDTO buscarPorId(Long id) throws RemoteException {
        if (id == null || id <= 0) {
            return null;
        }
        return productoDAO.buscarPorId(id);
    }

    @Override
    public List<ProductoDTO> buscarInventario(String termino, String filtroStock, String filtroVencimiento, int pagina, int porPagina) throws RemoteException {
        int paginaSegura = pagina <= 0 ? 1 : pagina;
        int porPaginaSeguro = porPagina <= 0 || porPagina > 100 ? 15 : porPagina;
        return productoDAO.buscarInventario(termino, filtroStock, filtroVencimiento, paginaSegura, porPaginaSeguro);
    }

    @Override
    public int contarInventario(String termino, String filtroStock, String filtroVencimiento) throws RemoteException {
        return productoDAO.contarInventario(termino, filtroStock, filtroVencimiento);
    }

    @Override
    public Map<String, Integer> obtenerEstadisticasInventario() throws RemoteException {
        Map<String, Integer> estadisticas = new HashMap<>();
        estadisticas.put("totalProductos", productoDAO.contarTotal());
        estadisticas.put("stockBajo", productoDAO.contarStockBajo());
        estadisticas.put("agotados", productoDAO.contarAgotados());
        estadisticas.put("porVencer", productoDAO.contarPorVencer(30));
        return estadisticas;
    }
}
