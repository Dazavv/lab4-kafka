CREATE TABLE IF NOT EXISTS event_notifications (
                                                   id SERIAL PRIMARY KEY,
                                                   event_id BIGINT NOT NULL,
                                                   user_id BIGINT NOT NULL,
                                                   message VARCHAR(1000) NOT NULL,
    status VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );