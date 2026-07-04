# Estructura Del Proyecto

```text
EconoSalud/
├── BoticaAPIREST/
├── BoticaMobileFlutter/
├── BoticaRMIInterface/
├── BoticaRMIServidor/
├── BoticaWebCliente/
├── Database/
├── Documentacion/
├── Library/
├── Postman/
├── README.md
├── .gitignore
└── pom.xml
```

## Descripcion

```text
BoticaAPIREST          Proyecto Maven Web para endpoints JSON.
BoticaMobileFlutter    Proyecto Flutter de la app movil.
BoticaRMIInterface     Proyecto Maven JAR con interfaces y DTOs RMI.
BoticaRMIServidor      Proyecto Maven JAR con implementaciones RMI.
BoticaWebCliente       Proyecto Java Web Ant con JSP/Servlet.
Database               Scripts SQL del sistema.
Documentacion          Markdown tecnico del sistema.
Library                JARs necesarios para BoticaWebCliente.
Postman                Colecciones de pruebas API.
```

## BoticaAPIREST

```text
src/main/java/pe/edu/botica/api/
├── auth/
├── productos/
├── ventas/
├── compras/
├── reportes/
├── common/
├── config/
└── filter/
```

## BoticaRMIInterface

```text
src/main/java/pe/edu/botica/rmi/
├── auth/
├── productos/
├── ventas/
├── compras/
├── reportes/
├── dto/
└── exception/
```

## BoticaRMIServidor

```text
src/main/java/pe/edu/botica/rmi/
├── server/
├── services/
│   ├── auth/
│   ├── productos/
│   ├── ventas/
│   ├── compras/
│   └── reportes/
├── dao/
├── config/
└── util/
```

## BoticaMobileFlutter

```text
lib/
├── main.dart
├── core/
│   ├── config/
│   ├── network/
│   ├── storage/
│   └── widgets/
└── features/
    ├── auth/
    ├── dashboard/
    ├── productos/
    └── ventas/
```

## Postman

```text
Postman/
├── collections/
└── environments/
```
