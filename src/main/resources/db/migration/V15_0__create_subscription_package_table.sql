CREATE TABLE "subscription_package"(
       id SERIAL PRIMARY KEY ,
       description TEXT NOT NULL,
       subscription_type INTEGER NOT NULL UNIQUE,
       capacity DOUBLE PRECISION
);

ALTER TABLE "tenant"
    ADD COLUMN subscription_package_id INTEGER;

ALTER TABLE "tenant"
    ADD FOREIGN KEY (subscription_package_id)
        REFERENCES subscription_package(id);

INSERT INTO subscription_package(description, subscription_type, capacity) values ('description1', 0, 1048576),
                                                                              ('description1', 1, 10485760),
                                                                              ('description2', 2, 20971520),
                                                                              ('description3', 3, NULL);
