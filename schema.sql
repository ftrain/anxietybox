-- Information about user.
DROP TABLE IF EXISTS box CASCADE;
DROP TABLE IF EXISTS anxiety CASCADE;
DROP TABLE IF EXISTS anxiety_box CASCADE;
DROP TABLE IF EXISTS queue CASCADE;
--DROP INDEX IF EXISTS lower_email_index CASCADE;

CREATE TABLE queue (
       run DATE NOT NULL DEFAULT NOW()
       );

CREATE TABLE box (
       id SERIAL PRIMARY KEY,
       fullname VARCHAR(100) NOT NULL,
       email VARCHAR(100) UNIQUE NOT NULL,
       count INT NOT NULL DEFAULT 0,
       project TEXT NOT NULL,
       confirm uuid NOT NULL,
       active BOOLEAN DEFAULT FALSE
       );

CREATE UNIQUE INDEX lower_email_index ON box (lower(email));

CREATE UNIQUE INDEX confirm_index ON box (confirm);

CREATE TABLE anxiety (
       id SERIAL PRIMARY KEY,
       box_id int REFERENCES box(id) ON DELETE CASCADE,
       description text
       );

CREATE TABLE anxiety_box (
       id SERIAL PRIMARY KEY,
       box_id int REFERENCES box(id) ON DELETE CASCADE,
       anxiety_id int REFERENCES anxiety(id) ON DELETE CASCADE,       
       sent DATE NOT NULL DEFAULT NOW()
       );

-- You don't have enough information to do [TK].
-- You are a bad person who will never finish [TK].
-- Everyone will laugh at you when you show them.
-- You are going to let down the people you trust.
-- You will never get yourself under control.
-- No matter how hard you work on [P] it will never be good enough.
