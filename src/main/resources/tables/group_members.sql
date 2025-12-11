CREATE TABLE group_members (
           group_id VARCHAR(10),
           student_id VARCHAR(10),
           joined_at TIMESTAMP NOT NULL,

           PRIMARY KEY (group_id, student_id),
           FOREIGN KEY (group_id) REFERENCES groups(group_id) ON DELETE CASCADE,
           FOREIGN KEY (student_id) REFERENCES users(user_id)
);
