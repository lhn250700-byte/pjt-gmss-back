# 도메인/DNS 설정 정리 및 수정 사항

현재 구조: **www.gmss.site** = 프론트(Vercel), **api.gmss.site** = 백엔드(AWS 3.36.106.17).  
API 요청을 **gmss.site** 가 아닌 **api.gmss.site** 로 보내면 Vercel 307 리다이렉트를 피할 수 있습니다.

---

## 1. 확인된 설정 (이미 올바른 부분)

| 항목 | 현재 값 | 의미 |
|------|--------|------|
| **api** A 레코드 | 3.36.106.17 | api.gmss.site → 백엔드 서버 ✓ |
| **@** (gmss.site) A | 216.198.79.1 | Vercel → gmss.site 요청은 Vercel로 감 |
| Vercel **gmss.site** | 307 → www.gmss.site | 그래서 gmss.site/api/... 가 307 리다이렉트됨 |
| Vercel **www.gmss.site** | Production 연결 | 프론트 정상 |

---

## 2. DNS 확인 결과 (최신 화면 기준)

| 타입 | 호스트 | 값/위치 | 판단 |
|------|--------|---------|------|
| **A** | api | 3.36.106.17 | ✅ api.gmss.site → 백엔드 서버 정상 |
| **A** | @ | 216.198.79.1 | ✅ gmss.site → Vercel (프론트 리다이렉트용, 유지) |
| **CNAME** | www | 12bd6e17b81b4435.vercel-dns-017.com. | ✅ www.gmss.site → Vercel 정상 |

- **www** CNAME이 하나만 있음 (중복 제거된 상태). **이상 없음.**

---

## 3. 수정 사항 요약

## 3. 수정 사항 요약

| 구분 | 할 일 |
|------|--------|
| **프론트엔드** | ✅ **반영 완료** — testchat / pjt-gmss-front 의 `.env`, `.env.example`, `config.js` 에 `https://api.gmss.site` 적용. Vercel 배포 시 대시보드에서도 `VITE_API_BASE_URL=https://api.gmss.site` 설정 권장. |
| **DNS** | ✅ **이상 없음** (위 2번 표 참고) |
| **Vercel** | 변경 없음 (gmss.site 307 유지해도 됨) |
| **백엔드 Nginx** | `api.gmss.site` server_name 추가 완료. SSL은 5번 참고 |

---

## 4. DNS에서 할 일

- **www** 호스트에 CNAME이 **두 개** 있으면 충돌할 수 있습니다.
  - `www` → `gmss.site.`
  - `www` → `12bd6e17b81b4435.vercel-dns-017.com.`
- **권장**: `www.gmss.site`를 Vercel에서 쓰고 있다면 **`www` → `gmss.site.` 레코드는 삭제**하고, **`www` → `12bd6e17b81b4435.vercel-dns-017.com.` 만 유지**하세요. (이미 하나만 있으면 생략.)
- **@ (gmss.site)** A 레코드는 Vercel IP(216.198.79.1)로 두어도 됩니다. API는 **api.gmss.site** 로만 쓰면 됩니다.

---

## 5. 프론트엔드에서 할 일

- API 요청 주소를 **gmss.site** 가 아니라 **api.gmss.site** 로 통일합니다.
- **이미 반영됨**: `testchat` / `pjt-gmss-front` 의 `.env`, `.env.example`, `config.js` 에 `VITE_API_BASE_URL=https://api.gmss.site` (또는 `VITE_BACKEND_URL`) 적용해 두었습니다.
- Vercel에 배포한 경우, Vercel 대시보드 **Environment Variables** 에서 `VITE_API_BASE_URL=https://api.gmss.site` 가 설정되어 있는지 확인하고, 변경 후 **프론트 다시 빌드·배포** (push만 하면 자동 배포되는 경우 재배포 확인).

---

## 6. 백엔드(Nginx)에서 한 일

- `server_name`에 **api.gmss.site** 를 추가해 두었습니다.  
  (파일: `nginx/conf.d/default.conf`)
- **SSL**: 현재 인증서 경로는 `gmss.site` 용입니다.  
  - **api.gmss.site** 가 같은 인증서에 포함되어 있으면 그대로 사용 가능합니다.  
  - 포함되어 있지 않으면 서버에서 예시대로 발급 후 Nginx에 반영하세요.  
    ```bash
    # 예: api.gmss.site 전용 발급 (이미 gmss.site 인증서가 있을 때)
    sudo certbot certonly --webroot -w /opt/gmss/pjt-gmss-back/certbot/www -d api.gmss.site
    ```
    발급 후 `default.conf`에서 api.gmss.site 전용 server 블록을 두거나, 기존 인증서를 multi-SAN으로 다시 발급해 사용할 수 있습니다.  
    (와일드카드 `*.gmss.site` 를 쓰면 api도 한 번에 커버 가능합니다.)

---

## 7. 적용 후 확인

1. **DNS 전파 후** (몇 분 소요될 수 있음):
   ```bash
   curl -s -D - -X OPTIONS "https://api.gmss.site/api/auth/refresh" \
     -H "Origin: https://www.gmss.site" \
     -H "Access-Control-Request-Method: POST" \
     -o /dev/null
   ```
   - **204** + `Access-Control-Allow-Origin: https://www.gmss.site` 나오면 CORS·라우팅 정상.
2. 브라우저에서 **https://www.gmss.site** 접속 후 로그인/API 호출이 되는지 확인.

---

정리: **API는 api.gmss.site만 사용**하고, **프론트는 그 주소로만 요청**하도록 바꾸면, gmss.site에 대한 Vercel 307 설정은 그대로 두어도 CORS/리다이렉트 오류가 사라집니다.
