-- Creates one database per service (database-per-service pattern).
-- Aligned to the 5-service contract in specs/. Runs once on first Postgres start.

CREATE DATABASE identity_db;
CREATE DATABASE learning_db;
CREATE DATABASE assessment_db;
CREATE DATABASE portfolio_db;
CREATE DATABASE analytics_db;

GRANT ALL PRIVILEGES ON DATABASE identity_db   TO itsh;
GRANT ALL PRIVILEGES ON DATABASE learning_db   TO itsh;
GRANT ALL PRIVILEGES ON DATABASE assessment_db TO itsh;
GRANT ALL PRIVILEGES ON DATABASE portfolio_db  TO itsh;
GRANT ALL PRIVILEGES ON DATABASE analytics_db  TO itsh;
