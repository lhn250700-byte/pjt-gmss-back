# CORS 오류 확인 방법 (오류 확인법)

`https://www.gmss.site` → `https://gmss.site/api/...` 호출 시 CORS/Preflight 오류가 날 때, 아래 순서로 확인하면 원인 파악에 도움이 됩니다.

---

## 1. 브라우저 개발자 도구로 확인

1. **F12** → **Network** 탭
2. **Preserve log** 체크 (리다이렉트/새로고침 시에도 요청 유지)
3. 페이지 새로고침 후 실패한 요청(빨간색) 클릭

### 확인할 것

| 항목 | 위치 | 정상 예시 | 비정상 시 의심 |
|------|------|-----------|----------------|
| **요청 URL** | Headers → Request URL | `https://gmss.site/api/...` | `http://gmss.site/...` 이면 80→443 리다이렉트로 Preflight 실패 가능 |
| **요청 메서드** | Request Headers | GET / POST / **OPTIONS** | OPTIONS가 301/302 받으면 "Redirect is not allowed for a preflight request" |
| **응답 상태 코드** | Status | 200, 204 | 301/302(리다이렉트), 403, 500 |
| **CORS 헤더** | Response Headers | `Access-Control-Allow-Origin: https://www.gmss.site` | 없으면 "No 'Access-Control-Allow-Origin' header" |

- **OPTIONS** 요청이 301/302를 받으면 → Preflight가 리다이렉트되는 것이므로, **요청 URL이 https인지** 먼저 확인하고, 서버(Nginx/Spring)에서 OPTIONS를 리다이렉트하지 않도록 수정해야 합니다.
- **GET/POST**는 200인데 CORS 헤더가 없으면 → Spring/Nginx가 해당 응답에 CORS 헤더를 붙이지 않는 경우(예: 에러 응답 경로)입니다.

---

## 2. curl로 서버 응답 직접 확인

로컬 터미널에서 아래처럼 호출해 **실제 응답 헤더**를 확인합니다.

### 2-1. Preflight(OPTIONS) 확인

```bash
curl -s -D - -X OPTIONS "https://gmss.site/api/bbs?page=1&limit=4" \
  -H "Origin: https://www.gmss.site" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: authorization,content-type" \
  -o /dev/null
```

**확인할 것**

- **상태 코드**: `204` 또는 `200`이어야 함. **301/302**가 나오면 Preflight 리다이렉트 문제.
- **응답 헤더**에 다음이 있어야 함:
  - `Access-Control-Allow-Origin: https://www.gmss.site`
  - `Access-Control-Allow-Credentials: true` (쿠키/인증 사용 시)

### 2-2. 실제 요청(GET) 확인

```bash
curl -s -D - "https://gmss.site/api/bbs?page=1&limit=4&del_yn=N&bbs_div=NOTI" \
  -H "Origin: https://www.gmss.site" \
  -o /dev/null
```

**확인할 것**

- **상태 코드**: 200 등 정상 코드
- **응답 헤더**에 `Access-Control-Allow-Origin: https://www.gmss.site` 있는지

### 2-3. auth/refresh 확인

```bash
curl -s -D - -X POST "https://gmss.site/api/auth/refresh" \
  -H "Origin: https://www.gmss.site" \
  -H "Content-Type: application/json" \
  -o /dev/null
```

동일하게 **상태 코드**와 **Access-Control-Allow-Origin** 존재 여부를 확인합니다.

---

## 3. 원인별 대응 요약

| 증상 | 가능 원인 | 조치 |
|------|-----------|------|
| Preflight에 301/302 | 요청이 `http://` 로 나감 → 80 포트에서 https로 리다이렉트 | 프론트엔드 BASE_URL을 반드시 `https://gmss.site` 로 통일 |
| Preflight에 301/302 | Nginx/Spring이 OPTIONS를 로그인 등으로 리다이렉트 | Nginx에서 OPTIONS는 리다이렉트 없이 204 + CORS 헤더로 응답하도록 처리 |
| 200인데 CORS 헤더 없음 | Spring 에러 응답/일부 경로에 CORS 미적용 | Nginx에서 `/api` 프록시 응답에 CORS 헤더 항상 추가 (`add_header ... always`) |
| 403/500 + CORS 없음 | 인증/예외 처리 경로에 CORS 미적용 | 동일하게 Nginx에서 CORS 헤더 추가 또는 Spring 전역 CORS/필터 점검 |

---

## 4. 재배포 후 체크리스트

1. **프론트**: `VITE_API_BASE_URL` 또는 API base URL이 **반드시 https** (`https://gmss.site`) 인지 확인. `http://` 로 요청하면 80 포트에서 301 리다이렉트되어 Preflight 오류가 납니다.
2. **백엔드**: `CORS_ORIGINS`에 `https://www.gmss.site` 포함 여부 확인 (`docker-compose.yml` 등).
3. **Nginx**: 서버에 `nginx/conf.d/00_cors_map.conf`, `nginx/conf.d/default.conf` 가 반영되었는지 확인 후 `docker compose restart nginx` 또는 `docker compose up -d --build` 로 재시작.
4. 위 **2. curl** 명령으로 OPTIONS/GET 한 번씩 호출해 응답 헤더 확인.

