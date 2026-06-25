# 곳곳 요구사항 명세서

작성일: 2026-06-09  
최종 업데이트: 2026-06-26

이 문서는 `docs/project/동네핀.pdf`를 기준으로 곳곳 MVP 백로그를 정리한다. 기존 명칭과 기획 흐름은 더 이상 사용하지 않고, 사용자가 저장한 **장소**와 길 위에 남긴 **쪽지**를 묶어 만드는 **코스**를 핵심 개념으로 둔다.

- 기준 자료: `docs/project/동네핀.pdf`
- 로컬 문서: `docs/project/dongnepin-requirements.md`
- MVP 지역 전략: 서울 동 단위 후보 지역(`망원·합정·연남`, `성수·서울숲`, `을지로·충무로`) 우선
- 하단 탭: 홈 / 지도 / + / 보관함 / 마이

이 문서는 애자일 프로젝트 백로그로 바로 옮길 수 있도록 사용자 관점의 유저 스토리와 인수 조건 중심으로 정리한다. 우선순위는 `Must`, `Should`, `Could`로 나눈다.

## 핵심 용어

| 용어 | 정의 |
|---|---|
| 장소 | TourAPI, 운영자 큐레이션, 사용자 저장 등을 통해 지도에 표시되는 실제 방문 지점이다. 관광지, 음식점, 카페, 행사, 산책 지점 등을 포함한다. |
| 쪽지 | 사용자가 길거리, 골목, 이동 중, 장소 주변 등 특정 좌표에 남기는 위치 기반 기록이다. 장소 자체는 아니며 제목, 내용, 사진, 좌표, 태그, 공개 범위를 가진다. |
| 코스 | 사용자가 저장한 장소와 쪽지를 순서 있게 묶은 지도 기반 경로다. 코스에는 장소와 쪽지가 모두 들어갈 수 있고, 지도 경로와 태그를 가진다. |
| 보관함 | 사용자가 저장한 장소, 저장한 쪽지, 내 코스, 저장한 코스를 관리하는 공간이다. |
| 친구 | 사용자가 관계를 맺은 다른 사용자다. 친구 공개 쪽지를 볼 수 있다. |
| 공개 범위 | 쪽지의 노출 범위다. `전체공개(PUBLIC)`, `친구공개(FRIENDS)`, `나만보기(PRIVATE)`를 지원한다. |
| 태그 | 쪽지와 코스에 붙이는 분류 레이블이다. AI 제안 태그를 수동으로 수정할 수 있다. |

## 구현 상태 표기 규칙

| 상태 | 의미 |
|---|---|
| 구현됨 | 현재 backend repo에서 유저 스토리를 직접 만족하는 API, 도메인, 저장소, 테스트 근거가 있다. |
| 부분 구현 | 기반 API나 도메인은 있으나 해당 스토리를 완전히 만족하는 계약이 없거나 프론트 연동이 남아 있다. |
| 미구현 | 현재 backend repo에서 해당 기능을 찾지 못했다. |
| 결정 필요 | MVP 범위, 데이터 정책, 외부 연동 방식 등 제품/기술 결정을 먼저 내려야 한다. |
| 문서화 | 구현 기능이 아니라 요구사항, Ready/Done 기준, 동기화 규칙을 정리하는 항목이다. |

---

## Epic 1. 홈에서 오늘 주변 정보 보기

| ID | 유저 스토리 | Priority | 상태 | 구현 근거 |
|---|---|---|---|---|
| US-01 | 홈 상단에서 현재 계절과 날씨 보기 | Must | 구현됨 | `GET /api/weather/briefings`와 `GET /api/neighborhood/briefing`으로 홈 상단 날씨·AI 브리핑을 제공한다. |
| US-02 | 현재 위치 기준 가까운 장소를 인기순으로 보기 | Must | 구현됨 | `GET /api/attractions/popular-nearby`가 좌표·반경 기준 인기순 장소를 반환한다. 좌표 미전달 시 서울 시청(126.978, 37.5665) 기본값 사용. |
| US-03 | 근처 최근 쪽지 보기 | Must | 구현됨 | `GET /api/notes/nearby`가 좌표·반경 기준 최신순 접근 가능 쪽지를 반환한다. |
| US-04 | 홈 항목에서 지도 이동하기 | Must | 부분 구현 | 백엔드는 장소·쪽지 좌표를 응답에 포함한다. 홈 항목 클릭 시 지도 화면 이동과 바텀시트 오픈은 프론트 라우팅 구현이 남아 있다. |

### US-01. 홈 상단에서 현재 계절과 날씨 보기

**As a** 오늘 주변에서 갈 곳을 찾는 사용자  
**I want** 홈 맨 위에서 현재 계절과 날씨를 바로 확인하고 싶다  
**So that** 오늘 야외 활동과 주변 탐색에 적합한지 빠르게 판단할 수 있다.

**Acceptance Criteria**

- Given 사용자가 홈에 진입하면, Then 화면 최상단에 현재 계절과 현재 날씨가 표시된다.
- Then 날씨 정보에는 최소한 날씨 상태, 기온, 위치 기준 시각이 포함된다.
- Given 날씨 API가 실패했을 때, Then 마지막으로 확인된 날씨 또는 기본 안내 문구가 표시된다.
- Then 계절과 날씨는 아래의 주변 장소 추천과 최근 쪽지 섹션보다 먼저 보여야 한다.

### US-02. 현재 위치 기준 가까운 장소를 인기순으로 보기

**As a** 지금 내 주변에서 갈 만한 곳을 찾는 사용자  
**I want** 현재 위치와 가까운 장소를 인기순으로 보고 싶다  
**So that** 멀리 검색하지 않아도 주변의 검증된 장소를 빠르게 고를 수 있다.

**Acceptance Criteria**

- Given 사용자가 위치 권한을 허용하고 홈에 진입하면, Then 현재 위치 기준 가까운 장소 목록이 표시된다.
- Then 장소 목록은 거리 기준 후보 중 인기 점수순으로 정렬된다.
- Then 각 장소 항목에는 장소명, 대표 이미지 또는 기본 이미지, 거리, 인기 지표, 카테고리가 표시된다.
- Given 현재 위치를 사용할 수 없을 때, Then 서울 시청(126.978, 37.5665) 기본 좌표 기준 장소 목록을 표시한다.

### US-03. 근처 최근 쪽지 보기

