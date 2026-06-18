# Hotplaces API

> Source: `core/core-api/src/docs/asciidoc/index.adoc`

### HTTP Request

```http
GET /api/hotplaces HTTP/1.1
Host: localhost:8080
```

### HTTP Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 370

{
  "data" : {
    "hotplaces" : [ {
      "id" : "h1",
      "userId" : "ssafy",
      "title" : "남산",
      "type" : "view",
      "visitDate" : "2026-05-14",
      "lat" : 37.55,
      "lng" : 126.99,
      "description" : "야경",
      "photo" : "",
      "createdAt" : "2026-05-14 11:00:00"
    } ]
  },
  "error" : null,
  "success" : true
}
```


### Curl Request

```bash
$ curl 'http://localhost:8080/api/hotplaces' -i -X GET
```
