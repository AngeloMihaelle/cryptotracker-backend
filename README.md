# 📈 CryptoTracker Web — Backend Service

Microservicio Java REST que obtiene y expone el precio de las 10 principales criptomonedas, usando la API Pro de CoinGecko.

Un servidor backend simple en Java que proporciona datos de precios de criptomonedas usando la API de CoinGecko.
Implementa un servidor HTTP básico que expone endpoints para obtener estado de salud, precios actuales, historial de precios y actualización de precios.

---

## Descripción

Este proyecto implementa un servidor HTTP básico utilizando el servidor embebido de Java (`HttpServer`).
Se integra con la API de CoinGecko mediante una implementación propia (ya que no fue posible usar la librería oficial `net.osslabz:coingecko-java`).
Esto permite obtener datos como precios actuales de criptomonedas y gráficos históricos.

Características principales:

* Endpoint para verificar que el servidor esté activo
* Obtener los 10 principales criptomonedas por capitalización de mercado con sus precios
* Obtener datos históricos (gráfico de mercado) para una moneda y rango de tiempo específicos
* Endpoint para forzar actualización de precios (implementación básica)

El proyecto usa Java 8, Maven y un servidor HTTP embebido ligero, ideal para pruebas o desarrollos rápidos.

---

## Tecnologías usadas

* Java 8
* Maven para manejo de dependencias
* Cliente personalizado para API CoinGecko
* Servidor HTTP embebido de Java (`com.sun.net.httpserver.HttpServer`)
* Jackson para serialización JSON

---

## Instalación y ejecución

1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd backend
```

2. Construir con Maven

```bash
mvn clean package
```

3. Ejecutar el servidor

```bash
java -cp target/backend-1.0-SNAPSHOT.jar com.backend.WebServer
```

El servidor arrancará escuchando en el puerto **8080**.

---

## Endpoints disponibles

### 1. Estado del servidor (Health Check)

* **URL:** `/api/health`
* **Método:** `GET`
* **Descripción:** Retorna un JSON simple indicando que el servidor está activo.
* **Ejemplo de respuesta:**

```json
{
  "status": "ok"
}
```

---

### 2. Actualizar precios

* **URL:** `/api/update-prices`
* **Método:** `POST`
* **Descripción:** Endpoint para forzar la actualización de precios en el backend (implementación básica).
* **Ejemplo de respuesta:**

```json
{
  "message": "Precios actualizados exitosamente"
}
```

**Nota:** Actualmente no requiere cuerpo JSON; la llamada puede hacerse con un POST vacío.

---

### 3. Últimos precios

* **URL:** `/api/prices/latest`
* **Método:** `GET`
* **Descripción:** Devuelve los datos actuales de mercado de las 10 principales criptomonedas por capitalización en USD.
* **Respuesta:** Array JSON con objetos que contienen detalles de mercado (id, símbolo, precio, capitalización, etc.)

**Ejemplo de respuesta:**

```json
[
  {
    "id": "bitcoin",
    "symbol": "btc",
    "name": "Bitcoin",
    "current_price": 30000,
    "market_cap": 600000000000,
    ...
  },
  ...
]
```

---

### 4. Historial de precios

* **URL:** `/api/prices/history/{simbolo}?hours={horas}`
* **Método:** `GET`
* **Descripción:** Devuelve datos históricos de precios (gráfico de mercado) en USD para la criptomoneda indicada.
* **Parámetros:**

  * `simbolo` (en la ruta): símbolo o id de la criptomoneda, por ejemplo `bitcoin` o `ethereum`
  * `hours` (query param opcional): cantidad de horas del historial a obtener. Por defecto 24.

**Ejemplo de petición:**

```
GET /api/prices/history/bitcoin?hours=48
```

**Respuesta:** JSON con arrays de precios, capitalización y volúmenes en el rango solicitado.

---

## Formato JSON para peticiones y respuestas

* Los endpoints que aceptan peticiones con cuerpo (`POST`) actualmente no requieren datos en JSON para funcionar.
* Las respuestas siempre están en formato JSON, con codificación UTF-8.
* Si en un futuro decides extender el endpoint `/api/update-prices` para aceptar parámetros en JSON, el formato esperado podría ser algo como:

```json
{
  "forceUpdate": true
}
```

* Para consumir las respuestas, simplemente parsea el JSON recibido. Ejemplo en Java con Jackson:

```java
ObjectMapper mapper = new ObjectMapper();
List<Market> markets = mapper.readValue(jsonResponse, new TypeReference<List<Market>>() {});
```

---

## Estructura del proyecto

```
src/
├── main/
│   ├── java/
│   │   ├── com.backend/
│   │   │   ├── WebServer.java               # Punto de entrada principal
│   │   │   ├── handlers/
│   │   │   │   ├── HealthHandler.java
│   │   │   │   ├── UpdatePricesHandler.java
│   │   │   │   ├── LatestPricesHandler.java
│   │   │   │   ├── PriceHistoryHandler.java
│   │   │   │   └── QueryParser.java         # Utilidad para parsear query strings
│   │   │   └── services/
│   │   │       └── CoinGeckoService.java    # Cliente personalizado para CoinGecko API
├── resources/
└── pom.xml
```

---

## Dependencias

Actualmente no se usa la librería oficial `net.osslabz:coingecko-java` porque no fue posible integrarla correctamente.
Se usa un cliente propio dentro de `CoinGeckoService` para manejar llamadas HTTP a la API de CoinGecko.

Además:

* Jackson (`com.fasterxml.jackson.core:jackson-databind`) para JSON
* JUnit para pruebas (scope test)

---

## Cómo extender el proyecto

* Implementar caché o actualizaciones periódicas automáticas dentro de `CoinGeckoService` para mejorar rendimiento.
* Añadir autenticación para proteger endpoints sensibles.
* Agregar más endpoints para otros datos de CoinGecko (info de monedas, exchanges, etc.)
* Mejorar manejo de errores y validaciones.
* Añadir pruebas unitarias e integración.

---

## Notas finales

* Este proyecto usa el servidor HTTP embebido de Java para simplicidad y aprendizaje. Para producción considera frameworks más robustos (Spring Boot, Micronaut, etc.).
* Se requiere conexión a Internet para consultar la API de CoinGecko.
* La API de CoinGecko tiene límites y políticas que deben respetarse.



