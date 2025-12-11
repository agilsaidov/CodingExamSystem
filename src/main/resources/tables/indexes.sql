-- ============================================
-- USERS TABLE INDEXES
-- ============================================
-- Username is already indexed via UNIQUE constraint
-- user_id is already indexed via PRIMARY KEY

-- Index for filtering users by role
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Index for searching by full name
CREATE INDEX IF NOT EXISTS idx_users_full_name ON users(full_name);


-- ============================================
-- GROUPS TABLE INDEXES
-- ============================================
-- group_id is already indexed via PRIMARY KEY

-- Index for finding all groups taught by an instructor
CREATE INDEX IF NOT EXISTS idx_groups_instructor_id ON groups(instructor_id);

-- Index for searching groups by name
CREATE INDEX IF NOT EXISTS idx_groups_name ON groups(group_name);


-- ============================================
-- GROUP_MEMBERS TABLE INDEXES
-- ============================================
-- Composite primary key (group_id, student_id) already creates an index

-- Index for finding all groups a student belongs to
CREATE INDEX IF NOT EXISTS idx_group_members_student_id ON group_members(student_id);

-- Index for filtering by join date
CREATE INDEX IF NOT EXISTS idx_group_members_joined_at ON group_members(joined_at DESC);


-- ============================================
-- EXAMS TABLE INDEXES
-- ============================================
-- exam_id is already indexed via PRIMARY KEY

-- Index for finding all exams in a group
CREATE INDEX IF NOT EXISTS idx_exams_group_id ON exams(group_id);

-- Index for finding all exams created by an instructor
CREATE INDEX IF NOT EXISTS idx_exams_instructor_id ON exams(instructor_id);

-- Index for finding active exams
CREATE INDEX IF NOT EXISTS idx_exams_is_active ON exams(is_active);

-- Composite index for finding active exams within time range
CREATE INDEX IF NOT EXISTS idx_exams_active_time ON exams(is_active, start_time, end_time)
    WHERE is_active = true;

-- Index for sorting exams by creation date
CREATE INDEX IF NOT EXISTS idx_exams_created_at ON exams(created_at DESC);

-- Composite index for instructor's active exams
CREATE INDEX IF NOT EXISTS idx_exams_instructor_active ON exams(instructor_id, is_active);


-- ============================================
-- EXAM_RESULTS TABLE INDEXES
-- ============================================
-- result_id is already indexed via PRIMARY KEY

-- CRITICAL: Composite unique index (already in table definition)
-- This prevents duplicate entries and speeds up lookups
CREATE UNIQUE INDEX IF NOT EXISTS idx_exam_results_exam_student ON exam_results(exam_id, student_id);

-- Index for finding all results for an exam (leaderboard)
CREATE INDEX IF NOT EXISTS idx_exam_results_exam_score ON exam_results(exam_id, total_score DESC);

-- Index for finding all results for a student
CREATE INDEX IF NOT EXISTS idx_exam_results_student ON exam_results(student_id);

-- Index for filtering by status
CREATE INDEX IF NOT EXISTS idx_exam_results_status ON exam_results(status);

-- Composite index for group leaderboards (via exam's group)
-- (This might be better handled at query level, but if you frequently filter by status + score)
CREATE INDEX IF NOT EXISTS idx_exam_results_exam_status_score ON exam_results(exam_id, status, total_score DESC)
    WHERE status = 'COMPLETED';

-- Index for finding in-progress exams
CREATE INDEX IF NOT EXISTS idx_exam_results_in_progress ON exam_results(student_id, status, started_at)
    WHERE status = 'IN_PROGRESS';

-- Index for time-based analytics
CREATE INDEX IF NOT EXISTS idx_exam_results_finished_at ON exam_results(finished_at DESC) WHERE finished_at IS NOT NULL;

-- ============================================
-- PROBLEMS TABLE INDEXES
-- ============================================
-- problem_id is already indexed via PRIMARY KEY

-- Index for finding all problems in an exam
CREATE INDEX IF NOT EXISTS idx_problems_exam_id ON problems(exam_id);

-- Composite index for getting ordered problems in an exam
CREATE INDEX IF NOT EXISTS idx_problems_exam_order ON problems(exam_id, order_index);

-- Index for sorting by creation date
CREATE INDEX IF NOT EXISTS idx_problems_created_at ON problems(created_at DESC);


-- ============================================
-- TEST_CASES TABLE INDEXES
-- ============================================
-- test_case_id is already indexed via PRIMARY KEY

-- Index for finding all test cases for a problem
CREATE INDEX IF NOT EXISTS idx_test_cases_problem_id ON test_cases(problem_id);

-- Composite index for ordered test cases
CREATE INDEX IF NOT EXISTS idx_test_cases_problem_order ON test_cases(problem_id, order_index);

-- Index for filtering hidden vs visible test cases
CREATE INDEX IF NOT EXISTS idx_test_cases_problem_hidden ON test_cases(problem_id, is_hidden);


-- ============================================
-- SUBMISSIONS TABLE INDEXES
-- ============================================
-- submission_id is already indexed via PRIMARY KEY

-- Index for finding all submissions by a student
CREATE INDEX IF NOT EXISTS idx_submissions_student_id ON submissions(student_id);

-- Index for finding all submissions for a problem
CREATE INDEX IF NOT EXISTS idx_submissions_problem_id ON submissions(problem_id);

-- Composite index for student's submissions to a specific problem
CREATE INDEX IF NOT EXISTS idx_submissions_student_problem ON submissions(student_id, problem_id);

-- Index for filtering by submission status
CREATE INDEX IF NOT EXISTS idx_submissions_status ON submissions(status);

-- Composite index for finding student's latest submissions
CREATE INDEX IF NOT EXISTS idx_submissions_student_time ON submissions(student_id, submitted_at DESC);

-- Composite index for problem submissions ordered by time
CREATE INDEX IF NOT EXISTS idx_submissions_problem_time ON submissions(problem_id, submitted_at DESC);

-- Index for filtering by score (for leaderboards)
CREATE INDEX IF NOT EXISTS idx_submissions_score ON submissions(score DESC) WHERE score IS NOT NULL;

-- Composite index for problem leaderboard queries
CREATE INDEX IF NOT EXISTS idx_submissions_problem_score ON submissions(problem_id, score DESC, submitted_at)
    WHERE score IS NOT NULL;

-- Index for pending/judging submissions
CREATE INDEX IF NOT EXISTS idx_submissions_status_time ON submissions(status, submitted_at)
    WHERE status IN ('PENDING', 'JUDGING');


-- ============================================
-- TEST_CASE_RESULTS TABLE INDEXES
-- ============================================
-- id is already indexed via PRIMARY KEY

-- Index for finding all results for a submission
CREATE INDEX IF NOT EXISTS idx_test_case_results_submission ON test_case_results(submission_id);

-- Index for finding results for a specific test case
CREATE INDEX IF NOT EXISTS idx_test_case_results_test_case ON test_case_results(test_case_id);

-- Composite index for submission results with status
CREATE INDEX IF NOT EXISTS idx_test_case_results_submission_status ON test_case_results(submission_id, status);

-- Index for analyzing performance metrics
CREATE INDEX IF NOT EXISTS idx_test_case_results_performance ON test_case_results(execution_time, memory_used)
    WHERE execution_time IS NOT NULL;