package edu.umich.med.michr.track.config.source;

import edu.umich.med.michr.track.config.ConfigurationException;
import edu.umich.med.michr.track.config.PropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads configuration from a file on the filesystem.
 */
public class FileConfigurationSource implements ConfigurationSource {
  private final String filePath;

  public FileConfigurationSource(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public boolean isAvailable() {
    if (filePath == null) {
      return false;
    }

    File file = new File(filePath);
    return file.exists() && file.isFile() && file.canRead();
  }

  @Override
  public PropertySource loadProperties() throws ConfigurationException {
    if (!isAvailable()) {
      return new PropertySource(new Properties(), filePath);
    }

    try (FileInputStream stream = new FileInputStream(filePath)) {
      Properties props = new Properties();
      props.load(stream);
      return new PropertySource(props, filePath);
    } catch (IOException e) {
      throw new ConfigurationException("Error loading properties from file: " + filePath, e);
    }
  }

  @Override
  public String getSourceName() {
    return filePath;
  }
}
