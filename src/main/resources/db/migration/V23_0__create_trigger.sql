CREATE TABLE "trigger"
(
    id            SERIAL PRIMARY KEY,
    src_folder_id INTEGER NOT NULL,
    trigger_type  INTEGER NOT NULL,
    CONSTRAINT fk_file
        FOREIGN KEY (src_folder_id)
            REFERENCES file (id)
            ON DELETE CASCADE
)