package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.ClientConfiguration;
import edu.umich.med.michr.track.domain.StandardParameter;
import edu.umich.med.michr.track.exception.ValidationException;
import edu.umich.med.michr.track.util.RequestUtil;
import edu.umich.med.michr.track.util.TestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OriginValidatorImpl Tests")
class OriginValidatorImplTest {

  @Mock
  private ClientConfigurationService clientConfigService;
  @Mock
  private RequestUtil requestUtil;
  @Mock
  private HttpServletRequest request;
  @InjectMocks
  private OriginValidatorImpl originValidator;

  private final String clientId = "client1";
  private final String clientName = "client1-name";

  @Nested
  @DisplayName("Standard Parameter Validation")
  class StandardParameterValidation {
    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should throw exception when clientId is missing")
    void testValidate_clientIdIsMissing(String clientId) {
      // Arrange
      when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request)).thenReturn(clientId);

      // Act & Assert
      assertThatThrownBy(() -> originValidator.validate(request))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("Origin ID could not be found")
          .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("Origin Header Validation")
  class OriginHeaderValidation {

    @BeforeEach
    void setupStandardParams() {
      when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request)).thenReturn(clientId);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should throw exception when Origin header is missing")
    void testValidate_originHeaderMissing(String originHeader) {
      // Arrange
      when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn(originHeader);

      // Act & Assert
      assertThatThrownBy(() -> originValidator.validate(request))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("Origin header is not set")
          .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("Site Configuration Validation")
  class ClientConfigurationValidation {
    @BeforeEach
    void setup() {
      when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request)).thenReturn(clientId);
      when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn("https://allowed.com");
    }

    @Test
    @DisplayName("Should throw exception when ClientConfiguration is not found")
    void testValidate_clientConfigurationNotFound() {
      // Arrange
      when(clientConfigService.getClientConfiguration(clientId)).thenReturn(null);

      // Act & Assert
      assertThatThrownBy(() -> originValidator.validate(request))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("No allowed origins configuration found")
          .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @SuppressWarnings("HttpUrlsUsage")
    @ParameterizedTest
    @ValueSource(strings = {"https://notallowed.com", "http://unauthorized.org"})
    @DisplayName("Should throw exception when origin is not allowed")
    void testValidate_originNotAllowed(String originHeader) {
      // Arrange
      when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn(originHeader);
      // Create a config with one allowed domain
      ClientConfiguration config = TestUtils.createClientConfig(clientId, clientName, "https://allowed.com", "https://allowed2.com");
      when(clientConfigService.getClientConfiguration(clientId)).thenReturn(config);

      // Act & Assert
      assertThatThrownBy(() -> originValidator.validate(request))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("Origin is not allowed")
          .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Should pass validation when origin is allowed")
    void testValidate() {
      // Arrange
      when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn("https://allowed.com/page");
      ClientConfiguration config = TestUtils.createClientConfig(clientId, clientName, "https://allowed.com", "https://another.com");
      when(clientConfigService.getClientConfiguration(clientId)).thenReturn(config);

      // Act & Assert - No exception thrown
      assertDoesNotThrow(() -> originValidator.validate(request));
      verify(clientConfigService).getClientConfiguration(clientId);
    }
  }

  @Nested
  @DisplayName("Edge Case Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle case-insensitive origin matching")
    void testValidate_caseInsensitiveOriginMatching() {
      // Arrange
      when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request)).thenReturn(clientId);
      when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn("https://ExAMPLE.com");
      ClientConfiguration config = TestUtils.createClientConfig(clientId, clientName, "https://exaMple.com");
      when(clientConfigService.getClientConfiguration(clientId)).thenReturn(config);

      // Act & Assert - Should throw because of case mismatch
      assertThatThrownBy(() -> originValidator.validate(request))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("Origin is not allowed");
    }

    @SuppressWarnings("HttpUrlsUsage")
    @ParameterizedTest
    @CsvSource({"https://example.com, http://example.com","http://example.com, https://example.com"})
    @DisplayName("Should handle protocol differences correctly")
    void testValidate_protocolDifferencesCorrectly(String originHeader, String allowedOrigin) {
      // Arrange
      when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request)).thenReturn(clientId);
      when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn(originHeader);
      ClientConfiguration config = TestUtils.createClientConfig(clientId, clientName, allowedOrigin);
      when(clientConfigService.getClientConfiguration(clientId)).thenReturn(config);

      // Act & Assert - Should throw because of protocol mismatch
      assertThatThrownBy(() -> originValidator.validate(request))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("Origin is not allowed");
    }
  }
}
