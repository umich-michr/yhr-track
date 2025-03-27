package edu.umich.med.michr.track.controller;

import edu.umich.med.michr.track.service.ClientConfigurationService;
import edu.umich.med.michr.track.util.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for CORS configuration.
 * Uses TestConfiguration to ensure mocks are configured before Spring context is initialized.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CORS Integration Tests")
class CorsIntegrationTest {

  private static final String CLIENT_ID = "test-client";

  // Define test configuration that will be loaded before the main context
  @TestConfiguration
  static class CorsTestConfig {

    @Bean
    @Primary
    public ClientConfigurationService mockClientConfigService() {
      ClientConfigurationService mockService = mock(ClientConfigurationService.class);

      // Configure the mock during bean creation
      when(mockService.getAllAuthorizedOrigins()).thenReturn(Set.of(
          "https://example.com",
          "https://test-app.org",
          "https://your-tracked-app.com"
      ));
      when(mockService.getClientConfiguration(CLIENT_ID))
          .thenReturn(TestUtils.createClientConfig(CLIENT_ID, "Test Client", "https://example.com", "https://test-app.org"));

      return mockService;
    }
  }

  @Autowired
  private MockMvc mockMvc;

  private static final String ENDPOINT = "/analytics/events";
  private static final String ALLOWED_ORIGIN = "https://example.com";
  private static final String UNAUTHORIZED_ORIGIN = "https://unauthorized.com";

  @ParameterizedTest(name = "Should allow preflight requests from authorized domains using: {0}")
  @ValueSource(strings = {"GET", "POST"})
  void shouldAllowPreflightRequestsFromAuthorizedDomains(String method) throws Exception {
    // Arrange & Act
    final ResultActions result = mockMvc.perform(options(ENDPOINT)
        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method)
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"));

    // Assert
    result
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
        .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
        .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
  }

  @Test
  @DisplayName("Should reject preflight requests from unauthorized domains")
  void shouldRejectPreflightRequestsFromUnauthorizedDomains() throws Exception {
    // Arrange & Act
    final ResultActions result = mockMvc.perform(options(ENDPOINT)
        .header(HttpHeaders.ORIGIN, UNAUTHORIZED_ORIGIN)
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"));

    // Assert
    result.andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Should allow actual requests from authorized domains")
  void shouldAllowActualRequestsFromAuthorizedDomains() throws Exception {
    // Arrange & Act
    final ResultActions result = mockMvc.perform(get(ENDPOINT)
        .header("Referer", ALLOWED_ORIGIN)
        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
        .param("client-id", CLIENT_ID)
        .param("user-id", "test-user")
        .param("event-type", "event")
        .param("page", "https://example.com/page"));

    // Assert
    result
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
  }

  @Test
  @DisplayName("Should reject actual requests from unauthorized domains")
  void shouldRejectActualRequestsFromUnauthorizedDomains() throws Exception {
    // Arrange & Act
    final ResultActions result = mockMvc.perform(get(ENDPOINT)
        .header(HttpHeaders.ORIGIN, UNAUTHORIZED_ORIGIN)
        .param("client-id", CLIENT_ID)
        .param("page", "https://example.com/page"));

    // Assert
    result.andExpect(status().isForbidden());
  }
}
