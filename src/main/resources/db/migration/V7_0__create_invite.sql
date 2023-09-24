CREATE TABLE "invitation" (
                        id SERIAL PRIMARY KEY,
                        firstname CHARACTER VARYING(30) NOT NULL,
                        lastname CHARACTER VARYING(30) NOT NULL,
                        email CHARACTER VARYING(64) NOT NULL UNIQUE,
                        timestamp TIMESTAMP NOT NULL,
                        verification_code CHARACTER VARYING(64),
                        role INTEGER NOT NULL,
                        tenant_id INTEGER NOT NULL,
                        CONSTRAINT fk_tenant
                        FOREIGN KEY (tenant_id)
                        REFERENCES tenant(id)
                        ON DELETE CASCADE
);