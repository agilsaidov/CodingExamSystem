-- USERS TABLE
-- (username already indexed via UNIQUE, user_id via PRIMARY KEY)
CREATE INDEX idx_users_role ON users(role);

-- GROUPS TABLE
-- (group_id already indexed via PRIMARY KEY)
CREATE INDEX idx_groups_instructor_id ON groups(instructor_id);

-- GROUP_MEMBERS TABLE
-- (composite primary key already creates index on group_id, student_id)
CREATE INDEX idx_group_members_student_id ON group_members(student_id);

-- EXAMS TABLE
-- (exam_id already indexed via PRIMARY KEY)
CREATE INDEX idx_exams_group_id ON exams(group_id);
CREATE INDEX idx_exams_instructor_id ON exams(instructor_id);
CREATE INDEX idx_exams_active_time ON exams(is_active, start_time, end_time);

-- PROBLEMS TABLE
-- (problem_id already indexed via PRIMARY KEY)
CREATE INDEX idx_problems_exam_id ON problems(exam_id);
CREATE INDEX idx_problems_exam_order ON problems(exam_id, order_index);

-- TEST_CASES TABLE
-- (test_case_id already indexed via PRIMARY KEY)
CREATE INDEX idx_test_cases_problem_id ON test_cases(problem_id);

-- SUBMISSIONS TABLE
-- (submission_id already indexed via PRIMARY KEY)
CREATE INDEX idx_submissions_student_id ON submissions(student_id);
CREATE INDEX idx_submissions_problem_id ON submissions(problem_id);
CREATE INDEX idx_submissions_student_problem ON submissions(student_id, problem_id, submitted_at DESC);
CREATE INDEX idx_submissions_problem_time ON submissions(problem_id, submitted_at DESC);

-- TEST_CASE_RESULTS TABLE
-- (id already indexed via PRIMARY KEY)
CREATE INDEX idx_test_case_results_submission ON test_case_results(submission_id);