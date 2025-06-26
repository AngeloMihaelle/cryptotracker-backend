
# 📈 CryptoTracker Web — Backend Service (Versión 2.0)

Microservicio Java REST que obtiene, almacena y expone el precio de las principales criptomonedas usando datos de CoinGecko y una base de datos PostgreSQL para persistencia y filtrado.

Este backend ligero utiliza el servidor HTTP embebido de Java, integra consultas a CoinGecko, y almacena precios históricos en base de datos para consulta, generación de gráficos y regresión lineal.

---

## Descripción

Este proyecto provee un servidor HTTP que:

* Obtiene precios en vivo de criptomonedas desde CoinGecko para un conjunto activo configurado en base de datos.
* Almacena el historial de precios en PostgreSQL.
* Expone endpoints REST para:

  * Verificar estado del servidor (health check).
  * Consultar los últimos precios de criptomonedas activas.
  * Consultar historial de precios filtrado por símbolo y rango horario.
  * Forzar actualización de precios.
  * Generar gráficos de precios y regresiones lineales en formato PNG.
  * Servir archivos estáticos para front-end o documentación.

Las criptomonedas activas y su información (símbolo, nombre, logo, estado activo) se gestionan en la base de datos.

---

## Características principales

* **Persistencia en PostgreSQL:** para mantener histórico y estado actualizado.
* **Filtrado por criptomonedas activas:** solo datos relevantes se consultan y almacenan.
* **Mapeo actualizado con nuevas criptos:** TRX, HYPE, BCH, LINK agregadas al catálogo.
* **Endpoints para gráficos:** se generan imágenes PNG con JFreeChart para precios y regresiones.
* **Servicios REST completos:** para manejo de precios, historial, gráficos, y actualización manual.
* **Separación clara de capas:** handlers HTTP, servicio de integración con CoinGecko, y acceso a base de datos.
* **Configuración vía variables de entorno:** URL, usuario y contraseña de BD.

---

## Tecnologías usadas

* Java 17+ (recomendado)
* PostgreSQL
* OkHttp para llamadas HTTP a CoinGecko
* Gson para parsing JSON
* JFreeChart para generación de gráficos
* Apache Commons IO para manejo de streams
* JDBC para conexión y consultas a base de datos
* Servidor HTTP embebido `com.sun.net.httpserver.HttpServer`

---

## Instalación y ejecución

