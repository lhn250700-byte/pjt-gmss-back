# EC2 배포 시 실시간/주간 인기글 점검 체크리스트

DB(Supabase) 연결이 맞고 EC2에 배포 중일 때, 실시간/주간이 안 나오면 아래를 순서대로 확인하세요.

---

## 1. 배포된 코드 버전 (가장 흔한 원인)

**확인**: EC2에 올라간 JAR(또는 이미지)가 **실시간/주간 전용 쿼리**가 들어간 최신 빌드인지.

- 이 레포의 `BbsRepository`에는 `findPopularPostsRealtime()`, `findPopularPostsWeekly()` 메서드가 있음.
- 예전에는 `findPopularPosts(period)` 하나로만 처리했고, 그때는 파라미터 바인딩 이슈로 빈 결과가 났을 수 있음.

**조치**:
1. 로컬에서 `./mvnw clean package -DskipTests` (또는 사용 중인 빌드 명령)로 **새로 빌드**.
2. 생성된 JAR를 EC2에 올리고 **재기동**.
3. 재배포 후 브라우저에서 인기글 탭 실시간/주간 다시 확인.

---

## 2. EC2 서버에서 API 직접 호출

**목적**: Spring이 정상 응답하는지, DB에서 데이터를 가져오는지 확인.

**Docker 사용 시**: Spring 컨테이너는 기본적으로 8080을 호스트에 안 붙이므로, 아래 중 하나로 확인.

### A. nginx 경유 (80/443)

```bash
# Host 헤더로 api.gmss.site 블록 사용 (-k: 로컬이므로 인증서 검증 생략)
curl -k -H "Host: api.gmss.site" "https://127.0.0.1/api/bbs_popularPostRealtimeList?period=realtime"
curl -k -H "Host: api.gmss.site" "https://127.0.0.1/api/bbs_popularPostWeeklyList?period=week"
```

### B. 8080 직접 (docker-compose에 ports: '8080:8080' 추가 후)

`docker compose up -d` 로 재기동한 뒤:

```bash
# 실시간 (최근 1일)
curl "http://localhost:8080/api/bbs_popularPostRealtimeList?period=realtime"

# 주간 (최근 7일)
curl "http://localhost:8080/api/bbs_popularPostWeeklyList?period=week"
```

- **`[]` 빈 배열**: DB에 조건에 맞는 글이 없거나, 아직 예전 쿼리 버전이 배포된 것일 수 있음 → 1번(코드 버전) 재확인 및 재배포.
- **JSON 배열에 글이 나옴**: 백엔드는 정상. 프론트 요청 URL/캐시/네트워크 확인(3, 4번).

---

## 3. EC2 → Supabase 연결

**확인**: Spring이 올라간 EC2에서 Supabase Postgres(aws-1-ap-southeast-2.pooler.supabase.com:6543)로 **아웃바운드**가 되는지.

- EC2 보안 그룹 **Outbound**: 최소 6543(또는 5432) 포트가 인터넷(0.0.0.0/0)으로 허용되어 있는지.
- Supabase는 일반적으로 공개 연결을 허용하므로, EC2에서 **나가는** 연결만 허용되면 됨.

**조치**: 2번 curl에서 데이터가 안 나오고, 로그에 DB 연결 오류가 있으면 여기부터 점검.

---

## 4. 실행 시 설정 적용 여부 (환경변수 덮어쓰기)

**확인**: EC2에서 앱을 실행할 때 `application.properties`의 `spring.datasource.url`이 **다른 값으로 덮어쓰이지 않는지**.

- systemd 서비스 파일, Docker `environment`, 또는 `java -jar` 전에 설정하는 `SPRING_DATASOURCE_URL` 등이 있으면 **그게 우선**.
- 운영에서는 보통 환경변수로 비밀번호 등을 넣는데, URL까지 바꿔서 **다른 DB**를 가리키면 실시간/주간 데이터가 없을 수 있음.

**조치**: 실제 실행 명령/환경변수 목록을 확인하고, `spring.datasource.url`이 Supabase URL로 적용되는지 확인.

---

## 5. 프론트 요청이 EC2로 가는지

**확인**: 브라우저에서 인기글을 요청할 때 사용하는 도메인이 **실제로 해당 EC2(또는 그 앞단 로드밸런서)**로 가는지.

- 예: `https://api.gmss.site` → DNS/로드밸런서가 EC2를 가리키는지.
- 다른 서버(예: 예전 배포 서버)를 가리키면, DB를 맞춰도 그 서버는 예전 코드/설정일 수 있음.

**조치**: api.gmss.site의 DNS/로드밸런서 설정이 현재 배포한 EC2를 바라보는지 확인.

---

## 6. (선택) 캐시

**확인**: 인기글 서비스에 **캐시**(예: 90초 TTL)가 있으면, 예전에 빈 결과가 캐시됐을 수 있음.

**조치**: 1번에서 최신 JAR로 재기동하면 캐시도 초기화됨. 재배포 후 1~2분 지나서 다시 실시간/주간 탭을 열어보기.

---

## 요약

| 순서 | 확인 항목 | 조치 |
|------|-----------|------|
| 1 | 배포된 JAR에 실시간/주간 전용 쿼리 포함 여부 | 최신 코드로 다시 빌드 후 EC2에 배포·재기동 |
| 2 | EC2에서 실시간/주간 API 직접 호출 결과 | `curl`로 응답 확인, 데이터 나오면 백엔드 정상 |
| 3 | EC2 → Supabase 아웃바운드 | 보안 그룹에서 6543(또는 5432) 허용 확인 |
| 4 | EC2 실행 시 datasource URL | 환경변수/설정이 Supabase URL로 적용되는지 확인 |
| 5 | api.gmss.site가 현재 EC2를 가리키는지 | DNS/로드밸런서 설정 확인 |
| 6 | 캐시 | 재기동 후 1~2분 뒤 다시 요청 |

**가장 먼저 할 것**: **1번(최신 JAR 재배포)**과 **2번(EC2에서 curl로 API 호출)** 확인.

---

## 보안 권장 (운영)

`application.properties`에 DB 비밀번호를 그대로 두지 말고, EC2에서는 **환경변수**로 넣는 것을 권장합니다.

- 예: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- 파일에는 `spring.datasource.password=비밀번호`를 제거하거나, 설정 파일을 Git에 올리지 않고 EC2에만 배포하는 방식 사용.
