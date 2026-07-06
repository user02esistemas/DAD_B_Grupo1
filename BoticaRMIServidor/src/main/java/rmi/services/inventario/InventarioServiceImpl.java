package rmi.services.inventario;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import rmi.dao.MovimientoInventarioDAO;
import rmi.dto.MovimientoInventarioDTO;
import rmi.inventario.InventarioServiceRMI;

public class InventarioServiceImpl extends UnicastRemoteObject implements InventarioServiceRMI {

    private final MovimientoInventarioDAO movimientoDAO;

    public InventarioServiceImpl() throws RemoteException {
        this.movimientoDAO = new MovimientoInventarioDAO();
    }

    @Override
    public boolean ajustarStock(Long productoId, int nuevoStock, String motivo, Long usuarioId) throws RemoteException {
        if (productoId == null || productoId <= 0 || usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("Datos de ajuste incompletos");
        }
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar un motivo");
        }
        return movimientoDAO.realizarAjuste(productoId, nuevoStock, motivo.trim(), usuarioId);
    }

    @Override
    public List<MovimientoInventarioDTO> listarMovimientos(String termino, int pagina, int porPagina) throws RemoteException {
        return movimientoDAO.listar(termino, pagina <= 0 ? 1 : pagina, porPagina <= 0 || porPagina > 100 ? 15 : porPagina);
    }

    @Override
    public int contarMovimientos(String termino) throws RemoteException {
        return movimientoDAO.contar(termino);
    }
}
