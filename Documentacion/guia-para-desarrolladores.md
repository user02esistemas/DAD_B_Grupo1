# Guia Para Desarrolladores

## No Saltarse Capas

```text
Web -> RMI -> DAO -> MySQL
Flutter -> API REST -> RMI -> DAO -> MySQL
```

## Agregar Una Funcionalidad Nueva

Ejemplo: productos, ventas, reportes o usuarios.

### 1. Revisar Contrato RMI

Buscar si ya existe DTO o metodo en `BoticaRMIInterface`.

Si falta algo:

- Agregar DTO en `pe.edu.botica.rmi.dto`.
- Agregar metodo en el servicio correspondiente.
- Compilar `BoticaRMIInterface`.
- Actualizar servidor, API y cliente que lo use.

### 2. Implementar En Servidor RMI

Ubicacion:

```text
BoticaRMIServidor/src/main/java
```

Aqui se implementa la logica y se llama a DAO/JDBC.

### 3. Exponer En API Si Lo Usa Flutter

Ubicacion:

```text
BoticaAPIREST/src/main/java
```

La API solo recibe HTTP/JSON y llama a RMI.

### 4. Probar Con Postman

Antes de conectar Flutter, probar endpoint y guardar coleccion en `Postman/`.

### 5. Conectar Web O Flutter

El cliente no debe conectarse directo a la base de datos.

## Git

Ramas sugeridas:

```text
main
develop
feature/rmi-productos
feature/api-productos
feature/flutter-productos
feature/web-productos
```
