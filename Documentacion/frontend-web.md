# Frontend Web

El cliente web vive en `BoticaWebCliente`.

Tecnologia actual:

```text
JSP + Servlets + CSS + JavaScript + Tomcat 10.1
```

## Modulos Web Existentes

```text
Login
Dashboard
Usuarios
Inventario
Compras
Ventas
Caja
Reportes
```

## Flujo Objetivo

Actualmente algunos controladores usan DAO directamente. La meta del curso es migrar operaciones principales a RMI:

```text
JSP -> Servlet Controller -> Cliente RMI -> Servicio RMI -> DAO -> MySQL
```

## Reglas

1. No duplicar logica de negocio en JSP.
2. Los JSP solo muestran datos y envian formularios.
3. Los Servlets coordinan solicitudes.
4. La logica fuerte debe quedar en `BoticaRMIServidor`.
