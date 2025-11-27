CREATE TABLE haven_posts (
    post_id SERIAL PRIMARY KEY,
    haven_id INTEGER NOT NULL REFERENCES havens(haven_id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT NOW()
);
