# Seguridad Y Permisos

## Seguridad Actual

El sistema web usa autenticacion con usuario y contrasena cifrada con BCrypt.

## Seguridad Objetivo

Para web:

```text
Sesion HTTP + roles de usuario
```

Para API movil:

```text
Login JSON + token para solicitudes protegidas
```

## Roles Iniciales

| Rol | Descripcion | Uso principal |
|---|---|---|
| `ROLE_ADMIN` | Administrador del sistema | Gestion completa del sistema, usuarios, reportes, inventario, compras y ventas |
| `ROLE_FARMACEUTICO` | Farmaceutico | Operacion de atencion, ventas, caja y consulta de productos |
| `ROLE_ALMACENERO` | Almacenero | Gestion de inventario, compras, stock y vencimientos |

Estos roles vienen de la tabla `roles` y se asignan mediante `usuario_roles`.

## Aplicacion Movil

La app movil debe aplicar la misma regla de negocio que la web:

| Modulo movil | Admin | Farmaceutico | Almacenero |
|---|---|---|---|
| Dashboard | Si | Si | Si |
| Productos | Si | Si | Si |
| Ventas | Si | Si | No |
| Compras | Si | No | Si |
| Caja | Si | Si | No |
| Reportes | Si | Consulta limitada | Consulta limitada |
| Usuarios | Si | No | No |

Flutter puede ocultar opciones segun rol, pero la API/RMI debe validar permisos en servidor para evitar acceso no autorizado.

## Reglas

1. No guardar contrasenas en texto plano.
2. No exponer datos sensibles en respuestas JSON.
3. Validar permisos en servidor, no solo en frontend.
4. Registrar errores sin mostrar stacktrace al usuario final.
