-- ========================================
-- GMSS 백엔드 추가 테이블 생성 SQL
-- ========================================
-- Supabase SQL Editor에서 실행하세요
-- 각 테이블을 하나씩 실행하거나, 전체를 한번에 실행 가능합니다

-- ========================================
-- 1. sensitive_keywords 테이블 (민감 키워드)
-- ========================================
CREATE TABLE IF NOT EXISTS sensitive_keywords (
  keyword_id SERIAL PRIMARY KEY,
  keyword VARCHAR(100) NOT NULL UNIQUE,
  category VARCHAR(50),
  severity INTEGER CHECK (severity >= 1 AND severity <= 5),
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT NOW()
);

-- 기본 키워드 데이터 삽입
INSERT INTO sensitive_keywords (keyword, category, severity) VALUES
  ('자살', 'suicide', 5),
  ('자해', 'suicide', 5),
  ('죽고싶', 'suicide', 5),
  ('우울', 'mental_health', 3),
  ('힘들', 'mental_health', 2),
  ('폭력', 'violence', 4),
  ('위험', 'violence', 3)
ON CONFLICT (keyword) DO NOTHING;

COMMENT ON TABLE sensitive_keywords IS '민감 키워드 관리 테이블';
COMMENT ON COLUMN sensitive_keywords.severity IS '심각도 (1-5, 5가 가장 심각)';

-- ========================================
-- 2. bbs_risk 테이블 (위험 게시물 관리)
-- ========================================
CREATE TABLE IF NOT EXISTS bbs_risk (
  id SERIAL PRIMARY KEY,
  table_id VARCHAR NOT NULL,
  bbs_div VARCHAR NOT NULL,
  bbs_id INTEGER NOT NULL,
  content TEXT,
  member_id VARCHAR,
  action VARCHAR,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_risk_created ON bbs_risk(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_risk_bbs_id ON bbs_risk(bbs_id);

COMMENT ON TABLE bbs_risk IS '민감 키워드가 감지된 위험 게시물 기록';
COMMENT ON COLUMN bbs_risk.table_id IS '테이블 구분 (bbs, comment 등)';
COMMENT ON COLUMN bbs_risk.action IS '조치 내용 (삭제, 경고 등)';

-- ========================================
-- 3. activity_logs 테이블 (활동 내역)
-- ========================================
CREATE TABLE IF NOT EXISTS activity_logs (
  log_id SERIAL PRIMARY KEY,
  member_id VARCHAR,
  member_email VARCHAR,
  member_role VARCHAR,
  action_type VARCHAR(50) NOT NULL,
  target_type VARCHAR(50) NOT NULL,
  target_id INTEGER,
  description TEXT,
  ip_address VARCHAR(45),
  user_agent TEXT,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_logs_created ON activity_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_logs_member ON activity_logs(member_id);
CREATE INDEX IF NOT EXISTS idx_logs_action ON activity_logs(action_type);

COMMENT ON TABLE activity_logs IS '사용자 활동 로그 (게시글 작성, 수정, 삭제 등)';
COMMENT ON COLUMN activity_logs.action_type IS '활동 타입 (create, update, delete, login 등)';
COMMENT ON COLUMN activity_logs.target_type IS '대상 타입 (post, comment, review 등)';

-- ========================================
-- 완료 확인 쿼리
-- ========================================
-- 아래 쿼리를 실행하여 테이블이 정상적으로 생성되었는지 확인하세요
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name IN ('sensitive_keywords', 'bbs_risk', 'activity_logs')
ORDER BY table_name;

-- 민감 키워드 개수 확인
SELECT COUNT(*) as keyword_count FROM sensitive_keywords;
