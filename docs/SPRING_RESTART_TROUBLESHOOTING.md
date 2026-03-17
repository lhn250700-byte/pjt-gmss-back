# Spring 컨테이너 반복 재시작 시 점검

`docker ps` 에서 `gmss-spring` 이 **Up 10 seconds** → **Up 2 seconds** 처럼 자꾸 리셋되면, 프로세스가 종료된 뒤 `restart: always` 로 다시 올라가는 상태입니다.

## 1. 로그로 원인 확인

```bash
cd /opt/gmss/pjt-gmss-back
docker logs gmss-spring --tail 300
```

끝부분의 **Exception**, **Caused by**, **Error** 를 확인하세요.

## 2. 자주 나오는 원인

| 로그/메시지 | 원인 | 조치 |
|-------------|------|------|
| `Failed to configure a DataSource` | DB 설정 없음/오류 | `src/main/resources/application.properties` 의 `spring.datasource.*` 확인 |
| `Connection refused` / `Connection timed out` | Supabase(DB)에 연결 불가 | EC2 보안 그룹 **아웃바운드** 에서 6543 포트 허용. Supabase URL/포트 확인. |
| `FATAL: password authentication failed` | DB 비밀번호 불일치 | Supabase 대시보드 → Settings → Database 에서 Connection string 비밀번호와 `application.properties` 의 `spring.datasource.password` 일치 여부 확인 |
| `OutOfMemoryError` / `Killed` | 메모리 부족 | JVM 힙 제한 또는 Docker 메모리 한도 설정 (아래 참고) |
| `Address already in use` / `8080` | 포트 충돌 | `ss -tlnp \| grep 8080` 로 사용 중인 프로세스 확인 후 종료 또는 포트 변경 |
| `BeanCreationException` / `OAuth2AuthorizationRequestResolver` / `CustomSecurityConfig` | OAuth2·Security 빈 생성 실패 | 카카오 리졸버·ClientRegistrationRepository 등 Security 설정 확인. `docs/카카오_로그인_설정.md` 참고. |

## 3. OOM일 때: JVM 메모리 제한

`docker-compose.yml` 에서:

```yaml
  spring:
    build: { context: . }
    container_name: gmss-spring
    environment:
      - JAVA_OPTS=-Xms256m -Xmx512m
      # ... 기존 환경변수
    deploy:
      resources:
        limits:
          memory: 768M
```

`Dockerfile` 마지막 줄을 다음처럼 변경:

```dockerfile
ENTRYPOINT ["sh","-c","java ${JAVA_OPTS:-} -jar /app/app.jar"]
```

이미지에 `sh` 가 없으면 `eclipse-temurin` 기본 경로의 `sh` 를 사용하거나, `ENTRYPOINT` 에 그대로 `java -Xmx512m -jar /app/app.jar` 처럼 고정값을 넣어도 됩니다.

## 4. 재시작만 잠시 멈추고 싶을 때

```bash
docker update gmss-spring --restart=no
```

원인 수정 후 다시 켜려면:

```bash
docker update gmss-spring --restart=always
docker start gmss-spring
```

---

상세 내용은 프로젝트 루트 `docs/API_ERROR_401_500.md` §0 "Spring 컨테이너가 계속 재부팅될 때" 를 참고하세요.
