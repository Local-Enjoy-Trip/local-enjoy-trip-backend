# Members API

> Source: `core/core-api/src/docs/asciidoc/index.adoc`

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
      "name" : "SSAFY",
      "nickname" : "SSAFY",
      "email" : "ssafy@example.com",
      "profileImageUrl" : null
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

## Profile image updates

`PUT /api/members/me` is no longer the profile-image mutation path. Use
[`Member Profile Images API`](member-profile-images.md) to create a presigned upload URL and persist the uploaded
`objectKey`. Member read responses continue to expose only `profileImageUrl` for display compatibility.
