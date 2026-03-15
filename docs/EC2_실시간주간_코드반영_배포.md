# EC2 실시간/주간 인기글 코드 반영 및 재배포

재빌드 후에도 `[]`가 나오면 **EC2의 소스가 예전 버전**일 가능성이 큽니다. 아래 순서대로 진행하세요.

---

## 1. EC2에서 현재 소스 확인

EC2에 SSH 접속 후:

```bash
cd /opt/gmss/pjt-gmss-back

# 실시간/주간 전용 메서드가 있는지 확인 (1 이상 나와야 함)
grep -c findPopularPostsRealtime src/main/java/com/study/spring/Bbs/repository/BbsRepository.java
grep -c findPopularPostsWeekly src/main/java/com/study/spring/Bbs/repository/BbsRepository.java
```

- **0** 또는 **No such file** → EC2 소스가 구버전. 2단계로 소스 갱신 후 3단계 재빌드.
- **1 이상** → 소스는 있음. 3단계에서 `--no-cache` 로 완전 재빌드 후, 그래도 `[]`면 DB 연결/타임존 점검(4단계).

---

## 2. EC2 소스 갱신 (두 방법 중 하나)

### A. Git 사용 시

```bash
cd /opt/gmss/pjt-gmss-back
git fetch origin
git status
git pull origin main   # 또는 사용 중인 브랜치
```

### B. Git 없이 파일만 덮어쓸 때

로컬(이 레포)에서 아래 두 파일을 EC2로 복사합니다.

- `src/main/java/com/study/spring/Bbs/repository/BbsRepository.java`
- `src/main/java/com/study/spring/Bbs/service/BbsService.java`

예 (로컬 PC에서):

```bash
scp src/main/java/com/study/spring/Bbs/repository/BbsRepository.java ubuntu@<EC2_IP>:/opt/gmss/pjt-gmss-back/src/main/java/com/study/spring/Bbs/repository/
scp src/main/java/com/study/spring/Bbs/service/BbsService.java ubuntu@<EC2_IP>:/opt/gmss/pjt-gmss-back/src/main/java/com/study/spring/Bbs/service/
```

---

## 3. 캐시 없이 재빌드 후 재기동

EC2에서:

```bash
cd /opt/gmss/pjt-gmss-back
docker compose down
docker compose build --no-cache spring
docker compose up -d
```

기동 후 30초~1분 뒤에:

```bash
curl -k -H "Host: api.gmss.site" "https://127.0.0.1/api/bbs_popularPostRealtimeList?period=realtime"
curl -k -H "Host: api.gmss.site" "https://127.0.0.1/api/bbs_popularPostWeeklyList?period=week"
```

- JSON 배열에 글이 나오면 성공.
- 여전히 `[]`이면 4단계로.

---

## 4. 그래도 `[]`일 때 (DB 연결·쿼리 확인)

- **컨테이너가 쓰는 DB URL 확인**  
  JAR 안에 들어 있는 `application.properties` 또는 환경변수로 Supabase URL이 들어가는지 확인.  
  `docker compose`에 `SPRING_DATASOURCE_URL` 등이 있으면 그 값이 우선합니다.

- **Spring 로그 확인**  
  ```bash
  docker logs gmss-spring 2>&1 | tail -100
  ```  
  DB 연결 오류, SQL 예외가 있는지 확인.

- **Supabase와 동일 DB인지 확인**  
  Supabase 대시보드에서 해당 프로젝트의 Connection string(Pooler, 6543)이 EC2/컨테이너에서 사용하는 URL과 동일한지 확인.

---

## 요약

| 단계 | 할 일 |
|------|--------|
| 1 | EC2에서 `grep -c findPopularPostsRealtime` 로 소스 버전 확인 |
| 2 | 0이면 `git pull` 또는 BbsRepository.java, BbsService.java 복사로 소스 갱신 |
| 3 | `docker compose build --no-cache spring` 후 `up -d`, curl로 재확인 |
| 4 | 여전히 `[]`면 로그·DB URL·Supabase 동일 프로젝트 여부 확인 |
