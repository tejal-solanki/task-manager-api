CREATE TABLE task_manager (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(50),
    created_at DATE,
    due_date DATE
);
