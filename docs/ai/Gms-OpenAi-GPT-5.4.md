# gpt-5.4

## 사용방법

POST https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions

Quick Example

```bash
curl "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $GMS_KEY" \
  -d '{
    "model": "gpt-5.4",
    "messages": [
      {
        "role": "developer",
        "content": "Answer in Korean"
      },
      {
        "role": "user",
        "content": "Write a detailed implementation plan for OAuth2 SSO."
      }
    ]
  }'

```

## Reference

엔드포인트를 반드시 https://gms.ssafy.io/gmsapi/로 변경하고, 환경 변수를 사용해 호출해야 합니다.
세부 파라미터·응답 포맷은 OpenAI 공식 문서와 동일합니다.
