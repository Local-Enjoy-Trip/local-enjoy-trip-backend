# Plans API

> Source: `backend/app/src/docs/asciidoc/index.adoc`

### HTTP Request

```http
GET /api/plans HTTP/1.1
Host: localhost:8080
```

### HTTP Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 362

{
  "data" : {
    "plans" : [ {
      "id" : "p1",
      "userId" : "ssafy",
      "title" : "서울 여행",
      "startDate" : "2026-05-14",
      "endDate" : "2026-05-15",
      "budget" : 100000,
      "note" : "메모",
      "routeItems" : [ {
        "routeId" : "10",
        "routeItemId" : 10,
        "id" : 1,
        "attractionId" : 1,
        "title" : "경복궁",
        "addr1" : "서울 종로구",
        "position" : 1,
        "day" : 1,
        "memo" : "오전 방문",
        "stayMinutes" : 120,
        "favoriteCount" : 12,
        "ratingAverage" : 4.6,
        "ratingCount" : 5,
        "tags" : [ ]
      } ],
      "createdAt" : "2026-05-14 11:00:00"
    } ]
  },
  "error" : null,
  "success" : true
}
```


### Curl Request

```bash
$ curl 'http://localhost:8080/api/plans' -i -X GET
```

### Course Management

코스 변경 API는 인증이 필요하며 JWT subject가 계획의 `userId`와 일치해야 합니다. Mutation API는 JSON request body만 사용하고, 서버는 `routeItems` 배열을 `plan_items` 테이블에 정규화해 저장합니다.

```http
GET /api/plans/p1 HTTP/1.1
Authorization: Bearer <token>
```

```http
POST /api/plans/items HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/json

{
  "id": "p1",
  "title": "서울 여행",
  "startDate": "2026-05-14",
  "endDate": "2026-05-15",
  "budget": 100000,
  "note": "오전 출발",
  "routeItems": [
    {
      "attractionId": 1,
      "day": 1,
      "memo": "오전 방문",
      "stayMinutes": 120
    }
  ]
}
```

```http
PUT /api/plans/p1 HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "서울 수정",
  "startDate": "2026-05-14",
  "endDate": "2026-05-16",
  "routeItems": [
    {"attractionId": 1},
    {"attractionId": 2}
  ]
}
```

```http
PUT /api/plans/p1/items HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/json

{
  "routeItems": [
    {"attractionId": 1, "day": 1, "stayMinutes": 90},
    {"attractionId": 2, "day": 1, "stayMinutes": 60}
  ]
}
```

```http
DELETE /api/plans/p1/items/10 HTTP/1.1
Authorization: Bearer <token>
```

```http
DELETE /api/plans/p1 HTTP/1.1
Authorization: Bearer <token>
```
