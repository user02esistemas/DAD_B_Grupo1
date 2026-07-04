# Contratos RMI

Los contratos RMI viven en `BoticaRMIInterface`.

Regla principal:

```text
BoticaRMIInterface no contiene logica de negocio ni acceso a base de datos.
```

## Paquetes Propuestos

```text
pe.edu.botica.rmi.auth
pe.edu.botica.rmi.productos
pe.edu.botica.rmi.ventas
pe.edu.botica.rmi.compras
pe.edu.botica.rmi.reportes
pe.edu.botica.rmi.dto
pe.edu.botica.rmi.exception
```

## Servicios Propuestos

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

## Reglas Para Cambiar Contratos

1. No cambiar firmas usadas por otro modulo sin avisar al equipo.
2. Los DTO compartidos deben implementar `Serializable`.
3. Los metodos remotos deben declarar `throws RemoteException`.
4. Primero se actualiza `BoticaRMIInterface`, luego servidor, API y clientes.
