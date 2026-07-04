package pe.edu.botica.rmi.reportes;

import pe.edu.botica.rmi.dto.TransaccionDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ReporteServiceRMI extends Remote {

    String SERVICE_NAME = "ReporteService";

    List<TransaccionDTO> reporteVentasPorFecha(String fechaInicio, String fechaFin) throws RemoteException;

    List<TransaccionDTO> reporteComprasPorFecha(String fechaInicio, String fechaFin) throws RemoteException;
}
