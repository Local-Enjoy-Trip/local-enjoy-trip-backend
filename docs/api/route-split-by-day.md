# Route Split By Day API

> Source: `backend/web/src/docs/asciidoc/index.adoc`

### HTTP Request

```http
GET /api/route/split-by-day?points=37.5665%2C126.9780%7C35.1796%2C129.0756%7C33.4996%2C126.5312&days=2 HTTP/1.1
Host: localhost:8080
```

### HTTP Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 139

{
  "data" : {
    "days" : [ [ 0 ], [ 1, 2 ] ],
    "dayDistanceKm" : [ 0.0, 299.0984 ]
  },
  "error" : null,
  "success" : true
}
```


### Curl Request

```bash
$ curl 'http://localhost:8080/api/route/split-by-day?points=37.5665%2C126.9780%7C35.1796%2C129.0756%7C33.4996%2C126.5312&days=2' -i -X GET
```
