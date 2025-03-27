package edu.umich.med.michr.track.config.cors;

import edu.umich.med.michr.track.service.ClientConfigurationService;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;
import java.util.Set;

/**
 * A class that dynamically checks allowed origins for CORS requests.
 * <p>
 * Unlike static CORS setups, this class uses {@link ClientConfigurationService} to fetch the latest
 * list of allowed origins from the database during each request. This keeps the configuration
 * up-to-date even if the allowed origins change after the app starts. However, pay attention to {@link ClientConfigurationService}
 * if it caches the allowed origins for performance reasons then allowed origins may not be updated.
 * </p>
 */
public class DynamicCorsConfiguration extends CorsConfiguration {
  private final ClientConfigurationService clientConfigurationService;

  public DynamicCorsConfiguration(ClientConfigurationService clientConfigurationService) {
    this.clientConfigurationService = clientConfigurationService;
  }

  /**
   * Called by Spring for each CORS request to validate the {@code Origin} header. Returns the origin
   * if allowed, or {@code null} if not.
   *
   * @param requestOrigin the origin from the request's {@code Origin} header
   * @return the origin if allowed, otherwise {@code null}
   */
  @Override
  public String checkOrigin(String requestOrigin) {
    if (requestOrigin == null) {
      return null;
    }
    Set<String> allowedOrigins = clientConfigurationService.getAllAuthorizedOrigins();
    if (allowedOrigins.contains(requestOrigin)) {
      return requestOrigin;
    }
    return null;
  }

  public void applyDefaultSettings() {
    setAllowedMethods(List.of("GET", "POST"));
    setAllowedHeaders(List.of("*"));
    setAllowCredentials(!clientConfigurationService.getAllAuthorizedOrigins().contains("*"));
  }
}
