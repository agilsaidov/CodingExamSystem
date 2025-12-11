CREATE TABLE groups (
            group_id VARCHAR(10) PRIMARY KEY,
            group_name VARCHAR(50) NOT NULL,
            instructor_id VARCHAR(10) NOT NULL,
            created_at TIMESTAMP NOT NULL,

            FOREIGN KEY (instructor_id) REFERENCES users(user_id)
);

