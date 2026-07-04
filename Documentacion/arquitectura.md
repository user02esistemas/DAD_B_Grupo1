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
