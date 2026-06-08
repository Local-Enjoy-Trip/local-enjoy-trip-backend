import os
import json
import logging
from app.config import settings
from app.schemas.ai import RecommendRequest, RecommendResponse, DayItinerary, Place

logger = logging.getLogger("uvicorn.error")

class AIService:
    def __init__(self):
        pass

    async def recommend_itinerary(self, req: RecommendRequest) -> RecommendResponse:
        return self._generate_mock_recommendation(req)

    async def chat_assistant(self, message: str, history: list) -> str:
        return f"현재 로컬 모의 비서가 답변드립니다. 질문하신 '{message}'에 대해 말씀드리면, 해당 명소는 즐거운 여행을 계획하기에 안성맞춤인 장소입니다!"

    def _generate_mock_recommendation(self, req: RecommendRequest) -> RecommendResponse:
        dest = req.destination
        theme = req.theme or "관광"
        days = req.duration_days
        
        itinerary = []
        
        if "서울" in dest:
            spots = [
                [
                    Place(name="경복궁", description="조선 왕조의 법궁으로, 아름다운 경회루와 근정전을 거닐며 역사와 문화를 동시에 체험하기 좋은 곳입니다.", recommended_time="오전 10:00", latitude=37.5796, longitude=126.9770),
                    Place(name="북촌한옥마을", description="한옥의 골목길을 걸으며 전통 가옥의 매력을 느끼고, 멋진 인생샷을 남길 수 있는 서울의 상징적 명소입니다.", recommended_time="오후 01:30", latitude=37.5829, longitude=126.9835),
                    Place(name="인사동 쌈지길", description="전통 공예품과 갤러리, 그리고 이색 카페가 가득하여 활기찬 분위기와 한국 고유의 미를 함께 느낄 수 있습니다.", recommended_time="오후 04:00", latitude=37.5742, longitude=126.9848),
                    Place(name="N서울타워", description="남산 정상에서 환상적인 서울의 야경과 파노라마 뷰를 감상하며 로맨틱한 저녁 시간을 완성합니다.", recommended_time="저녁 07:30", latitude=37.5511, longitude=126.9882)
                ],
                [
                    Place(name="연남동 동진시장 & 카페거리", description="독특한 소품숍과 아기자기하고 세련된 디저트 카페들이 즐비하여 젊은 감성을 한껏 느낄 수 있습니다.", recommended_time="오전 11:00", latitude=37.5614, longitude=126.9248),
                    Place(name="망원한강공원", description="탁 트인 한강을 바라보며 자전거를 타거나, 돗자리를 펴고 힐링하는 피크닉을 즐기기 아주 좋은 한강의 명소입니다.", recommended_time="오후 03:00", latitude=37.5557, longitude=126.8972),
                    Place(name="명동 & 남대문시장", description="각종 로드숍과 다채로운 길거리 음식, 그리고 전통 재래시장의 푸근함까지 한국의 활기를 압축적으로 보여줍니다.", recommended_time="저녁 06:30", latitude=37.5635, longitude=126.9815)
                ]
            ]
            tips = ["서울의 대중교통은 세계적 수준입니다. 교통카드를 미리 발급받으면 환승 혜택을 톡톡히 누릴 수 있습니다.", "경복궁은 한복 착용 시 입장료가 전액 면제되니 이색적인 한복 대여 체험을 추천합니다."]
        elif "전주" in dest:
            spots = [
                [
                    Place(name="전주한옥마을", description="한국의 전통 주거문화인 한옥들이 보존되어 있으며, 한복을 입고 고즈넉한 한옥 골목길을 걷는 특별한 경험을 할 수 있습니다.", recommended_time="오전 10:00", latitude=35.8146, longitude=127.1526),
                    Place(name="전주 경기전", description="조선 태조 이성계의 어진을 봉안한 곳으로, 대나무 숲길과 고풍스러운 전각 내에서 아름다운 사진을 남기기 좋습니다.", recommended_time="오후 01:30", latitude=35.8152, longitude=127.1498),
                    Place(name="해질녘 전주천 산책", description="기획안 추천 코스이자, 잔잔한 물소리와 붉게 물드는 노을을 보며 한적하고 조용하게 사색하기 좋은 최고의 산책 명소입니다.", recommended_time="오후 05:00", latitude=35.8115, longitude=127.1550),
                    Place(name="전주 남부시장 야시장", description="남부시장 2층 청년몰과 더불어 주말 저녁 활기차게 열리는 야시장에서 전주의 다양한 길거리 음식과 미식을 즐길 수 있습니다.", recommended_time="저녁 07:00", latitude=35.8123, longitude=127.1472)
                ],
                [
                    Place(name="한옥마을 외곽 찻집", description="북적이는 한옥마을 중심가에서 벗어나 외곽 골목의 조용하고 고즈넉한 전통 찻집에서 차 한 잔의 여유를 가집니다.", recommended_time="오전 11:00", latitude=35.8135, longitude=127.1542),
                    Place(name="자만벽화마을", description="아기자기하고 알록달록한 벽화들이 그려진 언덕길 골목으로, 동화 같은 풍경과 독특한 분위기의 루프탑 카페들이 가득합니다.", recommended_time="오후 02:00", latitude=35.8163, longitude=127.1585),
                    Place(name="전주 덕진공원", description="여름철 아름다운 홍련이 가득 피어나는 호수공원으로, 호수를 관통하는 연화교를 걸으며 운치 있는 전주의 자연을 느낍니다.", recommended_time="오후 04:30", latitude=35.8442, longitude=127.1215)
                ]
            ]
            tips = ["전주는 맛의 고장입니다. 유명 맛집 투어 외에도 길거리 음식(피순대, 초코파이 등)을 가볍게 맛보는 것을 추천합니다.", "한옥마을 내부 도로는 주말 및 공휴일에 차량 통행이 제한되니 외곽 공영주차장을 이용해 주세요."]
        elif "부산" in dest:
            spots = [
                [
                    Place(name="감천문화마을", description="부산의 산토리니로 불리는 알록달록한 계단식 마을로, 골목 구석구석 숨겨진 예쁜 벽화와 예술 조형물을 감상하는 재미가 쏠쏠합니다.", recommended_time="오전 10:00", latitude=35.0975, longitude=129.0092),
                    Place(name="자갈치시장", description="한국 최대의 수산시장으로 활기 넘치는 상인들의 모습과 싱싱하고 펄떡이는 제철 해산물을 직접 맛볼 수 있습니다.", recommended_time="오후 01:00", latitude=35.0967, longitude=129.0305),
                    Place(name="송도 용궁구름다리", description="송도 앞바다의 푸른 물결 위를 직접 걷는 듯한 짜릿함과 웅장한 기암괴석의 절경을 한눈에 담을 수 있는 곳입니다.", recommended_time="오후 03:30", latitude=35.0763, longitude=129.0232),
                    Place(name="광안리 해수욕장 & 광안대교", description="은은하게 부서지는 파도 소리를 들으며 다채로운 불빛으로 반짝이는 광안대교의 야경을 감상하기에 최고의 명소입니다.", recommended_time="저녁 07:30", latitude=35.1532, longitude=129.1189)
                ],
                [
                    Place(name="해동용궁사", description="파도가 넘실거리는 해안 바위 위에 지어진 이색적인 사찰로, 끝없이 넓은 동해 바다를 바라보며 경건한 마음을 가져볼 수 있습니다.", recommended_time="오전 09:30", latitude=35.1885, longitude=129.2234),
                    Place(name="블루라인파크 해변열차", description="동해 남부선 옛 철길을 따라 해안 절경을 감상하며 낭만적인 해안가 뷰를 즐길 수 있는 이색 열차 체험입니다.", recommended_time="오후 01:00", latitude=35.1592, longitude=129.1670),
                    Place(name="해운대 더베이101", description="마천루 빌딩들의 환상적인 LED 조명이 물에 비치는 화려한 야경을 감상하며 시원한 맥주 한 잔으로 피로를 풀어봅니다.", recommended_time="저녁 07:00", latitude=35.1565, longitude=129.1518)
                ]
            ]
            tips = ["부산 해안열차는 주말에 매진되기 쉬우니 탑승 1~2일 전 온라인 예약을 추천합니다.", "부산 시내버스는 급정거가 잦으니 승차 시 손잡이를 꼬옥 잡아주세요."]
        else:
            spots = [
                [
                    Place(name=f"{dest} 중앙 공원", description="도심 속 푸르른 녹음과 아름다운 인공 호수가 펼쳐진 공원으로 가벼운 산책과 피크닉으로 여행의 설렘을 시작합니다.", recommended_time="오전 10:30", latitude=37.5, longitude=127.0),
                    Place(name=f"{dest} 로컬 맛집 거리", description="지역 주민들에게 사랑받는 오랜 역사와 전통의 향토 음식을 맛보며 미식의 매력을 만끽합니다.", recommended_time="오후 01:00", latitude=37.5, longitude=127.0),
                    Place(name=f"{dest} 시립 미술관 & 문화 공간", description="지역의 고유한 예술가들의 정취가 녹아든 현대 미술품과 갤러리를 관람하며 차분한 사색에 잠깁니다.", recommended_time="오후 03:30", latitude=37.5, longitude=127.0),
                    Place(name=f"{dest} 야경 전망대", description="높은 지대에서 온 시내의 은은하고 은하수 같은 조명을 관람하며 아름다운 하루를 차분히 마무리합니다.", recommended_time="저녁 08:00", latitude=37.5, longitude=127.0)
                ],
                [
                    Place(name=f"{dest} 감성 카페 거리", description="독창적인 인테리어와 수제 디저트가 가득한 예쁜 카페들 사이에서 한가로운 모닝커피의 여유를 즐깁니다.", recommended_time="오전 11:00", latitude=37.5, longitude=127.0),
                    Place(name=f"{dest} 자연 생태 공원", description="산들산들 부는 바람과 계절 꽃들이 드넓게 피어있어 복잡한 마음을 비우고 진정한 자연 속 힐링을 누릴 수 있는 곳입니다.", recommended_time="오후 02:30", latitude=37.5, longitude=127.0),
                    Place(name=f"{dest} 전통 시장", description="골목 구석구석 활력 넘치는 현지인들의 에너지를 느끼고, 달콤하고 짭조름한 전통 주전부리를 사먹는 소박한 재미가 있습니다.", recommended_time="오후 06:00", latitude=37.5, longitude=127.0)
                ]
            ]
            tips = ["여행지의 대표적인 전통시장이나 로컬 상점은 온누리상품권을 이용하면 10% 가까이 저렴하게 이용할 수 있습니다.", "지역 특산품은 관광지 한가운데보다 전통시장 뒷골목이나 대형마트 로컬 푸드 코너가 가성비가 훌륭합니다."]

        for d in range(1, days + 1):
            idx = (d - 1) % len(spots)
            day_spots = spots[idx]
            itinerary.append(DayItinerary(
                day=d,
                places=[
                    Place(
                        name=p.name,
                        description=p.description,
                        recommended_time=p.recommended_time,
                        latitude=p.latitude,
                        longitude=p.longitude
                    ) for p in day_spots
                ]
            ))

        return RecommendResponse(
            destination=dest,
            theme=theme,
            duration_days=days,
            itinerary=itinerary,
            tips=tips
        )
