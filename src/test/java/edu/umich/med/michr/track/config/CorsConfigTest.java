package edu.umich.med.michr.track.config;

import edu.umich.med.michr.track.config.cors.CorsConfig;
import edu.umich.med.michr.track.service.ClientConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CORS Configuration Tests")
class CorsConfigTest {

  @Mock
  private ClientConfigurationService clientConfigurationService;

  @InjectMocks
  private CorsConfig corsConfig;

  private final Set<String> testDomains = Set.of(
      "https://test-app.org",
      "https://example.com"
  );

  private final String ENDPOINT_PATH = "/analytics/events";

  @BeforeEach
  void setUp() {
    when(clientConfigurationService.getAllAuthorizedOrigins()).thenReturn(testDomains);
  }

  @Test
  @DisplayName("Should provide proper CORS configuration source")
  void shouldProvideProperCorsConfigurationSource() {
    // Arrange
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(ENDPOINT_PATH);

    // Act
    CorsConfigurationSource source = corsConfig.corsConfigurationSource();
    CorsConfiguration config = source.getCorsConfiguration(request);

    // Assert
    assertNotNull(config);
    assertNull(config.getAllowedOrigins()); // allowed origins are dynamically checked in every request check CorsIntegrationTest
    assertTrue(Objects.requireNonNull(config.getAllowedMethods()).contains("GET"));
    assertTrue(config.getAllowedMethods().contains("POST"));
    assertTrue(Objects.requireNonNull(config.getAllowedHeaders()).contains("*"));
    assertEquals(Boolean.TRUE, config.getAllowCredentials());
  }

  @Test
  @DisplayName("Should handle empty domains list gracefully")
  void shouldHandleEmptyDomainsListGracefully() {
    // Arrange
    when(clientConfigurationService.getAllAuthorizedOrigins()).thenReturn(Collections.emptySet());

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(ENDPOINT_PATH);

    // Act
    CorsConfigurationSource source = corsConfig.corsConfigurationSource();
    CorsConfiguration config = source.getCorsConfiguration(request);

    // Assert
    assertNotNull(config);
    assertTrue(config.getAllowedOrigins() == null || config.getAllowedOrigins().isEmpty(),
        "With no domains, allowed origins should be empty");
    assertTrue(Objects.requireNonNull(config.getAllowedMethods()).contains("GET"));
    assertTrue(config.getAllowedMethods().contains("POST"));
  }
}
