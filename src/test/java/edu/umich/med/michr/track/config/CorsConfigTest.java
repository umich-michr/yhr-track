package edu.umich.med.michr.track.config;

import edu.umich.med.michr.track.service.SiteConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CORS Configuration Tests")
class CorsConfigTest {

  @Mock
  private SiteConfigurationService siteConfigService;

  @Mock
  private CorsRegistry corsRegistry;

  @Mock
  private CorsRegistration corsRegistration;

  @InjectMocks
  private CorsConfig corsConfig;

  @Captor
  private ArgumentCaptor<String[]> originsCaptor;

  private final Set<String> testDomains = Set.of(
      "https://test-app.org",
      "https://example.com"
  );

  private final String ENDPOINT_PATH = "/analytics/requests";

  @BeforeEach
  void setUp() {
    when(siteConfigService.getAllAuthorizedDomains()).thenReturn(testDomains);
  }

  @Test
  @DisplayName("Should configure CORS registry with proper mappings")
  void shouldConfigureCorsRegistry() {
    // Arrange
    when(corsRegistry.addMapping(eq(ENDPOINT_PATH))).thenReturn(corsRegistration);
    when(corsRegistration.allowedOrigins(any(String[].class))).thenReturn(corsRegistration);
    when(corsRegistration.allowedMethods(any(String[].class))).thenReturn(corsRegistration);
    when(corsRegistration.allowedHeaders(any(String[].class))).thenReturn(corsRegistration);
    when(corsRegistration.allowCredentials(anyBoolean())).thenReturn(corsRegistration);

    // Act
    corsConfig.addCorsMappings(corsRegistry);

    // Assert
    verify(corsRegistry).addMapping(ENDPOINT_PATH);

    verify(corsRegistration).allowedOrigins(originsCaptor.capture());
    String[] capturedOrigins = originsCaptor.getValue();
    assertEquals(testDomains.size(), capturedOrigins.length);
    assertTrue(testDomains.containsAll(Set.of(capturedOrigins)));

    verify(corsRegistration).allowedMethods(eq("GET"), eq("POST"));
    verify(corsRegistration).allowedHeaders(eq("*"));
    verify(corsRegistration).allowCredentials(eq(true));
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
    assertTrue(Objects.requireNonNull(config.getAllowedOrigins()).containsAll(testDomains));
    assertTrue(Objects.requireNonNull(config.getAllowedMethods()).contains("GET"));
    assertTrue(config.getAllowedMethods().contains("POST"));
    assertTrue(Objects.requireNonNull(config.getAllowedHeaders()).contains("*"));
    assertEquals(Boolean.TRUE, config.getAllowCredentials());
  }

  @Test
  @DisplayName("Should handle empty domains list gracefully")
  void shouldHandleEmptyDomainsListGracefully() {
    // Arrange
    when(siteConfigService.getAllAuthorizedDomains()).thenReturn(Collections.emptySet());

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

  @Test
  @DisplayName("Should handle wildcard domain properly")
  void shouldHandleWildcardDomainProperly() {
    // Arrange
    when(siteConfigService.getAllAuthorizedDomains()).thenReturn(Set.of("*"));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(ENDPOINT_PATH);

    // Act
    CorsConfigurationSource source = corsConfig.corsConfigurationSource();
    CorsConfiguration config = source.getCorsConfiguration(request);

    // Assert
    assertNotNull(config);
    assertTrue(Objects.requireNonNull(config.getAllowedOrigins()).contains("*"),
        "Should include wildcard in allowed origins");

    // With wildcard, we cannot use allowCredentials=true as per CORS spec
    assertNotEquals(Boolean.TRUE, config.getAllowCredentials(), "Should not allow credentials when using wildcard origin");


    // Arrange
    when(corsRegistry.addMapping(eq(ENDPOINT_PATH))).thenReturn(corsRegistration);
    when(corsRegistration.allowedOrigins(any(String[].class))).thenReturn(corsRegistration);
    when(corsRegistration.allowedMethods(any(String[].class))).thenReturn(corsRegistration);
    when(corsRegistration.allowedHeaders(any(String[].class))).thenReturn(corsRegistration);
    when(corsRegistration.allowCredentials(anyBoolean())).thenReturn(corsRegistration);

    // Act
    corsConfig.addCorsMappings(corsRegistry);

    verify(corsRegistry).addMapping(ENDPOINT_PATH);

    verify(corsRegistration).allowedOrigins(originsCaptor.capture());
    String[] capturedOrigins = originsCaptor.getValue();
    assertEquals(1, capturedOrigins.length);
    assertEquals("*", capturedOrigins[0]);

    verify(corsRegistration).allowedMethods(eq("GET"), eq("POST"));
    verify(corsRegistration).allowedHeaders(eq("*"));
    verify(corsRegistration).allowCredentials(eq(false));
  }
}
