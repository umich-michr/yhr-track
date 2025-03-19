package edu.umich.med.michr.track.config;

import java.util.List;

/**
 * Provides a list of configuration sources in order of precedence.
 */
public interface ConfigurationSourceProvider {
  /**
   * Gets the available configuration sources in order of increasing precedence.
   * Sources later in the list override properties from earlier sources.
   *
   * @return List of configuration sources
   */
  List<ConfigurationSource> getConfigurationSources();
}
