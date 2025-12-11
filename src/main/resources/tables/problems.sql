CREATE TABLE problems (
            problem_id BIGSERIAL PRIMARY KEY,
            exam_id VARCHAR(10) NOT NULL,
            title VARCHAR(255) NOT NULL,
            description TEXT NOT NULL,
            points INTEGER NOT NULL,
            time_limit INTEGER DEFAULT 5,
            memory_limit INTEGER DEFAULT 128000,
            order_index INTEGER,
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP,
            FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE
);
