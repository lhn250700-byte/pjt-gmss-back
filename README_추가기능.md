# GMSS 백엔드 추가 기능 구현 완료

## ✅ 구현 완료 항목

### 1. 게시판 CRUD
- **위치:** `com.study.spring.Bbs`
- **구현 내용:**
  - ✅ 게시글 작성 (POST /api/bbs)
  - ✅ 게시글 목록 조회 (GET /api/bbs)
  - ✅ 게시글 상세 조회 (GET /api/bbs/{id})
  - ✅ 게시글 수정 (PUT /api/bbs/{id})
  - ✅ 게시글 삭제 (DELETE /api/bbs/{id})
  - ✅ 실시간 인기글 (GET /api/bbs/popular/realtime)
  - ✅ 주간 인기글 (GET /api/bbs/popular/weekly)

### 2. 리뷰 CRUD
- **위치:** `com.study.spring.Cnsl`
- **구현 내용:**
  - ✅ 리뷰 작성 (POST /api/reviews)
  - ✅ 리뷰 목록 조회 (GET /api/reviews)
  - ✅ 리뷰 상세 조회 (GET /api/reviews/{id})
  - ✅ 리뷰 수정 (PUT /api/reviews/{id})
  - ✅ 리뷰 삭제 (DELETE /api/reviews/{id})
  - ✅ 평균 평점 조회 (GET /api/reviews/counsel/{cnslId}/average)

### 3. 민감 키워드 시스템
- **위치:** `com.study.spring.keyword`
- **구현 내용:**
  - ✅ 민감 키워드 관리 (SensitiveKeyword 엔티티)
  - ✅ 위험 게시물 기록 (BbsRisk 엔티티)
  - ✅ 게시글 작성 시 자동 키워드 검사
  - ✅ 실시간 내용 검사 API (POST /api/risks/check)
  - ✅ 키워드 목록 조회 (GET /api/keywords)
  - ✅ 키워드 추가 (POST /api/keywords)
  - ✅ 위험 게시물 목록 (GET /api/risks)
  - ✅ 위험 게시물 통계 (GET /api/risks/stats)

### 4. 활동 내역 시스템
- **위치:** `com.study.spring.activity`
- **구현 내용:**
  - ✅ 활동 로그 기록 (ActivityLog 엔티티)
  - ✅ 자동 활동 로깅 (ActivityLogger)
  - ✅ 전체 활동 내역 조회 (GET /api/activities)
  - ✅ 회원별 활동 조회 (GET /api/activities/member/{memberId})
  - ✅ 최근 활동 조회 (GET /api/activities/recent)
  - ✅ 활동 통계 (GET /api/activities/stats)

---

## 📁 프로젝트 구조

```
backend/src/main/java/com/study/spring/
├── Bbs/
│   ├── entity/Bbs.java
│   ├── repository/BbsRepository.java
│   ├── service/BbsService.java (게시판 CRUD 추가)
│   └── controller/BbsController.java (API 추가)
├── Cnsl/
│   ├── entity/Cnsl_Review.java
│   ├── repository/CnslReviewRepository.java
│   ├── service/ReviewService.java (신규)
│   └── controller/ReviewController.java (신규)
├── keyword/ (신규 패키지)
│   ├── entity/
│   │   ├── SensitiveKeyword.java
│   │   └── BbsRisk.java
│   ├── repository/
│   │   ├── SensitiveKeywordRepository.java
│   │   └── BbsRiskRepository.java
│   ├── service/KeywordService.java
│   └── controller/
│       ├── KeywordController.java
│       └── RiskController.java
└── activity/ (신규 패키지)
    ├── entity/ActivityLog.java
    ├── repository/ActivityLogRepository.java
    ├── service/ActivityLogger.java
    └── controller/ActivityController.java
```

---

## 🗄️ Supabase 테이블 생성

`backend/SUPABASE_TABLES.sql` 파일을 Supabase SQL Editor에서 실행하세요.

**생성되는 테이블:**
1. `sensitive_keywords` - 민감 키워드 관리
2. `bbs_risk` - 위험 게시물 기록
3. `activity_logs` - 사용자 활동 로그

---

## 🚀 API 엔드포인트

### 게시판 API
```
POST   /api/bbs              - 게시글 작성
GET    /api/bbs              - 게시글 목록
GET    /api/bbs/{id}         - 게시글 상세
PUT    /api/bbs/{id}         - 게시글 수정
DELETE /api/bbs/{id}         - 게시글 삭제
GET    /api/bbs/popular/realtime - 실시간 인기글
GET    /api/bbs/popular/weekly   - 주간 인기글
```

