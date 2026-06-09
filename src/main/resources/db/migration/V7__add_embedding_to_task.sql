CREATE EXTENSION IF NOT EXISTS vector;
ALTER TABLE task_manager ADD COLUMN embedding vector(1024);
CREATE INDEX ON task_manager USING ivfflat (embedding vector_cosine_ops) WITH (lists = 10);
