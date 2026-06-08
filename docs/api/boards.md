# Boards API

> Source: `backend/app/src/docs/asciidoc/index.adoc`

### HTTP Request

```http
GET /api/boards HTTP/1.1
Host: localhost:8080
```

### HTTP Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 263

{
  "data" : {
    "boards" : [ {
      "id" : "b1",
      "title" : "제목",
      "content" : "내용",
      "author" : "ssafy",
      "createdAt" : "2026-05-14 11:00:00",
      "updatedAt" : ""
    } ]
  },
  "error" : null,
  "success" : true
}
```


### Curl Request

```bash
$ curl 'http://localhost:8080/api/boards' -i -X GET
```
