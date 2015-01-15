DROP TABLE IF EXISTS account CASCADE;
DROP TABLE IF EXISTS anxiety CASCADE;
DROP TABLE IF EXISTS reply CASCADE;

DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS anxieties CASCADE;
DROP TABLE IF EXISTS replies CASCADE;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- The account of a user
CREATE TABLE accounts (
       created_time TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
       id SERIAL PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       email VARCHAR(100) UNIQUE NOT NULL,
       count INT NOT NULL DEFAULT 0,
       confirm uuid DEFAULT UUID_GENERATE_V4(),
       active BOOLEAN DEFAULT FALSE
);

CREATE UNIQUE INDEX lower_email_index ON accounts (lower(email));
CREATE UNIQUE INDEX confirm_index ON accounts (confirm);

-- Track anxieties
CREATE TABLE anxieties (
       id SERIAL PRIMARY KEY,
       account_id int REFERENCES accounts(id) ON DELETE CASCADE,
       tracker uuid DEFAULT UUID_GENERATE_V4(),
       description text UNIQUE NOT NULL
);

CREATE UNIQUE INDEX track_index ON accounts (confirm);

-- Track replies
CREATE TABLE replies (
       created_time TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
       id SERIAL PRIMARY KEY,
       account_id int REFERENCES accounts(id),
       anxiety_id int REFERENCES anxieties(id) ON DELETE CASCADE,
       description text
);
