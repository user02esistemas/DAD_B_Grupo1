# EconoSalud

Sistema distribuido para la gestion de una botica con interfaz web, interfaz movil Flutter, API JSON, servicios Java RMI y base de datos MySQL/MariaDB.

## Componentes

```text
BoticaAPIREST/          API HTTP/JSON para Web, Flutter y Postman
BoticaMobileFlutter/    Aplicacion movil Flutter
BoticaRMIInterface/     Interfaces remotas Java RMI
BoticaRMIServidor/      Servidor e implementaciones RMI
BoticaWebCliente/       Cliente web JSP/Servlet existente
Database/               Scripts SQL del sistema
Documentacion/          Documentacion tecnica en Markdown
Library/                Librerias JAR compartidas del cliente web Ant
Postman/                Colecciones de pruebas API
```

## Apertura En NetBeans

En NetBeans se pueden abrir como proyectos separados, igual que una arquitectura distribuida por componentes:

```text
BoticaRMIInterface   Proyecto Maven Java / JAR
BoticaRMIServidor    Proyecto Maven Java / JAR
BoticaAPIREST        Proyecto Maven Web / WAR
BoticaWebCliente     Proyecto Java Web Ant / WAR
```

Carpetas que no son proyecto ejecutable:

```text
Database
Documentacion
Library
Postman
BoticaMobileFlutter
```

`BoticaMobileFlutter` es el proyecto Flutter de la aplicacion movil.

## Arquitectura

```text
Web JSP/Servlet -> API JSON -> RMI -> DAO/JDBC -> MySQL
Flutter -> API JSON -> RMI -> DAO/JDBC -> MySQL
```

## Equipo

| Alumno | Rol en el proyecto | Responsabilidad principal |
|---|---|---|
| Escribano Macalopu Daniel Erick | Lider tecnico e integrador full stack | Arquitectura distribuida, backend RMI/API, apoyo en web y movil, GitHub e integracion general |
| Montenegro Villalobos Hector Samir | Desarrollador backend distribuido | Servicios Java RMI, API REST JSON, DTOs y pruebas con Postman |
| Capitan Leon Alexander Grabiel | Desarrollador movil | Flutter, consumo de API REST, pantallas moviles y evidencias de prueba |
| Mejia Quiroz Arnold Braian | Desarrollador web y documentador | Cliente web JSP/Servlet, apoyo en base de datos, manuales tecnicos y evidencias |

La division sigue la guia del informe final: cada integrante debe evidenciar su aporte mediante commits, pruebas, documentacion, capturas o tareas completadas.

## Documentacion Tecnica

```text
Documentacion/arquitectura.md
Documentacion/estructura-proyecto.md
Documentacion/api-rest-endpoints.md
Documentacion/contratos-rmi.md
Documentacion/frontend-web.md
Documentacion/guia-instalacion-ejecucion.md
Documentacion/guia-para-desarrolladores.md
Documentacion/seguridad-permisos.md
Documentacion/permisos-db.md
```

## Ejecucion Local

```text
1. Encender XAMPP MySQL.
2. Importar `Database/schema.sql`.
3. Ejecutar BoticaRMIServidor.
4. Ejecutar BoticaAPIREST en Tomcat.
5. Ejecutar BoticaWebCliente en Tomcat.
6. Probar API con Postman.
7. Ejecutar BoticaMobileFlutter.
```

Verificacion rapida de API y RMI:

```text
GET http://localhost:8081/BoticaAPIREST/api/health
```

## Versiones Recomendadas

```text
JDK 17
NetBeans 19 o superior
Tomcat 10.1
XAMPP MySQL/MariaDB
Flutter SDK
Postman
Git/GitHub
```

Nota: el Tomcat incluido en XAMPP puede usar `javax.servlet`, por eso para la aplicacion Java Web/API se recomienda Tomcat 10.1 con `jakarta.servlet`. XAMPP se usara principalmente para MySQL/MariaDB.

## Flujo GitHub

Ramas del proyecto:

| Rama | Uso | Responsable principal |
|---|---|---|
| `main` | Version estable del proyecto | Daniel |
| `develop` | Integracion antes de pasar a `main` | Daniel |
| `funcionalidad/backend` | RMI, API REST, DTOs y Postman | Daniel y Hector |
| `funcionalidad/web` | Cliente web JSP/Servlet | Arnold y Daniel |
| `funcionalidad/movil` | App movil Flutter | Alexander y Daniel |

Regla principal: nadie debe trabajar directo en `main`. Los cambios se hacen en ramas `funcionalidad/*`, se integran primero en `develop`, se prueban y luego pasan a `main`.

Antes de subir cambios:

```text
1. Compilar los proyectos Maven.
2. Probar endpoints principales en Postman.
3. Revisar cambios con git status y git diff.
4. Hacer commit con mensaje claro en la rama correspondiente.
5. Subir a GitHub y avisar al lider para integracion.
```
