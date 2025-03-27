package edu.umich.med.michr.track.repository;

import edu.umich.med.michr.track.domain.ClientConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientConfigurationRepository extends JpaRepository<ClientConfiguration, String> {}