1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd backend
```

2. Configurar variables de entorno para conexión a PostgreSQL

```bash
export DB_URL="jdbc:postgresql://host:port/dbname"
export DB_USER="usuario"
export DB_PASSWORD="contraseña"
```

3. Construir con Maven

```bash
mvn clean package
```

4. Ejecutar el servidor

```bash
java -cp target/backend-1.0-SNAPSHOT.jar com.backend.WebServer
```

El servidor se ejecutará por defecto en el puerto **8080**.

---

## Endpoints disponibles

### 1. Estado del servidor (Health Check)

* **URL:** `/api/health`
* **Método:** `GET`
* **Respuesta:**

```json
{
  "status": "ok"
}
```

---

### 2. Actualizar precios (Forzar actualización)

* **URL:** `/api/update-prices`
* **Método:** `POST`
* **Descripción:** Fuerza la actualización de precios desde CoinGecko y almacena en base de datos.
* **Respuesta:** Texto simple indicando resultado.

---

### 3. Últimos precios

* **URL:** `/api/prices/latest`
* **Método:** `GET`
* **Descripción:** Devuelve últimos precios almacenados para criptomonedas activas.
* **Respuesta:** Array JSON con objetos que incluyen símbolo, nombre, logo, precio y timestamp.

---

### 4. Historial de precios

* **URL:** `/api/prices/history/{simbolo}?hours={horas}`
* **Método:** `GET`
* **Parámetros:**

  * `simbolo` (ruta): símbolo de la criptomoneda (ej. BTC, ETH)
  * `hours` (query opcional): horas de histórico a consultar (por defecto 24)
* **Descripción:** Devuelve histórico de precios en rango definido.
* **Respuesta:** JSON con lista de objetos `{ timestamp, price }`

---

### 5. Gráfico simple de precios (PNG)

* **URL:** `/api/chart/single/{simbolo}?hours={horas}`
* **Método:** `GET`
* **Descripción:** Genera y devuelve gráfico PNG de precios para la cripto y rango dado.
* **Respuesta:** Imagen PNG

---

### 6. Gráfico de regresión lineal (PNG)

* **URL:** `/api/chart/regression/{simbolo}?start=HH:mm&end=HH:mm`
* **Método:** `GET`
* **Descripción:** Genera gráfico PNG con línea de regresión lineal para precios dentro del rango horario.
* **Parámetros:** Horas en formato 24h, ej. `start=09:00&end=18:00`
* **Respuesta:** Imagen PNG

---

### 7. Servir archivos estáticos

* **URL:** `/` o rutas de archivos (ej. `/index.html`, `/css/style.css`)
* **Método:** `GET`
* **Descripción:** Sirve archivos estáticos embebidos en el jar o recursos.

---
Claro, aquí te dejo el README actualizado con una sección **completa de documentación para usar todos los endpoints** con ejemplos de petición y respuesta:

---

## Documentación de Endpoints REST

---

### 1. Estado del servidor (Health Check)

* **URL:** `/api/health`
* **Método:** `GET`
* **Descripción:** Verifica que el servidor está activo y funcionando.
* **Ejemplo de petición:**

```bash
curl -X GET http://localhost:8080/api/health
```

* **Respuesta esperada (JSON):**

```json
{
  "status": "ok"
}
```

---

### 2. Actualizar precios (Forzar actualización)

* **URL:** `/api/update-prices`
* **Método:** `POST`
* **Descripción:** Forzar que el backend recupere los precios actuales de las criptomonedas activas desde CoinGecko y actualice la base de datos.
* **Ejemplo de petición:**

```bash
curl -X POST http://localhost:8080/api/update-prices
```

* **Respuesta esperada (texto plano):**

```
Actualización de precios forzada
```

* **Código HTTP:** `200 OK` si se actualiza correctamente; `405 Method Not Allowed` para otros métodos.

---

### 3. Últimos precios

* **URL:** `/api/prices/latest`
* **Método:** `GET`
* **Descripción:** Obtiene los últimos precios almacenados de las criptomonedas activas.
* **Ejemplo de petición:**

```bash
curl -X GET http://localhost:8080/api/prices/latest
```

* **Respuesta esperada (JSON):**

```json
[
  {
    "symbol": "BTC",
    "name": "Bitcoin",
    "logo_url": "https://assets.coingecko.com/coins/images/1/large/bitcoin.png",
    "current_price": 30000.1234,
    "last_updated": "2025-06-26T10:15:00Z"
  },
  {
    "symbol": "ETH",
    "name": "Ethereum",
    "logo_url": "https://assets.coingecko.com/coins/images/279/large/ethereum.png",
    "current_price": 1900.5678,
    "last_updated": "2025-06-26T10:15:00Z"
  }
  // ...más criptomonedas
]
```

---

### 4. Historial de precios

* **URL:** `/api/prices/history/{symbol}?hours={hours}`
* **Método:** `GET`
* **Parámetros:**

  * `symbol` (en la ruta): símbolo de la criptomoneda (ej. BTC, ETH)
  * `hours` (query param, opcional): rango en horas para obtener el histórico (por defecto 24)
* **Descripción:** Obtiene el historial de precios en USD para la cripto especificada en el rango indicado.
* **Ejemplo de petición:**

```bash
curl -X GET "http://localhost:8080/api/prices/history/BTC?hours=48"
```

* **Respuesta esperada (JSON):**

```json
[
  {
    "timestamp": 1687747200000,
    "price": 29950.45
  },
  {
    "timestamp": 1687750800000,
    "price": 30010.30
  }
  // ...más puntos de datos
]
```

---

### 5. Gráfico simple de precios (PNG)

* **URL:** `/api/chart/single/{symbol}?hours={hours}`
* **Método:** `GET`
* **Parámetros:**

  * `symbol` (ruta): símbolo de la criptomoneda (ej. BTC)
  * `hours` (query opcional): cantidad de horas para mostrar en el gráfico (por defecto 24)
* **Descripción:** Genera y devuelve una imagen PNG con gráfico de líneas de precios en el rango especificado.
* **Ejemplo de petición:**

```bash
curl -X GET "http://localhost:8080/api/chart/single/ETH?hours=12" --output eth_price_chart.png
```

* **Respuesta:** Imagen PNG con gráfico.

---

### 6. Gráfico de regresión lineal (PNG)

* **URL:** `/api/chart/regression/{symbol}?start=HH:mm&end=HH:mm`
* **Método:** `GET`
* **Parámetros:**

  * `symbol` (ruta): símbolo de la cripto (ej. BTC)
  * `start` (query): hora de inicio en formato 24h `HH:mm` (ej. 09:00)
  * `end` (query): hora de fin en formato 24h `HH:mm` (ej. 18:00)
* **Descripción:** Genera y devuelve gráfico PNG con la línea de regresión lineal para el intervalo horario.
* **Ejemplo de petición:**

```bash
curl -X GET "http://localhost:8080/api/chart/regression/BTC?start=08:00&end=16:00" --output btc_regression_chart.png
```

* **Respuesta:** Imagen PNG con gráfico de regresión.

---

### 7. Servir archivos estáticos

* **URL:** `/` o `/nombre_del_archivo`
* **Método:** `GET`
* **Descripción:** Sirve archivos estáticos embebidos (HTML, CSS, JS, imágenes) para el frontend o documentación.
* **Ejemplo de petición:**

```bash
curl -X GET http://localhost:8080/index.html
```

* **Respuesta:** Archivo solicitado con el `Content-Type` adecuado.

---

## Ejemplos prácticos con `curl`

* Obtener estado:

```bash
curl http://localhost:8080/api/health
```

* Actualizar precios:

```bash
curl -X POST http://localhost:8080/api/update-prices
```

* Obtener últimos precios:

```bash
curl http://localhost:8080/api/prices/latest
```

* Obtener historial últimos 12 horas para ETH:

```bash
curl "http://localhost:8080/api/prices/history/ETH?hours=12"
```

* Descargar gráfico de precios BTC últimas 24 horas:

```bash
curl "http://localhost:8080/api/chart/single/BTC?hours=24" --output btc_chart.png
```

* Descargar gráfico de regresión BTC de 09:00 a 17:00:

```bash
curl "http://localhost:8080/api/chart/regression/BTC?start=09:00&end=17:00" --output btc_regression.png
```

---

## Estructura del proyecto

```
src/
├── main/
│   ├── java/
│   │   └── com.backend/
│   │       ├── WebServer.java                  # Clase principal con arranque del servidor HTTP
│   │       ├── handlers/                        # Handlers HTTP para los endpoints
│   │       │   ├── HealthHandler.java
│   │       │   ├── UpdatePricesHandler.java
│   │       │   ├── LatestPricesHandler.java
│   │       │   ├── PriceHistoryHandler.java
│   │       │   ├── RegressionChartHandler.java
│   │       │   ├── SingleChartHandler.java
│   │       │   ├── StaticFileHandler.java
│   │       │   └── QueryParser.java             # Parser de query params
│   │       └── services/
│   │           ├── CoinGeckoService.java        # Cliente personalizado CoinGecko + DB + lógica
│   │           └── DatabaseService.java         # Acceso a PostgreSQL y lógica SQL
├── resources/
│   └── static/                                  # Archivos estáticos servidos (HTML, CSS, JS)
└── pom.xml
```

---

## Base de datos

### Tablas principales

* **cryptocurrencies**

| Campo     | Tipo        | Descripción                 |
| --------- | ----------- | --------------------------- |
| id        | SERIAL PK   | Identificador único         |
| symbol    | VARCHAR(10) | Símbolo abreviado (ej. BTC) |
| name      | VARCHAR(50) | Nombre completo             |
| logo\_url | TEXT        | URL del logo                |
| active    | BOOLEAN     | Si la cripto está activa    |

* **price\_history**

| Campo      | Tipo          | Descripción                     |
| ---------- | ------------- | ------------------------------- |
| crypto\_id | INT FK        | Referencia a `cryptocurrencies` |
| price\_usd | NUMERIC(18,8) | Precio en USD                   |
| timestamp  | TIMESTAMP     | Fecha y hora de registro        |

---

## Dependencias destacadas

* OkHttp — para llamadas HTTP
* Gson — para manejo JSON
* PostgreSQL JDBC driver — conexión a base de datos
* JFreeChart — generación de gráficos
* Apache Commons IO — utilidades IO
* Apache Commons Math — regresión estadística

---

## Notas y recomendaciones

* Ajusta variables de entorno para conectar a tu base de datos PostgreSQL.
* El servicio `CoinGeckoService` gestiona actualización, consulta y mapeo de símbolos a IDs de CoinGecko.
* La base de datos almacena solo datos para criptomonedas activas, mejorando rendimiento y control.
* Las imágenes PNG generadas pueden ser usadas en un frontend web o app móvil para visualización dinámica.
* Para producción, considera agregar autenticación, caché y manejo robusto de errores.
