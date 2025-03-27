package edu.umich.med.michr.track.config.source;

import edu.umich.med.michr.track.config.ConfigurationException;
import edu.umich.med.michr.track.config.PropertySource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("FileConfigurationSource Behavior")
class FileConfigurationSourceTest {

  @Nested
  @DisplayName("Availability Checks")
  class AvailabilityChecks {

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("should report unavailable when file path is null")
    void testIsAvailable_FilePathIsNull(String filePath) {
      FileConfigurationSource actual = new FileConfigurationSource(filePath);

      assertFalse(actual.isAvailable());
    }

    @ParameterizedTest(name = "should report unavailable for non-existent file: {0}")
    @ValueSource(strings = {"/nonexistent/path/config.properties", "C:\\nonexistent\\config.properties"})
    @DisplayName("should report unavailable for non-existent files")
    void testIsAvailable_NonExistentFile(String filePath) {
      FileConfigurationSource actual = new FileConfigurationSource(filePath);

      assertFalse(actual.isAvailable());
    }

    @Test
    @DisplayName("should report available for an existing, readable file")
    void testIsAvailable_ExistingReadableFile(@TempDir Path tempDir) throws Exception {
      File tempFile = tempDir.resolve("config.properties").toFile();
      try (FileOutputStream fos = new FileOutputStream(tempFile)) {
        fos.write("key=value".getBytes());
      }
      FileConfigurationSource actual = new FileConfigurationSource(tempFile.getAbsolutePath());

      assertTrue(actual.isAvailable());
    }

    @Test
    @DisplayName("should return false when the path is a directory, not a file")
    void testIsAvailable_DirectoryNotFile(@TempDir Path tempDir) {
      FileConfigurationSource actual = new FileConfigurationSource(tempDir.toAbsolutePath().toString());

      assertFalse(actual.isAvailable());
    }
  }

  @Nested
  @DisplayName("Properties Loading Behavior")
  class PropertiesLoadingBehavior {

    @Test
    @DisplayName("should load empty properties and retain provided source name when file is not available")
    void testLoadProperties_NonExistentFile() throws ConfigurationException {
      String filePath = "/nonexistent/path/config.properties";
      FileConfigurationSource source = new FileConfigurationSource(filePath);

      PropertySource actual = source.loadProperties();

      assertTrue(actual.properties().isEmpty());
      assertEquals(filePath, actual.sourceName());
    }

    @Test
    @DisplayName("should load properties correctly from a valid file")
    void testLoadProperties_ValidFile(@TempDir Path tempDir) throws Exception {
      File tempFile = tempDir.resolve("config.properties").toFile();

      String propertiesContent =
          """
              # Test properties file
              app.name=TestApp
              app.version=1.0
              app.environment=test
              """;

      Files.writeString(tempFile.toPath(), propertiesContent);

      FileConfigurationSource actual = new FileConfigurationSource(tempFile.getAbsolutePath());

      assertThat(actual.isAvailable()).isTrue();

      PropertySource propSource = actual.loadProperties();

      Properties loadedProps = propSource.properties();
      assertNotNull(loadedProps);
      assertEquals(3, loadedProps.size());
      assertEquals("TestApp", loadedProps.getProperty("app.name"));
      assertEquals("1.0", loadedProps.getProperty("app.version"));
      assertEquals("test", loadedProps.getProperty("app.environment"));

      assertThat(propSource.sourceName()).isEqualTo(tempFile.getAbsolutePath());
    }

    @Test
    @DisplayName("should load properties with characters correctly")
    void testLoadProperties_SpecialCharacters(@TempDir Path tempDir) throws Exception {
      PropertySource actual = getPropertySource(tempDir);

      Properties loadedProps = actual.properties();
      assertEquals(4, loadedProps.size());
      assertEquals("!@#$%^&*()_+", loadedProps.getProperty("special.chars"));
      assertEquals("C:\\Windows\\System32", loadedProps.getProperty("path.with.backslash"));
      assertEquals("こんにちは", loadedProps.getProperty("unicode.chars"));
      assertTrue(loadedProps.getProperty("empty.value").isEmpty());
    }

