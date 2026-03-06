-- ========================================
-- 더미 상담사 · 상담 내역 테스트 데이터
-- ========================================
-- Supabase SQL Editor에서 실행하세요.
-- ROLE_SWITCHING_GUIDE에서 "상담사" 더미 로그인 후 상담 내역 리스트가 보이도록 합니다.

-- 1) 더미 회원 (상담사 · 일반회원) - member 테이블에 없으면 추가
INSERT INTO member (member_id, pw, social, nickname, created_at, updated_at)
VALUES
  ('counselor@test.com', '', true, '상담사', NOW(), NOW()),
  ('user@test.com', '', true, '일반사용자', NOW(), NOW())
ON CONFLICT (member_id) DO NOTHING;

-- 2) cnsl_id 시퀀스 맞추기 (DELETE 후 중복 키 에러 나면 반드시 실행 후 3) 실행)
-- 시퀀스 이름이 다르면 Supabase Table Editor에서 cnsl_reg → cnsl_id 컬럼의 Default 확인
SELECT setval(
  pg_get_serial_sequence('cnsl_reg', 'cnsl_id'),
  COALESCE((SELECT MAX(cnsl_id) FROM cnsl_reg), 0)
);

-- 3) 상담사(counselor@test.com)에게 들어온 상담 예약/내역 샘플 (cnsl_reg)
-- cnsl_stat: A=예약대기, B=상담예정, C=상담진행중, D=상담완료
-- (이미 같은 제목·상담사·일자 조합이 있으면 넣지 않음 → 여러 번 실행해도 에러 없음)

INSERT INTO cnsl_reg (
  member_id, cnsler_id, cnsl_tp, cnsl_cate, cnsl_dt, cnsl_start_time, cnsl_end_time,
  cnsl_stat, cnsl_title, cnsl_content, cnsl_todo_yn, del_yn, created_at, updated_at
)
SELECT 'user@test.com', 'counselor@test.com', 'chat', 'career', CURRENT_DATE + 7, '14:00', '15:00', 'A', '진로 상담 신청', '취업 준비 관련 상담 희망합니다.', 'Y', 'N', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cnsl_reg WHERE cnsler_id = 'counselor@test.com' AND cnsl_title = '진로 상담 신청' AND cnsl_dt = CURRENT_DATE + 7);

INSERT INTO cnsl_reg (
  member_id, cnsler_id, cnsl_tp, cnsl_cate, cnsl_dt, cnsl_start_time, cnsl_end_time,
  cnsl_stat, cnsl_title, cnsl_content, cnsl_todo_yn, del_yn, created_at, updated_at
)
SELECT 'user@test.com', 'counselor@test.com', 'chat', 'emotion', CURRENT_DATE + 3, '10:00', '11:00', 'B', '스트레스 관리 상담', '요즘 스트레스가 많아요.', 'Y', 'N', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cnsl_reg WHERE cnsler_id = 'counselor@test.com' AND cnsl_title = '스트레스 관리 상담' AND cnsl_dt = CURRENT_DATE + 3);

INSERT INTO cnsl_reg (
  member_id, cnsler_id, cnsl_tp, cnsl_cate, cnsl_dt, cnsl_start_time, cnsl_end_time,
  cnsl_stat, cnsl_title, cnsl_content, cnsl_todo_yn, del_yn, created_at, updated_at
)
SELECT 'user@test.com', 'counselor@test.com', 'chat', 'career', CURRENT_DATE - 1, '09:00', '10:00', 'D', '상담 완료 테스트', '상담 잘 받았습니다.', 'N', 'N', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cnsl_reg WHERE cnsler_id = 'counselor@test.com' AND cnsl_title = '상담 완료 테스트' AND cnsl_dt = CURRENT_DATE - 1);

-- 확인: 상담사 더미 로그인 후 마이페이지 → 상담 내역에서 위 3건이 보이면 성공입니다.
