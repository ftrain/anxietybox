DROP TABLE IF EXISTS account CASCADE;
DROP TABLE IF EXISTS anxiety CASCADE;
DROP TABLE IF EXISTS reply CASCADE;

-- The account of a user
CREATE TABLE account (
       created_time TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
       id SERIAL PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       email VARCHAR(100) UNIQUE NOT NULL,
       count INT NOT NULL DEFAULT 0,
       confirm uuid NOT NULL,
       active BOOLEAN DEFAULT FALSE
);

CREATE UNIQUE INDEX lower_email_index ON account (lower(email));
CREATE UNIQUE INDEX confirm_index ON account (confirm);

-- Track anxieties
CREATE TABLE anxiety (
       id SERIAL PRIMARY KEY,
       account_id int REFERENCES account(id) ON DELETE CASCADE,
       tracker uuid NOT NULL,
       description text UNIQUE NOT NULL
);

CREATE UNIQUE INDEX track_index ON account (confirm);

-- Track replies
CREATE TABLE reply (
       created_time TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
       id SERIAL PRIMARY KEY,
       account_id int REFERENCES account(id),
       anxiety_id int REFERENCES anxiety(id) ON DELETE CASCADE,
       description text
);