    @Test
    @DisplayName("should return empty properties and provided file name when file is not available")
    void testLoadProperties_FileNotAvailable() throws ConfigurationException {
      FileConfigurationSource actual = new FileConfigurationSource("nonexistent.properties");

      PropertySource propSource = actual.loadProperties();

      assertTrue(propSource.properties().isEmpty());
      assertEquals("nonexistent.properties", propSource.sourceName());
    }

    @Test
    @DisplayName("should handle IOException during property loading")
    void testLoadProperties_IoExceptionHandling(@TempDir Path tempDir) throws Exception {
      Path tempFilePath = tempDir.resolve("corrupt.properties");
      File tempFile = tempFilePath.toFile();

      // Create a partial mock/spy to test the IOException handling
      FileConfigurationSource sourceSpy = spy(new FileConfigurationSource(tempFile.getAbsolutePath()));

      Files.writeString(tempFilePath, "key=value");

      assertThat(tempFile.exists()).isTrue();
      assertThat(tempFile.canRead()).isTrue();

      // Create an unreadable file situation
      // Note: This is platform-dependent and might not work on all systems
      // For Windows, we can try making the file read-only and then secretly zero it out
      if (tempFile.setReadable(false)) {
        // The file exists but can't be read - this should trigger our error path
        PropertySource actual = sourceSpy.loadProperties();

        assertTrue(actual.properties().isEmpty());
        assertEquals(tempFile.getAbsolutePath(), actual.sourceName());

        verify(sourceSpy).isAvailable();
      } else {
        // If we can't make the file unreadable, let's verify how the real implementation works
        // Make the spy return true for isAvailable() but create a corrupted file scenario
        doReturn(true).when(sourceSpy).isAvailable();

        // Set the file to an empty or corrupt state if possible
        Files.write(tempFilePath, new byte[0]);

        PropertySource actual = sourceSpy.loadProperties();

        assertTrue(actual.properties().isEmpty());
        assertEquals(tempFile.getAbsolutePath(), actual.sourceName());
      }
    }

    @Test
    @DisplayName("should throw ConfigurationException when file disappears after availability check")
    void testLoadProperties_FileDisappears(@TempDir Path tempDir) throws Exception {
      File tempFile = tempDir.resolve("disappearing.properties").toFile();
      try (FileOutputStream fos = new FileOutputStream(tempFile)) {
        fos.write("key=value".getBytes());
      }

      FileConfigurationSource source = new FileConfigurationSource(tempFile.getAbsolutePath()) {
        @Override
        public boolean isAvailable() {
          return true;
        }
      };

      // Delete the file after checking availability but before loading
      Files.delete(tempFile.toPath());

      ConfigurationException exception = assertThrows(
          ConfigurationException.class,
          source::loadProperties
      );

      assertTrue(exception.getMessage().contains("Error loading properties from file: " + tempFile.getAbsolutePath()));
      assertInstanceOf(IOException.class, exception.getCause());
    }
  }

  @Nested
  @DisplayName("Source Name Retrieval Behavior")
  class SourceNameRetrievalBehavior {

    @Test
    @DisplayName("should return the file path as the source name")
    void testGetSourceName(@TempDir Path tempDir) {
      File tempFile = tempDir.resolve("config.properties").toFile();
      FileConfigurationSource source = new FileConfigurationSource(tempFile.getAbsolutePath());

      String actual = source.getSourceName();

      assertEquals(tempFile.getAbsolutePath(), actual);
    }

    @Test
    @DisplayName("should return null when file path is null")
    void testGetSourceName_NullPath() {
      FileConfigurationSource source = new FileConfigurationSource(null);

      String actual = source.getSourceName();

      assertNull(actual);
    }
  }

  private static PropertySource getPropertySource(Path tempDir) throws IOException, ConfigurationException {
    File tempFile = tempDir.resolve("special.properties").toFile();

    Properties props = new Properties();
    props.setProperty("special.chars", "!@#$%^&*()_+");
    props.setProperty("path.with.backslash", "C:\\Windows\\System32");
    props.setProperty("unicode.chars", "こんにちは");
    props.setProperty("empty.value", "");

    try (FileOutputStream out = new FileOutputStream(tempFile)) {
      props.store(out, "Test with special characters");
    }

    FileConfigurationSource source = new FileConfigurationSource(tempFile.getAbsolutePath());
    return source.loadProperties();
  }
}
