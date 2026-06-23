-- 1. auth_logs 테이블 변경
ALTER TABLE auth_logs ADD COLUMN member_id bigint;
UPDATE auth_logs a SET member_id = m.id FROM members m WHERE a.user_id = m.user_id;
DELETE FROM auth_logs WHERE member_id IS NULL;
ALTER TABLE auth_logs DROP COLUMN user_id;
ALTER TABLE auth_logs ALTER COLUMN member_id SET NOT NULL;

-- 2. hotplaces 테이블 변경
ALTER TABLE hotplaces ADD COLUMN member_id bigint;
UPDATE hotplaces h SET member_id = m.id FROM members m WHERE h.user_id = m.user_id;
DELETE FROM hotplaces WHERE member_id IS NULL;
ALTER TABLE hotplaces DROP COLUMN user_id;
ALTER TABLE hotplaces ALTER COLUMN member_id SET NOT NULL;

-- 3. plans 테이블 변경
ALTER TABLE plans ADD COLUMN member_id bigint;
UPDATE plans p SET member_id = m.id FROM members m WHERE p.user_id = m.user_id;
DELETE FROM plans WHERE member_id IS NULL;
ALTER TABLE plans DROP COLUMN user_id;
ALTER TABLE plans ALTER COLUMN member_id SET NOT NULL;

-- 4. attraction_saves 테이블 변경
ALTER TABLE attraction_saves DROP CONSTRAINT IF EXISTS fk_attraction_saves_member;
ALTER TABLE attraction_saves DROP CONSTRAINT IF EXISTS uk_attraction_saves_attraction_user;
ALTER TABLE attraction_saves ADD COLUMN member_id bigint;
UPDATE attraction_saves s SET member_id = m.id FROM members m WHERE s.user_id = m.user_id;
DELETE FROM attraction_saves WHERE member_id IS NULL;
ALTER TABLE attraction_saves DROP COLUMN user_id;
ALTER TABLE attraction_saves ALTER COLUMN member_id SET NOT NULL;
ALTER TABLE attraction_saves ADD CONSTRAINT fk_attraction_saves_member FOREIGN KEY (member_id) REFERENCES members (id);
ALTER TABLE attraction_saves ADD CONSTRAINT uk_attraction_saves_attraction_member UNIQUE (attraction_id, member_id);

-- 5. attraction_ratings 테이블 변경
ALTER TABLE attraction_ratings DROP CONSTRAINT IF EXISTS uk_attraction_ratings_attraction_user;
ALTER TABLE attraction_ratings ADD COLUMN member_id bigint;
UPDATE attraction_ratings r SET member_id = m.id FROM members m WHERE r.user_id = m.user_id;
DELETE FROM attraction_ratings WHERE member_id IS NULL;
ALTER TABLE attraction_ratings DROP COLUMN user_id;
ALTER TABLE attraction_ratings ALTER COLUMN member_id SET NOT NULL;
ALTER TABLE attraction_ratings ADD CONSTRAINT uk_attraction_ratings_attraction_member UNIQUE (attraction_id, member_id);

-- 6. notes 테이블 변경
ALTER TABLE notes DROP CONSTRAINT IF EXISTS fk_notes_author;
ALTER TABLE notes ADD COLUMN author_member_id bigint;
UPDATE notes n SET author_member_id = m.id FROM members m WHERE n.author_user_id = m.user_id;
DELETE FROM notes WHERE author_member_id IS NULL;
ALTER TABLE notes DROP COLUMN author_user_id;
ALTER TABLE notes ALTER COLUMN author_member_id SET NOT NULL;
ALTER TABLE notes ADD CONSTRAINT fk_notes_author FOREIGN KEY (author_member_id) REFERENCES members (id);

-- 7. note_saves 테이블 변경
ALTER TABLE note_saves DROP CONSTRAINT IF EXISTS fk_note_saves_member;
ALTER TABLE note_saves DROP CONSTRAINT IF EXISTS uk_note_saves_note_user;
ALTER TABLE note_saves ADD COLUMN member_id bigint;
UPDATE note_saves s SET member_id = m.id FROM members m WHERE s.user_id = m.user_id;
DELETE FROM note_saves WHERE member_id IS NULL;
ALTER TABLE note_saves DROP COLUMN user_id;
ALTER TABLE note_saves ALTER COLUMN member_id SET NOT NULL;
ALTER TABLE note_saves ADD CONSTRAINT fk_note_saves_member FOREIGN KEY (member_id) REFERENCES members (id);
ALTER TABLE note_saves ADD CONSTRAINT uk_note_saves_note_member UNIQUE (note_id, member_id);

