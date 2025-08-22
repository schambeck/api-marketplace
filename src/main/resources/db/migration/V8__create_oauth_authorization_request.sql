CREATE TABLE oauth_authorization_request
(
    id           SERIAL PRIMARY KEY,
    user_id      INTEGER REFERENCES "user" (id),
    client_id    TEXT NOT NULL,
    redirect_uri TEXT NOT NULL,
    state        TEXT NOT NULL UNIQUE,
    version      INTEGER NOT NULL,
    created_at   TIMESTAMP,
    modified_at  TIMESTAMP
);
