# Members API

> Source: `backend/app/web/src/docs/asciidoc/index.adoc`

### HTTP Request

```http
GET /api/members HTTP/1.1
Host: localhost:8080
```

### HTTP Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 224

{
  "data" : {
    "users" : [ {
      "userId" : "ssafy",
      "name" : "SSAFY",
      "email" : "ssafy@example.com",
      "createdAt" : "2026-05-14 11:00:00"
    } ]
  },
  "error" : null,
  "success" : true
}
```


### Curl Request

```bash
$ curl 'http://localhost:8080/api/members' -i -X GET
```
