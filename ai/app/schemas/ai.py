from pydantic import BaseModel, ConfigDict, Field
from typing import List, Optional

class RecommendRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    destination: str = Field(..., description="여행지 (예: 서울, 부산, 제주)")
    duration_days: int = Field(default=2, description="여행 기간 (일수)")
    theme: Optional[str] = Field(default="힐링", description="여행 테마 (예: 힐링, 맛집, 역사, 커플, 액티비티)")
    preferences: Optional[List[str]] = Field(default=[], description="선호 사항 (예: 박물관, 바다, 야경)")
    request_id: Optional[str] = Field(default=None, alias="requestId", description="SSE 응답 상관관계 ID")
    client_id: Optional[str] = Field(default=None, alias="clientId", description="SSE 대상 client/user ID")

class Place(BaseModel):
    name: str = Field(..., description="장소명")
    description: str = Field(..., description="설명")
    recommended_time: str = Field(..., description="추천 시간대 (예: 오전 10:00)")
    latitude: Optional[float] = None
    longitude: Optional[float] = None

class DayItinerary(BaseModel):
    day: int = Field(..., description="일차")
    places: List[Place] = Field(..., description="방문할 장소 리스트")

class RecommendResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    destination: str
    theme: str
    duration_days: int
    itinerary: List[DayItinerary] = Field(..., description="일자별 상세 일정")
    tips: List[str] = Field(..., description="여행 꿀팁")
    request_id: Optional[str] = Field(default=None, alias="requestId")
    client_id: Optional[str] = Field(default=None, alias="clientId")

class ChatRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    message: str = Field(..., description="사용자 질문 메시지")
    history: Optional[List[dict]] = Field(default=[], description="대화 기록")
    request_id: Optional[str] = Field(default=None, alias="requestId", description="SSE 응답 상관관계 ID")
    client_id: Optional[str] = Field(default=None, alias="clientId", description="SSE 대상 client/user ID")

class ChatResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    reply: str = Field(..., description="AI의 응답")
    request_id: Optional[str] = Field(default=None, alias="requestId")
    client_id: Optional[str] = Field(default=None, alias="clientId")
