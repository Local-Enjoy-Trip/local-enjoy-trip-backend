import os
from fastapi import APIRouter, Depends, HTTPException
from app.schemas.ai import ChatRequest, ChatResponse
from app.services.ai_service import AIService
from app.services.redis_stream_producer import RedisStreamProducerService

router = APIRouter(prefix="/ai", tags=["AI Chatbot"])
AI_RESPONSE_STREAM_KEY = os.getenv("AI_RESPONSE_STREAM_KEY", "enjoytrip-ai-responses")

@router.post("/chat", response_model=ChatResponse)
async def chat_with_assistant(
    request: ChatRequest,
    ai_service: AIService = Depends(AIService)
):
    try:
        reply = await ai_service.chat_assistant(request.message, request.history)
        response = ChatResponse(
            reply=reply,
            request_id=request.request_id,
            client_id=request.client_id,
        )
        if request.client_id:
            payload = response.model_dump(by_alias=True)
            payload["message"] = request.message
            payload["action"] = "chat"
            if not await RedisStreamProducerService.send_response(AI_RESPONSE_STREAM_KEY, payload):
                raise HTTPException(status_code=503, detail="Redis Stream handoff failed.")
        return response
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Chat failed: {str(e)}")
