import os
from fastapi import APIRouter, Depends, HTTPException
from app.schemas.ai import RecommendRequest, RecommendResponse
from app.services.ai_service import AIService
from app.services.redis_stream_producer import RedisStreamProducerService

router = APIRouter(prefix="/ai", tags=["AI Recommendation"])
AI_RESPONSE_STREAM_KEY = os.getenv("AI_RESPONSE_STREAM_KEY", "enjoytrip-ai-responses")

@router.post("/recommend", response_model=RecommendResponse)
async def get_travel_recommendation(
    request: RecommendRequest,
    ai_service: AIService = Depends(AIService)
):
    try:
        response = await ai_service.recommend_itinerary(request)
        response = response.model_copy(update={
            "request_id": request.request_id,
            "client_id": request.client_id,
        })
        if request.client_id:
            payload = response.model_dump(by_alias=True)
            payload["action"] = "recommendation"
            if not await RedisStreamProducerService.send_response(AI_RESPONSE_STREAM_KEY, payload):
                raise HTTPException(status_code=503, detail="Redis Stream handoff failed.")
        return response
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Recommendation failed: {str(e)}")
