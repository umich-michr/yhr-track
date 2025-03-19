-- Insert a sample TrackingRequest row for testing
INSERT INTO TRACKING_REQUEST (id, user_agent, user_id, page_url, cookie_language, browser_language, site_id, ip_address, log_time)
VALUES (10000, 'TestAgent', 'user-test', '/initial/url', 'en', 'en-US', 'site1', '127.0.0.1', CURRENT_TIMESTAMP);
