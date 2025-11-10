# 💱 Conversor de Moneda en Java

Aplicación de consola escrita en **Java 11+** que permite **convertir montos entre diferentes monedas** utilizando la [ExchangeRate-API](https://www.exchangerate-api.com/).

El programa consulta las tasas de cambio en tiempo real (o diarias si no hay API Key) y muestra el resultado de forma simple y legible.

---

## 🚀 Características principales

- Conversión entre **cualquier par de monedas** (USD, EUR, CLP, etc.).
- Soporte para **tasa en tiempo real** usando el endpoint `pair` de ExchangeRate-API.
- Modo de prueba **sin API Key** usando el endpoint público `open`.
- Manejo de errores comunes: conexión, API inválida, códigos no soportados, etc.
- Implementación moderna con `HttpClient` (Java 11) y `Gson` para parsear JSON.

---

## 🧱 Requisitos previos

- **Java 11 o superior**
- **Maven 3.6+**
- Conexión a Internet

---

## 📦 Instalación y ejecución

### 1️⃣ Clonar el repositorio

```bash
git clone https://github.com/tuusuario/conversor-moneda-java.git
cd conversor-moneda-java
