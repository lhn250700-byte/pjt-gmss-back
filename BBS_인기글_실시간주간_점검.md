# 인기글 실시간/주간/추천순 미노출 원인 점검

## 1. Supabase MCP 확인 결과 (bbs 테이블)

| 항목 | 값 |
|------|-----|
| 전체 건수 | 380 |
| 최근 1일 이내 `created_at` | **9건** |
| 최근 7일 이내 | **149건** |
| 최근 1개월 이내 | 240건 |
| `del_yn = 'N'` 또는 null | 380건 전부 |
| **실시간 조건 충족** (1일 + 미삭제) | **9건** |
| **주간 조건 충족** (7일 + 미삭제) | **149건** |

- Supabase에서 **실시간(1일)/주간(7일) 조건을 만족하는 데이터는 충분히 존재**합니다.
- 동일 조건의 네이티브 쿼리로 직접 조회 시에도 9건(실시간), 149건(주간)이 정상 조회됩니다.

## 2. 프론트 연동 구조

| 탭 | API | 호출 대상 |
|----|-----|-----------|
| 실시간 | `GET /api/bbs_popularPostRealtimeList?period=realtime` | **Spring (authApi)** → `VITE_API_BASE_URL` (예: api.gmss.site) |
| 주간 | `GET /api/bbs_popularPostWeeklyList?period=week` | **Spring (authApi)** |
| 월간 | 비로그인: Spring `/api/bbs_popularPostMonthlyList` / 로그인: `GET /monthly-top` | Spring / **testchatpy(ML)** |
| 추천순 | `POST /recommend` (user_id) | **testchatpy(ML)** |

- **실시간·주간**은 항상 **Spring 백엔드**만 호출합니다.
- **추천순**은 **testchatpy(ML)** 만 호출하며, 로그인 시에만 요청합니다.

## 3. 원인 정리

### 실시간 / 주간이 나오지 않는 이유

1. **Spring이 조회하는 DB가 Supabase가 아님**  
   - 운영(api.gmss.site)의 `spring.datasource.url`이 **Supabase Postgres가 아닌 다른 DB**를 가리키면, Supabase에만 있는 위 9건/149건 데이터는 조회되지 않습니다.
   - 이 경우 실시간/주간은 빈 목록으로 나오고, 월간만 나올 수 있는 이유는  
     - 로그인 시 월간을 **testchatpy `/monthly-top`**으로 호출하고, testchatpy가 Supabase를 쓰는 경우, 또는  
     - 비로그인 월간은 Spring을 쓰는데, 해당 DB에 “한 달” 구간 데이터만 있고 “1일/7일” 구간 데이터는 없는 경우입니다.

2. **실시간/주간 전용 쿼리 미배포**  
   - 이 레포에서는 이미 `findPopularPostsRealtime()`, `findPopularPostsWeekly()` 전용 쿼리로 수정되어 있습니다.
   - 운영 서버에 **이 변경이 반영된 빌드가 배포되지 않았다면**, 예전 단일 쿼리(`:period` 바인딩)로 인해 빈 결과가 나올 수 있습니다.

### 추천순이 나오지 않는 이유

- **testchatpy**의 `POST /recommend`가  
  - 404/405/503/5xx 를 반환하거나  
  - 빈 배열을 반환하면  
  프론트는 추천순을 빈 목록으로 표시합니다.
- ML 서비스 미기동, 엔드포인트 미구현, 또는 에러 시 빈 목록 반환 로직이 원인일 수 있습니다.

## 4. 조치 사항

### Spring (실시간/주간)

1. **운영 Spring이 사용하는 DB 확인**  
   - api.gmss.site가 떠 있는 서버의 환경변수 또는 `application.properties`/`application.yml`에서  
     `spring.datasource.url` 값을 확인합니다.
   - Supabase Postgres URL(풀 연결 문자열)인지 확인합니다.
2. **Supabase가 아니면**  
   - 옵션 A: 운영 Spring의 datasource를 **Supabase Postgres**로 변경 (Supabase에 이미 데이터가 있으므로 실시간/주간 즉시 노출 가능).  
   - 옵션 B: 현재 연결된 DB에 실시간/주간용 데이터를 넣거나, 마이그레이션으로 Supabase와 동기화합니다.
3. **실시간/주간 전용 쿼리 배포 확인**  
   - `BbsRepository`의 `findPopularPostsRealtime()`, `findPopularPostsWeekly()` 사용 여부가 반영된 버전이 배포되었는지 확인합니다.

### testchatpy (추천순)

1. **서비스 기동 여부**  
   - `POST /recommend` 가 호출되는 서버(testchatpy)가 실제로 떠 있는지 확인합니다.
2. **엔드포인트 및 응답**  
   - `/recommend` 구현 여부, 정상 시 200 + 추천 목록 반환 형태인지 확인합니다.
   - 503/5xx 시 프론트는 빈 목록으로 처리하므로, 서버 로그로 에러 원인 확인이 필요합니다.

## 5. 요약

- **Supabase MCP 기준**: bbs 테이블에 실시간(1일) 9건, 주간(7일) 149건 존재하며, 원인은 **데이터 부재가 아님**.
- **실시간/주간**: Spring이 **Supabase가 아닌 DB**를 보거나, **실시간/주간 전용 쿼리 미배포**일 가능성이 큼.  
  → Spring DB를 Supabase로 통일하고, 전용 쿼리 포함된 버전을 배포하면 해결됩니다.
- **추천순**: testchatpy `/recommend` 서비스/엔드포인트 상태와 응답 형식 확인이 필요합니다.
