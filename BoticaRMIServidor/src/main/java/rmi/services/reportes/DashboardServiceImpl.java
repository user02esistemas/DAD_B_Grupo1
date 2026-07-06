package rmi.services.reportes;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import rmi.dao.DashboardDAO;
import rmi.dto.DashboardResumenDTO;
import rmi.dto.ProductoAlertaDTO;
import rmi.reportes.DashboardServiceRMI;

public class DashboardServiceImpl extends UnicastRemoteObject implements DashboardServiceRMI {

    private final DashboardDAO dashboardDAO;

    public DashboardServiceImpl() throws RemoteException {
        this.dashboardDAO = new DashboardDAO();
    }

    @Override
    public DashboardResumenDTO obtenerResumenCompleto() throws RemoteException {
        return dashboardDAO.obtenerResumenCompleto();
    }

    @Override
    public List<ProductoAlertaDTO> obtenerProductosStockBajo(int limite) throws RemoteException {
        return dashboardDAO.obtenerProductosStockBajo(limite);
    }

    @Override
    public List<ProductoAlertaDTO> obtenerProductosPorVencer(int limite) throws RemoteException {
        return dashboardDAO.obtenerProductosPorVencer(limite);
    }

    @Override
    public Map<String, Object> obtenerVentasTurno(Long sesionCajaId) throws RemoteException {
        return dashboardDAO.obtenerVentasTurno(sesionCajaId);
    }

    @Override
    public Map<String, Object> obtenerVentasDelDiaUsuario(Long usuarioId) throws RemoteException {
        return dashboardDAO.obtenerVentasDelDiaUsuario(usuarioId);
    }

    @Override
    public List<Map<String, Object>> obtenerUltimasVentasUsuario(Long usuarioId, int limite) throws RemoteException {
        return dashboardDAO.obtenerUltimasVentasUsuario(usuarioId, limite);
    }
}
