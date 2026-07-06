package api.config;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import rmi.auth.AuthServiceRMI;
import rmi.auth.UsuarioServiceRMI;
import rmi.compras.CompraServiceRMI;
import rmi.inventario.InventarioServiceRMI;
import rmi.productos.CatalogoServiceRMI;
import rmi.productos.ProductoServiceRMI;
import rmi.reportes.DashboardServiceRMI;
import rmi.reportes.ReporteServiceRMI;
import rmi.ventas.VentaServiceRMI;

public final class RMIClientFactory {

    private static final String RMI_HOST = "localhost";
    private static final int RMI_PORT = 1099;
    private static final String PRODUCTO_SERVICE = ProductoServiceRMI.SERVICE_NAME;
    private static final String CATALOGO_SERVICE = CatalogoServiceRMI.SERVICE_NAME;
    private static final String AUTH_SERVICE = AuthServiceRMI.SERVICE_NAME;
    private static final String USUARIO_SERVICE = UsuarioServiceRMI.SERVICE_NAME;
    private static final String DASHBOARD_SERVICE = DashboardServiceRMI.SERVICE_NAME;
    private static final String REPORTE_SERVICE = ReporteServiceRMI.SERVICE_NAME;
    private static final String VENTA_SERVICE = VentaServiceRMI.SERVICE_NAME;
    private static final String COMPRA_SERVICE = CompraServiceRMI.SERVICE_NAME;
    private static final String INVENTARIO_SERVICE = InventarioServiceRMI.SERVICE_NAME;

    private RMIClientFactory() {
    }

    public static ProductoServiceRMI getProductoService() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            return (ProductoServiceRMI) registry.lookup(PRODUCTO_SERVICE);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo conectar con ProductoService RMI", ex);
        }
    }

    public static CatalogoServiceRMI getCatalogoService() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            return (CatalogoServiceRMI) registry.lookup(CATALOGO_SERVICE);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo conectar con CatalogoService RMI", ex);
        }
    }

    public static AuthServiceRMI getAuthService() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            return (AuthServiceRMI) registry.lookup(AUTH_SERVICE);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo conectar con AuthService RMI", ex);
        }
    }

    public static UsuarioServiceRMI getUsuarioService() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            return (UsuarioServiceRMI) registry.lookup(USUARIO_SERVICE);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo conectar con UsuarioService RMI", ex);
        }
    }

    public static DashboardServiceRMI getDashboardService() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            return (DashboardServiceRMI) registry.lookup(DASHBOARD_SERVICE);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo conectar con DashboardService RMI", ex);
        }
    }

    public static ReporteServiceRMI getReporteService() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            return (ReporteServiceRMI) registry.lookup(REPORTE_SERVICE);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo conectar con ReporteService RMI", ex);
        }
    }

    public static VentaServiceRMI getVentaService() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            return (VentaServiceRMI) registry.lookup(VENTA_SERVICE);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo conectar con VentaService RMI", ex);
        }
    }

    public static CompraServiceRMI getCompraService() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            return (CompraServiceRMI) registry.lookup(COMPRA_SERVICE);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo conectar con CompraService RMI", ex);
        }
    }

    public static InventarioServiceRMI getInventarioService() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            return (InventarioServiceRMI) registry.lookup(INVENTARIO_SERVICE);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo conectar con InventarioService RMI", ex);
        }
    }
}
