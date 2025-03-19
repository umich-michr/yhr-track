package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.SiteConfiguration;

import java.util.Set;

public interface SiteConfigurationService {
  SiteConfiguration getSiteConfiguration(String siteId);
  Set<String> getAllAuthorizedDomains();
}
