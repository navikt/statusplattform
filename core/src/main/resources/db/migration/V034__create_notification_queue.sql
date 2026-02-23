CREATE TABLE notification_queue (
    id              UUID NOT NULL PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES subscription(id) ON DELETE CASCADE,
    notification_type VARCHAR(30) NOT NULL,
    reference_id    UUID,
    subject         VARCHAR(500) NOT NULL,
    body_html       TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts        INTEGER NOT NULL DEFAULT 0,
    last_attempted  timestamp with time zone,
    created_at      timestamp with time zone NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notification_queue_status ON notification_queue(status, created_at);
