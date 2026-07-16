# Guia De Instalacion Y Ejecucion

Esta guia permite clonar, compilar y ejecutar EconoSalud en una PC nueva usando Windows, XAMPP, NetBeans, Tomcat 10.1, Maven y Postman.

## 1. Requisitos

Instalar o tener disponible:

| Herramienta | Version recomendada | Uso |
|---|---|---|
| Git | Ultima estable | Clonar y actualizar el repositorio |
| JDK | 17 | Compilar y ejecutar Java |
| NetBeans | 19 o superior | Abrir y ejecutar proyectos Java |
| Apache Tomcat | 10.1 | Ejecutar `BoticaAPIREST` y el cliente web si aplica |
| XAMPP | MariaDB/MySQL | Base de datos local |
| Postman | Ultima estable | Probar endpoints REST |
| Flutter SDK | Ultima estable | Ejecutar la app movil cuando se implemente |

Importante: usar Tomcat 10.1 externo para la API REST porque el proyecto usa `jakarta.servlet`. El Tomcat incluido en XAMPP puede usar `javax.servlet` y puede generar errores.

## 2. Clonar Proyecto

Abrir PowerShell en la carpeta donde se guardaran los proyectos.

```powershell
git clone https://github.com/DanielErickEM/EconoSalud.git
```

Entrar al proyecto:

```powershell
Set-Location -LiteralPath "EconoSalud"
```

Verificar rama y estado:

```powershell
git branch --show-current
git status --short
```

Debe estar en `main` y sin cambios locales.

Si el integrante va a desarrollar, debe cambiar a su rama asignada:

```powershell
git fetch origin
git checkout funcionalidad/backend
```

Ramas disponibles:

```text
develop
funcionalidad/backend
funcionalidad/web
funcionalidad/movil
```

## 3. Estructura Principal

```text
BoticaRMIInterface      Contratos y DTOs RMI
BoticaRMIServidor       Servidor RMI y acceso JDBC a MySQL
BoticaAPIREST           API REST JSON para Postman y Flutter
BoticaWebCliente        Cliente web JSP/Servlet heredado
Database                Script SQL de base de datos
Postman                 Coleccion y entorno de pruebas
Documentacion           Guias tecnicas
```

## 4. Configurar Base De Datos

Abrir XAMPP y encender:

```text
MySQL
```

La conexion esperada por el servidor RMI es:

```text
Host: localhost
Puerto: 3306
Base de datos: bd_BoticaEconoSalud
Usuario: root
Clave: vacia
```

Archivo de configuracion:

```text
BoticaRMIServidor/src/main/java/rmi/config/DatabaseConfig.java
```

### Opcion A: Importar Con phpMyAdmin

1. Abrir `http://localhost/phpmyadmin`.
2. Ir a `Importar`.
3. Seleccionar `Database/schema.sql`.
4. Ejecutar la importacion.
5. Verificar que exista la base `bd_BoticaEconoSalud`.

### Opcion B: Importar Con Comando

Si MySQL de XAMPP esta en la ruta por defecto:

```powershell
& "C:\xampp\mysql\bin\mysql.exe" -u root < "Database\schema.sql"
```

Verificar base creada:

```powershell
& "C:\xampp\mysql\bin\mysql.exe" -u root -e "SHOW DATABASES LIKE 'bd_BoticaEconoSalud';"
```

Verificar usuario administrador del sistema:

```powershell
& "C:\xampp\mysql\bin\mysql.exe" -u root -D bd_BoticaEconoSalud -e "SELECT id, username, nombre_completo, activo FROM usuarios WHERE username='admin';"
```

Credenciales de prueba:

```text
Usuario: admin
Clave: admin
```

## 5. Compilar Proyectos Maven

Desde la raiz del proyecto:

```powershell
mvn clean install
```

Si no tienen Maven instalado pero usan NetBeans 19, pueden usar el Maven incluido en NetBeans:

```powershell
& "C:\Program Files\NetBeans-19\netbeans\java\maven\bin\mvn.cmd" clean install
```

Resultado esperado:

```text
BUILD SUCCESS
```

Este comando compila:

```text
BoticaRMIInterface
BoticaRMIServidor
BoticaAPIREST
```

## 6. Abrir En NetBeans

Abrir estos proyectos por separado:

```text
BoticaRMIInterface
BoticaRMIServidor
BoticaAPIREST
BoticaWebCliente
```

Orden recomendado:

```text
1. BoticaRMIInterface
2. BoticaRMIServidor
3. BoticaAPIREST
4. BoticaWebCliente
```

## 7. Ejecutar Servidor RMI

El servidor RMI debe ejecutarse antes que la API REST.

Puerto usado:

```text
1099
```

### Opcion A: Desde NetBeans

1. Abrir `BoticaRMIServidor`.
2. Buscar la clase:

```text
BoticaRMIServidor/src/main/java/rmi/server/RMIServer.java
```

3. Ejecutar `RMIServer.java`.

Consola esperada:

