-- Information about user.
DROP TABLE IF EXISTS box CASCADE;
DROP TABLE IF EXISTS anxiety CASCADE;
DROP TABLE IF EXISTS reply CASCADE;

CREATE TABLE box (
       created_time TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
       id SERIAL PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       email VARCHAR(100) UNIQUE NOT NULL,
       count INT NOT NULL DEFAULT 0,
       confirm uuid NOT NULL,
       active BOOLEAN DEFAULT FALSE
       );

CREATE UNIQUE INDEX lower_email_index ON box (lower(email));

CREATE UNIQUE INDEX confirm_index ON box (confirm);

CREATE TABLE anxiety (
       id SERIAL PRIMARY KEY,
       box_id int REFERENCES box(id) ON DELETE CASCADE,
       description text UNIQUE NOT NULL
       );

CREATE TABLE reply (
       created_time TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
       id SERIAL PRIMARY KEY,
       box_id int REFERENCES box(id) ON DELETE CASCADE,
       description text
       );
       