**As a** 주변 사람들이 방금 남긴 이야기를 보고 싶은 사용자  
**I want** 홈 하단에서 내 근처의 최근 쪽지를 보고 싶다  
**So that** 장소 검색만으로 알 수 없는 길 위의 분위기와 팁을 발견할 수 있다.

**Acceptance Criteria**

- Given 사용자가 홈에 진입하면, Then 가까운 장소 목록 아래에 근처 최근 쪽지 섹션이 표시된다.
- Then 최근 쪽지는 현재 위치 기준 접근 가능한 쪽지 중 최신순으로 정렬된다.
- Then 쪽지 항목에는 제목, 내용 일부, 사진 여부, 거리, 작성 시각, 공개 범위에 따른 작성자 표시가 포함된다.
- Then `PRIVATE` 쪽지는 작성자 본인에게만 표시되고, `FRIENDS` 쪽지는 작성자의 수락된 친구에게만 표시된다.
- Given 근처 최근 쪽지가 없을 때, Then 빈 상태와 쪽지 남기기 CTA가 표시된다.

**Backend implementation notes**

- 홈 backend는 단일 `/api/home`이 아니라 독립 API를 조합한다.
- 날씨는 `GET /api/weather/briefings`를 사용하며, 외부 날씨 저장소가 실패하거나 비어도 기본 지역 fallback을 반환한다.
- 홈 상단 자연어 브리핑은 `GET /api/neighborhood/briefing?regionName=서울`을 사용한다. Spring AI/GMS 호출 실패나 저장된 공개 코스 후보 부재 시에도 성공 envelope와 fallback 문장을 반환한다.
- 주변 인기 장소는 `GET /api/attractions/popular-nearby`를 사용한다. 좌표가 없으면 서울 시청 좌표와 500m 반경을 사용한다.
- 주변 최근 쪽지는 `GET /api/notes/nearby`를 사용한다. 장소와 같은 서울 시청 좌표 및 500m 기본 반경을 사용한다.

### US-04. 홈 항목에서 지도 이동하기

**As a** 홈에서 마음에 드는 장소나 쪽지를 발견한 사용자  
**I want** 홈 항목을 누르면 지도에서 바로 확인하고 싶다  
**So that** 저장하거나 코스에 추가하기 전에 위치와 주변 맥락을 볼 수 있다.

**Acceptance Criteria**

- Given 사용자가 홈의 가까운 장소 항목을 선택하면, Then 지도 화면으로 이동하고 해당 장소 좌표 중심으로 표시된다.
- Given 사용자가 홈의 최근 쪽지 항목을 선택하면, Then 지도 화면으로 이동하고 해당 쪽지 좌표 중심으로 표시된다.
- Then 바텀시트에는 선택한 장소 또는 쪽지 요약, 저장 액션, 코스에 추가 액션이 표시된다.

---

## Epic 2. 지도에서 장소, 쪽지, 친구 기록 탐색하기

| ID | 유저 스토리 | Priority | 상태 | 구현 근거 |
|---|---|---|---|---|
| US-05 | 지도에서 장소 핀 탐색하기 | Must | 구현됨 | `GET /api/map/explore?filter=PLACE`가 좌표·반경 기준 장소 핀을 반환한다. |
| US-06 | 지도에서 쪽지 핀 탐색하기 | Must | 구현됨 | `GET /api/map/explore?filter=NOTE`가 접근 가능한 쪽지 핀을 반환한다. |
| US-07 | 지도 필터 사용하기 | Must | 구현됨 | `MapExploreFilter`로 `ALL / PLACE / NOTE / FRIEND / SAVED_PLACE`를 지원하고, `noteCategory`로 쪽지 하위 필터를 제공한다. |
| US-08 | 마커 클러스터 보기 | Should | 미구현 | 서버 사이드 클러스터링 집계 모델이 없다. 클라이언트 사이드 클러스터링으로 대체 가능. |
| US-09 | 지도 키워드 통합 검색하기 | Must | 구현됨 | `GET /api/map/search?keyword=...`가 장소와 쪽지를 매칭 등급·거리 기준 단일 리스트로 반환한다. |

### US-05. 지도에서 장소 핀 탐색하기

**As a** 특정 동네를 둘러보고 싶은 사용자  
**I want** 지도에서 장소 핀을 보고 싶다  
**So that** 동네 안에 어떤 장소와 경험이 있는지 직관적으로 탐색할 수 있다.

**Acceptance Criteria**

- Given 사용자가 지도 탭에 진입하면, Then 현재 위치 또는 선택 지역 기준 지도가 표시된다.
- When 사용자가 지역을 선택하면, Then 해당 지역의 장소 핀이 지도에 표시된다.
- When 사용자가 장소 핀을 선택하면, Then 바텀시트에서 장소 요약, 저장 액션, 코스에 추가 액션을 확인할 수 있다.

### US-06. 지도에서 쪽지 핀 탐색하기

**As a** 다른 사람들이 남긴 로컬 이야기를 보고 싶은 사용자  
**I want** 지도에서 쪽지 핀을 보고 싶다  
**So that** 장소 리뷰가 아닌 길 위의 감각과 팁을 발견할 수 있다.

**Acceptance Criteria**

- Given 지도 화면에서 NOTE 또는 ALL 필터가 활성화되면, Then 접근 권한이 있는 쪽지 핀이 표시된다.
- Then 쪽지 핀은 장소 핀과 구분되는 마커 타입으로 표시된다.
- When 사용자가 쪽지 핀을 선택하면, Then 제목, 내용 일부, 사진 여부, 태그, 작성자 공개 정보, 저장 액션, 코스에 추가 액션이 표시된다.

### US-07. 지도 필터 사용하기

**As a** 지도 정보가 많다고 느끼는 사용자  
**I want** 지도 필터로 보고 싶은 정보만 선택하고 싶다  
**So that** 장소, 쪽지, 친구 기록, 저장한 항목을 목적에 맞게 탐색할 수 있다.

**Acceptance Criteria**

- Given 지도 화면에 진입하면, Then `전체 / 장소 / 쪽지 / 친구 / 저장 장소` 필터가 표시된다.
- When 사용자가 `쪽지` 필터를 선택하면, Then 쪽지 카테고리 하위 필터를 추가로 선택할 수 있다.
- When 사용자가 `친구` 필터를 선택하면, Then 친구가 `FRIENDS` 또는 `PUBLIC`으로 남긴 쪽지 핀만 표시된다.
- When 사용자가 `저장 장소` 필터를 선택하면, Then 내가 저장한 장소 핀만 표시된다.

