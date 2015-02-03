DROP TABLE IF EXISTS account CASCADE;
DROP TABLE IF EXISTS anxiety CASCADE;
DROP TABLE IF EXISTS reply CASCADE;
DROP TABLE IF EXISTS mail_to_send CASCADE;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE OR REPLACE FUNCTION upd_tracker() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.tracker = UUID_GENERATE_V4();
    RETURN NEW;
END;
$$;

-- The account of a user
CREATE TABLE account (
       created_time TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
       timezone VARCHAR(100) DEFAULT 'America/New_York',
       id SERIAL PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       email VARCHAR(100) UNIQUE NOT NULL,
       tracker uuid DEFAULT UUID_GENERATE_V4(),
       note TEXT DEFAULT NULL,
       active BOOLEAN DEFAULT FALSE
);

CREATE UNIQUE INDEX account_lower_email_idx ON account (lower(email));
CREATE UNIQUE INDEX account_tracker_idx ON account (tracker);

CREATE TRIGGER t_account_upd_tracker
  BEFORE UPDATE OR INSERT
  ON account
  FOR EACH ROW
  EXECUTE PROCEDURE upd_tracker();

-- Track anxiety
CREATE TABLE anxiety (
       created_time TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
       id SERIAL PRIMARY KEY,
       account_id int REFERENCES account(id) ON DELETE CASCADE NOT NULL,
       tracker uuid DEFAULT UUID_GENERATE_V4(),
       description text NOT NULL,
       UNIQUE(account_id, description)
);

CREATE UNIQUE INDEX anxiety_text_idx ON anxiety (account_id,description);
CREATE UNIQUE INDEX anxiety_tracker_idx ON anxiety (tracker);

CREATE TRIGGER t_anxiety_upd_tracker
  BEFORE UPDATE OR INSERT
  ON anxiety
  FOR EACH ROW
  EXECUTE PROCEDURE upd_tracker();
  
-- Track reply
CREATE TABLE reply (
       created_time TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
       id SERIAL PRIMARY KEY,
       anxiety_id int REFERENCES anxiety(id) ON DELETE CASCADE NOT NULL,
       body text
);

-- Once every X duration (i.e. 24 hours; see code) we take all of
-- the anxieties and assign each one a time that is X + 24 * rand()
-- hours in the future. This gives us a nice, randomly-spaced queue
-- of emails to go out.

-- Log the mail responses

CREATE TABLE mail_to_send (
       id SERIAL PRIMARY KEY,       
       anxiety_id int REFERENCES anxiety(id) ON DELETE CASCADE NOT NULL,
       send_time TIMESTAMP WITHOUT TIME ZONE DEFAULT
       		 NOW() + (TO_CHAR(24 * RANDOM(), '999.999') || ' hours')::interval,
       sent BOOLEAN DEFAULT false
);
