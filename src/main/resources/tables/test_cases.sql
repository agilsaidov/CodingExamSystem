CREATE TABLE test_cases (
            test_case_id BIGSERIAL PRIMARY KEY,
            problem_id BIGINT NOT NULL,
            input TEXT NOT NULL,
            expected_output TEXT NOT NULL,
            is_hidden BOOLEAN NOT NULL DEFAULT false,
            order_index INTEGER,
            points INTEGER,
            created_at TIMESTAMP NOT NULL,

            FOREIGN KEY (problem_id) REFERENCES problems(problem_id) ON DELETE CASCADE
);