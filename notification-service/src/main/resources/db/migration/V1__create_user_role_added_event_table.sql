CREATE TABLE IF NOT EXISTS user_role_added_event (
                                                     id SERIAL PRIMARY KEY,
                                                     login VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );