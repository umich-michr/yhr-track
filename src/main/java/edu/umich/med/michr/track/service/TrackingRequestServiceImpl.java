package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.TrackingRequest;
import edu.umich.med.michr.track.repository.TrackingRequestRepository;
import edu.umich.med.michr.track.exception.ValidationException;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class TrackingRequestServiceImpl implements TrackingRequestService {

  private final TrackingRequestRepository requestRepository;
  private final OriginValidator originValidator;
  private final Clock clock;

  @Inject
  public TrackingRequestServiceImpl(
      TrackingRequestRepository requestRepository,
      OriginValidator originValidator,
      Clock clock) {
    this.requestRepository = requestRepository;
    this.originValidator = originValidator;
    this.clock = clock;
  }

  @Override
  public void processTrackingRequest(
      Map<String , String> params,
      String userId,
      String userAgent,
      String browserLanguage,
      String cookieLanguage,
      HttpServletRequest request) throws ValidationException {

    final String siteId = getRequiredRequestParam(params, "site-id");
    final String pageUrl = getRequiredRequestParam(params, "page-url");

    if (!originValidator.isValid(request, siteId)) {
      throw new ValidationException("Invalid origin for site ID", HttpStatus.FORBIDDEN);
    }

    final TrackingRequest trackingRequest = TrackingRequest.builder(siteId, pageUrl, LocalDateTime.now(clock))
        .withUserId(userId)
        .withUserAgent(userAgent)
        .withBrowserLanguage(browserLanguage)
        .withCookieLanguage(cookieLanguage)
        .withIpAddress(extractIpAddress(request))
        .build();

    requestRepository.save(trackingRequest);
  }

  private String getRequiredRequestParam(Map<String, String> params, String key) {
    final String value = params.get(key);
    if (value == null) {
      throw new ValidationException("Missing required parameter: " + key, HttpStatus.BAD_REQUEST);
    }
    return value;
  }

  private String extractIpAddress(HttpServletRequest request) {
    return Stream.of("X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP")
        .map(request::getHeader)
        .filter(this::isValidIp)
        .findFirst()
        .orElse(request.getRemoteAddr());
  }

  private boolean isValidIp(String ip) {
    return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
  }
}
