CREATE TABLE oauth_authorization_code
(
    id           SERIAL PRIMARY KEY,
    user_id      TEXT      NOT NULL,
    code         TEXT      NOT NULL UNIQUE,
    client_id    TEXT      NOT NULL,
    redirect_uri TEXT      NOT NULL,
    expires_at   TIMESTAMP NOT NULL,
    used         BOOLEAN   NOT NULL,
    version      INTEGER   NOT NULL,
    created_at   TIMESTAMP,
    modified_at  TIMESTAMP
);
