package rmi.reportes;

import rmi.dto.DashboardResumenDTO;
import rmi.dto.ProductoAlertaDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface DashboardServiceRMI extends Remote {

    String SERVICE_NAME = "DashboardService";

    DashboardResumenDTO obtenerResumenCompleto() throws RemoteException;

    List<ProductoAlertaDTO> obtenerProductosStockBajo(int limite) throws RemoteException;

    List<ProductoAlertaDTO> obtenerProductosPorVencer(int limite) throws RemoteException;

    Map<String, Object> obtenerVentasTurno(Long sesionCajaId) throws RemoteException;

    Map<String, Object> obtenerVentasDelDiaUsuario(Long usuarioId) throws RemoteException;

    List<Map<String, Object>> obtenerUltimasVentasUsuario(Long usuarioId, int limite) throws RemoteException;
}
