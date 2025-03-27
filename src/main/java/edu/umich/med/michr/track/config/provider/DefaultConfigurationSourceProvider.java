package edu.umich.med.michr.track.config.provider;

import edu.umich.med.michr.track.config.source.ConfigurationSource;
import edu.umich.med.michr.track.config.source.EnvVarConfigurationSource;
import edu.umich.med.michr.track.config.source.SystemPropertyConfigurationSource;
import edu.umich.med.michr.track.config.source.UserHomeConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of ConfigurationSourceProvider that provides
 * user home, environment variable, and system property sources in
 * order of increasing precedence.
 */
public class DefaultConfigurationSourceProvider implements ConfigurationSourceProvider {

  @Override
  public List<ConfigurationSource> getConfigurationSources() {
    return Arrays.asList(
        new UserHomeConfigurationSource(),    // Lowest precedence
        new EnvVarConfigurationSource(),
        new SystemPropertyConfigurationSource() // Highest precedence
    );
  }
}
