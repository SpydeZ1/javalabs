CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (username, email, password_hash, role, status, created_at, updated_at) VALUES
    ('ngkoka', 'ngkoka@studio.local', crypt('Pass123', gen_salt('bf', 10)), 'TRANSLATOR', 'ACTIVE', NOW(), NOW()),
    ('pikachu', 'pokemon@studio.local', crypt('pika-pika', gen_salt('bf', 10)), 'EDITOR', 'ACTIVE', NOW(), NOW());

INSERT INTO translation_projects (name, target_language, owner_id, status, created_at, updated_at) VALUES
    ('Вера в бога', 'en', 1, 'DRAFT', NOW(), NOW()),
    ('Письма счастья', 'de', 1, 'IN_PROGRESS', NOW(), NOW());

INSERT INTO source_segments (project_id, content, sequence_number, created_at, updated_at) VALUES
    (1, 'Сохранить изменения', 1, NOW(), NOW()),
    (2, 'Ваш запрос принят в работу.', 1, NOW(), NOW()),
    (2, 'Мы вам перезвоним(нет).', 2, NOW(), NOW());

INSERT INTO translations (segment_id, author_id, text, status, created_at, updated_at) VALUES
    (2, 1, 'Your request has been received.', 'REJECTED', NOW(), NOW()),
    (3, 1, 'We will not reply within a day.', 'DRAFT', NOW(), NOW());

INSERT INTO editorial_comments (translation_id, content, status, created_at, updated_at) VALUES
    (1, 'Your request is being processed.', 'OPEN', NOW(), NOW());
