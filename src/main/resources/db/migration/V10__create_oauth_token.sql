CREATE TABLE oauth_token
(
    id            SERIAL PRIMARY KEY,
    user_id       INTEGER   NOT NULL,
    access_token  TEXT      NOT NULL UNIQUE,
    refresh_token TEXT      NOT NULL,
    public_key    TEXT      NOT NULL,
    client_id     TEXT      NOT NULL,
    scope         TEXT      NOT NULL,
    expires_at    TIMESTAMP NOT NULL,
    version       INTEGER   NOT NULL,
    created_at    TIMESTAMP,
    modified_at   TIMESTAMP
);
