# 🚗 SGIVU - Servicio de Vehículos

## 📘 Descripción

Microservicio encargado de centralizar el inventario de vehículos de SGIVU (autos y motocicletas). Expone APIs REST
para registrar, consultar, actualizar, eliminar y cambiar el estado operativo de los vehículos que pueden ser
asignados a clientes o flotas internas.

## 🧱 Arquitectura y Rol

* Tipo: Microservicio Spring Boot / Spring Cloud orientado a gestión de catálogo vehicular.
* Interactúa con: `sgivu-config`, `sgivu-discovery`, `sgivu-gateway` y `sgivu-auth` para configuración, registro y
  autenticación.
* Expone controladores REST separados (`/v1/cars`, `/v1/motorcycles`) con operaciones CRUD, búsqueda y métricas rápidas.
* Persiste la jerarquía `vehicles` / `cars` / `motorcycles` mediante JPA con estrategia JOINED sobre PostgreSQL.
* Carga esquemas y datos semilla desde `src/main/resources/database/schema.sql` y `data.sql` para entornos de desarrollo.
* `GlobalExceptionHandler` entrega respuestas JSON uniformes ante errores de validación, integridad o autorización.

## ⚙️ Tecnologías

* **Lenguaje:** Java 21 (Amazon Corretto).
* **Framework:** Spring Boot 3.5.7, Spring Cloud 2025.0.0.
* **Seguridad:** Spring Security, OAuth 2.1 Resource Server, validación de JWT (claim `rolesAndPermissions`).
* **Persistencia:** Spring Data JPA, PostgreSQL, scripts `schema.sql` y `data.sql`.
* **Infraestructura y utilitarios:** Spring Boot Actuator, Lombok, Jakarta Bean Validation, Docker.

## 🚀 Ejecución Local

1. Ubicarse en `sgivu-vehicle` y compilar el proyecto:

   ```bash
   ./mvnw clean package
   ```

2. Levantar las dependencias básicas:
   * Config Server (`sgivu-config`) con la configuración del servicio.
   * Eureka (`sgivu-discovery`) para el registro.
   * Authorization Server (`sgivu-auth`) para emitir los JWT.
   * PostgreSQL con la base `sgivu_vehicle_db`; ejecutar `database/schema.sql` y `database/data.sql` si se desea contar
     con datos iniciales.

3. Crear un `application-local.yml` (o variables de entorno equivalentes) con los parámetros mínimos:

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/sgivu_vehicle_db
       username: sgivu
       password: sgivu
     jpa:
       hibernate:
         ddl-auto: none
     sql:
       init:
         mode: never # usar "always" solo si se quiere aplicar schema/data.sql automáticamente
   server:
     port: 8083
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka
   services:
     map:
       sgivu-auth:
         url: http://localhost:9000
   ```

4. Ejecutar el microservicio apuntando al perfil deseado:

   ```bash
   SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
   ```

5. Consumir las APIs desde `http://localhost:8083` o mediante `sgivu-gateway`. Todos los endpoints (excepto actuator) exigen
   token Bearer emitido por `sgivu-auth`.

## 🔗 Endpoints Principales

```text
POST   /v1/cars                           -> Crea un automóvil.
GET    /v1/cars/{id}                      -> Consulta por identificador.
GET    /v1/cars                           -> Lista completa de autos.
GET    /v1/cars/page/{page}               -> Catálogo paginado (page size = 10).
PUT    /v1/cars/{id}                      -> Actualiza atributos propios del automóvil.
DELETE /v1/cars/{id}                      -> Elimina un auto del inventario.
PATCH  /v1/cars/{id}/status               -> Cambia el estado (`VehicleStatus`).
GET    /v1/cars/count                     -> Métricas rápidas (total/available/unavailable).
GET    /v1/cars/search?...                -> Búsqueda por plate, brand, line, model, fuelType, bodyType.

POST   /v1/motorcycles                    -> Crea una motocicleta.
GET    /v1/motorcycles/{id}               -> Consulta por identificador.
GET    /v1/motorcycles                    -> Lista completa de motos.
GET    /v1/motorcycles/page/{page}        -> Catálogo paginado (page size = 10).
PUT    /v1/motorcycles/{id}               -> Actualiza atributos propios de la motocicleta.
DELETE /v1/motorcycles/{id}               -> Elimina una moto del inventario.
PATCH  /v1/motorcycles/{id}/status        -> Cambia el estado (`VehicleStatus`).
GET    /v1/motorcycles/count              -> Métricas rápidas (total/available/unavailable).
GET    /v1/motorcycles/search?...         -> Búsqueda por plate, brand, line, model, motorcycleType.

GET    /actuator/health|info              -> Endpoints públicos para chequeos.
```

