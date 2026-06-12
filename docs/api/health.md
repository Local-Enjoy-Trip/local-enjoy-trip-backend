# Health API

> Source: `backend/web/src/docs/asciidoc/index.adoc`

### HTTP Request

```http
GET /health HTTP/1.1
Host: localhost:8080
```

### HTTP Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 84

{
  "data" : {
    "status" : "ok"
  },
  "error" : null,
  "success" : true
}
```


### Curl Request

```bash
$ curl 'http://localhost:8080/health' -i -X GET
```
