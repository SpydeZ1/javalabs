CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT users_role_check CHECK (role IN ('TRANSLATOR', 'EDITOR'))
);

CREATE TABLE translation_projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    target_language VARCHAR(64) NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES users (id),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT projects_status_check CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'COMPLETED'))
);

CREATE TABLE source_segments (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES translation_projects (id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    sequence_number INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT segments_project_seq UNIQUE (project_id, sequence_number),
    CONSTRAINT segments_seq_positive CHECK (sequence_number >= 1)
);

CREATE TABLE translations (
    id BIGSERIAL PRIMARY KEY,
    segment_id BIGINT NOT NULL REFERENCES source_segments (id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users (id),
    text TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT translations_status_check CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED'))
);

CREATE TABLE editorial_comments (
    id BIGSERIAL PRIMARY KEY,
    translation_id BIGINT NOT NULL REFERENCES translations (id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT comments_status_check CHECK (status IN ('OPEN', 'RESOLVED'))
);

-- У сегмента может быть не более одного утверждённого перевода
CREATE UNIQUE INDEX translations_one_approved_per_segment
    ON translations (segment_id) WHERE status = 'APPROVED';
