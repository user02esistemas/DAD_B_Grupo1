# Contratos RMI

Los contratos RMI viven en `BoticaRMIInterface`.

Regla principal:

```text
BoticaRMIInterface no contiene logica de negocio ni acceso a base de datos.
```

## Paquetes

```text
rmi.auth
rmi.productos
rmi.ventas
rmi.compras
rmi.reportes
rmi.dto
rmi.exception
```

## Servicios

| Servicio | Responsabilidad |
|---|---|
| `AuthServiceRMI` | Login y validacion de usuario |
| `ProductoServiceRMI` | Busqueda y detalle de productos |
| `InventarioServiceRMI` | Stock, alertas y vencimientos |
| `VentaServiceRMI` | Registro y consulta de ventas |
| `CajaServiceRMI` | Apertura, cierre y resumen de caja |
| `CompraServiceRMI` | Registro y consulta de compras |
| `ProveedorServiceRMI` | Gestion de proveedores |
| `DashboardServiceRMI` | Indicadores principales |
| `ReporteServiceRMI` | Reportes operativos |

## Servicios Publicados Actualmente

```text
AuthService
UsuarioService
ProductoService
DashboardService
ReporteService
VentaService
CompraService
```

## ReporteServiceRMI

Metodos principales:

```text
reporteVentasPorFecha(fechaInicio, fechaFin)
reporteComprasPorFecha(fechaInicio, fechaFin)
obtenerResumenVentas(fechaInicio, fechaFin)
obtenerVentasPorDia(fechaInicio, fechaFin)
obtenerProductosMasVendidos(limite)
```

## Reglas Para Cambiar Contratos

1. No cambiar firmas usadas por otro modulo sin avisar al equipo.
2. Los DTO compartidos deben implementar `Serializable`.
3. Los metodos remotos deben declarar `throws RemoteException`.
4. Primero se actualiza `BoticaRMIInterface`, luego servidor, API y clientes.
