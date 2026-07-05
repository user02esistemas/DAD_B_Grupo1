# API REST Endpoints

La API REST sera consumida por Flutter y probada con Postman.

Regla de arquitectura:

```text
Flutter/Postman -> BoticaAPIREST -> BoticaRMIInterface -> BoticaRMIServidor -> MySQL
```

La API no debe acceder directamente a la base de datos.

## Endpoints Implementados

| Modulo | Metodo | Ruta | Descripcion |
|---|---|---|---|
| Auth | POST | `/api/auth/login` | Autentica usuario movil |
| Auth | POST | `/api/auth/logout` | Cierra sesion logica |
| Productos | GET | `/api/productos` | Lista o busca productos |
| Productos | GET | `/api/productos/{id}` | Obtiene detalle de producto |
| Ventas | POST | `/api/ventas` | Registra venta simple |
| Ventas | GET | `/api/ventas/ultimas` | Lista ultimas ventas |
| Compras | POST | `/api/compras` | Registra compra simple |
| Compras | GET | `/api/compras/proveedores` | Lista proveedores activos |
| Dashboard | GET | `/api/dashboard/resumen` | Muestra resumen movil |
| Dashboard | GET | `/api/dashboard/productos-alerta` | Lista productos con alerta |
| Reportes | GET | `/api/reportes/ventas` | Consulta ventas por rango |
| Reportes | GET | `/api/reportes/productos-mas-vendidos` | Lista productos con mayor venta |
| Usuarios | GET | `/api/usuarios` | Lista usuarios activos |
| Usuarios | GET | `/api/usuarios/{id}` | Obtiene detalle de usuario |

## Respuesta Base

```json
{
  "success": true,
  "message": "Operacion exitosa",
  "data": {}
}
```

## Error Base

```json
{
  "success": false,
  "message": "Descripcion del error"
}
```

## Prueba En Postman

Antes de conectar Flutter, cada endpoint debe probarse en Postman y guardarse en `Postman/`.

Endpoints implementados:

```text
POST {{base_url}}/api/auth/login
```

```text
POST {{base_url}}/api/auth/logout
```

```text
GET {{base_url}}/api/productos?termino=para&limite=10
```

```text
GET {{base_url}}/api/productos/14
```

```text
GET {{base_url}}/api/dashboard/resumen
```

```text
GET {{base_url}}/api/dashboard/productos-alerta?limite=10
```

```text
POST {{base_url}}/api/ventas
```

```text
GET {{base_url}}/api/ventas/ultimas?limite=10
```

```text
POST {{base_url}}/api/compras
```

```text
GET {{base_url}}/api/compras/proveedores
```

```text
GET {{base_url}}/api/usuarios
```

```text
GET {{base_url}}/api/usuarios/1
```

```text
GET {{base_url}}/api/reportes/ventas?desde=2026-01-01&hasta=2026-12-31
```

```text
GET {{base_url}}/api/reportes/productos-mas-vendidos?limite=10
```

## Reporte De Ventas

Parametros requeridos:

```text
desde=yyyy-MM-dd
hasta=yyyy-MM-dd
```

Respuesta `data`:

```json
{
  "desde": "2026-01-01",
  "hasta": "2026-12-31",
  "resumen": {},
  "ventasPorDia": [],
  "ventas": []
}
```
