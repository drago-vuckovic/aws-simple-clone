CREATE TABLE "file"(
    id SERIAL PRIMARY KEY ,
    name CHARACTER VARYING NOT NULL ,
    is_folder BOOLEAN NOT NULL ,
    last_modified TIMESTAMP NOT NULL,
    size DOUBLE PRECISION NOT NULL,
    parent_id INTEGER NOT NULL,
    CONSTRAINT fk_file_parent
    FOREIGN KEY (parent_id)
    REFERENCES file(id)
    ON DELETE CASCADE,
    bucket_id INTEGER NOT NULL ,
    CONSTRAINT fk_file_bucket
    FOREIGN KEY (bucket_id)
    REFERENCES bucket(id)
    ON DELETE CASCADE,
    owner_id INTEGER NOT NULL,
    CONSTRAINT fk_file_user
    FOREIGN KEY (owner_id)
    REFERENCES "user"(id)
    ON DELETE SET NULL
);

CREATE TABLE "permission"(
    id SERIAL PRIMARY KEY ,
    user_group INTEGER NOT NULL,
    permission_type INTEGER NOT NULL,
    file_id INTEGER NOT NULL ,
    CONSTRAINT fk_permissions_file
    FOREIGN KEY (file_id)
    REFERENCES file(id)
    ON DELETE CASCADE
)