### US-08. 마커 클러스터 보기

**As a** 핀이 많은 지역을 보는 사용자  
**I want** 겹친 핀을 묶어서 보고 싶다  
**So that** 지도 화면이 복잡해지지 않게 탐색할 수 있다.

**Acceptance Criteria**

- Given 줌 아웃 상태에서 장소, 쪽지 핀이 겹치면, Then 클러스터 마커가 표시된다.
- Then 클러스터에는 타입별 개수가 표시된다.
- When 사용자가 줌 인하면, Then 개별 장소 핀과 쪽지 핀으로 분리된다.

### US-09. 지도 키워드 통합 검색하기

**As a** 특정 키워드로 주변 정보를 찾고 싶은 사용자  
**I want** 지도에서 키워드로 장소와 쪽지를 함께 검색하고 싶다  
**So that** 이름이 정확하지 않아도 근처의 관련 정보를 빠르게 발견할 수 있다.

**Acceptance Criteria**

- Given 사용자가 지도 검색창에 키워드를 입력하면, Then 주변 장소와 쪽지를 통합 검색한 결과가 표시된다.
- Then 결과는 매칭 등급 우선, 거리 차순으로 단일 리스트로 정렬된다.
- When 결과 항목을 선택하면, Then 지도에서 해당 위치로 이동하고 바텀시트가 열린다.

---

## Epic 3. 쪽지 남기고 관리하기

| ID | 유저 스토리 | Priority | 상태 | 구현 근거 |
|---|---|---|---|---|
| US-10 | 쪽지 작성하기 | Must | 구현됨 | `POST /api/notes`가 제목, 내용, 좌표, 공개 범위, 태그를 포함한 쪽지를 생성한다. |
| US-11 | 쪽지 위치 선택하기 | Must | 구현됨 | `NoteCreateRequest`의 `latitude`, `longitude` 필드로 좌표를 저장하며, 프론트에서 지도 중심점을 좌표로 전달한다. |
| US-12 | 쪽지 공개 범위 설정하기 | Must | 구현됨 | `visibility: PUBLIC / FRIENDS / PRIVATE`가 쪽지 생성·수정 요청에 포함된다. |
| US-13 | 쪽지 태그 관리하기 | Should | 구현됨 | 쪽지 생성 시 AI 제안 태그가 자동으로 붙고, `PUT /api/notes/{id}/tags`로 작성자가 수동 수정한다. |
| US-14 | 쪽지 수정/삭제하기 | Should | 구현됨 | `PUT /api/notes/{id}`로 수정, `DELETE /api/notes/{id}`로 soft delete한다. |
| US-15 | 쪽지에 이미지 첨부하기 | Should | 구현됨 | `POST /api/notes/images/presigned-upload`로 presigned URL을 발급받아 MinIO에 직접 업로드하고, `imageUrls`를 쪽지에 포함한다. |

### US-10. 쪽지 작성하기

**As a** 길 위에서 괜찮은 순간을 발견한 사용자  
**I want** 특정 좌표에 쪽지를 남기고 싶다  
**So that** 장소가 아니어도 그 순간의 감각과 팁을 기록하고 공유할 수 있다.

**Acceptance Criteria**

- Given 사용자가 하단 `+` 버튼을 누르면, Then 쪽지 작성 화면으로 이동한다.
- Then 쪽지 작성 필드는 제목, 내용, 사진 첨부, 좌표, 공개 범위, 태그를 포함한다.
- When 사용자가 필수값을 입력하고 등록하면, Then 쪽지가 저장되고 지도에 표시될 수 있다.
- Then 쪽지는 장소 엔티티로 저장하지 않고 독립적인 위치 기반 기록(`notes` 테이블)으로 저장한다.

### US-11. 쪽지 위치 선택하기

**As a** 정확한 위치에 기록을 남기고 싶은 사용자  
**I want** 지도에서 쪽지를 남길 좌표를 선택하고 싶다  
**So that** 특정 장소명이 없어도 길거리나 골목의 위치를 표현할 수 있다.

**Acceptance Criteria**

- Given 쪽지 작성 화면에 진입하면, Then 현재 위치 또는 기본 지도 중심 좌표가 제안된다.
- When 사용자가 지도를 움직이면, Then 지도 중심점이 쪽지 좌표로 선택된다.
- Then 선택된 좌표는 `latitude`, `longitude`로 저장된다.

### US-12. 쪽지 공개 범위 설정하기

**As a** 내 기록의 노출 범위를 관리하고 싶은 사용자  
**I want** 쪽지마다 공개 범위를 설정하고 싶다  
**So that** 모두에게 보여줄 기록과 친구에게만 보여줄 기록, 나만 볼 기록을 구분할 수 있다.

**Acceptance Criteria**

- Given 쪽지 작성 또는 수정 화면에 있을 때, Then 공개 범위 선택 UI가 표시된다.
- Then 공개 범위는 `PUBLIC`, `FRIENDS`, `PRIVATE` 중 하나여야 한다.
- When `PUBLIC`으로 저장하면, Then 모든 사용자가 해당 쪽지를 지도에서 볼 수 있다.
- When `FRIENDS`로 저장하면, Then 작성자의 수락된 친구만 해당 쪽지를 볼 수 있다.
- When `PRIVATE`로 저장하면, Then 작성자 본인만 해당 쪽지를 볼 수 있다.

### US-13. 쪽지 태그 관리하기

**As a** 쪽지를 탐색하는 사용자  
**I want** 쪽지가 관심사별로 분류되어 있기를 원한다  
**So that** 내가 원하는 감각의 정보만 빠르게 찾을 수 있다.

**Acceptance Criteria**

- Given 사용자가 쪽지를 작성하면, Then AI가 쪽지 제목과 내용을 분석해 태그를 자동으로 제안한다.
- Then 작성자는 `PUT /api/notes/{id}/tags`로 태그를 수동으로 수정할 수 있다.
- Then 태그는 사전 정의된 태그 목록 중에서 선택하거나, 이름으로 매핑된다.
- Then AI 태그 제안 실패 시 태그 없이 저장된다.

### US-14. 쪽지 수정/삭제하기

