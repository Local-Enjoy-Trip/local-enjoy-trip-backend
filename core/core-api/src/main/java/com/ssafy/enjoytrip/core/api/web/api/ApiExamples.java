package com.ssafy.enjoytrip.core.api.web.api;

final class ApiExamples {
    static final String SUCCESS_VOID = """
            {
              "success": true,
              "data": null,
              "error": null
            }
            """;

    static final String ATTRACTION_STATS_RESPONSE = """
            {
              "success": true,
              "data": {
                "stats": {
                  "attractionId": 125405,
                  "saveCount": 12,
                  "ratingAverage": 4.5,
                  "ratingCount": 8,
                  "saved": true,
                  "myRating": 5,
                  "tags": [
                    {"id": 1, "name": "야경"}
                  ]
                }
              },
              "error": null
            }
            """;

    static final String ATTRACTION_TAGS_RESPONSE = """
            {
              "success": true,
              "data": {
                "tags": [
                  {"id": 1, "name": "야경"},
                  {"id": 2, "name": "산책"}
                ]
              },
              "error": null
            }
            """;

    static final String FRIENDSHIP_MUTATION_RESPONSE = """
            {
              "success": true,
              "data": {
                "friendship": {
                  "id": 10,
                  "requesterEmail": "ssafy@example.com",
                  "requesterDisplayName": "김싸피",
                  "addresseeEmail": "dongne@example.com",
                  "addresseeDisplayName": "동네친구",
                  "status": "PENDING",
                  "requestedAt": "2026-06-22T10:00:00",
                  "respondedAt": null
                }
              },
              "error": null
            }
            """;

    static final String FRIENDS_RESPONSE = """
            {
              "success": true,
              "data": {
                "friends": [
                  {
                    "friendshipId": 10,
                    "email": "dongne@example.com",
                    "displayName": "동네친구"
                  }
                ]
              },
              "error": null
            }
            """;

    static final String FRIENDSHIP_REQUESTS_RESPONSE = """
            {
              "success": true,
              "data": {
                "requests": [
                  {
                    "id": 10,
                    "requesterEmail": "ssafy@example.com",
                    "requesterDisplayName": "김싸피",
                    "addresseeEmail": "dongne@example.com",
                    "addresseeDisplayName": "동네친구",
                    "status": "PENDING",
                    "requestedAt": "2026-06-22T10:00:00",
                    "respondedAt": null
                  }
                ]
              },
              "error": null
            }
            """;

    static final String MAP_EXPLORE_RESPONSE = """
            {
              "success": true,
              "data": {
                "center": {"longitude": 126.9780, "latitude": 37.5665},
                "radiusMeters": 500.0,
                "filter": "ALL",
                "places": [
                  {
                    "id": 125405,
                    "title": "경복궁",
                    "latitude": 37.579617,
                    "longitude": 126.977041,
                    "distanceMeters": 1450.2,
                    "saveCount": 12,
                    "saved": true
                  }
                ],
                "notes": [
                  {
                    "id": 1,
                    "title": "서울 산책 메모",
                    "latitude": 37.5665,
                    "longitude": 126.9780,
                    "visibility": "PUBLIC",
                    "distanceMeters": 42.0
                  }
                ]
              },
              "error": null
            }
            """;

    static final String USERS_RESPONSE = """
            {
              "success": true,
              "data": {
                "users": [{
                  "name": "김싸피",
                  "nickname": "동네핀러",
                  "email": "ssafy@example.com",
                  "profileImageUrl": "https://cdn.example.com/profile.png"
                }]
              },
              "error": null
            }
            """;

    static final String LOGIN_RESPONSE = """
            {
              "success": true,
              "data": {
                "user": {
                  "name": "김싸피",
                  "nickname": "동네핀러",
                  "email": "Case@Test.example",
                  "profileImageUrl": "https://cdn.example.com/profile.png"
                },
                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                "tokenType": "Bearer",
                "expiresIn": 3600
              },
              "error": null
            }
            """;

    static final String OAUTH_LOGIN_RESPONSE = """
            {
              "success": true,
              "data": {
                "user": {
                  "name": "김싸피",
                  "nickname": "동네핀러",
                  "email": "ssafy@example.com",
                  "profileImageUrl": null
                },
                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                "tokenType": "Bearer",
                "expiresIn": 3600
              },
              "error": null
            }
            """;