* Los filtros de `/search` aceptan múltiples parámetros opcionales y combinan resultados sin duplicados.
* Los `PATCH` esperan el payload mínimo (enum `VehicleStatus` para status).

## 🔐 Seguridad

* Opera como Resource Server validando JWT emitidos por `sgivu-auth`, cuya URL se obtiene de `services.map.sgivu-auth.url`.
* Convierte el claim `rolesAndPermissions` en `SimpleGrantedAuthority` para aplicar reglas `@PreAuthorize`.
* Permisos esperados: `car:create|read|update|delete` y `motorcycle:create|read|update|delete`.
* `GlobalExceptionHandler` retorna respuestas con códigos `403/409/500` ante denegaciones o violaciones de integridad.
* `GET /actuator/health` y `GET /actuator/info` permanecen abiertos para monitoreo; el resto de endpoints requiere token.

## 🧩 Dependencias

* `sgivu-config` para configuración centralizada (datasource, URLs, secretos).
* `sgivu-discovery` para registrar y descubrir instancias vía Eureka.
* `sgivu-gateway` como punto de entrada público a las APIs.
* `sgivu-auth` para emisión y validación de tokens OAuth 2.1/OpenID Connect.
* Base de datos PostgreSQL (`sgivu_vehicle_db`) con la estructura JOINED (vehicles/cars/motorcycles).

## 🧮 Dockerización

* Imagen: `sgivu-vehicle` construida sobre `amazoncorretto:21-alpine-jdk`.
* Puerto expuesto: `8083/tcp`.
* Ejemplo de build & run:

  ```bash
  ./mvnw clean package -DskipTests
  docker build -t sgivu-vehicle .
  docker run --rm -p 8083:8083 \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e SPRING_CONFIG_IMPORT=configserver:http://sgivu-config:8888 \
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/sgivu_vehicle_db \
    -e SPRING_DATASOURCE_USERNAME=sgivu \
    -e SPRING_DATASOURCE_PASSWORD=sgivu \
    -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://sgivu-discovery:8761/eureka \
    -e SERVICES_MAP_SGIVU_AUTH_URL=http://sgivu-auth:9000 \
    sgivu-vehicle
  ```

## ☁️ Despliegue en AWS

* Publicar la imagen en Amazon ECR (o repositorio equivalente) desde los pipelines.
* Desplegar en ECS/Fargate, EKS o EC2 asegurando conectividad privada hacia `sgivu-config`, `sgivu-discovery` y `sgivu-auth`.
* Aprovisionar PostgreSQL gestionado (AWS RDS) y aplicar `schema.sql` durante el bootstrap inicial.
* Inyectar credenciales y URLs mediante AWS Secrets Manager / SSM Parameter Store (`SPRING_CONFIG_IMPORT`,
  `SPRING_DATASOURCE_*`, `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`, `SERVICES_MAP_SGIVU_AUTH_URL`).
* Exponer el servicio únicamente vía `sgivu-gateway` detrás de un ALB/NLB con políticas de seguridad y observabilidad activas.

## 📊 Monitoreo

* Actuator habilita `health`, `info`, `metrics` y `prometheus` (según configuración del Config Server).
* Logs estructurados listos para agregarse en CloudWatch, ELK o Loki.
* Compatible con Micrometer/Zipkin; activar `management.tracing.enabled=true` y configurar `management.zipkin.tracing.endpoint`
  cuando se requiera trazabilidad distribuida.

## ✨ Autor

* **Steven Ricardo Quiñones**
