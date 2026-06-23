-- members 테이블에서 user_id 컬럼 제거 및 관련 제약 조건 변경
ALTER TABLE members DROP CONSTRAINT IF EXISTS uk_members_user_id;
ALTER TABLE members DROP COLUMN IF EXISTS user_id;

-- auth_logs 테이블 변경
ALTER TABLE auth_logs RENAME COLUMN user_id TO member_id;

-- hotplaces 테이블 변경
ALTER TABLE hotplaces RENAME COLUMN user_id TO member_id;

-- plans 테이블 변경
ALTER TABLE plans RENAME COLUMN user_id TO member_id;

-- attraction_favorites 테이블 변경
ALTER TABLE attraction_favorites RENAME COLUMN user_id TO member_id;
ALTER TABLE attraction_favorites DROP CONSTRAINT IF EXISTS uk_attraction_favorites_attraction_user;
ALTER TABLE attraction_favorites ADD CONSTRAINT uk_attraction_favorites_attraction_member UNIQUE (attraction_id, member_id);

-- attraction_ratings 테이블 변경
ALTER TABLE attraction_ratings RENAME COLUMN user_id TO member_id;
ALTER TABLE attraction_ratings DROP CONSTRAINT IF EXISTS uk_attraction_ratings_attraction_user;
ALTER TABLE attraction_ratings ADD CONSTRAINT uk_attraction_ratings_attraction_member UNIQUE (attraction_id, member_id);

-- notes 테이블 변경
ALTER TABLE notes RENAME COLUMN author_user_id TO author_member_id;
ALTER TABLE notes DROP CONSTRAINT IF EXISTS fk_notes_author;
ALTER TABLE notes ADD CONSTRAINT fk_notes_author FOREIGN KEY (author_member_id) REFERENCES members (id);

-- note_saves 테이블 변경
ALTER TABLE note_saves RENAME COLUMN user_id TO member_id;
ALTER TABLE note_saves DROP CONSTRAINT IF EXISTS fk_note_saves_member;
ALTER TABLE note_saves ADD CONSTRAINT fk_note_saves_member FOREIGN KEY (member_id) REFERENCES members (id);
ALTER TABLE note_saves DROP CONSTRAINT IF EXISTS uk_note_saves_note_user;
ALTER TABLE note_saves ADD CONSTRAINT uk_note_saves_note_member UNIQUE (note_id, member_id);

-- note_reports 테이블 변경
ALTER TABLE note_reports RENAME COLUMN reporter_user_id TO reporter_member_id;
ALTER TABLE note_reports DROP CONSTRAINT IF EXISTS fk_note_reports_reporter;
ALTER TABLE note_reports ADD CONSTRAINT fk_note_reports_reporter FOREIGN KEY (reporter_member_id) REFERENCES members (id);
ALTER TABLE note_reports DROP CONSTRAINT IF EXISTS uk_note_reports_note_reporter;
ALTER TABLE note_reports ADD CONSTRAINT uk_note_reports_note_reporter UNIQUE (note_id, reporter_member_id);

-- courses 테이블 변경
ALTER TABLE courses RENAME COLUMN owner_user_id TO owner_member_id;
ALTER TABLE courses DROP CONSTRAINT IF EXISTS fk_courses_owner;
ALTER TABLE courses ADD CONSTRAINT fk_courses_owner FOREIGN KEY (owner_member_id) REFERENCES members (id);

-- course_saves 테이블 변경
ALTER TABLE course_saves RENAME COLUMN user_id TO member_id;
ALTER TABLE course_saves DROP CONSTRAINT IF EXISTS fk_course_saves_member;
ALTER TABLE course_saves ADD CONSTRAINT fk_course_saves_member FOREIGN KEY (member_id) REFERENCES members (id);
ALTER TABLE course_saves DROP CONSTRAINT IF EXISTS uk_course_saves_course_user;
ALTER TABLE course_saves ADD CONSTRAINT uk_course_saves_course_member UNIQUE (course_id, member_id);

-- course_reports 테이블 변경
ALTER TABLE course_reports RENAME COLUMN reporter_user_id TO reporter_member_id;
ALTER TABLE course_reports DROP CONSTRAINT IF EXISTS fk_course_reports_reporter;
ALTER TABLE course_reports ADD CONSTRAINT fk_course_reports_reporter FOREIGN KEY (reporter_member_id) REFERENCES members (id);
ALTER TABLE course_reports DROP CONSTRAINT IF EXISTS uk_course_reports_course_reporter;
ALTER TABLE course_reports ADD CONSTRAINT uk_course_reports_course_reporter UNIQUE (course_id, reporter_member_id);

-- friendships 테이블 변경
ALTER TABLE friendships RENAME COLUMN requester_user_id TO requester_member_id;
ALTER TABLE friendships RENAME COLUMN addressee_user_id TO addressee_member_id;
ALTER TABLE friendships DROP CONSTRAINT IF EXISTS fk_friendships_requester;
ALTER TABLE friendships ADD CONSTRAINT fk_friendships_requester FOREIGN KEY (requester_member_id) REFERENCES members (id);
ALTER TABLE friendships DROP CONSTRAINT IF EXISTS fk_friendships_addressee;
ALTER TABLE friendships ADD CONSTRAINT fk_friendships_addressee FOREIGN KEY (addressee_member_id) REFERENCES members (id);
DROP INDEX IF EXISTS uk_friendships_user_pair_active;
CREATE UNIQUE INDEX uk_friendships_member_pair_active ON friendships (least(requester_member_id, addressee_member_id), greatest(requester_member_id, addressee_member_id)) WHERE status IN ('PENDING', 'ACCEPTED');

-- member_settings 테이블 변경
ALTER TABLE member_settings RENAME COLUMN user_id TO member_id;
ALTER TABLE member_settings DROP CONSTRAINT IF EXISTS fk_member_settings_member;
ALTER TABLE member_settings ADD CONSTRAINT fk_member_settings_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE;

-- notification_outbox 테이블 변경
ALTER TABLE notification_outbox RENAME COLUMN recipient_user_id TO recipient_member_id;
ALTER TABLE notification_outbox DROP CONSTRAINT IF EXISTS fk_notification_outbox_recipient;
ALTER TABLE notification_outbox ADD CONSTRAINT fk_notification_outbox_recipient FOREIGN KEY (recipient_member_id) REFERENCES members (id);

-- notifications 테이블 변경
ALTER TABLE notifications RENAME COLUMN recipient_user_id TO recipient_member_id;
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS fk_notifications_recipient;
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_member_id) REFERENCES members (id);

-- 인덱스명 변경
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
