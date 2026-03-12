# pjt-gmss-back 자동 배포 (GitHub Actions)

`main` 브랜치에 push하면 배포 서버에서 `git pull` 후 `docker compose up -d --build`가 실행됩니다.

---

## 1. 배포 서버 사전 준비

1. **저장소 클론** (최초 1회)
   ```bash
   git clone https://github.com/<your-org>/<pjt-gmss-back-repo>.git ~/pjt-gmss-back
   cd ~/pjt-gmss-back
   ```
   - 비공개 저장소면 서버에 Deploy Key 또는 GitHub 토큰을 설정해 `git pull`이 되도록 해 두세요.

2. **Docker / Docker Compose** 설치되어 있어야 합니다.

3. **SSL 인증서**  
   첫 배포 시에는 `docs` 또는 루트의 docker-compose 주석대로 80만 띄운 뒤 certbot 발급 후 443을 사용하세요.

---

## 2. GitHub Secrets 설정

저장소 **Settings → Secrets and variables → Actions**에서 다음을 추가합니다.

| Secret 이름       | 필수 | 설명 |
|-------------------|------|------|
| `DEPLOY_SSH_KEY`  | 예   | 배포 서버 SSH **개인키** 전체 내용 (PEM). `cat ~/.ssh/id_rsa` 결과를 그대로 넣으면 됨. |
| `DEPLOY_HOST`     | 예   | 배포 서버 호스트 (예: `gmss.site` 또는 IP). |
| `DEPLOY_USER`     | 예   | SSH 로그인 사용자 (예: `ubuntu`, `root`). |
| `DEPLOY_PORT`     | 아니오 | SSH 포트. 없으면 22 사용. |
| `DEPLOY_PATH`     | 아니오 | 서버에서 프로젝트 경로. 없으면 `~/pjt-gmss-back` 사용. |

---

## 3. 동작 방식

- **트리거**: `main` 브랜치에 push 시 자동 실행.
- **수동 실행**: Actions 탭에서 "Deploy to server" 워크플로 선택 후 "Run workflow".
- **실행 내용**: 배포 서버에 SSH 접속 → `DEPLOY_PATH`로 이동 → `git fetch origin main` → `git reset --hard origin/main` → `docker compose up -d --build` → 사용하지 않는 이미지 정리.

---

## 4. 배포 경로를 바꾸는 경우

서버에서 다른 경로에 둔 경우(예: `/opt/gmss/pjt-gmss-back`) Secrets에 `DEPLOY_PATH`를 `/opt/gmss/pjt-gmss-back`처럼 설정하면 됩니다. `~`는 홈 디렉터리로 치환됩니다.
