CREATE TABLE game_context
(
    id          SERIAL PRIMARY KEY,
    game        VARCHAR(255) NOT NULL,
    publisher   VARCHAR(255),
    platform    VARCHAR(255) NOT NULL,
    region      VARCHAR(255),
    server      VARCHAR(255),
    variant     VARCHAR(255),
    status      VARCHAR(32)  NOT NULL,
    version     INTEGER      NOT NULL,
    created_at  TIMESTAMP,
    modified_at TIMESTAMP
);

CREATE INDEX idx_game_context_game ON game_context (game);
CREATE INDEX idx_game_context_platform ON game_context (platform);
CREATE INDEX idx_game_context_region ON game_context (region);
CREATE INDEX idx_game_context_status ON game_context (status);

CREATE TABLE game_context_history
(
    id              SERIAL PRIMARY KEY,
    game_context_id INTEGER      NOT NULL REFERENCES game_context (id),
    action          VARCHAR(32)  NOT NULL,
    game            VARCHAR(255) NOT NULL,
    publisher       VARCHAR(255),
    platform        VARCHAR(255) NOT NULL,
    region          VARCHAR(255),
    server          VARCHAR(255),
    variant         VARCHAR(255),
    status          VARCHAR(32)  NOT NULL,
    changed_by      VARCHAR(255),
    recorded_at     TIMESTAMP    NOT NULL
);

CREATE INDEX idx_game_context_history_context ON game_context_history (game_context_id);
