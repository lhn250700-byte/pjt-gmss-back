-- 취업지원/상담센터 초기 데이터 (이미 있으면 스킵)
INSERT INTO support_center (name, address, phone, latitude, longitude, business_hours, description, website, category)
SELECT '서울특별시 구로구청', '서울특별시 구로구 구로중앙로 1', '02-860-2114', 37.4954, 126.8876,
       '평일 09:00 - 18:00' || E'\n' || '점심시간 12:00 - 13:00' || E'\n' || '주말 및 공휴일 휴무',
       '구로구 청년 일자리 지원 및 취업 상담 제공', 'https://www.guro.go.kr', 'government'
WHERE NOT EXISTS (SELECT 1 FROM support_center WHERE name = '서울특별시 구로구청');

INSERT INTO support_center (name, address, phone, latitude, longitude, business_hours, description, website, category)
SELECT '서울시금천청년자립청소년센터', '서울특별시 금천구 시흥대로 239', '02-803-9090', 37.4563, 126.8956,
       '평일 09:00 - 18:00', '청년 자립 지원 및 취업 상담', NULL, 'youth'
WHERE NOT EXISTS (SELECT 1 FROM support_center WHERE name = '서울시금천청년자립청소년센터');

INSERT INTO support_center (name, address, phone, latitude, longitude, business_hours, description, website, category)
SELECT '화원종합사회복지관', '경기도 광명시 오리로 838', '02-2618-5757', 37.4785, 126.8666,
       '평일 09:00 - 18:00', '취업 지원 및 복지 상담', NULL, 'welfare'
WHERE NOT EXISTS (SELECT 1 FROM support_center WHERE name = '화원종합사회복지관');

INSERT INTO support_center (name, address, phone, latitude, longitude, business_hours, description, website, category)
SELECT '광명시청년정책지원센터', '경기도 광명시 광명로 844', '02-2680-6060', 37.4789, 126.8665,
       '평일 09:00 - 18:00', '청년 정책 및 취업 지원', NULL, 'support'
WHERE NOT EXISTS (SELECT 1 FROM support_center WHERE name = '광명시청년정책지원센터');
