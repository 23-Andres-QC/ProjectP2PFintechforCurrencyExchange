# APIs de Tipos de Cambio en Vivo — Reporte

> Proyecto: PeruExchange P2P  
> Fecha: 2026-06-04  
> Investigador: Claude Code

---

## API implementada en el backend

### ExchangeRate-API (Open / sin key)

| Campo | Valor |
|---|---|
| **URL base** | `https://api.exchangerate-api.com/v4/latest/{BASE}` |
| **Costo** | Gratis (sin API key) |
| **Monedas soportadas** | 161 (incluye PEN, USD, EUR, GBP, BRL, CLP, COP, ARS, MXN, JPY...) |
| **Frecuencia de actualización** | Cada hora |
| **Autenticación** | Ninguna para el endpoint `/v4/latest` |
| **Formato** | JSON |

#### Ejemplo de respuesta

```json
{
  "base": "USD",
  "date": "2026-06-04",
  "time_last_updated": 1749024000,
  "rates": {
    "PEN": 3.72,
    "EUR": 0.92,
    "GBP": 0.79,
    ...
  }
}
```

---

## Endpoints construidos en `/api/v1/exchange`

### 1. Tasas principales desde una moneda base

**`GET /api/v1/exchange/rates?from=USD`**

Devuelve las monedas principales (USD, PEN, EUR, GBP, BRL, CLP, COP, ARS, MXN, JPY, CAD, AUD).

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:5000/api/v1/exchange/rates?from=USD"
```

Respuesta:
```json
{
  "base": "USD",
  "rates": [
    { "from_currency": "USD", "to_currency": "PEN", "rate": 3.72, "amount": 1, "converted": 3.72 },
    { "from_currency": "USD", "to_currency": "EUR", "rate": 0.92, "amount": 1, "converted": 0.92 }
  ],
  "source": "ExchangeRate-API (api.exchangerate-api.com)",
  "updated_at": "Thu, 05 Jun 2026 00:00:01 +0000"
}
```

---

### 2. Tipo de cambio entre dos monedas

**`GET /api/v1/exchange/rates?from=USD&to=PEN`**

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:5000/api/v1/exchange/rates?from=USD&to=PEN"
```

Respuesta:
```json
{
  "from_currency": "USD",
  "to_currency": "PEN",
  "rate": 3.72,
  "amount": 1,
  "converted": 3.72,
  "source": "ExchangeRate-API (api.exchangerate-api.com)",
  "updated_at": "Thu, 05 Jun 2026 00:00:01 +0000"
}
```

---

### 3. Conversión directa con monto

**`GET /api/v1/exchange/convert?from=USD&to=PEN&amount=100`**

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:5000/api/v1/exchange/convert?from=USD&to=PEN&amount=100"
```

Respuesta:
```json
{
  "from_currency": "USD",
  "to_currency": "PEN",
  "rate": 3.72,
  "amount": 100,
  "converted": 372.0
}
```

Casos de uso en la app:
- ¿Cuántos soles son 100 dólares? → `from=USD&to=PEN&amount=100`
- ¿Cuántos dólares son 1000 soles? → `from=PEN&to=USD&amount=1000`

---

### 4. Lista de todas las monedas disponibles

**`GET /api/v1/exchange/currencies`**

```bash
curl -H "Authorization: Bearer <token>" \
  "http://localhost:5000/api/v1/exchange/currencies"
```

---

## Parámetros configurables (en `exchange/routes.py`)

| Variable | Valor actual | Descripción |
|---|---|---|
| `_CACHE_TTL` | `600` (10 min) | Tiempo de caché en segundos. Bajar para más frescura, subir para menos llamadas externas. |
| `MAIN_CURRENCIES` | `["USD","PEN","EUR",...]` | Lista de monedas que devuelve el endpoint sin `?to=`. Agregar o quitar según se necesite. |
| `EXTERNAL_URL` | `https://api.exchangerate-api.com/v4/latest/{base}` | URL de la fuente. Fácil de cambiar por otra API. |

---

## Otras APIs investigadas (alternativas)

| API | Free tier | Perú (PEN) | Key requerida | Notas |
|---|---|---|---|---|
| **ExchangeRate-API v4** (implementada) | ✅ sin límite | ✅ | ❌ | La más simple. Sin key. Ideal para dev y prod pequeña. |
| **Open Exchange Rates** | ✅ 1000 req/mes | ✅ | ✅ | Mejor para producción. Plan gratuito con key. |
| **Frankfurter.app** | ✅ sin límite | ❌ | ❌ | Solo monedas del BCE europeo. Sin PEN. |
| **Fixer.io** | ✅ 100 req/mes | ✅ | ✅ | Plan pro recomendado. |
| **SBS Perú** | ✅ oficial | Solo USD/PEN | ❌ | Tipo de cambio oficial del Banco Central peruano. Sin endpoint REST estándar, requiere scraping HTML. |
| **BCRP Perú** (Banco Central) | ✅ oficial | Solo USD/PEN | ❌ | Publicación diaria en PDF/HTML. No REST. |
| **Alpha Vantage** | ✅ 25 req/día | ✅ | ✅ | Buen para forex en tiempo real (mercado abierto). |

### Nota sobre el tipo de cambio oficial peruano

El **Banco Central de Reserva del Perú (BCRP)** y la **SBS** publican el tipo de cambio oficial USD/PEN diariamente, pero no tienen una API REST pública estándar. Para producción se recomienda:

1. Usar **ExchangeRate-API** (ya implementada) para todos los pares.
2. O combinar con BCRP scraping si se requiere el tipo de cambio oficial exacto del BCRP.

---

## Cómo cambiar la fuente de datos

En [p2p_backend/app/api/v1/exchange/routes.py](../p2p_backend/app/api/v1/exchange/routes.py), modificar:

```python
# Cambiar la URL de la API externa:
EXTERNAL_URL = "https://api.exchangerate-api.com/v4/latest/{base}"

# Reducir el caché para tasas más frescas (en segundos):
_CACHE_TTL = 300  # 5 minutos

# Agregar más monedas de Latinoamérica:
MAIN_CURRENCIES = ["USD", "PEN", "EUR", "GBP", "BRL", "CLP", "COP", "ARS", "MXN", "BOB", "PYG", "UYU"]
```
