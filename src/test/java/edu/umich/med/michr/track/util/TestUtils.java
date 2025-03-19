package edu.umich.med.michr.track.util;

import edu.umich.med.michr.track.domain.SiteConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for creating test data and test objects.
 * Centralizes test data creation to avoid duplication across test classes.
 */
public class TestUtils {

  // Constants for common test values
  public static final String VALID_SITE_ID = "test-site";
  public static final String VALID_PAGE_URL = "https://example.com/page";

  private TestUtils() {}

  public static SiteConfiguration createSiteConfig(String siteId, String siteName, String... domains) {
    SiteConfiguration config = new SiteConfiguration();
    ReflectionTestUtils.setField(config, "siteId", siteId);
    ReflectionTestUtils.setField(config, "siteName", siteName);
    ReflectionTestUtils.setField(config, "authorizedDomains", Arrays.asList(domains));
    return config;
  }

  public static Set<String> createAuthorizedDomains() {
    return new HashSet<>(Arrays.asList(
        "https://example.com",
        "https://test-app.org",
        "https://your-tracked-app.com"
    ));
  }
}
