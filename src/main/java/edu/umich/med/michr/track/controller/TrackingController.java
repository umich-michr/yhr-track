package edu.umich.med.michr.track.controller;

import edu.umich.med.michr.track.service.TrackingRequestService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/analytics/requests")
public class TrackingController {

  private final TrackingRequestService trackingService;

  @Inject
  public TrackingController(TrackingRequestService trackingService) {
    this.trackingService = trackingService;
  }

  /**
   * Handles GET requests for tracking analytics events.
   * Typically used for image pixel tracking or simple link clicks.
   */
  @GetMapping
  public ResponseEntity<String> trackViaGet(
      @RequestParam Map<String, String> params,
      @RequestHeader(value = "User-Agent", required = false) String userAgent,
      @RequestHeader(value = "Accept-Language", required = false) String language,
      @CookieValue(value = "lang", required = false) String preferredLanguage,
      @CookieValue(value = "user-id", required = false) String userId,
      HttpServletRequest request) {

    return processTrackingRequest(params, userId, userAgent, language, preferredLanguage, request);
  }

  /**
   * Handles POST requests for tracking analytics events.
   * Typically used for form submissions or more complex tracking scenarios.
   */
  @PostMapping(consumes = "application/json")
  public ResponseEntity<String> trackViaPost(
      @RequestBody Map<String, String> params,
      @RequestHeader(value = "User-Agent", required = false) String userAgent,
      @RequestHeader(value = "Accept-Language", required = false) String language,
      @CookieValue(value = "lang", required = false) String preferredLanguage,
      @CookieValue(value = "user-id", required = false) String userId,
      HttpServletRequest request) {

    return processTrackingRequest(params, userId, userAgent, language, preferredLanguage, request);
  }

  /**
   * Common logic for processing tracking requests.
   * Extracts this shared logic to avoid code duplication between GET and POST handlers.
   */
  private ResponseEntity<String> processTrackingRequest(
      Map<String, String> params,
      String userId,
      String userAgent,
      String language,
      String preferredLanguage,
      HttpServletRequest request) {

      trackingService.processTrackingRequest(
          params,
          userId,
          userAgent,
          language,
          preferredLanguage,
          request
      );

    return ResponseEntity.noContent().build();
  }
}
