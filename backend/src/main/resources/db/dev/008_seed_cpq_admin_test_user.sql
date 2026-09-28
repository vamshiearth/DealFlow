-- =========================================================
-- DEV ONLY
-- Seed CPQ_ADMIN test user for local DealFlow API testing.
-- Do not use this seed in production.
-- =========================================================

INSERT INTO users (
    id,
    email,
    first_name,
    last_name,
    password_hash,
    status,
    created_at,
    updated_at
)
SELECT
    5,
    'admin@dealflow.local',
    'Ethan',
    'Rao',
    '$2a$10$WATxTDT8dSTMNE2IfZyfludbqkGMTRS0FnuQjVvaqO1Phtte5qGGm',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'admin@dealflow.local'
);

INSERT INTO user_roles (
    user_id,
    role_id
)
SELECT
    u.id,
    5
FROM users u
WHERE u.email = 'admin@dealflow.local'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = 5
  );