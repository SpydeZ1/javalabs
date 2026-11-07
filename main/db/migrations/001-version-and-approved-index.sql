ALTER TABLE users ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE translation_projects ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE source_segments ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE translations ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE editorial_comments ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX translations_one_approved_per_segment
    ON translations (segment_id) WHERE status = 'APPROVED';

UPDATE translations SET status = 'REJECTED'
WHERE status = 'SUBMITTED'
  AND EXISTS (SELECT 1 FROM editorial_comments c WHERE c.translation_id = translations.id AND c.status = 'OPEN');
