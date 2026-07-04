package pe.edu.botica.rmi.reportes;

import pe.edu.botica.rmi.dto.DashboardResumenDTO;
import pe.edu.botica.rmi.dto.ProductoAlertaDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DashboardServiceRMI extends Remote {

    String SERVICE_NAME = "DashboardService";

    DashboardResumenDTO obtenerResumenCompleto() throws RemoteException;

    List<ProductoAlertaDTO> obtenerProductosStockBajo(int limite) throws RemoteException;

    List<ProductoAlertaDTO> obtenerProductosPorVencer(int limite) throws RemoteException;
}
