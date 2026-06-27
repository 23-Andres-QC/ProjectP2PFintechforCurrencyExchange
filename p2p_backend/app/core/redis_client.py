import json
import os

import redis

_client: redis.Redis | None = None


def _get_client() -> redis.Redis:
    global _client
    if _client is None:
        _client = redis.from_url(
            os.environ.get('REDIS_URL', 'redis://localhost:6379/0'),
            decode_responses=True,
            socket_connect_timeout=2,
            socket_timeout=2,
        )
    return _client


def cache_get(key: str) -> dict | None:
    try:
        raw = _get_client().get(key)
        return json.loads(raw) if raw else None
    except Exception:
        return None


def cache_set(key: str, value: dict, ttl_seconds: int) -> None:
    try:
        _get_client().setex(key, ttl_seconds, json.dumps(value))
    except Exception:
        pass
