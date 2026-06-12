# Controller Contract API

> Source: `backend/web/src/docs/asciidoc/index.adoc`

Controller request and response contracts must use named DTO objects. Raw map contracts such as `@RequestParam Map`, `@RequestBody Map`, `ApiResponse<Map<...>>`, and `Map.of(...)` response bodies are not allowed.
