package edu.umich.med.michr.track.config;

/**
 * Represents a source of configuration properties.
 */
public interface ConfigurationSource {
  /**
   * Checks if this configuration source is available.
   *
   * @return true if the source exists and can be accessed, false otherwise
   */
  boolean isAvailable();

  /**
   * Loads properties from this source.
   *
   * @return A PropertySource containing the loaded properties
   * @throws ConfigurationException if an error occurs loading the properties
   */
  PropertySource loadProperties() throws ConfigurationException;

  /**
   * Gets a descriptive name for this configuration source.
   *
   * @return The source name
   */
  String getSourceName();
}
