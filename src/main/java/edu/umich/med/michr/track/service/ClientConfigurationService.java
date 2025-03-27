package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.ClientConfiguration;

import java.util.Set;

public interface ClientConfigurationService {
  ClientConfiguration getClientConfiguration(String id);
  Set<String> getAllAuthorizedOrigins();
}
