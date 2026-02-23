CREATE TABLE otp_rate_limit (
    email        VARCHAR(255) PRIMARY KEY,
    send_count   INTEGER NOT NULL DEFAULT 0,
    window_start TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