-- 8. note_reports 테이블 변경
ALTER TABLE note_reports DROP CONSTRAINT IF EXISTS fk_note_reports_reporter;
ALTER TABLE note_reports DROP CONSTRAINT IF EXISTS uk_note_reports_note_reporter;
ALTER TABLE note_reports ADD COLUMN reporter_member_id bigint;
UPDATE note_reports r SET reporter_member_id = m.id FROM members m WHERE r.reporter_user_id = m.user_id;
DELETE FROM note_reports WHERE reporter_member_id IS NULL;
ALTER TABLE note_reports DROP COLUMN reporter_user_id;
ALTER TABLE note_reports ALTER COLUMN reporter_member_id SET NOT NULL;
ALTER TABLE note_reports ADD CONSTRAINT fk_note_reports_reporter FOREIGN KEY (reporter_member_id) REFERENCES members (id);
ALTER TABLE note_reports ADD CONSTRAINT uk_note_reports_note_reporter UNIQUE (note_id, reporter_member_id);

-- 9. courses 테이블 변경
ALTER TABLE courses DROP CONSTRAINT IF EXISTS fk_courses_owner;
ALTER TABLE courses ADD COLUMN owner_member_id bigint;
UPDATE courses c SET owner_member_id = m.id FROM members m WHERE c.owner_user_id = m.user_id;
DELETE FROM courses WHERE owner_member_id IS NULL;
ALTER TABLE courses DROP COLUMN owner_user_id;
ALTER TABLE courses ALTER COLUMN owner_member_id SET NOT NULL;
ALTER TABLE courses ADD CONSTRAINT fk_courses_owner FOREIGN KEY (owner_member_id) REFERENCES members (id);

-- 10. course_saves 테이블 변경
ALTER TABLE course_saves DROP CONSTRAINT IF EXISTS fk_course_saves_member;
ALTER TABLE course_saves DROP CONSTRAINT IF EXISTS uk_course_saves_course_user;
ALTER TABLE course_saves ADD COLUMN member_id bigint;
UPDATE course_saves s SET member_id = m.id FROM members m WHERE s.user_id = m.user_id;
DELETE FROM course_saves WHERE member_id IS NULL;
ALTER TABLE course_saves DROP COLUMN user_id;
ALTER TABLE course_saves ALTER COLUMN member_id SET NOT NULL;
ALTER TABLE course_saves ADD CONSTRAINT fk_course_saves_member FOREIGN KEY (member_id) REFERENCES members (id);
ALTER TABLE course_saves ADD CONSTRAINT uk_course_saves_course_member UNIQUE (course_id, member_id);

-- 11. course_reports 테이블 변경
ALTER TABLE course_reports DROP CONSTRAINT IF EXISTS fk_course_reports_reporter;
ALTER TABLE course_reports DROP CONSTRAINT IF EXISTS uk_course_reports_course_reporter;
ALTER TABLE course_reports ADD COLUMN reporter_member_id bigint;
UPDATE course_reports r SET reporter_member_id = m.id FROM members m WHERE r.reporter_user_id = m.user_id;
DELETE FROM course_reports WHERE reporter_member_id IS NULL;
ALTER TABLE course_reports DROP COLUMN reporter_user_id;
ALTER TABLE course_reports ALTER COLUMN reporter_member_id SET NOT NULL;
ALTER TABLE course_reports ADD CONSTRAINT fk_course_reports_reporter FOREIGN KEY (reporter_member_id) REFERENCES members (id);
ALTER TABLE course_reports ADD CONSTRAINT uk_course_reports_course_reporter UNIQUE (course_id, reporter_member_id);

