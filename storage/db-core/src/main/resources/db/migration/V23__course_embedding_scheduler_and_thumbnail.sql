ALTER TABLE course_embeddings
    DROP CONSTRAINT chk_course_embeddings_status;
ALTER TABLE course_embeddings
    ADD CONSTRAINT chk_course_embeddings_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'EMBEDDED', 'FAILED'));

ALTER TABLE courses
    ADD COLUMN thumbnail_url VARCHAR(1024);
