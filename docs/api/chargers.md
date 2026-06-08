# Chargers API

> Source: `backend/app/src/docs/asciidoc/index.adoc`

### HTTP Request

```http
GET /api/chargers?keyword=%EC%84%9C%EC%9A%B8 HTTP/1.1
Host: localhost:8080
```

### HTTP Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 418

{
  "data" : {
    "chargers" : [ {
      "statId" : "ST001",
      "statNm" : "서울충전소",
      "chgerId" : "01",
      "chgerType" : "06",
      "addr" : "서울",
      "location" : "",
      "lat" : 37.5,
      "lng" : 127.0,
      "useTime" : "24시간",
      "busiNm" : "환경부",
      "busiCall" : "1661-9408",
      "stat" : "2"
    } ]
  },
  "error" : null,
  "success" : true
}
```


### Curl Request

```bash
$ curl 'http://localhost:8080/api/chargers?keyword=%EC%84%9C%EC%9A%B8' -i -X GET
```
