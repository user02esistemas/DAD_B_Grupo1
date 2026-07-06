package rmi.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import rmi.auth.AuthServiceRMI;
import rmi.auth.UsuarioServiceRMI;
import rmi.compras.CompraServiceRMI;
import rmi.productos.CatalogoServiceRMI;
import rmi.productos.ProductoServiceRMI;
import rmi.reportes.DashboardServiceRMI;
import rmi.reportes.ReporteServiceRMI;
import rmi.services.auth.AuthServiceImpl;
import rmi.services.auth.UsuarioServiceImpl;
import rmi.services.compras.CompraServiceImpl;
import rmi.services.productos.CatalogoServiceImpl;
import rmi.services.productos.ProductoServiceImpl;
import rmi.services.reportes.DashboardServiceImpl;
import rmi.services.reportes.ReporteServiceImpl;
import rmi.services.ventas.VentaServiceImpl;
import rmi.ventas.VentaServiceRMI;

public class RMIServer {

    public static final int RMI_PORT = 1099;
    public static final String PRODUCTO_SERVICE = ProductoServiceRMI.SERVICE_NAME;
    public static final String CATALOGO_SERVICE = CatalogoServiceRMI.SERVICE_NAME;
    public static final String AUTH_SERVICE = AuthServiceRMI.SERVICE_NAME;
    public static final String DASHBOARD_SERVICE = DashboardServiceRMI.SERVICE_NAME;
    public static final String REPORTE_SERVICE = ReporteServiceRMI.SERVICE_NAME;
    public static final String VENTA_SERVICE = VentaServiceRMI.SERVICE_NAME;
    public static final String COMPRA_SERVICE = CompraServiceRMI.SERVICE_NAME;
    public static final String USUARIO_SERVICE = UsuarioServiceRMI.SERVICE_NAME;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            registry.rebind(AUTH_SERVICE, new AuthServiceImpl());
            registry.rebind(USUARIO_SERVICE, new UsuarioServiceImpl());
            registry.rebind(PRODUCTO_SERVICE, new ProductoServiceImpl());
            registry.rebind(CATALOGO_SERVICE, new CatalogoServiceImpl());
            registry.rebind(DASHBOARD_SERVICE, new DashboardServiceImpl());
            registry.rebind(REPORTE_SERVICE, new ReporteServiceImpl());
            registry.rebind(VENTA_SERVICE, new VentaServiceImpl());
            registry.rebind(COMPRA_SERVICE, new CompraServiceImpl());
            System.out.println("Servidor RMI iniciado en puerto " + RMI_PORT);
            System.out.println("Servicio publicado: " + AUTH_SERVICE);
            System.out.println("Servicio publicado: " + USUARIO_SERVICE);
            System.out.println("Servicio publicado: " + PRODUCTO_SERVICE);
            System.out.println("Servicio publicado: " + CATALOGO_SERVICE);
            System.out.println("Servicio publicado: " + DASHBOARD_SERVICE);
            System.out.println("Servicio publicado: " + REPORTE_SERVICE);
            System.out.println("Servicio publicado: " + VENTA_SERVICE);
            System.out.println("Servicio publicado: " + COMPRA_SERVICE);
        } catch (Exception ex) {
            System.err.println("No se pudo iniciar el servidor RMI: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
