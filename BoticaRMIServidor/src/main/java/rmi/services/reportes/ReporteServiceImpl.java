package rmi.services.reportes;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import rmi.dao.ReporteDAO;
import rmi.dto.ProductoVendidoDTO;
import rmi.dto.ResumenVentasDTO;
import rmi.dto.TransaccionDTO;
import rmi.dto.VentaDiaDTO;
import rmi.reportes.ReporteServiceRMI;

public class ReporteServiceImpl extends UnicastRemoteObject implements ReporteServiceRMI {

    private final ReporteDAO reporteDAO;

    public ReporteServiceImpl() throws RemoteException {
        this.reporteDAO = new ReporteDAO();
    }

    @Override
    public List<TransaccionDTO> reporteVentasPorFecha(String fechaInicio, String fechaFin) throws RemoteException {
        return reporteDAO.reporteVentasPorFecha(fechaInicio, fechaFin);
    }

    @Override
    public List<TransaccionDTO> reporteComprasPorFecha(String fechaInicio, String fechaFin) throws RemoteException {
        return reporteDAO.reporteComprasPorFecha(fechaInicio, fechaFin);
    }

    @Override
    public ResumenVentasDTO obtenerResumenVentas(String fechaInicio, String fechaFin) throws RemoteException {
        return reporteDAO.obtenerResumenVentas(fechaInicio, fechaFin);
    }

    @Override
    public List<VentaDiaDTO> obtenerVentasPorDia(String fechaInicio, String fechaFin) throws RemoteException {
        return reporteDAO.obtenerVentasPorDia(fechaInicio, fechaFin);
    }

    @Override
    public List<ProductoVendidoDTO> obtenerProductosMasVendidos(int limite) throws RemoteException {
        return reporteDAO.obtenerProductosMasVendidos(limite);
    }

    @Override
    public List<Map<String, Object>> obtenerProductosPorVencer(int diasDesde, int diasHasta) throws RemoteException {
        return reporteDAO.obtenerProductosPorVencer(diasDesde, diasHasta);
    }

    @Override
    public List<Map<String, Object>> obtenerProductosPorVencerRango(String fechaDesde, String fechaHasta) throws RemoteException {
        return reporteDAO.obtenerProductosPorVencerRango(fechaDesde, fechaHasta);
    }

    @Override
    public Map<String, Integer> contarVencimientosPorCriticidad() throws RemoteException {
        return reporteDAO.contarVencimientosPorCriticidad();
    }

    @Override
    public List<Map<String, Object>> listarSesionesCaja(String fechaDesde, String fechaHasta, Long usuarioId) throws RemoteException {
        return reporteDAO.listarSesionesCaja(fechaDesde, fechaHasta, usuarioId);
    }

    @Override
    public Map<String, Object> obtenerDetalleSesionCaja(Long sesionId) throws RemoteException {
        return reporteDAO.obtenerDetalleSesionCaja(sesionId);
    }

    @Override
    public List<Map<String, Object>> obtenerVentasSesionCaja(Long sesionId) throws RemoteException {
        return reporteDAO.obtenerVentasSesionCaja(sesionId);
    }
}
