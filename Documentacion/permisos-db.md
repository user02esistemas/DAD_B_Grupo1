# Permisos Y Base De Datos

## Base Local

```text
bd_BoticaEconoSalud
```

Script principal:

```text
Database/schema.sql
```

## Tablas Principales

```text
usuarios
roles
usuario_roles
catalogo_productos_digemid
productos
proveedores
transacciones
detalle_transacciones
movimientos_inventario
sesiones_caja
cajas
tipos_transaccion
```

## Reglas

1. Los cambios de estructura deben actualizar `Database/schema.sql`.
2. No colocar credenciales reales en el repositorio.
3. Las consultas deben ejecutarse desde DAO o servicios del servidor RMI.
4. No conectar Flutter directamente a MySQL.
