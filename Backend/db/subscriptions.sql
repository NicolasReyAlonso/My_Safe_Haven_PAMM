CREATE TABLE IF NOT EXISTS subscriptions (
    subscription_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    haven_id INTEGER NOT NULL REFERENCES havens(haven_id) ON DELETE CASCADE,
    subscribed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, haven_id)
);
