CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    firstname CHARACTER VARYING(30) NOT NULL,
    lastname CHARACTER VARYING(30) NOT NULL,
    email CHARACTER VARYING(64) NOT NULL,
    password CHARACTER VARYING(255) NOT NULL,
    company CHARACTER VARYING(255)  NOT NULL,
    subdomain CHARACTER VARYING(64) NOT NULL,
    role INTEGER NOT NULL
);