**As a** 내가 남긴 쪽지를 관리하고 싶은 사용자  
**I want** 쪽지 내용을 수정하거나 삭제하고 싶다  
**So that** 잘못 남긴 내용이나 더 이상 공개하고 싶지 않은 기록을 관리할 수 있다.

**Acceptance Criteria**

- Given 작성자 본인이 쪽지 상세를 볼 때, Then 수정과 삭제 액션이 표시된다.
- When 작성자가 제목, 내용, 사진, 좌표, 공개 범위를 수정하고 저장하면, Then 변경된 쪽지가 조회된다.
- When 작성자가 삭제하면, Then 지도, 보관함에서 더 이상 표시되지 않는다. (soft delete)

### US-15. 쪽지에 이미지 첨부하기

**As a** 현장 분위기를 사진으로 남기고 싶은 사용자  
**I want** 쪽지에 사진을 첨부하고 싶다  
**So that** 텍스트만으로 부족한 현장의 감각을 전달할 수 있다.

**Acceptance Criteria**

- Given 사용자가 쪽지를 작성할 때, Then presigned URL을 발급받아 이미지를 직접 업로드할 수 있다.
- Then 업로드된 이미지 URL 목록이 쪽지 응답에 포함된다.
- Then 사진이 없어도 쪽지를 저장할 수 있다.

---

## Epic 4. 보관함에서 장소와 쪽지, 코스 저장하기

| ID | 유저 스토리 | Priority | 상태 | 구현 근거 |
|---|---|---|---|---|
| US-16 | 장소 저장하기 | Must | 구현됨 | `POST /api/attractions/{id}/save`와 `DELETE /api/attractions/{id}/save`로 장소를 저장·해제한다. |
| US-17 | 쪽지 저장하기 | Must | 구현됨 | `POST /api/notes/{id}/save`와 `DELETE /api/notes/{id}/save`로 쪽지를 저장·해제한다. |
| US-18 | 코스 저장하기 | Must | 구현됨 | `POST /api/courses/{id}/save`와 `DELETE /api/courses/{id}/save`로 코스를 저장·해제한다. |
| US-19 | 보관함 보기 | Must | 구현됨 | `GET /api/attractions/saved`, `GET /api/notes/saved`, `GET /api/courses/mine`으로 각 도메인별 저장 목록을 조회한다. |

### US-16. 장소 저장하기

**As a** 나중에 다시 보고 싶은 장소를 발견한 사용자  
**I want** 장소를 저장하고 싶다  
**So that** 보관함에서 다시 확인하고 코스에 넣을 수 있다.

**Acceptance Criteria**

- Given 사용자가 장소 바텀시트 또는 장소 상세를 보고 있을 때, Then 저장 액션이 표시된다.
- When 사용자가 저장을 누르면, Then 장소가 내 보관함의 저장한 장소에 추가된다.
- Then 이미 저장한 장소는 저장된 상태로 표시된다.

### US-17. 쪽지 저장하기

**As a** 나중에 다시 보고 싶은 쪽지를 발견한 사용자  
**I want** 쪽지를 저장하고 싶다  
**So that** 여행 후에도 마음에 든 로컬 정보를 다시 확인하고 코스에 넣을 수 있다.

**Acceptance Criteria**

- Given 사용자가 접근 가능한 쪽지 카드를 보고 있을 때, Then 저장 액션이 표시된다.
- When 사용자가 저장을 누르면, Then 쪽지가 내 보관함의 저장한 쪽지에 추가된다.
- Then 이미 저장한 쪽지는 저장된 상태로 표시된다.
- Then 쪽지의 공개 범위가 변경되어 접근 권한이 사라지면, 보관함에서도 내용을 볼 수 없어야 한다.

### US-18. 코스 저장하기

**As a** 마음에 드는 다른 사람의 코스를 발견한 사용자  
**I want** 코스를 저장하고 싶다  
**So that** 나중에 다시 참고하거나 따라갈 수 있다.

**Acceptance Criteria**

- Given 사용자가 공개 코스를 보고 있을 때, Then 저장 액션이 표시된다.
- When 사용자가 저장을 누르면, Then 해당 코스가 내 저장 코스 목록에 추가된다.
- Then 저장 코스 수가 코스 상세에 반영된다.

### US-19. 보관함 보기

**As a** 저장한 로컬 정보를 관리하는 사용자  
**I want** 저장한 장소, 저장한 쪽지, 내 코스를 한 곳에서 보고 싶다  
**So that** 발견한 정보를 코스로 엮고 다시 방문할 수 있다.

**Acceptance Criteria**

- Given 사용자가 보관함 탭에 진입하면, Then 저장한 장소, 저장한 쪽지, 내 코스 목록이 구분되어 표시된다.
- Given 아직 저장한 장소나 쪽지가 없을 때, Then 빈 상태와 지도 보러가기 CTA가 표시된다.
- When 사용자가 저장한 장소나 쪽지를 선택하면, Then 상세 또는 지도 바텀시트로 이동한다.

---

## Epic 5. 코스 만들고 관리하기

| ID | 유저 스토리 | Priority | 상태 | 구현 근거 |
|---|---|---|---|---|
| US-20 | 새 코스 만들기 | Must | 구현됨 | `POST /api/courses`가 장소와 쪽지를 혼합한 `items`(ATTRACTION/NOTE 타입)로 코스를 생성한다. |
| US-21 | 코스에 장소 추가하기 | Must | 구현됨 | `CourseItemRequest`의 `itemType=ATTRACTION`으로 장소를 코스 경유지로 추가한다. |
| US-22 | 코스에 쪽지 추가하기 | Must | 구현됨 | `CourseItemRequest`의 `itemType=NOTE`로 쪽지를 코스 경유지로 추가한다. |
| US-23 | 코스 순서 편집하기 | Must | 구현됨 | `PUT /api/courses/{id}`로 `items` 목록을 교체해 순서를 수정한다. |
| US-24 | AI 동선 최적화하기 | Should | 구현됨 | `POST /api/courses/{id}/recommend-order`가 LLM 기반 순서 최적화를 수행하며 실패 시 좌표 휴리스틱 fallback으로 전환한다. |
| US-25 | AI 코스 자동 생성하기 | Should | 구현됨 | `POST /api/courses/ai-generate`가 동행자·테마·여행 속도·개수 조건을 기반으로 벡터 검색 + LLM 코스를 생성한다. |
| US-26 | 코스 삭제하기 | Must | 구현됨 | `DELETE /api/courses/{id}`로 본인 코스를 삭제한다. |

