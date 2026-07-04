# Guia De Instalacion Y Ejecucion

## Requisitos

```text
JDK 17
NetBeans 19 o superior
Tomcat 10.1
XAMPP MySQL/MariaDB
Flutter SDK
Postman
Git
```

## Base De Datos

1. Encender MySQL desde XAMPP.
2. Importar `Database/schema.sql`.
3. Verificar que exista la base `bd_BoticaEconoSalud`.

## Abrir En NetBeans

Abrir como proyectos:

```text
BoticaRMIInterface
BoticaRMIServidor
BoticaAPIREST
BoticaWebCliente
```

## Orden De Ejecucion

```text
1. MySQL/MariaDB
2. BoticaRMIServidor
3. BoticaAPIREST en Tomcat 10.1
4. BoticaWebCliente en Tomcat 10.1
5. Postman para probar API
6. BoticaMobileFlutter
```

## Nota Sobre Tomcat

El Tomcat incluido en XAMPP puede usar `javax.servlet`. Este proyecto usa `jakarta.servlet`, por eso se recomienda Tomcat 10.1 externo.
