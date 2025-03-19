package edu.umich.med.michr.track.repository;

import edu.umich.med.michr.track.domain.SiteConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteConfigurationRepository extends JpaRepository<SiteConfiguration, String> {
  SiteConfiguration findBySiteId(String siteId);
}
