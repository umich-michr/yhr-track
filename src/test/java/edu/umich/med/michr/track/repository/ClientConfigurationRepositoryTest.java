package edu.umich.med.michr.track.repository;

import edu.umich.med.michr.track.domain.ClientConfiguration;
import edu.umich.med.michr.track.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/client.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ClientConfigurationRepositoryTest {

  @Autowired
  private ClientConfigurationRepository clientConfigurationRepository;

  @Test
  void testSaveAndFindById() {
    final String expectedClientId = "client1";
    final String expectedClientName = "Client 1";
    final List<String> expectedAuthorizedDomains = Arrays.asList("example1.com", "test1.org");
    final ClientConfiguration config = TestUtils.createClientConfig(expectedClientId, expectedClientName, expectedAuthorizedDomains.toArray(new String[0]));

    final ClientConfiguration actual = clientConfigurationRepository.save(config);

    final Optional<ClientConfiguration> foundConfig = clientConfigurationRepository.findById(actual.getId());
    assertTrue(foundConfig.isPresent(), "ClientConfiguration should be present");
    assertEquals(expectedClientId, foundConfig.get().getId());
    assertEquals(expectedClientName, foundConfig.get().getName());
    assertEquals(expectedAuthorizedDomains, foundConfig.get().getAuthorizedOrigins());
  }

  @Test
  void testFindAll() {
    final List<ClientConfiguration> allConfigs = clientConfigurationRepository.findAll();

    assertEquals(3, allConfigs.size());
  }

  @Test
  void testDelete() {
    final String clientId = "client1";
    clientConfigurationRepository.deleteById(clientId);

    final Optional<ClientConfiguration> deletedConfig = clientConfigurationRepository.findById(clientId);

    assertFalse(deletedConfig.isPresent(), "ClientConfiguration should be deleted");
  }

  @Test
  void testUpdate() {
    final String client1 = "client1";
    final String expectedClientName = "Client Updated 1";
    final Optional<ClientConfiguration> config = clientConfigurationRepository.findById(client1);
    ReflectionTestUtils.setField(config.orElseThrow(), "name", expectedClientName);

    clientConfigurationRepository.save(config.orElseThrow());

    final Optional<ClientConfiguration> actual = clientConfigurationRepository.findById(client1);

    assertTrue(actual.isPresent(), "Updated ClientConfiguration should be found");
    assertEquals(expectedClientName, actual.get().getName());
  }
}