    static final String USER_ENVELOPE_RESPONSE = """
            {
              "success": true,
              "data": {
                "user": {
                  "name": "김싸피",
                  "nickname": "동네핀러",
                  "email": "ssafy@example.com",
                  "profileImageUrl": "https://cdn.example.com/profile.png"
                }
              },
              "error": null
            }
            """;

    static final String PRESIGNED_PROFILE_IMAGE_RESPONSE = """
            {
              "success": true,
              "data": {
                "objectKey": "profiles/11/123e4567-e89b-12d3-a456-426614174000.png",
                "uploadUrl": "https://storage.example.com/profiles/11/image.png?signature=...",
                "expiresAt": "2026-06-22T10:10:00Z",
                "publicUrl": "https://cdn.example.com/profiles/11/image.png"
              },
              "error": null
            }
            """;

    static final String NOTE_RESPONSE = """
            {
              "success": true,
              "data": {
                "id": 1,
                "title": "서울 산책 메모",
                "content": "시청 근처 산책 기록",
                "category": "DAILY",
                "visibility": "PUBLIC",
                "latitude": 37.5665,
                "longitude": 126.9780,
                "regionName": "서울 중구",
                "imageObjectKey": null,
                "status": "ACTIVE",
                "createdAt": "2026-06-22T10:00:00",
                "updatedAt": "2026-06-22T10:00:00"
              },
              "error": null
            }
            """;

    static final String NOTES_RESPONSE = """
            {
              "success": true,
              "data": {
                "notes": [
                  {
                    "id": 1,
                    "title": "서울 산책 메모",
                    "content": "시청 근처 산책 기록",
                    "category": "DAILY",
                    "visibility": "PUBLIC",
                    "latitude": 37.5665,
                    "longitude": 126.9780,
                    "regionName": "서울 중구",
                    "imageObjectKey": null,
                    "status": "ACTIVE",
                    "createdAt": "2026-06-22T10:00:00",
                    "updatedAt": "2026-06-22T10:00:00"
                  }
                ]
              },
              "error": null
            }
            """;

    static final String NOTE_IMAGE_PRESIGNED_RESPONSE = """
            {
              "success": true,
              "data": {
                "objectKey": "notes/11/123e4567-e89b-12d3-a456-426614174000.jpg",
                "uploadUrl": "https://storage.example.com/notes/11/image.jpg?signature=...",
                "expiresAt": "2026-06-22T10:10:00Z",
                "publicUrl": "https://cdn.example.com/notes/11/image.jpg"
              },
              "error": null
            }
            """;

    static final String NOTIFICATIONS_RESPONSE = """
            {
              "success": true,
              "data": {
                "notifications": [
                  {
                    "id": 100,
                    "type": "FRIEND_REQUEST",
                    "referenceType": "FRIENDSHIP",
                    "referenceId": 10,
                    "payload": "dongne",
                    "read": false,
                    "readAt": null,
                    "createdAt": "2026-06-22T10:00:00"
                  }
                ]
              },
              "error": null
            }
            """;

    static final String NOTIFICATION_UNREAD_STATUS_RESPONSE = """
            {
              "success": true,
              "data": {
                "hasUnread": true
              },
              "error": null
            }
            """;

    static final String PLAN_RESPONSE = """
            {
              "success": true,
              "data": {
                "id": "p1",
                "title": "제주 여행",
                "startDate": "2026-07-01",
                "endDate": "2026-07-03",
                "budget": 300000,
                "note": "렌터카 예약",
                "routeItems": [
                  {
                    "routeId": "p1",
                    "routeItemId": 1,
                    "id": 125405,
                    "attractionId": 125405,
                    "title": "경복궁",
                    "position": 0,
                    "day": 1,
                    "memo": "오전 방문",
                    "stayMinutes": 90
                  }
                ],
                "createdAt": "2026-06-22"
              },
              "error": null
            }
            """;

    static final String TAG_REQUEST = """
            {
              "name": "야경"
            }
            """;

    static final String FRIEND_REQUEST = """
            {
              "targetEmail": "dongne@example.com"
            }
            """;

    static final String RATING_REQUEST = """
            {
              "rating": 5
            }
            """;

