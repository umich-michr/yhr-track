package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.ClientConfiguration;
import edu.umich.med.michr.track.domain.StandardParameter;
import edu.umich.med.michr.track.exception.ValidationException;
import edu.umich.med.michr.track.util.RequestUtil;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OriginValidatorImpl implements OriginValidator {
  private static final Logger logger = LoggerFactory.getLogger(OriginValidatorImpl.class);

  private final ClientConfigurationService clientConfigurationService;
  private final RequestUtil requestUtil;

  @Inject
  public OriginValidatorImpl(ClientConfigurationService clientConfigurationService, RequestUtil requestUtil) {
    this.clientConfigurationService = clientConfigurationService;
    this.requestUtil = requestUtil;
  }

  @Override
  public void validate(HttpServletRequest request) {
    final String clientId = validateClientId(request);
    String resolvedOrigin = resolveOrigin(request);
    final ClientConfiguration clientConfiguration = getClientConfiguration(clientId);
    validateResolvedOrigin(resolvedOrigin, clientConfiguration);
  }

  private String validateClientId(HttpServletRequest request) {
    final String clientId = requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request);
    if (clientId == null || clientId.isEmpty()) {
      throw new ValidationException("Origin ID could not be found in the request parameter, cannot authorize", HttpStatus.FORBIDDEN);
    }
    return clientId;
  }

  private ClientConfiguration getClientConfiguration(String clientId) {
    final ClientConfiguration clientConfiguration = clientConfigurationService.getClientConfiguration(clientId);
    if (clientConfiguration == null) {
      throw new ValidationException("No allowed origins configuration found for the origin, can not authorize requests", HttpStatus.FORBIDDEN);
    }
    return clientConfiguration;
  }

  private String resolveOrigin(HttpServletRequest request) {
    return getOriginFromHeader(request)
        .or(() -> getOriginFromReferer(request))
        .or(() -> getOriginFromEmailId(request))
        .map(origin -> origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin)
        .orElseThrow(() -> new ValidationException("No Origin or Referer header found in the request", HttpStatus.FORBIDDEN));
  }

  private Optional<String> getOriginFromHeader(HttpServletRequest request) {
    String origin = request.getHeader(HttpHeaders.ORIGIN);
    return (origin != null && !origin.isEmpty()) ? Optional.of(origin) : Optional.empty();
  }

  private Optional<String> getOriginFromReferer(HttpServletRequest request) {
    String referer = request.getHeader(HttpHeaders.REFERER);
    if (referer != null && !referer.isEmpty()) {
      logger.debug("Origin header missing, using Referer header: {}", referer);
      return Optional.of(referer);
    }
    return Optional.empty();
  }

  private Optional<String> getOriginFromEmailId(HttpServletRequest request) {
    if (request.getMethod().equals(HttpMethod.GET.name())) {
      final String emailId = requestUtil.getParameterValue(StandardParameter.EMAIL_ID, request);
      if (emailId != null && !emailId.isEmpty()) {
        logger.debug("Using email-id request parameter for request authorization: {}", emailId);
        return Optional.of(emailId);
      }
    }
    return Optional.empty();
  }

  private void validateResolvedOrigin(String resolvedOrigin, ClientConfiguration clientConfiguration) {
    if (clientConfiguration.getAuthorizedOrigins().stream().noneMatch(resolvedOrigin::equalsIgnoreCase)) {
      logger.debug("Resolved origin doesn't correspond to the authorized origin parameters, rejected the request: {}", resolvedOrigin);
      throw new ValidationException("Origin is not allowed", HttpStatus.FORBIDDEN);
    }
  }
}
