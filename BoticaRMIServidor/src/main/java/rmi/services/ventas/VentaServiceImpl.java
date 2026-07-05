package rmi.services.ventas;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
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
        return ventaDAO.listarUltimas(porPagina);
    }

    @Override
    public List<TransaccionDTO> buscar(String termino, int pagina, int porPagina) throws RemoteException {
        return ventaDAO.listarUltimas(porPagina);
    }

    @Override
    public int contarVentas() throws RemoteException {
        throw new UnsupportedOperationException("Conteo de ventas pendiente");
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
        throw new UnsupportedOperationException("Sesion de caja pendiente");
    }

    @Override
    public boolean cerrarSesionCaja(SesionCajaDTO sesion) throws RemoteException {
        throw new UnsupportedOperationException("Sesion de caja pendiente");
    }

    @Override
    public SesionCajaDTO buscarSesionAbierta(Long usuarioId) throws RemoteException {
        throw new UnsupportedOperationException("Sesion de caja pendiente");
    }

    @Override
    public boolean tieneSesionAbierta(Long usuarioId) throws RemoteException {
        throw new UnsupportedOperationException("Sesion de caja pendiente");
    }
}
