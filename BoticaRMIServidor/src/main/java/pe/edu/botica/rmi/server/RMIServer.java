package pe.edu.botica.rmi.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import pe.edu.botica.rmi.services.productos.ProductoServiceImpl;

public class RMIServer {

    public static final int RMI_PORT = 1099;
    public static final String PRODUCTO_SERVICE = "ProductoService";

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            registry.rebind(PRODUCTO_SERVICE, new ProductoServiceImpl());
            System.out.println("Servidor RMI iniciado en puerto " + RMI_PORT);
            System.out.println("Servicio publicado: " + PRODUCTO_SERVICE);
        } catch (Exception ex) {
            System.err.println("No se pudo iniciar el servidor RMI: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
