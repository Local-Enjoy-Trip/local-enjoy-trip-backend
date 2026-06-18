# Route Optimize API

> Source: `core/core-api/src/docs/asciidoc/index.adoc`

### HTTP Request

```http
GET /api/route/optimize?points=37.5665%2C126.9780%7C35.1796%2C129.0756%7C33.4996%2C126.5312 HTTP/1.1
Host: localhost:8080
```

### HTTP Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 125

{
  "data" : {
    "order" : [ 0, 1, 2 ],
    "totalDistanceKm" : 624.2097
  },
  "error" : null,
  "success" : true
}
```


### Curl Request

```bash
$ curl 'http://localhost:8080/api/route/optimize?points=37.5665%2C126.9780%7C35.1796%2C129.0756%7C33.4996%2C126.5312' -i -X GET
```
