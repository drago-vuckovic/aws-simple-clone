ALTER TABLE "subscription_package"
    ADD COLUMN max_num_of_lambdas INTEGER NOT NULL DEFAULT 0;

ALTER TABLE "tenant"
    ADD COLUMN total_num_of_lambdas INTEGER NOT NULL DEFAULT 0;