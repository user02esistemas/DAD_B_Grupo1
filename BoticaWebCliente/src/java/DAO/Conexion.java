/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase para manejar la conexión a la base de datos MySQL/MariaDB
 * Patrón Singleton para una única instancia de conexión
 */
public class Conexion {

    // Instancia única de conexión
    private static Connection conexion = null;

    // Configuración de la base de datos
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bd_BoticaEconoSalud";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";  // Cambia si tienes contraseña
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Constructor privado para evitar instanciación externa
     */
    private Conexion() {
    }

    /**
     * Obtiene la conexión a la base de datos
     * @return Connection objeto de conexión
     */
    public static Connection getConnection() {
        try {
            // Verificar si la conexión existe y está abierta
            if (conexion == null || conexion.isClosed()) {
                // Registrar el hook de shutdown para cerrar la conexión al finalizar
                Runtime.getRuntime().addShutdownHook(new CerrarConexionHook());

                // Cargar el driver de MySQL
                Class.forName(DB_DRIVER);

                // Establecer la conexión
                conexion = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                System.out.println("✓ Conexión establecida exitosamente a MySQL");
            }
            return conexion;

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("ERROR: No se encontró el driver MySQL JDBC. "
                    + "Asegúrate de tener mysql-connector-java.jar en WEB-INF/lib", e);
        } catch (SQLException e) {
            throw new RuntimeException("ERROR: No se pudo conectar a la base de datos."
                    + "Verifica que MySQL esté ejecutándose y los datos sean correctos.\n"
                    + "Detalles: " + e.getMessage(), e);
        }
    }

    /**
     * Cierra la conexión manualmente (opcional, normalmente se usa el hook)
     */
    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("✓ Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            System.err.println("ERROR al cerrar la conexión: " + e.getMessage());
        }
    }

    /**
     * Hook para cerrar la conexión automáticamente cuando se cierra la aplicación
     */
    static class CerrarConexionHook extends Thread {
        @Override
        public void run() {
            try {
                if (conexion != null && !conexion.isClosed()) {
                    conexion.close();
                    System.out.println("✓ Conexión cerrada automáticamente al finalizar la JVM");
                }
            } catch (SQLException e) {
                System.err.println("ERROR al cerrar conexión en shutdown: " + e.getMessage());
            }
        }
    }

    /**
     * Método para probar la conexión (útil para debugging)
     */
    public static boolean probarConexion() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            System.err.println("Fallo en la prueba de conexión: " + e.getMessage());
            return false;
        }
    }
}
