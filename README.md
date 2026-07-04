# Botica_EconoSalud_Distribuido

Sistema distribuido para la gestion de una botica con interfaz web, interfaz movil Flutter, API JSON, servicios Java RMI y base de datos MySQL/MariaDB.

## Componentes

```text
Documentacion/          Informe, arquitectura, diagramas y evidencias
Library/                Librerias JAR compartidas
BoticaAPIREST/          API HTTP/JSON para Flutter y Postman
BoticaRMIInterface/     Interfaces remotas Java RMI
BoticaRMIServidor/      Servidor e implementaciones RMI
BoticaWebCliente/       Cliente web JSP/Servlet existente
BoticaMobileFlutter/    Aplicacion movil Flutter
Postman/                Colecciones de pruebas API
script.sql              Script principal de base de datos
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

## Ejecucion Local

```text
1. Encender XAMPP MySQL.
2. Importar `script.sql`.
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
