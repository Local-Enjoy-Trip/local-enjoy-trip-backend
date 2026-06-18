# News API

> Source: `core/core-api/src/docs/asciidoc/index.adoc`

### HTTP Request

```http
GET /api/news HTTP/1.1
Host: localhost:8080
```

### HTTP Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 287

{
  "data" : {
    "news" : [ {
      "id" : "news_1",
      "title" : "관광 뉴스",
      "link" : "https://example.com",
      "summary" : "요약",
      "source" : "관광 뉴스",
      "publishedAt" : "2026-05-14"
    } ]
  },
  "error" : null,
  "success" : true
}
```


### Curl Request

```bash
$ curl 'http://localhost:8080/api/news' -i -X GET
```
