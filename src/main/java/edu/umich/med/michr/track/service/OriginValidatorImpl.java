package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.ClientConfiguration;
import edu.umich.med.michr.track.domain.StandardParameter;
import edu.umich.med.michr.track.exception.ValidationException;
import edu.umich.med.michr.track.util.RequestUtil;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OriginValidatorImpl implements OriginValidator {

  private final ClientConfigurationService clientConfigurationService;
  private final RequestUtil requestUtil;

  @Inject
  public OriginValidatorImpl(ClientConfigurationService clientConfigurationService, RequestUtil requestUtil) {
    this.clientConfigurationService = clientConfigurationService;
    this.requestUtil = requestUtil;
  }

  public void validate(HttpServletRequest request) {
    final String clientId = requestUtil.getParameterValue(StandardParameter.CLIENT_ID, request);
    if(clientId == null || clientId.isEmpty()){
      throw new ValidationException("Origin ID could not be found in the request parameter, cannot authenticate", HttpStatus.FORBIDDEN);
    }

    final String origin = request.getHeader(HttpHeaders.ORIGIN);
    if (origin == null || origin.isEmpty()) {
      throw new ValidationException("Origin header is not set, can not authenticate the request", HttpStatus.FORBIDDEN);
    }

    final ClientConfiguration clientConfiguration = clientConfigurationService.getClientConfiguration(clientId);
    if (clientConfiguration == null) {
      throw new ValidationException("No allowed origins configuration found for the origin", HttpStatus.FORBIDDEN);
    }

   if(clientConfiguration.getAuthorizedOrigins().stream().noneMatch(origin::startsWith)){
     throw new ValidationException("Origin is not allowed", HttpStatus.FORBIDDEN);
   }
  }
}
