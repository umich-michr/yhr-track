package edu.umich.med.michr.track.util;

import edu.umich.med.michr.track.domain.ClientConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Utility class for creating test data and test objects.
 * Centralizes test data creation to avoid duplication across test classes.
 */
public class TestUtils {

  // Constants for common test values
  public static final Instant FIXED_INSTANT = LocalDateTime
      .parse("2025-03-26 22:56:38", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
      .toInstant(ZoneOffset.UTC);
  public static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

  private TestUtils() {}

  public static ClientConfiguration createClientConfig(String clientId, String name, String... domains) {
    ClientConfiguration config = new ClientConfiguration();
    ReflectionTestUtils.setField(config, "id", clientId);
    ReflectionTestUtils.setField(config, "name", name);
    ReflectionTestUtils.setField(config, "authorizedOrigins", Arrays.asList(domains));
    return config;
  }
}
