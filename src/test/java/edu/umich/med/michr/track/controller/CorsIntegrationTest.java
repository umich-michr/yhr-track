package edu.umich.med.michr.track.controller;

import edu.umich.med.michr.track.service.SiteConfigurationService;
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

  // Define test configuration that will be loaded before the main context
  @TestConfiguration
  static class CorsTestConfig {

    @Bean
    @Primary
    public SiteConfigurationService mockSiteConfigService() {
      SiteConfigurationService mockService = mock(SiteConfigurationService.class);

      // Configure the mock during bean creation
      when(mockService.getAllAuthorizedDomains()).thenReturn(TestUtils.createAuthorizedDomains());
      when(mockService.getSiteConfiguration(TestUtils.VALID_SITE_ID))
          .thenReturn(TestUtils.createSiteConfig(TestUtils.VALID_SITE_ID, "Test Site", "example.com", "test-app.org"));

      return mockService;
    }
  }

  @Autowired
  private MockMvc mockMvc;

  private static final String ENDPOINT = "/analytics/requests";
  private static final String ALLOWED_ORIGIN = "https://example.com";
  private static final String UNAUTHORIZED_ORIGIN = "https://unauthorized.com";

  @ParameterizedTest(name = "Should allow preflight requests from authorized domains using: {0}")
  @ValueSource(strings = {"GET", "POST"})
  void shouldAllowPreflightRequestsFromAuthorizedDomains(String method) throws Exception {
    // Arrange & Act
    ResultActions result = mockMvc.perform(options(ENDPOINT)
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
    ResultActions result = mockMvc.perform(options(ENDPOINT)
        .header(HttpHeaders.ORIGIN, UNAUTHORIZED_ORIGIN)
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"));

    // Assert
    result
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Should allow actual requests from authorized domains")
  void shouldAllowActualRequestsFromAuthorizedDomains() throws Exception {
    // Arrange & Act
    ResultActions result = mockMvc.perform(get(ENDPOINT)
        .header("Referer", ALLOWED_ORIGIN)
        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
        .param("site-id", TestUtils.VALID_SITE_ID)
        .param("page-url", TestUtils.VALID_PAGE_URL));

    // Assert
    result
        .andExpect(status().isNoContent())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
  }

  @Test
  @DisplayName("Should reject actual requests from unauthorized domains")
  void shouldRejectActualRequestsFromUnauthorizedDomains() throws Exception {
    // Arrange & Act
    ResultActions result = mockMvc.perform(get(ENDPOINT)
        .header(HttpHeaders.ORIGIN, UNAUTHORIZED_ORIGIN)
        .param("site-id", TestUtils.VALID_SITE_ID)
        .param("page-url", TestUtils.VALID_PAGE_URL));

    // Assert
    result
        .andExpect(status().isForbidden());
  }
}
