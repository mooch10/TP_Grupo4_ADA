# Guía Completa de Ejecución (Desde Cero)

Esta guía está pensada para una persona que recién clona el proyecto y necesita ejecutar correctamente el ecosistema para aprobar el examen de `cliente-service`.

## 1. Requisitos previos

Instalar en Windows:

1. Java 21 o superior (recomendado Java 21 LTS).
2. Maven 3.9 o superior.
3. Git.
4. Visual Studio Code (opcional, recomendado).
5. Extensión Java Extension Pack en VS Code (opcional, recomendado).

Verificación en terminal:

```powershell
java -version
mvn -version
git --version
```

## 2. Clonar y abrir el proyecto

```powershell
git clone <URL_DEL_REPOSITORIO>
cd arquitecturaaplicaciones_microservicios
```

Abrir la carpeta raiz en VS Code.

## 3. Recargar proyectos Java/Maven (VS Code)

1. Ejecutar `Maven: Reload All Projects`.
2. Si no aparecen modulos o hay errores raros: `Java: Clean Java Language Server Workspace` y reiniciar VS Code.

## 4. Compilar una vez todo el proyecto

Desde la raiz del repositorio:

```powershell
mvn clean install -DskipTests
```

Si aparece `no POM in this directory`, no estas en la carpeta correcta.

## 5. Orden de arranque recomendado

Abrir 5 terminales separadas y ejecutar en este orden:

### Terminal 1 - Config Server

```powershell
cd config-server
mvn spring-boot:run
```

### Terminal 2 - Eureka Server

```powershell
cd eureka-server
mvn spring-boot:run
```

### Terminal 3 - Auth Service

```powershell
cd auth-service
mvn spring-boot:run
```

### Terminal 4 - Cliente Service

```powershell
cd cliente-service
mvn spring-boot:run
```

### Terminal 5 - API Gateway

```powershell
cd api-gateway
mvn spring-boot:run
```

## 6. Verificaciones minimas de salud

1. Eureka UI: http://localhost:8761
2. Deben verse `AUTH-SERVICE` y `CLIENTE-SERVICE` en estado `UP`.
3. Gateway health: http://localhost:8080/actuator/health

## 7. Obtener JWT para pruebas

```powershell
$body = @{ username="admin"; password="admin123" } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/auth/login" -ContentType "application/json" -Body $body
$token = $login.token
$token
```

## 8. Probar CRUD de cliente via gateway

### Crear

```powershell
$new = @{ nombre="Juan Perez"; telefono="+54 11 1234-5678" } | ConvertTo-Json
$c = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/clientes" -Headers @{ Authorization="Bearer $token" } -ContentType "application/json" -Body $new
$id = $c.id
```

### Listar

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/clientes" -Headers @{ Authorization="Bearer $token" }
```

### Obtener por ID

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/clientes/$id" -Headers @{ Authorization="Bearer $token" }
```

### Actualizar

```powershell
$u = @{ nombre="Juan Editado"; telefono="+54 11 7000-0000" } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/clientes/$id" -Headers @{ Authorization="Bearer $token" } -ContentType "application/json" -Body $u
```

### Eliminar

```powershell
Invoke-WebRequest -Method Delete -Uri "http://localhost:8080/api/clientes/$id" -Headers @{ Authorization="Bearer $token" }
```

## 9. Ejecutar tests oficiales del examen

Abrir terminal en el modulo de tests:

```powershell
cd exam-cliente-integration-tests
$env:EXAM_STUDENT_ID = "TU_LEGAJO"
$env:GATEWAY_BASE_URL = "http://localhost:8080"
mvn verify
```

Salida esperada:

1. `Tests run: 9, Failures: 0, Errors: 0`
2. `BUILD SUCCESS`
3. `Resultado: APROBADO`

## 10. Solucion de problemas comunes

### Error: `mvn is not recognized`

- Maven no esta en PATH.
- Usar ruta completa a `mvn.cmd` o instalar Maven correctamente.

### Error: `JAVA_HOME is not defined correctly`

En PowerShell:

```powershell
$env:JAVA_HOME = "C:\ruta\a\tu\jdk"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

### Error: `no POM in this directory`

- Entrar a la carpeta correcta del proyecto o modulo.

### Error 403 en Eureka

- Reiniciar `eureka-server` y refrescar navegador.
- Validar URL: `http://localhost:8761`.

### Tests fallan con 404 en `/api/clientes`

- Reiniciar `api-gateway` para asegurar que tomo la configuracion de rutas.
- Verificar que `cliente-service` este en `UP` en Eureka.

## 11. Recomendaciones finales para entrega

1. Mantener corriendo `config-server`, `eureka-server`, `auth-service`, `cliente-service` y `api-gateway` durante `mvn verify`.
2. Guardar evidencia: captura de `BUILD SUCCESS` y `Resultado: APROBADO`.
3. Entregar tambien el archivo de reporte generado en:
   - `exam-cliente-integration-tests/target/exam-report/resultado-examen.json`
4. No ejecutar comandos desde carpetas equivocadas.
5. Si algo falla, reiniciar primero `api-gateway` y luego reintentar.
