package edu.umich.med.michr.track.config;

import edu.umich.med.michr.track.service.SiteConfigurationService;
import jakarta.inject.Inject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Set;

/**
 * CORS configuration that dynamically loads allowed origins from the database
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  private final SiteConfigurationService siteConfigService;

  @Inject
  public CorsConfig(SiteConfigurationService siteConfigService) {
    this.siteConfigService = siteConfigService;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    Set<String> domains = siteConfigService.getAllAuthorizedDomains();

    registry.addMapping("/analytics/requests")
        .allowedOrigins(domains.toArray(new String[0]))
        .allowedMethods("GET", "POST")
        .allowedHeaders("*")
        .allowCredentials(!domains.contains("*"));
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    Set<String> domains = siteConfigService.getAllAuthorizedDomains();

    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.copyOf(domains));
    configuration.setAllowedMethods(List.of("GET", "POST"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(!domains.contains("*"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/analytics/requests", configuration);
    return source;
  }
}
