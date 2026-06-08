# gemini-embedding-2

## 사용 방법

POST https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/gemini-embedding-2:embedContent
Google의 차세대 고정밀 임베딩 전용 모델입니다.
gemini-embedding-2는 자연어 단어와 단락의 깊은 맥락을 인지하여 더욱 입체적이고 유의미한 수치 벡터 표현을 생성합니다. RAG(검색 증강 생성) 아키텍처에서 사용자 문장과 매뉴얼 문서 간의 정확한
코사인 유사도 연산을 지원하여 오답 비율을 획기적으로 낮추어 줍니다.

Quick Example

```bash
curl "https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/gemini-embedding-2:embedContent" \
  -H 'Content-Type: application/json' \
  -H "x-goog-api-key: $GMS_KEY" \
  -X POST \
  -d '{
    "content": {
      "parts": [
        {
          "text": "RAG 아키텍처 설계 지침 문서를 임베딩 전처리해줘."
        }
      ]
    }
  }'

```

## 참고 문서

다음 문서를 참고하여 API를 사용하시면 됩니다. 다만 https://gms.ssafy.io/gmsapi/로 엔드포인트를 수정하고
헤더 혹은 쿼리스트링에 $GMS_KEY를 삽입하여 호출해야 합니다.
