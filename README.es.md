# PriceTracker

[English version](README.md) | Espanol

PriceTracker es una solucion backend desarrollada en Java 21 con Spring Boot orientada al rastreo, monitoreo historico y evaluacion de variaciones de precios de productos en tiendas de comercio electronico (enfocado inicialmente en librerias y tiendas online como BuscaLibre y Libreria Palito).

El sistema permite registrar productos a partir de su enlace web, extraer automaticamente su informacion y precio actual mediante tecnicas de web scraping y parsing de datos embebidos, registrar la evolucion de precios en el tiempo a traves de procesos batch programados, y gestionar usuarios con suscripciones y condiciones de alerta personalizadas.

---

## Tabla de Contenidos

- Descripcion General
- Arquitectura del Sistema
- Patrones de Diseno
- Decisiones Tecnicas y de Arquitectura
- Modelo de Dominio y Datos
- Implementacion de Docker
- Configuracion y Variables de Entorno
- Guia de Instalacion y Lanzamiento
- Documentacion de la API y Endpoints
- Tareas Programadas y Background Jobs

---

## Descripcion General

El proposito fundamental de PriceTracker es resolver el problema de la volatilidad de precios en mercados digitales, permitiendo a los usuarios automatizar el seguimiento de productos y recibir notificaciones cuando se cumplan condiciones predefinidas (por ejemplo, cuando el precio baja respecto a un umbral determinado).

### Capacidades Principales

- Extraccion automatica de metadatos de productos (nombre, descripcion, precio, moneda) a partir de una URL soportada.
- Orquestacion desacoplada de scrapers por tienda mediante arquitectura extensible.
- Consultas avanzadas y dinamicas sobre el catalogo de productos utilizando JPA Specifications.
- Actualizacion periodica y desatendida de precios mediante tareas programadas (Spring Scheduler).
- Aislamiento transaccional para garantizar tolerancia a fallos en procesamiento por lotes.
- Manejo uniforme de errores bajo el estandar RFC 7807 (Problem Details).
- Entorno de desarrollo autocontenido con Docker, PostgreSQL y MailHog.

---

## Arquitectura del Sistema

El proyecto esta estructurado siguiendo una arquitectura por capas modular con enfoque orientado al dominio (Package by Feature / DDD pragmatico). Cada modulo agrupa sus entidades, repositorios, servicios, DTOs, mappers y controladores asociados.

```
com.eduardomango.pricetracker
|-- common
|   |-- architecture.scraping     # Clientes de scraping, interfaces y orquestador
|   |   `-- adapters              # Adaptadores especificos por tienda (BuscaLibre, LibreriaPalito)
|   |-- config                    # Configuracion de WebClient y beans transversales
|   |-- exceptions                # Jerarquia de excepciones de negocio
|   |-- handlers                  # Controlador global de excepciones (RFC 7807)
|   `-- model                     # Value Objects (Price, Email) y contratos base (IMapper)
|-- product
|   |-- domain                    # Entidad ProductEntity, Value Objects (URL, AlertCondition) y DTOs
|   |-- mapper                    # Mappers MapStruct
|   |-- ProductController         # Endpoints REST de catalogo
|   |-- ProductService            # Logica de negocio de productos
|   |-- ProductSpecification      # Predicados JPA dinamicos
|   `-- ProductRepository         # Acceso a datos de productos
|-- pricehistory
|   |-- domain                    # PriceHistoryEntity para trazabilidad historica
|   |-- PriceHistoryService       # Tareas programadas (@Scheduled)
|   |-- PriceUpdateService        # Transacciones aisladas por producto (REQUIRES_NEW)
|   `-- PriceHistoryRepository    # Acceso a datos de historial
|-- user
|   |-- domain                    # UserEntity, DTOs y mappers
|   |-- UserController            # Endpoints REST de usuarios
|   |-- UserService               # Logica de negocio de usuarios
|   `-- UserRepository            # Acceso a datos de usuarios
|-- subscription
|   |-- domain                    # SubscriptionEntity y reglas de asociacion
|   `-- SubscriptionRepository    # Acceso a datos de suscripciones
`-- notification
    `-- EmailService              # Infraestructura de envio de notificaciones
```

### Flujo de Datos

```
[Cliente HTTP / Frontend]
          |
          v
  [Controladores REST]
          |
          v
  [Capa de Servicio]
     |          |
     |          +---> [ClientOrchestrator] ---> [ClientService / Adapters] ---> [Tiendas Externas]
     v
[JPA Repositories]
     |
     v
[PostgreSQL Database]
```

