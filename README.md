# 📈 CryptoTracker Web — Backend Service

Microservicio Java REST que obtiene y expone el precio de las 10 principales criptomonedas, usando la API Pro de CoinGecko.

---

## 📋 Tabla de Contenidos
- [Descripción](#descripción)
- [Características](#características)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Uso](#uso)
  - Endpoints
- [Configuración](#configuración)
- [Estrategia de despliegue](#estrategia-de-despliegue)
- [Pruebas](#pruebas)
- [Roadmap](#roadmap)
- [Contacto](#contacto)

---

## 🧩 Descripción

Este microservicio proporciona los precios actuales y el historial de precios de criptomonedas. Usa la API Pro de CoinGecko para recopilar datos, expone endpoints para salud del servicio, actualización manual de precios, precios actuales y históricos por símbolo.

---

## ✅ Características

- ✅ Endpoint `/api/health` — Verifica el estado del servicio.
- ✅ Endpoint `/api/update-prices` (POST) — Fuerza la actualización manual.
- ✅ Endpoint `/api/prices/latest` (GET) — Devuelve precios actuales de las 10 principales criptos.
- ✅ Endpoint `/api/prices/history/{symbol}?hours=n` (GET) — Devuelve historial horario de una cripto.

/api/health para monitoreo en watchdog.

---

## 📌 Requisitos

- Java 11+
- Maven
- CoinGecko Java SDK (`coingecko-java` 1.0.0)
- (Opcional) `com.fasterxml.jackson.core:jackson-databind` o `Gson`

---

## 🛠 Instalación

1. Clona este repositorio:
   ```bash
   git clone <url-del-repo>
   cd cryptotracker-backend


2. Agrega dependencias en `pom.xml`:

   ```xml
   <dependency>
     <groupId>net.osslabz</groupId>
     <artifactId>coingecko-java</artifactId>
     <version>1.0.0</version>
   </dependency>
   <dependency>
     <groupId>com.fasterxml.jackson.core</groupId>
     <artifactId>jackson-databind</artifactId>
     <version>2.14.0</version>
   </dependency>
   ```
3. Compila el proyecto:

   ```bash
   mvn clean package
   ```

---

## 🚀 Uso

Ejecuta el servicio:

```bash
java -jar target/cryptotracker-backend.jar
```

Se iniciará en el puerto `8080` (puede configurarse por argumento o variable de entorno).

### 🔌 Endpoints

| Método | URL                                    | Descripción                                                   |
| ------ | -------------------------------------- | ------------------------------------------------------------- |
| GET    | `/api/health`                          | Estado del servicio                                           |
| POST   | `/api/update-prices`                   | Actualización manual de precios                               |
| GET    | `/api/prices/latest`                   | Lista JSON con precios actuales de las 10 principales criptos |
| GET    | `/api/prices/history/{symbol}?hours=n` | Historial por símbolo con parámetro `hours` (ej. `?hours=6`)  |

Ejemplo:

```bash
curl http://localhost:8080/api/prices/history/BTC?hours=6
```

---

## 🔧 Configuración

* **CLAVE\_API\_CG** (obligatoria): tu `CG-...` de CoinGecko Pro
* **PORT**: puerto del servidor (por defecto `8080`)
* **THREADS**: número de hilos en el executor (opcional)

Ejemplo de ejecución:

```bash
export CG_KEY=CG-D8E6XXXXXXXXXXXXXXXXXX
export PORT=8080
java -jar target/cryptotracker-backend.jar
```

---

## 🌐 Estrategia de despliegue

* Contenerización con Docker
* Escalabilidad usando Kubernetes o GCP App Engine
* Debe ser compatible con entornos cloud y contener firewall configurable

---

## 🧪 Pruebas

* Pruebas unitarias con JUnit y Mockito
* Verificar:

  * Respuesta de `/api/health`
  * JSON en `/api/prices/latest`
  * Datos consistentes en `/api/prices/history`
* Integración backend + CoinGecko en test controlado

---

## 📅 Roadmap

* [ ] Almacenamiento en base de datos para históricos
* [ ] Endpoint `/api/prices/history/all`
* [ ] Mecanismo cron job / scheduler automático
* [ ] Monitoreo con métricas Prometheus / Grafana

---


