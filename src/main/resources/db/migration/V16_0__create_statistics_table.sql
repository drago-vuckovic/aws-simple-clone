CREATE TABLE "statistic"(
       id SERIAL PRIMARY KEY ,
       num_of_downloads INTEGER NOT NULL DEFAULT 0,
       num_of_uploads INTEGER NOT NULL DEFAULT 0
);

ALTER TABLE "bucket"
    ADD COLUMN statistic_id INTEGER;

ALTER TABLE "bucket"
    ADD FOREIGN KEY (statistic_id)
        REFERENCES statistic(id);
