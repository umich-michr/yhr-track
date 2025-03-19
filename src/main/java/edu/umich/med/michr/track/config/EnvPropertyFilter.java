package edu.umich.med.michr.track.config;

import java.util.Properties;

/**
 * Filters properties based on an environment prefix.
 * <p>
 * This filter extracts properties that have a specific environment prefix (e.g., "dev.property.name")
 * and makes them available without the prefix (e.g., "property.name"). This allows for environment-specific
 * configuration in a single properties file.
 * </p>
 * <p>
 * Example:
 * <pre>
 * # Original properties:
 * app.name=My App
 * dev.db.url=jdbc:h2:mem:dev
 * prod.db.url=jdbc:mysql://production
 *
 * # After filtering with env="dev":
 * db.url=jdbc:h2:mem:dev
 * # (app.name is not included as it doesn't have the env prefix)
 * </pre>
 * </p>
 */
public class EnvPropertyFilter implements PropertyFilter {

  /**
   * Filters properties by extracting only those with the specified environment prefix
   * and removing that prefix from the property key.
   *
   * @param source The source containing the properties to filter
   * @param env The environment prefix to filter by (e.g., "dev", "prod")
   * @return A new PropertySource containing only the properties for the specified environment,
   *         or the original source if no environment is specified or no properties match
   */
  @Override
  public PropertySource filterProperties(PropertySource source, String env) {
    if (shouldSkipFiltering(source, env)) {
      return source;
    }

    Properties filteredProperties = extractPropertiesWithPrefix(source.properties(), env);

    if (filteredProperties.isEmpty()) {
      return new PropertySource(new Properties(), source.sourceName() + "[filtered:" + env + "](empty)");
    }

    return new PropertySource(filteredProperties, source.sourceName() + "[filtered:" + env + "]");
  }

  private boolean shouldSkipFiltering(PropertySource source, String env) {
    return env == null || env.trim().isEmpty() || !source.hasProperties();
  }

  private Properties extractPropertiesWithPrefix(Properties sourceProperties, String env) {
    final String prefix = env + ".";
    Properties filteredProperties = new Properties();

    for (String key : sourceProperties.stringPropertyNames()) {
      if (key.startsWith(prefix)) {
        String unprefixedKey = key.substring(prefix.length());
        filteredProperties.put(unprefixedKey, sourceProperties.getProperty(key));
      }
    }

    return filteredProperties;
  }
}
