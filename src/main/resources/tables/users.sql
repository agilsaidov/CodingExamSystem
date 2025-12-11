CREATE TABLE users (
           user_id VARCHAR(10) PRIMARY KEY,
           username VARCHAR(50) NOT NULL UNIQUE,
           password VARCHAR(255) NOT NULL,
           role VARCHAR(20) NOT NULL,
           full_name VARCHAR(100),
           created_at TIMESTAMP NOT NULL,
           updated_at TIMESTAMP
);