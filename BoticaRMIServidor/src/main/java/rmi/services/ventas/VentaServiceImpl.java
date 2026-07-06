package rmi.services.ventas;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import rmi.dao.VentaDAO;
import rmi.dto.DetalleTransaccionDTO;
import rmi.dto.SesionCajaDTO;
import rmi.dto.TransaccionDTO;
import rmi.ventas.VentaServiceRMI;

public class VentaServiceImpl extends UnicastRemoteObject implements VentaServiceRMI {

    private final VentaDAO ventaDAO;

    public VentaServiceImpl() throws RemoteException {
        this.ventaDAO = new VentaDAO();
    }

    @Override
    public Long registrarVenta(TransaccionDTO venta, List<DetalleTransaccionDTO> detalles) throws RemoteException {
        return ventaDAO.registrarVenta(venta, detalles);
    }

    @Override
    public TransaccionDTO buscarPorId(Long id) throws RemoteException {
        return ventaDAO.buscarPorId(id);
    }

    @Override
    public TransaccionDTO buscarPorNumero(String numero) throws RemoteException {
        throw new UnsupportedOperationException("Busqueda por numero pendiente");
    }

    @Override
    public List<TransaccionDTO> listarVentas(int pagina, int porPagina) throws RemoteException {
        return ventaDAO.buscar(null, pagina, porPagina);
    }

    @Override
    public List<TransaccionDTO> buscar(String termino, int pagina, int porPagina) throws RemoteException {
        return ventaDAO.buscar(termino, pagina, porPagina);
    }

    @Override
    public int contarVentas() throws RemoteException {
        return ventaDAO.contarVentas(null);
    }

    @Override
    public int contarVentas(String termino) throws RemoteException {
        return ventaDAO.contarVentas(termino);
    }

    @Override
    public boolean anular(Long id) throws RemoteException {
        throw new UnsupportedOperationException("Anulacion pendiente");
    }

    @Override
    public String generarNumeroVenta() throws RemoteException {
        return ventaDAO.generarNumeroVenta();
    }

    @Override
    public Long abrirSesionCaja(SesionCajaDTO sesion) throws RemoteException {
        return ventaDAO.abrirSesionCaja(sesion);
    }

    @Override
    public boolean cerrarSesionCaja(SesionCajaDTO sesion) throws RemoteException {
        return ventaDAO.cerrarSesionCaja(sesion);
    }

    @Override
    public SesionCajaDTO buscarSesionAbierta(Long usuarioId) throws RemoteException {
        SesionCajaDTO sesion = ventaDAO.buscarSesionAbierta(usuarioId);
        return sesion == null ? null : ventaDAO.obtenerResumenSesion(sesion.getId());
    }

    @Override
    public boolean tieneSesionAbierta(Long usuarioId) throws RemoteException {
        return ventaDAO.tieneSesionAbierta(usuarioId);
    }

    @Override
    public List<SesionCajaDTO> listarUltimasSesionesCaja(int limite) throws RemoteException {
        return ventaDAO.listarUltimasSesionesCaja(limite);
    }

    @Override
    public SesionCajaDTO obtenerResumenSesionCaja(Long sesionId) throws RemoteException {
        return ventaDAO.obtenerResumenSesion(sesionId);
    }

    @Override
    public List<Map<String, Object>> listarCajasDisponibles() throws RemoteException {
        return ventaDAO.listarCajasDisponibles();
    }
}
