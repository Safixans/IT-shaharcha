-- Dev seed: one loginable ACTIVE student account.
--   email:    student@itsh.dev
--   username: student
--   password: Password123
-- password_hash is BCrypt(cost 12) of "Password123" (matches BCryptPasswordEncoder(12)).
-- Idempotent: safe to run multiple times.

INSERT INTO accounts (email, username, password_hash, status, email_verified, provider, created_by)
VALUES (
    'student@itsh.dev',
    'student',
    '$2a$12$u4986hzYiFvfIyNghS37SuyDQ5sznTFANy6PkQxym/.ELjHELPvfq',
    'ACTIVE',
    TRUE,
    'LOCAL',
    'seed'
)
ON CONFLICT (email) DO UPDATE
    SET password_hash = EXCLUDED.password_hash,
        status        = 'ACTIVE',
        email_verified = TRUE;

INSERT INTO account_roles (account_id, role_id)
SELECT a.id, r.id
FROM accounts a
JOIN roles r ON r.name = 'ROLE_STUDENT'
WHERE a.email = 'student@itsh.dev'
ON CONFLICT DO NOTHING;
