package edu.umich.med.michr.track.config;

import edu.umich.med.michr.track.config.filter.EnvPropertyFilter;
import edu.umich.med.michr.track.config.filter.PropertyFilter;
import edu.umich.med.michr.track.config.provider.ConfigurationSourceProvider;
import edu.umich.med.michr.track.config.provider.DefaultConfigurationSourceProvider;
import edu.umich.med.michr.track.config.source.ConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.List;
import java.util.Optional;

/**
 * Spring EnvironmentPostProcessor that loads external configuration from various sources
 * with a defined precedence order.
 */
public class ExternalConfigEnvironmentPostProcessor implements EnvironmentPostProcessor {
  private static final Logger logger = LoggerFactory.getLogger(ExternalConfigEnvironmentPostProcessor.class);

  private final ConfigurationSourceProvider sourceProvider;
  private final PropertyFilter propertyFilter;

  /**
   * Default constructor required by Spring Boot's EnvironmentPostProcessor mechanism by reading entries from META-INF/spring.factories.
   * This loader uses reflection to instantiate your processor and requires a no-argument constructor.
   *
   * @see org.springframework.core.io.support.SpringFactoriesLoader
   */
  @SuppressWarnings("unused")
  public ExternalConfigEnvironmentPostProcessor() {
    this(new DefaultConfigurationSourceProvider(), new EnvPropertyFilter());
  }

  protected ExternalConfigEnvironmentPostProcessor(
      ConfigurationSourceProvider sourceProvider,
      PropertyFilter propertyFilter) {
    this.sourceProvider = sourceProvider;
    this.propertyFilter = propertyFilter;
  }

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    final String env = System.getProperty(ConfigConstants.ENV);
    logger.info("Processing external configuration with env={}", env);

    List<ConfigurationSource> sources = sourceProvider.getConfigurationSources();

    // later sources override earlier ones
    sources.forEach(source ->
        processConfigurationSource(source, env, environment)
    );
  }

  private void processConfigurationSource(ConfigurationSource source, String env, ConfigurableEnvironment environment) {
    Optional.of(source)
        .filter(this::isSourceAvailable)
        .flatMap(this::loadPropertiesSafely)
        .filter(this::hasPropertiesOrLog)
        .map(propSource -> applyFilteringIfNeeded(propSource, env))
        .ifPresent(propSource -> addToEnvironment(propSource, environment));
  }

  private boolean isSourceAvailable(ConfigurationSource source) {
    boolean isAvailable = source.isAvailable();
    if (!isAvailable) {
      logger.debug("Skipping configuration source {}", source.getSourceName());
    }
    return isAvailable;
  }

  private Optional<PropertySource> loadPropertiesSafely(ConfigurationSource source) {
    try {
      return Optional.of(source.loadProperties());
    } catch (ConfigurationException e) {
      logger.warn("Failed to process configuration from {}: {}",
          source.getSourceName(), e.getMessage(), e);
      return Optional.empty();
    }
  }

  private boolean hasPropertiesOrLog(PropertySource propSource) {
    boolean hasProperties = propSource.hasProperties();
    if (!hasProperties) {
      logger.debug("No properties found in source {}, skipping", propSource.sourceName());
    }
    return hasProperties;
  }

  private PropertySource applyFilteringIfNeeded(PropertySource propSource, String env) {
    if (env != null && !env.trim().isEmpty()) {
      return propertyFilter.filterProperties(propSource, env);
    }
    return propSource;
  }

  /**
   * Adds a property source to Spring's environment and logs the operation.
   *
   * <p>In Spring, configuration properties are managed through a hierarchy of {@link org.springframework.core.env.PropertySource}
   * objects. Each property source represents a distinct source of configuration (e.g., system properties,
   * application.properties, environment variables).</p>
   *
   * <p><strong>Property Source Naming:</strong><br>
   * The "externalConfig:" prefix in the property source name serves multiple purposes:
   * <ul>
   *   <li>Provides clear identification in logs and debugging output</li>
   *   <li>Prevents name collisions with other property sources</li>
   *   <li>Follows Spring conventions for property source naming</li>
   *   <li>Makes it easier to manipulate these sources programmatically</li>
   * </ul>
   * </p>
   *
   * <p><strong>Property Source Precedence:</strong><br>
   * By using {@code addLast()}, we're giving these external properties the lowest precedence
   * in the environment. This means that properties defined elsewhere (like system properties or
   * environment variables) will override these external configuration values. To change this behavior,
   * consider using {@code addFirst()} or {@code addBefore()/addAfter()} methods instead.</p>
   *
   * <p><strong>Property Resolution:</strong><br>
   * When code calls {@code environment.getProperty("some.key")}, Spring searches through all property
   * sources in order of precedence until it finds a matching property.</p>
   *
   * @param propSource The property source containing the external configuration properties
   * @param environment The Spring environment to which the properties should be added
   *
   * @see org.springframework.core.env.PropertySource
   * @see org.springframework.core.env.ConfigurableEnvironment#getPropertySources()
   */
  private void addToEnvironment(PropertySource propSource, ConfigurableEnvironment environment) {
    environment.getPropertySources().addLast(
        new PropertiesPropertySource(
            "externalConfig:" + propSource.sourceName(),
            propSource.properties()
        )
    );

    logger.info("Loaded {} properties from {}",
        propSource.properties().size(), propSource.sourceName());
  }
}