```text
Servidor RMI iniciado en puerto 1099
Servicio publicado: AuthService
Servicio publicado: UsuarioService
Servicio publicado: ProductoService
Servicio publicado: DashboardService
Servicio publicado: ReporteService
Servicio publicado: VentaService
Servicio publicado: CompraService
```

### Opcion B: Desde PowerShell Con Maven

Desde la raiz del proyecto:

```powershell
mvn -pl BoticaRMIServidor exec:java -Dexec.mainClass="rmi.server.RMIServer"
```

Con Maven de NetBeans:

```powershell
& "C:\Program Files\NetBeans-19\netbeans\java\maven\bin\mvn.cmd" -pl BoticaRMIServidor exec:java -Dexec.mainClass="rmi.server.RMIServer"
```

Mantener esta consola abierta mientras se prueba la API.

## 8. Ejecutar API REST En Tomcat 10.1

La API usa el contexto:

```text
/BoticaAPIREST
```

URL base local usada en Postman:

```text
http://localhost:8081/BoticaAPIREST
```

### Opcion A: Desde NetBeans

1. Registrar Tomcat 10.1 externo en NetBeans.
2. Abrir `BoticaAPIREST`.
3. Verificar que use Tomcat 10.1, no Tomcat de XAMPP.
4. Ejecutar el proyecto.

### Opcion B: Desplegar WAR Manualmente

Compilar:

```powershell
mvn clean package
```

Con Maven de NetBeans:

```powershell
& "C:\Program Files\NetBeans-19\netbeans\java\maven\bin\mvn.cmd" clean package
```

Copiar el WAR a Tomcat. Ajustar la ruta si Tomcat esta en otra carpeta:

```powershell
Copy-Item -LiteralPath "BoticaAPIREST\target\BoticaAPIREST.war" -Destination "C:\Users\51999\Documents\apache-tomcat-10.1.46\webapps\BoticaAPIREST.war" -Force
```

Iniciar Tomcat:

```powershell
& "C:\Users\51999\Documents\apache-tomcat-10.1.46\bin\startup.bat"
```

Si Tomcat usa puerto `8080`, la URL sera:

```text
http://localhost:8080/BoticaAPIREST
```

Si Tomcat fue configurado en `8081`, usar:

```text
http://localhost:8081/BoticaAPIREST
```

## 9. Importar Postman

Importar coleccion:

```text
Postman/collections/EconoSalud-API.postman_collection.json
```

Importar environment:

```text
Postman/environments/EconoSalud-Local.postman_environment.json
```

Seleccionar environment:

```text
EconoSalud Local
```

Verificar variable:

```text
base_url = http://localhost:8081/BoticaAPIREST
```

Si su Tomcat corre en `8080`, cambiar `base_url` a:

```text
http://localhost:8080/BoticaAPIREST
```

## 10. Probar Endpoints Principales

Probar en este orden:

### Login

```text
POST {{base_url}}/api/auth/login
```

Body:

```json
{
  "username": "admin",
  "password": "admin"
}
```

### Productos

```text
GET {{base_url}}/api/productos?termino=para&limite=10
```

```text
GET {{base_url}}/api/productos/14
```

### Dashboard

```text
GET {{base_url}}/api/dashboard/resumen
```

```text
GET {{base_url}}/api/dashboard/productos-alerta?limite=10
```

### Ventas

```text
GET {{base_url}}/api/ventas/ultimas?limite=10
```

### Compras

```text
GET {{base_url}}/api/compras/proveedores
```

### Usuarios

```text
GET {{base_url}}/api/usuarios
```

```text
GET {{base_url}}/api/usuarios/1
```

### Reportes

```text
GET {{base_url}}/api/reportes/ventas?desde=2026-01-01&hasta=2026-12-31
```

```text
GET {{base_url}}/api/reportes/productos-mas-vendidos?limite=10
```

Respuesta esperada general:

```json
{
  "success": true,
  "message": "...",
  "data": {}
}
```

## 11. Ejecutar Cliente Web

`BoticaWebCliente` es el cliente web heredado JSP/Servlet.

Recomendado:

1. Abrir `BoticaWebCliente` en NetBeans.
2. Configurar Tomcat compatible con el proyecto web.
3. Ejecutar desde NetBeans.
4. Verificar login y modulos principales.

Si hay conflicto de puertos, usar un Tomcat para API y otro para web, o ejecutar uno a la vez.

## 12. Ejecutar Flutter

`BoticaMobileFlutter` ya es un proyecto Flutter con estructura MVVM por feature. Para ejecutarlo:

```powershell
Set-Location -LiteralPath "BoticaMobileFlutter"
flutter pub get
flutter run --dart-define=API_BASE_URL=http://localhost:8081/BoticaAPIREST
```

La app movil debe consumir la API REST, no RMI ni MySQL directamente.

En emulador Android puede usarse la URL por defecto `http://10.0.2.2:8081/BoticaAPIREST`. En celular fisico se debe usar la IP local de la PC donde corre Tomcat.

