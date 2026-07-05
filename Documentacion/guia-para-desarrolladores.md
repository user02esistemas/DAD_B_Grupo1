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

- Agregar DTO en `rmi.dto`.
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

## Reparto Del Equipo

Matriz base de responsabilidades para el informe y la organizacion del desarrollo:

| Integrante | Rol desempenado | Capa o modulo principal | Actividades principales | Evidencia objetiva | Contribucion referencial |
|---|---|---|---|---|---|
| Escribano Macalopu Daniel Erick | Lider tecnico e integrador full stack | Arquitectura, backend, web, movil, integracion y GitHub | Coordinar estructura distribuida, implementar e integrar backend RMI/API, apoyar web y movil, revisar compilacion, mantener repositorio y documentacion principal | Commits de integracion, clases RMI/API, README, arquitectura, historial GitHub, pruebas generales | 35% |
| Montenegro Villalobos Hector Samir | Desarrollador backend distribuido | RMI, API REST y Postman | Implementar contratos RMI, servicios remotos, endpoints JSON, manejo de respuestas y pruebas API junto al lider | Clases RMI/API, coleccion Postman, evidencias de endpoints funcionando | 25% |
| Capitan Leon Alexander Grabiel | Desarrollador movil | Flutter, consumo API y pruebas moviles | Construir app movil, consumir endpoints REST, validar pantallas moviles y registrar evidencias de ejecucion | Pantallas Flutter, consumo HTTP, capturas y pruebas moviles | 20% |
| Mejia Quiroz Arnold Braian | Desarrollador web y documentador | JSP/Servlet, web, documentacion y apoyo en base de datos | Mantener cliente web heredado, apoyar consultas SQL, documentar uso, preparar manuales y evidencias de prueba | Cambios en web, capturas de flujo web, manuales, documentacion y consultas verificadas | 20% |

Los porcentajes son referenciales y deben ajustarse al final segun commits, tareas completadas y evidencias reales.

## Criterios De Evidencia

| Tipo de evidencia | Uso esperado |
|---|---|
| Commits en GitHub | Validar avance tecnico individual o integraciones realizadas |
| Capturas de Postman | Confirmar endpoints REST probados correctamente |
| Capturas de la web | Confirmar funcionamiento del cliente JSP/Servlet |
| Capturas de Flutter | Confirmar consumo movil de la API |
| Scripts SQL | Respaldar cambios en tablas, datos, triggers o consultas |
| Documentacion Markdown | Sustentar arquitectura, endpoints, contratos y guias de ejecucion |

Cada integrante debe trabajar sobre una parte clara, probarla y comunicar al lider que archivos modifico antes de integrar a `main`.

## Matriz RACI Base

| Actividad | Daniel | Hector | Alexander | Arnold |
|---|---|---|---|---|
| Arquitectura distribuida | A/R | C | C | C |
| Contratos e implementacion RMI | A/R | R | C | I |
| API REST y Postman | A/R | R | C | C |
| Base de datos MySQL/MariaDB | A/R | C | I | C |
| Cliente web JSP/Servlet | A/R | C | I | R |
| App movil Flutter | A/R | C | R | I |
| Documentacion tecnica | A/R | C | C | R |
| Integracion GitHub | A/R | C | C | C |

Leyenda: R = responsable de ejecutar, A = responsable final, C = consultado, I = informado.

## Antes De Hacer Commit

```text
1. Ejecutar compilacion Maven desde la raiz.
2. Reiniciar RMI si se agrego o cambio un servicio.
3. Reiniciar BoticaAPIREST en Tomcat si se agrego un endpoint.
4. Probar en Postman los endpoints afectados.
5. Revisar git status y git diff.
```
