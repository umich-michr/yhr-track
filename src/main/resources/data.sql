-- !!!! WARNING !!!!
-- This file contains DEVELOPMENT TEST DATA ONLY
-- It will only execute with embedded databases (H2) when spring.sql.init.mode=embedded
-- It will NOT execute with external databases like Oracle
-- DO NOT change spring.sql.init.mode to "always" in production!

INSERT INTO SITE (site_id, site_name) VALUES (1, 'Local Development Site');
INSERT INTO SITE_AUTHORIZED_DOMAIN (site_id, domain) VALUES (1, 'localhost');
INSERT INTO SITE_AUTHORIZED_DOMAIN (site_id, domain) VALUES (1, 'localhost:3000');
INSERT INTO SITE_AUTHORIZED_DOMAIN (site_id, domain) VALUES (1, 'localhost:8443');