### US-20. 새 코스 만들기

**As a** 저장한 로컬 정보를 엮고 싶은 사용자  
**I want** 보관함에서 새 코스를 만들고 싶다  
**So that** 장소와 쪽지를 하나의 지도 경로로 저장할 수 있다.

**Acceptance Criteria**

- Given 사용자가 보관함의 내 코스에서 새 코스 만들기를 누르면, Then 코스 생성 화면으로 이동한다.
- Then 사용자는 코스명, 지역, 날짜를 입력할 수 있다.
- Then 사용자는 저장한 장소와 쪽지를 선택해 코스 항목으로 추가할 수 있다.
- Then 코스에 태그를 여러 개 붙일 수 있다.
- When 코스를 저장하면, Then 코스 상세 화면으로 이동한다.

### US-21. 코스에 장소 추가하기

**As a** 마음에 드는 장소를 발견한 사용자  
**I want** 장소를 코스에 추가하고 싶다  
**So that** 나중에 방문할 경로를 만들 수 있다.

**Acceptance Criteria**

- Given 사용자가 장소 바텀시트 또는 장소 상세를 보고 있을 때, Then 코스에 추가 액션이 표시된다.
- When 사용자가 코스에 추가를 누르면, Then 새 코스 생성 또는 기존 코스 선택 흐름이 제공된다.
- Then 추가된 장소는 코스 경유지 타입 `ATTRACTION`으로 저장되고 순서를 가진다.

### US-22. 코스에 쪽지 추가하기

**As a** 감각적인 기록을 코스에 넣고 싶은 사용자  
**I want** 쪽지를 코스에 추가하고 싶다  
**So that** 장소 사이의 골목, 이동 팁, 분위기 기록까지 코스에 포함할 수 있다.

**Acceptance Criteria**

- Given 사용자가 접근 가능한 쪽지를 보고 있을 때, Then 코스에 추가 액션이 표시된다.
- When 사용자가 코스에 추가를 누르면, Then 새 코스 생성 또는 기존 코스 선택 흐름이 제공된다.
- Then 추가된 쪽지는 코스 경유지 타입 `NOTE`로 저장되고 순서를 가진다.
- Then 쪽지는 장소가 아니므로 장소 ID 없이 쪽지 ID를 기준으로 코스에 포함된다.

### US-23. 코스 순서 편집하기

**As a** 추천받거나 저장한 코스를 조정하고 싶은 사용자  
**I want** 코스의 장소와 쪽지 순서를 바꾸고 싶다  
**So that** 실제 이동 동선에 맞게 코스를 고칠 수 있다.

**Acceptance Criteria**

- Given 코스 상세에서 사용자가 수정하기를 누르면, Then 편집 모드로 진입한다.
- Then 사용자는 장소와 쪽지를 추가, 삭제, 순서 변경할 수 있다.
- When 저장하면, Then 변경된 코스 상세와 지도 경로가 표시된다.

### US-24. AI 동선 최적화하기

**As a** 여러 장소와 쪽지를 코스에 담은 사용자  
**I want** AI 동선 최적화를 받고 싶다  
**So that** 이동이 자연스러운 순서로 코스를 정리할 수 있다.

**Acceptance Criteria**

- Given 코스에 둘 이상의 위치 항목이 있을 때, Then AI 동선 최적화 버튼이 표시된다.
- When 사용자가 최적화를 누르면, Then 장소 좌표와 쪽지 좌표를 함께 고려한 추천 순서가 제안된다.
- Then AI 최적화 실패 시 좌표 기반 휴리스틱(`CoordinateRouteOrderOptimizer`)으로 자동 전환한다.
- Then 사용자는 제안된 순서를 적용하거나 취소할 수 있다.

### US-25. AI 코스 자동 생성하기

**As a** 어디 갈지 고민하기 귀찮은 사용자  
**I want** AI에게 코스를 추천받고 싶다  
**So that** 취향과 동행자에 맞는 동네 코스를 빠르게 받을 수 있다.

**Acceptance Criteria**

- Given 사용자가 AI 코스 생성 화면에 진입하면, Then 동네, 동행자, 테마, 여행 속도, 방문지 개수를 입력할 수 있다.
- When 요청을 보내면, Then AI가 벡터 유사도 검색으로 후보 장소를 추리고 LLM이 코스를 구성해 미리보기로 반환한다.
- Then 미리보기에는 코스 제목, 추천 이유, 장소 목록, AI 제안 태그가 포함된다.
- Then 사용자는 미리보기를 확인하고 코스로 저장할 수 있다.

### US-26. 코스 삭제하기

**As a** 내 코스를 정리하고 싶은 사용자  
**I want** 불필요한 코스를 삭제하고 싶다  
**So that** 보관함을 깔끔하게 유지할 수 있다.

**Acceptance Criteria**

- Given 코스 상세에서 작성자가 삭제를 누르면, Then 코스가 삭제된다.
- Then 본인 코스가 아니면 삭제할 수 없다.

---

## Epic 6. 코스 피드와 추천

| ID | 유저 스토리 | Priority | 상태 | 구현 근거 |
|---|---|---|---|---|
| US-27 | 주변 공개 코스 피드 보기 | Must | 구현됨 | `GET /api/courses/feed`가 좌표 기준 거리순 공개 코스 피드를 반환한다. |
| US-28 | 인기 코스 보기 | Should | 구현됨 | `GET /api/courses/popular-feed?regionName=...`이 저장 수 기준 동네 인기 코스를 반환한다. |
| US-29 | 개인화 코스 추천 받기 | Should | 구현됨 | `GET /api/courses/recommendations`가 개인화 임베딩 벡터 기반 코스를 추천한다. 임베딩 부재 시 동네 기반 fallback. |

### US-27. 주변 공개 코스 피드 보기

**As a** 내 주변의 좋은 코스를 발견하고 싶은 사용자  
**I want** 현재 위치 근처의 공개 코스 목록을 보고 싶다  
**So that** 내 주변에서 지금 당장 따라갈 수 있는 코스를 찾을 수 있다.

**Acceptance Criteria**

- Given 사용자가 코스 피드 화면에 진입하면, Then 현재 위치 기준 거리순으로 공개 코스 목록이 표시된다.
- Then 각 코스 카드에는 코스명, 지역, 경유지 요약, 저장 수, 태그가 표시된다.

