package edu.umich.med.michr.track.config.cors;

import edu.umich.med.michr.track.service.ClientConfigurationService;
import jakarta.inject.Inject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Sets up dynamic CORS for the app so origins can be checked at request time.
 * Static Cors configuration (conventional) setup will not work because the allowed origins
 * are read from the database and when running the app in development mode the db is populated
 * after spring context is set up making it impossible to have the static config the information
 * coming from the db.
 * <p>
 * Uses {@link DynamicCorsConfiguration} to validate allowed origins from the database via
 * {@link ClientConfigurationService}. This ensures CORS logic is executed per request.
 * </p>
 */
@Configuration
public class CorsConfig {

  private final ClientConfigurationService clientConfigService;

  @Inject
  public CorsConfig(ClientConfigurationService clientConfigService) {
    this.clientConfigService = clientConfigService;
  }

  /**
   * The corsConfigurationSource bean provides a CorsConfigurationSource that Spring Boot automatically uses to configure a CorsFilter.
   * This filter applies CORS checks per request.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    DynamicCorsConfiguration configuration = new DynamicCorsConfiguration(clientConfigService);
    configuration.applyDefaultSettings();
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/analytics/events", configuration);
    return source;
  }
}