    static final String ATTRACTION_TAGS_REQUEST = """
            {
              "tagIds": [1, 2]
            }
            """;

    static final String BOARD_CREATE_REQUEST = """
            {
              "id": "b1",
              "title": "여행 후기",
              "content": "경복궁 야간 관람이 좋았습니다.",
              "author": "ssafy"
            }
            """;

    static final String BOARD_UPDATE_REQUEST = """
            {
              "title": "수정된 여행 후기",
              "content": "야간 관람 동선을 보완했습니다."
            }
            """;

    static final String HOTPLACE_CREATE_REQUEST = """
            {
              "id": "h1",
              "title": "성산일출봉",
              "type": "nature",
              "visitDate": "2026-07-01",
              "lat": 33.458,
              "lng": 126.942,
              "description": "일출 명소",
              "photo": "https://cdn.example.com/hotplaces/h1.jpg"
            }
            """;

    static final String MEMBER_SIGNUP_REQUEST = """
            {
              "name": "김싸피",
              "nickname": "동네핀러",
              "email": "ssafy@example.com",
              "password": "passw0rd!",
              "profileImageUrl": "https://cdn.example.com/profile.png"
            }
            """;

    static final String MEMBER_LOGIN_REQUEST = """
            {
              "email": "ssafy@example.com",
              "password": "passw0rd!"
            }
            """;

    static final String MEMBER_OAUTH_SIGNUP_REQUEST = """
            {
              "oauthSignupTicket": "oauth-signup-ticket-value",
              "name": "김싸피",
              "nickname": "동네핀러"
            }
            """;


    static final String MEMBER_UPDATE_REQUEST = """
            {
              "nickname": "새동네핀러"
            }
            """;

    static final String PROFILE_IMAGE_PRESIGNED_REQUEST = """
            {
              "contentType": "image/png",
              "fileExtension": "png"
            }
            """;

    static final String PROFILE_IMAGE_UPDATE_REQUEST = """
            {
              "objectKey": "profiles/11/123e4567-e89b-12d3-a456-426614174000.png",
              "contentType": "image/png"
            }
            """;

    static final String NOTE_REQUEST = """
            {
              "title": "서울 산책 메모",
              "content": "시청 근처 산책 기록",
              "category": "DAILY",
              "visibility": "PUBLIC",
              "latitude": 37.5665,
              "longitude": 126.9780,
              "regionName": "서울 중구",
              "image": null
            }
            """;

    static final String NOTE_IMAGE_PRESIGNED_REQUEST = """
            {
              "contentType": "image/jpeg",
              "fileExtension": "jpg"
            }
            """;

    static final String NOTICE_CREATE_REQUEST = """
            {
              "title": "서비스 점검 안내",
              "content": "6월 22일 02:00부터 점검합니다.",
              "author": "admin"
            }
            """;

    static final String NOTICE_UPDATE_REQUEST = """
            {
              "title": "서비스 점검 일정 변경",
              "content": "점검 시간이 03:00으로 변경되었습니다."
            }
            """;

    static final String PLAN_CREATE_REQUEST = """
            {
              "id": "p1",
              "title": "제주 여행",
              "startDate": "2026-07-01",
              "endDate": "2026-07-03",
              "budget": 300000,
              "note": "렌터카 예약",
              "routeItems": [
                {
                  "attractionId": 125405,
                  "day": 1,
                  "memo": "오전 방문",
                  "stayMinutes": 90
                }
              ]
            }
            """;

    static final String PLAN_UPDATE_REQUEST = """
            {
              "title": "제주 가족 여행",
              "startDate": "2026-07-01",
              "endDate": "2026-07-04",
              "budget": 350000,
              "note": "숙소 변경",
              "routeItems": [
                {
                  "attractionId": 125405,
                  "day": 1,
                  "memo": "오전 방문",
                  "stayMinutes": 120
                }
              ]
            }
            """;

    static final String PLAN_REPLACE_ITEMS_REQUEST = """
            {
              "routeItems": [
                {
                  "attractionId": 125405,
                  "day": 1,
                  "memo": "첫 번째 코스",
                  "stayMinutes": 90
                },
                {
                  "attractionId": 126508,
                  "day": 1,
                  "memo": "두 번째 코스",
                  "stayMinutes": 60
                }
              ]
            }
            """;

    private ApiExamples() {
    }
}
