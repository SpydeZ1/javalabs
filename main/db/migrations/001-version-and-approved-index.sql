-- Для уже созданной базы (том Postgres существует). Выполнить от имени translation_owner.
-- Для новой базы не нужно: db/init/01-schema.sql уже содержит эти изменения.
ALTER TABLE users ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE translation_projects ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE source_segments ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE translations ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE editorial_comments ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Если в базе уже есть сегменты с несколькими APPROVED-переводами, индекс не создастся:
-- сначала нужно привести данные в порядок.
CREATE UNIQUE INDEX translations_one_approved_per_segment
    ON translations (segment_id) WHERE status = 'APPROVED';

-- Seed-данные: первый перевод с открытым замечанием должен быть REJECTED, а не SUBMITTED.
UPDATE translations SET status = 'REJECTED'
WHERE status = 'SUBMITTED'
  AND EXISTS (SELECT 1 FROM editorial_comments c WHERE c.translation_id = translations.id AND c.status = 'OPEN');
