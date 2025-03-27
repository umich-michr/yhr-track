package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.ClientConfiguration;
import edu.umich.med.michr.track.repository.ClientConfigurationRepository;
import edu.umich.med.michr.track.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientConfigurationService Tests")
class ClientConfigurationServiceImplTest {

  @Mock
  private ClientConfigurationRepository repository;

  @InjectMocks
  private ClientConfigurationServiceImpl service;

  private final String clientId1 = "client1";
  private final String clientId2 = "client2";
  private final Set<String> authorizedOrigins = Set.of("example1.com", "test1.org", "example2.com", "test2.org");

  @BeforeEach
  void setUp() {
    final ClientConfiguration clientConfig1 = TestUtils.createClientConfig(clientId1, "Client 1", "example1.com", "test1.org");
    final ClientConfiguration clientConfig2 = TestUtils.createClientConfig(clientId2, "Client 2", "example2.com", "test2.org");

    final List<ClientConfiguration> clientConfigurations = Arrays.asList(clientConfig1, clientConfig2);
    when(repository.findAll()).thenReturn(clientConfigurations);
  }

  @Test
  @DisplayName("Should return client configuration for existing client id")
  void testGetClientConfiguration_existingClientId() {
    // Act
    final ClientConfiguration actual = service.getClientConfiguration(clientId1);

    // Assert
    assertNotNull(actual);
    assertEquals(clientId1, actual.getId());
    assertEquals("Client 1", actual.getName());

    verify(repository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should return null for non-existing client id")
  void testGetClientConfiguration_nonExistingClientId() {
    // Act
    final ClientConfiguration actual = service.getClientConfiguration("non-existing-client");

    // Assert
    assertNull(actual);

    verify(repository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should use cache for subsequent calls")
  void testGetClientConfiguration_hitCacheForSubsequentCalls() {
    // First call to initialize cache
    final ClientConfiguration actual1 = service.getClientConfiguration(clientId1);

    // Second call should use cache
    final ClientConfiguration actual2 = service.getClientConfiguration(clientId2);

    assertEquals(clientId1, actual1.getId());
    assertEquals(clientId2, actual2.getId());

    verify(repository, times(1)).findAll();
  }

  @Test
  @DisplayName("Should return all the authorized origins for all clients")
  void testGetAllAuthorizedOrigins_existingClientId() {
    // Act
    final Set<String> actual = service.getAllAuthorizedOrigins();

    // Assert
    assertEquals(authorizedOrigins, actual);

    verify(repository, times(1)).findAll();
  }
}
