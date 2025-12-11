CREATE TABLE test_case_results (
               id BIGSERIAL PRIMARY KEY,
               submission_id BIGINT NOT NULL,
               test_case_id BIGINT NOT NULL,
               status VARCHAR(50) NOT NULL,
               actual_output TEXT,
               execution_time DOUBLE PRECISION,
               memory_used INTEGER,
               stderr TEXT,
               compile_output TEXT,
               created_at TIMESTAMP NOT NULL,
               FOREIGN KEY (submission_id) REFERENCES submissions(submission_id) ON DELETE CASCADE,
               FOREIGN KEY (test_case_id) REFERENCES test_cases(test_case_id)
);