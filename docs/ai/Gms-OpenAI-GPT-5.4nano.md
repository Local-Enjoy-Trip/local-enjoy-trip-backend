POST https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions


```text
curl "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $GMS_KEY" \
  -d '{
    "model": "gpt-5.4-nano",
    "messages": [
      {
        "role": "developer",
        "content": "Answer in Korean"
      },
      {
        "role": "user",
        "content": "Extract keywords from: GMS is a great API gateway system."
      }
    ]
  }'


```