---

## Patrones de Diseno

En el desarrollo de la aplicacion se implementaron patrones de diseno clasicos y de arquitectura de software:

### 1. Strategy Pattern (Estrategia de Scraping)
- **Componentes**: `ClientService` (interfaz), `BuscaLibreClient`, `LibreriaPalitoClient` (estrategias concretas).
- **Funcionamiento**: Cada proveedor web implementa su propia logica de extraccion de datos, selectores CSS, regex y parsing de JSON embebido (`dataLayer` o scripts de detalle). Todos comparten el contrato definido en `ClientService`:
  - `boolean supports(URI url)`: Determina si el adaptador puede procesar el dominio solicitado.
  - `ProductEntity getProduct(URI url)`: Realiza la extraccion inicial completa.
  - `Price getPrice(URI url)`: Realiza extracciones ligeras para actualizaciones recurrentes.

### 2. Orchestrator / Dispatcher Pattern
- **Componente**: `ClientOrchestrator`.
- **Funcionamiento**: Spring inyecta automaticamente una lista de todos los beans que implementan `ClientService` (`List<ClientService>`). El orquestador recibe la URL, consulta a cada cliente mediante `supports(url)` y delega la ejecucion a la estrategia correspondiente de manera transparente para el resto de la aplicacion. Permite incorporar nuevas tiendas sin modificar el codigo consumidor (abierto a extension, cerrado a modificacion).

### 3. Specification Pattern
- **Componente**: `ProductSpecification`.
- **Funcionamiento**: Utiliza la API `PredicateSpecification` de Spring Data JPA para construir consultas SQL dinamicas y componibles (`nameContains`, `nameEquals`, `descriptionContains`, `priceGreaterThan`, `priceLesserThan`). Si un filtro no es suministrado por el cliente (valor nulo o vacio), se resuelve como `conjunction()`, evitando la construccion manual de cadenas SQL o metodos de repositorio redundantes.

### 4. Value Object Pattern (Domain-Driven Design)
- **Componentes**: `Price`, `URL`, `Email`, `AlertCondition`.
- **Funcionamiento**: Se modelan como Java `record`s inmutables marcados con `@Embeddable`. Cuentan con validacion intrinseca en su constructor compacto:
  - `Price`: Garantiza que el monto sea estrictamente mayor a cero y estandariza el redondeo a 2 decimales (`HALF_UP`).
  - `URL`: Valida sintaxis URI y esquemas permitidos (`http` / `https`).
  - `Email`: Aplica una expresion regular estricta para asegurar consistencia en las direcciones de correo.
  - `AlertCondition`: Encapsula la logica de negocio para determinar si un precio actual satisface la condicion mediante `isSatisfiedBy(Price currentPrice)`.

### 5. DTO y Mapper Pattern
- **Componentes**: `ProductRequest`, `ProductResponse`, `UserDTO`, `NewUserDTO`, `PriceDTO`, interfaces basadas en MapStruct (`ProductResponseMapper`, `UserMapper`, etc.).
- **Funcionamiento**: Separa el modelo persistente de base de datos de los contratos de entrada y salida expuestos en la API publica, previniendo sobreexposicion de atributos internos y ciclos de serializacion.

---

## Decisiones Tecnicas y de Arquitectura

### Desacoplamiento de I/O de Red y Transacciones de Base de Datos
En la ejecucion de la tarea programada de actualizacion de precios (`PriceHistoryService`), el scraping web (operacion bloqueante de red) se ejecuta estrictamente **fuera** de cualquier transaccion de base de datos. Esto previene retener conexiones del pool HikariCP durante llamadas HTTP lentas o inestables, evitando la degradacion del rendimiento de la base de datos.

