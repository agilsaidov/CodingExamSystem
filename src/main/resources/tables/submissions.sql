CREATE TABLE submissions (
             submission_id BIGSERIAL PRIMARY KEY,
             student_id VARCHAR(10) NOT NULL,
             problem_id BIGINT NOT NULL,
             source_code TEXT NOT NULL,
             language_id INTEGER NOT NULL,
             status VARCHAR(50) NOT NULL,
             score INTEGER,
             passed_test_cases INTEGER,
             total_test_cases INTEGER,
             submitted_at TIMESTAMP NOT NULL,
             judged_at TIMESTAMP,

             FOREIGN KEY (student_id) REFERENCES users(user_id),
             FOREIGN KEY (problem_id) REFERENCES problems(problem_id)
);