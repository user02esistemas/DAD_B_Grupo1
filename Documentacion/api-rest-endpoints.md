# API REST Endpoints

La API REST sera consumida por Flutter y probada con Postman.

Regla de arquitectura:

```text
Flutter/Postman -> BoticaAPIREST -> BoticaRMIInterface -> BoticaRMIServidor -> MySQL
```

La API no debe acceder directamente a la base de datos.

## Endpoints Iniciales

| Modulo | Metodo | Ruta | Descripcion |
|---|---|---|---|
| Auth | POST | `/api/auth/login` | Autentica usuario movil |
| Productos | GET | `/api/productos` | Lista o busca productos |
| Productos | GET | `/api/productos/{id}` | Obtiene detalle de producto |
| Ventas | POST | `/api/ventas` | Registra venta simple |
| Dashboard | GET | `/api/dashboard/resumen` | Muestra resumen movil |
| Reportes | GET | `/api/reportes/ventas` | Consulta ventas por rango |

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

Primer endpoint implementado:

```text
GET {{base_url}}/api/productos?termino=para&limite=10
```
