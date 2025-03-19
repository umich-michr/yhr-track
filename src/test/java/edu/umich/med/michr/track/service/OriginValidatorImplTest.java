package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.SiteConfiguration;
import edu.umich.med.michr.track.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Origin Validator Tests")
class OriginValidatorImplTest {

  @Mock
  private SiteConfigurationService siteConfigService;

  @InjectMocks
  private OriginValidatorImpl originValidator;

  private MockHttpServletRequest request;
  private SiteConfiguration siteConfig;
  private final String siteId = "test-site";

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    siteConfig = TestUtils.createSiteConfig(siteId, "Test Site", "example.com", "test-app.org");
  }

  @Nested
  @DisplayName("When site configuration exists")
  class WithValidSiteConfiguration {

    @ParameterizedTest(name = "Should validate origin from {0}")
    @CsvSource({
        "https://example.com/page, true",
        "https://test-app.org/analytics?page=home, true",
        "https://malicious-site.com/fake, false",
        "https://blog.example.com/post, false"
    })
    @DisplayName("Domain validation scenarios")
    void shouldValidateOriginDomains(String origin, boolean expected) {
      // Arrange
      when(siteConfigService.getSiteConfiguration(siteId)).thenReturn(siteConfig);
      request.addHeader(HttpHeaders.ORIGIN, origin);

      // Act
      boolean result = originValidator.isValid(request, siteId);

      // Assert
      assertEquals(expected, result,
          () -> expected ? "Should accept origin: " + origin :
              "Should reject origin: " + origin);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should reject missing or empty origin")
    void shouldRejectMissingOrEmptyOrigin(String origin) {
      // Arrange
      if (origin != null) {
        request.addHeader("Origin", origin);
      }

      // Act
      boolean result = originValidator.isValid(request, siteId);

      // Assert
      assertFalse(result, "Should reject when origin is missing or empty");
    }
  }

  @Nested
  @DisplayName("When site configuration is missing")
  class WithMissingSiteConfiguration {

    @BeforeEach
    void configureMissingSiteConfig() {
      when(siteConfigService.getSiteConfiguration(siteId)).thenReturn(null);
    }

    @Test
    @DisplayName("Should reject any origin when site config is not found")
    void shouldRejectOriginWhenSiteConfigNotFound() {
      // Arrange
      request.addHeader("Origin", "https://example.com/page");

      // Act
      boolean result = originValidator.isValid(request, siteId);

      // Assert
      assertFalse(result, "Should reject when site configuration is not found");
    }
  }
}
