# Notices API

> Source: `backend/app/web/src/docs/asciidoc/index.adoc`

### HTTP Request

```http
GET /api/notices HTTP/1.1
Host: localhost:8080
```

### HTTP Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 261

{
  "data" : {
    "notices" : [ {
      "id" : 1,
      "title" : "공지",
      "content" : "내용",
      "author" : "admin",
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
$ curl 'http://localhost:8080/api/notices' -i -X GET
```
