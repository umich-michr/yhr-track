package edu.umich.med.michr.track.repository;

import edu.umich.med.michr.track.domain.TrackingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackingRequestRepository extends JpaRepository<TrackingRequest, Long> {
}
