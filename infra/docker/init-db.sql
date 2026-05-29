-- Creates one database per microservice (database-per-service pattern).
-- Runs once on first Postgres container start.

CREATE DATABASE auth_db;
CREATE DATABASE identity_db;
CREATE DATABASE users_db;
CREATE DATABASE ielts_db;
CREATE DATABASE sat_db;
CREATE DATABASE learning_db;
CREATE DATABASE video_db;
CREATE DATABASE typing_db;
CREATE DATABASE analytics_db;
CREATE DATABASE leaderboard_db;
CREATE DATABASE notification_db;
CREATE DATABASE files_db;

GRANT ALL PRIVILEGES ON DATABASE auth_db         TO itsh;
GRANT ALL PRIVILEGES ON DATABASE identity_db     TO itsh;
GRANT ALL PRIVILEGES ON DATABASE users_db        TO itsh;
GRANT ALL PRIVILEGES ON DATABASE ielts_db        TO itsh;
GRANT ALL PRIVILEGES ON DATABASE sat_db          TO itsh;
GRANT ALL PRIVILEGES ON DATABASE learning_db     TO itsh;
GRANT ALL PRIVILEGES ON DATABASE video_db        TO itsh;
GRANT ALL PRIVILEGES ON DATABASE typing_db       TO itsh;
GRANT ALL PRIVILEGES ON DATABASE analytics_db    TO itsh;
GRANT ALL PRIVILEGES ON DATABASE leaderboard_db  TO itsh;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO itsh;
GRANT ALL PRIVILEGES ON DATABASE files_db        TO itsh;
