package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.SiteConfiguration;
import edu.umich.med.michr.track.repository.SiteConfigurationRepository;
import jakarta.inject.Inject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SiteConfigurationServiceImpl implements SiteConfigurationService {

  private final SiteConfigurationRepository repository;
  private Map<String, SiteConfiguration> siteConfigCache;

  @Inject
  public SiteConfigurationServiceImpl(SiteConfigurationRepository repository) {
    this.repository = repository;
  }

  @Override
  public SiteConfiguration getSiteConfiguration(String siteId) {
    initCacheIfNeeded();
    return siteConfigCache.get(siteId);
  }

  @Override
  public Set<String> getAllAuthorizedDomains() {
    initCacheIfNeeded();

    return siteConfigCache.values().stream()
        .flatMap(config -> config.getAuthorizedDomains().stream())
        .map(domain -> "https://" + domain)
        .collect(Collectors.toSet());
  }

  private void initCacheIfNeeded() {
    if (siteConfigCache == null) {
      List<SiteConfiguration> allConfigs = repository.findAll();
      siteConfigCache = new ConcurrentHashMap<>();

      for (SiteConfiguration config : allConfigs) {
        siteConfigCache.put(config.getSiteId(), config);
      }
    }
  }
}
