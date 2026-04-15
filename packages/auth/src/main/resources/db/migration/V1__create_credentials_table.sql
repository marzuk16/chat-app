CREATE TABLE credentials (
    id              UUID                     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by      VARCHAR(255)             NOT NULL,
    updated_by      VARCHAR(255)             NOT NULL,
    email           VARCHAR(255)             NOT NULL,
    password_hash   VARCHAR(255)             NOT NULL,
    role            VARCHAR(50)              NOT NULL,
    locked          BOOLEAN                  NOT NULL DEFAULT FALSE,
    email_verified  BOOLEAN                  NOT NULL DEFAULT FALSE,
    failed_attempts INTEGER                  NOT NULL DEFAULT 0,
    locked_until    TIMESTAMP WITH TIME ZONE,
    CONSTRAINT credentials_pkey PRIMARY KEY (id),
    CONSTRAINT credentials_email_uq UNIQUE (email)
);
