# Guía Completa de Ejecución del TP - Order Service

## Requisitos Previos

- Java 21 JDK
- Maven 3.9+
- RabbitMQ o Kafka (broker) accesible en localhost
- Git

## Arquitectura Implementada

```
┌─────────────────────────────────────────────────────────────────┐
│                         Cliente                                  │
└────────────────┬────────────────────────────────────────────────┘
                 │ HTTP/REST
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                  API Gateway (8080)                              │
│  - Enrutamiento de requests                                      │
│  - Validación de JWT                                             │
│  - LoadBalancer                                                  │
└────────────────┬────────────────────────────────────────────────┘
         │       │       │       │
         ▼       ▼       ▼       ▼
    ┌────────┬────────┬─────────┬──────────┐
    │ Auth   │Inventory│Notification│ Order  │
    │Service │Service  │   Service   │Service │
    │(8083)  │ (8082)  │   (8084)    │(8085)  │
    └────────┴────────┴─────────┴──────────┘
         │       │       │       │
         └───────┴───────┴───────┘
                 │
        ┌────────▼────────┐
        │  Eureka Server  │
        │     (8761)      │
        └─────────────────┘
         │       │       │
         └───────┼───────┘
                 │
        ┌────────▼────────────────┐
        │  Message Broker         │
        │  RabbitMQ (5672)        │
        │  o Kafka (9092)         │
        └────────────────────────┘
```

## PASO 1: Clonación y Preparación

```bash
cd c:\Programas\arquitecturaaplicaciones_microservicios

# Verificar que todos los módulos están presentes
ls -la order-service/
ls -la eureka-server/
ls -la auth-service/
ls -la api-gateway/
ls -la inventory-service/
ls -la notification-service/
```

## PASO 2: Compilar el Proyecto

```bash
# Compilar solo order-service para validación rápida
mvn clean compile -pl order-service -DskipTests

# O compilar todo el proyecto
mvn clean compile -DskipTests
```

**Salida esperada**: `BUILD SUCCESS`

<!-- Se removieron las instrucciones relacionadas con contenedores; este repositorio está preparado para ejecución local. -->

## PASO 4: Ejecutar Localmente

### Terminal 1 - RabbitMQ

Si no tenés RabbitMQ instalado, instalalo nativamente (por ejemplo con el instalador oficial en Windows o `choco install rabbitmq` si usás Chocolatey). Asegurate de que el broker esté accesible en `localhost:5672` y la interfaz en `http://localhost:15672`.

### Terminal 2 - Config Server

```bash
cd c:\Programas\arquitecturaaplicaciones_microservicios
mvn -pl config-server spring-boot:run
```

### Terminal 3 - Eureka Server

```bash
mvn -pl eureka-server spring-boot:run
```

### Terminal 4 - Auth Service

```bash
mvn -pl auth-service spring-boot:run
```

### Terminal 5 - API Gateway

```bash
mvn -pl api-gateway spring-boot:run
```

### Terminal 6 - Inventory Service

```bash
mvn -pl inventory-service spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=rabbitmq"
```

### Terminal 7 - Notification Service (Consumidor de eventos)

```bash
mvn -pl notification-service spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=rabbitmq"
```

### Terminal 8 - Order Service (NUEVO)

```bash
mvn -pl order-service spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=rabbitmq"
```

## PASO 5: Verificar que Todo Está Funcionando

### 5.1 Eureka Dashboard

Abrir en el navegador: http://localhost:8761

**Debería mostrar:**
- ✅ CONFIG-SERVER
- ✅ AUTH-SERVICE
- ✅ API-GATEWAY
- ✅ INVENTORY-SERVICE
- ✅ NOTIFICATION-SERVICE
- ✅ **ORDER-SERVICE** (NUEVO)

### 5.2 RabbitMQ Management

Abrir en el navegador: http://localhost:15672

**Credenciales:** guest / guest

**Verificar que existan:**
- Exchange: `order.exchange` (tipo: topic)
- Queue: `order.created.queue`
- Binding con routing key: `order.created`

## PASO 6: Pruebas de Funcionamiento

### 6.1 Obtener Token JWT

```bash
$token = (curl -X POST http://localhost:8080/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","password":"admin123"}' | ConvertFrom-Json).token

echo $token
```

### 6.2 Crear una Orden (Publica Evento)

```bash
curl -X POST http://localhost:8080/api/orders `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "productName":"MacBook Pro",
    "quantity":1,
    "price":2499.99,
    "status":"PENDING"
  }'
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "productName": "MacBook Pro",
  "quantity": 1,
  "price": 2499.99,
  "status": "PENDING"
}
```

**Verificación**: El evento debe aparecer en RabbitMQ o en los logs de notification-service

### 6.3 Obtener Todas las Órdenes

```bash
curl -X GET http://localhost:8080/api/orders `
  -H "Authorization: Bearer $token"
```

### 6.4 Obtener Orden Específica

```bash
curl -X GET http://localhost:8080/api/orders/1 `
  -H "Authorization: Bearer $token"
```

### 6.5 Actualizar Orden

```bash
curl -X PUT http://localhost:8080/api/orders/1 `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "productName":"MacBook Pro M3",
    "quantity":2,
    "price":2999.99,
    "status":"CONFIRMED"
  }'
```

### 6.6 Eliminar Orden

```bash
curl -X DELETE http://localhost:8080/api/orders/1 `
  -H "Authorization: Bearer $token"
