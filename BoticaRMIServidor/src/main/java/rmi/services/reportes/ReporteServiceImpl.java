package rmi.services.reportes;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
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
}