### US-28. 인기 코스 보기

**As a** 이미 검증된 코스를 찾고 싶은 사용자  
**I want** 특정 동네의 인기 코스 목록을 보고 싶다  
**So that** 다른 사람들이 좋아하는 코스를 참고할 수 있다.

**Acceptance Criteria**

- Given 사용자가 동네를 선택하면, Then 해당 동네의 저장 수 기준 인기 코스가 표시된다.

### US-29. 개인화 코스 추천 받기

**As a** 내 취향에 맞는 코스를 받고 싶은 사용자  
**I want** 내가 저장한 장소와 쪽지를 기반으로 코스 추천을 받고 싶다  
**So that** 내 취향과 비슷한 코스를 빠르게 발견할 수 있다.

**Acceptance Criteria**

- Given 사용자가 장소·쪽지 저장 이력이 있을 때, Then 취향 임베딩 벡터 기반 코스 추천 목록이 표시된다.
- Given 저장 이력이 없을 때, Then 동네 기반 fallback 코스 목록이 표시된다.

---

## Epic 7. 친구와 공개 범위 기반으로 공유하기

| ID | 유저 스토리 | Priority | 상태 | 구현 근거 |
|---|---|---|---|---|
| US-30 | 친구 추가하기 | Must | 구현됨 | `POST /api/friendships/request`로 친구 요청을 보내고, `PUT /api/friendships/{id}/accept`로 수락한다. |
| US-31 | 친구 목록 관리하기 | Must | 구현됨 | 친구 목록 조회, 받은 요청, 보낸 요청, 요청 거절·취소, 친구 끊기 API가 모두 제공된다. |
| US-32 | 친구 공개 쪽지 보기 | Must | 구현됨 | 쪽지 조회 API는 viewer의 친구 관계를 확인해 `FRIENDS` 쪽지 노출 여부를 결정한다. |
| US-33 | 코스 초대 보내기 | Should | 구현됨 | `POST /api/courses/{id}/invitations`로 친구를 코스에 초대하고, 수락 시 상대방 코스 목록에 추가된다. |
| US-34 | 알림 확인하기 | Should | 구현됨 | `GET /api/notifications`가 미처리 친구 요청 알림을 반환하고, `GET /api/notifications/unread-status`로 미읽음 여부를 확인한다. |

### US-30. 친구 추가하기

**As a** 지인과 로컬 기록을 공유하고 싶은 사용자  
**I want** 친구를 추가하고 싶다  
**So that** 친구 공개 쪽지를 서로 볼 수 있다.

**Acceptance Criteria**

- Given 사용자가 친구 추가 화면에 진입하면, Then 닉네임 또는 이메일로 사용자를 검색할 수 있다.
- When 사용자가 친구 요청을 보내면, Then 상대방에게 요청 상태가 생성되고 알림이 전송된다.
- When 상대방이 요청을 수락하면, Then 두 사용자는 친구 관계가 된다.

### US-31. 친구 목록 관리하기

**As a** 친구 관계를 관리하는 사용자  
**I want** 친구 목록과 요청 목록을 관리하고 싶다  
**So that** 공유 범위를 신뢰할 수 있는 사람에게 제한할 수 있다.

**Acceptance Criteria**

- Given 사용자가 마이페이지의 친구 관리에 진입하면, Then 친구 목록, 받은 요청, 보낸 요청이 표시된다.
- When 사용자가 친구를 끊으면, Then 양쪽의 FRIENDS 공개 쪽지 접근 권한이 사라진다.
- When 사용자가 받은 요청을 거절하거나 보낸 요청을 취소하면, Then 요청이 제거된다.

### US-32. 친구 공개 쪽지 보기

**As a** 친구의 로컬 기록이 궁금한 사용자  
**I want** 친구가 공개한 쪽지를 지도에서 보고 싶다  
**So that** 지인의 감각과 팁을 내 탐색에 활용할 수 있다.

**Acceptance Criteria**

- Given 사용자가 지도에서 FRIEND 필터를 선택하면, Then 친구가 `FRIENDS` 또는 `PUBLIC`으로 남긴 쪽지가 표시된다.
- Then 친구가 `PRIVATE`로 남긴 쪽지는 표시되지 않는다.
- When 친구 관계가 끊기면, Then 기존에 보이던 FRIENDS 쪽지는 더 이상 조회되지 않는다.

### US-33. 코스 초대 보내기

**As a** 친구와 코스를 함께 공유하고 싶은 사용자  
**I want** 친구를 내 코스에 초대하고 싶다  
**So that** 친구가 내 코스를 받아서 함께 따라갈 수 있다.

**Acceptance Criteria**

- Given 코스 호스트가 친구를 초대하면, Then 친구에게 초대 알림이 발송된다.
- When 친구가 초대를 수락하면, Then 해당 코스가 친구의 코스 목록에 추가된다.
- When 친구가 초대를 거절하면, Then 초대 상태가 거절됨으로 변경된다.

### US-34. 알림 확인하기

**As a** 친구 요청을 놓치고 싶지 않은 사용자  
**I want** 받은 알림을 확인하고 싶다  
**So that** 친구 요청을 적시에 처리할 수 있다.

**Acceptance Criteria**

- Given 사용자가 알림 화면에 진입하면, Then 미처리 친구 요청 알림 목록이 표시된다.
- Then 알림에는 요청 보낸 사람 정보와 요청 시각이 포함된다.
- When 사용자가 알림에서 수락 또는 거절을 누르면, Then 해당 알림이 처리됨으로 변경된다.
- Then 탭 아이콘에서 미읽음 알림 존재 여부를 확인할 수 있다.

---

## Epic 8. AI 추천과 개인화

| ID | 유저 스토리 | Priority | 상태 | 구현 근거 |
|---|---|---|---|---|
| US-35 | 홈 AI 브리핑 보기 | Must | 구현됨 | `GET /api/neighborhood/briefing`이 지역·날씨·저장된 공개 코스 기반 자연어 문장을 반환한다. |
| US-36 | 개인화 관광지 추천 받기 | Should | 구현됨 | `GET /api/attractions/recommendations`가 사용자 임베딩 벡터 기반 관광지를 추천한다. |
| US-37 | 개인화 쪽지 추천 받기 | Should | 구현됨 | `GET /api/notes/recommendations`가 사용자 임베딩 벡터 기반 PUBLIC 쪽지를 추천한다. |
| US-38 | 취향 임베딩 자동 갱신하기 | Should | 구현됨 | 장소 저장·해제, 쪽지 작성·삭제 이벤트 시 비동기로 `member_profile_embeddings`를 갱신한다. |

