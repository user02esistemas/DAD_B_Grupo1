package rmi.ventas;

import rmi.dto.DetalleTransaccionDTO;
import rmi.dto.SesionCajaDTO;
import rmi.dto.TransaccionDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface VentaServiceRMI extends Remote {

    String SERVICE_NAME = "VentaService";

    Long registrarVenta(TransaccionDTO venta, List<DetalleTransaccionDTO> detalles) throws RemoteException;

    TransaccionDTO buscarPorId(Long id) throws RemoteException;

    TransaccionDTO buscarPorNumero(String numero) throws RemoteException;

    List<TransaccionDTO> listarVentas(int pagina, int porPagina) throws RemoteException;

    List<TransaccionDTO> buscar(String termino, int pagina, int porPagina) throws RemoteException;

    int contarVentas() throws RemoteException;

    int contarVentas(String termino) throws RemoteException;

    boolean anular(Long id) throws RemoteException;

    String generarNumeroVenta() throws RemoteException;

    // Sesión de caja
    Long abrirSesionCaja(SesionCajaDTO sesion) throws RemoteException;

    boolean cerrarSesionCaja(SesionCajaDTO sesion) throws RemoteException;

    SesionCajaDTO buscarSesionAbierta(Long usuarioId) throws RemoteException;

    boolean tieneSesionAbierta(Long usuarioId) throws RemoteException;

    List<SesionCajaDTO> listarUltimasSesionesCaja(int limite) throws RemoteException;

    SesionCajaDTO obtenerResumenSesionCaja(Long sesionId) throws RemoteException;

    List<Map<String, Object>> listarCajasDisponibles() throws RemoteException;
}
