package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.ClientConfiguration;
import edu.umich.med.michr.track.domain.StandardParameter;
import edu.umich.med.michr.track.exception.ValidationException;
import edu.umich.med.michr.track.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OriginValidatorImplTest {

  @Mock
  private ClientConfigurationService clientConfigurationService;
  @Mock
  private RequestUtil requestUtil;
  @Mock
  private HttpServletRequest request;

  private OriginValidatorImpl originValidator;

  @BeforeEach
  public void setUp() {
    originValidator = new OriginValidatorImpl(clientConfigurationService, requestUtil);
  }

  @Nested
  @DisplayName("Client ID Validation Tests")
  class ClientIdValidationTests {
    @ParameterizedTest
    @NullAndEmptySource
    public void testValidate_clientIdMissing(String clientId) {
      when(requestUtil.getParameterValue(any(), eq(request))).thenReturn(clientId);

      ValidationException exception = assertThrows(ValidationException.class,
          () -> originValidator.validate(request));
      assertEquals("Origin ID could not be found in the request parameter, cannot authorize", exception.getMessage());
      assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    public void testValidate_clientConfigurationMissing() {
      String clientId = "client1";
      when(requestUtil.getParameterValue(eq(StandardParameter.CLIENT_ID), eq(request))).thenReturn(clientId);

      String originHeader = "https://example.com";
      when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn(originHeader);

      when(clientConfigurationService.getClientConfiguration(clientId)).thenReturn(null);

      ValidationException exception = assertThrows(ValidationException.class,
          () -> originValidator.validate(request));
      assertEquals("No allowed origins configuration found for the origin, can not authorize requests", exception.getMessage());
      assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }
  }

  @Nested
  @DisplayName("Origin Resolution Tests")
  class OriginResolutionTests {
    private final String clientId = "client1";

    @ParameterizedTest
    @ValueSource(strings = {"https://referer.com", "https://referer.com/", "https://refEreR.com/", "https://referer.COM"})
    public void testValidate_resolvesOriginFromRefererWhenOriginHeaderMissing(String referer) {
      when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request)).thenReturn(clientId);

      when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn(null);
      when(request.getHeader(HttpHeaders.REFERER)).thenReturn(referer);

      ClientConfiguration config = mock(ClientConfiguration.class);
      when(config.getAuthorizedOrigins()).thenReturn(Collections.singletonList("https://referer.com"));
      when(clientConfigurationService.getClientConfiguration(clientId)).thenReturn(config);

      assertDoesNotThrow(() -> originValidator.validate(request));
    }

    @Test
    public void testValidate_resolvesOriginFromEmailIdForGetRequest() {
      when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request)).thenReturn(clientId);

      when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn("");
      when(request.getHeader(HttpHeaders.REFERER)).thenReturn("");
      when(request.getMethod()).thenReturn(HttpMethod.GET.name());

      String emailId = "abc-45-jkf";
      when(requestUtil.getParameterValue(StandardParameter.EMAIL_ID, request)).thenReturn(emailId);

      ClientConfiguration config = mock(ClientConfiguration.class);
      when(config.getAuthorizedOrigins()).thenReturn(Collections.singletonList(emailId));
      when(clientConfigurationService.getClientConfiguration(clientId)).thenReturn(config);

      assertDoesNotThrow(() -> originValidator.validate(request));
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void testValidate_noOriginFound_GET(String origin) {
      when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request)).thenReturn(clientId);

      when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn(origin);
      when(request.getHeader(HttpHeaders.REFERER)).thenReturn(origin);
      when(requestUtil.getParameterValue(StandardParameter.EMAIL_ID, request)).thenReturn(origin);
      when(request.getMethod()).thenReturn(HttpMethod.GET.name());

      ValidationException exception = assertThrows(ValidationException.class,
          () -> originValidator.validate(request));
      assertEquals("No Origin or Referer header found in the request", exception.getMessage());
      assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void testValidate_noOriginFound_POST(String origin) {
      when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request)).thenReturn(clientId);

      when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn(origin);
      when(request.getHeader(HttpHeaders.REFERER)).thenReturn(origin);
      when(request.getMethod()).thenReturn(HttpMethod.POST.name());

      ValidationException exception = assertThrows(ValidationException.class,
          () -> originValidator.validate(request));
      assertEquals("No Origin or Referer header found in the request", exception.getMessage());
      assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }
  }

  @Nested
  @DisplayName("Authorized Origin Validation Tests")
  class AuthorizedOriginValidationTests {
    @Test
    public void testValidate_originNotAllowed() {
      String clientId = "client5";
      when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request)).thenReturn(clientId);

      final String origin = "https://notallowed.com";
      when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn(origin);

      ClientConfiguration config = mock(ClientConfiguration.class);
      when(config.getAuthorizedOrigins()).thenReturn(Collections.singletonList("https://allowed.com"));
      when(clientConfigurationService.getClientConfiguration(clientId)).thenReturn(config);

      ValidationException exception = assertThrows(ValidationException.class,
          () -> originValidator.validate(request));
      assertEquals("Origin is not allowed", exception.getMessage());
      assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }
  }
}
