-- ========================================
-- Spring Boot bbs 관련 테이블 (relation "bbs" does not exist 해결용)
-- ========================================
-- Supabase SQL Editor에서 실행. member 테이블이 이미 있어야 합니다.
-- Hibernate ddl-auto=update 전에 이 스크립트를 실행하면 FK 오류를 피할 수 있습니다.

-- 1) bbs (게시글)
CREATE TABLE IF NOT EXISTS bbs (
  bbs_id       SERIAL PRIMARY KEY,
  bbs_div      VARCHAR(255) NOT NULL,
  member_id     VARCHAR(255) NOT NULL,
  mbti         VARCHAR(255),
  title        VARCHAR(255),
  content      TEXT,
  views        INTEGER DEFAULT 0,
  img_name     VARCHAR(255),
  img_url      VARCHAR(255),
  del_yn       VARCHAR(1) DEFAULT 'N',
  created_at   TIMESTAMP,
  updated_at   TIMESTAMP,
  CONSTRAINT fk_bbs_member FOREIGN KEY (member_id) REFERENCES member(member_id)
);

-- 2) bbs_comment (댓글)
CREATE TABLE IF NOT EXISTS bbs_comment (
  cmt_id       SERIAL PRIMARY KEY,
  bbs_id       INTEGER NOT NULL,
  member_id     VARCHAR(255) NOT NULL,
  content      TEXT,
  del_yn       VARCHAR(1) DEFAULT 'N',
  created_at   TIMESTAMP,
  updated_at   TIMESTAMP,
  CONSTRAINT fk_bbs_comment_bbs    FOREIGN KEY (bbs_id) REFERENCES bbs(bbs_id),
  CONSTRAINT fk_bbs_comment_member FOREIGN KEY (member_id) REFERENCES member(member_id)
);

-- 3) bbs_like (게시글 좋아요/싫어요)
CREATE TABLE IF NOT EXISTS bbs_like (
  like_id      SERIAL PRIMARY KEY,
  bbs_id       INTEGER NOT NULL,
  member_id     VARCHAR(255) NOT NULL,
  is_like      BOOLEAN,
  created_at   TIMESTAMP,
  CONSTRAINT fk_bbs_like_bbs    FOREIGN KEY (bbs_id) REFERENCES bbs(bbs_id),
  CONSTRAINT fk_bbs_like_member FOREIGN KEY (member_id) REFERENCES member(member_id)
);

-- 4) cmt_like (댓글 좋아요)
CREATE TABLE IF NOT EXISTS cmt_like (
  clike_id     SERIAL PRIMARY KEY,
  cmt_id       INTEGER NOT NULL,
  member_id     VARCHAR(255) NOT NULL,
  is_like      BOOLEAN,
  created_at   TIMESTAMP,
  CONSTRAINT fk_cmt_like_comment FOREIGN KEY (cmt_id) REFERENCES bbs_comment(cmt_id),
  CONSTRAINT fk_cmt_like_member  FOREIGN KEY (member_id) REFERENCES member(member_id)
);

-- (선택) pgvector 사용 시 bbs.embedding 컬럼 추가
-- CREATE EXTENSION IF NOT EXISTS vector;
-- ALTER TABLE bbs ADD COLUMN IF NOT EXISTS embedding vector(1536);
