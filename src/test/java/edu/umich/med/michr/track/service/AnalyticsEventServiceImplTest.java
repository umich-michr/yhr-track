package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.AnalyticsEvent;
import edu.umich.med.michr.track.domain.StandardParameter;
import edu.umich.med.michr.track.exception.ValidationException;
import edu.umich.med.michr.track.repository.AnalyticsEventRepository;
import edu.umich.med.michr.track.util.RequestUtil;
import edu.umich.med.michr.track.util.TestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsEventServiceImpl Tests")
class AnalyticsEventServiceImplTest {

  @Mock
  private AnalyticsEventRepository repository;

  @Mock
  private RequestUtil requestUtil;

  @Mock
  private HttpServletRequest request;

  private AnalyticsEventServiceImpl service;

  private final String clientId = "client123";
  private final String userId = "user123";
  private final String eventType = "pageView";
  private final String page = "home";
  private final String userAgent = "TestUserAgent";
  private final String ipAddress = "127.0.0.1";
  private final String browserLanguage = "en-US";
  private final Map<String, String> customAttributes = Map.of("attr1", "value1");

  @BeforeEach
  void setUp() {
    service = new AnalyticsEventServiceImpl(repository, requestUtil, TestUtils.FIXED_CLOCK);
  }

  @Nested
  @DisplayName("createAnalyticsEvent() Tests")
  class CreateAnalyticsEventTests {

    @BeforeEach
    void setupValidRequest() {
      when(request.getHeader("User-Agent")).thenReturn(userAgent);
      when(request.getHeader("Accept-Language")).thenReturn(browserLanguage);
      when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request)).thenReturn(clientId);
    }

    @Test
    @DisplayName("Should create a valid AnalyticsEvent when all parameters are present")
    void shouldCreateValidAnalyticsEvent() {
      // Arrange
      when(requestUtil.getParameterValue(StandardParameter.USER_ID, request)).thenReturn(userId);
      when(requestUtil.getParameterValue(StandardParameter.EVENT_TYPE, request)).thenReturn(eventType);
      when(requestUtil.getParameterValue(StandardParameter.PAGE, request)).thenReturn(page);
      when(requestUtil.extractIpAddress(request)).thenReturn(ipAddress);
      when(requestUtil.extractCustomAttributes(request)).thenReturn(customAttributes);

      // Act
      final AnalyticsEvent actual = service.createAnalyticsEvent(request);

      // Assert
      assertEquals(clientId, actual.getClientId());
      assertEquals(userId, actual.getUserId());
      assertEquals(eventType, actual.getEventType());
      assertEquals(page, actual.getPage());
      assertEquals(userAgent, actual.getUserAgent());
      assertEquals(browserLanguage, actual.getBrowserLanguage());
      assertEquals(ipAddress, actual.getIpAddress());
      assertEquals(customAttributes, actual.getCustomAttributes());
      assertEquals(TestUtils.FIXED_INSTANT, actual.getEventTimestamp());
    }

    //specifying PER_CLASS allow sharing setup data between tests and also non-static method source declarations for the same reason.
    @Nested
    @DisplayName("Validation Tests for Required Standard Parameters")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ValidationTests {

      Stream<org.junit.jupiter.params.provider.Arguments> missingStandardParameters() {
        return Stream.of(
            Arguments.of(StandardParameter.CLIENT_ID, null),
            Arguments.of(StandardParameter.CLIENT_ID, ""),
            Arguments.of(StandardParameter.USER_ID, null),
            Arguments.of(StandardParameter.USER_ID, ""),
            Arguments.of(StandardParameter.EVENT_TYPE, null),
            Arguments.of(StandardParameter.EVENT_TYPE, ""),
            Arguments.of(StandardParameter.PAGE, null),
            Arguments.of(StandardParameter.PAGE, "")
        );
      }

      @ParameterizedTest(name = "Should throw ValidationException when {0} is \"{1}\"")
      @MethodSource("missingStandardParameters")
      void shouldThrowExceptionForMissingStandardParameter(StandardParameter param, String missingValue) {
        // Arrange: stub RequestUtil.getParameterValue() for the tested parameter.
        when(requestUtil.getParameterValue(param, request)).thenReturn(missingValue);
        // For other parameters, return valid values.
        lenient().when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request))
            .thenReturn(param == StandardParameter.CLIENT_ID ? missingValue : clientId);
        lenient().when(requestUtil.getParameterValue(StandardParameter.USER_ID, request))
            .thenReturn(param == StandardParameter.USER_ID ? missingValue : userId);
        lenient().when(requestUtil.getParameterValue(StandardParameter.EVENT_TYPE, request))
            .thenReturn(param == StandardParameter.EVENT_TYPE ? missingValue : eventType);
        lenient().when(requestUtil.getParameterValue(StandardParameter.PAGE, request))
            .thenReturn(param == StandardParameter.PAGE ? missingValue : page);

        // Act & Assert: Expect a ValidationException indicating the missing parameter.
        assertThatThrownBy(() -> service.createAnalyticsEvent(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining(param.name())
            .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
      }
    }
  }

  @Nested
  @DisplayName("processAndSaveEvent() Tests")
  class ProcessAndSaveEventTests {

    @Captor
    private ArgumentCaptor<AnalyticsEvent> eventCaptor;

    @BeforeEach
    void setupValidRequest() {
      when(request.getHeader("User-Agent")).thenReturn(userAgent);
      when(request.getHeader("Accept-Language")).thenReturn(browserLanguage);

      when(requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request)).thenReturn(clientId);
      when(requestUtil.getParameterValue(StandardParameter.USER_ID, request)).thenReturn(userId);
      when(requestUtil.getParameterValue(StandardParameter.EVENT_TYPE, request)).thenReturn(eventType);
      when(requestUtil.getParameterValue(StandardParameter.PAGE, request)).thenReturn(page);

      when(requestUtil.extractCustomAttributes(request)).thenReturn(customAttributes);
      when(requestUtil.extractIpAddress(request)).thenReturn(ipAddress);
    }

    @Test
    @DisplayName("Should process and save a valid AnalyticsEvent")
    void shouldProcessAndSaveAnalyticsEvent() {
      // Act
      service.processAndSaveEvent(request);

      // Assert
      verify(repository, times(1)).save(eventCaptor.capture());

      final AnalyticsEvent actual = eventCaptor.getValue();

      assertEquals(clientId, actual.getClientId());
      assertEquals(userId, actual.getUserId());
      assertEquals(eventType, actual.getEventType());
      assertEquals(page, actual.getPage());
      assertEquals(userAgent, actual.getUserAgent());
      assertEquals(browserLanguage, actual.getBrowserLanguage());
      assertEquals(ipAddress, actual.getIpAddress());
      assertEquals(customAttributes, actual.getCustomAttributes());
      assertEquals(TestUtils.FIXED_INSTANT, actual.getEventTimestamp());
    }
  }
}