### US-35. 홈 AI 브리핑 보기

**As a** 오늘 동네 분위기가 궁금한 사용자  
**I want** 홈 상단에서 동네와 날씨를 반영한 자연어 브리핑을 보고 싶다  
**So that** 오늘 어디를 가면 좋을지 빠르게 감을 잡을 수 있다.

**Acceptance Criteria**

- Given 사용자가 홈에 진입하면, Then 지역과 날씨 정보를 반영한 4줄 내외의 자연어 브리핑이 표시된다.
- Then 브리핑은 해요체 구어체로 작성되고 근거 없는 장소를 생성하지 않는다.
- Given LLM 호출이 실패하면, Then 로컬 fallback 문장이 표시되고 API는 성공 envelope를 반환한다.
- Then 동일 지역·시간대 반복 호출은 캐시를 활용한다.

### US-36. 개인화 관광지 추천 받기

**As a** 내 취향에 맞는 관광지를 발견하고 싶은 사용자  
**I want** 저장 이력 기반으로 관광지를 추천받고 싶다  
**So that** 매번 검색하지 않아도 취향에 맞는 장소를 빠르게 찾을 수 있다.

**Acceptance Criteria**

- Given 사용자가 저장한 장소 이력이 있으면, Then 임베딩 벡터 기반 유사 관광지 목록이 추천된다.
- Given 저장 이력이 없으면, Then 인기순 기본 관광지가 반환된다.

### US-37. 개인화 쪽지 추천 받기

**As a** 내 취향과 비슷한 쪽지를 발견하고 싶은 사용자  
**I want** 내 저장 이력 기반으로 쪽지를 추천받고 싶다  
**So that** 관심사에 맞는 로컬 이야기를 빠르게 발견할 수 있다.

**Acceptance Criteria**

- Given 사용자가 저장·작성 이력이 있으면, Then 임베딩 벡터 기반 유사 PUBLIC 쪽지 목록이 추천된다.
- Given 이력이 없으면, Then 최근 PUBLIC 쪽지가 반환된다.

### US-38. 취향 임베딩 자동 갱신하기

**As a** 계속 서비스를 사용하는 사용자  
**I want** 추천이 내 최근 활동을 반영하기를 원한다  
**So that** 저장하고 기록할수록 추천이 정확해지는 경험을 할 수 있다.

**Acceptance Criteria**

- Given 사용자가 장소를 저장하거나 쪽지를 작성하면, Then 비동기로 사용자 취향 임베딩이 갱신된다.
- Then 임베딩 갱신은 API 응답을 blocking하지 않는다.
- Then 갱신 실패 시 기존 임베딩을 유지하고 로그를 남긴다.

---

## Epic 9. 마이페이지와 회원 관리

| ID | 유저 스토리 | Priority | 상태 | 구현 근거 |
|---|---|---|---|---|
| US-39 | 회원 가입하기 | Must | 구현됨 | `POST /api/members/signup`으로 이메일·비밀번호 기반 가입, Google OAuth2 가입도 지원한다. |
| US-40 | 로그인하기 | Must | 구현됨 | `POST /api/members/login`으로 JWT 토큰 발급, Google OAuth2 로그인도 지원한다. |
| US-41 | 프로필 수정하기 | Should | 구현됨 | `PUT /api/members/me`로 기본 정보를 수정하고, presigned URL로 프로필 이미지를 교체한다. |
| US-42 | 활동 통계 보기 | Could | 미구현 | 완료 코스, 저장 장소, 저장/작성 쪽지 통계 모델이 없다. |

### US-39. 회원 가입하기

**As a** 서비스를 처음 사용하는 사용자  
**I want** 계정을 만들고 싶다  
**So that** 쪽지, 코스, 친구 기능을 모두 사용할 수 있다.

**Acceptance Criteria**

- Given 사용자가 이름, 이메일, 비밀번호를 입력하면, Then 계정이 생성된다.
- Then 이미 사용 중인 이메일은 가입을 거부한다.
- Given 사용자가 Google 계정으로 로그인을 시도하면, Then OAuth2 흐름으로 자동 가입·로그인된다.

### US-40. 로그인하기

**As a** 기존 계정이 있는 사용자  
**I want** 로그인하고 싶다  
**So that** 내 쪽지, 코스, 친구 데이터를 이용할 수 있다.

**Acceptance Criteria**

- Given 이메일과 비밀번호가 정확히 일치하면, Then JWT access token이 발급된다.
- Given 잘못된 자격증명이면, Then 인증 실패 오류가 반환된다.

### US-41. 프로필 수정하기

**As a** 서비스를 사용하는 사용자  
**I want** 내 프로필 사진과 기본 정보를 수정하고 싶다  
**So that** 친구에게 보이는 내 정보를 관리할 수 있다.

**Acceptance Criteria**

- Given 사용자가 마이페이지에 진입하면, Then 프로필 사진과 기본 정보가 표시된다.
- When 사용자가 프로필을 수정하고 저장하면, Then 변경된 프로필이 조회된다.
- Then presigned URL로 프로필 이미지를 직접 업로드할 수 있다.

---

## 기술/데이터 스토리

