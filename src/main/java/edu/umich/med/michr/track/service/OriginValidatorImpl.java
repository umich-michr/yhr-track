package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.SiteConfiguration;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class OriginValidatorImpl implements OriginValidator {

  private final SiteConfigurationService siteConfigService;

  @Inject
  public OriginValidatorImpl(SiteConfigurationService siteConfigService) {
    this.siteConfigService = siteConfigService;
  }

  public boolean isValid(HttpServletRequest request, String siteId) {
    final String origin = request.getHeader(HttpHeaders.ORIGIN);
    if (origin == null || origin.isEmpty()) {
      return false;
    }

    final SiteConfiguration siteConfig = siteConfigService.getSiteConfiguration(siteId);
    if (siteConfig == null) {
      return false;
    }

   return siteConfig.getAuthorizedDomains().stream().anyMatch(authorizedDomain -> origin.startsWith("https://" + authorizedDomain));
  }
}
