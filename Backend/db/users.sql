CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    mail VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    profile_image_path VARCHAR(500),
    password_hash VARCHAR(255) NOT NULL
);
