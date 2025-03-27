package edu.umich.med.michr.track.config;

import java.util.Properties;

/**
 * Wraps a Properties object with metadata about its source.
 */
public record PropertySource(Properties properties, String sourceName) {
  public boolean hasProperties() {
    return properties != null && !properties.isEmpty();
  }
}
