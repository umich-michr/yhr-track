package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.TrackingRequest;
import edu.umich.med.michr.track.repository.TrackingRequestRepository;
import edu.umich.med.michr.track.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackingRequestService Tests")
class TrackingRequestServiceImplTest {

  private static final String FIXED_TEST_TIME = "2025-03-13T10:15:30.00Z";
  private static final String FIXED_TEST_TIME_ZONE = "UTC";

  @Mock
  private OriginValidator originValidator;

  @Mock
  private TrackingRequestRepository requestRepository;

  @Captor
  private ArgumentCaptor<TrackingRequest> trackingRequestCaptor;

  private final Clock fixedClock = Clock.fixed(Instant.parse(FIXED_TEST_TIME), ZoneId.of(FIXED_TEST_TIME_ZONE));

  private MockHttpServletRequest request;

  private TrackingRequestServiceImpl trackingRequestService;

  private Map<String, String> params;
  private static final String SITE_ID = "test-site-id";
  private final String IP_ADDRESS = "192.168.1.1";
  private final String USER_ID = "80b74d08-8c22-4f47-aeb5-297aab5be3be";
  private final String USER_AGENT = "mozilla";
  private static final String PAGE_URL = "https://test.com/page";
  private final String COOKIE_LANGUAGE = "en";
  private final String BROWSER_LANGUAGE = "en-GB";

  @BeforeEach
  void setup() {
    request = new MockHttpServletRequest();
    request.addHeader("X-Forwarded-For", IP_ADDRESS);

    params = new HashMap<>();
    params.put("site-id", SITE_ID);
    params.put("page-url", PAGE_URL);

    trackingRequestService = new TrackingRequestServiceImpl(requestRepository, originValidator, fixedClock);
  }

  @Nested
  @DisplayName("Successful tracking request processing")
  class SuccessfulProcessing {
    @BeforeEach
    void setup() {
      when(originValidator.isValid(any(HttpServletRequest.class), eq(SITE_ID))).thenReturn(true);
    }

    @Test
    @DisplayName("Should save tracking request with all parameters")
    void shouldSaveTrackingRequestWithAllParameters() {
      // Act
      trackingRequestService.processTrackingRequest(
          params,
          USER_ID,
          USER_AGENT,
          BROWSER_LANGUAGE,
          COOKIE_LANGUAGE,
          request);

      // Assert
      verify(requestRepository).save(trackingRequestCaptor.capture());
      TrackingRequest capturedRequest = trackingRequestCaptor.getValue();

      assertEquals(SITE_ID, capturedRequest.getSiteId());
      assertEquals(PAGE_URL, capturedRequest.getPageUrl());
      assertEquals(USER_ID, capturedRequest.getUserId());
      assertEquals(USER_AGENT, capturedRequest.getUserAgent());
      assertEquals(BROWSER_LANGUAGE, capturedRequest.getBrowserLanguage());
      assertEquals(COOKIE_LANGUAGE, capturedRequest.getCookieLanguage());
      assertEquals(IP_ADDRESS, capturedRequest.getIpAddress());
      assertEquals(LocalDateTime.now(fixedClock), capturedRequest.getTimestamp());
    }

    @Test
    @DisplayName("Should handle null user parameters")
    void shouldHandleNullUserParameters() {
      // Act
      trackingRequestService.processTrackingRequest(
          params,
          null,
          null,
          null,
          null,
          request);

      // Assert
      verify(requestRepository).save(trackingRequestCaptor.capture());
      TrackingRequest capturedRequest = trackingRequestCaptor.getValue();

      assertEquals(SITE_ID, capturedRequest.getSiteId());
      assertEquals(PAGE_URL, capturedRequest.getPageUrl());
      assertNull(capturedRequest.getUserId());
      assertNull(capturedRequest.getUserAgent());
      assertNull(capturedRequest.getBrowserLanguage());
      assertNull(capturedRequest.getCookieLanguage());
      assertEquals(IP_ADDRESS, capturedRequest.getIpAddress());
    }
  }

