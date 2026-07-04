# BoticaRMIServidor

Componente encargado de publicar los servicios remotos RMI y ejecutar la logica de negocio distribuida.

Este servidor usara las interfaces de `BoticaRMIInterface` y accedera a MySQL mediante DAO/JDBC.

Estructura interna:

```text
pe.edu.botica.rmi.server
pe.edu.botica.rmi.services.auth
pe.edu.botica.rmi.services.productos
pe.edu.botica.rmi.services.ventas
pe.edu.botica.rmi.services.compras
pe.edu.botica.rmi.services.reportes
pe.edu.botica.rmi.dao
pe.edu.botica.rmi.config
pe.edu.botica.rmi.util
```
