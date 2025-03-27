package edu.umich.med.michr.track.config.source;

import edu.umich.med.michr.track.config.ConfigConstants;

/**
 * Loads configuration from a file specified by an environment variable.
 */
public class EnvVarConfigurationSource extends FileConfigurationSource {

  public EnvVarConfigurationSource() {
    super(System.getenv(ConfigConstants.CONFIG_ENV_VAR));
  }

  @Override
  public String getSourceName() {
    return "environmentVarConfig";
  }
}
