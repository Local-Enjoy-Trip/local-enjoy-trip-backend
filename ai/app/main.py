import os
import uvicorn
import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routes import recommendation, chat
from app.config import settings
from app.services.redis_stream_producer import RedisStreamProducerService

logger = logging.getLogger("uvicorn.error")
AI_RESPONSE_STREAM_KEY = os.getenv("AI_RESPONSE_STREAM_KEY", "enjoytrip-ai-responses")

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Warm up Redis Stream producer
    await RedisStreamProducerService.get_client()

    yield

    await RedisStreamProducerService.close()

app = FastAPI(
    title="EnjoyTrip AI Service API",
    description="EnjoyTrip의 HTTP 및 Redis Stream 연동을 지원하는 FastAPI AI 마이크로서비스",
    version="1.0.0",
    lifespan=lifespan
)

# Enable CORS for frontend and main backend integration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include HTTP routers
app.include_router(recommendation.router)
app.include_router(chat.router)

@app.get("/")
def read_root():
    return {
        "status": "online",
        "service": "EnjoyTrip AI Service (HTTP + Redis Stream)",
        "endpoints": {
            "http": {
                "recommend": "/ai/recommend [POST]",
                "chat": "/ai/chat [POST]",
                "docs": "/docs [GET]"
            },
            "redis_stream": {
                "stream_key": AI_RESPONSE_STREAM_KEY
            }
        }
    }

if __name__ == "__main__":
    uvicorn.run("app.main:app", host=settings.AI_HOST, port=settings.AI_PORT, reload=True)
