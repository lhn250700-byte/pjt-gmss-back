# IDE 새로고침 및 재빌드 방법

## 🔄 현재 상황
파일은 정상적으로 수정되었지만, IDE가 변경사항을 인식하지 못하고 있습니다.

## ✅ 해결 방법 (순서대로 시도)

### 방법 1: Eclipse/STS 프로젝트 새로고침

1. **프로젝트 우클릭** → **Refresh** (또는 F5 키)
2. **프로젝트 우클릭** → **Gradle** → **Refresh Gradle Project**
3. **Project** 메뉴 → **Clean...** 선택 → 프로젝트 선택 → OK
4. **Project** 메뉴 → **Build Project** (또는 자동 빌드 활성화)

### 방법 2: Gradle 클린 빌드 (터미널)

```bash
cd C:\KSJ\Fiveguys\pjt-gmss\backend

# Gradle 클린 빌드
./gradlew clean build
```

### 방법 3: 워크스페이스 완전 재빌드

1. **File** → **Restart** (또는 Eclipse/STS 종료 후 재시작)
2. 프로젝트 다시 열기
3. **Project** → **Clean...** → **Clean all projects**

### 방법 4: 수동 파일 확인

다음 파일들이 제대로 수정되었는지 직접 확인:

**확인할 파일 3개:**

#### 1. BbsRisk.java
```
C:\KSJ\Fiveguys\pjt-gmss\backend\src\main\java\com\study\spring\keyword\entity\BbsRisk.java
```

43-49번째 줄이 다음과 같아야 합니다:
```java
@CreationTimestamp
@Column(name = "created_at")
private LocalDateTime created_at;    // ← created_at (언더스코어)

@UpdateTimestamp
@Column(name = "updated_at")
private LocalDateTime updated_at;    // ← updated_at (언더스코어)
```

❌ **잘못된 예:** `private LocalDateTime createdAt;` (카멜케이스)

#### 2. ActivityLog.java
```
C:\KSJ\Fiveguys\pjt-gmss\backend\src\main\java\com\study\spring\activity\entity\ActivityLog.java
```

52-54번째 줄:
```java
@CreationTimestamp
@Column(name = "created_at")
private LocalDateTime created_at;    // ← created_at
```

#### 3. SensitiveKeyword.java
```
C:\KSJ\Fiveguys\pjt-gmss\backend\src\main\java\com\study\spring\keyword\entity\SensitiveKeyword.java
```

37-39번째 줄:
```java
@CreationTimestamp
@Column(name = "created_at")
private LocalDateTime created_at;    // ← created_at
```

---

## 🚀 재시작 후 서버 실행

1. **Run As** → **Spring Boot App** 선택
2. 또는 `Application.java` 파일에서 **Run** 버튼 클릭

---

## 🔍 여전히 안 될 경우

### Gradle Daemon 재시작
```bash
cd C:\KSJ\Fiveguys\pjt-gmss\backend
./gradlew --stop
./gradlew clean build
```

### 빌드 폴더 수동 삭제
```bash
# 터미널에서 실행
cd C:\KSJ\Fiveguys\pjt-gmss\backend
rm -r -Force bin
rm -r -Force build
rm -r -Force target
rm -r -Force .gradle
```

그 다음 Eclipse/STS를 재시작하고 프로젝트를 다시 빌드하세요.

---

## ✨ 성공 확인

서버가 정상 실행되면 다음 메시지가 표시됩니다:
```
Started Application in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

---

**참고:** 파일은 이미 정상적으로 수정되어 있으므로, IDE가 변경사항을 인식만 하면 됩니다!
