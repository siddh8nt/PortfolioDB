-- Run this in your Railway MySQL console as an admin user.
-- Replace DEMO_PASSWORD_HERE with a strong password.

CREATE USER IF NOT EXISTS 'portfolio_demo'@'%' IDENTIFIED BY 'DEMO_PASSWORD_HERE';

GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE
ON portfoliodb.*
TO 'portfolio_demo'@'%';

FLUSH PRIVILEGES;

-- Optional cleanup after presentation:
-- DROP USER 'portfolio_demo'@'%';
