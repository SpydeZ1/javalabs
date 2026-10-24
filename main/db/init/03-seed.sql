INSERT INTO users (username, email, role, created_at, updated_at) VALUES
    ('anna', 'anna@studio.local', 'TRANSLATOR', NOW(), NOW()),
    ('boris', 'boris@studio.local', 'EDITOR', NOW(), NOW());

INSERT INTO translation_projects (name, target_language, owner_id, status, created_at, updated_at) VALUES
    ('Интерфейс кабинета', 'en', 1, 'DRAFT', NOW(), NOW()),
    ('Письма поддержки', 'de', 1, 'IN_PROGRESS', NOW(), NOW());

INSERT INTO source_segments (project_id, content, sequence_number, created_at, updated_at) VALUES
    (1, 'Сохранить изменения', 1, NOW(), NOW()),
    (2, 'Ваш запрос принят в работу.', 1, NOW(), NOW()),
    (2, 'Мы ответим в течение суток.', 2, NOW(), NOW());

INSERT INTO translations (segment_id, author_id, text, status, created_at, updated_at) VALUES
    (2, 1, 'Your request has been received.', 'REJECTED', NOW(), NOW()),
    (3, 1, 'We will reply within a day.', 'DRAFT', NOW(), NOW());

INSERT INTO editorial_comments (translation_id, content, status, created_at, updated_at) VALUES
    (1, 'Лучше: Your request is being processed.', 'OPEN', NOW(), NOW());
