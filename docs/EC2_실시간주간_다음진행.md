# 실시간/주간 소스 있는데도 [] 나올 때 — 다음 진행

EC2에서 `grep`으로 전용 메서드가 1개씩 확인됐는데도 API가 `[]`를 반환할 때 순서대로 진행하세요.

---

## 1. 이미지 완전 삭제 후 재빌드

캐시된 이미지가 쓰이지 않도록 이미지를 지운 뒤 다시 빌드합니다.

```bash
cd /opt/gmss/pjt-gmss-back
docker compose down
docker rmi pjt-gmss-back-spring
docker compose build --no-cache spring
docker compose up -d
```

기동 후 1분 정도 기다렸다가:

```bash
curl -k -H "Host: api.gmss.site" "https://127.0.0.1/api/bbs_popularPostRealtimeList?period=realtime"
curl -k -H "Host: api.gmss.site" "https://127.0.0.1/api/bbs_popularPostWeeklyList?period=week"
```

---

## 2. Spring 로그에서 DB 연결 확인

여전히 `[]`이면 컨테이너가 어떤 DB를 쓰는지 확인합니다.

```bash
docker logs gmss-spring 2>&1 | head -80
```

확인할 것:

- `HikariPool-1 - Start completed` → DB 연결 성공.
- `jdbc:postgresql://...pooler.supabase.com:6543/...` → Supabase Pooler 주소인지.
- `Connection refused`, `FATAL`, `password authentication failed` 등 → DB URL/계정/네트워크 문제.

---

## 3. 컨테이너에서 DB 연결 테스트 (선택)

Spring이 쓰는 DB와 Supabase가 같은지 확인하려면, 같은 호스트/포트로 접속이 되는지 봅니다.

`application.properties`에 있는 Supabase 호스트가 예: `aws-1-ap-southeast-2.pooler.supabase.com` 이라면:

```bash
# 컨테이너 안에서 해당 호스트로 연결 가능한지 (실제로는 6543 포트)
docker exec gmss-spring sh -c "apt-get update -qq && apt-get install -qq -y netcat-openbsd > /dev/null && nc -zv aws-1-ap-southeast-2.pooler.supabase.com 6543" 2>&1 || true
```

(JRE 전용 이미지라 `apt-get`이 없을 수 있음. 그럴 땐 2번 로그만으로 판단.)

---

## 4. JAR 안에 새 클래스가 들어갔는지 확인 (선택)

빌드된 JAR에 실시간/주간 전용 쿼리 메서드가 포함됐는지 확인합니다.

```bash
cd /opt/gmss/pjt-gmss-back
docker run --rm -v $(pwd)/build/libs:/jars eclipse-temurin:17-jre sh -c "cd /jars && jar tf *.jar | grep -E 'BbsRepository|BbsService'" 2>/dev/null || unzip -l build/libs/*.jar | grep -E 'BbsRepository|BbsService'
```

또는 로컬에서 `./gradlew clean bootJar` 후:

```bash
jar tf build/libs/pjt-gmss-back-*.jar | grep Bbs
```

`BbsRepository.class`, `BbsService.class` 등이 최신 수정 시각이면, 그 JAR로 이미지를 만든 뒤 배포했는지 확인합니다.

---

## 5. 요약 체크리스트

| 순서 | 작업 | 목적 |
|------|------|------|
| 1 | `docker rmi pjt-gmss-back-spring` 후 `build --no-cache spring` | 예전 이미지/캐시 제거 후 완전 재빌드 |
| 2 | `docker logs gmss-spring` | 실제 사용 중인 DB URL·연결 성공 여부 확인 |
| 3 | (선택) 컨테이너에서 Supabase 호스트:6543 연결 테스트 | 네트워크/방화벽 확인 |
| 4 | (선택) JAR 목록에서 Bbs 관련 클래스 확인 | 실시간/주간 코드가 JAR에 포함됐는지 확인 |

1번으로 대부분 해결됩니다. 그래도 `[]`이면 2번 로그에 나온 DB URL이 Supabase Pooler(6543)와 동일한지, 그리고 해당 Supabase 프로젝트의 `bbs` 테이블에 최근 1일/7일 데이터가 있는지 확인하세요.