### 리뷰 API
```
POST   /api/reviews                       - 리뷰 작성
GET    /api/reviews                       - 리뷰 목록
GET    /api/reviews/{id}                  - 리뷰 상세
PUT    /api/reviews/{id}                  - 리뷰 수정
DELETE /api/reviews/{id}                  - 리뷰 삭제
GET    /api/reviews/counsel/{cnslId}/average - 평균 평점
```

### 민감 키워드 API
```
GET    /api/keywords           - 키워드 전체 목록
GET    /api/keywords/active    - 활성 키워드 목록
POST   /api/keywords           - 키워드 추가
PATCH  /api/keywords/{id}/toggle - 키워드 활성화/비활성화

POST   /api/risks/check        - 실시간 내용 검사
GET    /api/risks              - 위험 게시물 목록
GET    /api/risks/stats        - 위험 게시물 통계
GET    /api/risks/recent       - 최근 위험 게시물
```

### 활동 내역 API
```
GET    /api/activities                  - 전체 활동 내역
GET    /api/activities/member/{memberId} - 회원별 활동
GET    /api/activities/recent           - 최근 활동
GET    /api/activities/stats            - 활동 통계
GET    /api/activities/action/{actionType} - 액션별 활동
```

---

## 🔧 사용 방법

### 1. Supabase 테이블 생성
```sql
-- backend/SUPABASE_TABLES.sql 파일 실행
```

### 2. 서버 실행
```bash
# Spring Boot 애플리케이션 실행
./mvnw spring-boot:run
```

### 3. Swagger UI 접속
```
http://localhost:8080/docs
```

### 4. API 테스트 예시

#### 게시글 작성
```bash
POST http://localhost:8080/api/bbs
Content-Type: application/json
X-User-Id: test@example.com

{
  "bbs_div": "FREE",
  "title": "테스트 게시글",
  "content": "게시글 내용입니다",
  "mbti": "INFP"
}
```

#### 민감 키워드 검사
```bash
POST http://localhost:8080/api/risks/check
Content-Type: application/json

{
  "content": "너무 힘들고 우울해요"
}
```

#### 리뷰 작성
```bash
POST http://localhost:8080/api/reviews
Content-Type: application/json
X-User-Id: test@example.com

{
  "cnsl_id": 1,
  "title": "좋은 상담이었어요",
  "content": "친절하게 상담해주셔서 감사합니다",
  "eval_pt": 5
}
```

---

## ⚙️ 주요 기능

### 민감 키워드 자동 검사
- 게시글 작성 시 자동으로 민감 키워드 검사
- 키워드 발견 시 `bbs_risk` 테이블에 자동 기록
- 심각도(1-5) 기반 분류

### 활동 로그 자동 기록
- 게시글, 리뷰 작성/수정/삭제 시 자동 로깅
- IP 주소, User-Agent 자동 수집
- 액션 타입별 통계 제공

---

## 📊 기본 민감 키워드

| 키워드 | 카테고리 | 심각도 |
|--------|----------|--------|
| 자살 | suicide | 5 |
| 자해 | suicide | 5 |
| 죽고싶 | suicide | 5 |
| 우울 | mental_health | 3 |
| 힘들 | mental_health | 2 |
| 폭력 | violence | 4 |
| 위험 | violence | 3 |

---

## 🔍 테스트 시나리오

### 1. 민감 키워드 감지 테스트
1. 민감 키워드가 포함된 게시글 작성
2. `/api/risks` 에서 위험 게시물 확인
3. `/api/risks/stats` 에서 통계 확인

### 2. 활동 로그 테스트
1. 게시글 작성
2. 게시글 수정
3. 게시글 삭제
4. `/api/activities/recent` 에서 활동 확인

### 3. 리뷰 시스템 테스트
1. 상담 리뷰 작성
2. 평균 평점 조회
3. 리뷰 목록 필터링 (상담별, 회원별)

---

## 📝 참고사항

- 모든 삭제는 소프트 삭제 (del_yn = 'Y')
- 활동 로그는 실패해도 메인 기능에 영향 없음
- 민감 키워드 검사는 게시글 저장 후 수행
- 페이징은 1-based index 사용

---

## 🎯 다음 단계 제안

1. **Spring Security 추가**
   - JWT 인증 구현
   - 권한 기반 접근 제어

2. **예외 처리 개선**
   - @ControllerAdvice 전역 예외 처리
   - 커스텀 예외 클래스

3. **테스트 코드 작성**
   - JUnit 단위 테스트
   - MockMvc API 테스트

4. **성능 최적화**
   - 쿼리 최적화
   - 캐싱 전략

---

**작성일:** 2026-02-12  
**작성자:** AI Assistant  
**버전:** 1.0.0
