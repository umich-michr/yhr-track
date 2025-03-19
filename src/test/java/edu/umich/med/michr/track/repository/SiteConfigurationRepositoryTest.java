package edu.umich.med.michr.track.repository;

import edu.umich.med.michr.track.domain.SiteConfiguration;
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
@Sql(scripts = "/site.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class SiteConfigurationRepositoryIntegrationTest {

  @Autowired
  private SiteConfigurationRepository siteConfigurationRepository;

  @Test
  void testSaveAndFindById() {
    final String expectedSiteId = "site1";
    final String expectedSiteName = "Site 1";
    final List<String> expectedAuthorizedDomains = Arrays.asList("example1.com", "test1.org");
    final SiteConfiguration config = TestUtils.createSiteConfig(expectedSiteId, expectedSiteName, expectedAuthorizedDomains.toArray(new String[0]));

    final SiteConfiguration actual = siteConfigurationRepository.save(config);

    final Optional<SiteConfiguration> foundConfig = siteConfigurationRepository.findById(actual.getSiteId());
    assertTrue(foundConfig.isPresent(), "SiteConfiguration should be present");
    assertEquals(expectedSiteId, foundConfig.get().getSiteId());
    assertEquals(expectedSiteName, foundConfig.get().getSiteName());
    assertEquals(expectedAuthorizedDomains, foundConfig.get().getAuthorizedDomains());
  }

  @Test
  void testFindAll() {
    final List<SiteConfiguration> allConfigs = siteConfigurationRepository.findAll();

    assertEquals(3, allConfigs.size());
  }

  @Test
  void testDelete() {
    final String siteId = "site1";
    siteConfigurationRepository.deleteById(siteId);

    final Optional<SiteConfiguration> deletedConfig = siteConfigurationRepository.findById(siteId);

    assertFalse(deletedConfig.isPresent(), "SiteConfiguration should be deleted");
  }

  @Test
  void testUpdate() {
    final String siteId = "site1";
    final String expectedSiteName = "Site Updated 1";
    final SiteConfiguration config = siteConfigurationRepository.findBySiteId(siteId);
    ReflectionTestUtils.setField(config, "siteName", expectedSiteName);

    siteConfigurationRepository.save(config);

    final Optional<SiteConfiguration> actual = siteConfigurationRepository.findById(siteId);

    assertTrue(actual.isPresent(), "Updated SiteConfiguration should be found");
    assertEquals(expectedSiteName, actual.get().getSiteName());
  }
}
