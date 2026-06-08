import asyncio
import json
import logging
import os
from typing import Any
from urllib.parse import urlsplit, urlunsplit

import redis.asyncio as redis

logger = logging.getLogger("uvicorn.error")


def _redacted_url(redis_url: str) -> str:
    try:
        parsed = urlsplit(redis_url)
        netloc = parsed.hostname or "localhost"
        if parsed.port:
            netloc = f"{netloc}:{parsed.port}"
        return urlunsplit((parsed.scheme, netloc, parsed.path, "", ""))
    except Exception:
        return "redis://<redacted>"


class RedisStreamProducerService:
    _client: redis.Redis | None = None

    @classmethod
    async def get_client(cls) -> redis.Redis | None:
        if cls._client is None:
            redis_url = os.getenv("REDIS_URL", "redis://localhost:6379/0")
            try:
                cls._client = redis.from_url(redis_url, decode_responses=True)
                await cls._client.ping()
                logger.info("Redis Stream producer connected to %s.", _redacted_url(redis_url))
            except Exception as e:
                logger.warning("Redis Stream producer not connected (will retry when needed): %s", e)
                cls._client = None
        return cls._client

    @classmethod
    async def send_response(cls, stream_key: str, data: dict[str, Any]) -> bool:
        attempts = int(os.getenv("AI_RESPONSE_STREAM_PUBLISH_ATTEMPTS", "3"))
        retry_delay = float(os.getenv("AI_RESPONSE_STREAM_PUBLISH_RETRY_SECONDS", "0.2"))
        max_len = int(os.getenv("AI_RESPONSE_STREAM_MAXLEN", "10000"))

        for attempt in range(1, attempts + 1):
            try:
                client = await cls.get_client()
                if client is None:
                    raise ConnectionError("Redis client is not connected")

                message_id = await client.xadd(
                    stream_key,
                    {
                        "action": str(data.get("action", "message")),
                        "clientId": str(data.get("clientId", data.get("client_id", ""))),
                        "requestId": str(data.get("requestId", data.get("request_id", ""))),
                        "payload": json.dumps(data, ensure_ascii=False),
                    },
                    maxlen=max_len,
                    approximate=True,
                )
                logger.info(
                    "Published message %s to Redis Stream '%s': action=%s",
                    message_id,
                    stream_key,
                    data.get("action", "message"),
                )
                return True
            except Exception as e:
                logger.warning(
                    "Redis Stream publish attempt %s/%s failed for stream '%s': %s",
                    attempt,
                    attempts,
                    stream_key,
                    e,
                )
                cls._client = None
                if attempt < attempts:
                    await asyncio.sleep(retry_delay)

        logger.error("Failed to publish to Redis Stream '%s' after %s attempts.", stream_key, attempts)
        return False

    @classmethod
    async def close(cls) -> None:
        if cls._client:
            try:
                await cls._client.aclose()
                logger.info("Redis Stream producer connection closed.")
            except Exception as e:
                logger.error("Error closing Redis Stream producer connection: %s", e)
            finally:
                cls._client = None
