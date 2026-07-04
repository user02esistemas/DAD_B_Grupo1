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

```text
ROLE_ADMIN
ROLE_FARMACEUTICO
```

## Reglas

1. No guardar contrasenas en texto plano.
2. No exponer datos sensibles en respuestas JSON.
3. Validar permisos en servidor, no solo en frontend.
4. Registrar errores sin mostrar stacktrace al usuario final.
