package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.AnalyticsEvent;
import edu.umich.med.michr.track.domain.StandardParameter;
import edu.umich.med.michr.track.exception.ValidationException;
import edu.umich.med.michr.track.repository.AnalyticsEventRepository;
import edu.umich.med.michr.track.util.RequestUtil;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

@Service
public class AnalyticsEventServiceImpl implements AnalyticsEventService {

  private final AnalyticsEventRepository repository;
  private final RequestUtil requestUtil;
  private final Clock clock;

  @Inject
  public AnalyticsEventServiceImpl(AnalyticsEventRepository repository, RequestUtil requestUtil, Clock clock) {
    this.repository = repository;
    this.requestUtil = requestUtil;
    this.clock = clock;
  }

  public AnalyticsEvent createAnalyticsEvent(HttpServletRequest request) {
    final String userAgent = request.getHeader("User-Agent");
    final String browserLanguage = request.getHeader("Accept-Language");

    final String clientId = getAndValidateParameter(StandardParameter.CLIENT_ID, request);
    final String userId = getAndValidateParameter(StandardParameter.USER_ID, request);
    final String eventType = getAndValidateParameter(StandardParameter.EVENT_TYPE, request);
    final String page = getAndValidateParameter(StandardParameter.PAGE, request);

    final Map<String, String> customAttributes = requestUtil.extractCustomAttributes(request);

    final String ipAddress = requestUtil.extractIpAddress(request);

    return AnalyticsEvent.builder(clientId, userId, eventType, page, Instant.now(clock))
        .ipAddress(ipAddress)
        .userAgent(userAgent)
        .browserLanguage(browserLanguage)
        .customAttributes(customAttributes)
        .build();
  }

  @Transactional
  public void processAndSaveEvent(HttpServletRequest request) {
    repository.save(createAnalyticsEvent(request));
  }

  private String getAndValidateParameter(StandardParameter param, HttpServletRequest request) {
    String value = requestUtil.getParameterValue(param, request);
    if (value == null || value.isEmpty()) {
      throw new ValidationException("Required param " + param.name() + " is missing", HttpStatus.BAD_REQUEST);
    }
    return value;
  }
}
