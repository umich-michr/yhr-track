package edu.umich.med.michr.track.controller;

import edu.umich.med.michr.track.exception.GlobalExceptionHandler;
import edu.umich.med.michr.track.exception.ValidationException;
import edu.umich.med.michr.track.service.AnalyticsEventService;
import edu.umich.med.michr.track.service.OriginValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasLength;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AnalyticsEventController.class, GlobalExceptionHandler.class})
@DisplayName("Analytics Event Controller Tests")
class AnalyticsEventControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @SuppressWarnings("unused")
  @MockitoBean
  private OriginValidator originValidator;

  @SuppressWarnings("unused")
  @MockitoBean
  private AnalyticsEventService analyticsEventService;

  @Nested
  @DisplayName("Success path tests")
  class SuccessPathTests {

    @Test
    @DisplayName("POST endpoint should return 204 No Content on success")
    void postEndpointShouldReturn204OnSuccess() throws Exception {
      // Arrange
      doNothing().when(originValidator).validate(any());
      doNothing().when(analyticsEventService).processAndSaveEvent(any());

      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.add("client-id", "test-client");

      // Act & Assert
      mockMvc.perform(post("/analytics/events")
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .params(formData))
          .andExpect(status().isNoContent())
          .andExpect(header().doesNotExist("Content-Type"));

      verify(originValidator).validate(any());
      verify(analyticsEventService).processAndSaveEvent(any());
    }

    @Test
    @DisplayName("GET endpoint should return 1x1 GIF with correct headers")
    void getEndpointShouldReturn1x1GifWithCorrectHeaders() throws Exception {
      // Arrange
      doNothing().when(originValidator).validate(any());
      doNothing().when(analyticsEventService).processAndSaveEvent(any());

      byte[] expectedGif = new byte[] {
          (byte)0x47, (byte)0x49, (byte)0x46, (byte)0x38, (byte)0x39, (byte)0x61,
          (byte)0x01, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x80, (byte)0x00,
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0xff,
          (byte)0xff, (byte)0x21, (byte)0xf9, (byte)0x04, (byte)0x01, (byte)0x00,
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x2c, (byte)0x00, (byte)0x00,
          (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x01, (byte)0x00,
          (byte)0x00, (byte)0x02, (byte)0x02, (byte)0x44, (byte)0x01, (byte)0x00,
          (byte)0x3b
      };

      // Act & Assert
      mockMvc.perform(get("/analytics/events")
              .param("client-id", "test-client"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.IMAGE_GIF))
          .andExpect(content().bytes(expectedGif))
          .andExpect(header().string("Cache-Control", containsString("no-cache")))
          .andExpect(header().exists("ETag"))
          .andExpect(header().string("Pragma", "no-cache"))
          .andExpect(header().string("Expires", "Thu, 01 Jan 1970 00:00:00 GMT"));

      verify(originValidator).validate(any());
      verify(analyticsEventService).processAndSaveEvent(any());
    }

    @Test
    @DisplayName("GET response should contain exactly 43 bytes for the GIF")
    void getResponseShouldContainExactly43BytesForTheGif() throws Exception {
      // Arrange
      doNothing().when(originValidator).validate(any());
      doNothing().when(analyticsEventService).processAndSaveEvent(any());

      // Act & Assert
      mockMvc.perform(get("/analytics/events")
              .param("client-id", "test-client"))
          .andExpect(status().isOk())
          .andExpect(content().string(hasLength(43)));
    }
  }

  @Nested
  @DisplayName("ValidationException handling tests")
  class ValidationExceptionTests {

    @Test
    @DisplayName("ValidationException from origin validator should return proper error response for POST")
    void validationExceptionFromOriginValidatorShouldReturnProperErrorResponseForPost() throws Exception {
      // Arrange
      String errorMessage = "Invalid origin";
      HttpStatus errorStatus = HttpStatus.FORBIDDEN;
      doThrow(new ValidationException(errorMessage, errorStatus)).when(originValidator).validate(any());

      // Act & Assert
      mockMvc.perform(post("/analytics/events")
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .param("client-id", "invalid-origin"))
          .andExpect(status().isForbidden())
          .andExpect(content().string(errorMessage));

      verify(originValidator).validate(any());
      verify(analyticsEventService, never()).processAndSaveEvent(any());
    }

    @Test
    @DisplayName("ValidationException from origin validator should return proper error response for GET")
    void validationExceptionFromOriginValidatorShouldReturnProperErrorResponseForGet() throws Exception {
      // Arrange
      String errorMessage = "Missing required parameter";
      HttpStatus errorStatus = HttpStatus.BAD_REQUEST;
      doThrow(new ValidationException(errorMessage, errorStatus)).when(originValidator).validate(any());

      // Act & Assert
      mockMvc.perform(get("/analytics/events"))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(errorMessage));

      verify(originValidator).validate(any());
      verify(analyticsEventService, never()).processAndSaveEvent(any());
    }

    @Test
    @DisplayName("ValidationException from analytics service should return proper error response")
    void validationExceptionFromAnalyticsServiceShouldReturnProperErrorResponse() throws Exception {
      // Arrange
      String errorMessage = "Missing event type";
      HttpStatus errorStatus = HttpStatus.BAD_REQUEST;
      doNothing().when(originValidator).validate(any());
      doThrow(new ValidationException(errorMessage, errorStatus)).when(analyticsEventService).processAndSaveEvent(any());

      // Act & Assert
      mockMvc.perform(get("/analytics/events")
              .param("client-id", "test-client"))
          .andExpect(status().isBadRequest())
          .andExpect(content().string(errorMessage));

      verify(originValidator).validate(any());
      verify(analyticsEventService).processAndSaveEvent(any());
    }
  }

  @Nested
  @DisplayName("Generic exception handling tests")
  class GenericExceptionTests {

    @Test
    @DisplayName("RuntimeException should be handled with 500 Internal Server Error")
    void runtimeExceptionShouldBeHandledWith500InternalServerError() throws Exception {
      // Arrange
      String errorMessage = "Database connection failed";
      doNothing().when(originValidator).validate(any());
      doThrow(new RuntimeException(errorMessage)).when(analyticsEventService).processAndSaveEvent(any());

      // Act & Assert
      mockMvc.perform(post("/analytics/events")
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .param("client-id", "test-client"))
          .andExpect(status().isInternalServerError())
          .andExpect(content().string(containsString("An unexpected error occurred")))
          .andExpect(content().string(containsString(errorMessage)));

      verify(originValidator).validate(any());
      verify(analyticsEventService).processAndSaveEvent(any());
    }

    @Test
    @DisplayName("NullPointerException should be handled with 500 Internal Server Error")
    void nullPointerExceptionShouldBeHandledWith500InternalServerError() throws Exception {
      // Arrange
      String errorMessage = "Null reference";
      doThrow(new NullPointerException(errorMessage)).when(originValidator).validate(any());

      // Act & Assert
      mockMvc.perform(get("/analytics/events")
              .param("client-id", "test-client"))
          .andExpect(status().isInternalServerError())
          .andExpect(content().string(containsString("An unexpected error occurred")))
          .andExpect(content().string(containsString(errorMessage)));

      verify(originValidator).validate(any());
      verify(analyticsEventService, never()).processAndSaveEvent(any());
    }
  }

  @Nested
  @DisplayName("Content type and request validation")
  class ContentTypeAndRequestValidationTests {

    @Test
    @DisplayName("POST with incorrect content type should return 415 Unsupported Media Type")
    void postWithIncorrectContentTypeShouldReturn415UnsupportedMediaType() throws Exception {
      // Act & Assert
      mockMvc.perform(post("/analytics/events")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"clientId\": \"test-client\"}"))
          .andExpect(status().isUnsupportedMediaType());

      verifyNoInteractions(originValidator);
      verifyNoInteractions(analyticsEventService);
    }

    @Test
    @DisplayName("POST with missing content type should return 415 Unsupported Media Type")
    void postWithMissingContentTypeShouldReturn415UnsupportedMediaType() throws Exception {
      // Act & Assert
      mockMvc.perform(post("/analytics/events"))
          .andExpect(status().isUnsupportedMediaType());

      verifyNoInteractions(originValidator);
      verifyNoInteractions(analyticsEventService);
    }
  }
}
