package edu.umich.med.michr.track.controller;

import edu.umich.med.michr.track.exception.GlobalExceptionHandler;
import edu.umich.med.michr.track.exception.ValidationException;
import edu.umich.med.michr.track.service.TrackingRequestService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tracking Controller Tests")
class TrackingControllerTest {
  // Test constants
  private static final String SITE_ID = "test-site-id";
  private static final String PAGE_URL = "https://test.com/page";
  private static final String USER_AGENT = "Mozilla/5.0 Test User Agent";
  private static final String LANGUAGE = "en-US";
  private static final String COOKIE_LANGUAGE = "en";
  private static final String USER_ID = "user-123";
  private static final String IP_ADDRESS = "192.168.0.1";
  private static final String ENDPOINT = "/analytics/requests";

  private MockMvc mockMvc;

  @Mock
  private TrackingRequestService trackingService;

  @InjectMocks
  private TrackingController trackingController;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(trackingController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .alwaysDo(result -> System.out.println("Response Headers: " + result.getResponse().getHeaderNames()))
        .build();
  }

  @Nested
  @DisplayName("GET request handling")
  class GetRequestTests {

    @Test
    @DisplayName("Should return 204 No Content for valid GET request")
    void shouldReturnNoContentForValidRequest() throws Exception {
      // Arrange & Act
      ResultActions result = mockMvc.perform(
          get(ENDPOINT)
              .header("User-Agent", USER_AGENT)
              .header("Accept-Language", LANGUAGE)
              .header("X-Forwarded-For", IP_ADDRESS)
              .cookie(
                  new Cookie("lang", COOKIE_LANGUAGE),
                  new Cookie("user-id", USER_ID)
              )
              .param("site-id", SITE_ID)
              .param("page-url", PAGE_URL)
      );

      // Assert
      result.andExpect(status().isNoContent());

      verify(trackingService).processTrackingRequest(
          eq(Map.of("site-id", SITE_ID, "page-url", PAGE_URL)),
          eq(USER_ID),
          eq(USER_AGENT),
          eq(LANGUAGE),
          eq(COOKIE_LANGUAGE),
          any(HttpServletRequest.class)
      );
    }

    @Test
    @DisplayName("Should return error status and message for invalid GET request")
    void shouldReturnErrorForInvalidRequest() throws Exception {
      // Arrange
      String errorMessage = "Test validation error";
      doThrow(new ValidationException(errorMessage, HttpStatus.BAD_REQUEST))
          .when(trackingService).processTrackingRequest(any(), any(), any(), any(), any(), any());

      // Act
      ResultActions result = mockMvc.perform(get(ENDPOINT));

      // Assert
      result
          .andExpect(status().isBadRequest())
          .andExpect(content().string(containsString(errorMessage)));

      verify(trackingService).processTrackingRequest(
          eq(Map.of()),
          eq(null),
          eq(null),
          eq(null),
          eq(null),
          any(HttpServletRequest.class)
      );
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error for generic exception")
    void shouldReturnInternalServerErrorForGenericException() throws Exception {
      // Arrange
      String errorMessage = "An unexpected error occurred";
      doThrow(new RuntimeException("Generic exception"))
          .when(trackingService).processTrackingRequest(any(), any(), any(), any(), any(), any());

      // Act
      ResultActions result = mockMvc.perform(get(ENDPOINT));

      // Assert
      result
          .andExpect(status().isInternalServerError())
          .andExpect(content().string(containsString(errorMessage)));

      verify(trackingService).processTrackingRequest(
          eq(Map.of()),
          eq(null),
          eq(null),
          eq(null),
          eq(null),
          any(HttpServletRequest.class)
      );
    }
  }

  @Nested
  @DisplayName("POST request handling")
  class PostRequestTests {

    @Test
    @DisplayName("Should return 204 No Content for valid POST request")
    void shouldReturnNoContentForValidRequest() throws Exception {
      // Arrange
      String payload = String.format(
          "{\"site-id\": \"%s\", \"page-url\": \"%s\"}",
          SITE_ID, PAGE_URL
      );

      // Act
      ResultActions result = mockMvc.perform(
          post(ENDPOINT)
              .contentType(MediaType.APPLICATION_JSON)
              .content(payload)
              .header("User-Agent", USER_AGENT)
              .header("Accept-Language", LANGUAGE)
              .cookie(
                  new Cookie("user-id", USER_ID),
                  new Cookie("lang", COOKIE_LANGUAGE)
              )
              .with(request -> {
                request.setRemoteAddr(IP_ADDRESS);
                return request;
              })
      );

      // Assert
      result.andExpect(status().isNoContent());

      verify(trackingService).processTrackingRequest(
          eq(Map.of("site-id", SITE_ID, "page-url", PAGE_URL)),
          eq(USER_ID),
          eq(USER_AGENT),
          eq(LANGUAGE),
          eq(COOKIE_LANGUAGE),
          any(HttpServletRequest.class)
      );
    }

    @Test
    @DisplayName("Should return error status and message for invalid POST request")
    void shouldReturnErrorForInvalidRequest() throws Exception {
      // Arrange
      String errorMessage = "Test validation error";
      String payload = String.format(
          "{\"site-id\": \"%s\", \"page-url\": \"%s\"}",
          SITE_ID, PAGE_URL
      );

      doThrow(new ValidationException(errorMessage, HttpStatus.BAD_REQUEST))
          .when(trackingService).processTrackingRequest(any(), any(), any(), any(), any(), any());

      // Act
      ResultActions result = mockMvc.perform(
          post(ENDPOINT)
              .contentType(MediaType.APPLICATION_JSON)
              .content(payload)
      );

      // Assert
      result
          .andExpect(status().isBadRequest())
          .andExpect(content().string(containsString(errorMessage)));

      verify(trackingService).processTrackingRequest(
          eq(Map.of("site-id", SITE_ID, "page-url", PAGE_URL)),
          eq(null),
          eq(null),
          eq(null),
          eq(null),
          any(HttpServletRequest.class)
      );
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error for generic exception")
    void shouldReturnInternalServerErrorForGenericException() throws Exception {
      // Arrange
      String errorMessage = "An unexpected error occurred";
      String payload = String.format(
          "{\"site-id\": \"%s\", \"page-url\": \"%s\"}",
          SITE_ID, PAGE_URL
      );

      doThrow(new RuntimeException("Generic exception"))
          .when(trackingService).processTrackingRequest(any(), any(), any(), any(), any(), any());

      // Act
      ResultActions result = mockMvc.perform(
          post(ENDPOINT)
              .contentType(MediaType.APPLICATION_JSON)
              .content(payload)
      );

      // Assert
      result
          .andExpect(status().isInternalServerError())
          .andExpect(content().string(containsString(errorMessage)));

      verify(trackingService).processTrackingRequest(
          eq(Map.of("site-id", SITE_ID, "page-url", PAGE_URL)),
          eq(null),
          eq(null),
          eq(null),
          eq(null),
          any(HttpServletRequest.class)
      );
    }
  }
}
