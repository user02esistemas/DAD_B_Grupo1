# BoticaMobileFlutter

Aplicacion movil Flutter del sistema EconoSalud. Consume la API REST y no se conecta directamente a RMI ni a MySQL.

Flujo de comunicacion esperado:

```text
Flutter -> BoticaAPIREST -> BoticaRMIInterface -> BoticaRMIServidor -> MySQL
```

Estructura MVVM por feature:

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
    │   ├── model/
    │   ├── service/
    │   ├── view/
    │   └── viewmodel/
    ├── dashboard/
    │   ├── model/
    │   ├── service/
    │   ├── view/
    │   └── viewmodel/
    ├── health/
    │   ├── model/
    │   ├── service/
    │   ├── view/
    │   └── viewmodel/
    └── productos/
        ├── model/
        ├── service/
        ├── view/
        └── viewmodel/
```

## Capas internas

```text
core/config             URL base y configuracion general.
core/network            Cliente HTTP que consume respuestas ApiResponse.
core/widgets            Widgets compartidos.
features/*/model        Entidades y parseo JSON.
features/*/service      Consumo REST usando core/network/api_client.dart.
features/*/viewmodel    Estado de pantalla, carga, error y acciones.
features/*/view         Pantallas y widgets propios del modulo.
```

Regla: la app movil solo consume endpoints JSON de `BoticaAPIREST`. No debe conectarse directo a RMI ni a MySQL.

## Roles y permisos

La app movil debe respetar los mismos roles definidos en la web y en la base de datos:

| Rol | Descripcion | Acceso movil esperado |
|---|---|---|
| `ROLE_ADMIN` | Administrador del sistema | Acceso completo a dashboard, productos, ventas, compras, reportes, usuarios y configuracion |
| `ROLE_FARMACEUTICO` | Farmaceutico | Acceso operativo a ventas, productos, caja y consultas necesarias para atencion |
| `ROLE_ALMACENERO` | Almacenero | Acceso a inventario, productos, compras y alertas de stock/vencimiento |

El control visual en Flutter solo ayuda a ocultar opciones. La validacion real de permisos debe hacerse en la API/RMI igual que en el sistema web.

## Funcionalidad inicial

```text
Login con /api/auth/login
Estado API/RMI con /api/health
Dashboard con /api/dashboard/resumen
Busqueda de productos con /api/productos
Menu de modulos filtrado por roles del usuario autenticado
```

## Modulos pendientes

```text
ventas      Registrar venta, listar ultimas ventas y validar stock.
compras     Registrar compra y listar proveedores.
caja        Abrir/cerrar sesion de caja y consultar estado.
usuarios    Listar usuarios y administrar acceso segun rol.
reportes    Ventas por rango y productos mas vendidos.
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
