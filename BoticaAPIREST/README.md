# BoticaAPIREST

API HTTP/JSON consumida por Flutter y probada con Postman.

La API no accedera directamente a la base de datos. Actuara como puente:

```text
Flutter/Postman -> API REST -> RMI -> DAO/JDBC -> MySQL
```

Endpoints propuestos:

```text
POST /api/auth/login
GET  /api/productos
GET  /api/productos/{id}
POST /api/ventas
GET  /api/dashboard/resumen
```
