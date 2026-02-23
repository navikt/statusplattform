CREATE TABLE subscription (
    id              UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    unsubscribe_token UUID NOT NULL DEFAULT gen_random_uuid(),
    is_internal     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ
);

CREATE TABLE subscription_service (
    subscription_id UUID NOT NULL REFERENCES subscription(id) ON DELETE CASCADE,
    service_id      UUID NOT NULL REFERENCES service(id) ON DELETE CASCADE,
    PRIMARY KEY (subscription_id, service_id)
);

CREATE TABLE otp_verification (
    id          UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    otp_code    VARCHAR(6) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    verified    BOOLEAN NOT NULL DEFAULT FALSE,
    attempts    INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_otp_email_code ON otp_verification(email, otp_code);
CREATE INDEX idx_subscription_email ON subscription(email);
CREATE INDEX idx_subscription_unsubscribe_token ON subscription(unsubscribe_token);
