# Arquitectura Del Sistema

EconoSalud se organiza como un sistema distribuido con dos interfaces: web y movil.

## Flujo Web

```text
BoticaWebCliente -> BoticaRMIInterface -> BoticaRMIServidor -> MySQL/MariaDB
```

## Flujo Movil

```text
BoticaMobileFlutter -> BoticaAPIREST -> BoticaRMIInterface -> BoticaRMIServidor -> MySQL/MariaDB
```

La aplicacion movil consume unicamente JSON por HTTP. No debe abrir conexiones JDBC, no debe usar RMI directo y no debe conocer la estructura interna de MySQL.

Internamente, `BoticaMobileFlutter` usa MVVM por feature:

```text
view -> viewmodel -> service -> core/network/api_client.dart -> BoticaAPIREST
model <- parseo JSON desde service
```

Los servicios moviles solo llaman endpoints REST. Ningun `service` de Flutter invoca RMI ni consulta MySQL.

Modulos moviles iniciales:

| Modulo Flutter | Endpoint REST | Servicio RMI usado por API |
|---|---|---|
| Auth | `/api/auth/login` | `AuthService` |
| Dashboard | `/api/dashboard/resumen` | `DashboardService` |
| Productos | `/api/productos` | `ProductoService` |

Modulos moviles pendientes y su integracion esperada:

| Modulo Flutter | Endpoint REST esperado | Servicio RMI |
|---|---|---|
| Ventas | `/api/ventas`, `/api/ventas/ultimas` | `VentaService` |
| Compras | `/api/compras`, `/api/compras/proveedores` | `CompraService` |
| Caja | Pendiente de endpoint API | `VentaService` / sesiones de caja |
| Usuarios | `/api/usuarios` | `UsuarioService` |
| Reportes | `/api/reportes/ventas`, `/api/reportes/productos-mas-vendidos` | `ReporteService` |

Los roles de `usuarios.roles` deben controlar las opciones visibles en Flutter y las validaciones reales en API/RMI.

## Componentes

```text
BoticaWebCliente      Cliente web JSP/Servlet.
BoticaMobileFlutter   Aplicacion movil Flutter.
BoticaAPIREST         API HTTP/JSON para Flutter y Postman.
BoticaRMIInterface    Contratos RMI compartidos.
BoticaRMIServidor     Servidor de servicios remotos y logica de negocio.
Database              Scripts SQL.
Postman               Colecciones de prueba de API.
Library               Librerias del cliente web Ant.
```
