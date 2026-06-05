"""Exchange rates — /api/v1/exchange/*

Fuente primaria: ExchangeRate-API (https://api.exchangerate-api.com)
  • Endpoint público gratuito, sin API key, actualizado cada hora.
  • URL: https://api.exchangerate-api.com/v4/latest/{base}
  • Cubre 161 monedas, incluido PEN (Sol peruano).

Cache en memoria: 10 minutos para no saturar la API externa.
"""
import time
import requests as http
from flask import Blueprint, request
from flask_jwt_extended import jwt_required

exchange_bp = Blueprint('exchange', __name__, url_prefix='/exchange')

# ── Cache en memoria ─────────────────────────────────────────────────────────
_CACHE: dict[str, dict] = {}   # base_currency → {rates, fetched_at}
_CACHE_TTL = 600               # 10 minutos

EXTERNAL_URL = "https://api.exchangerate-api.com/v4/latest/{base}"

# Monedas más relevantes para la app (Perú + globales)
MAIN_CURRENCIES = ["USD", "PEN", "EUR", "GBP", "BRL", "CLP", "COP", "ARS", "MXN", "JPY", "CAD", "AUD"]


def _fetch_rates(base: str) -> dict:
    """Obtiene tasas desde ExchangeRate-API con caché de 10 min."""
    now = time.time()
    cached = _CACHE.get(base)
    if cached and (now - cached['fetched_at']) < _CACHE_TTL:
        return cached

    resp = http.get(EXTERNAL_URL.format(base=base), timeout=10)
    resp.raise_for_status()
    data = resp.json()

    result = {
        'base': data['base'],
        'rates': data['rates'],
        'fetched_at': now,
        'source': 'ExchangeRate-API (api.exchangerate-api.com)',
        'next_update': data.get('time_next_update_utc', ''),
    }
    _CACHE[base] = result
    return result


# ── GET /exchange/rates ──────────────────────────────────────────────────────
@exchange_bp.route('/rates', methods=['GET'])
@jwt_required()
def get_rates():
    """Retorna tasas de cambio reales.

    Query params:
      from  — moneda base (default USD)
      to    — moneda destino (opcional, si se omite devuelve todas las principales)
      amount — monto a convertir (opcional, default 1)
    """
    from_c  = request.args.get('from', 'USD').upper()
    to_c    = request.args.get('to', '').upper()
    amount  = float(request.args.get('amount', 1))

    try:
        data = _fetch_rates(from_c)
    except Exception as e:
        return {'error': {'code': 'EXCHANGE_FETCH_FAILED', 'message': str(e)}}, 502

    all_rates = data['rates']

    if to_c:
        if to_c not in all_rates:
            return {'error': {'code': 'CURRENCY_NOT_FOUND', 'message': f'{to_c} not supported'}}, 404
        rate = all_rates[to_c]
        return {
            'from_currency': from_c,
            'to_currency': to_c,
            'rate': rate,
            'amount': amount,
            'converted': round(amount * rate, 4),
            'source': data['source'],
            'updated_at': data['next_update'],
        }, 200

    # Devuelve solo las monedas principales
    rates_list = [
        {
            'from_currency': from_c,
            'to_currency': cur,
            'rate': all_rates[cur],
            'amount': amount,
            'converted': round(amount * all_rates[cur], 4),
        }
        for cur in MAIN_CURRENCIES
        if cur in all_rates and cur != from_c
    ]

    return {
        'base': from_c,
        'rates': rates_list,
        'source': data['source'],
        'updated_at': data['next_update'],
    }, 200


# ── GET /exchange/convert ─────────────────────────────────────────────────────
@exchange_bp.route('/convert', methods=['GET'])
@jwt_required()
def convert():
    """Conversión directa entre dos monedas.

    Query params:
      from   — moneda origen  (ej: USD)
      to     — moneda destino (ej: PEN)
      amount — monto a convertir (ej: 100)
    """
    from_c  = request.args.get('from', 'USD').upper()
    to_c    = request.args.get('to', 'PEN').upper()
    amount  = float(request.args.get('amount', 1))

    try:
        data = _fetch_rates(from_c)
    except Exception as e:
        return {'error': {'code': 'EXCHANGE_FETCH_FAILED', 'message': str(e)}}, 502

    rate = data['rates'].get(to_c)
    if rate is None:
        return {'error': {'code': 'CURRENCY_NOT_FOUND', 'message': f'{to_c} not supported'}}, 404

    return {
        'from_currency': from_c,
        'to_currency': to_c,
        'rate': rate,
        'amount': amount,
        'converted': round(amount * rate, 4),
        'source': data['source'],
        'updated_at': data['next_update'],
    }, 200


# ── GET /exchange/currencies ──────────────────────────────────────────────────
@exchange_bp.route('/currencies', methods=['GET'])
@jwt_required()
def list_currencies():
    """Lista todas las monedas disponibles desde la API externa."""
    try:
        data = _fetch_rates('USD')
    except Exception as e:
        return {'error': {'code': 'EXCHANGE_FETCH_FAILED', 'message': str(e)}}, 502

    currencies = sorted(data['rates'].keys())
    return {
        'currencies': currencies,
        'total': len(currencies),
        'source': data['source'],
    }, 200