| ID | 유저 스토리 | Priority | 상태 | 구현 근거 |
|---|---|---|---|---|
| TS-01 | 공식 관광 데이터 연동하기 | Must | 구현됨 | TourAPI client와 attraction storage가 구현되어 있고, keyword expansion 배치로 임베딩 텍스트를 확장한다. |
| TS-02 | 날씨 기반 추천 정보 반영하기 | Must | 구현됨 | OpenWeatherMap 기반 날씨 브리핑과 fallback이 구현되어 있고, AI 홈 브리핑에 날씨 정보가 주입된다. |
| TS-03 | 초기 큐레이션 데이터 운영하기 | Must | 부분 구현 | AdminPageController/AdminPlaceController로 운영자 화면이 있으나, AI 생성 데이터와 운영자 데이터 구분 추적은 확인 필요다. |
| TS-04 | 쪽지와 사용자 생성 데이터 관리하기 | Must | 부분 구현 | soft delete, visibility 제어가 있고 `status` 필드로 숨김 처리가 가능하나 신고 API는 없다. |
| TS-05 | 공개 범위 권한 검증하기 | Must | 구현됨 | 쪽지 조회 API 전체에서 viewer의 인증 상태와 친구 관계를 기반으로 `PUBLIC/FRIENDS/PRIVATE` 노출 여부를 검증한다. |
| TS-06 | 임베딩 대상 지역 제한하기 | Must | 구현됨 | `batch/src/main/resources/embedding-target-regions.yml`과 validator가 구현되어 있고, 실행 전 지역 검증이 강제된다. |
| TS-07 | 이미지 업로드 관리하기 | Must | 구현됨 | 쪽지 이미지와 프로필 이미지 모두 MinIO presigned URL 방식으로 클라이언트가 직접 업로드한다. |
| TS-08 | 쪽지·코스 임베딩 파이프라인 운영하기 | Must | 구현됨 | 쪽지 생성·수정 이벤트 시 `NoteEmbeddingRequestedEvent`로 비동기 임베딩이 저장되고, 코스는 `CourseEmbeddingScheduler`가 주기적으로 처리한다. |

### TS-01. 공식 관광 데이터 연동하기

**As a** 서비스 운영자  
**I want** 한국관광공사 관광지, 음식점, 행사, 축제 데이터를 연동하고 싶다  
**So that** 초기에도 안정적인 장소 후보 데이터를 제공할 수 있다.

**Acceptance Criteria**

- Given 관광 데이터 API 키가 설정되어 있을 때, Then 지정한 지역의 관광 데이터를 수집할 수 있다.
- Then 수집 데이터에는 위치, 카테고리, 이미지, 상세 정보가 포함되어야 한다.
- Then API 실패 시 재시도 또는 실패 로그가 남아야 한다.

### TS-02. 날씨 기반 추천 정보 반영하기

**As a** 사용자  
**I want** 현재 날씨가 홈 브리핑에 반영되기를 원한다  
**So that** 오늘 실제로 가기 좋은 곳을 판단할 수 있다.

**Acceptance Criteria**

- Given 날씨 API가 정상 응답할 때, Then 홈 AI 브리핑에 날씨 정보가 반영된다.
- Given 날씨 API가 실패할 때, Then 기본 문구 또는 마지막으로 확인된 날씨 기반 fallback이 제공된다.

### TS-03. 초기 큐레이션 데이터 운영하기

**As a** 서비스 운영자  
**I want** 초기 MVP 지역의 장소, 브리핑, 추천 코스 초안을 직접 관리하고 싶다  
**So that** 사용자 콘텐츠가 부족해도 서비스 품질을 유지할 수 있다.

**Acceptance Criteria**

- Given 운영자가 큐레이션 데이터를 등록하면, Then 홈, 지도, 코스 추천에 노출될 수 있다.
- Then 운영자 데이터는 AI 생성 데이터와 구분해 추적 가능해야 한다.
- Then 잘못된 추천 문구는 운영자가 수정할 수 있어야 한다.

### TS-04. 쪽지와 사용자 생성 데이터 관리하기

**As a** 서비스 운영자  
**I want** 사용자가 남긴 쪽지와 사진을 관리하고 싶다  
**So that** 부적절한 콘텐츠를 제어하고 추천 품질을 유지할 수 있다.

**Acceptance Criteria**

- Given 사용자가 쪽지를 등록하면, Then 작성자, 제목, 내용, 사진, 좌표, 태그, 공개 범위, 생성 시각이 저장된다.
- Then soft delete로 삭제해도 데이터를 복구할 수 있다.
- Then 개인정보와 위치 데이터 보관 정책을 적용할 수 있어야 한다.

### TS-05. 공개 범위 권한 검증하기

**As a** 개발자  
**I want** 쪽지 조회 시 공개 범위를 항상 검증하고 싶다  
**So that** FRIENDS와 PRIVATE 쪽지가 잘못 노출되지 않게 할 수 있다.

**Acceptance Criteria**

- Given 쪽지가 `PRIVATE`일 때, Then 작성자 외 사용자는 목록·상세·지도·검색 응답에서 볼 수 없다.
- Given 쪽지가 `FRIENDS`일 때, Then 작성자 본인과 수락된 친구만 볼 수 있다.
- Given 쪽지가 `PUBLIC`일 때, Then 로그인 사용자와 anonymous viewer가 볼 수 있다.
- Then 모든 목록 API와 상세 API는 동일한 공개 범위 정책을 적용한다.

### TS-06. 임베딩 대상 지역 제한하기

**As a** 개발자  
**I want** attraction embedding backfill 대상 지역을 명확히 제한하고 싶다  
**So that** 예상하지 못한 대량 데이터 처리와 잘못된 지역 처리를 방지할 수 있다.

**Acceptance Criteria**

- Given `embedding-target-regions.yml`이 있을 때, Then 지정된 지역 외 entry가 있으면 batch가 실행되지 않아야 한다.
- Then 곳곳 MVP 후보 지역은 서울 동 단위 후보 지역을 우선 검토한다.

### TS-07. 이미지 업로드 관리하기

**As a** 사용자  
**I want** 쪽지에 사진을 첨부하고 프로필 이미지를 변경하고 싶다  
**So that** 텍스트만으로 부족한 현장 분위기를 남길 수 있다.

**Acceptance Criteria**

- Given 사용자가 쪽지를 작성할 때, Then presigned URL을 발급받아 MinIO에 직접 이미지를 업로드할 수 있다.
- Then 업로드된 이미지 URL이 쪽지 응답에 포함된다.
- Given 사용자가 프로필 이미지를 변경할 때, Then 같은 방식으로 프로필 이미지를 교체할 수 있다.

### TS-08. 쪽지·코스 임베딩 파이프라인 운영하기

**As a** 개발자  
**I want** 쪽지와 코스가 생성·수정될 때 자동으로 임베딩이 저장되기를 원한다  
**So that** 개인화 추천에서 최신 콘텐츠를 반영할 수 있다.

**Acceptance Criteria**

- Given 쪽지가 생성 또는 수정되면, Then 비동기로 `note_embeddings`가 갱신된다.
- Given 코스가 변경되면, Then `CourseEmbeddingScheduler`가 주기적으로 `course_embeddings`를 갱신한다.
- Then 임베딩 실패 시 추천 결과에는 최신순 fallback이 사용된다.
