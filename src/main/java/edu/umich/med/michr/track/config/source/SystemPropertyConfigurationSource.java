package edu.umich.med.michr.track.config.source;

import edu.umich.med.michr.track.config.ConfigConstants;

/**
 * Loads configuration from a file specified by a system property.
 */
public class SystemPropertyConfigurationSource extends FileConfigurationSource {

  public SystemPropertyConfigurationSource() {
    super(System.getProperty(ConfigConstants.CONFIG_PROPERTY));
  }

  @Override
  public String getSourceName() {
    return "systemPropertyConfig";
  }
}
