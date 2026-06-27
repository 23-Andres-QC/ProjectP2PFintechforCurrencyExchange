import requests as http
from flask import Blueprint, request
from flask_jwt_extended import jwt_required

from app.core.redis_client import cache_get, cache_set

exchange_bp = Blueprint('exchange', __name__, url_prefix='/exchange')

CACHE_TTL = 600

EXTERNAL_URL = "https://api.exchangerate-api.com/v4/latest/{base}"

MAIN_CURRENCIES = ["USD", "PEN", "EUR", "GBP", "BRL", "CLP", "COP", "ARS", "MXN", "JPY", "CAD", "AUD"]


def _fetch_rates(base: str) -> dict:
    cache_key = f'exchange_rates:{base}'
    cached = cache_get(cache_key)
    if cached:
        return cached

    resp = http.get(EXTERNAL_URL.format(base=base), timeout=10)
    resp.raise_for_status()
    data = resp.json()

    result = {
        'base': data['base'],
        'rates': data['rates'],
        'source': 'ExchangeRate-API (api.exchangerate-api.com)',
        'next_update': data.get('time_next_update_utc', ''),
    }
    cache_set(cache_key, result, CACHE_TTL)
    return result


@exchange_bp.route('/rates', methods=['GET'])
@jwt_required()
def get_rates():
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


@exchange_bp.route('/convert', methods=['GET'])
@jwt_required()
def convert():
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


@exchange_bp.route('/currencies', methods=['GET'])
@jwt_required()
def list_currencies():
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
