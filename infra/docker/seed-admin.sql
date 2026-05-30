-- Dev seed: one ADMIN and one TEACHER account so the Console is usable out of the box.
--   admin@itsh.dev   / admin   / Password123  -> ROLE_ADMIN
--   teacher@itsh.dev / teacher / Password123  -> ROLE_TEACHER
--
-- password_hash is BCrypt(cost 12) of "Password123" (matches BCryptPasswordEncoder(12)).
-- Run against identity_db AFTER identity-service has migrated (the roles table is seeded
-- by identity's Flyway V1). Idempotent: safe to run repeatedly.
--
--   docker compose -f infra/docker/docker-compose.full.yml exec -T postgres \
--     psql -U itsh -d identity_db < infra/docker/seed-admin.sql

INSERT INTO accounts (email, username, password_hash, status, email_verified, provider, created_by)
VALUES
    ('admin@itsh.dev',   'admin',   '$2a$12$u4986hzYiFvfIyNghS37SuyDQ5sznTFANy6PkQxym/.ELjHELPvfq', 'ACTIVE', TRUE, 'LOCAL', 'seed'),
    ('teacher@itsh.dev', 'teacher', '$2a$12$u4986hzYiFvfIyNghS37SuyDQ5sznTFANy6PkQxym/.ELjHELPvfq', 'ACTIVE', TRUE, 'LOCAL', 'seed')
ON CONFLICT (email) DO UPDATE
    SET status = 'ACTIVE', email_verified = TRUE;

-- admin -> ROLE_ADMIN
INSERT INTO account_roles (account_id, role_id)
SELECT a.id, r.id
FROM accounts a JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE a.email = 'admin@itsh.dev'
ON CONFLICT DO NOTHING;

-- teacher -> ROLE_TEACHER
INSERT INTO account_roles (account_id, role_id)
SELECT a.id, r.id
FROM accounts a JOIN roles r ON r.name = 'ROLE_TEACHER'
WHERE a.email = 'teacher@itsh.dev'
ON CONFLICT DO NOTHING;