-- 12. friendships 테이블 변경
ALTER TABLE friendships DROP CONSTRAINT IF EXISTS fk_friendships_requester;
ALTER TABLE friendships DROP CONSTRAINT IF EXISTS fk_friendships_addressee;
ALTER TABLE friendships ADD COLUMN requester_member_id bigint;
ALTER TABLE friendships ADD COLUMN addressee_member_id bigint;
UPDATE friendships f SET requester_member_id = m.id FROM members m WHERE f.requester_user_id = m.user_id;
UPDATE friendships f SET addressee_member_id = m.id FROM members m WHERE f.addressee_user_id = m.user_id;
DELETE FROM friendships WHERE requester_member_id IS NULL OR addressee_member_id IS NULL;
ALTER TABLE friendships DROP COLUMN requester_user_id;
ALTER TABLE friendships DROP COLUMN addressee_user_id;
ALTER TABLE friendships ALTER COLUMN requester_member_id SET NOT NULL;
ALTER TABLE friendships ALTER COLUMN addressee_member_id SET NOT NULL;
ALTER TABLE friendships ADD CONSTRAINT fk_friendships_requester FOREIGN KEY (requester_member_id) REFERENCES members (id);
ALTER TABLE friendships ADD CONSTRAINT fk_friendships_addressee FOREIGN KEY (addressee_member_id) REFERENCES members (id);
DROP INDEX IF EXISTS uk_friendships_user_pair_active;
CREATE UNIQUE INDEX uk_friendships_member_pair_active ON friendships (least(requester_member_id, addressee_member_id), greatest(requester_member_id, addressee_member_id)) WHERE status IN ('PENDING', 'ACCEPTED');

-- 13. notifications 테이블 변경
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS fk_notifications_recipient;
ALTER TABLE notifications ADD COLUMN recipient_member_id bigint;
UPDATE notifications n SET recipient_member_id = m.id FROM members m WHERE n.recipient_user_id = m.user_id;
DELETE FROM notifications WHERE recipient_member_id IS NULL;
ALTER TABLE notifications DROP COLUMN recipient_user_id;
ALTER TABLE notifications ALTER COLUMN recipient_member_id SET NOT NULL;
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_member_id) REFERENCES members (id);

-- 14. 인덱스명 변경
DROP INDEX IF EXISTS idx_auth_logs_user_id;
CREATE INDEX idx_auth_logs_member_id ON auth_logs (member_id);
DROP INDEX IF EXISTS idx_hotplaces_user_id_created_at;
CREATE INDEX idx_hotplaces_member_id_created_at ON hotplaces (member_id, created_at desc);
DROP INDEX IF EXISTS idx_plans_user_id_created_at;
CREATE INDEX idx_plans_member_id_created_at ON plans (member_id, created_at desc);
DROP INDEX IF EXISTS idx_notes_author_created;
CREATE INDEX idx_notes_author_created ON notes (author_member_id, created_at desc);
DROP INDEX IF EXISTS idx_note_saves_user_created;
CREATE INDEX idx_note_saves_member_created ON note_saves (member_id, created_at desc);
DROP INDEX IF EXISTS idx_courses_owner_created;
CREATE INDEX idx_courses_owner_created ON courses (owner_member_id, created_at desc);
DROP INDEX IF EXISTS idx_course_saves_user_created;
CREATE INDEX idx_course_saves_member_created ON course_saves (member_id, created_at desc);
DROP INDEX IF EXISTS idx_friendships_requester_status;
CREATE INDEX idx_friendships_requester_status ON friendships (requester_member_id, status);
DROP INDEX IF EXISTS idx_friendships_addressee_status;
CREATE INDEX idx_friendships_addressee_status ON friendships (addressee_member_id, status);
DROP INDEX IF EXISTS idx_notifications_recipient_created_at;
CREATE INDEX idx_notifications_recipient_created_at ON notifications (recipient_member_id, created_at desc);
DROP INDEX IF EXISTS idx_notifications_recipient_read_at;
CREATE INDEX idx_notifications_recipient_read_at ON notifications (recipient_member_id, read_at);
DROP INDEX IF EXISTS idx_attraction_saves_user_created;
CREATE INDEX idx_attraction_saves_member_created ON attraction_saves (member_id, created_at desc);

-- 15. members 테이블에서 user_id 컬럼 제거 및 관련 제약 조건 변경 (가장 마지막에 실행)
ALTER TABLE members DROP CONSTRAINT IF EXISTS uk_members_user_id;
ALTER TABLE members DROP COLUMN IF EXISTS user_id;