  @Nested
  @DisplayName("Validation exceptions")
  class ValidationExceptionTests {
    @Test
    @DisplayName("Should throw exception when page-url is missing")
    void shouldThrowExceptionWhenPageUrlIsMissing() {
      // Arrange
      params.remove("page-url");

      // Act & Assert
      ValidationException exception = assertThrows(ValidationException.class, () ->
          trackingRequestService.processTrackingRequest(
              params,
              USER_ID,
              USER_AGENT,
              BROWSER_LANGUAGE,
              COOKIE_LANGUAGE,
              request)
      );

      assertEquals("Missing required parameter: page-url", exception.getMessage());
      assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
      verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when parameters map is empty")
    void shouldThrowExceptionWhenParametersMapIsEmpty() {
      // Act & Assert
      ValidationException exception = assertThrows(ValidationException.class, () ->
          trackingRequestService.processTrackingRequest(
              new HashMap<>(),
              USER_ID,
              USER_AGENT,
              BROWSER_LANGUAGE,
              COOKIE_LANGUAGE,
              request)
      );

      assertEquals("Missing required parameter: site-id", exception.getMessage());
      verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when origin is invalid")
    void shouldThrowExceptionWhenOriginIsInvalid() {
      // Arrange
      when(originValidator.isValid(any(HttpServletRequest.class), eq(SITE_ID))).thenReturn(false);

      // Act & Assert
      ValidationException exception = assertThrows(ValidationException.class, () ->
          trackingRequestService.processTrackingRequest(
              params,
              USER_ID,
              USER_AGENT,
              BROWSER_LANGUAGE,
              COOKIE_LANGUAGE,
              request)
      );

      assertEquals("Invalid origin for site ID", exception.getMessage());
      assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
      verify(requestRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("IP Address extraction tests")
  class IpAddressExtractionTests {
    @BeforeEach
    void setup() {
      when(originValidator.isValid(any(HttpServletRequest.class), eq(SITE_ID))).thenReturn(true);
    }

    @ParameterizedTest
    @CsvSource({
        "X-Forwarded-For, 10.0.0.1",
        "Proxy-Client-IP, 10.1.1.1",
        "WL-Proxy-Client-IP, 10.2.2.2"
    })
    @DisplayName("Should extract IP from different headers")
    void shouldExtractIpFromVariousHeaders(String headerName, String ipValue) {
      // Arrange
      request = new MockHttpServletRequest();
      request.addHeader(headerName, ipValue);

      // Act
      trackingRequestService.processTrackingRequest(
          params,
          USER_ID,
          USER_AGENT,
          BROWSER_LANGUAGE,
          COOKIE_LANGUAGE,
          request);

      // Assert
      verify(requestRepository).save(trackingRequestCaptor.capture());
      TrackingRequest capturedRequest = trackingRequestCaptor.getValue();
      assertEquals(ipValue, capturedRequest.getIpAddress());
    }

    @Test
    @DisplayName("Should extract IP from remote address when headers are missing")
    void shouldExtractIpFromRemoteAddr() {
      // Arrange
      request = new MockHttpServletRequest();
      request.setRemoteAddr("127.0.0.1");

      // Act
      trackingRequestService.processTrackingRequest(
          params,
          USER_ID,
          USER_AGENT,
          BROWSER_LANGUAGE,
          COOKIE_LANGUAGE,
          request);

      // Assert
      verify(requestRepository).save(trackingRequestCaptor.capture());
      TrackingRequest capturedRequest = trackingRequestCaptor.getValue();
      assertEquals("127.0.0.1", capturedRequest.getIpAddress());
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", ""})
    @DisplayName("Should ignore headers with 'unknown' or empty values")
    void shouldIgnoreUnknownOrEmptyHeaderValues(String invalidValue) {
      // Arrange
      request = new MockHttpServletRequest();
      request.addHeader("X-Forwarded-For", invalidValue);
      request.addHeader("Proxy-Client-IP", invalidValue);
      request.addHeader("WL-Proxy-Client-IP", invalidValue);
      request.setRemoteAddr("192.168.0.1");

      // Act
      trackingRequestService.processTrackingRequest(
          params,
          USER_ID,
          USER_AGENT,
          BROWSER_LANGUAGE,
          COOKIE_LANGUAGE,
          request);

      // Assert
      verify(requestRepository).save(trackingRequestCaptor.capture());
      TrackingRequest capturedRequest = trackingRequestCaptor.getValue();
      assertEquals("192.168.0.1", capturedRequest.getIpAddress());
    }

    @Test
    @DisplayName("Should prioritize headers in correct order")
    void shouldPrioritizeHeadersInCorrectOrder() {
      // Arrange
      request = new MockHttpServletRequest();
      request.addHeader("X-Forwarded-For", "1.1.1.1");
      request.addHeader("Proxy-Client-IP", "2.2.2.2");
      request.addHeader("WL-Proxy-Client-IP", "3.3.3.3");
      request.setRemoteAddr("4.4.4.4");

      // Act
      trackingRequestService.processTrackingRequest(
          params,
          USER_ID,
          USER_AGENT,
          BROWSER_LANGUAGE,
          COOKIE_LANGUAGE,
          request);

      // Assert
      verify(requestRepository).save(trackingRequestCaptor.capture());
      TrackingRequest capturedRequest = trackingRequestCaptor.getValue();
      assertEquals("1.1.1.1", capturedRequest.getIpAddress(),
          "Should use X-Forwarded-For header first");
    }
  }
}
