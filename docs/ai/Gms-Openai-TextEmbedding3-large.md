# text-embedding-3-large

POST https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings
OpenAI의 최고 성능 텍스트 임베딩 모델입니다.
text-embedding-3-large는 영어뿐 아니라 다양한 비영어권 언어 작업에서도 강력한 성능을 발휘하는 임베딩 모델입니다.
텍스트를 고차원의 벡터로 변환하여 문장 간 유사도 분석, 의미 기반 검색, 추천 시스템, 분류, 이상 탐지, 클러스터링 등 여러 AI 응용 분야에 활용할 수 있습니다.
정확도가 중요한 임베딩 기반 시스템에서 최적의 선택지입니다.

Quick Example

```bash
curl "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $GMS_KEY" \
  -d '{
    "model": "text-embedding-3-large",
    "input": "이 문장을 벡터로 변환해서 의미 기반 유사도 검색에 활용하고 싶어요."
  }'

```

## 참고 문서

다음 문서를 참고하여 API를 사용하시면 됩니다. 다만 https://gms.ssafy.io/gmsapi/로 엔드포인트를 수정하고
GMS_KEY를 사용해서 호출하셔야 합니다.
