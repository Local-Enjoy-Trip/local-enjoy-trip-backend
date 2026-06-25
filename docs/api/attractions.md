# Attractions API

> Source: `core/core-api/src/docs/asciidoc/index.adoc`

### HTTP Request

```http
GET /api/attractions?sidoCode=1&keyword=%EA%B6%81 HTTP/1.1
Host: localhost:8080
```

### HTTP Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 640

{
  "data" : {
    "attractions" : [ {
      "id" : 1,
      "title" : "경복궁",
      "addr1" : "서울 종로구",
      "addr2" : "",
      "zipcode" : "",
      "tel" : "",
      "firstImage" : "",
      "firstImage2" : "",
      "readcount" : 0,
      "sidoCode" : 1,
      "gugunCode" : 1,
      "latitude" : 37.5796,
      "longitude" : 126.977,
      "mlevel" : "6",
      "contentTypeId" : "",
      "overview" : "",
      "saveCount" : 12,
      "ratingAverage" : 4.6,
      "ratingCount" : 5,
      "saved" : true,
      "myRating" : 5
    } ]
  },
  "error" : null,
  "success" : true
}
```


### Curl Request

```bash
$ curl 'http://localhost:8080/api/attractions?sidoCode=1&keyword=%EA%B6%81' -i -X GET
```

### Detail Endpoint

`GET /api/attractions/{id}`는 공개 활성 관광지의 단건 상세를 반환합니다.
로그인 사용자는 `Authorization: Bearer <token>` 헤더를 함께 보내면 `saved`, `myRating`에 본인 상태가 반영됩니다.
비로그인 응답에서는 `saved=false`, `myRating=null`입니다.

```http
GET /api/attractions/1 HTTP/1.1
Host: localhost:8080
```

```json
{
  "success": true,
  "data": {
    "attraction": {
      "id": 1,
      "title": "경복궁",
      "addr1": "서울 종로구",
      "addr2": "",
      "zipcode": "03045",
      "tel": "02-3700-3900",
      "firstImage": "https://example.com/gyeongbokgung.jpg",
      "firstImage2": "",
      "readcount": 42,
      "sidoCode": 1,
      "gugunCode": 1,
      "latitude": 37.5796,
      "longitude": 126.977,
      "mlevel": "6",
      "contentTypeId": "12",
      "overview": "조선 시대 궁궐입니다.",
      "saveCount": 12,
      "ratingAverage": 4.5,
      "ratingCount": 8,
      "saved": false,
      "myRating": null
    }
  },
  "error": null
}
```

없는 장소, 숨김 장소, 삭제 장소, 중복으로 정리된 장소는 404를 반환합니다.

### Engagement Endpoints

인증이 필요한 요청은 `Authorization: Bearer <token>` 헤더를 사용합니다. `GET /api/attractions`, `GET /api/attractions/{id}`,
`GET /api/attractions/{id}/stats`는 비로그인도 가능하지만, 비로그인 응답에서는 `saved=false`, `myRating=null`입니다.

```http
PUT /api/attractions/1/save HTTP/1.1
Authorization: Bearer <token>
```

```http
DELETE /api/attractions/1/save HTTP/1.1
Authorization: Bearer <token>
```

```http
PUT /api/attractions/1/rating HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/x-www-form-urlencoded

rating=5
```

`rating`은 1~5 정수만 허용합니다.

```http
DELETE /api/attractions/1/rating HTTP/1.1
Authorization: Bearer <token>
```

```http
GET /api/attractions/1/stats HTTP/1.1
Authorization: Bearer <token>
```

```json
{
  "success": true,
  "data": {
    "stats": {
      "attractionId": 1,
      "saveCount": 12,
      "ratingAverage": 4.6,
      "ratingCount": 5,
      "saved": true,
      "myRating": 5
    }
  },
  "error": null
}
```