---

## 5. 배포 서버에서 설정 반영 여부 확인

배포 서버에 SSH 접속한 뒤, 프로젝트 디렉터리에서 아래 명령으로 확인합니다.  
(프로젝트 경로 예: `~/pjt-gmss-back`, `/opt/gmss/pjt-gmss-back` — 실제 배포 경로로 `cd` 후 실행.)

```bash
# 프로젝트 디렉터리로 이동 (실제 경로로 변경. 예: cd /opt/gmss/pjt-gmss-back)
cd /opt/gmss/pjt-gmss-back

# 1) 00_cors_map.conf 존재 여부
ls -la nginx/conf.d/00_cors_map.conf
# 있으면: -rw-r--r-- ... 00_cors_map.conf 처럼 출력
# 없으면: No such file or directory

# 2) default.conf 에 /api/ location 블록이 있는지
grep -A 2 "location /api/" nginx/conf.d/default.conf
# 정상이면 예: "location /api/" 다음에 add_header, if ($request_method = 'OPTIONS') 등이 보임

# 3) CORS 관련 add_header가 들어 있는지
grep "Access-Control-Allow-Origin" nginx/conf.d/default.conf
# 정상이면: add_header 'Access-Control-Allow-Origin' $cors_origin always; 가 보임

# 4) 한 번에 요약 확인 (이미 프로젝트 디렉터리에 있을 때)
echo "=== 00_cors_map.conf ===" && test -f nginx/conf.d/00_cors_map.conf && echo "OK 존재" || echo "없음"
echo "=== default.conf /api/ 블록 ===" && grep -c "location /api/" nginx/conf.d/default.conf && echo "개 있음 (1이면 정상)"
```

**Docker 컨테이너 안의 Nginx가 쓰는 설정**을 보고 싶다면 (볼륨 마운트로 호스트와 동일하다면 위와 같음):

```bash
docker exec gmss-nginx ls -la /etc/nginx/conf.d/
docker exec gmss-nginx grep "location /api/" /etc/nginx/conf.d/default.conf
```

---

## 6. gmss.site 요청이 백엔드가 아닌 Vercel로 가는 경우 (307 Redirect)

**증상**: `curl -I "https://gmss.site/api/..."` 또는 브라우저에서 `https://gmss.site` 호출 시  
응답이 **307 Temporary Redirect**, **Server: Vercel**, **Location: https://www.gmss.site/...** 로 나옴.

**원인**: 도메인 **gmss.site** 가 DNS 또는 호스팅 설정상 **Vercel**로 연결되어 있음.  
Vercel이 apex(gmss.site)를 www(www.gmss.site)로 307 리다이렉트하고 있어, API 요청이 실제 백엔드(AWS Nginx/Spring)에 도달하지 않음.  
→ Nginx CORS 설정을 해도 요청이 Nginx에 오지 않으므로 CORS/Preflight 오류가 계속 남.

**해결**:

1. **DNS에서 API 도메인을 백엔드 서버로 연결**
   - **gmss.site** (apex): A 레코드를 **백엔드 서버 공인 IP**(예: AWS EC2)로 설정.
   - 또는 **api.gmss.site** 서브도메인만 백엔드로 쓰고, A/CNAME으로 백엔드 서버를 가리키도록 설정.
   - 그러면 `https://gmss.site` 또는 `https://api.gmss.site` 요청이 Vercel이 아닌 **AWS Nginx**로 감.

2. **Vercel에서 gmss.site(apex) 제거**
   - Vercel 프로젝트 도메인 설정에서 **gmss.site** (apex)를 제거하거나, “Redirect to www”가 적용되지 않도록 함.
   - 프론트는 **www.gmss.site** 만 Vercel에 연결하고, **gmss.site** 는 백엔드 전용으로 둠.

3. **SSL**
   - 백엔드 서버(AWS)에서 **gmss.site** 또는 **api.gmss.site** 용 인증서를 사용 중이어야 함 (기존 certbot 사용 시 해당 도메인으로 발급).

4. **프론트엔드 API Base URL**
   - 백엔드를 **api.gmss.site** 로 쓰기로 했다면, 프론트의 `VITE_API_BASE_URL`(또는 동일 설정)을 `https://api.gmss.site` 로 변경.

정리: **gmss.site → Vercel** 이면 CORS 설정과 무관하게 API가 백엔드에 도달하지 않으므로, **gmss.site(또는 api.gmss.site)를 DNS에서 백엔드 IP로 연결**하는 것이 필수입니다.

이 순서로 확인하면 CORS/Preflight 오류 원인을 빠르게 좁힐 수 있습니다.
