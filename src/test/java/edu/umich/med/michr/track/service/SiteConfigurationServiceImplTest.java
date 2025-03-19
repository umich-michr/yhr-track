package edu.umich.med.michr.track.service;

import edu.umich.med.michr.track.domain.SiteConfiguration;
import edu.umich.med.michr.track.repository.SiteConfigurationRepository;
import edu.umich.med.michr.track.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SiteConfigurationService Tests")
class SiteConfigurationServiceImplTest {

  @Mock
  private SiteConfigurationRepository repository;

  private SiteConfigurationServiceImpl service;
  private final String siteId1 = "site1";
  private final String siteId2 = "site2";

  @BeforeEach
  void setUp() {
    final SiteConfiguration siteConfig1 = TestUtils.createSiteConfig(siteId1, "Site 1", "example1.com", "test1.org");
    final SiteConfiguration siteConfig2 = TestUtils.createSiteConfig(siteId2, "Site 2", "example2.com", "test2.org");

    final List<SiteConfiguration> siteConfigurations = Arrays.asList(siteConfig1, siteConfig2);
    when(repository.findAll()).thenReturn(siteConfigurations);

    service = new SiteConfigurationServiceImpl(repository);
  }

  @Nested
  @DisplayName("Site configuration retrieval")
  class SiteConfigurationRetrieval {

    @Test
    @DisplayName("Should return site configuration for existing site ID")
    void shouldReturnSiteConfigurationForExistingSiteId() {
      // Act
      SiteConfiguration result = service.getSiteConfiguration(siteId1);

      // Assert
      assertNotNull(result);
      assertEquals(siteId1, result.getSiteId());
      assertEquals("Site 1", result.getSiteName());

      verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return null for non-existing site ID")
    void shouldReturnNullForNonExistingSiteId() {
      // Act
      SiteConfiguration result = service.getSiteConfiguration("non-existing-site");

      // Assert
      assertNull(result);

      verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should use cache for subsequent calls")
    void shouldUseCacheForSubsequentCalls() {
      // First call to initialize cache
      SiteConfiguration firstResult = service.getSiteConfiguration(siteId1);

      // Second call should use cache
      SiteConfiguration secondResult = service.getSiteConfiguration(siteId2);

      assertEquals(siteId1, firstResult.getSiteId());
      assertEquals(siteId2, secondResult.getSiteId());

      verify(repository, times(1)).findAll();
    }
  }
}
