# Courses API

## Create course

`POST /api/courses`

Request item order is the order of the `items` array. The compatibility `position` field may still be sent by old clients, but it is ignored and must not determine storage order.

```json
{
  "id": "course-1",
  "title": "서울 산책",
  "visibility": "PRIVATE",
  "status": "READY",
  "items": [
    {"itemType": "ATTRACTION", "attractionId": 1},
    {"itemType": "ATTRACTION", "attractionId": 2}
  ]
}
```

Response keeps server-generated item ids and positions. Create does not run route planning or coordinate optimization; `routeSummary.segmentCount`, duration, and distance remain zero until a recommendation preview computes an in-memory route.

## Update course

`PUT /api/courses/{id}`

Use the `items` array order to save a changed order. Request item `position` is ignored for compatibility; response item `position` remains server-generated. Normal update only persists the user's chosen order and clears stored route segments; optimization is opt-in through the recommendation endpoint.

## Preview AI order recommendation

`POST /api/courses/{id}/order-recommendation`

Returns `ApiResponse<CourseResponse>` for the authenticated owner. The endpoint is preview-only: it does not persist course, item, or route-segment rows. Accepted preview order is saved by sending the desired `items` array through the existing `PUT /api/courses/{id}` endpoint.

The response does not expose `recommendationSource`, `fallbackReason`, provider, or similar metadata. If AI recommendation fails, returns an ordinary success-shaped `CourseResponse` using the internal coordinate fallback or the current stored order when coordinates are insufficient.
