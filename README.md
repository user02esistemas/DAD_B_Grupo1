# EconoSalud

Sistema distribuido para la gestion de una botica con interfaz web, interfaz movil Flutter, API JSON, servicios Java RMI y base de datos MySQL/MariaDB.

## Componentes

```text
BoticaAPIREST/          API HTTP/JSON para Flutter y Postman
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

`BoticaMobileFlutter` sera proyecto Flutter cuando se cree con Flutter SDK.

## Arquitectura

```text
Web JSP -> Servlets Web -> RMI -> DAO/JDBC -> MySQL
Flutter -> API JSON -> RMI -> DAO/JDBC -> MySQL
```

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

## Fuera Del Repositorio

El informe Word, capturas pesadas, videos y anexos finales del curso deben mantenerse fuera de este repositorio para no mezclar evidencias academicas con el codigo fuente del sistema.

La documentacion tecnica en Markdown si puede estar en `Documentacion/`.

Estructura local sugerida:

```text
PAF/
├── EconoSalud/          Proyecto GitHub
└── Entrega_Final/       Informe, capturas, diagramas y anexos
```
