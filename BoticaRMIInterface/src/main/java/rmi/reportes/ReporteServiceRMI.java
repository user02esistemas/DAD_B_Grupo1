package rmi.reportes;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
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

    List<Map<String, Object>> obtenerProductosPorVencer(int diasDesde, int diasHasta) throws RemoteException;

    List<Map<String, Object>> obtenerProductosPorVencerRango(String fechaDesde, String fechaHasta) throws RemoteException;

    Map<String, Integer> contarVencimientosPorCriticidad() throws RemoteException;

    List<Map<String, Object>> listarSesionesCaja(String fechaDesde, String fechaHasta, Long usuarioId) throws RemoteException;

    Map<String, Object> obtenerDetalleSesionCaja(Long sesionId) throws RemoteException;

    List<Map<String, Object>> obtenerVentasSesionCaja(Long sesionId) throws RemoteException;
}
