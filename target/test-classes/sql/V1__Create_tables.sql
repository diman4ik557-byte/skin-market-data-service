
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS studio_members CASCADE;
DROP TABLE IF EXISTS artist_profiles CASCADE;
DROP TABLE IF EXISTS studios CASCADE;
DROP TABLE IF EXISTS social_links CASCADE;
DROP TABLE IF EXISTS profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    balance DECIMAL(19,2) DEFAULT 0,
    role VARCHAR(20) NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS profiles (
                                        id BIGSERIAL PRIMARY KEY,
                                        user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    bio TEXT,
    is_artist BOOLEAN DEFAULT FALSE,
    is_studio BOOLEAN DEFAULT FALSE
    );

CREATE TABLE IF NOT EXISTS social_links (
                                            id BIGSERIAL PRIMARY KEY,
                                            profile_id BIGINT NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    user_identifier VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    UNIQUE(profile_id, platform)
    );

CREATE TABLE IF NOT EXISTS studios (
                                       id BIGSERIAL PRIMARY KEY,
                                       profile_id BIGINT NOT NULL UNIQUE REFERENCES profiles(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    founded_at DATE,
    manager_id BIGINT REFERENCES users(id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS artist_profiles (
                                               id BIGSERIAL PRIMARY KEY,
                                               profile_id BIGINT NOT NULL UNIQUE REFERENCES profiles(id) ON DELETE CASCADE,
    studio_id BIGINT REFERENCES studios(id) ON DELETE SET NULL,
    styles VARCHAR(500),
    min_price DECIMAL(19,2),
    average_time INT,
    is_available BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS studio_members (
                                              id BIGSERIAL PRIMARY KEY,
                                              studio_id BIGINT NOT NULL REFERENCES studios(id) ON DELETE CASCADE,
    member_id BIGINT NOT NULL REFERENCES artist_profiles(id) ON DELETE CASCADE,
    role VARCHAR(20) DEFAULT 'ARTIST',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(studio_id, member_id)
    );

CREATE TABLE IF NOT EXISTS orders (
                                      id BIGSERIAL PRIMARY KEY,
                                      customer_id BIGINT NOT NULL REFERENCES users(id),
    artist_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    description TEXT,
    price DECIMAL(19,2) NOT NULL,
    final_file_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS messages (
                                        id BIGSERIAL PRIMARY KEY,
                                        order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT,
    attachment_url VARCHAR(500),
    is_preview BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );