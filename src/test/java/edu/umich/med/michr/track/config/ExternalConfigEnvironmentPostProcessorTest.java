package edu.umich.med.michr.track.config;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalConfigEnvironmentPostProcessorTest {

  @Mock
  private ConfigurationSourceProvider sourceProvider;

  @Mock
  private PropertyFilter propertyFilter;

  @Mock
  private ConfigurableEnvironment environment;

  @Mock
  private SpringApplication application;

  @Mock
  private MutablePropertySources propertySources;

  @Mock
  private ConfigurationSource source1;

  @Mock
  private ConfigurationSource source2;

  private ExternalConfigEnvironmentPostProcessor processor;

  @BeforeEach
  void setUp() {
    processor = new ExternalConfigEnvironmentPostProcessor(sourceProvider, propertyFilter);
  }

  @AfterEach
  void tearDown() {
    System.clearProperty(ConfigConstants.CONFIG_PROPERTY);
    System.clearProperty(ConfigConstants.ENV);
  }

  @Test
  @DisplayName("Default constructor should create a working processor that loads and adds properties")
  void defaultConstructorShouldCreateWorkingProcessor(@TempDir Path tempDir) throws IOException {
    // Arrange
    when(environment.getPropertySources()).thenReturn(propertySources);
    ExternalConfigEnvironmentPostProcessor processor = new ExternalConfigEnvironmentPostProcessor();

    Path testPropsFile = tempDir.resolve("test.properties");
    Properties testProps = new Properties();
    testProps.setProperty("test.key", "test.value");
    testProps.setProperty("another.key", "another.value");

    try (OutputStream out = Files.newOutputStream(testPropsFile)) {
      testProps.store(out, "Test Properties");
    }

    System.setProperty(ConfigConstants.CONFIG_PROPERTY, testPropsFile.toAbsolutePath().toString());

    // Act
    processor.postProcessEnvironment(environment, application);

    // Assert
    ArgumentCaptor<PropertiesPropertySource> captor =
        ArgumentCaptor.forClass(PropertiesPropertySource.class);

    verify(propertySources, atLeastOnce()).addLast(captor.capture());

    // Find the property source that contains our test properties
    PropertiesPropertySource actualSource = captor.getAllValues().stream()
        .filter(source -> "test.value".equals(source.getProperty("test.key")))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Property source with test property not found"));

    // Assert property values are correctly loaded
    assertEquals("test.value", actualSource.getProperty("test.key"));
    assertEquals("another.value", actualSource.getProperty("another.key"));

    // Assert source name follows the convention with "externalConfig:" prefix
    assertTrue(actualSource.getName().startsWith("externalConfig:"),
        "Property source name should start with 'externalConfig:'");
  }

  @Nested
  @DisplayName("Property Loading and Processing Tests")
  class PropertyLoadingTests {
    @Test
    @DisplayName("Should skip sources with no properties")
    void shouldSkipSourceWithNoProperties() throws ConfigurationException {
      // Arrange
      when(sourceProvider.getConfigurationSources()).thenReturn(Collections.singletonList(source1));
      when(source1.isAvailable()).thenReturn(true);

      PropertySource emptyPropertySource = mock(PropertySource.class);
      when(emptyPropertySource.hasProperties()).thenReturn(false);
      when(emptyPropertySource.sourceName()).thenReturn("source1");
      when(source1.loadProperties()).thenReturn(emptyPropertySource);

      // Act
      processor.postProcessEnvironment(environment, application);

      // Assert
      verify(propertySources, never()).addLast(any());
    }
  }

  @Nested
  @DisplayName("Environment-Based Property Filtering Tests")
  class PropertyFilteringTests {
    @BeforeEach
    void setUp() {
      when(environment.getPropertySources()).thenReturn(propertySources);
    }

    @Test
    @DisplayName("Should apply filtering when environment is specified")
    void shouldApplyFilteringWithEnv() throws ConfigurationException {
      // Arrange
      System.setProperty(ConfigConstants.ENV, "dev");
      when(sourceProvider.getConfigurationSources()).thenReturn(Collections.singletonList(source1));
      when(source1.isAvailable()).thenReturn(true);

      PropertySource originalPropertySource = mock(PropertySource.class);
      when(originalPropertySource.hasProperties()).thenReturn(true);
      when(source1.loadProperties()).thenReturn(originalPropertySource);

      Properties filteredProps = new Properties();
      filteredProps.setProperty("filteredKey", "filteredValue");

      PropertySource filteredPropertySource = mock(PropertySource.class);
      when(filteredPropertySource.sourceName()).thenReturn("source1-filtered");
      when(filteredPropertySource.properties()).thenReturn(filteredProps);

      when(propertyFilter.filterProperties(originalPropertySource, "dev")).thenReturn(filteredPropertySource);

      // Act
      processor.postProcessEnvironment(environment, application);

      // Assert
      ArgumentCaptor<PropertiesPropertySource> captor = ArgumentCaptor.forClass(PropertiesPropertySource.class);
      verify(propertySources).addLast(captor.capture());

      PropertiesPropertySource capturedSource = captor.getValue();
      assertEquals("externalConfig:source1-filtered", capturedSource.getName());
      assertEquals("filteredValue", capturedSource.getProperty("filteredKey"));
    }

    @Test
    @DisplayName("Should not apply filtering when environment is empty")
    void shouldNotApplyFilteringWithEmptyEnv() throws ConfigurationException {
      // Arrange
      System.setProperty(ConfigConstants.ENV, "  ");
      when(sourceProvider.getConfigurationSources()).thenReturn(Collections.singletonList(source1));
      when(source1.isAvailable()).thenReturn(true);

      Properties props = new Properties();
      props.setProperty("key1", "value1");

      PropertySource propertySource = mock(PropertySource.class);
      when(propertySource.hasProperties()).thenReturn(true);
      when(propertySource.sourceName()).thenReturn("source1");
      when(propertySource.properties()).thenReturn(props);
      when(source1.loadProperties()).thenReturn(propertySource);

      // Act
      processor.postProcessEnvironment(environment, application);

      // Assert
      verify(propertyFilter, never()).filterProperties(any(), any());

      ArgumentCaptor<PropertiesPropertySource> captor = ArgumentCaptor.forClass(PropertiesPropertySource.class);
      verify(propertySources).addLast(captor.capture());

      PropertiesPropertySource capturedSource = captor.getValue();
      assertEquals("externalConfig:source1", capturedSource.getName());
      assertEquals("value1", capturedSource.getProperty("key1"));
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {
    @BeforeEach
    void setUp() {
      when(environment.getPropertySources()).thenReturn(propertySources);
    }

    @Test
    @DisplayName("Should continue to next source when a source throws exception")
    void shouldContinueToNextSourceWhenSourceThrowsException() throws ConfigurationException {
      // Arrange
      when(sourceProvider.getConfigurationSources()).thenReturn(Arrays.asList(source1, source2));
      when(source1.isAvailable()).thenReturn(true);
      when(source2.isAvailable()).thenReturn(true);

      when(source1.loadProperties()).thenThrow(new ConfigurationException("Test exception"));

      Properties props2 = new Properties();
      props2.setProperty("key2", "value2");

      PropertySource propertySource2 = mock(PropertySource.class);
      when(propertySource2.hasProperties()).thenReturn(true);
      when(propertySource2.sourceName()).thenReturn("source2");
      when(propertySource2.properties()).thenReturn(props2);
      when(source2.loadProperties()).thenReturn(propertySource2);

      // Act
      processor.postProcessEnvironment(environment, application);

      // Assert
      ArgumentCaptor<PropertiesPropertySource> captor = ArgumentCaptor.forClass(PropertiesPropertySource.class);
      verify(propertySources).addLast(captor.capture());

      PropertiesPropertySource capturedSource = captor.getValue();
      assertEquals("externalConfig:source2", capturedSource.getName());
      assertEquals("value2", capturedSource.getProperty("key2"));
    }
  }
}
