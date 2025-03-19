package edu.umich.med.michr.track.config;

import java.io.File;

/**
 * Loads configuration from a file in the user's home directory.
 */
public class UserHomeConfigurationSource extends FileConfigurationSource {

  public UserHomeConfigurationSource() {
    super(buildUserHomePath());
  }

  private static String buildUserHomePath() {
    return System.getProperty("user.home") + File.separator + ConfigConstants.USER_CONFIG_PATH;
  }

  @Override
  public String getSourceName() {
    return "userHomeConfig";
  }
}
