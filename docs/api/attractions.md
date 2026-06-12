# Attractions API

> Source: `backend/app/web/src/docs/asciidoc/index.adoc`

### HTTP Request

```http
GET /api/attractions?sidoCode=1&keyword=%EA%B6%81 HTTP/1.1
Host: localhost:8080
```

### HTTP Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 692

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
      "favoriteCount" : 12,
      "ratingAverage" : 4.6,
      "ratingCount" : 5,
      "tags" : [ {
        "id" : 1,
        "name" : "가족여행"
      } ],
      "favorited" : true,
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

### Engagement Endpoints

인증이 필요한 요청은 `Authorization: Bearer <token>` 헤더를 사용합니다. `GET /api/attractions`와 `GET /api/attractions/{id}/stats`는 비로그인도 가능하지만, 비로그인 응답에서는 `favorited=false`, `myRating=null`입니다.

```http
PUT /api/attractions/1/favorite HTTP/1.1
Authorization: Bearer <token>
```

```http
DELETE /api/attractions/1/favorite HTTP/1.1
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
      "favoriteCount": 12,
      "ratingAverage": 4.6,
      "ratingCount": 5,
      "tags": [{ "id": 1, "name": "가족여행" }],
      "favorited": true,
      "myRating": 5
    }
  },
  "error": null
}
```

### Tag Endpoints

태그 조회는 공개 API이고, 생성/수정/삭제 및 관광지 연결은 인증이 필요합니다.

```http
GET /api/attraction-tags HTTP/1.1
```

```http
POST /api/attraction-tags HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/x-www-form-urlencoded

name=가족여행
```

```http
PUT /api/attraction-tags/1 HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/x-www-form-urlencoded

name=우정여행
```

```http
DELETE /api/attraction-tags/1 HTTP/1.1
Authorization: Bearer <token>
```

```http
PUT /api/attractions/1/tags HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/x-www-form-urlencoded

tagIds=1,2,3
```
