CREATE TABLE exams (
           exam_id VARCHAR(10) PRIMARY KEY,
           group_id VARCHAR(10) NOT NULL,
           instructor_id VARCHAR(10) NOT NULL,
           title VARCHAR(255) NOT NULL,
           description TEXT,
           start_time TIMESTAMP,
           end_time TIMESTAMP,
           duration_minutes INTEGER,
           is_active BOOLEAN NOT NULL DEFAULT false,
           created_at TIMESTAMP NOT NULL,
           updated_at TIMESTAMP,

           FOREIGN KEY (instructor_id) REFERENCES users(user_id),
           FOREIGN KEY (group_id) REFERENCES groups(group_id) ON DELETE CASCADE
);