### Aislamiento Transaccional y Evasion del Bypass de Proxy de Spring
Para la persistencia de las actualizaciones de precio se creo una clase dedicada (`PriceUpdateService`) con el metodo:
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void updateSingleProduct(ProductEntity product, Price newPrice)
```
- **Motivo de `REQUIRES_NEW`**: Cada producto se actualiza en su propia transaccion independiente. Si la operacion falla para un producto, no invalida ni revierte los cambios de los demas productos del lote.
- **Motivo de la clase separada**: Spring implementa AOP mediante proxies dinamicos. Si un metodo de un bean invoca internamente a otro metodo transaccional de la misma clase (self-invocation), la llamada no pasa a traves del proxy y la transaccion no se inicia. Mover la logica a un servicio separado garantiza la intercepcion correcta de la anotacion.

### Separacion de Identificador Interno y Externo (UUID Publico)
Las entidades poseen dos identificadores:
- `id` (`Long`, autoincremental): Clave primaria interna para indexacion de alto rendimiento y relaciones de clave foranea en PostgreSQL.
- `externalId` (`UUID`): Identificador publico generado automaticamente en `@PrePersist`. Todos los endpoints de la API REST consumen y exponen exclusivamente este UUID, evitando ataques de enumeracion y protegiendo informacion sensible sobre el volumen de registros.

### Adopcion del Estandar RFC 7807 para Respuestas de Error
El controlador global de excepciones (`GlobalExceptionHandler`) utiliza la clase `ProblemDetail` nativa de Spring, estructurando las respuestas de error en formato estandarizado JSON (`type`, `title`, `status`, `detail`, `instance`, junto a propiedades extendidas como `timestamp`, `path` y `method`).

---

## Modelo de Dominio y Datos

El modelo relacional esta compuesto por las siguientes entidades principales:

```
+--------------------+            +------------------------+
|      products      | 1        * |     price_history      |
+--------------------+------------+------------------------+
| id (PK, Long)      |            | id (PK, Long)          |
| external_id (UUID) |            | product_id (FK)        |
| name               |            | current_price (amount) |
| url (value)        |            | currency               |
| current_price      |            | checked_at             |
| currency           |            +------------------------+
| description        |
| status             |            +------------------------+
| last_checked       | 1        * |     subscriptions      |
| created_at         |------------+------------------------+
| updated_at         |            | id (PK, Long)          |
+--------------------+            | product_id (FK)        |
                                  | user_id (FK)           |
+--------------------+            | target_price           |
|       users        | 1        * | target_currency        |
+--------------------+------------+ comparison_type        |
| id (PK, Long)      |            | active                 |
| external_id (UUID) |            +------------------------+
| email_address      |
+--------------------+
```

### Enumeraciones y Tipos de Estado

- **ProductStatus**: `ACTIVE`, `INACTIVE`.
- **ComparisonType**: `LOWER_THAN`, `HIGHER_THAN`, `EQUALS`.

---

## Implementacion de Docker

El proyecto cuenta con un entorno contenerizado completo para ejecucion homogenea y despliegue reproducible.

### Dockerfile Multi-Stage

El archivo `Dockerfile` divide el proceso en dos etapas para optimizar el peso final de la imagen y reforzar la seguridad:

```dockerfile
# Fase 1: Compilacion y empaquetado
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Fase 2: Entorno de ejecucion minimo
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- **Optimizacion de Cache**: Se copia `pom.xml` y se ejecuta `dependency:go-offline` antes de copiar el codigo fuente. Si el codigo cambia pero las dependencias no, Docker reutiliza la capa descargada.
- **Imagen Base Reducida**: La etapa de runtime emplea Alpine Linux con Eclipse Temurin 21 JRE, reduciendo el consumo de almacenamiento y eliminando herramientas de compilacion del contenedor final.

### Docker Compose

El archivo `docker-compose.yml` orquesta los tres servicios requeridos por la arquitectura:

1. **`db` (PostgreSQL 16)**:
   - Base de datos relacional del sistema.
   - Persistencia asegurada mediante el volumen nominal `postgres_data`.
   - Expuesto en el puerto `5432`.
2. **`app` (PriceTracker)**:
   - Aplicacion Spring Boot construida a partir del `Dockerfile`.
   - Depende del contenedor `db`.
   - Expuesto en el puerto `8080`.
3. **`mailhog` (MailHog)**:
   - Servidor SMTP ficticio para interceptacion y visualizacion de correos electronicos de prueba.
   - Puerto `1025`: Protocolo SMTP.
   - Puerto `8025`: Interfaz web de usuario para inspeccionar correos.

---

## Configuracion y Variables de Entorno

La aplicacion define valores predeterminados para desarrollo local y permite sobreescribirlos mediante variables de entorno:

| Variable | Descripcion | Valor por Defecto |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | URL JDBC de conexion a PostgreSQL | `jdbc:postgresql://localhost:5432/pricetracker` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de base de datos | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contrasena de base de datos | `postgres` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia de generacion DDL | `update` |
| `SPRING_MAIL_HOST` | Host del servidor SMTP | `mailhog` |
| `SPRING_MAIL_PORT` | Puerto del servidor SMTP | `1025` |

---

## Guia de Instalacion y Lanzamiento

### Requisitos Previos