```

## PASO 7: Verificar Eventos en RabbitMQ

### Via Management Console

1. Ir a http://localhost:15672
2. Ir a "Queues" 
3. Click en "order.created.queue"
4. Scroll down a "Get messages"
5. Click en "Get Message(s)"
6. Debería mostrar el JSON del evento OrderCreatedEvent

### Via Logs

Buscar en los logs del order-service:

```
[order-service] Evento publicado: OrderCreated [id=1, productName=MacBook Pro]
```

Buscar en los logs del notification-service:

```
=== NOTIFICACIÓN RECIBIDA ===
Nueva orden creada:
  ID:       1
  Nombre:   MacBook Pro
  ...
=============================
```

## PASO 8: Verificar Tracing (Zipkin - Opcional)

```bash
# Ver logs con traceId (sin Docker): observa la consola donde corre `order-service`
# o ejecuta `mvn -pl order-service spring-boot:run` y busca `traceId` en los logs.
```

O acceder a Zipkin en http://localhost:9411

## Consigna Completada ✅

| Requisito | Estado | Detalles |
|-----------|--------|----------|
| Crear microservicio nuevo | ✅ | order-service con 5 endpoints REST |
| Registrar en Eureka | ✅ | Configurado con @EnableDiscoveryClient |
| Integrar en API Gateway | ✅ | Ruta /api/orders/** → lb://order-service |
| Asegurar con JWT | ✅ | Todos los endpoints requieren token |
| Publicar eventos | ✅ | OrderCreatedEvent a RabbitMQ |
| Consumir eventos | ✅ | Listo para notification-service |
| Entidad persistida | ✅ | Table ORDERS en BD H2 |
| Tracing Zipkin | ✅ | Configurado en application.yml |
| Logs con traceId | ✅ | Pattern de logging configu

rado |
| RabbitMQ | ✅ | Exchange topic, queue, binding |

## Troubleshooting

### Error: "Failed to connect to Eureka"
- Verifica que Eureka Server está corriendo en puerto 8761
- Comprueba la conectividad de red

### Error: "Invalid JWT token"
- Regenera el token con el endpoint /auth/login
- Verifica que el JWT_SECRET es consistente en todos los servicios

### Error: "RabbitMQ connection refused"
- Verifica que RabbitMQ está corriendo:
  ```bash
  # Verifica que RabbitMQ está corriendo en localhost:5672 (p. ej. con telnet o herramientas del SO)
  ```

### "order-service not appearing in Eureka"
- Espera 30 segundos (tiempo por defecto de registro)
-- Verifica los logs en la consola donde corre `order-service`

### No hay eventos en RabbitMQ
- Verifica que order-service está corriendo con perfil `rabbitmq`
- Comprueba que el POST /api/orders se completó exitosamente (status 201)

## Parar Servicios

<!-- Instrucciones Docker removidas: para parar los servicios locales, cerrá las terminales donde corren o usá Ctrl+C en cada `mvn spring-boot:run`. -->

### Con Maven
```
Ctrl+C en cada terminal
```

## Archivos Generados

- `order-service/pom.xml` - Configuración Maven
- `order-service/src/main/java/com/uade/order/` - Código fuente
-- `order-service/src/main/resources/` - Configuración de aplicación
<!-- Los Dockerfiles y la configuración de Docker fueron eliminados; el repositorio está preparado para ejecución local. -->
-- Actualizado `pom.xml` - Nuevo módulo agregado
-- Actualizado `api-gateway/application.yml` - Nueva ruta

## Referencias Útiles

- Documentación Spring Cloud: https://spring.io/projects/spring-cloud
- Spring Security OAuth2: https://spring.io/projects/spring-security
- RabbitMQ: https://www.rabbitmq.com/
- Spring Data JPA: https://spring.io/projects/spring-data-jpa

## Consigna Original

[Ver documento PDF adjunto]

---

**Fecha de ejecución**: Mayo 2026  
**Estado**: ✅ COMPLETADO

## Ejecutar sin Docker (script automático)

Si preferís ejecutar todo localmente, creé un script PowerShell `run-local.ps1` en la raíz del repositorio que abre ventanas separadas de `cmd` para cada servicio.

Requisitos previos (locales):
- Tener `Java` y `Maven` en el `PATH`.
- Tener `RabbitMQ` corriendo localmente en `localhost:5672` (o iniciar RabbitMQ manualmente).

Cómo usar el script:

```powershell
# Permitir ejecución de scripts (una sola vez)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Ejecutar el script desde la raíz del repositorio
cd C:\Programas\arquitecturaaplicaciones_microservicios
./run-local.ps1
```

El script hace lo siguiente:
- Verifica que Java y Maven estén disponibles (avisa si no lo están).
- Comprueba si RabbitMQ responde en `localhost:5672` y muestra instrucciones si no está disponible.
- Abre ventanas de `cmd` con `mvn -pl <modulo> spring-boot:run` para los siguientes módulos (en ese orden):
  `config-server`, `eureka-server`, `auth-service`, `api-gateway`, `inventory-service` (perfil `rabbitmq`), `notification-service` (perfil `rabbitmq`), `order-service` (perfil `rabbitmq`).

Notas:
- Si no querés usar RabbitMQ local, podés editar `run-local.ps1` o arrancar `inventory-service`, `notification-service` y `order-service` sin el argumento `--spring.profiles.active=rabbitmq` para usar el adaptador NoOp.
- El script asume un entorno Windows; si usás Linux/macOS, ejecutá los comandos `mvn -pl <modulo> spring-boot:run` en terminales separadas manualmente.

