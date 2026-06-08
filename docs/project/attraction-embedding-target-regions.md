# Attraction embedding target regions

첫 attraction embedding backfill은 전체 지역이 아니라 다음 두 TourAPI 지역 pair로만 제한한다.

| 시도 | 시군구 | sido_code | gugun_code | 근거 |
|---|---:|---:|---:|---|
| 강원특별자치도 | 강릉시 | 32 | 1 | 한국관광공사 TourAPI KorService2 `areaCode2`/`areaBasedList2`; 공개 코드 예시는 `area --area-code 32 --sigungu-code 1`을 강릉 조회 예로 제시한다. 한국관광공사 API 출처 데이터 샘플도 `강릉 솔향수목원` 주소에 `AREACODE=32`, `SIGUNGUCODE=1`을 싣고 있다. |
| 전북특별자치도 | 전주시 | 37 | 12 | 한국관광공사 TourAPI KorService2 `areaCode2`/`areaBasedList2`; 한국관광공사 API 출처 데이터 샘플은 전주시 주소 행들에 `AREACODE=37`, `SIGUNGUCODE=12`를 싣고 있다. |

Canonical runtime artifact는 `backend/batch/src/main/resources/embedding-target-regions.yml`이다.
Live batch는 이 파일에 두 지역 외 entry가 있거나 코드 pair가 다르면 실행하지 않는다.

## Checked proof references

- KTO TourAPI CLI reference: KorService2 wrapper 문서는 `areaCode2`를 17개 광역시도 → 시군구 코드 트리로 설명하고, `areaBasedList2`가 `areaCode`/`sigunguCode` 조합으로 조회한다고 설명한다. 같은 문서의 강릉 예시는 `bash scripts/area.sh --area-code 32 --sigungu-code 1 --content-type-id 12 --num 10`이다.
  - URL: https://implexa.ai/s/clawhub/kto-tourapi-cli
- 강릉 proof sample: 데이터 출처가 한국관광공사 API인 공개 관광지 샘플에서 `강릉 솔향수목원` 행은 `ADDR1=강원도 강릉시 ...`, `AREACODE=32`, `SIGUNGUCODE=1`이다.
  - URL: https://www.bigdata-forest.kr/product/PTP009801
- 전주 proof sample: 데이터 출처가 한국관광공사 API인 공개 숙박/음식점 샘플에서 전주시 주소 행들은 `AREACODE=37`, `SIGUNGUCODE=12`이다.
  - URL: https://www.bigdata-forest.kr/product/PTP009901
  - URL: https://www.bigdata-forest.kr/product/PTP010001

주의: 현재 로컬 `.env`에는 TourAPI key가 없어 live `areaCode2` 재조회 스냅샷은 생성하지 않았다. 운영 실행 전에는 `TOUR_API_KEY` 또는 `ENJOYTRIP_TOUR_API_KEY`로 `areaCode2?areaCode=32`와 `areaCode2?areaCode=37`을 재조회해 위 pair가 유지되는지 확인한다.
