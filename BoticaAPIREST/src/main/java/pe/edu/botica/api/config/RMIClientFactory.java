package pe.edu.botica.api.config;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import pe.edu.botica.rmi.productos.ProductoServiceRMI;

public final class RMIClientFactory {

    private static final String RMI_HOST = "localhost";
    private static final int RMI_PORT = 1099;
    private static final String PRODUCTO_SERVICE = "ProductoService";

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
}