- Docker y Docker Compose instalados.
- Alternativamente para desarrollo local sin Docker:
  - Java 21 JDK.
  - Maven 3.9+ (o utilizar el wrapper `./mvnw`).
  - Instancia local de PostgreSQL corriendo en el puerto 5432.

### Opcion A: Lanzamiento Rapido con Docker Compose (Recomendado)

1. Clonar el repositorio y situarse en la raiz del proyecto:
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd PriceTracker
   ```

2. Construir e iniciar todos los servicios:
   ```bash
   docker compose up --build
   ```

3. Verificar que los tres contenedores esten saludables:
   - API de PriceTracker: `http://localhost:8080`
   - Documentacion Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Interfaz MailHog: `http://localhost:8025`

4. Para detener los servicios preservando datos:
   ```bash
   docker compose down
   ```

### Opcion B: Ejecucion Local en Modo Desarrollo

1. Iniciar unicamente la base de datos y MailHog via Docker:
   ```bash
   docker compose up -d db mailhog
   ```

2. Compilar la aplicacion:
   ```bash
   ./mvnw clean compile
   ```

3. Ejecutar la aplicacion con Spring Boot:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Ejecutar la suite de pruebas unitarias y de integracion:
   ```bash
   ./mvnw test
   ```

---

## Documentacion de la API y Endpoints

La documentacion interactiva se encuentra disponible de forma automatica mediante OpenAPI 3.0 y Swagger UI en:
- Interfaz Swagger: `http://localhost:8080/swagger-ui.html`
- Definicion OpenAPI JSON: `http://localhost:8080/v3/api-docs`

A continuacion se detallan los recursos principales:

### 1. Gestion de Productos (`/products`)

#### Registrar un Producto
Inicia el scraping del sitio web, extrae metadatos y almacena el producto con su precio actual.
```http
POST /products
Content-Type: application/json

{
  "name": "Libro de Ejemplo",
  "url": "https://www.buscalibre.com.ar/libro-ejemplo/p/123456"
}
```

#### Listar Productos con Filtros Dinamicos
Permite combinar filtros opcionales de busqueda:
```http
GET /products?name=quijote&minPrice=1000&maxPrice=15000
```
Parametros disponibles:
- `name` (string, opcional): Coincidencia parcial no sensible a mayusculas.
- `nameMatch` (string, opcional): Coincidencia exacta no sensible a mayusculas.
- `description` (string, opcional): Coincidencia parcial en la descripcion.
- `minPrice` (decimal, opcional): Precio minimo.
- `maxPrice` (decimal, opcional): Precio maximo.

#### Consultar Producto por ID Externo
```http
GET /products/{productId}
```
Donde `{productId}` corresponde al UUID publico retornado al crear el producto.

#### Eliminar un Producto
```http
DELETE /products/{productId}
```

---

### 2. Gestion de Usuarios (`/users`)

#### Crear Usuario
```http
POST /users
Content-Type: application/json

{
  "email": "usuario@ejemplo.com"
}
```

#### Listar Todos los Usuarios
```http
GET /users
```

#### Consultar Usuario por ID Externo
```http
GET /users/{userId}
```

#### Actualizar Correo de Usuario
```http
PUT /users/{userId}
Content-Type: application/json

{
  "email": "nuevo_correo@ejemplo.com"
}
```

#### Eliminar Usuario
```http
DELETE /users/{userId}
```

---

## Tareas Programadas y Background Jobs

La aplicacion incluye soporte para ejecucion automatica de tareas en segundo plano mediante la anotacion `@EnableScheduling`.

### Cron de Actualizacion de Precios
Ubicado en `PriceHistoryService.updatePriceHistory()`:
- **Expresion Cron**: `0 0 9,17 * * *`
- **Frecuencia**: Se ejecuta dos veces al dia, a las 09:00 y a las 17:00 horas.
- **Mecanismo de Ejecucion**:
  1. Recupera la lista de productos activos registrados en el sistema.
  2. Para cada producto, invoca al cliente de scraping correspondiente para consultar el valor actual del mercado.
  3. Registra una nueva entrada en `PriceHistoryEntity` con la marca temporal del chequeo.
  4. Actualiza el `currentPrice` y `lastChecked` en `ProductEntity`.
  5. En caso de error de red o cambios en el DOM del proveedor, captura la excepcion individualmente y continua con el siguiente elemento del lote sin interrumpir la secuencia.

---

## Licencia

Este proyecto esta licenciado bajo los terminos de la Licencia MIT. Para mas detalles, consulte el archivo [LICENSE](LICENSE).

