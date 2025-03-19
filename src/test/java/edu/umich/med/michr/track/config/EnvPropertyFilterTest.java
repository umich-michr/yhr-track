package edu.umich.med.michr.track.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class EnvPropertyFilterTest {

  private final EnvPropertyFilter filter = new EnvPropertyFilter();

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t", "\n"})
  @DisplayName("Should return original source when env is null or empty")
  void shouldReturnOriginalSourceWhenEnvIsNullOrEmpty(String env) {
    // Arrange
    Properties props = new Properties();
    props.setProperty("key", "value");
    PropertySource expected = new PropertySource(props, "testSource");

    // Act
    final PropertySource actual = filter.filterProperties(expected, env);

    // Assert
    assertSame(expected, actual, "Should return the same expected object without modification");
  }

  @Test
  @DisplayName("Should return original source when source has no properties")
  void shouldReturnOriginalSourceWhenSourceHasNoProperties() {
    // Arrange
    PropertySource expected = new PropertySource(new Properties(), "expected");

    // Act
    final PropertySource actual = filter.filterProperties(expected, "dev");

    // Assert
    assertSame(expected, actual, "Should return the same source object without modification");
  }

  @Test
  @DisplayName("Should return empty properties when no properties match the env prefix")
  void shouldReturnEmptyPropertiesWhenNoPropertiesMatchEnvPrefix() {
    // Arrange
    Properties props = new Properties();
    props.setProperty("common.property", "value");
    props.setProperty("prod.property", "prod-value");
    PropertySource source = new PropertySource(props, "testSource");

    // Act
    final PropertySource actual = filter.filterProperties(source, "dev");

    // Assert
    assertTrue(actual.properties().isEmpty(), "Should return empty properties");
    assertTrue(actual.sourceName().contains("empty"), "Source name should indicate empty result");
  }

  @Test
  @DisplayName("Should extract properties with environment prefix and remove prefix")
  void shouldExtractPropertiesWithEnvPrefixAndRemovePrefix() {
    // Arrange
    Properties props = new Properties();
    props.setProperty("dev.db.url", "dev-db-url");
    props.setProperty("dev.app.name", "dev-app");
    props.setProperty("prod.db.url", "prod-db-url");
    props.setProperty("common.property", "common-value");
    PropertySource source = new PropertySource(props, "testSource");

    // Act
    final PropertySource actual = filter.filterProperties(source, "dev");

    // Assert
    assertEquals(2, actual.properties().size(), "Should contain only matching properties");
    assertEquals("dev-db-url", actual.properties().getProperty("db.url"), "Prefix should be removed from keys");
    assertEquals("dev-app", actual.properties().getProperty("app.name"), "Prefix should be removed from keys");
    assertNull(actual.properties().getProperty("prod.db.url"), "Should not contain other env properties");
    assertNull(actual.properties().getProperty("common.property"), "Should not contain common properties");
    assertTrue(actual.sourceName().contains("filtered"), "Source name should indicate filtering");
    assertTrue(actual.sourceName().contains("dev"), "Source name should indicate environment");
  }

  @Test
  @DisplayName("Should handle properties with exact match to prefix")
  void shouldHandlePropertiesWithExactMatchToPrefix() {
    // Arrange
    Properties props = new Properties();
    props.setProperty("dev", "dev-value");  // Exact match to prefix
    props.setProperty("dev.property", "dev-property-value");
    PropertySource source = new PropertySource(props, "testSource");

    // Act
    final PropertySource actual = filter.filterProperties(source, "dev");

    // Assert
    assertEquals(1, actual.properties().size(), "Should contain only properties with prefix + dot");
    assertEquals("dev-property-value", actual.properties().getProperty("property"));
    assertNull(actual.properties().getProperty(""), "Should not have empty key from exact prefix match");
  }
}
