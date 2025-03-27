package edu.umich.med.michr.track.config.cors;

import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsFilterConfig {

  private final CorsConfigurationSource corsConfigurationSource;

  public CorsFilterConfig(CorsConfigurationSource corsConfigurationSource) {
    this.corsConfigurationSource = corsConfigurationSource;
  }

  @Bean
  public Filter corsFilter() {
    return new CorsFilter(corsConfigurationSource);
  }
}
