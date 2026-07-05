package rmi.reportes;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import rmi.dto.ProductoVendidoDTO;
import rmi.dto.ResumenVentasDTO;
import rmi.dto.TransaccionDTO;
import rmi.dto.VentaDiaDTO;

public interface ReporteServiceRMI extends Remote {

    String SERVICE_NAME = "ReporteService";

    List<TransaccionDTO> reporteVentasPorFecha(String fechaInicio, String fechaFin) throws RemoteException;

    List<TransaccionDTO> reporteComprasPorFecha(String fechaInicio, String fechaFin) throws RemoteException;

    ResumenVentasDTO obtenerResumenVentas(String fechaInicio, String fechaFin) throws RemoteException;

    List<VentaDiaDTO> obtenerVentasPorDia(String fechaInicio, String fechaFin) throws RemoteException;

    List<ProductoVendidoDTO> obtenerProductosMasVendidos(int limite) throws RemoteException;
}
