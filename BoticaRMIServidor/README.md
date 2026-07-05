# BoticaRMIServidor

Componente encargado de publicar los servicios remotos RMI y ejecutar la logica de negocio distribuida.

Este servidor usara las interfaces de `BoticaRMIInterface` y accedera a MySQL mediante DAO/JDBC.

Estructura interna:

```text
rmi.server
rmi.services.auth
rmi.services.productos
rmi.services.ventas
rmi.services.compras
rmi.services.reportes
rmi.dao
rmi.config
rmi.util
```