## 13. Orden Completo De Ejecucion

Seguir siempre este orden:

```text
1. Encender XAMPP MySQL.
2. Importar o verificar Database/schema.sql.
3. Compilar con mvn clean install.
4. Ejecutar BoticaRMIServidor.
5. Ejecutar BoticaAPIREST en Tomcat 10.1.
6. Importar Postman y seleccionar EconoSalud Local.
7. Probar login, productos, dashboard, ventas, compras, usuarios y reportes.
8. Ejecutar BoticaWebCliente si se trabajara la web.
9. Ejecutar BoticaMobileFlutter si se trabajara la app movil.
```

## 14. Actualizar Codigo Desde GitHub

Antes de empezar a trabajar, ubicarse en la rama asignada.

Ejemplo para backend:

```powershell
git checkout funcionalidad/backend
git pull origin funcionalidad/backend
```

Ejemplo para web:

```powershell
git checkout funcionalidad/web
git pull origin funcionalidad/web
```

Ejemplo para movil:

```powershell
git checkout funcionalidad/movil
git pull origin funcionalidad/movil
```

Si solo se quiere ejecutar la version estable del proyecto:

```powershell
git pull origin main
```

Si se quiere probar la version de integracion:

```powershell
git checkout develop
git pull origin develop
```

Verificar estado:

```powershell
git status --short
```

Si aparecen cambios locales que no son tuyos, avisar al lider antes de sobrescribir o borrar archivos.

## 15. Flujo De Trabajo Con Ramas

Ramas del equipo:

| Rama | Uso | Responsable principal |
|---|---|---|
| `main` | Version estable para entrega | Daniel |
| `develop` | Integracion y pruebas generales | Daniel |
| `funcionalidad/backend` | RMI, API REST y Postman | Daniel y Hector |
| `funcionalidad/web` | Cliente web JSP/Servlet | Arnold y Daniel |
| `funcionalidad/movil` | App movil Flutter | Alexander y Daniel |

Flujo obligatorio:

```text
funcionalidad/* -> develop -> main
```

Para subir cambios en la rama asignada:

```powershell
git status --short
git add .
git commit -m "Describe el cambio realizado"
git push
```

No hacer commits directos en `main` salvo integracion final aprobada por el lider.

## 16. Problemas Frecuentes

### Error: No se pudo conectar con XService RMI

Causa probable: el servidor RMI no esta ejecutandose o esta corriendo una version antigua.

Solucion:

```text
1. Detener el servidor RMI actual.
2. Compilar con mvn clean install.
3. Ejecutar nuevamente RMIServer.java.
4. Verificar que publique todos los servicios.
```

### Error: Address already in use 1099

Causa: ya existe otro RMI usando el puerto `1099`.

Buscar proceso:

```powershell
netstat -ano | findstr :1099
```

Finalizar proceso. Reemplazar `PID` por el numero encontrado:

```powershell
taskkill /PID PID /F
```

### Error 404 En Postman

Revisar:

```text
1. Tomcat esta encendido.
2. El WAR BoticaAPIREST esta desplegado.
3. El puerto del environment coincide con Tomcat.
4. La ruta empieza con /BoticaAPIREST.
```

### Error 500 En Postman

Revisar:

```text
1. MySQL esta encendido.
2. La base bd_BoticaEconoSalud existe.
3. RMI esta ejecutandose.
4. La consola de Tomcat muestra el detalle del error.
```

### Maven No Encuentra BoticaRMIInterface

Ejecutar desde la raiz del proyecto:

```powershell
mvn clean install
```

No compilar `BoticaAPIREST` aislado si primero no se instalo `BoticaRMIInterface`.

### Tomcat De XAMPP No Funciona Con API

Usar Tomcat 10.1 externo porque la API usa `jakarta.servlet`.

## 17. Comandos Rapidos

Compilar todo:

```powershell
mvn clean install
```

Ejecutar RMI:

```powershell
mvn -pl BoticaRMIServidor exec:java -Dexec.mainClass="rmi.server.RMIServer"
```

Generar WAR:

```powershell
mvn clean package
```

Ver estado Git:

```powershell
git status --short
```

Actualizar desde GitHub:

```powershell
git pull
```

Cambiar a rama backend:

```powershell
git checkout funcionalidad/backend
```

Cambiar a rama web:

```powershell
git checkout funcionalidad/web
```

Cambiar a rama movil:

```powershell
git checkout funcionalidad/movil
```

## 18. Checklist Final

Antes de decir que el proyecto esta funcionando, verificar:

```text
[ ] MySQL encendido.
[ ] Base bd_BoticaEconoSalud importada.
[ ] mvn clean install con BUILD SUCCESS.
[ ] RMIServer ejecutandose en puerto 1099.
[ ] API REST desplegada en Tomcat 10.1.
[ ] Postman usa el base_url correcto.
[ ] Login responde 200 OK.
[ ] Productos responde 200 OK.
[ ] Dashboard responde 200 OK.
[ ] Reportes responde 200 OK.
```
