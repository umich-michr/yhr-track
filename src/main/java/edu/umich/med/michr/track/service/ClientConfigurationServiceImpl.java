package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.ClientConfiguration;
import edu.umich.med.michr.track.repository.ClientConfigurationRepository;
import jakarta.inject.Inject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ClientConfigurationServiceImpl implements ClientConfigurationService {

  private final ClientConfigurationRepository repository;
  private Map<String, ClientConfiguration> clientConfigCache;

  @Inject
  public ClientConfigurationServiceImpl(ClientConfigurationRepository repository) {
    this.repository = repository;
  }

  @Override
  public ClientConfiguration getClientConfiguration(String id) {
    initCacheIfNeeded();
    return clientConfigCache.get(id);
  }

  @Override
  public Set<String> getAllAuthorizedOrigins() {
    initCacheIfNeeded();

    return clientConfigCache.values().stream()
        .flatMap(config -> config.getAuthorizedOrigins().stream())
        .collect(Collectors.toSet());
  }

  private void initCacheIfNeeded() {
    if (clientConfigCache == null || clientConfigCache.isEmpty()) {
      List<ClientConfiguration> allConfigs = repository.findAll();
      clientConfigCache = new ConcurrentHashMap<>();

      for (ClientConfiguration config : allConfigs) {
        clientConfigCache.put(config.getId(), config);
      }
    }
  }
}
