# BoticaMobileFlutter

Aplicacion movil Flutter del sistema EconoSalud. Consume la API REST y no se conecta directamente a RMI ni a MySQL.

Flujo de comunicacion esperado:

```text
Flutter -> BoticaAPIREST -> BoticaRMIInterface -> BoticaRMIServidor -> MySQL
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

## Capas internas

```text
core/config       URL base y configuracion general.
core/network      Cliente HTTP que consume respuestas ApiResponse.
core/widgets      Widgets compartidos.
features/auth     Login y modelo de usuario.
features/dashboard Indicadores principales.
features/productos Busqueda y listado de productos.
```

Regla: la app movil solo consume endpoints JSON de `BoticaAPIREST`. No debe conectarse directo a RMI ni a MySQL.

## Funcionalidad inicial

```text
Login con /api/auth/login
Dashboard con /api/dashboard/resumen
Busqueda de productos con /api/productos
```

## Ejecucion

Instalar Flutter SDK y ejecutar:

```powershell
flutter create .
flutter pub get
flutter run
```

`flutter create .` se ejecuta una sola vez cuando la carpeta todavia no tiene plataformas generadas como `android/`, `ios/` o `windows/`. El comando conserva el codigo existente en `lib/` y completa la estructura estandar de Flutter.

Por defecto, la app apunta a:

```text
http://10.0.2.2:8081/BoticaAPIREST
```

Ese host funciona para emulador Android. Para Windows, navegador o celular fisico, indicar la URL de la API:

```powershell
flutter run --dart-define=API_BASE_URL=http://localhost:8081/BoticaAPIREST
```

Si se prueba desde un celular fisico, usar la IP local de la PC donde corre Tomcat:

```powershell
flutter run --dart-define=API_BASE_URL=http://192.168.1.50:8081/BoticaAPIREST
```

## Verificacion

Comandos ejecutados correctamente:

```powershell
flutter pub get
flutter analyze
flutter test
flutter build apk --debug
```

APK generado:

```text
build/app/outputs/flutter-apk/app-debug.apk
```
