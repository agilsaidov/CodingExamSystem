CREATE TABLE exam_results (
              result_id BIGSERIAL PRIMARY KEY,
              exam_id VARCHAR(10) NOT NULL,
              student_id VARCHAR(10) NOT NULL,
              total_score INTEGER NOT NULL,
              max_possible_score INTEGER NOT NULL,
              percentage DECIMAL(5,2),
              status VARCHAR(20) NOT NULL,
              started_at TIMESTAMP,
              finished_at TIMESTAMP,
              time_spent_minutes INTEGER,
              created_at TIMESTAMP NOT NULL,
              FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE,
              FOREIGN KEY (student_id) REFERENCES users(user_id),
              UNIQUE(exam_id, student_id)
);