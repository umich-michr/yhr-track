package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Service for handling tracking requests
 */
public interface TrackingRequestService {

  /**
   * Creates, validates and saves a tracking request
   *
   * @param params params from the request
   * @param userId The user identifier (from cookie)
   * @param userAgent The user agent
   * @param browserLanguage The browser language
   * @param cookieLanguage The language from cookie
   * @param request The HTTP request to extract IP and origin
   * @throws ValidationException if the request validation fails
   */
  void processTrackingRequest(
      Map<String, String> params,
      String userId,
      String userAgent,
      String browserLanguage,
      String cookieLanguage,
      HttpServletRequest request) throws ValidationException;
}
