# BoticaMobileFlutter

Carpeta reservada para la aplicacion movil Flutter del sistema.

Flujo de comunicacion esperado:

```text
Flutter -> BoticaAPIREST -> BoticaRMIServidor -> MySQL
```

Estructura inicial:

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
