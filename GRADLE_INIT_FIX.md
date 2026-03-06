# Gradle 사용 가이드 (팀 공용)

## 1. 스프링/Gradle에서 “진짜” 기준이 되는 것

이 프로젝트의 **공식 빌드·실행 도구**는 **프로젝트에 포함된 Gradle Wrapper**입니다.

| 구분 | 설명 |
|------|------|
| **Gradle Wrapper** | `gradlew`(Mac/Linux) / `gradlew.bat`(Windows) — 프로젝트 루트의 `backend` 폴더에 있음. **팀 공통으로 이걸로 빌드·실행하면 됨.** |
| **IDE의 "Refresh Gradle"** | IntelliJ, Cursor, Eclipse 등에서 “Gradle 프로젝트 다시 불러오기” 버튼. **IDE가 프로젝트를 인식하기 위한 기능**일 뿐, 스프링이나 Gradle 공식 동작이 아님. |

즉, **“Refresh Gradle이 되어야만 스프링이 되는 것”이 아니라**,  
**터미널에서 `gradlew`로 빌드/실행할 수 있으면 스프링 프로젝트는 정상**입니다.  
Refresh Gradle은 “그걸 IDE에서도 편하게 쓰기 위한 것”입니다.

---

## 2. 팀 공통으로 쓸 명령어 (권장)

아래는 **IDE와 상관없이** 모든 팀원이 동일하게 사용할 수 있는 방법입니다.

**backend 폴더로 이동 후:**

| 목적 | Windows | Mac/Linux |
|------|---------|-----------|
| **스프링 부트 실행** | `.\gradlew.bat bootRun` | `./gradlew bootRun` |
| **빌드만** | `.\gradlew.bat build` | `./gradlew build` |
| **테스트 제외 빌드** | `.\gradlew.bat build -x test` | `./gradlew build -x test` |
| **클린 빌드** | `.\gradlew.bat clean build` | `./gradlew clean build` |

이 명령들이 터미널에서 정상 동작하면 **스프링/Gradle 설정은 올바른 것**입니다.

---

## 3. "init script does not exist" 에러에 대해

에러 예시:
```text
The specified initialization script '...\redhat.java\...\init.gradle' does not exist.
```

- **원인:** Cursor(또는 Red Hat Java 확장)가 Gradle을 호출할 때 **존재하지 않는 init 스크립트 경로**를 넘기기 때문.
- **영향:** **해당 IDE에서 “Refresh Gradle” 할 때만** 실패함. **프로젝트의 Gradle 설정(build.gradle, gradlew)과는 무관**함.
- **확인:** 터미널에서 `gradlew build` 또는 `gradlew bootRun`이 성공하면, **스프링/빌드 자체는 정상**입니다.

정리하면, **“Refresh Gradle을 꼭 해야 스프링이 되는가?” → 아님.**  
**“스프링을 팀이 공통으로 쓰려면?” → 터미널에서 `gradlew` 사용을 기준으로 하면 됨.**

---

## 4. IDE별로 하고 싶을 때

### 4-1. Cursor에서 Refresh Gradle이 필요할 때

- **방법 A:** 터미널에서 `.\gradlew.bat build` 등으로 빌드해 두면, Cursor가 결과를 인식하는 경우가 많음.
- **방법 B:** 설정에서 `Java > Import > Gradle: Init Script`(또는 비슷한 이름) 항목을 **비우기**.
- **방법 C:** “Extension Pack for Java” / “Language Support for Java” 제거 후 재설치, Cursor 재시작 후 다시 Refresh Gradle.

### 4-2. IntelliJ / Eclipse / 다른 IDE

- 해당 IDE의 “Gradle 프로젝트 가져오기” 시 **이 프로젝트 루트의 `backend` 폴더**(또는 `build.gradle`이 있는 폴더)를 지정.
- IDE가 내부적으로 `gradlew`를 쓰도록 설정되어 있으면, **init script 에러는 Cursor 쪽 문제이므로 다른 IDE에서는 안 날 수 있음.**

---

## 5. 팀에서 맞춰둘 것 (요약)

1. **빌드/실행 기준:** `backend` 폴더에서 **`gradlew` / `gradlew.bat`** 사용.
2. **“Refresh Gradle”:** IDE 기능일 뿐이며, 실패해도 **터미널에서 gradlew가 성공하면 스프링 사용에는 문제 없음.**
3. **init script 에러**가 나는 사람은 Cursor 사용자일 가능성이 높고, 위 4-1 대로 조정하거나, 터미널로 빌드/실행하면 됨.

이렇게 정해두면 팀원들이 서로 다른 IDE를 써도 **스프링/Gradle 사용 기준은 하나**로 맞출 수 있습니다.
