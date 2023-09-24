ALTER TABLE subscription_package ALTER COLUMN max_num_of_lambdas DROP NOT NULL;

UPDATE subscription_package SET max_num_of_lambdas=1 WHERE subscription_type = 0;
UPDATE subscription_package SET max_num_of_lambdas=10 WHERE subscription_type = 1;
UPDATE subscription_package SET max_num_of_lambdas=20 WHERE subscription_type = 2;
UPDATE subscription_package SET max_num_of_lambdas=null WHERE subscription_type = 3;