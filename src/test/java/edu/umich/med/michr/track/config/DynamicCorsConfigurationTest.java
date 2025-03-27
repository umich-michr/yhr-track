package edu.umich.med.michr.track.config;

import edu.umich.med.michr.track.config.cors.DynamicCorsConfiguration;
import edu.umich.med.michr.track.service.ClientConfigurationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamicCorsConfigurationTest {

  @Mock
  private ClientConfigurationService clientConfigurationService;

  @InjectMocks
  private DynamicCorsConfiguration dynamicCorsConfiguration;

  private final Set<String> allowedOrigins = new HashSet<>();

  @Test
  void testCheckOrigin_nullOrigin() {
    assertNull(dynamicCorsConfiguration.checkOrigin(null), "Should return null for null origin");
  }

  @Test
  void testCheckOrigin_originAllowed() {
    when(clientConfigurationService.getAllAuthorizedOrigins()).thenReturn(allowedOrigins);
    String allowedOrigin = "example.com";
    allowedOrigins.add(allowedOrigin);

    final String actual = dynamicCorsConfiguration.checkOrigin(allowedOrigin);

    assertEquals(allowedOrigin, actual, "Should return the origin if it's allowed");
  }

  @Test
  void testCheckOrigin_originNotAllowed() {
    String notAllowedOrigin = "malicious.com";

    final String actual = dynamicCorsConfiguration.checkOrigin(notAllowedOrigin);

    assertNull(actual, "Should return null if the origin is not allowed");
  }

  @Test
  void testApplyDefaultSettings_setsAllowedMethods() {
    dynamicCorsConfiguration.applyDefaultSettings();

    final List<String> actual = dynamicCorsConfiguration.getAllowedMethods();

    assertEquals(Arrays.asList("GET", "POST"), actual, "Allowed methods should be GET and POST");
  }

  @Test
  void testApplyDefaultSettings_setsAllowedHeaders() {
    dynamicCorsConfiguration.applyDefaultSettings();

    final List<String> actual = dynamicCorsConfiguration.getAllowedHeaders();

    assertEquals(List.of("*"), actual, "Allowed headers should be wildcard");
  }

  @Test
  void testApplyDefaultSettings_allowCredentialsFalseWhenWildcardAllowed() {
    when(clientConfigurationService.getAllAuthorizedOrigins()).thenReturn(allowedOrigins);
    allowedOrigins.add("*");
    dynamicCorsConfiguration.applyDefaultSettings();

    final Boolean actual = dynamicCorsConfiguration.getAllowCredentials();

    assertNotEquals(Boolean.TRUE, actual, "Allow credentials should be false when wildcard is an allowed origin");
  }

  @Test
  void testApplyDefaultSettings_allowCredentialsTrueWhenWildcardNotAllowed() {
    allowedOrigins.add("example.com");
    dynamicCorsConfiguration.applyDefaultSettings();

    final Boolean actual = dynamicCorsConfiguration.getAllowCredentials();

    assertEquals(Boolean.TRUE, actual, "Allow credentials should be true when wildcard is not an allowed origin");
  }

  @Test
  void testApplyDefaultSettings_allowCredentialsTrueWhenNoAllowedOrigins() {
    allowedOrigins.clear();
    dynamicCorsConfiguration.applyDefaultSettings();

    Boolean actual = dynamicCorsConfiguration.getAllowCredentials();

    assertEquals(Boolean.TRUE, actual, "Allow credentials should be true when there are no allowed origins");
  }
}
